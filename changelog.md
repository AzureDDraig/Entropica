# Changelog — Entropica Multi-Loader Migration Update

## Build 000-1-26-201-08-59

### Added & Polished
*   **Specialized Fehu Filters**:
    *   Added `filterType` property to `ScribedChalkBlockEntity` (with full NBT serialization).
    *   Right-clicking a Fehu (Filter) node with an elemental `EssenceItem` binds the filter to that specific element. Right-clicking with empty hand/Tuning Fork resets it.
    *   Dyeing/specialization limits passing signals strictly to elements matching the filter type.
    *   Emits custom-colored `DustParticleOptions` particles matching the active element around the specialized node on client.
    *   **Dynamic Shifting**: The unpowered chalk line color of specialized Fehu filters cycles/shifts through the element's colors (including prismatic shifting for true dynamic/ENTROPICA types) based on level gameTime, matching the active essence channels.
*   **Concentric Alchemical Shell Auto-Drawing**:
    *   Right-clicking an active magic circle `OUTPUT` center node (`CIRCUIT == false`) with `Advanced Chalk` automatically traces/scribes a concentric outer ring of advanced alchemical circuit chalk lines (`CIRCUIT == true`) around the magic circle.
    *   Automatically calculates the distance of offset blocks to the player and leaves the closest block empty as a **connection gap**, allowing you to immediately connect the shell to your alchemical circuit lines. Consumes durability from the held chalk stack.
*   **Diagonal Connection Support**:
    *   Enabled diagonal connections (NE, NW, SE, SW) between alchemical chalk circuit nodes. Non-diode/non-gate chalk blocks placed diagonally now connect and render lines to each other, allowing circles and complex diagonal routing to work correctly.
    *   Restored coaxial shell drawing to place concentric circle offsets (which now connect diagonally to form actual circular loops).
*   **Propagation Strength & Reservoir Charge Separation**:
    *   Introduced a separate `propagationStrength` field (saved/loaded in NBT) to propagate alchemical signals and handle decay (by 1 per block) across the circuit.
    *   Stored essence level (`essenceLevel`) resides solely in the `SOURCE` node. Placing an `EssenceItem` or `EssenceAmpouleItem` into the `SOURCE` node charges its reservoir by its alchemical yield based on size (Tier 0/1 = 1, Tier 2 = 4, Tier 3 = 16).
    *   `EXTRACTION` nodes find connected sources using Breadth-First Search (BFS) and consume matching essence charge (or drain Orbis Cell vis based on `orb.getChargeAmount()`) per extraction, preventing infinite extraction loops from a single essence.
*   **Essence Orb Scaling Alignment**:
    *   Aligned the essence amount of `EssenceOrbEntity` and alchemical circuit `ScribedChalkBlock` insertion with the Entropic Core/Furnace size-based scaling values (Weak/Small = 1, Average/Medium = 4, Strong/Large = 16).
    *   Source nodes now accept both `EssenceItem` and `EssenceAmpouleItem`.
    *   Fallback extraction node orb spawning consumes essence/vis from the source matching the spawned orb's charge amount (`orb.getChargeAmount()`).
*   **Upgraded Extraction Nodes (Pipeline Transfer)**:
    *   Upgrades automatically when placing a pressure conduit or vapor pipe directly **above** the Extraction Node.
    *   Boosts extraction tick rate from 40 ticks to 15 ticks.
    *   Drains and pipes essence directly into the conduit above as gaseous `Materia Fumus` (conserving surplus essence if network is full).
    *   Spawns a vertical rising particle stream of `WITCH` sparkles and plays alchemical chime audio on transfer.
*   **Materia-Infused Zombie Drops & Aesthetics**:
    *   Custom zombie drops a `WEAK_ESSENCE` (Tier 2 essence item level) with random `VITAE` or `BLOOD` element type when slain.
    *   Zombies emit a persistent glowing client trail of purple `WITCH` sparks and red `DustParticleOptions` particles.
*   **Documentation Flowcharts**:
    *   Updated the advanced alchemical circuits guide with clear mermaid flowcharts illustrating connections, logic gate routing, and demultiplexing.

---

## Build 000-1-26-201-08-16

