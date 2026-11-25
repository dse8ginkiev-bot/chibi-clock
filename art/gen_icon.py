#!/usr/bin/env python3
"""Draw the launcher icon (PIL only) into app/src/main/res/mipmap-*/ic_launcher.png.

Run:  python art/gen_icon.py
"""
import os
import math
from PIL import Image, ImageDraw

SIZES = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}

BG_TOP = (76, 60, 200)
BG_BOTTOM = (255, 92, 138)
FACE = (17, 14, 30)


def lerp(a, b, t):
    return tuple(int(round(a[i] + (b[i] - a[i]) * t)) for i in range(3))


def draw_icon(size):
    ss = 4  # supersample
    s = size * ss
    img = Image.new("RGBA", (s, s), (0, 0, 0, 0))

    # rounded gradient background
    grad = Image.new("RGB", (1, s))
    for y in range(s):
        grad.putpixel((0, y), lerp(BG_TOP, BG_BOTTOM, y / (s - 1)))
    grad = grad.resize((s, s))
    mask = Image.new("L", (s, s), 0)
    ImageDraw.Draw(mask).rounded_rectangle([0, 0, s - 1, s - 1], radius=int(s * 0.22), fill=255)
    img.paste(grad, (0, 0), mask)

    d = ImageDraw.Draw(img)
    cx, cy = s / 2, s / 2
    r = s * 0.30
    w = max(2, int(s * 0.055))
    d.ellipse([cx - r, cy - r, cx + r, cy + r], outline=(255, 255, 255, 235), width=w)

    # hands at 10:10
    for angle_deg, length, width in ((300, 0.62, 0.075), (60, 0.82, 0.055)):
        a = math.radians(angle_deg)
        x2 = cx + math.cos(a) * r * length
        y2 = cy - math.sin(a) * r * length
        d.line([cx, cy, x2, y2], fill=(255, 255, 255, 245), width=max(2, int(s * width)))

    d.ellipse([cx - s * 0.035, cy - s * 0.035, cx + s * 0.035, cy + s * 0.035],
              fill=(255, 255, 255, 255))

    # blush
    for dx in (-0.20, 0.20):
        bx = cx + dx * s
        by = cy + r * 0.55
        rr = s * 0.035
        d.ellipse([bx - rr, by - rr, bx + rr, by + rr], fill=(255, 170, 190, 190))

    return img.resize((size, size), Image.LANCZOS)


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    for dens, px in SIZES.items():
        out = os.path.join(root, "app", "src", "main", "res", "mipmap-" + dens)
        os.makedirs(out, exist_ok=True)
        icon = draw_icon(px)
        path = os.path.join(out, "ic_launcher.png")
        icon.save(path)
        print("wrote", path, icon.size)


if __name__ == "__main__":
    main()
