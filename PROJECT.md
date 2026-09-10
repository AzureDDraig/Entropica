# Project: Gravity Manipulation System for Entropica

## Architecture
- **Framework & Loaders**: Architectury multi-loader (`common`, `fabric`, `neoforge`) targeting Minecraft 1.21.10 on Java 21.
- **Subproject Organization**:
  - `common`: Universal gravity math, spatial fields, status effects, items, blocks, block entities, BER renderers, common mixins.
  - `fabric`: Fabric client/common entrypoints, client events (`HudRenderCallback`, `ClientTickEvents`), Fabric mixins (`CameraMixin`).
  - `neoforge`: NeoForge client/common entrypoints, NeoForge events (`ViewportEvent.ComputeCameraAngles`, `RegisterGuiLayersEvent`), datagen (`CuriosTagProvider`).
- **Data Flow & Lifecycle**:
  - `GravityApi`: Central query & mutation facade for `Attributes.GRAVITY`, `Attributes.SAFE_FALL_DISTANCE`, `Attributes.FALL_DAMAGE_MULTIPLIER`.
  - `GravityFieldManager`: Spatial manager ticking in `SERVER_LEVEL_POST`. Scans entities, manages zero-g item drift, bends projectiles towards singularity centers, repulses in shields, cycles tidal pulse.
  - `GravityCameraHandler`: Client-side 10-tick smoothstep (Hermite cubic) camera roll for 180° inversion and 15°–25° singularity slant. Hooked on NeoForge via `ViewportEvent.ComputeCameraAngles` and Fabric via `CameraMixin`.
  - Third-Person Models: Inverted via `LivingEntityRendererMixin` setting `state.isUpsideDown = true` for inverted entities.
  - Blocks & BERs: `GravitationalAnchorBlock` (multi-mode emitter) and `GravLiftProjectorBlock` (directional pneumatic beam) using 1.21.10 `BlockEntityRenderer<BE, RenderState>` and `SubmitNodeCollector`.
  - Curios/Accessories: `GravitonSolesItem` (`curios:feet`) and `InertialAnchorAmuletItem` (`curios:charm`) registered via `CuriosTagProvider` and data tags.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Core Gravity API & Attributes | `GravityApi.java` managing `Attributes.GRAVITY`, `SAFE_FALL_DISTANCE`, `FALL_DAMAGE_MULTIPLIER` for zero-g, moon, inverted, and crush | M1 | ORIGINAL_REQUEST §R1 |
