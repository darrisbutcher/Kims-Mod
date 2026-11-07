# Kims-Mod: Security Cameras (Fabric, Minecraft 26.3)

Place security cameras and watch them live on a tablet.

## Installing

You need, in your launcher's Minecraft 26.3 Fabric profile:

- **Java 25** (Minecraft 26.3 requires it)
- **Fabric Loader 0.20.1 or newer**
- **Fabric API** for 26.3

Put `securitycams-0.1.0.jar` (from `build/libs/` after building) in the `mods` folder. On a multiplayer
server, install the same jar on the server *and* on every player's client.

## Using it

**Recipes**

| Item | Pattern |
| --- | --- |
| Security Camera | `I G I` / `I R I` (I = iron ingot, G = glass, R = redstone) |
| Security Tablet | `I G I` / `I R I` / `I L I` (I = iron ingot, G = glass pane, R = redstone, L = lapis) |

Both are also in the creative inventory (Camera under Redstone, Tablet under Tools & Utilities).

1. **Place a camera** on any solid surface (wall, ceiling or floor). The lens points away from the
   surface you placed it on.
2. **Right-click the camera with the tablet** to link it. A tablet holds up to 16 cameras.
3. **Right-click anywhere else with the tablet** to open it. Your view jumps to the camera.

On the tablet:

- `<<` / `>>` switch between linked cameras
- `<` / `>` turn the camera left / right (up to 60 degrees from where it was placed)
- `Sweep` makes the camera slowly pan left and right on its own

The camera's head turns in the world to match, so you can see where a camera is looking.
- The text box renames the camera (press Enter or close the tablet to save)
- `Remove` unlinks the camera from the tablet
- `Close` (or Escape) leaves the feed

Cameras watch for players and hostile mobs within about 12 blocks, in front of the lens and in line of
sight. Anyone carrying a tablet linked to that camera gets a "Motion at <camera>: <what>" alert, even
when they're far away, and it shows as a red banner if they're watching a feed.

Notes:

- A camera works while it is within your render distance (chunks that far away are loaded). Move closer
  or raise your render distance if the tablet says "out of range".
- Anyone holding a tablet linked to a camera can watch and control it.
- Taking damage closes the feed so you can react.

## Building

```
.\gradlew.bat build
```

Needs JDK 25. The jar is written to `build/libs/`.

## Releasing

Releases are built and published automatically by GitHub Actions (`.github/workflows/release.yml`).
To publish one, push a tag that starts with `v`:

```
git tag v0.2.0
git push origin v0.2.0
```

GitHub builds the mod with that version number, creates a release named after the tag with generated
release notes, and attaches `securitycams-0.2.0.jar`. A tag with a dash in it (such as `v0.2.0-beta`)
is marked as a pre-release. You can also run the workflow by hand from the repository's Actions tab to
check that the build works; that keeps the jar as a download on the run but does not create a release.

## Testing

```
gradlew runClientGameTest
```

Starts a real client, builds a throwaway world, places a camera, links a tablet, opens the feed, sweeps,
triggers a motion alert, and takes screenshots into `build/run/clientGameTest/screenshots/`.
