# BetterMeteor

BetterMeteor is a Meteor Client addon for Minecraft `1.21.11` focused on crystal combat helpers, render utilities, quality-of-life tools, HUD info, and a few custom UI tweaks.

## What This Addon Changes

### Combat

#### `Crystal`
- `client-crystal`: removes attacked end crystals client-side immediately instead of waiting for the server destroy packet.
- `click-crystal`: when holding an end crystal, left click places crystals on valid blocks so you can spam place and break with LMB.
- `click-swap`: when left clicking obsidian, swaps to a hotbar end crystal by simulating the matching hotbar key.

#### `Human Auto Totem`
- Smart totem switching with separate danger checks and randomized millisecond delays.
- Health-based swapping.
- Elytra collision protection.
- Riptide collision protection.
- Fall-distance protection.
- Explosion protection for TNT, anchors, and end crystals.
- Optional idle pause.

### Render

#### `Show Hitbox`
- Uses vanilla Minecraft hitbox rendering instead of custom ESP boxes.
- Lets you choose exactly which entity types should show hitboxes through a full entity selector.

#### `Item Nametags`
- Renders nametags above selected dropped items.
- Supports stack counts.
- Supports grouped nearby item plates.
- Supports per-item enchantment filtering and enchantment text.
- Includes custom colors and scaling options.

#### `Hand Chams`
- Tweaks first-person hand rendering.
- Can keep or remove hand textures.
- Supports custom hand color.

### Misc

#### `Sign Command`
- Reads a chosen line from a clicked sign.
- Builds a command from a configurable template.
- Opens chat, pastes the command, and sends it automatically.

### Minigames

#### `Murder Mystery`
- Detects Murder Mystery from the scoreboard title.
- Applies shader ESP to dropped gold ingots.
- Applies shader ESP to other players.

### HUD

#### `Ender Pearl Info`
- Shows predicted ender pearl landing timing.
- Shows nearby-player danger at the landing spot.
- Shows nearby crystal danger at the landing spot.
- Shows nearby respawn anchor danger at the landing spot.
- Shows TNT danger at the landing spot.
- Shows water/lava landing info.
- Supports alignment, spacing, text scale, custom text color, and background color.

### UI and Client Tweaks

- Adds a custom `Minigames` module category.
- Adds custom Meteor GUI roundness settings and related window/header styling tweaks.
- Adds widget/window animation-related mixins.
- Adds custom outline rendering support used by the Murder Mystery visuals.
- Adds first-person hand renderer tweaks used by `Hand Chams`.
- Adds entity hitbox filtering hooks used by `Show Hitbox`.

## Development

- Run the `Minecraft Client` configuration from your IDE to launch Minecraft with Meteor Client and this addon loaded.
- Build with `./gradlew build`.
- The built jar will be created in `build/libs`.

## Project Identity

- Mod id: `bettermeteor`
- Mod name: `BetterMeteor`
- Java package: `com.bettermeteor.addon`
- Minecraft version: `1.21.11`
