# Project: Forcefield & Firmament Barrier System (Entropica)

## Architecture
- **Multi-Loader Core**: Architectury API targeting Minecraft 1.21.10 (`common`, `fabric`, `neoforge`).
- **Data & Entity Model**: Paper-thin, non-block entity `ForcefieldBarrierEntity` with continuous swept collision detection, avoiding block voxel grid restrictions so blocks and machines can pass directly through.
- **Pluggable Registries**:
  - `BarrierShapeRegistry` & `BarrierShapeHandler` for arbitrary mathematical primitives.
  - `ApexPredatorThemeRegistry` & `ApexPredatorTheme` for standard thin-film and the 6 Apex Predators of Astral Materia.
- **Rendering Pipeline**: Procedural vertex emission using `SubmitNodeCollector` and `RenderType.entityTranslucentEmissive(WHITE_TEXTURE)` with full emissive light (`FULL_LIGHT = 15728880`). Ensures 100% compatibility with shader engines (Iris, Sodium, Oculus) without fragile CoreShader replacements.
- **Physics & Collision Engine**: Swept continuous collision detection in `BarrierFieldManager` and `EntityMixin`, sorting by earliest collision parameter $t_{\min}$, evaluating approach normal $d \cdot n$, side-of-approach elastic velocity reflection $v' = v - (1+e)(v \cdot n_{\text{effective}})n_{\text{effective}}$, and directional one-way valve pass-through.
- **Creator UI & Networking**: Extended container menu `BarrierConfigMenu`, celestial screen `BarrierConfigScreen`, and C2S `UpdateBarrierConfigPayload` with server-side creator/creative validation.
- **Weaver Utilities & In-World Preview**: `FirmamentWeaverItem` with Two-Point Drag & Snap, 0.5m Edge-Fusing, holographic wireframe preview, look-at telemetry, and quick dispel.
- **Physics Parity**: `GravitonBouncepadBlock` with gravity-relative upward launch ($-g$), analogue redstone scaling (0–15), and fall damage landing immunity.

## Feature Inventory
| # | Feature | Description | Milestone | Source |
|---|---------|-------------|-----------|--------|
| 1 | Compilation Baseline Fix | Add missing `BarrierRenderState` to `ForcefieldBarrierRenderer.java`, fix mixin shadowing | M1 | Survey 1, 2, 3 |
| 2 | Modular Geometry Handlers | Pluggable `BarrierShapeHandler` & `BarrierShapeRegistry` for 6 primitives (QUAD, DISC, DOME, BUBBLE, CYLINDER, POLYGON) | M1 | R1 |
| 3 | Modular Theme Architecture | `ApexPredatorThemeRegistry` & `ApexPredatorTheme` for thin-film rainbow and 6 Apex Predator themes | M1 | R1 |
| 4 | Continuous Swept Collision (CCD) | Earliest-$t$ swept intersection sorting, swept projectile tick interception, safe boundary displacement | M2 | R5 |
| 5 | Side-of-Approach Velocity Reflection | $v' = v - (1+e)(v \cdot n)n$, tangential conservation, minimum rebound impulse, ripple impact payload | M2 | R5 |
| 6 | One-Way Directional Valve Mode | `DATA_ONE_WAY` synched boolean, passage check ($d \cdot n < 0$ pass, $d \cdot n > 0$ reflect), drifting starlight arrows | M1, M2 | R2, R5 |
| 7 | Redstone & Materia Switchability | `DATA_IS_ACTIVE`, `DATA_REDSTONE_MODE`, neighbor signal sampling, dormant collision-free state, Materia linking | M2 | R2 |
| 8 | Materia Color Tinting | `DATA_COLOR_TINT`, right-click with vanilla dyes or Materia crystals, procedural tint blending in shader helper | M1, M3 | R2 |
| 9 | Interactive Creator Config Screen | `BarrierConfigMenu` & `BarrierConfigScreen` (shape, filter, width, height, radius, elasticity, one-way, redstone, whitelist) | M4 | R3 |
| 10 | Network Config Synchronization | Expand `UpdateBarrierConfigPayload` (C2S), creator/creative validation, client broadcast | M4 | R3 |
| 11 | Two-Point Drag & Snap | Right-click Point A (anchor beacon), right-click Point B (auto-span planar barrier between A and B) | M3 | R2 |
| 12 | Edge-Fusing / Seamless Snapping | 0.5m snap distance query in `BarrierFieldManager`, vertex/angle flush alignment to eliminate z-fighting | M3 | R2 |
| 13 | Holographic Placement Preview | Real-time starlight wireframe preview when holding Weaver (`FirmamentHologramPreviewRenderer`) in Fabric & NeoForge | M3 | R4 |
| 14 | Weaver Fast-Action Utilities | Shift+Scroll shape cycling, creator look-at telemetry HUD, quick dispel on left-click (`SoundEvents.BUBBLE_POP`) | M3 | R4 |
| 15 | Boss Arena Barrier Integration | Invulnerability while Apex Predator lives (`hurtServer` fix), `EntityEvent.LIVING_DEATH` dissolution fanfare | M2 | R5 |
| 16 | Graviton Bouncepad Parity | Omnidirectional mounting, gravity-relative upward launch ($-g$), analogue redstone scaling (0–15), landing fall immunity | M2 | R6 |
| 17 | Registries, Recipes & Localization | Recipes for `firmament_weaver` and `graviton_bouncepad`, complete `en_us.json` strings | M5 | R7 |
| 18 | Entropic Codex Integration | Research nodes in `CodexCategoryRegistry.java` branching from `MAGIC` and `MACHINERY` hubs | M5 | Custom Rules |
| 19 | Obsidian OKF Vault Synchronization | Update `firmament_weaver.md`, `graviton_bouncepad.md`, `forcefield_barriers.md` in OKF vault | M5 | R7, Custom Rules |
| 20 | Multi-Loader Build & Deployment | `./gradlew --no-parallel build` (0 errors), `./gradlew --no-parallel deploytoDev`, `changelog.md` under `Build 000-1-26-253` | M5 | R7 |

