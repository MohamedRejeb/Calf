#!/usr/bin/env bash
#
# Build the native file picker library for all supported desktop targets.
#
# Usage:
#   ./build-all-targets.sh              # Build all targets
#   ./build-all-targets.sh macos        # Build macOS targets only
#   ./build-all-targets.sh windows      # Build Windows target only
#   ./build-all-targets.sh linux        # Build Linux target only
#
# Output:
#   ../src/desktopMain/resources/native/<os>-<arch>/<libfile>
#
# Prerequisites:
#   - Rust toolchain (rustup)
#   - For cross-compilation: appropriate targets installed via rustup
#     macOS (from macOS):  rustup target add x86_64-apple-darwin aarch64-apple-darwin
#     Windows (cross):     requires cargo-xwin or cross
#     Linux (cross):       requires cross or appropriate linker
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
OUTPUT_BASE="$SCRIPT_DIR/../src/desktopMain/resources/native"

cd "$SCRIPT_DIR"

build_target() {
    local rust_target="$1"
    local os_dir="$2"
    local lib_file="$3"

    echo "==> Building for $rust_target..."

    cargo build --release --target "$rust_target"

    local out_dir="$OUTPUT_BASE/$os_dir"
    mkdir -p "$out_dir"
    cp "target/$rust_target/release/$lib_file" "$out_dir/"

    local size
    size=$(du -h "$out_dir/$lib_file" | cut -f1)
    echo "    -> $out_dir/$lib_file ($size)"
}

filter="${1:-all}"

# macOS targets (can be built natively on macOS)
if [[ "$filter" == "all" || "$filter" == "macos" ]]; then
    build_target "aarch64-apple-darwin" "macos-arm64" "libcalf_filepicker_native.dylib"
    build_target "x86_64-apple-darwin"  "macos-x64"   "libcalf_filepicker_native.dylib"
fi

# Windows target
if [[ "$filter" == "all" || "$filter" == "windows" ]]; then
    build_target "x86_64-pc-windows-msvc" "windows-x64" "calf_filepicker_native.dll"
fi

# Linux targets
if [[ "$filter" == "all" || "$filter" == "linux" ]]; then
    build_target "x86_64-unknown-linux-gnu" "linux-x64" "libcalf_filepicker_native.so"
fi

echo ""
echo "Done. Native libraries are in: $OUTPUT_BASE"
ls -lhR "$OUTPUT_BASE"
