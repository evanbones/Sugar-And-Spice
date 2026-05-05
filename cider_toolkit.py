import os
import hashlib
import json
import shutil
import zipfile
from pathlib import Path
import tkinter as tk
from tkinter import messagebox

RESOURCE_PACKS_DIR = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\mod\run\resourcepacks")
CIDER_PACKS_DIR = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\ciderpacks")
MANIFEST_FILE = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\mod\run\config\spicedcider\spicedcider_manifest.json")
CACHE_DIR = Path(r"C:\Users\evan\Documents\GitHub\Spiced-Cider\mod\run\.spicedcider_cache")

def get_file_hash(filepath):
    hasher = hashlib.sha256()
    with open(filepath, 'rb') as f:
        while chunk := f.read(8192):
            hasher.update(chunk)
    return hasher.hexdigest()

def extract_packs():
    try:
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

        print("Done extracting!")
        messagebox.showinfo("Success", "Extraction complete!\nYou can now freely delete or modify files in the ciderpacks folder.")
    except Exception as e:
        messagebox.showerror("Error", f"An error occurred during extraction:\n{e}")

def build_manifest():
    try:
        print("Building manifest...")

        if CACHE_DIR.exists():
            print("  Clearing old Java JIT cache...")
            shutil.rmtree(CACHE_DIR)

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

                    # Only add unmodified files to the manifest
                    if rel_path in original_hashes and original_hashes[rel_path] == file_hash:
                        manifest_data["packs"][zip_name].append(rel_path)

        with open(MANIFEST_FILE, "w") as f:
            json.dump(manifest_data, f, indent=4)

        print(f"Success! Manifest written to {MANIFEST_FILE}")
        messagebox.showinfo("Success", f"Manifest successfully built and written to:\n{MANIFEST_FILE}")
    except Exception as e:
        messagebox.showerror("Error", f"An error occurred while building the manifest:\n{e}")

def run_gui():
    root = tk.Tk()
    root.title("Cider Toolkit")

    window_width = 300
    window_height = 150
    root.geometry(f"{window_width}x{window_height}")
    root.eval('tk::PlaceWindow . center')

    label = tk.Label(root, text="Select an action to perform:", pady=10)
    label.pack()

    btn_extract = tk.Button(root, text="Extract Packs", command=extract_packs, width=20, pady=5)
    btn_extract.pack(pady=5)

    btn_build = tk.Button(root, text="Build Manifest", command=build_manifest, width=20, pady=5)
    btn_build.pack(pady=5)

    root.mainloop()

if __name__ == "__main__":
    run_gui()