### Added & Polished
*   **Logic Gate Visual Feedback**:
    *   Implemented active/inactive client-side particles on alchemical logic gates (`AND`, `OR`, `NOT`).
    *   Powered gates (`essenceLevel > 0`) emit green alchemical sparkles (`HAPPY_VILLAGER`).
    *   Unpowered connected gates emit occasional grey smoke particles to clearly display signal status.
*   **Repulsion Ward Enhancements**:
    *   Added physical push feedback on both circle-based Algiz wards and dedicated ward blocks.
    *   Spawns expanding cloud shockwave particles and plays a low-frequency hum/whoosh sound when hostile entities are repelled.
*   **Multi-Tier Storage Cells Tooltips**:
    *   Refactored `OrbisCellItem` to dynamically calculate capacity and tier based on block type.
    *   Registered sublimated cells and all higher-tier calixes/cores as `OrbisCellItem` to automatically display beautiful, color-coded Materia capacities and progress bar tooltips.
*   **Terminal Extraction Nodes**:
    *   Restricted `EXTRACTION` nodes to only receive essence from traces, ignoring queries from adjacent nodes to prevent extraction nodes from acting as normal wire trace paths.

---

## Build 000-1-26-200-09-43

### Renamed & Refactored
*   **Materia Renaming**:
    *   Renamed `ManaEnrichedGlass` blocks, block entities, and renderers to `MateriaEnrichedGlass` across all packages and registries.
    *   Renamed `ManaPlume` blocks, block entities, and assets to `MateriaPlume`.
    *   Renamed `ManaFurnace` blocks, block entities, renderers, and assets to `MateriaFurnace`.
    *   Updated the Entropic Core multiblock logic to use Materia Fumus in place of Mana in all locations (`manaPool` to `materiaFumusPool`, NBT saving/loading, etc.).
*   **Config Keys**:
    *   Renamed all configuration fields and registry entries from "Mana" to "Materia" (e.g. `MANA_PER_ESSENCE` to `MATERIA_PER_ESSENCE`, `ORBIS_CELL_MAX_MANA` to `ORBIS_CELL_MAX_MATERIA`).
*   **Storage Cells NBT**:
    *   Updated storage cell NBT storage tags from `"StoredVis"` and `"StoredMana"` to a unified `"StoredMateria<Tier>"` format mapping directly to cell tiers 2-10 (e.g., `StoredMateria2` for Orbis Cells, `StoredMateria3` for Sublimated Orbis Cells).
    *   Renamed `"ManaType"` to `"MateriaType"` in all cell and calix entities.
    *   Updated referencing items, blocks, and the Extraction Node `tryPushVis` ticking logic to dynamically read and write the new NBT keys.
*   **Localization**:
    *   Corrected all translations in `en_us.json` for the renamed items and blocks.
    *   Added translation mapping for `small_materia_fumus_ampoule` and higher tiers to resolve missing item names.

---

## Build 000-1-26-200-02-56

### Added
*   **Extraction Node**: Added a dedicated `NodeType.EXTRACTION` node to advanced circuits.
    *   Automatically extracts matching essence/materia when `essenceLevel >= 16` (2-second cooldown).
    *   Pushes the extracted essence into an Orbis Cell (or sublimated/vas/theca cell) if a connected `INPUT` node is placed directly in the direction the Extraction node is facing.
    *   If the cell is full or not present, it spawns a physical `EssenceOrbEntity` in the world.
    *   Drains the node's essence level to 0 after successful extraction.

### Fixed & Improved
*   **Circuit Node Interactions**: Allowed placing/extracting items, Orbis cells, and runes on circuit nodes (`INPUT`, `SOURCE`, `RUNE`) by checking if the player is holding a `ChalkItem` instead of locking interactions entirely on active circuits.
*   **Path Wave Rendering**: Implemented a lighter, dynamic alchemical wave traveling outwards along the copper traces from active `SOURCE` nodes.
*   **Collection Node Pulse**: Balanced collection nodes to slowly accumulate ambient energy (120 ticks) and emit a level 32 pulse.
*   **Amplifier Overdrive**: Amplifiers now act as repeaters and multiply the ritual recipe output by `1.5x` per amplifier. Connected sources/capacitors drain twice as fast (`1 + 2 * amplifiers` per tick).
*   **Capacitor Recycling**: Capacitor nodes retain 20% of their essence when the ritual completes, recycling it back into the circuit.
*   **Resonator Wireless Sync**: Resonator nodes automatically search for and sync signal levels wirelessly with any other Resonator node placed up to 16 blocks away.

