import hashlib
import os
import re
import requests
import shutil
import subprocess
import time
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent if Path(__file__).resolve().parent.name == "scripts" else Path(
    r"C:\Users\evan\Documents\GitHub\Spiced-Cider")
PACK_DIR = REPO_ROOT / "pack"
PACKWIZ_MODS_DIR = PACK_DIR / "mods"
PRISM_MODS_DIR = Path(r"C:\Users\evan\AppData\Roaming\PrismLauncher\instances\Spiced Cider Dev\minecraft\mods")
prism_mod_cache = {}


def get_sha1(filepath):
    """Calculates the SHA-1 hash of a file for the Modrinth API."""
    hasher = hashlib.sha1()
    with open(filepath, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)
    return hasher.hexdigest()


def query_modrinth(sha1):
    """Queries Modrinth API to identify a jar file by its hash."""
    url = f"https://api.modrinth.com/v2/version_file/{sha1}?algorithm=sha1"
    try:
        resp = requests.get(url, timeout=5)
        if resp.status_code == 200:
            data = resp.json()
            return data.get("project_id"), data.get("id")  # project_id, version_id
    except:
        pass
    return None, None


def update_prism_cache(prism_jars):
    """Updates the cache of Prism jar SHA1s and Modrinth version IDs."""
    global prism_mod_cache
    current_filenames = set(prism_jars.keys())

    for filename in list(prism_mod_cache.keys()):
        if filename not in current_filenames:
            del prism_mod_cache[filename]

    for filename, jar_path in prism_jars.items():
        stat = jar_path.stat()

        if filename not in prism_mod_cache or prism_mod_cache[filename]['mtime'] != stat.st_mtime:
            if stat.st_size == 0:
                continue
            time.sleep(0.5)
            if stat.st_size != jar_path.stat().st_size:
                continue

            sha1 = get_sha1(jar_path)
            project_id, version_id = query_modrinth(sha1)
            prism_mod_cache[filename] = {
                'mtime': stat.st_mtime,
                'sha1': sha1,
                'project_id': project_id,
                'version_id': version_id
            }


def get_packwiz_state():
    """Reads all packwiz mods, extracting both filename and Modrinth version ID."""
    state = []
    if not PACKWIZ_MODS_DIR.exists():
        return state

    for f in PACKWIZ_MODS_DIR.iterdir():
        if f.suffix == '.jar':
            state.append({
                'path': f,
                'filename': f.name,
                'version_id': None
            })
        elif f.suffix in ['.toml', '.pw.toml']:
            try:
                content = f.read_text(encoding='utf-8')
                filename_match = re.search(r'filename\s*=\s*(["\'])(.*?)\1', content)
                version_match = re.search(r'version\s*=\s*(["\'])(.*?)\1', content)

                filename = filename_match.group(2) if filename_match else None
                version_id = version_match.group(2) if version_match else None

                if filename:
                    state.append({
                        'path': f,
                        'filename': filename,
                        'version_id': version_id
                    })
            except Exception as e:
                print(f"Error reading {f}: {e}")
    return state


def sync_loop():
    print("Watching for changes in Prism Launcher...")
    PACKWIZ_MODS_DIR.mkdir(parents=True, exist_ok=True)

    while True:
        try:
            prism_jars = {f.name: f for f in PRISM_MODS_DIR.iterdir() if f.is_file() and f.suffix == '.jar'}
            update_prism_cache(prism_jars)

            packwiz_state = get_packwiz_state()

            # DELETIONS: Mod exists in Packwiz, but was removed from Prism
            for pw_mod in packwiz_state:
                pw_filename = pw_mod['filename']
                pw_version = pw_mod['version_id']

                in_prism = pw_filename in prism_mod_cache

                if not in_prism and pw_version:
                    for p_cache in prism_mod_cache.values():
                        if p_cache['version_id'] == pw_version:
                            in_prism = True
                            break

                if not in_prism:
                    print(f"\n[-] Detected removal in Prism: {pw_filename}")
                    os.remove(pw_mod['path'])
                    subprocess.run(["packwiz", "refresh"], cwd=PACK_DIR, shell=True, stdout=subprocess.DEVNULL)
                    print(f"    -> Removed from Packwiz.")

            packwiz_state = get_packwiz_state()
            pw_filenames = {mod['filename'] for mod in packwiz_state}
            pw_versions = {mod['version_id'] for mod in packwiz_state if mod['version_id']}

            # ADDITIONS: Mod downloaded in Prism, but missing in Packwiz
            for p_filename, p_cache in prism_mod_cache.items():
                if p_filename in pw_filenames:
                    continue

                if p_cache['version_id'] and p_cache['version_id'] in pw_versions:
                    continue

                print(f"\n[+] Detected new mod in Prism: {p_filename}")

                if p_cache['project_id'] and p_cache['version_id']:
                    dl_url = f"https://modrinth.com/mod/{p_cache['project_id']}/version/{p_cache['version_id']}"
                    print(f"    Found on Modrinth! Importing: {dl_url}")
                    subprocess.run(["packwiz", "modrinth", "add", dl_url], cwd=PACK_DIR, shell=True)
                else:
                    print(f"    Not found on Modrinth (likely CurseForge). Adding as local override...")
                    jar_path = prism_jars[p_filename]
                    shutil.copy(jar_path, PACKWIZ_MODS_DIR / p_filename)
                    subprocess.run(["packwiz", "refresh"], cwd=PACK_DIR, shell=True, stdout=subprocess.DEVNULL)

        except Exception as e:
            print(f"Error during sync: {e}")

        time.sleep(2)


if __name__ == "__main__":
    sync_loop()