## Milestones
| # | Name | Scope | Dependencies | Status |
|---|------|-------|-------------|--------|
| M1 | Geometry, Themes & Rendering | Baseline compilation fix, `BarrierShapeRegistry`, `ApexPredatorThemeRegistry`, procedural shader & starlight arrows | none | DONE |
| M2 | Physics, CCD, One-Way & Bouncepad | Earliest-$t$ CCD, projectile swept interception, velocity reflection, one-way valves, boss arenas, bouncepad parity | M1 | DONE |
| M3 | Weaver Utilities, QoL & Holographic Preview | Drag & Snap, Edge-Fusing, holographic wireframe preview, telemetry HUD, quick dispel, color tinting | M1, M2 | DONE |
| M4 | Creator Config GUI & Network Sync | `BarrierConfigMenu`, `BarrierConfigScreen`, screen registration, `UpdateBarrierConfigPayload` expansion | M1, M2 | DONE |
| M5 | Registries, Codex, Vault, Build & Deploy | Recipes, localization, Codex nodes, Obsidian Vault docs, Gradle compile, dev deploy, changelog | M1, M2, M3, M4 | DONE |
| M6 | E2E Testing Verification & Hardening | Phase 1: 100% E2E test suite pass (Tiers 1-4); Phase 2: Adversarial coverage hardening (Tier 5) | M1, M2, M3, M4, M5 | DONE |
| E2E | E2E Testing Track | Requirement-driven test harness, Tiers 1-4 tests covering all features, publishes `TEST_READY.md` | none (parallel) | DONE |

## Interface Contracts

### `BarrierShapeHandler` ↔ `ForcefieldBarrierEntity` / `ForcefieldBarrierRenderer`
- `ResourceLocation getId()`
- `int getOrdinal()`
- `BarrierRaycastHit intersect(Vec3 center, float yRot, float xRot, float width, float height, float radius, Vec3 rayStart, Vec3 rayEnd, double entityRadius, ForcefieldBarrierEntity barrier)`
- `AABB computeBoundingBox(Vec3 center, float yRot, float xRot, float width, float height, float radius)`
- `void render(Matrix4f matrix, VertexConsumer consumer, BarrierRenderState state, PoseStack poseStack)`
- `void renderPreview(PoseStack poseStack, VertexConsumer consumer, Matrix4f pose, float width, float height, float radius, float r, float g, float b, float a)`

### `ApexPredatorTheme` ↔ `ForcefieldShaderHelper`
- `ColorResult evaluateColor(float u, float v, Vec3 normal, Vec3 viewDir, float ageTicks, float rippleIntensity, @Nullable Integer customColorTint)`

### `BarrierFieldManager` ↔ `EntityMixin`
- `boolean checkMovementCollisions(Entity entity, Vec3 startPos, Vec3 endPos)`
- Sorts intersecting candidate barriers by ascending swept hit time $t \in [0, 1]$ before executing reflection.

### `BarrierConfigMenu` ↔ `UpdateBarrierConfigPayload`
- Carries: `entityId, shapeOrdinal, width, height, radius, filterModeOrdinal, themeOrdinal, elasticity, oneWay, redstoneMode, colorTint, whitelistUsernames`
- Verified by server: sender is creator or creative mode player.

## Code Layout
- `common/src/main/java/ddraig/net/entropica/`:
  - `forcefield/`: `BarrierShapeRegistry.java`, `BarrierShapeHandler.java`, `BarrierShape.java`, `ApexPredatorThemeRegistry.java`, `ApexPredatorTheme.java`, `BarrierFieldManager.java`, `BarrierGeometry.java`, `BarrierRaycastHit.java`, `BarrierFilterMode.java`
  - `entity/forcefield/`: `ForcefieldBarrierEntity.java`
  - `item/`: `FirmamentWeaverItem.java`
  - `inventory/barrier/`: `BarrierConfigMenu.java`
  - `network/`: `UpdateBarrierConfigPayload.java`, `BarrierImpactPayload.java`
  - `block/`: `GravitonBouncepadBlock.java`
  - `block/entity/`: `GravitonBouncepadBlockEntity.java`
  - `client/renderer/forcefield/`: `ForcefieldBarrierRenderer.java`, `ForcefieldShaderHelper.java`, `FirmamentHologramPreviewRenderer.java`
  - `client/gui/`: `BarrierConfigScreen.java`, `BarrierTelemetryHudOverlay.java`
  - `client/renderer/`: `GravitonBouncepadRenderer.java`
- `common/src/main/resources/`:
  - `assets/entropica/lang/en_us.json`
  - `data/entropica/recipe/`: `firmament_weaver.json`, `graviton_bouncepad.json`
- `fabric/` & `neoforge/`: client screen registrations, preview renderer hooks, mixins.
- Obsidian Vault: `C:\Users\Ddraig__\Downloads\OBSIDIAN WIKIS\Entropica\Entropica`:
  - `wiki/entities/items/tools/firmament_weaver.md`
  - `wiki/entities/blocks/machines/graviton_bouncepad.md`
  - `wiki/articles/forcefield_barriers.md`
