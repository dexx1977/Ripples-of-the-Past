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

## Build status (this round)

`./gradlew compileJava` finishes with **0 javac errors** and **0 Mixin annotation
processor warnings**, and `./gradlew build` produces
`build/libs/JJBA-RipplesOfThePast-1.20.1-0.2.2.2-snapshot-port.1.jar` (14 MB) with
`mixins.jojo.json` (compatibility level JAVA_17) and a 87 entry refmap. The
shaded `mocha` library is bundled relocated to `dependency/standobyte/jojo/mocha`
and the mod's own classes reference that package, so the released jar is
self-contained.

## Executed runtime tests

| # | Build | Launch | Mode | Observed |
| --- | --- | --- | --- | --- |
| 1 | `b9a0c4056` + runtime fixes | `./gradlew runServer` (`run-server/`, `--nogui`) | dedicated server | Mod loads, every registry and data pack decodes, `jojo:mr_president` dimension loads, `Done (2.483s)! For help, type "help"`, no mod related error |
| 2 | same | `./gradlew runServer` with a temporary smoke data pack | dedicated server | `#minecraft:load` function placed mod blocks and summoned mod entities in the custom dimension, gave mod items, and set a mod block in the overworld: `[ROTP-TEST] start` and `[ROTP-TEST] done` with nothing failing in between |
| 3 | `87a158318` | `./gradlew runClient --args="--quickPlaySingleplayer porttest"` | client (dev) | Mod loading, registries, texture atlases, armour models, model baking (`ModelEvent.ModifyBakingResult`) and `Sound engine started` all pass; the run then stops in the resource reload with `NoClassDefFoundError: team/unnamed/mocha/runtime/value/Value`, a ForgeGradle dev classpath limitation (the library is on the plain classpath while the mod's dev module is not allowed to read it). The production jar carries the relocated classes, so the shipped mod is unaffected |

Two dev side workarounds were tried and both failed to make the library visible
to the mod's dev module: keeping it on the Gradle runtime classpath (it is there,
`dependencies --configuration runtimeClasspath` lists it) and switching it to
Forge's `jarJar`. The build file was reverted to the shade plus relocate setup,
which produces a self contained release jar.

The client case 3 is **not** a full client pass: the world was never entered,
because the resource reload aborts on the dev classpath issue above. The
recommended way to finish client, singleplayer and multiplayer testing is to drop
the built jar into a real 1.20.1 Forge instance (plus playerAnimator and
bendy-lib, which the mod declares as optional) instead of running from the dev
workspace.

Runtime defects found and fixed by these runs are listed in
[`PROGRESS-zh.md`](PROGRESS-zh.md) section 7 (resource location validation,
dimension data, reflection SRG names, item renderer dispatch, immutable model
cuboids, the read only model registry, the baked item overrides, the loot
modifier json shape, the bucket model loader and three mixin descriptors).

## Runtime test matrix (executed cases are marked)

Record build commit, exact mods, launch command, world mode, steps and observed
results for each executed case. An unexecuted case is not a passing case.

