#!/usr/bin/env python3
"""Generate the chibi-clock numeral pack as SVG (MIT).

Every glyph is a bold rounded numeral drawn with a single stroked path so it
stays crisp at any size and weighs a couple of hundred bytes. Output goes to
clock/digits/{0..9,colon}.svg plus a preview page for eyeballing.

Run:  python art/gen_digits.py
"""
import os

VIEWBOX = "0 0 100 140"
STROKE = 30

# Single-stroke skeletons. Stroke only, round caps/joins -> chunky rounded look.
PATHS = {
    "0": ["M50,70 m-22,0 a22,43 0 1,0 44,0 a22,43 0 1,0 -44,0"],
    "1": ["M36,36 L52,18 V122"],
    "2": ["M20,48 C20,26 34,16 51,16 C69,16 80,29 80,47 C80,63 68,74 54,86 L24,110 H80"],
    "3": ["M22,30 C34,18 52,14 64,23 C78,33 78,54 62,64 C80,68 84,92 70,108 C58,124 34,124 22,112"],
    "4": ["M62,122 V18 L20,88 H82"],
    "5": ["M78,18 H34 V62 C44,54 60,52 70,60 C84,72 84,100 70,114 C58,126 38,124 26,114"],
    "6": ["M74,20 C54,14 38,26 30,46 C24,62 23,80 25,96 C29,116 42,124 56,122 C72,120 81,106 79,90 C77,74 64,64 50,66 C38,68 28,78 26,92"],
    "7": ["M20,18 H80 L42,122"],
    "8": ["M50,34 m-17,0 a17,17 0 1,0 34,0 a17,17 0 1,0 -34,0",
          "M50,98 m-23,0 a23,24 0 1,0 46,0 a23,24 0 1,0 -46,0"],
    "9": ["M26,120 C46,126 62,114 70,94 C76,78 77,60 75,44 C71,24 58,16 44,18 C28,20 19,34 21,50 C23,66 36,76 50,74 C62,72 72,62 74,48"],
}

# Symbols are filled circles, not strokes: a 4-unit stroke has a zero-width
# bounding box, and a gradient on objectBoundingBox then paints nothing.
CIRCLES = {
    ":": [(50, 40, 15), (50, 100, 15)],
    ".": [(50, 118, 15)],
}

# Palette: hour digits warm, minute digits cool, separator neutral.
COLORS = {
    "h": ("#ff8fb1", "#ff5c8a"),
    "m": ("#9b8cff", "#6a5cff"),
    "s": ("#6fd3ff", "#2fa8e0"),
}

TEMPLATE = """<svg xmlns="http://www.w3.org/2000/svg" viewBox="{vb}" width="100" height="140" fill="none">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0" stop-color="{c1}"/>
      <stop offset="1" stop-color="{c2}"/>
    </linearGradient>
  </defs>
  <g stroke="url(#g)" stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round">
{paths}
  </g>
</svg>
"""


def svg_for(glyph, c1, c2, sw=STROKE):
    paths = "\n".join(
        '    <path d="%s"/>' % d for d in PATHS[glyph]
    )
    return TEMPLATE.format(vb=VIEWBOX, c1=c1, c2=c2, sw=sw, paths=paths)


def svg_symbol(glyph, c1, c2):
    circles = "\n".join(
        '    <circle cx="%d" cy="%d" r="%d" fill="url(#g)"/>' % c for c in CIRCLES[glyph]
    )
    # Narrow viewBox: a full-width box would waste ~60% of the separator's width.
    return """<svg xmlns="http://www.w3.org/2000/svg" viewBox="30 0 40 140" width="40" height="140" fill="none">
  <defs>
    <linearGradient id="g" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0" stop-color="{c1}"/>
      <stop offset="1" stop-color="{c2}"/>
    </linearGradient>
  </defs>
{circles}
</svg>
""".format(c1=c1, c2=c2, circles=circles)


def write_pack(outdir):
    os.makedirs(outdir, exist_ok=True)
    written = []
    for g in "0123456789":
        for key, (c1, c2) in COLORS.items():
            if key == "s":
                continue
            name = "%s_%s.svg" % (g, key)
            p = os.path.join(outdir, name)
            with open(p, "w", encoding="utf-8") as f:
                f.write(svg_for(g, c1, c2))
            written.append(name)
    for g, name in ((":", "colon"), (".", "dot")):
        c1, c2 = COLORS["s"]
        with open(os.path.join(outdir, "%s.svg" % name), "w", encoding="utf-8") as f:
            f.write(svg_symbol(g, c1, c2))
        written.append("%s.svg" % name)
    return written


if __name__ == "__main__":
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    out = os.path.join(root, "clock", "digits")
    names = write_pack(out)
    print("wrote %d files to %s" % (len(names), out))
