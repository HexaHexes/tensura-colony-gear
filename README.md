# Tensura Colony Gear

Lets your **MineColonies** citizens wear and wield Tensura: Reincarnated gear, gated by
building level the same way MineColonies gates its own equipment. Guards and workers can
equip Tensura blades and tools once the building is high enough.

## How it works

MineColonies gates citizen equipment by a per-item level: a citizen can use gear whose
level is at most one below their building's level (and anything at the building's max
level). This mod registers Tensura's gear onto that ladder:

| Level | Vanilla (compacted) | Tensura: Reincarnated |
|-------|--------------------|-----------------------|
| 1 | wood, gold, stone | — |
| 2 | iron, diamond | silver, low magisteel |
| 3 | netherite | high magisteel, mithril, orichalcum |
| 4 | — | pure magisteel, adamantite |
| 5 | — | hihiirokane |

- **Blades** — katanas, greatswords, spears, scythes and sickles — are recognised as
  weapons automatically and take their level from the table above.
- **Shields, bows, crossbows and kunai** get their level from their material name.
- **Armor is not touched** — MineColonies already grades armor sensibly from its own
  defense values (silver near iron, low magisteel near diamond, heavier sets at the top),
  so Tensura armor works without this mod listing it.
- **Multitools** are picked up automatically in Tensura versions that register them.

This is the mod that owns the shared *vanino* compaction (wood/gold/stone at 1, iron/
diamond at 2, netherite at 3) so it doesn't clash with the Ice and Fire gear mod.

## Getting it in-game

Nothing to configure — equip a colonist from their building's equipment screen and
Tensura gear appears wherever MineColonies already lists usable gear, once the building
level allows it.

## Dependencies

| | Mod | Why |
|---|---|---|
| **Required** | [MineColonies](https://www.curseforge.com/minecraft/mc-mods/minecolonies) | The citizen equipment system |
| Works with | Tensura: Reincarnated | The gear being added |

Made for the **Tensura RPG** pack (Minecraft 1.21.1, NeoForge). Works alongside
[Ice and Fire Colony Gear](https://github.com/HexaHexes/iceandfire-colony-gear).

## License

GNU Lesser General Public License v2.1 — see [`LICENSE`](LICENSE).
Copyright (c) 2026 Bitwise Madness.
