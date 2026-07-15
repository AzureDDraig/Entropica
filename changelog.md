# Changelog — Entropica Multi-Loader Migration Update

---

## Build 000-0a

### Added
*   **Architectury Multi-Loader Infrastructure**: Split the project into `:common`, `:neoforge`, and `:fabric` modules, utilizing platform-agnostic initializers and registration handlers to target multiple loaders from a single codebase.
*   **10-Stage Materia Cycle API**:
    *   Created `MateriaStack` as a generic, stateful base class supporting deep copying, capacity growing/shrinking, and `EssenceType` element tracking.
    *   Developed concrete, stage-specific subclasses (`MateriaFumusStack`, `MateriaSublimataStack`, `MateriaLiquidaStack`, `MateriaVolatilisStack`, `MateriaCoagulataStack`, `MateriaIchorStack`, `MateriaTransmutataStack`, `MateriaPerfectaStack`, `MateriaLiminaliaStack`).
    *   Integrated robust NBT serialization methods (`save`/`load` compound tags) to store and load Materia stack amounts and types across block entity saves, item stacks, and networking packets.
*   **Vapor & Hydraulic Handlers**:
    *   Added the `IVaporHandler` interface defining pressure-ratio dynamics, safe operating capacities (1.0 Pressure), absolute structural failure capacities (3.0 Pressure), and automated filling/draining methods for gaseous Fumus/Sublimata stacks.
    *   Added the `ILiquidMateriaHandler` interface defining simple capacity limitations and fluid transport operations for liquid Materia stacks.
*   **Vapor Pneumatic Pipes & Valve Blocks**:
    *   Added `VaporPneumaticPipeBlock` and `VaporPneumaticPipeBlockEntity` supporting copper, iron, gold, arcanite, diamond, viscanite, resonite, and charged variants.
    *   Added specialized gas-routing blocks including the `VaporPneumaticValveBlock` (manual flow toggle), `VaporPneumaticOneWayValveBlock` (forced unidirectional flow/venting), and `VaporPneumaticDiverterBlock` (network routing control).
    *   Implemented a custom BFS-based gas networking algorithm inside pipe entities to perform gas transport, pressure averaging, overpressure venting, and explosion handlers.
*   **8 Orbis Cell & Calix Variants**:
    *   Registered and implemented 8 new block/block-entity storage pairs to correspond to each stage of the Materia Cycle:
        *   `SublimatedOrbisCellBlock` / `SublimatedOrbisCellBlockEntity` (Stores T3 - Sublimata)
        *   `PneumaticCalixBlock` / `PneumaticCalixBlockEntity` (Stores T4 - Liquida)
        *   `VoltaicCalixBlock` / `VoltaicCalixBlockEntity` (Stores T5 - Volatilis)
        *   `MatrixCalixBlock` / `MatrixCalixBlockEntity` (Stores T6 - Coagulata)
        *   `ThecaCellBlock` / `ThecaCellBlockEntity` (Stores T7 - Ichor)
        *   `VasCellBlock` / `VasCellBlockEntity` (Stores T8 - Transmutata)
        *   `MonadCoreBlock` / `MonadCoreBlockEntity` (Stores T9 - Perfecta)
        *   `AthanorCoreBlock` / `AthanorCoreBlockEntity` (Stores T10 - Liminalis)
*   **Universal Decompression Coupler (T2-T10 one-way conversion)**:
    *   Added `DecompressionCouplerBlock` and `DecompressionCouplerBlockEntity` to perform straight-line, axis-aligned one-way Materia conversions (draining from higher tier, converting, and filling lower tier neighbor).
    *   Added coupler block models (`vapor_decompression_coupling_core.json`, `vapor_decompression_coupling_arm.json`), item model, and a `multipart` blockstate that renders the core and opposite-facing arms along its axis.
    *   Generated a custom 16x16 gradient texture (`decompression_coupling.png`) transitioning from silver to sky-blueish grey with a subtle brushed metal finish.
