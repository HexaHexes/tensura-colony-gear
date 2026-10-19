# Tensura Colony Gear

Folds **Tensura: Reincarnated** gear into **MineColonies'** citizen weapon/tool
ladder using only MineColonies' public registration API - no mixins, no access
transformers, no crafter recipes. Guards and workers can wear and wield Tensura
gear on MineColonies' building-level progression.

## The ladder

MineColonies gates citizen equipment by a per-item colony level: a citizen can
use gear whose level is `<= maxEquipmentLevel`, where `maxEquipmentLevel` is the
building's level minus one (unbounded once the building is at its max level).

Tools and weapons registered by this mod:

| level | vanilla (compacted)             | Tensura: Reincarnated                                  |
|-------|---------------------------------|--------------------------------------------------------|
| 1     | wood, gold, stone               | -                                                      |
| 2     | iron, diamond                   | silver, low_magisteel                                  |
| 3     | netherite                       | high_magisteel, mithril, orichalcum                    |
| 4     | -                               | pure_magisteel, adamantite                             |
| 5     | -                               | hihiirokane                                            |

Tensura blades (katanas, greatswords, spears, scythes, sickles) extend the
vanilla `SwordItem`/`DiggerItem` hierarchy, so MineColonies already recognizes
them as weapons; this mod supplies their levels. Shields, bows, crossbows and
kunai get their levels from a material-fragment fallback on the registry path.

**Armor is deliberately not registered.** MineColonies derives armor levels from
each piece's armor points against the vanilla per-slot distributions, and there
is no public override. Tensura's sets already grade sensibly that way (silver
near iron, low_magisteel near diamond, heavier sets at the top grade).

Multitools: Tensura 2.0.1.3 ships the `MultitoolItem` class but registers no
items of it. The mod nevertheless recognizes multitools as custom weapons via
`Compatibility.registerWeaponRecognizer` and reads their tier reflectively, so
they pick up the ladder automatically in Tensura versions that do register them.

## Building

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-21
gradle build
```

Compile-time dependency: MineColonies' public API (`libs/`, gitignored). The
gametest run stages `testmods/` (gitignored) into `run/mods/` exactly like the
real mod pack, so `gradle runGametest` verifies the ladder against the actual
Tensura and MineColonies builds:

```bat
gradle runGametest
```

Four registry-driven gametests verify the vanilla compaction, every Tensura
tool tier, the name-based fallback (and that MineColonies never overwrites our
registrations at runtime), and multitool handling.

## License

All Rights Reserved.
