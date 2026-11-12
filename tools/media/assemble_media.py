#!/usr/bin/env python3
"""Copies the captured media into docs/images and rewrites the docs' image tables. Idempotent.

usage: assemble_media.py [--dry] [--prune]   (--prune deletes images nothing references)
Input: build/media/final/<rig>/<Component>-<suffix>[-variant].png|gif (override with MEDIA_FINAL), rigs
android (material), ios18 (ios), ios26 (ios-liquid-glass), web, desktop, duo. Files the docs do not reference
stay in docs/images until you delete them; the script prints them.
"""
import os, re, shutil, sys

R = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
S = os.environ.get("MEDIA_FINAL", f"{R}/build/media/final")
IMG = f"{R}/docs/images"
GIF = f"{IMG}/gif"
DRY = "--dry" in sys.argv
PRUNE = "--prune" in sys.argv
RIGS = ["android", "ios18", "ios26", "web", "desktop", "duo"]
SUFFIX = {"android": "material", "ios18": "ios", "ios26": "ios-liquid-glass", "web": "web", "desktop": "desktop", "duo": "duo"}

# Android variants were named <Component>-<variant>-material; normalise to <Component>-material-<variant>.
ANDROID_VARIANTS = ["action-sheet", "text-field", "dialog", "system", "mail", "message", "prompt", "sheet"]


def canonical(rig, name):
    if rig != "android":
        return name
    for v in ANDROID_VARIANTS:
        m = re.match(rf"^(.+)-{v}-material\.(png|gif)$", name)
        if m:
            return f"{m.group(1)}-material-{v}.{m.group(2)}"
    return name


copied = []
for rig in RIGS:
    d = f"{S}/{rig}"
    if not os.path.isdir(d):
        continue
    for f in sorted(os.listdir(d)):
        if not f.endswith((".png", ".gif")):
            continue
        dst = f"{GIF if f.endswith('.gif') else IMG}/{canonical(rig, f)}"
        copied.append(os.path.basename(dst))
        if not DRY:
            shutil.copy(f"{d}/{f}", dst)

have = set(os.listdir(IMG)) | {f"gif/{x}" for x in os.listdir(GIF)}
have |= set(copied) | {f"gif/{x}" for x in copied if x.endswith(".gif")}


def exists(path):  # path relative to docs/images
    return path in have


def cell(alt, path, rel):
    """rel: prefix from the doc to docs/images ('images/' or '../images/')."""
    return f"![{alt}]({rel}{path})" if exists(path) else None


def table(headers, cells):
    keep = [(h, c) for h, c in zip(headers, cells) if c]
    if not keep:
        return None
    hs = " | ".join(h for h, _ in keep)
    cs = " | ".join(c for _, c in keep)
    seps = " | ".join("---" for _ in keep)
    return f"| {hs} |\n| {seps} |\n| {cs} |"


LOOKS = [("Material (Android, Desktop, Web)", "material"), ("Cupertino (iOS < 26)", "ios"), ("Liquid Glass (iOS 26+)", "ios-liquid-glass")]


def three(comp, rel, gif=True, variant=""):
    cells = []
    for _, suf in LOOKS:
        name = f"{comp}-{suf}{('-' + variant) if variant else ''}"
        path = f"gif/{name}.gif" if gif and exists(f"gif/{name}.gif") else f"{name}.png"
        cells.append(cell(f"{comp} {suf}", path, rel))
    return table([h for h, _ in LOOKS], cells)


def wide(comp, rel):
    cells = [cell(f"{comp} desktop", f"{comp}-desktop.png", rel), cell(f"{comp} web", f"{comp}-web.png", rel)]
    return table(["Desktop", "Web"], cells)


TABLE_RE = re.compile(r"(?:^\|[^\n]*\n)+", re.M)


def replace_tables(text, new_tables):
    """Replaces, in order, each markdown table that embeds an image with the given new table."""
    out, pos, i = [], 0, 0
    for m in TABLE_RE.finditer(text):
        if "images/" not in m.group(0) or i >= len(new_tables):
            continue
        out.append(text[pos:m.start()])
        out.append((new_tables[i] or "").rstrip("\n") + "\n")
        pos = m.end(); i += 1
    out.append(text[pos:])
    return "".join(out), i