*   **1.21.10 Flattened Asset Support**:
    *   Converted resource directories to support Minecraft 1.21.10 asset specifications.
    *   Generated model definitions under `assets/entropica/items/` containing the new `"model": {"type": "minecraft:model", "model": "..."}` JSON layout structure.
    *   Generated matching 3D and 2D model parents under `assets/entropica/models/item/` and custom multi-part blockstates/models for all blocks.
*   **Dynamic Item Tinting**:
    *   Registered custom item tint source codecs (`entropica:essence_tint` and `entropica:ampoule_tint`) through the `RegisterColorHandlersEvent.ItemTintSources` event.
    *   Added multi-layered model JSON layouts for empty/filled Ampoules and Essence Orbs, mapping layer indexes to dynamic color sources that read element type component data.
*   **Enhanced Pipe & Fluid Shaders**:
    *   **Bottom-to-Top Liquid Levels**: Implemented bottom-to-top Y-axis clipping on T4-T7 conduits based on actual fill ratio, allowing fluid overlays to rise dynamically.
    *   **Volatilis Sparks (T5)**: Added a dynamic 3D electric arc generator that renders glowing, jagged electric paths between the fluid surface and the pipe core walls.
    *   **Ichor Shimmering (T7)**: Implemented a mellow, slow-breathing Sanguine heartbeat shimmer animation.
    *   **Perfecta Sheen (T9)**: Added a smooth, rapid periodic sheen sweep with an 80% cycle cooldown to keep the base color clean.
    *   **Liminalis Gold-Chrome (T10)**: Designed a camera-aligned specular chrome reflection shader with dark edge shading.

### Changed
*   **Generalization of Vapor Transport (Fumus / Sublimata)**:
    *   Generalized `IVaporHandler` and `VaporPneumaticPipeBlockEntity` to handle `MateriaStack` instead of `MateriaFumusStack`, enabling vapor pipes to carry both Fumus (T2) and Sublimata (T3) stacks.
*   **Creative Generator Dynamic Tier Resolution**:
    *   Updated `CreativeMateriaGeneratorBlockEntity` to dynamically query the neighbor's cycle tier (T2-T10) using the coupler's tier checker, pushing the correct `MateriaStack` class into any connected pipe/conduit.
*   **Standardized Terminology (Vis $\rightarrow$ Materia)**:
    *   **Essence Items**: Updated `getName()` inside `EssenceItem.java` to dynamically prefix and format tier-0 items as `"Materia Fragment: "` instead of `"Vis Fragment: "` to match the Materia Cycle terminology.
    *   **Creative Tab Registrations**: Renamed `VIS_ITEMS_TAB` to `MATERIA_ITEMS_TAB` inside `ModCreativeTabs.java` and modified the registry key from `"vis_items_tab"` to `"materia_items_tab"`.
    *   **Translation Mapping Overhaul**: Reconfigured translations in `en_us.json` to replace legacy "Vis" terms with "Materia" or "Materia Fumus":
        *   Changed creative tab title `"itemGroup.entropica.vis_items"` from `"Entropica: Vis Items"` to `"Entropica: Materia Items"`.
        *   Updated items: `"Vis Value Detector"` $\rightarrow$ `"Materia Value Detector"`, `"Vis Capacitor Plate"` $\rightarrow$ `"Materia Capacitor Plate"`.
        *   Updated blocks: `"Vis Exhaust"` $\rightarrow$ `"Materia Exhaust"`.
        *   Updated gas settings: `"Vis Fume Settings"` $\rightarrow$ `"Vapor Pneumatic Network Settings"` and `"Vis Fume Pressure Vessel"` $\rightarrow$ `"Materia Fumus Pressure Vessel Settings"`.
        *   Updated item descriptions and tooltips: `"ambient static Vis"` $\rightarrow$ `"ambient static Materia"`, `"Vis Fumes"` $\rightarrow$ `"Materia Fumus"`, and `"Vis Vitae *"` $\rightarrow$ `"Materia Vitae *"` (e.g. `"Materia Vitae Pipe"`, `"Materia Vitae Anchor"`).
