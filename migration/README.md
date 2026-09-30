# Forge 1.20.1 migration

This branch ports the original 1.16.5 Forge implementation. It is **not yet a
working 1.20.1 release**. Compilation is a prerequisite, not the acceptance test.

## Provenance

- Base: `StandoByte/Ripples-of-the-Past`, branch `1.16.5`, commit
  `72862a5826ed45d1dc26e90b37853097acda35de`.
- Reference only: `StandoByte/Ripples-of-the-Past-1-21-1`, commit
  `1fe3ac189908aea3957ce716b0a560318d3d2e21`.
- Baseline: 1,273 Java files, 158,639 Java lines, 2,235 resource files.
- `baseline-files.json` records the original source/resource paths. Moves or
  replacements must be accounted for; compiling a subset is not a migration.
- Keep the `jojo` namespace and all existing content IDs and NBT keys.

## Toolchain and dependencies

- Java 17; Forge 1.20.1-47.4.10; ForgeGradle 6.0.54; Gradle 8.8.
- Official Minecraft 1.20.1 mappings; Mixin 0.8.5 annotation processor.
- playerAnimator 1.0.2-rc1+1.20; bendy-lib 4.0.0.
- JEI 15.62.0.217, Vampirism 1.20.1-1.10.17, ExpandAbility 9.0.4
  are compile-time optional integrations, preserving their original status.
- Mocha 3.0.1 remains shaded and relocated, as in the original project.
- Dependency coordinates are pinned; API compatibility still needs compilation
  and runtime validation, including animations with and without optional mods.

The local development host has Homebrew OpenJDK 17.0.15 (aarch64). Use a Java 17
`JAVA_HOME` when invoking `./gradlew`; no absolute JDK path is committed.

## Build and run

```sh
./gradlew compileJava
./gradlew build
./gradlew runClient
./gradlew runClient2
./gradlew runServer
./gradlew runGameTestServer
```

The run directories are separate (`run-client`, `run-client2`, `run-server`,
`run-gameTestServer`, `run-data`). The second client has a different development
username for synchronization testing. The legacy tracked `run/mods` JAR targets
1.16.5 and is not used in the new run directories.

## Migration order and acceptance

1. Build/toolchain, metadata, mappings and dependency resolution.
2. Vanilla and custom registries: retain IDs, ordering and numeric wire lookup.
3. Capability: registration, serialization, attachment invalidation, clone,
   save/load and dimension transfer, retaining all original NBT fields.
4. SimpleChannel: packet identity/order, direction, main-thread handling and
   dedicated-server class loading; exercise two clients and integrated server.
5. Stand core and player progression/persistence.
6. Client rendering, animation, input and HUD; retain optional integrations.
7. Combat, projectiles, effects and damage types.
8. Star Platinum, The World/time stop, then every other Stand.
9. Hamon, Vampirism, Pillar Man, Zombie.
10. Worldgen, dimensions, loot, recipes, commands and remaining systems.

Do not remove source sets, disable mixins/features, or add placeholder behavior
merely to make compilation pass. Where APIs disappeared, record the original
behavior and its replacement before implementing it.

## Runtime test matrix (not yet executed)

Record build commit, exact mods, launch command, world mode, steps and observed
results for each executed case. An unexecuted case is not a passing case.

| Area | Client / singleplayer | Dedicated server + two clients |
| --- | --- | --- |
| Startup | menu, new world, save/reload | startup, login, reconnect |
| Stand data | obtain/progress, save/reload | owner and tracking-player state |
| Lifecycle | death/respawn, End return, dimension transfer | same, with another observer |
| Networking | integrated-server queues | packet direction, no client classes loaded on server |
| Time stop | enter/exit, partial ticks, animation, projectiles | frozen entities, damage, observers, logout |
| Render/input | HUD, keybinds, all models, particles, player animation | observer rendering and synchronization |
| Other powers | progression, transformations, combat, persistence | owner/observer agreement |
| Worldgen | structures, loot, custom dimensions | generation, restart, chunk unload/reload |

No runtime tests have been performed at project creation. Build logs are kept
locally in `.porting/`; concise verified outcomes belong in `STATUS.md`.

## Progress log (updated while porting)

Compilation is the current gate; the counts below are javac errors from
`./gradlew compileJava` and show how much of the source still has to be ported.

| Milestone | Errors after |
| --- | --- |
| First compile against 1.20.1 (after namespace/type relocations) | 6,927 |
| Access transformers for the private vanilla members the mod touches | 6,927 |
| Batch of verified relocations (JOML, Component factories, nested types) | 5,840 |
| Capability system (tokens, RegisterCapabilitiesEvent, clone handling) | 5,658 |
| Networking (SimpleChannel factory, registry ids, spawn data) | 5,540 |
| Materials/blocks/tool tags and small API changes | 5,540 |
| Model layer rebuilt on the 1.20.1 geometry API | 3,546 |
| Client registration, widgets, render state calls | 3,181 |
| GUI/HUD drawing through GuiGraphics (GuiDraw), tooltips | 2,747 |
| Creative tab on the 1.20.1 builder API | 2,719 |
| Small API batches (input keys, sound events, item RNG, font draws, getEntity) | 2,580 |
| SRG reflection names translated from mapping data | 2,580 |
| Low-level geometry (Cube/Polygon/Vertex), custom cubes, blockbench parsers | 2,331 |
| Buttons via the builder API, screens passing GuiGraphics | 2,318 |
| Vanilla model part typing where vanilla models supply the parts | 2,278 |
| Damage sources on the 1.20.1 damage type registry | 2,226 |
| Remaining damage sources, isRemoved, texture binding calls | 2,200 |
| HUD overlays on RegisterGuiOverlaysEvent/RenderGuiOverlayEvent | 2,131 |
| Walk animation state, child attachment, sound attenuation, texture binds | 2,124 |

Committed systems: build toolchain, namespace/type relocation, capability,
networking, materials/blocks, model layer, client registration/widgets, GUI/HUD
drawing (GuiDraw over GuiGraphics), creative tab, and the small API batches.

Known remaining work, roughly in the order it should be tackled:

1. HUD overlays: `RenderGameOverlayEvent` became `RegisterGuiOverlaysEvent` plus
   `RenderGuiOverlayEvent`, so the mod's overlay handlers (actions HUD, stand
   effects instead of the potion icons, multi-line overlay message) still have to
   be registered and their signatures converted.
2. Low-level model geometry: `ModelBox`/`TexturedQuad`/`PositionTextureVertex`
   and `ClientReflection`'s SRG reflection, used by the Blockbench parsers and
   the custom cube subclasses (MeshModelBox/SlopeModelBox/CustomVerticesModelBox).
2. Worldgen: `Structure` is not generic, `StructureStart`/`StructureFeature`
   registration changed, `WorldGenRegistries`/`DimensionStructuresSettings` are
   gone (datapack worldgen), and the AT that strips `final` from `StructureStart`
   still has to be added.
4. Mixins and access transformers: targets, descriptors and SRG names still need
   a pass, plus `ObfuscationReflectionHelper` strings.
5. Optional integrations: bendy-lib's `IBendHelper` API changed, JEI/Vampirism
   entry points need their 1.20.1 shapes.

Runtime testing has not started: the build does not compile yet, so nothing has
been run in the client, a singleplayer world or a dedicated server. The local game
instance the user provided is `versions/1.20.1-Forge` (Forge 1.20.1, BootstrapLauncher)
with an empty `mods` directory; testing it will also need playerAnimator and
bendy-lib, which are the same libraries the mod already depends on.