---

## Build 000-1-26-200-02-31

### Fixed
*   **Magic Circle Ritual Recipes**:
    *   Fixed a bug where hardcoded fallback recipes (such as the Iron Ingot + Rune of Uruz + 50 Nether -> Arcanite Ingot placeholder) were not evaluated during ritual execution. Added a search fallback in `ScribedChalkBlock` to scan both registered datapack recipes and hardcoded fallback recipes.
    *   Added a datapack JSON recipe for the `arcanite_ingot` magic circle ritual to ensure native datapack support and correct JEI synchronization.
*   **Wildcard Essence Matching**:
    *   Enabled wildcard essence matching on magic circle recipes. Specifying `EssenceType.REGULAR` (serialized as `"regular"` in JSON) now dynamically matches and sums all active, infused essences in the circle.
    *   Updated the Arcanite Ingot placeholder recipe to require 50 of any essence (using `REGULAR` wildcard) instead of requiring Nether essence.
    *   Updated the JEI magic circle recipe category to render the wildcard requirement as "Any: <amount>".

---

## Build 000-1-26-199-16-25

### Fixed
*   **Magic Circle Rendering (Geometry & Timing)**:
    *   Resolved rendering geometry distortion and infinitely-stretched polygram lines by fixing float precision loss in rotation calculations. Swapped raw system time for a smooth sub-tick game time modulo calculation (`(level.getGameTime() % 360000L) + partialTick`).
    *   Overloaded `getCurrentRGB` in `EssenceType` to support `double` values, enabling smooth client-side alchemical color cycles.
*   **Node Input Lockout**:
    *   Restricted general item, essence, and rune insertion/extraction on `INPUT`, `SOURCE`, and `RUNE` nodes to active magic circles only (`isInActiveCircle() == true`). This prevents nodes from taking chalk or other materials out of the player's hand while they are cycling node types.

---

## Build 000-1-26-199-16-04

### Added
*   **Scribed Chalk & Magic Circles**:
    *   Implemented smooth magic circle rotations by using system time for rendering instead of game ticks.
    *   Added support for dynamic node counts, dynamically adjusting the circle's rendering, polygons, and star shapes based on the exact nodes placed (requiring a minimum of 1 input, 1 source, and 1 rune node, up to the maximum supported by the tier).
    *   Allowed general item insertion and extraction on `INPUT` nodes via right-clicking, and updated alchemical recipe matching/consumption to pull from these stored item slots before searching for floating entities.
    *   Hid individual chalk dots and connections once the magic circle is assembled.
    *   Implemented full stored item dropping behavior on circle breakdown (manual player break or programmatic cascade) for all stored items across input, rune, and output nodes, while destroying the essence.

---

## Build 000-1-26-199-15-46

### Fixed
*   **Scribed Chalk Blocks**:
    *   Resolved a potential null pointer / property lookup crash that occurs when placing, breaking, or ticking chalk lines on the ground. Added safety type checks to verify that the block state is a `ScribedChalkBlock` before attempting to retrieve its `NODE_TYPE` or `CIRCUIT` properties (protecting against air/fallback states during block state updates).

---

## Build 000-1-26-199-15-38

### Fixed
*   **Empty Ampoules**:
    *   Resolved missing textures for the base/empty ampoule items (`small_ampoule_base`, `medium_ampoule_base`, `large_ampoule_base`) by updating their item model JSON files to point directly to the existing empty ampoule texture files, avoiding files duplication.
*   **Decompression Coupling**:
    *   Completed the renaming refactor for `VAPOR_DECOMPRESSION_COUPLING_ITEM` to `DECOMPRESSION_COUPLING_ITEM` in `ModItems.java` and `ModModelProvider.java` to align with the block registry name, resolving compile errors.

---

## Build 000-1-26-199-15-31