| 2 | Status Effects Implementation | `WeightlessnessEffect` (I/II/III), `GravitationalCrushEffect`, `InertialAnchorEffect` registered in `ModEffects.java` | M1 | ORIGINAL_REQUEST §R1 |
| 3 | Spatial Gravity Field Container | `GravityField.java` with 7 operational modes, bounding boxes, and spherical containment | M2 | ORIGINAL_REQUEST §R2 |
| 4 | Spatial Gravity Field Manager | `GravityFieldManager.java` level ticking, entity sync, cleanup on exit | M2 | ORIGINAL_REQUEST §R2 |
| 5 | Dropped Item Drift | Zero-G items float and gently bob in 3D space instead of falling | M2 | ORIGINAL_REQUEST §R2 |
| 6 | Projectile Gravitational Lensing | Bends trajectory vectors of arrows, tridents, fireballs toward singularity well center | M2 | ORIGINAL_REQUEST §R2 |
| 7 | Repulsor Dome Deflection | Deflects incoming projectiles outward and pushes mobs away from barrier | M2 | ORIGINAL_REQUEST §R2 |
| 8 | Tidal Pulse Respiration | 80-tick rhythmic cycle (60 ticks moon float / 20 ticks heavy slam) | M2 | ORIGINAL_REQUEST §R2 |
| 9 | 180° Inversion Camera Roll | Smooth 10-tick cubic slerp/smoothstep camera roll rendering ceilings as floors | M3 | ORIGINAL_REQUEST §R3 |
| 10 | Gravitational Slant (Tidal Tilt) | Dynamic 15°–25° camera roll towards nearby singularity wells | M3 | ORIGINAL_REQUEST §R3 |
| 11 | Camera Roll Hooks (Multi-Loader) | NeoForge `ViewportEvent.ComputeCameraAngles` & Fabric `CameraMixin.java` | M3 | ORIGINAL_REQUEST §R3 |
| 12 | Third-Person & First-Person Orientation | Upside-down third-person models on ceilings and intuitive first-person hands | M3 | ORIGINAL_REQUEST §R3 |
| 13 | Screen Shimmer & Chromatic Lensing | `GravityScreenShimmerRenderer.java` 2D accretion overlay near singularities | M3 | ORIGINAL_REQUEST §R3 |
| 14 | Gravitational Anchor Block & BE | Multi-mode field emitter block & block entity with redstone control | M4 | ORIGINAL_REQUEST §R4 |
| 15 | Gravitational Anchor BER | Custom BER with spinning brass gimbals and mode-colored floating core (1.21.10 SubmitNodeCollector) | M4 | ORIGINAL_REQUEST §R4 |
| 16 | Grav-Lift Projector Block & BE | Directional elevator projector (+Y, -Y, horizontal) with 0.35 m/s travel beam | M4 | ORIGINAL_REQUEST §R4 |
| 17 | Grav-Lift Projector BER | Dynamic pneumatic beam renderer with animated wavefront rings | M4 | ORIGINAL_REQUEST §R4 |
| 18 | Graviton Soles Curio | Footwear/curio (`curios:feet`) for ceiling surface locking and zero fall damage | M5 | ORIGINAL_REQUEST §R5 |
| 19 | Singularity Grenade Projectile & Item | Thrown projectile creating 4-second singularity vortex, suction, and implosive collapse | M5 | ORIGINAL_REQUEST §R5 |
| 20 | Inertial Anchor Amulet Curio | Curio amulet (`curios:charm`) granting total immunity to gravity shifts & vortex suction | M5 | ORIGINAL_REQUEST §R5 |
| 21 | Graviton Wand Handheld Tool | Cycles player gravity or inflicts zero-g / crush on target mobs | M5 | ORIGINAL_REQUEST §R5 |
| 22 | Curios & Accessory Tags | `CuriosTagProvider.java` and data pack tags for feet & charm slots | M5 | ORIGINAL_REQUEST §R5 |
| 23 | Registries & Client Renderers | Wire `ModBlocks`, `ModItems`, `ModBlockEntities`, `ModEntityTypes`, Fabric & NeoForge client renderers | M6 | ORIGINAL_REQUEST §R6 |
| 24 | Compilation & Deployment Validation | Clean `./gradlew --no-parallel build` and `./gradlew --no-parallel deploytoDev` | M6 | ORIGINAL_REQUEST §R6 |
| 25 | Rolling Changelog Maintenance | Update `changelog.md` under `Build 000-1-26-252` in simple layman's terms | M7 | Core Rules §4 |
| 26 | Obsidian OKF Vault Documentation | Update/create notes in Entropica OKF vault using Materia terminology (never Vis) | M7 | Core Rules §1, §5 |
| 27 | Graviton Soles Ticking & Curios Detection | Hook `TickEvent.PLAYER_POST` in `Entropica.java`, correct `inventoryTick`, multi-loader Curios/Accessories reflection | M8 | ORIGINAL_REQUEST (2026-09-10) §R1 |
| 28 | Graviton Soles Wall Sticking & Climbing | Crouch-lock on wall, 0.22 m/s forward climbing, -0.22 m/s backward descending | M8 | ORIGINAL_REQUEST (2026-09-10) §R1 |
| 29 | Graviton Soles Ceiling Adhesion | Invert gravity to -0.08 on ceiling contact, 180° camera flip, upside-down model, surface locking | M8 | ORIGINAL_REQUEST (2026-09-10) §R1 |
| 30 | Graviton Soles Sneak-Ledge Gravity Flipping | Corner wrapping between Floor and Ceiling/Wall with 10-tick debounce, chime sound, sparkle particles | M8 | ORIGINAL_REQUEST (2026-09-10) §R1 |
| 31 | Graviton Soles Absolute Fall Negation | Layered attribute modifiers + `EntityEvent.LIVING_HURT` cancellation | M8 | ORIGINAL_REQUEST (2026-09-10) §R1 |
| 32 | True 3D Black Hole Geometry Engine | `GravitationalLensingRenderer.java`: Schwarzschild horizon, photon sphere, warped Einstein arcs, Doppler asymmetry | M9 | ORIGINAL_REQUEST (2026-09-10) §R2 |
| 33 | Singularity Grenade 3D Entity Renderer | `SingularityGrenadeRenderer.java` replacing `ThrownItemRenderer` with 4-second celestial vortex animation | M9 | ORIGINAL_REQUEST (2026-09-10) §R2 |
| 34 | Screen-Space Optical Distortion Overlay | `GravitationalLensingScreenOverlay.java`: quaternion world-to-screen projection, radial refraction, chromatic aberration | M9 | ORIGINAL_REQUEST (2026-09-10) §R2 |
| 35 | Curios Slot Datapack Definitions | `feet.json`, `charm.json`, `head.json`, `hands.json`, `belt.json`, `ring.json` in `data/curios/curios/slots/` | M10 | ORIGINAL_REQUEST (2026-09-10) §R3 |
| 36 | Curios Entity Slot Assignments | `default.json` and `player.json` assigning slots to `minecraft:player` with `data/entropica/curios/` fallback | M10 | ORIGINAL_REQUEST (2026-09-10) §R3 |
| 37 | Standalone Tidal Pulse Resonator Block & BE | `TidalPulseResonatorBlock.java` and `TidalPulseResonatorBlockEntity.java` with 80-tick respiration cycle and redstone toggle | M11 | ORIGINAL_REQUEST (2026-09-10) §R4 |
| 38 | Tidal Pulse Resonator Animated BER | `TidalPulseResonatorRenderer.java` with 4 vibrating tuning prongs, pulsating graviton core, and expanding shockwave rings | M11 | ORIGINAL_REQUEST (2026-09-10) §R4 |
| 39 | Resonator Recipes, Loot Table & Client Wire-Up | Blockstate, models, recipe, loot table, and registration in `ModBlocks`, `ModItems`, `ModBlockEntities`, Fabric & NeoForge | M11 | ORIGINAL_REQUEST (2026-09-10) §R4 |
| 40 | Multi-Loader Compilation & Deployment | Clean `./gradlew --no-parallel build` and `./gradlew --no-parallel deploytoDev` across common, fabric, neoforge | M12 | ORIGINAL_REQUEST (2026-09-10) §R5 |
| 41 | Documentation, Changelog & OKF Sync | `changelog.md` under `Build 000-1-26-252` and OKF Obsidian Vault note `tidal_pulse_resonator.md` | M13 | ORIGINAL_REQUEST (2026-09-10) §R5 |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Core Gravity API & Status Effects | `GravityApi.java`, `WeightlessnessEffect`, `GravitationalCrushEffect`, `InertialAnchorEffect`, `ModEffects.java` | None | DONE |
| M2 | Spatial Gravity Field Manager | `GravityField.java`, `GravityFieldManager.java`, server tick registration in `Entropica.java` | M1 | DONE |
| M3 | Client Camera & Visual Immersion | `GravityCameraHandler.java`, `GravityScreenShimmerRenderer.java`, `CameraMixin.java`, `LivingEntityRendererMixin.java`, client event subscriptions | M1, M2 | DONE |
| M4 | In-World Blocks, Emitters & Renderers | `GravitationalAnchorBlock`, BE & BER, `GravLiftProjectorBlock`, BE & BER, blockstates/models | M1, M2 | DONE |
| M5 | Gear, Curios, Projectiles & Tuning Wand | `GravitonSolesItem`, `SingularityGrenadeEntity` & Item, `InertialAnchorAmuletItem`, `GravitonWandItem`, Curios tags | M1, M2 | DONE |
| M6 | Platform Registries & Build Validation | `ModBlocks`, `ModItems`, `ModBlockEntities`, `ModEntityTypes`, `EntropicaClientFabric`, `ModClientEvents`, full compile & deploy | M1, M2, M3, M4, M5 | DONE |
| M7 | Documentation, Changelog & Wiki Sync | `changelog.md` (Build 000-1-26-252), Obsidian OKF vault notes, Materia terminology audit | M6 | DONE |
| M8 | Graviton Soles Overhaul & Physics | Ticking hook, wall-sticking, climbing, ceiling adhesion, sneak-ledge flipping, fall damage cancellation | M1, M5 | DONE |
| M9 | True Black Hole Gravitational Lensing Engine | `GravitationalLensingRenderer`, `SingularityGrenadeRenderer`, `GravitationalLensingScreenOverlay`, anchor integration | M3, M5 | DONE |
| M10 | Curios Slots Infrastructure & Datapack Definitions | `slots/*.json` and `entities/*.json` across `data/curios/curios/` and `data/entropica/curios/` | None | DONE |
| M11 | Standalone Tidal Pulse Resonator Block & BER | Block, BE, BER, blockstate, models, recipe, loot table, multi-loader registration | M2, M4 | DONE |
| M12 | Multi-Loader Compilation & Dev Deployment | Full gradle build and dev deployment verification across common, fabric, neoforge | M8, M9, M10, M11 | DONE |
| M13 | Documentation, Changelog & Obsidian OKF Sync | Rolling changelog and OKF Obsidian Vault note for Tidal Pulse Resonator | M11, M12 | DONE |

