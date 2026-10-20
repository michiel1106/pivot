# Pivot

A survival-friendly display entity editor for Fabric. Place, move, rotate, scale, lock and duplicate block display and item display entities in game. Players without the mod can still join a server that has it.

## Supported versions

| Mod version | Minecraft | Java |
|-------------|-----------|------|
| 2.0.0       | 26.3      | 25   |
| 1.0.0       | 1.21.6    | 21   |

The 1.21.6 line is tagged `mc-1.21.6`.

## Requirements

- Fabric Loader 0.19.5 or newer
- Fabric API

The edit screen uses vanilla widgets only. owo-lib is no longer required.

## Building

```
./gradlew build
```

The jar is written to `build/libs/` as `pivot-<mod version>+<minecraft version>.jar`. A JDK 25 is required.

## Porting notes

Minecraft ships unobfuscated since 26.1, so the project uses Mojang names and the non-remapping Loom plugin (`net.fabricmc.fabric-loom`). `Display.createTransformation` is widened through `src/main/resources/pivot.classtweaker`.
