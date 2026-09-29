"""
SecureGuard Source Code Bundle Packager
Creates a compact ZIP archive containing Flutter Mobile App, Go Backend,
Next.js SOC Dashboard, Documentation, and Automated Test Scripts.
Excludes node_modules, .next, .dart_tool, build/, and IDE temporary files.
"""

import os
import zipfile
import sys

EXCLUDE_DIRS = {
    "node_modules",
    ".next",
    ".turbo",
    ".dart_tool",
    "build",
    ".gradle",
    ".idea",
    ".vscode",
    "__pycache__",
    ".git",
    "tmp",
    "releases",
}

EXCLUDE_EXTS = {
    ".pyc",
    ".pyo",
    ".pyd",
    ".DS_Store",
    ".tmp",
    ".apk",
    ".aab",
}

INCLUDED_ROOT_DIRS = ["backend", "main", "web", "doc"]
INCLUDED_ROOT_FILES = [
    "test_poc_scenarios.ps1",
    "run_app.ps1",
    "README.md",
]


def create_bundle(output_zip: str, root_dir: str):
    print(f"Creating source code bundle: {output_zip}")
    file_count = 0
    total_uncompressed_bytes = 0

    with zipfile.ZipFile(output_zip, "w", zipfile.ZIP_DEFLATED) as zipf:
        # Add root files
        for fname in INCLUDED_ROOT_FILES:
            fpath = os.path.join(root_dir, fname)
            if os.path.exists(fpath):
                zipf.write(fpath, fname)
                file_count += 1
                total_uncompressed_bytes += os.path.getsize(fpath)
                print(f"  [ROOT] {fname}")

        # Add directories
        for target_dir in INCLUDED_ROOT_DIRS:
            target_path = os.path.join(root_dir, target_dir)
            if not os.path.exists(target_path):
                continue

            for root, dirs, files in os.walk(target_path):
                # Filter out excluded directories in-place
                dirs[:] = [d for d in dirs if d not in EXCLUDE_DIRS]

                for file in files:
                    _, ext = os.path.splitext(file)
                    if ext in EXCLUDE_EXTS:
                        continue

                    full_path = os.path.join(root, file)
                    rel_path = os.path.relpath(full_path, root_dir)

                    zipf.write(full_path, rel_path)
                    file_count += 1
                    total_uncompressed_bytes += os.path.getsize(full_path)

    zip_size_bytes = os.path.getsize(output_zip)
    zip_size_mb = zip_size_bytes / (1024 * 1024)
    orig_size_mb = total_uncompressed_bytes / (1024 * 1024)

    print("\n" + "=" * 60)
    print("BUNDLE PACKAGING COMPLETED")
    print("=" * 60)
    print(f"Archive Output    : {output_zip}")
    print(f"Total Files Added : {file_count} files")
    print(f"Original Size     : {orig_size_mb:.2f} MB")
    print(f"Compressed Size   : {zip_size_mb:.2f} MB")
    print(f"Compression Ratio : {(1 - (zip_size_bytes / max(total_uncompressed_bytes, 1))) * 100:.1f}%")
    print("=" * 60)


if __name__ == "__main__":
    current_root = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    out_file = os.path.join(current_root, "SecureGuard_SourceCode_Bundle.zip")
    create_bundle(out_file, current_root)
