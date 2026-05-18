import hashlib
import json
import os
import re
import shutil
import tkinter as tk
import zipfile
from pathlib import Path
from tkinter import messagebox

REPO_ROOT = Path(__file__).resolve().parent.parent if Path(__file__).resolve().parent.name == "scripts" else Path(
    r"C:\Users\evan\Documents\GitHub\Spiced-Cider")

PACKWIZ_RP_DIR = REPO_ROOT / "pack" / "resourcepacks"
PRISM_RP_DIR = Path(r"C:\Users\evan\AppData\Roaming\PrismLauncher\instances\Spiced Cider Dev\minecraft\resourcepacks")

CIDER_PACKS_DIR = REPO_ROOT / "ciderpacks"
MANIFEST_FILE = REPO_ROOT / "pack" / "config" / "spicedcider" / "spicedcider_manifest.json"
OVERRIDES_FILE = REPO_ROOT / "pack" / "config" / "resourcepackoverrides.json"
CACHE_DIR = REPO_ROOT / "mod" / "run" / ".spicedcider_cache"


def get_file_hash(filepath):
    """Calculates the SHA-256 hash of a file."""
    hasher = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)
    return hasher.hexdigest()


def get_valid_pack_names():
    """Reads Packwiz directory to find all valid pack names (stripping .zip)"""
    valid_names = set()
    if PACKWIZ_RP_DIR.exists():
        for f in PACKWIZ_RP_DIR.iterdir():
            if f.suffix == '.zip':
                valid_names.add(f.stem)
            elif f.suffix in ['.toml', '.pw.toml']:
                with open(f, 'r', encoding='utf-8') as toml_file:
                    content = toml_file.read()
                    match = re.search(r'filename\s*=\s*(["\'])(.*?)\1', content)
                    if match:
                        filename = match.group(2)
                        if filename.endswith('.zip'):
                            valid_names.add(filename[:-4])
                        else:
                            valid_names.add(filename)
    return valid_names


def extract_packs():
    try:
        print("Extracting resource packs...")
        CIDER_PACKS_DIR.mkdir(parents=True, exist_ok=True)
        PRISM_RP_DIR.mkdir(parents=True, exist_ok=True)

        valid_names = get_valid_pack_names()

        for cider_pack in CIDER_PACKS_DIR.iterdir():
            if cider_pack.is_dir() and cider_pack.name not in valid_names:
                print(f"  Removing stale extracted folder: {cider_pack.name}/")
                shutil.rmtree(cider_pack)

        for pack_name in valid_names:
            zip_path = PRISM_RP_DIR / f"{pack_name}.zip"
            target_dir = CIDER_PACKS_DIR / pack_name

            if zip_path.exists() and not target_dir.exists():
                print(f"  Unzipping {zip_path.name} -> {pack_name}/")
                with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                    zip_ref.extractall(target_dir)
            elif not zip_path.exists():
                print(f"  WARNING: Missing physical zip in Prism: {zip_path.name}. Launch the game to download it.")

        print("Done extracting!")
        messagebox.showinfo("Success", "Extraction and cleanup complete!")
    except Exception as e:
        messagebox.showerror("Error", f"An error occurred during extraction:\n{e}")


def update_overrides(valid_names):
    """Updates resourcepackoverrides.json to sync with currently available packs."""
    if not OVERRIDES_FILE.exists():
        print("  WARNING: resourcepackoverrides.json not found. Skipping overrides update.")
        return

    print("  Syncing resourcepackoverrides.json...")

    with open(OVERRIDES_FILE, 'r', encoding='utf-8') as f:
        data = json.load(f)

    valid_file_entries = [f"file/{name}.zip" for name in valid_names]
    valid_jit_entries = [f"spicedcider_{name}_jit" for name in valid_names]

    all_assigned_packs = set()
    for key, value in data.get("pack_overrides", {}).items():
        if isinstance(value, list):
            all_assigned_packs.update(value)

    list_1 = data.get("pack_overrides", {}).get("1", [])
    new_list_1 = [x for x in list_1 if not x.startswith("file/") or x in valid_file_entries]

    for entry in valid_file_entries:
        if entry not in all_assigned_packs and entry not in new_list_1:
            new_list_1.append(entry)
            all_assigned_packs.add(entry)
            print(f"    Added to 1: {entry}")

    data["pack_overrides"]["1"] = new_list_1

    list_3 = data.get("pack_overrides", {}).get("3", [])
    new_list_3 = [x for x in list_3 if
                  not (x.startswith("spicedcider_") and x.endswith("_jit")) or x in valid_jit_entries]

    for entry in valid_jit_entries:
        if entry not in all_assigned_packs and entry not in new_list_3:
            new_list_3.append(entry)
            all_assigned_packs.add(entry)
            print(f"    Added to 3: {entry}")

    data["pack_overrides"]["3"] = new_list_3

    with open(OVERRIDES_FILE, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)

    print("  resourcepackoverrides.json successfully synced!")


