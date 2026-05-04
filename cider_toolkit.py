import os
import hashlib
import json
import shutil
import zipfile
from pathlib import Path
import sys

RESOURCE_PACKS_DIR = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\mod\run\resourcepacks")
CIDER_PACKS_DIR = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\ciderpacks")
OVERRIDES_DIR = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\mod\run\config\openloader\packs\spicedcider_overrides")
MANIFEST_FILE = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\mod\run\config\spicedcider\spicedcider_manifest.json")

def get_file_hash(filepath):
    hasher = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)
    return hasher.hexdigest()

def extract_packs():
    print("Extracting resource packs...")
    CIDER_PACKS_DIR.mkdir(parents=True, exist_ok=True)
    
    for item in RESOURCE_PACKS_DIR.iterdir():
        if item.is_file() and item.name.endswith(".zip"):
            pack_name = item.stem 
            target_dir = CIDER_PACKS_DIR / pack_name
            
            if not target_dir.exists():
                print(f"  Unzipping {item.name} -> {pack_name}/")
                with zipfile.ZipFile(item, 'r') as zip_ref:
                    zip_ref.extractall(target_dir)
            else:
                print(f"  Skipping {item.name} (already extracted)")
                
    print("Done! You can now freely delete or modify files in the ciderpacks folder.")

def build_manifest_and_overrides():
    print("Building manifest and pushing overrides to OpenLoader...")
    
    if OVERRIDES_DIR.exists():
        shutil.rmtree(OVERRIDES_DIR)
    OVERRIDES_DIR.mkdir(parents=True, exist_ok=True)
    MANIFEST_FILE.parent.mkdir(parents=True, exist_ok=True)
    
    manifest_data = {"packs": {}}
    
    for cider_pack in CIDER_PACKS_DIR.iterdir():
        if not cider_pack.is_dir(): continue
        
        zip_name = cider_pack.name + ".zip"
        zip_path = RESOURCE_PACKS_DIR / zip_name
        
        if not zip_path.exists():
            print(f"  WARNING: Source zip {zip_name} not found in resourcepacks/. Skipping.")
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
                else:
                    dest_path = OVERRIDES_DIR / rel_path
                    dest_path.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(full_path, dest_path)
                    
    with open(OVERRIDES_DIR / "pack.mcmeta", "w") as f:
        json.dump({"pack": {"pack_format": 34, "description": "Spiced Cider Manual Overrides"}}, f, indent=4)

    with open(MANIFEST_FILE, "w") as f:
        json.dump(manifest_data, f, indent=4)
        
    print(f"Success! Manifest written to {MANIFEST_FILE}")
    print(f"Overrides automatically installed to {OVERRIDES_DIR}")

if __name__ == "__main__":
    if len(sys.argv) < 2 or sys.argv[1] not in ["extract", "build"]:
        print("Usage: python cider_toolkit.py [extract|build]")
    elif sys.argv[1] == "extract":
        extract_packs()
    elif sys.argv[1] == "build":
        build_manifest_and_overrides()