*   **Pipe Capacity & Progression Rebalance**:
    *   **Gold Pipes**: Re-slotted as an early-to-mid tier upgrade option, lowering default capacity from `500` to **`80`** and default transfer rate from `50` to **`20`** in `EntropicaNeoForgeConfig.java` and `EntropicaConfigImpl.java`.
    *   **Diamond Pipes**: Shifted up the progression ladder to sit between Charged Arcanite and Resonite, increasing default capacity from `80` to **`250`** and default transfer rate from `20` to **`40`**.
    *   **Viscanite Pipes**: Reduced safe capacity from `500` to **`300`** to match the Resonite capacity tier.
    *   **Charged Viscanite Pipes**: Reduced safe capacity from `1,000` to **`500`** to match the balanced top-tier limit.
    *   **Charged Resonite Pipes**: Reduced safe capacity from `50,000` to **`500`** (and transfer rate to **`5,000`**) to align on the same late-game capacity tier as Charged Viscanite while retaining a high flow rate.

### Fixed
*   **Gradle Production Transform Failures**:
    *   Resolved an issue where Architectury Loom's `transformProduction` tasks threw silent `ClassNotFoundException` errors and outputted empty production jars due to a lack of classpath references inside its task ClassLoader.
    *   Implemented a Gradle hook in [common/build.gradle](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/build.gradle) that dynamically extracts `compileClasspath` URL paths, registers them into the task ClassLoader at runtime in `doFirst`, and re-runs the class transformation in `doLast` to cleanly compile production jars for Fabric and NeoForge.
*   **Fabric Translucency Rendering Crash**:
    *   Resolved a compilation and launch crash inside [EntropicaClientFabric.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/fabric/src/main/java/ddraig/net/entropica/fabric/EntropicaClientFabric.java) where obsolete `RenderType.translucent()` references caused classloader failures on startup.
    *   Mapped block rendering translucency sheets to Minecraft 1.21.10 standard render map sheets `Sheets.translucentItemSheet()`.
*   **Missing Legacy Assets**:
    *   Resolved an asset loading issue where the new multi-loader sub-modules failed to pull assets from the legacy mod directories.
    *   Migrated **358 missing asset files** (comprising textures, block models, blockstates, item animations, and sound loops) from the root legacy folder path to `common/src/main/resources/assets/` to ensure all elements compile.
*   **Vapor Pipe Placement Crash**:
    *   Fixed a client-side classloading desynchronization crash that occurred when placing a Vapor Pneumatic pipe in a world.
    *   Resolved a JVM offset cache conflict by ensuring nested inner classes (specifically `PipeTier` inside `VaporPneumaticPipeBlockEntity`) load correctly on the main thread during world ticking events.
*   **Optional Entity Tag Registry Warnings**:
    *   Converted modded drops entity tags (like `alexsmobs`, `twilightforest`, etc.) to use the optional registry format (`"required": false`), preventing console warnings and load validation failures when optional mods are not present.
*   **Missing Item Model Warnings**:
    *   Created item model JSON files under `assets/entropica/items/` for all 10 custom alloy vapor pneumatic pipes and the `materia_pump` to comply with Minecraft 1.21.2+ asset directory requirements, resolving startup missing-model errors.
*   **Creative Generator Rendering Crashes**:
    *   Refactored `CreativeParticleGeneratorRenderer`, `CreativeMateriaGeneratorRenderer`, and `CreativeVisFumeGeneratorRenderer` to extract player hover checks during the thread-safe client game-tick `extractRenderState` phase, avoiding illegal concurrent queries to game-thread state on the render thread during `submit`.
    *   Added `bufferSource.endBatch()` inside submit methods to cleanly finalize text rendering buffers.
*   **Registry Bounds Safety**:
    *   Added bounds checks to `CreativeParticleGeneratorBlockEntity.loadAdditional()` to prevent startup indexing crashes when `validParticles` list is empty.