### Added
*   **Ores, Crystals & Geodes**:
    *   Added 3 new alchemical ore blocks: Vorpalite, Sorrowstone, and Umbralite, matching the crystal-on-stone structure of Entropic Ore.
    *   Added 7 corresponding raw ore drop items: `entropic_shard`, `vorpalite_crystal`, `sorrowstone_shard`, `umbralite_shard`, `mortisite_geode`, `aeterium_crystal`, and `ignisite_crystal`.
    *   Registered full Amethyst-like crystal/geode blocks for Aeterium, Ignisite, and Mortisite (including Crystal Blocks, Budding Blocks, Small/Medium/Large Buds, and Crystal Clusters) that grow dynamically via custom random ticking behavior.
    *   Generated custom blockstates, directional models, loot tables (supporting Fortune/Silk Touch), mineability tags, and unique seamless textures for all 22 blocks and 7 items.
    *   Added cutout render types to the model JSON files of all crystal buds and clusters, and registered them on Fabric's client BlockRenderLayerMap to render their backgrounds transparently.
    *   Implemented a mathematically solid, tapered crystal drawing algorithm in 32x32 resolution that completely eliminates outline gaps.
    *   Introduced hand-crafted pixel-art dithering/noise and vertical gradients directly within the solid shapes to give them authentic amethyst texture depth and shading.
    *   Significantly increased the shading contrast, diamond facets, and alchemical core brightness of the seamless crystal and budding blocks while preserving border flow.
    *   Redesigned the item textures for shards, crystals, and geodes: created an amethyst-shard-inspired diagonal shape for shards (`entropic_shard`, `sorrowstone_shard`, `umbralite_shard`), an echo-shard-inspired slender crystal needle shape for crystals (`vorpalite_crystal`, `aeterium_crystal`, `ignisite_crystal`), and a cracked stone shell shape for geodes (`mortisite_geode`), complete with rich 9-color alchemical gradients.

### Fixed
*   **Entropic Core**:
    *   Fixed the `entropic_core` item model redirect to render as a 3D block model in-game rather than a flat texture.

---

## Build 000-1-26-199-10-36

### Fixed
*   **Recipe Parsing Compatibility (MC 1.21.4)**:
    *   Fixed data parsing errors where Minecraft 1.21.4 failed to load recipes because ingredients used the legacy `{"item": "..."}` syntax without a specified type. Updated `decompression_coupling`, `arcane_brick_block`, `arcane_clay_block`, and `smelt_arcane_clay` to use direct string values for ingredient keys (`"item_id"`) which parses correctly on the new engine.

---

## Build 000-1-26-199-10-31

### Added
*   **Decompression Coupling Smart Connection**:
    *   Implemented smart alignment logic on placement and neighbor changes. The Decompression Coupling now dynamically scans adjacent axes for any connected pipe, conduit, pipeline, valve, diverter, port, or agitator blocks and automatically aligns its axis (`AXIS`) to match them, allowing seamless connections to the pipe network.

---

## Build 000-1-26-199-10-24

### Changed
*   **Dynamic Weapon Models**:
    *   Mapped all 15 dynamic weapon/tool item models (`dynamic_sword`, `dynamic_axe`, `dynamic_pickaxe`, `dynamic_shovel`, `dynamic_adze`, `dynamic_paxel`, `dynamic_spear`, `dynamic_mace`, `dynamic_morning_star`, `dynamic_warhammer`, `dynamic_shortbow`, `dynamic_longbow`, `dynamic_war_bow`, `dynamic_crossbow`, `dynamic_repeater`) to display their corresponding 2D shape concept textures in-game instead of rendering missing textures.
*   **Decompression Coupling**:
    *   Renamed "Vapor Decompression Coupling" to "Decompression Coupling" across registry identifiers (`decompression_coupling`), localization keys, and datagen classes.
    *   Updated the block's item model to render the pipe core model (`decompression_coupling_core`) instead of the solid block model.
    *   Added a shaped crafting recipe (`ZXZ` where `X` is a pressure-graded gasket and `Z` is essence-enriched glass).

---

## Build 000-1-26-198-07-18

