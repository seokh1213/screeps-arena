# Screeps Arena Kotlin Starter

Kotlin/JS starter for Screeps Arena.

## Project structure

- `screeps-types`: external bindings and shared type utilities
- `bot-app`: bot logic and exported `loop()` entry
- `arena/upload`: output files for the Arena client

`bot-app` depends on `screeps-types`.

## Build

```bash
./gradlew build
```

## Deploy (copy JS to `arena/upload`)

```bash
./gradlew deploy
```

This keeps `main.mjs` as the bot entry file.
