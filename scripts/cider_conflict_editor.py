import os
import tkinter as tk
from PIL import Image, ImageTk
from pathlib import Path
from tkinter import ttk, messagebox

REPO_ROOT = Path(__file__).resolve().parent.parent if Path(__file__).resolve().parent.name == "scripts" else Path(
    r"C:\Users\evan\Documents\GitHub\Spiced-Cider")
CIDER_PACKS_DIR = REPO_ROOT / "ciderpacks"


class ConflictResolverApp:
    def __init__(self, root):
        self.root = root
        self.root.title("Spiced Cider - Conflict Resolver")
        self.root.geometry("1000x600")

        self.conflicts = {}  # Format: { "assets/minecraft/textures/...png": [Path1, Path2] }
        self.current_preview_widgets = []

        self.setup_ui()
        self.scan_for_conflicts()

    def setup_ui(self):
        left_frame = ttk.Frame(self.root, padding=10)
        left_frame.pack(side=tk.LEFT, fill=tk.Y)

        ttk.Label(left_frame, text="Detected Conflicts:", font=("Arial", 12, "bold")).pack(anchor=tk.W)

        list_frame = ttk.Frame(left_frame)
        list_frame.pack(fill=tk.BOTH, expand=True, pady=5)

        scrollbar = ttk.Scrollbar(list_frame)
        scrollbar.pack(side=tk.RIGHT, fill=tk.Y)

        self.conflict_listbox = tk.Listbox(list_frame, width=50, yscrollcommand=scrollbar.set)
        self.conflict_listbox.pack(side=tk.LEFT, fill=tk.BOTH, expand=True)
        scrollbar.config(command=self.conflict_listbox.yview)

        self.conflict_listbox.bind('<<ListboxSelect>>', self.on_select_conflict)

        ttk.Button(left_frame, text="Rescan Packs", command=self.scan_for_conflicts).pack(fill=tk.X, pady=5)

        self.right_frame = ttk.Frame(self.root, padding=10)
        self.right_frame.pack(side=tk.RIGHT, fill=tk.BOTH, expand=True)

        self.preview_title = ttk.Label(self.right_frame, text="Select a conflict to preview",
                                       font=("Arial", 14, "bold"))
        self.preview_title.pack(pady=(0, 10))

        self.previews_container = ttk.Frame(self.right_frame)
        self.previews_container.pack(fill=tk.BOTH, expand=True)

    def scan_for_conflicts(self):
        """Scans the ciderpacks directory for overlapping relative paths."""
        self.conflict_listbox.delete(0, tk.END)
        self.conflicts.clear()

        if not CIDER_PACKS_DIR.exists():
            messagebox.showerror("Error", f"Directory not found:\n{CIDER_PACKS_DIR}")
            return

        all_files = {}

        for pack_dir in CIDER_PACKS_DIR.iterdir():
            if not pack_dir.is_dir(): continue

            for root, _, files in os.walk(pack_dir):
                for file in files:
                    full_path = Path(root) / file
                    rel_path = full_path.relative_to(pack_dir).as_posix()

                    if rel_path not in all_files:
                        all_files[rel_path] = []
                    all_files[rel_path].append(full_path)

        for rel_path, paths in all_files.items():
            if len(paths) > 1:
                self.conflicts[rel_path] = paths
                self.conflict_listbox.insert(tk.END, rel_path)

        self.preview_title.config(text=f"Found {len(self.conflicts)} conflicts.")
        self.clear_previews()

    def clear_previews(self):
        for widget in self.current_preview_widgets:
            widget.destroy()
        self.current_preview_widgets.clear()

    def on_select_conflict(self, event):
        selection = self.conflict_listbox.curselection()
        if not selection: return

        rel_path = self.conflict_listbox.get(selection[0])
        conflicting_files = self.conflicts[rel_path]

        self.preview_title.config(text=rel_path)
        self.clear_previews()

        for file_path in conflicting_files:
            pack_name = file_path.relative_to(CIDER_PACKS_DIR).parts[0]

            col_frame = ttk.Frame(self.previews_container, relief=tk.GROOVE, borderwidth=2, padding=5)
            col_frame.pack(side=tk.LEFT, fill=tk.BOTH, expand=True, padx=5)
            self.current_preview_widgets.append(col_frame)

            ttk.Label(col_frame, text=pack_name, font=("Arial", 11, "bold")).pack(pady=5)

            if file_path.suffix.lower() == '.png':
                self.render_image(col_frame, file_path)
            elif file_path.suffix.lower() in ['.jem', '.json', '.txt', '.mcmeta']:
                self.render_text_snippet(col_frame, file_path)
            else:
                ttk.Label(col_frame, text="No preview available").pack(pady=20)

            delete_btn = ttk.Button(
                col_frame,
                text=f"Delete from {pack_name}",
                command=lambda p=file_path: self.delete_file(p)
            )
            delete_btn.pack(side=tk.BOTTOM, pady=10)

    def render_image(self, parent, path):
        try:
            img = Image.open(path)
            img.thumbnail((250, 250), Image.Resampling.NEAREST)
            if img.width < 50:
                img = img.resize((img.width * 5, img.height * 5), Image.Resampling.NEAREST)

            photo = ImageTk.PhotoImage(img)
            lbl = tk.Label(parent, image=photo)
            lbl.image = photo
            lbl.pack(pady=10)

            original_size = Image.open(path).size
            ttk.Label(parent, text=f"Resolution: {original_size[0]}x{original_size[1]}").pack()
        except Exception as e:
            ttk.Label(parent, text=f"Error loading image:\n{e}").pack()

    def render_text_snippet(self, parent, path):
        try:
            with open(path, 'r', encoding='utf-8') as f:
                content = f.read(500)

            text_widget = tk.Text(parent, width=30, height=15, wrap=tk.WORD, font=("Consolas", 9))
            text_widget.insert(tk.END, content + ("..." if len(content) == 500 else ""))
            text_widget.config(state=tk.DISABLED)
            text_widget.pack(pady=10, fill=tk.BOTH, expand=True)
        except Exception as e:
            ttk.Label(parent, text=f"Error reading file:\n{e}").pack()

    def delete_file(self, path):
        if messagebox.askyesno("Confirm Delete", f"Are you sure you want to delete:\n{path.name}\nfrom this pack?"):
            try:
                os.remove(path)
                messagebox.showinfo("Success", "File deleted.")
                self.scan_for_conflicts()
            except Exception as e:
                messagebox.showerror("Error", f"Failed to delete file:\n{e}")


if __name__ == "__main__":
    root = tk.Tk()
    app = ConflictResolverApp(root)
    root.mainloop()