### Added
*   **Essence Repulsion Wards (Magic Circle & Block)**:
    *   **Mystical-Industrial Aesthetics**:
        *   Redesigned the Essence Repulsion Ward block faces to feature a mystical yet industrial look (heavy steel brackets, brass rivets, cosmic deep-purple backgrounds, and glowing neon-teal magic runes/circuits).
    *   **Magic Circle Repulsion Ward**:
        *   Can be activated by right-clicking the center `OUTPUT` node of a valid active magic circle containing a `RUNE_ALGIZ` (Protection/Warding rune) and at least 32 total essence. Consumes the Algiz rune and drains all essences on activation.
        *   Repels hostile monsters (subclasses of `Monster`) away from the circle perimeter for a baseline duration of 10 minutes (12000 ticks).
        *   Can be sustained / fed by right-clicking the `OUTPUT` node with `EssenceItem` (Materia Fragment adds 60s, Weak Essence adds 120s, Average Essence adds 5 mins, Strong Essence adds 20 mins).
        *   Spawns end rod perimeter particles and plays beacon activation/deactivation sounds.
    *   **Dedicated Repulsion Ward Block**:
        *   Added the `essence_repulsion_ward` block and item.
        *   Implements `IVaporHandler` and connects to any `Materia Fumus` pipe network.
        *   Consumes Materia Fumus stack over time (configurable, default 1 Materia Fumus per 120 seconds).
        *   Repels hostile monsters in a 15-block radius when active.
    *   **Local JSON Configuration**:
        *   Creates `config/entropica_ward.json` on startup to customize block consumption amounts, interval timings, and ward radii.
*   **JSON Assets & Localisation**:
        *   Added blockstate, block model, and item model JSON files for the ward block.
        *   Added english localization keys for the ward block and item.

---

## Build 000-1-26-198-01-50

### Added
*   **Runic Logic Gates (AND, OR, NOT)**:
    *   Integrated logic gate circuit blocks into `ScribedChalkBlockEntity` essence propagation.
    *   `AND` and `OR` gates propagate essence based on active inputs from non-facing directions (back, left, right).
    *   `NOT` gate acts as an inverter: blocks output if back input is powered, otherwise propagates signals from left/right inputs.
    *   Restricted connection checking so standard chalk paths cannot pull essence from logic gates unless they align with the gate's output facing direction.
*   **Runic Signal Modifiers (Uruz, Isa, Fehu, Thurisaz)**:
    *   **Uruz**: Acts as a signal booster, resetting essence strength back to maximum level 32 upon crossing the node.
    *   **Isa**: Acts as an insulator, blocking all essence level and color propagation entirely.
    *   **Fehu**: Acts as an essence filter, blocking plain/regular essence from passing through while allowing colored/elemental essence.
    *   **Thurisaz**: Acts as a wireless bridging portal, copying signal values and affinities between Thurisaz nodes within a 32-block radius.
*   **Sacrificial Knife & Ritual Overclocking**:
    *   Added the `SacrificialKnifeItem` dealing 8 points of true magic damage to the player on use.
    *   Performing a sacrifice near an active magic circle overclocks the output node for 30 seconds (double tick rate).
    *   Spawning of Vitae or Blood essence orbs upon successful sacrifice if no active ritual is nearby.
    *   Added dynamic rune upgrades: Uruz (+2 damage, double output yield) and Thurisaz (+2 damage, instant completion).
    *   Spawns a custom named "Materia-Infused Zombie" with persistent Strength I and Resistance II effects if the player dies during sacrifice.
*   **Most-Costly Recipe Selection & Ritual Countdown**:
    *   Implemented sorting to prioritize the most expensive matching magic circle recipe (highest sum of inputs, runes, and total essence amounts).
    *   Magic circles now consume ingredients immediately upon click and run a 5-second (100 ticks) countdown visual/audio ritual before spawning outputs.
*   **JSON Assets & Blockstate Permutations**:
    *   Added the 2D item model JSON for the Sacrificial Knife.
    *   Updated the blockstates file for Scribed Chalk to include all 13 node types (preventing missing variant errors).

---

## Build 000-1-26-197-19-55

### Added
*   **Directional Magical Diodes (One-Way Energy Gates)**:
    *   Added a brand new circuit node type: the **Diode** (represented by a "D" icon and a classic electronic diode schematic symbol). Diodes act as one-way gates for magical essence, letting energy pass through in only one direction.
    *   When you place or cycle a chalk node to a Diode, it automatically aligns itself to face the same direction your player is looking.
    *   Magical energy and colors can only enter the Diode from its backside (input) and flow out of its frontside (output). Energy trying to flow backwards or sideways is blocked.
    *   To prevent energy from leaking to adjacent lines, the Diode will only connect to other chalk blocks located directly in front of or behind it, automatically ignoring any lines trying to connect from the sides.