def insert_after_h1_para(text, block):
    """Inserts a block after the first paragraph that follows the H1 (before the next heading)."""
    m = re.search(r"^# [^\n]+\n\n(?:[^\n#][^\n]*\n)+", text, re.M)
    if not m:
        return text
    return text[: m.end()] + "\n" + block + "\n" + text[m.end():]


def insert_after_heading(text, heading, block):
    """Inserts a block after the first paragraph under the given heading, unless the block is there already."""
    if block in text:
        return text
    m = re.search(rf"^{re.escape(heading)}\n\n(?:[^\n#][^\n]*\n)+", text, re.M)
    if not m:
        return text
    return text[: m.end()] + "\n" + block + "\n" + text[m.end():]


edits = {}


def doc(path, rel, tables=None, insert=None):
    p = f"{R}/docs/{path}"
    t = open(p).read()
    if tables:
        t, n = replace_tables(t, tables)
        if n < len([x for x in tables if x]):
            print(f"  {path}: only {n} tables replaced of {len(tables)}")
    if insert and "images/" not in t:
        t = insert_after_h1_para(t, insert)
    edits[p] = t


U = "../images/"
D = "images/"
# component pages
for comp, page in [("AdaptiveButton", "ui/adaptive-button.md"), ("AdaptiveSwitch", "ui/adaptive-switch.md"), ("AdaptiveSlider", "ui/adaptive-slider.md"),
                   ("AdaptiveToolbar", "ui/adaptive-toolbar.md"), ("AdaptiveBottomSheet", "ui/adaptive-bottom-sheet.md"), ("AdaptiveDropdownMenu", "ui/adaptive-drop-down.md"),
                   ("AdaptiveNavigationBar", "ui/adaptive-navigation-bar.md"), ("AdaptiveClickable", "ui/adaptive-clickable.md"),
                   ("AdaptiveCircularProgressIndicator", "ui/adaptive-circular-progress-indicator.md"), ("AdaptiveTimePicker", "ui/adaptive-time-picker.md")]:
    doc(page, U, tables=[three(comp, U)])
doc("ui/adaptive-top-bar.md", U, tables=[three("AdaptiveTopAppBar", U)])  # the dropdown table further down keeps its old GIFs
doc("ui/adaptive-icon-button.md", U, tables=[three("AdaptiveButton", U)])
doc("ui/adaptive-alert-dialog.md", U, tables=[three("AdaptiveAlertDialog", U, gif=False), three("AdaptiveAlertDialog", U, gif=False, variant="action-sheet"), three("AdaptiveAlertDialog", U, gif=False, variant="text-field")])
doc("ui/adaptive-date-picker.md", U, insert=three("AdaptiveDatePicker", U))
# module pages: platform APIs show the system UI only (picker, sheet, prompt), one iOS column
def two(label, comp, variant, rel=D):
    """Android | iOS | Desktop (the desktop column only when that capture exists)."""
    return table([f"{label} (Android)", f"{label} (iOS)", f"{label} (Desktop)"],
                 [cell(f"{label} Android", f"{comp}-material-{variant}.png", rel), cell(f"{label} iOS", f"{comp}-ios-liquid-glass-{variant}.png", rel),
                  cell(f"{label} Desktop", f"{comp}-desktop-{variant}.png", rel)])


