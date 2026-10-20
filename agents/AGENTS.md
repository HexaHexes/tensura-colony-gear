# Agent rules and conventions â€” Tensura Colony Gear

**Repo:** `HexaHexes/tensura-colony-gear` Â· **Mod ID:** `tensura_colony_gear` Â· **Branch:** `master`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 Â· **Jar:** `build/libs/tensura-colony-gear-<version>.jar`

Folds Tensura: Reincarnated gear into MineColonies' citizen weapon/tool ladder using
only MineColonies' **public registration API** (`Compatibility.registerItemTier`) â€” no
mixins, no access transformers, no crafter recipes.

**Dependencies**
- Required: MineColonies (`minecolonies`).
- Tensura (`tensura`) is the gear source; the ladder keys off tier enum names and
  registry paths, so it stays valid across Tensura updates without recompiling.

**Test:** `./gradlew runGametest` â€” 4 registry-driven gametests. `expectedTests = 4`.

**Gotchas**
- **No mixins, ever.** This is a public-API addon; if something cannot be done through
  MineColonies' public API, it is out of scope â€” do not add a mixin.
- The ladder maps per-item colony level to `maxEquipmentLevel` = building level âˆ’ 1
  (unbounded at max building level).
- **Armor is deliberately not registered** â€” MineColonies derives armor levels from
  armor points; there is no public override.
- Multitools are recognized reflectively via `Compatibility.registerWeaponRecognizer`;
  Tensura 2.0.1.3 registers none, but the recognizer is forward-compatible.
- Shared vanilla compaction (wood/gold/stone=1, iron/diamond=2, netherite=3) is owned
  here; `iceandfire-colony-gear` deliberately stays namespace-pure against it.

---

## Branch sync (do this first)

Before starting any new development in this repo:

1. `git status` â€” confirm what uncommitted work exists. Never disturb or sweep it.
2. `git pull --ff-only` â€” sync with the remote. A fast-forward is the normal case,
   because we push straight to the shared branch.
3. If the pull would **not** fast-forward (the branch has diverged), or if it would
   collide with local uncommitted work, **stop and ask** rather than merging, rebasing
   or stashing on your own.

## Scope doctrine

- **One mod + bundled datapack.** No KubeJS scripts.
- **Data-driven over code** â€” prefer JSON/datapack content to Java when both work.
- **Public API only, no mixins** in the colony-gear-style addon mods. Sanctioned
  exception: `ds-tensura-fix` (tensura-dragon-skin-rebalance), a mixin fix mod by design.

## Verification

- The in-repo GameTest run is the **only** CI gate. Never verify against a live
  CurseForge instance.
- Keep `expectedTests` in `build.gradle` in sync with the `@GameTest` methods.
- **Client-render features cannot be proven headless.** The honest scope is
  "build green + the user's in-game check" â€” say so rather than overclaiming.

## Build environment

- JDK 21 (`JAVA_HOME` -> JDK 21); the toolchain targets Java 21.
- The Gradle wrapper is committed. `./gradlew build` compiles, `./gradlew runGametest`
  (or `runGameTestServer`) runs the GameTest server.
- `libs/` (compile-only) and `testmods/` (runtime closure) are **gitignored** staging
  folders. Never commit third-party jars.
- On `run/` file-lock errors, kill any stale `java` process and retry.

## PowerShell quirks (this org is developed on Windows)

- A `git push` "error" in PowerShell is usually stderr passthrough â€” success is the
  `hash..hash  branch -> branch` line, not the absence of red text.
- `New-Object Type(args)` breaks on arithmetic â€” use `::new()` or a parenthesized
  argument.

## Git & release

- Remote is SSH: `git@github.com:HexaHexes/<name>.git`.
- Push only after tests are green **and** the user has given the go.
- Never sweep the user's uncommitted work into a commit â€” commit only what is intended.
- Commit subjects: concise, imperative.

## House style

- **No icons** unless the user explicitly asks. Icons are being removed across all repos.
- READMEs are **player-facing only**: pitch, mechanics in plain terms, in-game usage,
  required/optional dependencies, credits, license. No build internals, no class names.
- All repos are **LGPL v2.1**, copyright Bitwise Madness. `NOTICE` / attribution files
  are mandatory and must never be deleted.
- Deploy to the Tensura RPG instance **only** when the user asks.

## CI & release model

- `ci.yml` runs on pull requests: a build job and a GameTest job. Both assert a
  non-zero result â€” a run that passes vacuously (0 tests) is a failure.
- `release.yml` runs on every push to the default branch, re-runs the tests, and â€” only
  when the version was bumped â€” tags and publishes a GitHub release with a cumulative
  changelog. Alpha/nightly publishing is opt-in via the `ALPHA_RELEASES` repo variable
  and is **off** by default.
- `version-bump.yml` opens an isolated version-bump PR (only the version file changes).
  Beta/stable bumps must not carry other changes; `-alpha` bumps may bundle work.
- CurseForge publishing (`curseforge.yml`) is **deferred** until a mod has a CurseForge
  listing and the `CURSEFORGE_PROJECT_ID` / `CURSEFORGE_API_KEY` secrets. Do not add it
  speculatively.