*   **8-Way Directional Overrides & 45-Degree Diagonal Branching**:
    *   Added manual connection controls for advanced chalk. Sneak+right clicking with the advanced chalk on an existing chalk block detects the cursor angle to target one of 8 direction sectors (cardinal and diagonal).
    *   Toggles between `DEFAULT` (auto-connect), `FORCE_CONNECT` (overriding parallel gates), and `FORCE_DISCONNECT`. Toggles are dynamically synchronized with adjacent chalk blocks.
    *   Implemented full 8-way essence level and color propagation across diagonal/cardinal overrides during chalk ticks.
*   **Soft Curved Corners (Bezier Fillets)**:
    *   Modified the renderer to draw smooth, rounded fillets at 90-degree corners instead of sharp mitered angles.
    *   Uses quadratic Bezier curve math to interpolate rendering segments between center points, while maintaining parallel offsets for side paths.
*   **Magic Circle Recipe Type & Ritual Execution**:
    *   Introduced a custom recipe type (`magic_circle`) using a flexible record structure containing input items, required runes, tier requirements, and required essence amounts.
    *   Implemented full JSON serialization and network packet synchronization via `MapCodec` and `StreamCodec`.
    *   Added a world-interaction trigger: right-clicking a Scribed Chalk block checks if it forms the center of a valid magic circle, scans the node layout for floating inputs/runes/essences, matches a recipe, consumes ingredients, and spawns the output with particle and sound effects.
*   **Just Enough Items (JEI) Compatibility**:
    *   Registered a custom recipe category for Magic Circles showing reagent items, rune requirements, and outputs.
    *   Renders dynamic labels for the minimum required circle tier and color-shifting essence values corresponding to the required essence type.
*   **PCB-Style 45-Degree Mitered Corners (Angled Bend Graphics)**:
    *   Improved the visual rendering of advanced circuit tracks when they turn a corner.
    *   Instead of drawing overlapping square blocks at 90-degree corners, the system now calculates custom coordinates to render smooth, professional-looking 45-degree mitered diagonal bends.
    *   This diagonal bend geometry is rendered for the bright center power line as well as the two thin outer parallel copper paths, making your ritual setups look like actual high-tech printed circuit boards.
*   **Dynamic & Shifting Essence Colors**:
    *   Updated the rendering of both scribed chalk lines, concentric magic circles, and the fluids/gases flowing inside all Vapor Pneumatic Pipes, Hydraulic Pipelines, and Conduits to fully support shifting, multi-stage, and prismatic colors.
    *   Previously, once these components were filled or dyed, they retained a static snapshot of the essence's starting color. Now, if an essence type has shifting color cycles (like Storm, Eclipse, or Prismatic), all of these components will smoothly pulse and flow through their color cycles in perfect synchronization on the client.

### Changed
*   **Smart Corner and Staircase Connection Detection (Improved Parallel Check)**:
    *   Redesigned the connection rules for advanced chalk lines to solve a bug where turns and bends wouldn't connect.
    *   Previously, the game blocked adjacent blocks from connecting if they ran parallel, but this accidentally blocked single circuit lines making a 90-degree bend, a loop, or a staircase-like slope because it thought the adjacent parts were separate parallel wires.
    *   The connection check is now much smarter: it only blocks adjacent lines from connecting if they are running in the exact same direction (running side-by-side). If the lines are turning a corner, stepping down in a staircase, or forming a loop, the system recognizes that they belong to the same path and connects them perfectly.

### Fixed
*   **Magic Circle Color Refresh Animation (Dynamic Re-Dyeing)**:
    *   Fixed a visual bug where changing the element type (color) of your energy source on an active magic circle wouldn't play the dyeing animation, leaving the old color statically visible or updating without smooth transition.
    *   Now, when the chalk circuit registers a change in essence/color, it automatically resets its visual wave timer back to zero. This triggers a fresh wave of color that flows smoothly from the source node throughout the entire magic circle system and all surrounding connections.

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