doc("filepicker.md", D, tables=[two("The file picker", "AdaptiveFilePicker", "system"), two("The image picker", "AdaptiveImagePicker", "system")])
p = f"{R}/docs/filepicker.md"
edits[p] = insert_after_heading(edits[p], "## File Saver", two("The save dialog", "FileSaver", "system") or "")
doc("webview.md", D, tables=[table(["Android", "iOS", "Web"], [cell("WebView Android", "WebView-material.png", D), cell("WebView iOS", "WebView-ios-liquid-glass.png", D), cell("WebView web", "WebView-web.png", D)])])
doc("permissions.md", D, tables=[two("The permission prompt", "Permissions", "prompt")])
cup = table(["Android", "iOS", "Liquid Glass (iOS 26+)", "Desktop", "Web"], [cell("Cupertino icons " + s, f"CupertinoIcons-{s}.png", D) for s in ["material", "ios", "ios-liquid-glass", "desktop", "web"]])
doc("sf-symbols.md", D, tables=[cup])
share = two("The share sheet", "Share", "sheet")
doc("share.md", D, tables=[share], insert=share)
fold = table(["Android", "iPhone", "Desktop", "Web"], [cell("Foldable material", "Foldable-material.png", D), cell("Foldable ios", "Foldable-ios-liquid-glass.png", D) or cell("Foldable ios", "Foldable-ios.png", D), cell("Foldable desktop", "Foldable-desktop.png", D), cell("Foldable web", "Foldable-web.png", D)])
doc("foldable.md", D, insert=fold)
nav = three("AdaptiveNavigation", D)
doc("navigation.md", D, insert=nav)
scaffold = three("AdaptiveScaffold", D, gif=False)
doc("ui.md", D, insert=scaffold)
# iPhone Duo page: fill the MEDIA slots that have a capture
duo_map = {
    "<!-- MEDIA: the sample's home on an open iPhone Duo: list column, selected demo highlighted, detail column -->":
        ("Home-duo-open-detail.png", "The sample's home on an open iPhone Duo: the list column and the Buttons demo in the detail column"),
    "<!-- MEDIA: open iPhone Duo, Mail tab of the sample: list column, detail column, tab bar and the detail's bar items in the vertical strip -->":
        ("AdaptiveNavigation-duo-open-mail-landscape.png", "The Mail tab on an open iPhone Duo in landscape: list column, detail column, and the tab bar in the vertical strip"),
}
p = f"{R}/docs/foldable.md"; t = edits[p]
slot = "<!-- MEDIA: the sample's Navigation 3 List-Detail demo on iPhone Duo: closed (one pane, a tap pushes the colour), open (two panes), book (the detail starts after the fold) -->"
if exists("Nav3ListDetail-web.png") and "Nav3ListDetail-web.png" not in t:
    t = t.replace(slot, slot + "\n\n![The Navigation 3 List-Detail demo on the web, two panes with an item open](images/Nav3ListDetail-web.png)")
edits[p] = t
p = f"{R}/docs/iphone-duo.md"; t = open(p).read()
for slot, (f, alt) in duo_map.items():
    if exists(f):
        t = t.replace(slot, f"![{alt}](images/{f})")
edits[p] = t
p = f"{R}/docs/navigation.md"; t = edits.get(p) or open(p).read()
slot = "<!-- MEDIA: open iPhone Duo, Mail tab of the sample: list column, detail column, tab bar and the detail's bar items in the vertical strip -->"
if exists("AdaptiveNavigation-duo-open-mail-landscape.png"):
    t = t.replace(slot, "![The Mail tab on an open iPhone Duo in landscape: list column, detail column, and the tab bar in the vertical strip](images/AdaptiveNavigation-duo-open-mail-landscape.png)")
edits[p] = t