| Area | Client / singleplayer | Dedicated server + two clients |
| --- | --- | --- |
| Startup | **partial** (see case 3: dev run stops at resource reload) | **done** (case 1: startup) |
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
| Custom buttons (renderWidget), widget Tooltips, scene translate calls | 2,073 |
| PlayerAnimator bending on the 1.20.1 stateless bend API | 2,044 |
| Save file capability accessors, data serializer registry | 2,012 |
| Collision helpers (ReuseableStream, horizontal distance) | 1,991 |

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
3. Magic damage audit (done): the 1.16.5 sources call setMagic() nowhere; the only
   living reader is bleed(), which skips bleeding for magical damage, plus the
   wrapper's isMagic() mirror (no callers). Both now ask the mod tag jojo:magic, which
   holds exactly the damage types the magic flagged 1.16.5 sources map to (magic,
   indirect magic, thorns). The witch resistant tag is not used for this: it means "the
   witch resists this" and, while it happens to contain those three types, it also
   contains sonic boom, which did not exist in 1.16.5 - it is a superset of the old
   flag, not an equivalent of it. No mod damage type sets the flag in 1.16.5, so
   none joins the tag. The bypassMagic flag is a separate idea ("ignores the resistance
   effect") and maps to the bypasses_resistance tag. bypassMagic() means "ignores Resistance", not "is magical", and maps to
   BYPASSES_RESISTANCE. Two call sites were fixed: jojo:mowzie_sun had asked for
   armour bypass, resistance bypass and fire in 1.16.5 (bypassArmor/bypassMagic/
   setIsFire) but was in no tag, and jojo:healthLink had been put into both bypass tags
   although its old source bypassed nothing.
4. The pillarman self detonation used the vanilla on fire damage source with the
   explosion flag. 1.20.1 keeps damage properties in the type, so the mod ships
   jojo:on_fire_explosion: its message id is the same onFire and it is listed in both
   minecraft:is_fire and minecraft:is_explosion, which restores the fire behaviour
   (fire immunity checks, no bleeding) and the explosion behaviour (blast protection,
   explosion checks) of the old source.
5. Removed file: client/render/world/TimeStopWeatherHandler.java (and its now empty
   directory). It implemented Forge's weather render handlers, which 1.20.1 dropped,
   but it was already dead code in the 1.16.5 base - the single reference is a
   commented out field in ClientTimeStopHandler and the class is marked "not used in
   the code anymore" and deprecated - so no behaviour depended on it and the 1.21.1
   port no longer has it either. Removal was confirmed with the repository owner.
6. Approximation: the stand chat message used ForgeHooks.onServerChatEvent to let other
   mods rewrite the text. 1.20.1 fires the chat event on the server side instead, and a
   signed player chat message cannot be forged, so this synthetic message is built and
   broadcast as a system message with the stand's name. This is an intentionally
   accepted compatibility difference: the message no longer passes through
   ServerChatEvent, so other mods cannot cancel or rewrite it the way they could in
   1.16.5.
7. Approximation: the input tick takes a slow down factor in 1.20.1; the fake client
   player passes 0.3, the vanilla sneak value, where 1.16.5 only passed the flag.
   Likewise the suffocation check walks the blocks in its box because the 1.20.1
   collision query no longer takes a block state predicate.
8. Compatibility difference: Screen#passEvents (the flag that told the game whether a
   screen lets key events through) no longer exists in 1.20.1, and there is no general
   query that replaces it. ROTP's own pass through screens are still recognised - they
   are WasdAllowingScreen subclasses and rely on their own key handling (keyReleased
   returning false, a non pausing screen, the search field consuming input while it has
   focus) - so their behaviour is restored. What cannot be restored without touching
   other mods is the general case: any vanilla or third party screen that allowed key
   pass through by setting that field in 1.16.5 is now treated as blocking input, so the
   mod's key bindings are ignored while such a screen is open. the 1.16.5 setters (setProjectile, setExplosion,
   bypassArmor, bypassMagic, setIsFire, setScalesWithDifficulty): a wrapper cannot
   change these in 1.20.1, so each call site has to pick a damage type carrying the
   right tags. Still to do.
5. Mob kill hook: 1.16.5 overrode `Mob.killed(ServerLevel, LivingEntity)` (and
   `awardKillScore`) to react to a mob killing something. Neither method exists in
   the 1.20.1 classes (checked in the mapped jar), so those overrides have to move to
   a hook that does exist - a `LivingDeathEvent` listener or overrides of the attack
   path - which is a behavioural decision rather than a rename.
4. TemporaryDimensionEffects: 1.16.5 stored sky/cloud/weather renderers on the
   dimension effects object through Forge interfaces. Those interfaces are gone and
   1.20.1's DimensionSpecialEffects is a plain data holder (the drawing lives in
   LevelRenderer and is hooked through RenderLevelStageEvent), and the effects object
   cannot be mutated, so the class now only keeps the per-dimension stack of the
   mod's own handler sets. Applying a stored set needs the level renderer stages;
   nothing in the mod registers handlers today, so no visible behaviour changed.
4. Worldgen: `Structure` is not generic, `StructureStart`/`StructureFeature`
   registration changed, `WorldGenRegistries`/`DimensionStructuresSettings` are
   gone (datapack worldgen), and the AT that strips `final` from `StructureStart`
   still has to be added.
4. Mixins and access transformers: targets, descriptors and SRG names still need
   a pass, plus `ObfuscationReflectionHelper` strings.
6. Optional integrations: bendy-lib's `IBendHelper` API changed, JEI/Vampirism
   entry points need their 1.20.1 shapes.

Runtime testing has not started: the build does not compile yet, so nothing has
been run in the client, a singleplayer world or a dedicated server. The local game
instance the user provided is `versions/1.20.1-Forge` (Forge 1.20.1, BootstrapLauncher)
with an empty `mods` directory; testing it will also need playerAnimator and
bendy-lib, which are the same libraries the mod already depends on.