def build_manifest():
    try:
        print("Building manifest...")

        if CACHE_DIR.exists():
            print("  Clearing old Java JIT cache...")
            shutil.rmtree(CACHE_DIR)

        MANIFEST_FILE.parent.mkdir(parents=True, exist_ok=True)
        valid_names = get_valid_pack_names()

        manifest_data = {"packs": {}}

        for cider_pack in CIDER_PACKS_DIR.iterdir():
            if not cider_pack.is_dir(): continue

            zip_name = cider_pack.name + ".zip"
            zip_path = PRISM_RP_DIR / zip_name

            if not zip_path.exists():
                print(f"  WARNING: Physical zip {zip_name} not found in Prism folder. Skipping hash compare.")
                continue

            print(f"  Processing {cider_pack.name}...")

            original_hashes = {}
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                for zip_info in zip_ref.infolist():
                    if not zip_info.is_dir():
                        with zip_ref.open(zip_info) as f:
                            original_hashes[zip_info.filename] = hashlib.sha256(f.read()).hexdigest()

            manifest_data["packs"][zip_name] = []

            for root, _, files in os.walk(cider_pack):
                for file in files:
                    full_path = Path(root) / file
                    rel_path = full_path.relative_to(cider_pack).as_posix()
                    file_hash = get_file_hash(full_path)

                    if rel_path in original_hashes and original_hashes[rel_path] == file_hash:
                        manifest_data["packs"][zip_name].append(rel_path)

        with open(MANIFEST_FILE, "w", encoding='utf-8') as f:
            json.dump(manifest_data, f, indent=4)

        print(f"  Manifest written to {MANIFEST_FILE}")

        update_overrides(valid_names)

        messagebox.showinfo("Success", f"Manifest successfully built and overrides updated!")
    except Exception as e:
        messagebox.showerror("Error", f"An error occurred while building the manifest:\n{e}")


def restore_from_manifest():
    """Wipes ciderpacks and reconstructs it exactly as defined in the Git-tracked manifest."""
    if not MANIFEST_FILE.exists():
        messagebox.showerror("Error", "Manifest file not found. Nothing to restore.")
        return

    if not messagebox.askyesno("Confirm Restore",
                               "This will completely overwrite your current ciderpacks folder with the state defined in the manifest.\n\nAny unsaved conflict resolutions will be lost. Proceed?"):
        return

    try:
        print("Restoring ciderpacks from manifest...")
        with open(MANIFEST_FILE, 'r', encoding='utf-8') as f:
            manifest_data = json.load(f)

        packs = manifest_data.get("packs", {})

        if CIDER_PACKS_DIR.exists():
            shutil.rmtree(CIDER_PACKS_DIR)
        CIDER_PACKS_DIR.mkdir(parents=True, exist_ok=True)

        for zip_name, files_to_keep in packs.items():
            pack_name = zip_name[:-4] if zip_name.endswith('.zip') else zip_name
            zip_path = PRISM_RP_DIR / zip_name

            if not zip_path.exists():
                print(f"  WARNING: Cannot restore {pack_name}. Missing physical zip in Prism: {zip_name}")
                continue

            target_dir = CIDER_PACKS_DIR / pack_name
            target_dir.mkdir(parents=True, exist_ok=True)

            print(f"  Restoring {pack_name} ({len(files_to_keep)} files)...")
            with zipfile.ZipFile(zip_path, 'r') as zip_ref:
                for rel_path in files_to_keep:
                    try:
                        zip_internal_path = rel_path.replace('\\', '/')
                        zip_ref.extract(zip_internal_path, path=target_dir)
                    except KeyError:
                        print(f"    WARNING: File {zip_internal_path} not found in {zip_name}")

        print("Done restoring!")
        messagebox.showinfo("Success", "Successfully restored ciderpacks from the manifest!")
    except Exception as e:
        messagebox.showerror("Error", f"An error occurred during restore:\n{e}")


def run_gui():
    root = tk.Tk()
    root.title("Cider Toolkit")

    window_width = 300
    window_height = 200
    root.geometry(f"{window_width}x{window_height}")
    root.eval('tk::PlaceWindow . center')

    label = tk.Label(root, text="Select an action to perform:", pady=10)
    label.pack()

    btn_extract = tk.Button(root, text="Extract Packs", command=extract_packs, width=20, pady=5)
    btn_extract.pack(pady=5)

    btn_restore = tk.Button(root, text="Restore from Manifest", command=restore_from_manifest, width=20, pady=5)
    btn_restore.pack(pady=5)

    btn_build = tk.Button(root, text="Build Manifest & Sync", command=build_manifest, width=20, pady=5)
    btn_build.pack(pady=5)

    root.mainloop()


if __name__ == "__main__":
    run_gui()