## Interface Contracts
### `GravityApi` ↔ Entities & Fields
- `GravityApi.setGravity(LivingEntity entity, double gravityMultiplier, double fallDistanceMultiplier)`
- `GravityApi.resetGravity(LivingEntity entity)`
- `GravityApi.isInverted(LivingEntity entity) -> boolean`
- `GravityApi.isAnchored(LivingEntity entity) -> boolean`
- `GravityApi.getNearestSingularityPos(Level level, Vec3 pos, double maxRadius) -> Vec3`
- `GravityApi.tickGravitonSoles(LivingEntity entity)`
- `GravityApi.hasGravitonSoles(LivingEntity entity) -> boolean`

### `GravitationalLensingRenderer` ↔ Renderers
- `GravitationalLensingRenderer.renderBlackHole(PoseStack poseStack, SubmitNodeCollector collector, float radius, float ageTicks, int light, int overlay)`

### `GravitationalLensingScreenOverlay` ↔ Client GUI
- `GravitationalLensingScreenOverlay.render(GuiGraphics guiGraphics, DeltaTracker deltaTracker)`

### `GravityFieldManager` ↔ Level Ticking & Blocks
- `GravityFieldManager.registerField(ResourceLocation id, GravityField field)`
- `GravityFieldManager.unregisterField(ResourceLocation id)`
- `GravityFieldManager.tickLevel(ServerLevel level)`
- `GravityFieldManager.cleanupExitedEntities(ServerLevel level)`

