# Linux kiosk version

The same `clock/` page that runs inside the APK also runs as a plain kiosk on
Linux — that is how this project started, on a Xiaomi Mi Pad 4 Plus with
postmarketOS + Plasma Mobile.

## Install on the target machine

```bash
git clone <this repo> ~/chibi-clock
mkdir -p ~/.config/systemd/user
cp ~/chibi-clock/linux/chibi-clock-*awake.service ~/.config/systemd/user/
cp ~/chibi-clock/linux/chibi-clock-kiosk.service ~/.config/systemd/user/
systemctl --user daemon-reload
systemctl --user enable --now chibi-clock-awake.service
systemctl --user enable --now chibi-clock-kiosk.service
```

Check it: `systemctl --user status chibi-clock-kiosk.service`.

## What actually keeps the screen on

On a KDE/Plasma session the display does not blank because of the app — the
*compositor* drops DPMS when no frames arrive. Three things are needed:

1. `~/.config/powermanagementprofilesrc` — in the AC / Battery / LowBattery
   profiles set `idleTime=0` for `DimDisplay`, `DPMSControl` and
   `SuspendSession`.
2. `plasma-powerdevil.service` running (it can sit `inactive`; the unit is
   `static`, so start it and add an autostart entry).
3. `chibi-clock-awake.service` (above) — inhibitor + periodic activity poke +
   `kscreen-doctor --dpms on`.

Also worth setting `Autolock=false` and `Timeout=0` in `kscreenlockerrc`, and
in Firefox's `user.js`:

```
user_pref("dom.min_background_timeout_value", 1000);
user_pref("privacy.reduce_timer_precision", false);
```

## Known trap: GPU crashes, not the app

On the Adreno 512 in the Mi Pad 4 the kernel can throw
`CP | opcode error` / `gpu fault`, the recovery worker wedges the CPU and the
machine hard-locks. That is a driver bug, not the clock. Reducing redraw work
helps (the SVG digits here are far cheaper than four animated GIFs), and the
radical fix is switching the compositor to software rendering (`llvmpipe`).
If the freeze repeats, that is the move.
