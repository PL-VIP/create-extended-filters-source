# Create Extended Filters

NeoForge addon for [Create](https://www.curseforge.com/minecraft/mc-mods/create) that extends the **List Filter** with per-component exclusions when **Respect Data** is enabled.

Match items by type and data, but ignore specific data components (or nested fields inside them) — for example, treat adult and baby axolotl buckets as the same item while still respecting everything else.

## Features

- **Component exclusions** — ignore whole data components or nested fields (e.g. `Age` inside bucket entity data)
- **Create-style GUI** — hover **Exclude components** in the filter screen to browse and toggle exclusions
- **Hierarchical browsing** — expandable components open sub-fields; use **← Back** to return
- **Works in contraptions** — filtering logic runs on the server, so funnels, deployers, and other Create logistics behave correctly
- **Polish & English** — UI strings available in `en_us` and `pl_pl`

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1+ |
| Create | 6.0.10 – 6.2.x |

## Installation

This mod must be installed on **both the client and the server** in multiplayer.

1. Install NeoForge 1.21.1, Create, and this mod
2. Place the `.jar` in the `mods` folder (client and dedicated server)
3. Launch the game

Single-player works out of the box (integrated server includes the mod automatically).

## Usage

1. Open a **List Filter** and enable **Respect Data**
2. Add reference items to the filter slots
3. Click **Exclude components** in the footer below the filter buttons
4. Hover the text to open the component list (scroll to select, click to toggle)
5. Excluded fields appear **struck through in red**
6. Confirm with the checkmark to save

### Example

- Reference item: **adult axolotl bucket**
- Exclude: `Age` (nested under bucket entity data)
- Result: **baby axolotl buckets** also pass through a whitelist filter

## Building from source

Requires **Java 21**.

```bash
git clone https://github.com/PL-VIP/create-extended-filters-source.git
cd create-extended-filters-source
./gradlew build
```

The compiled mod JAR is in `build/libs/`.

Run the development client:

```bash
./gradlew runClient
```

## How it works

Excluded paths are stored on the filter item as a data component (`createextendedfilters:filter_excluded_components`):

- `minecraft:bucket_entity_data` — ignore the entire component
- `minecraft:bucket_entity_data|Age` — ignore only the nested `Age` field

When matching items, Create's List Filter logic is extended to compare stacks while skipping excluded paths.

## License

MIT — see [LICENSE](LICENSE) if present, or the `mod_license` field in `gradle.properties`.

## Credits

- [Create](https://github.com/Creators-of-Create/Create) by the Creators of Create
- Addon by **sure**