### `GravityCameraHandler` ↔ Mixins & Events
- `GravityCameraHandler.clientTick(Minecraft mc)`
- `GravityCameraHandler.calculateCameraRoll(Camera camera, float partialTick) -> float`

## Code Layout
- `common/src/main/java/ddraig/net/entropica/api/`: `GravityApi.java`
- `common/src/main/java/ddraig/net/entropica/gravity/`: `GravityField.java`, `GravityFieldManager.java`
- `common/src/main/java/ddraig/net/entropica/effect/`: `WeightlessnessEffect.java`, `GravitationalCrushEffect.java`, `InertialAnchorEffect.java`
- `common/src/main/java/ddraig/net/entropica/block/`: `GravitationalAnchorBlock.java`, `GravLiftProjectorBlock.java`, `TidalPulseResonatorBlock.java`
- `common/src/main/java/ddraig/net/entropica/block/entity/`: `GravitationalAnchorBlockEntity.java`, `GravLiftProjectorBlockEntity.java`, `TidalPulseResonatorBlockEntity.java`
- `common/src/main/java/ddraig/net/entropica/client/renderer/`: `GravitationalAnchorRenderer.java`, `GravLiftProjectorRenderer.java`, `TidalPulseResonatorRenderer.java`, `GravitationalLensingRenderer.java`, `SingularityGrenadeRenderer.java`, `GravitationalLensingScreenOverlay.java`, `GravityScreenShimmerRenderer.java`
- `common/src/main/java/ddraig/net/entropica/client/camera/`: `GravityCameraHandler.java`
- `common/src/main/java/ddraig/net/entropica/item/`: `GravitonSolesItem.java`, `SingularityGrenadeItem.java`, `InertialAnchorAmuletItem.java`, `GravitonWandItem.java`
- `common/src/main/java/ddraig/net/entropica/entity/projectile/`: `SingularityGrenadeEntity.java`
- `common/src/main/java/ddraig/net/entropica/mixin/`: `LivingEntityRendererMixin.java`
- `fabric/src/main/java/ddraig/net/entropica/mixin/`: `CameraMixin.java`
- `neoforge/src/main/java/ddraig/net/entropica/datagen/`: `CuriosTagProvider.java`
- `common/src/main/resources/data/curios/curios/slots/`: `feet.json`, `charm.json`, `head.json`, `hands.json`, `belt.json`, `ring.json`
- `common/src/main/resources/data/curios/curios/entities/`: `default.json`, `player.json`
- `common/src/main/resources/data/entropica/curios/`: fallback slot and entity definitions
- `changelog.md`: Workspace root rolling changelog under `## Build 000-1-26-252`
- `C:\Users\Ddraig__\Downloads\OBSIDIAN WIKIS\Entropica\Entropica\`: OKF notes for items, blocks, mechanics, and articles