# MEDIA slots filled from the Duo and desktop rigs: the comment line is replaced by the image(s) once they exist.
# (page, images-relative prefix, slot text prefix, [(file, alt)], column headers or None for a single image)
F_BOOK = ("Foldable-duo-book.png", "The sample's Foldable screen on an iPhone Duo in Book pose: the fold across the screen, posture above it, details below")
F_OPEN = ("Foldable-duo-open.png", "The sample's Foldable screen on an open iPhone Duo: posture, fold and the two panes drawn live")
HOST = ("BarHost-duo-showcase.png", "The sample on plain Compose navigation on an open iPhone Duo: the Showcase with its bar items and tab bar in the vertical strip")
SLOTS = [
    ("../README.md", "docs/images/", "the sample's Foldable screen on iPhone Duo, closed / book / open", [F_BOOK, F_OPEN], ["Book", "Open"]),
    ("index.md", D, "the sample's Foldable screen on iPhone Duo, closed / book / open", [F_BOOK, F_OPEN], ["Book", "Open"]),
    ("iphone-duo.md", D, "the sample's Foldable screen on iPhone Duo, closed / book / open", [F_BOOK, F_OPEN], ["Book", "Open"]),
    ("foldable.md", D, "the sample's Foldable screen on iPhone Duo, closed / book / open", [F_BOOK, F_OPEN], ["Book", "Open"]),
    ("foldable.md", D, "the sample's Foldable screen half open with the fold across", [F_BOOK], None),
    ("foldable.md", D, "two panes laid out from FoldPanes on an open iPhone Duo", [F_OPEN], None),
    ("foldable.md", D, "the sample's Navigation 3 List-Detail demo on iPhone Duo",
     [("Nav3ListDetail-duo-open.png", "The Navigation 3 List-Detail demo on an open iPhone Duo: two panes"), ("Nav3ListDetail-duo-book.png", "The same demo in Book pose: the detail starts after the fold")], ["Open", "Book"]),
    ("iphone-duo.md", D, "the sample on plain Compose navigation on an open iPhone Duo", [HOST], None),
    ("navigation.md", D, "the sample on plain Compose navigation on an open iPhone Duo", [HOST], None),
    ("navigation.md", D, "the sample's home on an open iPhone Duo",
     [("Home-duo-open-detail.png", "The sample's home on an open iPhone Duo: the list column and the Buttons demo in the detail column")], None),
    ("navigation.md", D, "iPhone Duo closed, the sample's \"Bar items and overflow\" screen",
     [("AdaptiveNavigation-duo-open-bar-items.png", "The \"Bar items and overflow\" screen on an open iPhone Duo in landscape: the image items in the vertical strip, the Sort text item in the title row")], None),
    ("ui.md", D, "an open iPhone Duo, a Navigation 3 screen with its bar items", [HOST], None),
    ("ui/adaptive-navigation-bar.md", U, "a hosted tab bar on an open iPhone Duo", [HOST], None),
    ("ui/adaptive-top-bar.md", U, "a hosted top bar on iPhone during a Navigation 3 push",
     [("BarHost-duo-topbar.png", "The Top Bar demo hosted on an open iPhone Duo: the bar items in the vertical strip")], None),
    ("ui/adaptive-bottom-sheet.md", U, "a closed iPhone Duo with AdaptiveBottomSheet at the large detent",
     [("AdaptiveBottomSheet-duo-open.png", "AdaptiveBottomSheet on an open iPhone Duo, raised over the detail column")], None),
    ("ui/adaptive-alert-dialog.md", U, "an open iPhone Duo, the action-sheet style raised from the detail column",
     [("AdaptiveAlertDialog-duo-open-action-sheet.png", "The action-sheet style on an open iPhone Duo, raised from the detail column and centred in it")], None),
]
SLOT_RE = re.compile(r"^<!-- MEDIA: ([^\n]*?) -->\n", re.M)
for page, rel, prefix, images, headers in SLOTS:
    path = f"{R}/docs/{page}"
    text = edits.get(path) or open(path).read()
    cells = [cell(alt, f, rel) for f, alt in images]
    if not all(cells):
        continue
    block = table(headers, cells) if headers else cells[0]
    text = SLOT_RE.sub(lambda m: (block + "\n") if m.group(1).startswith(prefix) else m.group(0), text)
    edits[path] = text

if DRY:
    for p, t in edits.items():
        print("would edit", os.path.relpath(p, R))
else:
    for p, t in edits.items():
        open(p, "w").write(t)

# report dangling references across README and docs
refs = set()
for root, _, files in os.walk(f"{R}/docs"):
    for f in files:
        if f.endswith(".md"):
            txt = edits.get(f"{root}/{f}") or open(f"{root}/{f}").read()
            refs |= set(re.findall(r"images/([A-Za-z0-9_./-]+\.(?:png|gif))", txt))
refs |= set(re.findall(r"docs/images/([A-Za-z0-9_./-]+\.(?:png|gif))", open(f"{R}/README.md").read()))
missing = sorted(r for r in refs if not exists(r))
print("copied", len(copied), "files; missing references:", missing)
unused = sorted(f for f in os.listdir(IMG) if f.endswith(".png") and f not in refs and f not in ("logo.png", "bmc-yellow-button.png"))
unused += sorted(f"gif/{f}" for f in os.listdir(GIF) if f"gif/{f}" not in refs)
print("unreferenced images:", unused)
if PRUNE and not DRY:
    for f in unused:
        os.remove(f"{IMG}/{f}")
    print("pruned", len(unused))
