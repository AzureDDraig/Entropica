## Build 000-1-26-229

### Core Additions & Features
- **Astral Materia Suite Custom 3D Blockbench Workstations & Optics**:
  - Authored, textured, and configured complete sub-pixel 3D models with clean hierarchical inheritance and dedicated handcrafted texture suites across the primary Astral Materia workstations and optical devices:
    - 🏛️ **`resonance_pylon`**: Classical Astral Marble base plinth with fluted pilasters, glowing Materia flux conduits, and a floating $45.0^\circ$ faceted Starlight Crystal apex. Textures: `resonance_pylon_marble.png`, `resonance_pylon_trim.png`, `resonance_pylon_side.png`, `resonance_pylon_top.png`. Custom block class: `ResonancePylonBlock.java`.
    - 🪨 **`attunement_pedestal`**: 15-cube classical Astral Marble offering plinth with golden fluting, Starlight Obsidian ritual bowl, and $+0.01$ Z-fighting offsets. Textures: `attunement_pedestal_marble.png`, `attunement_pedestal_trim.png`, `attunement_pedestal_side.png`, `attunement_pedestal_top.png`.
    - 🔭 **`stationary_brass_telescope`**: Geared astronomical observation telescope with $+22.5^\circ$ skyward elevation aim, brushed antique brass on deep Arcanite mount, and strict 3-tier kinematic inheritance (`base` $\to$ `azimuth_turntable` $\to$ `elevation_barrel`). Textures: `stationary_brass_telescope_base.png`, `stationary_brass_telescope_brass.png`, `stationary_brass_telescope_lens.png`.
    - 🧭 **`celestial_armillary_controller`**: Openwork astrometric cage with concentric astrolabe horizon dial, radiant starlight core, 4-band multi-tonal ring textures with $90^\circ$ UV alignment, and strict 5-tier kinematic inheritance (`base` $\to$ `meridian_frame` $\to$ `outer_colure_ring` $\to$ `inner_ecliptic_ring` $\to$ `celestial_core`). Textures: `celestial_armillary_controller_marble.png`, `celestial_armillary_controller_side.png`, `celestial_armillary_controller_dial.png`, `celestial_armillary_controller_ring.png`, `celestial_armillary_controller_top.png`. Custom block class: `CelestialArmillaryControllerBlock.java`.
    - 💎 **`beam_splitter_prism`**: Symmetrical 4-pillar brass clamping vise (NW, NE, SW, SE) on a circular angle dial with an Arcanite base plinth holding a $45.0^\circ$ refractive diamond starlight crystal with internal chromatic dispersion. Kinematic inheritance: `base` $\to$ `turntable_prism`. Textures: `beam_splitter_prism_base.png`, `beam_splitter_prism_dial.png`, `beam_splitter_prism_brass.png`, `beam_splitter_prism_crystal.png`. Custom block class: `BeamSplitterPrismBlock.java`.
    - 🔍 **`refractive_astral_lens`**: Heavy circular pedestal base, U-shaped gimbal yoke stand, knurled friction thumbscrews, and an adjustable bezel holding a translucent double-convex starlight quartz disc. Kinematic inheritance: `base` $\to$ `azimuth_yoke` $\to$ `elevation_lens`. Textures: `refractive_astral_lens_base.png`, `refractive_astral_lens_brass.png`, `refractive_astral_lens_ring.png`, `refractive_astral_lens.png`. Custom block class: `RefractiveAstralLensBlock.java`.
    - 💡 **`pure_optic_fiber`**: Unified single-model $4\times4$ voxel cross-section conduit with 6 branch arms and core node (`pure_optic_fiber` $\to$ `core`, `arm_north`, `arm_south`, `arm_west`, `arm_east`, `arm_down`, `arm_up`), fully insulated in a solid vulcanized matte black rubber jacket with $2\times2$ transparent optical end port apertures for light beam transmission (zero brass). Texture: `pure_optic_fiber.png`. Custom block class: `PureOpticFiberBlock.java`.
  - Documented standard full-block treatment with directional face textures for `optical_transmitter_port`, `optical_receiver_port`, and `optical_booster_amplifier`.
- **Subterranean Geode Worldgen & Growth Dynamics**:
  - Implemented `BuddingCrystalBlock.java` supporting 6-directional random-tick bud growth (`small_astral_crystal_bud` $\to$ `medium_astral_crystal_bud` $\to$ `large_astral_crystal_bud` $\to$ `astral_crystal_cluster`).
  - Added subterranean Astral Geode configured and placed feature data definitions with NeoForge biome modifiers and Fabric `BiomeModifications` runtime registration.
- **Interactive Astral Multiblock JEI Blueprint Category (`AstralMultiblockRecipeCategory.java`)**:
  - Implemented custom JEI category ($180 \times 135$ layout) displaying layer-by-layer blueprints with dynamic 2-second layer cycling ($Y=1 \to Y=6$), 2D color-coded blueprint matrices, and exact material counts.
  - Defined recipes and layer matrices for:
    - **7x7x6 Grand Circular Astral Observatory & Celestial Mirror Pool**
    - **3x3x3 Celestial Beacon Sanctuary**
    - **Modular Upgradable Astral Altar** (Full block foundation platform without slab restrictions)
  - Registered JEI category, blueprints, and catalysts in `EntropicaJEIPlugin.java`.
- **24-Constellation Pantheon & Inverse Flux Rates Calibration (`ModConstellations.java` & `Constellation.java`)**:
  - Registered all 24 Entropica Pantheon Constellations across 4 tiers with exact `EssenceType` affinities and inverse cosmic energy density flux rates:
    - *Tier 1 Fundamental*: `vespa_aculeus` ($25\text{ flux/t}$), `lucerna_radialis` ($24\text{ flux/t}$), `serpens_veneni` ($22\text{ flux/t}$), `arbor_vitae` ($18\text{ flux/t}$), `scutum_aegis` ($18\text{ flux/t}$), `athanor_ignis` ($16\text{ flux/t}$), `gladius_ignis` ($10\text{ flux/t}$), `penna_aetheris` ($6\text{ flux/t}$).
    - *Tier 2 Advanced*: `mineralis_geodae` ($20\text{ flux/t}$), `glacies_crystalline` ($16\text{ flux/t}$), `ulteria_viatoris` ($15\text{ flux/t}$), `resina_succini` ($13\text{ flux/t}$), `sanguis_chalybis` ($12\text{ flux/t}$), `fulgur_tonitrus` ($12\text{ flux/t}$), `horologium_chroni` ($11\text{ flux/t}$), `bootes_pastoralis` ($8\text{ flux/t}$).
    - *Tier 3 Master*: `vorago_blighti` ($14\text{ flux/t}$), `velum_umbraticum` ($7\text{ flux/t}$), `echo_primordialis` ($5\text{ flux/t}$), `corona_solaris` ($5\text{ flux/t}$).
    - *Tier 4 Mythic*: `leviathan` ($12\text{ flux/t}$), `yggdrasil` ($8\text{ flux/t}$), `ouroboros` ($6\text{ flux/t}$), `azathoth` ($7\text{ flux/t}$).
- **Global 24-Language Parity & Codex Synchronization**:
  - Achieved $100\%$ key parity ($2,457$ translation entries each) across all 24 supported languages in `assets/entropica/lang/`.
  - Added `astral_pantheon_constellations` research node under `MAGIC` in `CodexCategoryRegistry.java`.
  - Updated notes in the Entropica OKF Obsidian Vault (`astral_workstations_and_optics.md`).
- **Astral Sorcery-Style In-Sky Observation & Stargazing Suite**:
  - **Camera Rotation & Angle Sync on Exit**: Leaving the telescope (`ESC`) synchronizes the player's world camera (`yRot`, `xRot`, `yHeadRot`, `yBodyRot`) to the exact azimuth and declination observed through the lens.
  - **Line of Sight / Sky Occlusion Raycasting**: Raycasts 128 blocks from the player's eyes along the viewing vector; if obstructed by ceilings, caves, or solid structures, the telescope view dims with a `"Line of Sight Obstructed"` alert.
  - **Free-Form Star Tracing & Validation**: Allows drawing lines between any stars in the telescope view; validating the constellation graph upon full puzzle completion, while Shift + Right-Click resets lines.
  - **Refractive Astral Lens Aiming & Starlight Beam Focusing**:
    - Created `RefractiveAstralLensBlockEntity.java` and `RefractiveAstralLensRenderer.java` (BER across Fabric & NeoForge).
    - Right-clicking the Astral Lens opens `SkyLookingGlassScreen.openForLens(pos)` allowing players to pan the heavens, inspect celestial bodies, and lock the optical lens on any star, guide beacon, or constellation (`SPACE` or Click to calibrate).
    - Physical 3D Astral Lens block in the world pivots its brass gimbal mount to point directly at the calibrated star and emits a concentrated beam of focused starlight.
  - **Organic Multi-Puff Cosmic Nebulae**:
    - Generated `nebula_puff.png` (Gaussian soft transparent radial falloff) and built 4 organic celestial cloud complexes: *Lagoon Veil* (Azure/Cyan), *Amethyst Remnant* (Violet/Magenta), *Amber Nursery* (Golden Amber/Crimson), and *Emerald Shroud* (Deep Teal/Indigo).
    - Replaced flat quad squares with overlapping billowy particle clouds rendered across both `CelestialSkyRenderer.java` and `SkyLookingGlassScreen.java`.
  - **Dynamic Essence-Type Constellation Coloring & Universal Star Resonance**:
    - Synchronized all celestial rendering systems (`CelestialSkyRenderer.java`, `SkyLookingGlassScreen.java`, `ConstellationTracingScreen.java`, and `CompletedStarChartItem.java`) with assigned `EssenceType` color palettes.
    - Assigned deterministic essence typings to all 260 ambient stars and named landmark beacons across the heavens.
    - Removed giveaway circular node indicators from constellation stars; all stars now render with authentic starlight billets and display rich stellar resonance data when inspected.
  - **Custom Charted Star Connections Persistence & Skybox Rendering**:
    - Created `CelestialStarHelper.java` providing unified spherical celestial coordinates (`StarSkyPos`) and essence color queries for all stars (ambient, landmark, constellation).
    - Created `SyncChartedConnectionsPayload.java` and updated `PlayerAstralProgress.java` to persist all custom star connections drawn by the player (`nodeA---nodeB`) on client and server.
    - All charted star connections (custom asterisms as well as official constellations) now persist and render as luminous starlight lines across the night skybox.
  - **Targeted Line Highlighting & Single-Line Erasure**:
    - Implemented point-to-segment distance collision in `SkyLookingGlassScreen.java` and `ConstellationTracingScreen.java`.
    - Hovering the mouse near any drawn line segment within $8\text{px}$ highlights only that line in vivid red/glow (`0xFFFF3333`).
    - Right-clicking now selectively erases only the hovered line rather than wiping out all lines, syncing the deletion immediately across client and server.
  - **Refractive Astral Lens Model & Aim Synchronization Overhaul**:
    - Rebuilt `RefractiveAstralLensRenderer.java` using exact Blockbench model element geometries, UVs, and hierarchical group rotation origins (`base`, `azimuth_yoke`, and `elevation_lens`).
    - Fixed player look angle and lens declination synchronization on screen exit: `syncPlayerRotation` now smoothly aligns player yaw and pitch directly along the calibrated aiming line of sight while preserving the lens's calibrated angles.
    - Fixed persistent beam state: resolved packet issue where closing the screen or mouse-panning previously overwrote `isFocused` with `false`, keeping the focused starlight influx and refracted output beams active indefinitely once calibrated.
  - **Procedural Unique Astronomical Star Naming System**:
    - Assigned unique astronomical names to all 320 ambient stars in `CelestialStarHelper.java` by combining celestial constellation roots and stellar designations (e.g. `Zephyros-Alpha`, `Thalassa-Prime`, `Astralis-VII`, `Pyralis-Beta`, `Novara-Major`).
    - Added `getConstellationStarName(Constellation, int)` assigning classical Greek designations to every star in every constellation (e.g. `Alpha Aculei (Apex)`, `Beta Aculei`, `Alpha Radialis`).
    - Integrated star name displays on Looking Glass, Telescope, Astral Lens HUD reticles, and the Star Chart Scribing Table.
  - **20-Block Pan & Zoom Astral Lens Target HUD (<60° Angle of Attack)**:
    - In `SkyLookingGlassScreen.java`, the pan-and-zoom interface now scans for all nearby `RefractiveAstralLensBlockEntity` within 20 blocks.
    - If a target lens has a clear line of sight (no intervening solid blocks) and is within a $60^\circ$ angle of attack from the current aim angle, it is projected onto the viewscreen as a glowing interactive target circle with live distance readouts (`§b✦ Lens [14.2m]`).
    - Aiming within $18\text{px}$ of the target circle snaps focus onto the target lens for optical calibration.
  - **Incoming Link Counter & Dual-Side Beam Mechanics**:
    - Added `incomingLinksCount` tracking to `RefractiveAstralLensBlockEntity.java`.
    - Implemented full 3D AABB bounding box ray intersection (`lensBox.clip(start, end)`), ensuring beams accurately detect and terminate immediately at the surface of target lenses or solid obstacles without overshooting past them into the sky.
    - If `incomingLinksCount == 0` and the lens is sky-focused, it renders the skyward influx beam entering from the heavens on the upper/rear face and projects the output beam on the other side.
    - When receiving a relayed beam from an upstream lens (`incomingLinksCount > 0`), the sky influx is omitted and the relayed starlight is redirected forward along the lens's calibrated angle.
- **Multi-Loader Compilation Verification**:
  - Executed `./gradlew deploytoDev` with **`BUILD SUCCESSFUL in 31s`** across `common`, `neoforge`, and `fabric`.

---

## Build 000-1-26-227

### Core Additions & Features
- **Astral Materia Architectural & Item Suite (35 Blocks & 18 Items)**:
  - Registered all 35 Astral Materia blocks in `ModBlocks.java` and their corresponding BlockItems in `ModItems.java`:
    - **Architectural Suite**: `astral_marble`, `astral_marble_bricks`, `astral_marble_slab`, `astral_marble_stairs`, `astral_marble_wall`, `sooty_marble`, `runed_astral_marble`, `engraved_astral_slate`, `chiseled_astral_marble`, `starlight_pillar` (rotatable `RotatedPillarBlock`), and `astral_mirror_block`.
    - **Extraterrestrial Impactite & Crystal Geodes**: `astral_impactite`, `budding_astral_impactite` (`BuddingCrystalBlock`), `small_astral_crystal_bud`, `medium_astral_crystal_bud`, `large_astral_crystal_bud`, `astral_crystal_cluster`, and `astral_crystal_block`.
    - **Workstations & Multiblock Devices**: `astral_altar_core`, `resonance_pylon`, `astral_pedestal`, `focal_lens_mount`, `astral_infusion_pedestal`, `astral_collector`, `celestial_beacon_controller`, `dormant_crystal_relic`, `stationary_brass_telescope`, `celestial_armillary_controller`, and `stone_hopper_basin`.
    - **Optical Conduits & Logistics**: `refractive_astral_lens`, `beam_splitter_prism`, `pure_optic_fiber`, `optical_transmitter_port`, `optical_receiver_port`, and `optical_booster_amplifier`.
  - Registered all 18 standalone celestial instruments, crystals, tools, and baubles in `ModItems.java`:
    - `looking_glass`, `astrolabe`, `drafting_compass`, `star_chart_blank`, `star_chart_completed`, `astral_linking_wand`, `mortar_and_pestle`.
    - `astral_crystal`, `astral_crystal_seed`, `starlight_silk`, `astral_crystal_thread`.
    - `crystal_sword`, `crystal_pickaxe`, `crystal_axe`, `crystal_shovel`, `drained_crystal_tool`, `resplendent_prism`, `mantle_of_the_stars`.
- **Connected Astral Mirror Block (`AstralMirrorBlock.java`)**:
  - Implemented 4-way horizontal connection checking (`NORTH`, `SOUTH`, `EAST`, `WEST`). When placed adjacent to other Astral Mirror blocks, interior borders disappear completely into an unbroken, smooth starlight mercury sheet, with beveled borders rendering only along the outer perimeter.
- **24-Constellation Engine Registry (`ModConstellations.java`)**:
  - Implemented `ConstellationTier` (Fundamental, Advanced, Master, Mythic) and `SpectralClass` (8 leakage classes from O to V).
  - Created immutable registry registering all 24 canonical constellations with star vertices, line segment connections, moon phase visibility bitmasks, and signature ritual powers.
- **Celestial Sky Rendering & Astronomy Engine (`CelestialSkyRenderer.java`)**:
  - Implemented 3D volumetric starlight sky rendering engine hooked into both Fabric (`WorldRenderEvents.END`) and NeoForge (`RenderLevelStageEvent.AfterSky`).
  - **Multi-Dimensional Support**: Fully renders all 24 constellations permanently in The End vacuum firmament, blocks starlight beneath Nether bedrock, and projects moon-phase aligned constellations at night in the Overworld.
  - **Weather & Cloud Altitude Dynamics**: Attenuates star brightness during rain/thunderstorms below cloud height ($Y < 192$), maintaining 100% crystal clarity above clouds.
  - Dynamically calculates 3D star billboard vertices with colors corresponding to their `SpectralClass` (Class O azure, B blue-white, A white, F yellow-white, G solar gold, K orange, M crimson, V void purple) with organic twinkling pulsing.
  - **Skybox Radiant Ignition**: Discovered constellations permanently ignite with golden ribbons and traveling stardust pulse beads in the player's night sky.
- **Celestial Looking Glass & Astrolabe (`LookingGlassItem.java`, `AstrolabeItem.java` & `LookingGlassOverlayRenderer.java`)**:
  - Implemented `LookingGlassItem` ($1\times \to 4\times$ zoom) and `AstrolabeItem` ($1\times \to 8\times$ zoom) with scoping states.
  - Designed custom brass astronomical reticle overlay (`looking_glass_overlay.png`) with degree notches, cardinal headings, crosshairs, and lens vignette.
  - Live HUD readouts for Azimuth angle, Cardinal heading, Declination / Altitude angle, Moon Phase status, and targeted constellation info card.
- **Interactive Constellation Scribing Screen (`ConstellationTracingScreen.java`)**:
  - Sneak-using astronomical instruments opens an interactive midnight parchment star chart with `<` and `>` constellation switching.
  - Features real-time line dragging between star vertices with a **12-pixel magnetic snap zone** assist and right-click line clearing.
  - Validates completed constellation geometry, automatically consumes a Blank Star Chart and grants a foil-enchanted Completed Star Chart with rich astrological tooltips.
- **Stationary Brass Telescope (`StationaryBrassTelescopeBlock.java`)**:
  - Ground-mounted observation workstation with $1\times \to 16\times$ zoom that opens the observation and scribing viewport on right-click.
- **Entropic Codex Integration (`CodexCategoryRegistry.java`)**:
  - Added 5 new branching research sub-nodes under `MAGIC` (`astral_instruments_and_charts`, `astral_altar_and_observatory`, `astral_optics_and_logistics`, `astral_crystals_and_impactite`, `astral_celestial_beacons`).
- **OKF Obsidian Vault Synchronization**:
  - Created and linked `wiki/entities/blocks/astral_materia_blocks.md`, `wiki/entities/blocks/astral_workstations_and_optics.md`, and `wiki/entities/items/astral_materia_items.md` in the Entropica OKF Obsidian Vault.
- **Multi-Loader Compilation Verification**:
  - Executed `./gradlew compileJava --parallel` with **`BUILD SUCCESSFUL in 10s`** across `common`, `neoforge`, and `fabric`.

---

## Build 000-1-26-225

### Core Additions & Features
- **10 Elemental Wood Barrels (`ModBarrelBlock.java` & `ModBarrelBlockEntity.java`)**:
  - Implemented `ModBarrelBlock` extending `BarrelBlock` and `ModBarrelBlockEntity` extending `RandomizableContainerBlockEntity` bound to `MOD_BARREL` in `ModBlockEntities.java`.
  - Registered all 10 custom wood barrels with distinct map colors and sounds:
    - 🌋 `pyre_ash_cedar_barrel`: Charred volcanic spruce bark with molten orange cinder wood, magma sparks, dark volcanic iron hoops, and an ember-lit interior cavity.
    - 🌌 `abyssal_spore_cypress_barrel`: Deep oceanic indigo wood with bioluminescent cyan spore nodes and cyan spore interior glow.
    - ✨ `starlight_aether_birch_barrel`: Shimmering white celestial birch wood banded in polished golden starlight rings with golden-amber growth rings on top.
    - 🩸 `blood_root_iron_oak_barrel`: Ironwood oak timber with dark iron bands and deep crimson heartwood veins bleeding vital sap.
    - 🟣 `astral_veil_willow_barrel`: Muted slate purple timber with double-recessed frame panels and deep astral violet bands with amethyst rim.
    - 🔮 `void_blight_mangrove_barrel`: Dark violet blight timber featuring carved concentric void rift rings on the lid and glowing void-iron hoops.
    - 🍯 `amber_barrel`: Rich golden amber timber coated in resin seams, studded with translucent amber chunks along reinforcement hoops.
    - 🪵 `rubber_barrel`: Dark mahogany latex rubber wood with dark iron reinforcement hoops and vertical amber latex resin drips.
    - 🌲 `silver_pine_barrel`: Pale alpine teal-emerald pine wood with frosted silver trim, vertical slat paneling, and frosted silver hoops.
    - 🌊 `materia_echo_barrel`: Ghostly pale ash wood inscribed with glowing spectral cyan rune line carvings and an illuminated ether-core cavity.
- **40 Handcrafted 16x16 Barrel Textures (`assets/entropica/textures/block/`)**:
  - Created 4 textures per wood species: `<wood>_barrel_bottom.png`, `<wood>_barrel_side.png`, `<wood>_barrel_top.png` (Closed), and `<wood>_barrel_top_open.png` (Open).
- **Crafting Recipes & Creative Tab Placement**:
  - Registered 10 shaped crafting recipes (6 Planks + 2 Slabs -> 1 Barrel).
  - Added all 10 barrels to `WORLD_TAB` in `ModCreativeTabs.java` placed immediately after their respective wood family chest.
  - Tagged in `#minecraft:barrels`, `#c:barrels`, and `#minecraft:mineable/axe`.
- **Codex & Obsidian Documentation**:
  - Added `machinery_custom_wood_barrels` research node to `CodexCategoryRegistry.java` under `MACHINERY`.
  - Created `elemental_wood_barrels.md` in the OKF Obsidian Vault.
- **Astral Materia & Constellation System Architecture (100-Question Lore Treatise)**:
  - Completed comprehensive 100-question `/grill-me` architectural design interview resolving all mechanics, rendering, astrophysics, and progression for Entropica's Astral Materia system.
  - Published master lore treatise `entropica - astral materia.md` (in workspace root and Obsidian Vault `concepts/entropica - astral materia.md`).
  - Created master OKF guide `wiki/articles/astral_astronomy_guide.md`.
  - Created master block and asset tracking register `Create and texture these for astral materia.md` cataloging all 36 required blocks, items, devices, particles, and sound events.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 46s`**.

## Build 000-1-26-224

### Core Fixes & Client Improvements
- **1. Chest Item Inventory Rendering Fix (`assets/entropica/items/*_chest.json`)**:
  - Updated all 10 chest item definition JSON files to the vanilla 1.21.2+ `minecraft:special` model definition format (`"type": "minecraft:special"`, `"base": "minecraft:item/chest"`, `"model": {"type": "minecraft:chest", "texture": "entropica:<wood>"}`).
  - Resolves invisible chest item stacks in GUIs, inventory slots, player hands, and ground drops.
- **2. Flower & Plant Cutout Transparency Audit (29 Block Models)**:
  - Added `"render_type": "minecraft:cutout"` across 29 missing block model JSON files in `assets/entropica/models/block/` (including all 20 potted flower variants, tree saplings, brambles, and spore caps), eliminating opaque black box rendering in-world.
- **3. Shelf Mushroom Hitbox & Collision Refinement (`AbstractFungalShelfBlock.java`)**:
  - Overrode `getCollisionShape` to return `Shapes.empty()`, allowing players to walk smoothly past wall-mounted shelf mushrooms without catching on solid block collision.
  - Preserved directional 14x4x12 pixel `getShape(...)` bounding boxes for selection outlines and interaction.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 42s`**.

### 3D Continuous Void Rift Ring Seam Fix
- **Void Blight Mangrove Chest Tiling (`assets/entropica/textures/entity/chest/void_blight_mangrove*.png`)**:
  - Re-calculated the carved glowing void rift ring pattern across 3D coordinates so that the ring on the top lid connects continuously over the top rim edge into all 4 side walls without any abrupt line cut-offs.
  - Formed a 3D continuous spherical/cylindrical void rift portal ring field around the entire chest body.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 48s`**.

### Obsidian Vault Lore-Authentic Wood Chest Art
- **30 Vault-Authentic Handcrafted Chest Textures (`assets/entropica/textures/entity/chest/*.png`)**:
  - Consulted and applied the exact species appearance, bark textures, wood grain palettes, and botanical lore from the OKF Obsidian Vault (`C:\Users\Ddraig__\Downloads\OBSIDIAN WIKIS\Entropica\Entropica\wiki\entities\blocks\`) for all 10 tree species:
    - 🌋 `pyre_ash_cedar`: Charred volcanic spruce bark outer frame, cinder-orange wood body, 1-pixel magma fissure veins, and volcanic iron bands with a magma-core lock.
    - 🌌 `abyssal_spore_cypress`: Deep dark navy-indigo jungle bark frame, dark cyan wood body, bioluminescent cyan spore nodes/gills along edges, and cyan spore crystal lock.
    - ✨ `starlight_aether_birch`: Shimmering white-gold birch bark frame, pale gold interior wood grain with golden-amber growth ring highlights, and polished gold starlight latch.
    - 🩸 `blood_root_iron_oak`: Heavy dark iron-oak bark frame, deep blood-crimson ironwood heartwood body, twisted crimson-black veins, and ruby-studded iron lock.
    - 🟣 `astral_veil_willow`: Muted slate purple willow bark frame, glowing violet wood body with soft double-recessed panels, and deep amethyst crystal lock.
    - 🔮 `void_blight_mangrove`: Deep dark void-magenta mangrove frame, dark violet blight timber body with carved void rift rings, and glowing void purple crystal lock.
    - 🍯 `amber_wood`: Sticky golden-orange wood body, dark resin frame, embedded translucent golden amber resin chunk studs along edges, and golden amber gem lock.
    - 🪵 `rubber_tree`: Dark grayish-brown rubber wood body, vertical amber latex resin streaks, dark iron hoops/bands, and heavy dark iron buckle ring lock.
    - 🌲 `silver_pine`: Pale silver-blue alpine bark frame, dark teal/cyan pine wood body with clean vertical fissures and frosted silver trim, and silver-lime crystal lock.
    - 🌊 `materia_echo`: Warm pale ash-wood body, soft ashen-gray bark frame, subtle sage-cyan ether rune line veins, and glowing echo-cyan gem lock.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 46s`**.

### Seamless Tiling & 4-Side Uniformity Alignment
- **4-Way Tiling Top & 4-Side Uniformity (`assets/entropica/textures/entity/chest/*.png`)**:
  - Re-mapped all 30 single and double chest texture maps so that all 4 side faces (Right, Front, Left, Back) share the identical side texture pattern around the chest perimeter.
  - Aligned global texture space coordinates so the top lid face tiles seamlessly into all 4 side edges in all 4 cardinal directions (North, South, East, West).
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 45s`**.

### Bespoke Botanical & Architectural Chest Artwork
- **30 Handcrafted Bespoke Pixel Art Textures (`assets/entropica/textures/entity/chest/*.png`)**:
  - Overhauled all 30 single and double chest texture maps to feature distinct architectural paneling, iron banding, carved runes, organic hyphae veins, and bespoke lock fittings tailored to each wood species:
    - 🌋 `pyre_ash_cedar`: Charred spruce timber with horizontal dark iron bands and fiery ember-orange core lock.
    - 🌌 `abyssal_spore_cypress`: Oceanic dark navy wood with bioluminescent cyan hyphae veins crawling across top/sides and cyan crystal lock.
    - ✨ `starlight_aether_birch`: Pale silver-white birch wood with fine vertical wood slat paneling and golden starlight inlaid trim.
    - 🩸 `blood_root_iron_oak`: Ironwood bark slate texture with iron corner studs and bleeding crimson sap seams.
    - 🟣 `astral_veil_willow`: Muted slate purple wood with double recessed frame panels and amethyst lock.
    - 🔮 `void_blight_mangrove`: Deep void blight purple wood with carved concentric void rift rings on lid and sides.
    - 🍯 `amber_wood`: Honey-amber wood planks with amber resin studs along edges and translucent amber gem lock.
    - 🪵 `rubber_tree`: Dark mahogany latex rubber wood with horizontal dark iron reinforcement hoops/straps.
    - 🌲 `silver_pine`: Deep alpine evergreen pine wood with vertical slat paneling and frosted silver trim.
    - 🌊 `materia_echo`: Ghostly spectral gray wood with glowing cyan rune line carvings along lid and front panel.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 45s`**.

### Core Texture Overhaul & Refinement
- **Authentic Wood Plank Retexturing (`assets/entropica/textures/entity/chest/*.png`)**:
  - Replaced hue/brightness multipliers with an authentic texture compositor pipeline that maps each wood species' **actual 16x16 / 32x32 custom plank pixel art** (`<wood>_planks.png`) directly onto all lid tops, lid sides, base bottoms, and base sides across all 30 single and double chest texture maps.
  - Calculated custom 1-pixel frame outline borders derived dynamically from the darkest shadow tones of each wood species' plank texture.
  - Retained high-contrast metallic accent latches shaded specifically to each tree family's accent color palette (Ember Orange, Bioluminescent Cyan, Golden Starlight, Crimson Blood, Astral Violet, Void Purple, Amber Gold, Latex Amber, Silver-Lime, Echo Cyan).
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 47s`**.

### Root Cause Diagnosed & Fixed
- **Empirical Crash Log Diagnosis (`crash-2026-08-12_15.33.24-client.txt`)**:
  - Log revealed `java.lang.IllegalArgumentException: Invalid atlas id: minecraft:textures/atlas/chest.png` in `ModChestRenderer.submit` when attempting `AtlasManager.getAtlasOrThrow(material.atlasLocation())`.
  - Root cause: In Minecraft 1.21.2+, `Sheets.CHEST_SHEET` is not registered as a standalone atlas in `AtlasManager`. Entity chest textures are standalone 64x64 PNG files rendered via `RenderType.entityCutout(...)`.
  - **Renderer Fix ([ModChestRenderer.java](file:///C:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/client/renderer/ModChestRenderer.java))**:
    - Replaced `AtlasManager.getAtlasOrThrow(...)` lookup with `RenderType.entityCutout(ResourceLocation.fromNamespaceAndPath("entropica", "textures/entity/chest/" + key + ".png"))`.
    - Passed `null` for `TextureAtlasSprite` in `SubmitNodeCollector.submitModel(...)`, completely resolving the atlas lookup exception while rendering custom 64x64 chest textures cleanly in-world.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 37s`**.

### Root Cause Diagnosed & Fixed
- **Empirical Crash Log Diagnosis (`crash-2026-08-12_15.26.59-client.txt`)**:
  - Log revealed `java.lang.IllegalStateException: Invalid block entity minecraft:chest for Block{entropica:astral_veil_willow_chest}` during in-world placement.
  - Root cause: `ChestBlock.newBlockEntity` in vanilla Minecraft was hardcoded to return `new ChestBlockEntity(...)` using `BlockEntityType.CHEST`, which rejected custom block IDs.
  - **Block Placement Fix (`ModChestBlock.java`)**: Overrode `newBlockEntity(BlockPos pos, BlockState state)` in `ModChestBlock` to return `new ModChestBlockEntity(pos, state)` bound to `ModBlockEntities.MOD_CHEST`.
- **Invisible Item Rendering Fix (`assets/entropica/items/*_chest.json`)**:
  - Root cause: 1.21.2+ item definitions used `"type": "minecraft:model"` referencing `"minecraft:item/chest"`, which failed to resolve custom textures.
  - **Item Definition Fix**: Updated all 10 item definition JSON files (`assets/entropica/items/<wood>_chest.json`) to use `"type": "minecraft:chest"` with `"texture": "entropica:entity/chest/<wood>"`, enabling 1.21.2+ native 3D chest rendering in GUI, hands, and drop states with custom 64x64 textures.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 48s`**.

### Core Additions & Features
- **10 Custom Wood Chest Blocks (`ModChestBlock.java`)**:
  - Implemented `ModChestBlock` extending `ChestBlock` with custom wood type identifiers:
    - 🌋 `pyre_ash_cedar_chest`: Burnt ash brown timber with fiery ember-orange latches.
    - 🌌 `abyssal_spore_cypress_chest`: Deep dark oceanic indigo timber with bioluminescent cyan latches.
    - ✨ `starlight_aether_birch_chest`: Pale silver-white birch timber with glowing golden starlight latches.
    - 🩸 `blood_root_iron_oak_chest`: Metallic iron-gray bark timber with deep crimson blood-red latches.
    - 🟣 `astral_veil_willow_chest`: Muted slate purple timber with astral violet latches.
    - 🔮 `void_blight_mangrove_chest`: Deep void blight purple-gray timber with glowing blight purple latches.
    - 🍯 `amber_chest`: Rich golden amber timber with amber crystal latches.
    - 🪵 `rubber_chest`: Dark mahogany latex rubber wood timber with amber gold latches.
    - 🌲 `silver_pine_chest`: Deep alpine pine-green timber with bright silver-lime latches.
    - 🌊 `materia_echo_chest`: Ghostly spectral gray timber with echo cyan-blue latches.
- **Custom Block Entity & Renderer (`ModChestBlockEntity.java` & `ModChestRenderer.java`)**:
  - Created `ModChestBlockEntity` extending `ChestBlockEntity` and registered `MOD_CHEST` in `ModBlockEntities.java`.
  - Implemented `ModChestRenderer` extending `ChestRenderer<ModChestBlockEntity>` cleanly overriding `submit` using `ChestRenderState` and `SubmitNodeCollector` for 100% 1.21.2+ rendering compatibility across Fabric (`EntropicaClientFabric.java`) and NeoForge (`ModClientEvents.java`).
- **30 Recolored 64x64 Texture Maps**:
  - Generated 30 custom 64x64 chest texture maps (`textures/entity/chest/*.png`, `*_left.png`, `*_right.png`) matching the exact pixel-perfect layout and authentic plank & latch color palettes of each tree family.
- **Crafting Recipes & Tab Placement**:
  - Registered 10 3x3 plank ring crafting recipes yielding 1 custom wood chest per wood species, preserving vanilla oak chest recipes.
  - Inserted each wood chest into `WORLD_TAB` in `ModCreativeTabs.java` immediately after its corresponding wood family's `PRESSURE_PLATE`.
- **Block & Item Tags**:
  - Added all 10 chest blocks and items to `#minecraft:chests`, `#minecraft:wooden_chests`, `#c:chests`, and `#c:wooden_chests`.
- **Documentation & Codex Sync**:
  - Created OKF Obsidian Vault note (`wiki/entities/blocks/elemental_wood_chests.md`).
  - Registered research node (`machinery_custom_wood_chests`) under `MACHINERY` in `CodexCategoryRegistry.java`.
- **Multi-Loader Build Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 51s`**.

### Core Bug Fixes
- **1.21.2+ Item Definition Tinting Restoration (`assets/entropica/items/*.json`)**:
  - Identified root cause where Minecraft 1.21.2+ item JSONs lacked `"tints"` specifications in their `model` blocks, causing items to render un-tinted / flat white.
  - Updated 1312 item JSON files across all custom item types:
    - **Essence Orbs** (`weak_essence`, `average_essence`, `strong_essence`, `fragment_essence`): Added `entropica:essence_tint`.
    - **Essence & Fume Ampoules** (`small_essence_ampoule`, `medium_essence_ampoule`, `large_essence_ampoule`, `small_materia_fumus_ampoule`, `medium_materia_fumus_ampoule`, `large_materia_fumus_ampoule`): Added 2-layer tinting (`minecraft:constant` -1 base bottle, `entropica:essence_tint` / `ampoule_tint` fluid content).
    - **Veil Shard** (`veil_shard`): Added `entropica:shard_tint` with biome and node attunement.
    - **Echo Fruit** (`echo_fruit`): Added `entropica:echo_fruit_tint`.
    - **Creative Generator** (`creative_materia_generator`): Added `entropica:generator_tint`.
    - **Shimmerpetal** (`shimmerpetal`): Added `entropica:ambient_essence_tint`.
    - **Spectral Dyes** (`spectral_dye_*`): Ensured 2-layer tinting with `entropica:spectral_dye_tint`.
    - **Aesthetic Glass Suite** (All 400+ glass blocks, panes, doors, trapdoors, slabs, stairs): Added `entropica:aesthetic_glass_tint`.
- **Multi-Loader Tint Source Registration (`EntropicaClientFabric.java` & `ModClientEvents.java`)**:
  - Registered missing `spectral_dye_tint` and `ambient_essence_tint` sources in both Fabric and NeoForge client initializers.
- **Dynamic Essence HSB Rainbow Cycling (`EssenceType.java`)**:
  - Updated `EssenceType.getCurrentRGB` for dynamic types (`ENTROPICA`, `CHIMERA`) to cycle through smooth HSB rainbow hues instead of returning static white (`{255, 255, 255}`).
- **Multi-Loader Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 53s`**.

### Core Fixes & Additions
- **Materia-Echo Vertical Slab Model Fix (`materia_echo_vertical_slab.json`)**:
  - Created missing `models/block/materia_echo_vertical_slab.json` parented to `entropica:block/base/vertical_slab_base` with `materia_echo_planks` textures. Fixed black/purple missing model error in world placement and item view.
- **Spectral Dye Rendering Fix (`spectral_dye_tint`)**:
  - Registered missing `spectral_dye_tint` (`ModItemTintSources.SpectralDyeTint`) loom tint source in `EntropicaClientFabric.java`.
  - Filtered `SpectralDyeItem` out of general `ITEMS_TAB` so dyes only display in their dedicated dynamic color section.
- **World Tab Categorization & Alphabetical Grouping (`WORLD_TAB`)**:
  - Reorganized `WORLD_TAB` in `ModCreativeTabs.java` into 5 clean, logically organized sections:
    1. **Ores & Minerals**: Alphabetical listing of crystal blocks, budding blocks, clusters, buds, and raw ores.
    2. **Flora, Plants & Mushrooms**: Alphabetical listing of flowers, orchids, petals, mosses, mushrooms, and succulents.
    3. **Wood Families**: Alphabetical listing by Wood Type name (Abyssal Spore-Cypress, Amber-Wood, Astral-Veil Willow, Blood-Root Iron-Oak, Materia-Echo Tree, Pyre-Ash Cedar, Rubber-Tree, Silver-Pine, Starlight Aether-Birch, Void-Blight Mangrove), with each Wood Family internally sorted in clean standard sequence (Log -> Stripped Log -> Wood -> Stripped Wood -> Planks -> Leaves -> Sapling -> Fruit/Bramble -> Stairs -> Slab -> Vertical Slab -> Fence -> Fence Gate -> Button -> Pressure Plate -> Bark/Flakes drop).
    4. **Spectral Dyes**: Alphabetical listing of all 22 spectral dyes.
    5. **Indigenous Fauna Spawn Eggs**: Alphabetical listing of all mob spawn eggs.
- **Shaded 16x16 Pixel Art Sapling Suite**:
  - Created customized, high-contrast 16x16 pixel art sapling textures with shaded stems, dark soil/mud mounds, and distinct botanical leaf palettes:
    - 🌋 `pyre_ash_cedar_sapling.png`: Volcanic ash/basalt soil base with ember specks, charcoal bark stem, fiery ember-tipped cedar needles.
    - 🌌 `abyssal_spore_cypress_sapling.png`: Deep dark mud soil base, navy stem, bioluminescent cyan spore clusters & needles.
    - ✨ `starlight_aether_birch_sapling.png`: Forest moss soil base, silver-white birch stem with dark bark notches, glowing golden starlight leaves.
    - 🩸 `blood_root_iron_oak_sapling.png`: Iron-rich red loam soil base, gnarled dark iron-oak stem, deep crimson blood-oak leaves.
- **Multi-Loader Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 49s`**.

### Core Features & Mechanics
- **Echo Fruit Natural Regrowth System (`MateriaEchoLeavesBlock.java`)**:
  - Implemented `MateriaEchoLeavesBlock` extending `Block` with `randomTicks()` and `BonemealableBlock` interface.
  - **Random Tick Regrowth**: On leaf random tick, if the space directly underneath (`pos.below()`) is empty (`AIR`), there is a 5% chance per tick to sprout a fresh Echo Fruit bud (`AGE=0`).
  - **Ripening Growth**: Sprouted Echo Fruit buds naturally receive random ticks to mature from `AGE=0` -> `1` -> `2` (fully ripe).
  - **Harvest Cycle**: Right-clicking a ripe fruit (`AGE=2`) harvests the fruit and resets the block state to `AGE=0` (unripe bud) to repeat the growth cycle indefinitely. If broken, the leaf will sprout a new bud over time.
  - **Bonemeal Support**:
    - Applying Bonemeal to `materia_echo_leaves` with an open space below instantly forces a new Echo Fruit bud (`AGE=0`) to sprout.
    - Applying Bonemeal directly to `materia_echo_fruit` advances its growth stage to fully ripe.
- **Multi-Loader Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 40s`**.

### Core Features & Additions
- **Materia-Echo Tree Wood Suite (15 Blocks)**:
  - Implemented the complete 15-block suite: `materia_echo_log`, `stripped_materia_echo_log`, `materia_echo_wood`, `stripped_materia_echo_wood`, `materia_echo_planks`, `materia_echo_leaves`, `materia_echo_sapling`, `materia_echo_stairs`, `materia_echo_slab`, `materia_echo_vertical_slab`, `materia_echo_fence`, `materia_echo_fence_gate`, `materia_echo_button`, `materia_echo_pressure_plate`, and `materia_echo_fruit`.
  - Applied grounded, organic pixel art textures with ashen bark (`#7D7A72`), sage-lichen ether veins (`#689B8F`), high-contrast leaves (`#23382D` shadow / `#DCF7EC` highlight tips), high-contrast planks (`#382E21` dark seams), punchy concentric growth ring log tops, and a custom soil-mounded sapling.
- **Hanging Echo Fruit (`materia_echo_fruit`)**:
  - Custom block (`MateriaEchoFruitBlock.java`) with growth stages `AGE=0..2` that hangs **exclusively beneath `materia_echo_leaves`**.
- **Biome-Attuned EssenceType Echo Fruit Item (`echo_fruit`)**:
  - Custom food item (`EchoFruitItem.java`) using a base 16x16 grayscale pixel art texture (`echo_fruit.png`) dynamically color-tinted in-game across 9 non-fragment `EssenceType`s (`FROZEN`, `NETHER`, `NATURE`, `VOID`, `LIGHTNING`, `WATER`, `UNDEAD`, `RADIANT`, `REGULAR`).
- **60-Second Materia Toxicity System**:
  - Consuming **2 or more Echo Fruits within 60 seconds (1200 ticks)** triggers severe **Materia Toxicity**, displaying warning HUD messages and inflicting elemental overloads based on the later fruit consumed (e.g. Freezing damage, Internal combustion fire, Poison III + Nausea, Levitation, Lightning strikes, Suffocation, Wither III).
- **Log Stripping Drop**:
  - Stripping Materia-Echo logs drops **Materia-Echo Bark** (`materia_echo_bark`) + cyan/silver particle sparkle.
- **Client & Infrastructure**:
  - Registered `BlockColor` tint provider and 1.21.2+ `EchoFruitTint` (`ItemTintSource`) across Fabric and NeoForge.
  - Cutout render layer enabled for leaves, sapling, and fruit.
  - Generated all 1.21.2+ item definitions, recipes, blockstates, models, loot tables, 24-locale translations, OKF Obsidian Vault note (`materia_echo_tree.md`), and Entropic Codex research node (`env_materia_echo_tree`).
- **Multi-Loader Verification**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 38s`**.

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
*   ** Flattened Asset Support**:
    *   Converted resource directories to support Minecraft asset specifications.
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
    *   Mapped block rendering translucency sheets to Minecraft standard render map sheets `Sheets.translucentItemSheet()`.
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

## Build 000-1-26-223

### Architecture Refactoring
- **Decoupled NeoForge Log Stripping Event Handling**:
  - Removed `TreeLogStrippingHandler` from `GlassCleansingNeoForgeEvents.java` (restoring it strictly to glass cleansing mechanics).
  - Created a dedicated `TreeStrippingNeoForgeEvents.java` class (`@EventBusSubscriber(modid = Entropica.MODID)`) to handle NeoForge `PlayerInteractEvent.RightClickBlock` log stripping events cleanly and modularly.
- **Multi-Loader Build & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 19s`**.

### Core Features & Additions
- **Unique Tree Log Stripping Drops & In-World Elemental Burst Mechanics**:
  - Implemented `TreeLogStrippingHandler.java` across both Fabric (`EntropicaFabric.java`) and NeoForge (`GlassCleansingNeoForgeEvents.java`), firing in-world elemental particle bursts and dropping functional items when stripping logs/wood with an Axe:
    - 🌋 **Pyre-Ash Cedar**: Drops **Cinder-Ash Flakes** (`cinder_ash_flakes`) + lava particle burst (high-heat fuel source burning 12 items & Ignisite alchemy).
    - 🌌 **Abyssal Spore-Cypress**: Drops **Bioluminescent Spore Pods** (`bioluminescent_spore_pod`) + cyan glow particle burst (crafts into Glow-Spore Dust for light sources without glowstone & Cyan alchemy).
    - ✨ **Starlight Aether-Birch**: Drops **Aetheric Birch Bark** (`aetheric_birch_bark`) + golden spark particle burst (crafts into Aetheric Parchment for scrolls/scribing) AND can be ground down 1:2 into **Starlight Flakes** (`starlight_flakes`).
    - 🩸 **Blood-Root Iron-Oak**: Drops **Iron-Root Sap** (`iron_root_sap`) + crimson drip particle burst (hardens into Iron-Root Resin for Iron-Bound Planks & Heavy Tool Handles).
    - 🟣 **Astral-Veil Willow**: Drops **Astral Willow Fibre** (`astral_willow_fibre`) + purple portal particle burst (weaves into Astral Cordage for bows & charms).
    - 🔮 **Void-Blight Mangrove**: Drops **Blighted Bark Flakes** (`blighted_bark_flakes`) + void smoke particle burst (Void/Blight decay alchemy catalyst).
- **Client Assets & Data Infrastructure**:
  - Generated 16x16 pixel art PNG textures for all 7 items (`cinder_ash_flakes`, `bioluminescent_spore_pod`, `aetheric_birch_bark`, `starlight_flakes`, `iron_root_sap`, `astral_willow_fibre`, `blighted_bark_flakes`).
  - Registered Item Models, 1.21.2+ Item Definitions (`assets/entropica/items/*.json`), Creative Tab placement in `WORLD`, recipes, and 24-locale translations.
- **Multi-Loader Build & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 1m 48s`**.

### Core Fixes & Enhancements
- **Amber Wood Suite Complete Overhaul**:
  - Upgraded Amber wood textures (`amber_log.png`, `amber_log_top.png`, `stripped_amber_log.png`, `stripped_amber_log_top.png`, `amber_planks.png`, `amber_leaves.png`, `amber_sapling.png`) to use authentic 1.21 Minecraft base textures with 3D continuous loop border log top alignment.
- **Wood Button Rotation & Hitbox Alignment**:
  - Rebuilt `blockstates/<button>.json` for all 9 wood types (`pyre_ash_cedar`, `abyssal_spore_cypress`, `starlight_aether_birch`, `blood_root_iron_oak`, `amber`, `rubber`, `silver_pine`, `astral_veil_willow`, `void_blight_mangrove`).
  - Added full `face=floor/wall/ceiling` and `facing=north/south/east/west` variant rotations so rendered button models match their collision hitboxes when placed on any surface.
- **Leaves Cutout Transparency Fix**:
  - Configured `RenderType.cutout()` for `PYRE_ASH_CEDAR_LEAVES`, `ABYSSAL_SPORE_CYPRESS_LEAVES`, `STARLIGHT_AETHER_BIRCH_LEAVES`, `BLOOD_ROOT_IRON_OAK_LEAVES` (and saplings) in `EntropicaClientFabric.java` AND `ItemBlockRenderTypes.setRenderLayer(..., ChunkSectionLayer.CUTOUT)` in `EntropicaClient.java` (NeoForge), removing black pixel background artifacts in-world.
- **Sharpened Plank Horizontal Seam Lines**:
  - Darkened shadow tint contrast (`planks_shadow`) across all 4 tree species plank textures (`pyre_ash_cedar_planks`, `abyssal_spore_cypress_planks`, `starlight_aether_birch_planks`, `blood_root_iron_oak_planks`), making board seam lines clearly defined.
- **Multi-Loader Build & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 50s`**.

### Core Additions & Features
- **Simultaneous Implementation of 4 New Tree Species (56 New Blocks)**:
  - **Pyre-Ash Cedar**: `log`, `stripped_log`, `wood`, `stripped_wood`, `planks`, `leaves`, `sapling`, `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Abyssal Spore-Cypress**: `log`, `stripped_log`, `wood`, `stripped_wood`, `planks`, `leaves`, `sapling`, `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Starlight Aether-Birch**: `log`, `stripped_log`, `wood`, `stripped_wood`, `planks`, `leaves`, `sapling`, `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Blood-Root Iron-Oak**: `log`, `stripped_log`, `wood`, `stripped_wood`, `planks`, `leaves`, `sapling`, `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
- **Authentic Vanilla Textures & Custom Features**:
  - Extracted authentic 1.21 Minecraft textures (`spruce_log`, `jungle_log`, `birch_log`, `oak_log`, `oak_planks`, `spruce_leaves`, `jungle_leaves`, `stripped_logs`, `log_tops`).
  - **Pyre-Ash Cedar**: Charred volcanic spruce bark with fine 1-pixel random magma vein cracks contained in `y=2..13`, cinder-orange oak plank pattern, and spruce leaves.
  - **Abyssal Spore-Cypress**: Deep navy-indigo jungle bark with smooth glowing cyan spore nodes, cyan oak plank pattern, and jungle leaves.
  - **Starlight Aether-Birch**: Shimmering white-gold birch bark with dark golden-amber growth rings, golden oak plank pattern, and jungle leaves.
  - **Blood-Root Iron-Oak**: Dark oak bark, bleeding crimson oak plank pattern, and spruce leaves.
- **Geometrically Aligned Log Tops**:
  - Configured radial square log tops and stripped log tops with 4-edge 3D continuous loop border alignment to match side bark top rows seamlessly.
- **Full Infrastructure & Registrations**:
  - Registered 56 block suppliers in `ModBlocks.java`, 56 `BlockItem` suppliers in `ModItems.java`, and populated `WORLD_TAB` in `ModCreativeTabs.java`.
  - Generated 56 Blockstates, 56 Block Models, 56 Item Models, 56 1.21.2+ Item Definition JSONs (`assets/entropica/items/*.json`), 56 Crafting Recipes, 56 Self-Drop Loot Tables, and updated all Minecraft tags.
- **Localization, OKF Vault & Codex Maintenance**:
  - Added 56 block translation keys across all 24 language JSON files.
  - Created OKF Obsidian Vault notes (`pyre_ash_cedar.md`, `abyssal_spore_cypress.md`, `starlight_aether_birch.md`, `blood_root_iron_oak.md`).
  - Registered research sub-nodes under `ENVIRONMENT & NATURE` in `CodexCategoryRegistry.java`.
- **Multi-Loader Build & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 53s`**.

### Core Additions & Features
- **Expanded All 5 Entropica Tree Families**:
  - **Astral-Veil Willow**: `wood`, `stripped_wood`, `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Void-Blight Mangrove**: `wood`, `stripped_wood`, `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Rubber Wood**: `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Silver Pine**: `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
  - **Amber Wood**: `stairs`, `slab`, `vertical_slab`, `fence`, `fence_gate`, `button`, `pressure_plate`
- **Vertical Slab Implementation**:
  - Registered custom `VerticalSlabBlock` variants for all 5 wood types with full double vertical slab merging (`double_ns`, `double_ew`), placement, and culling logic.
- **Complete Asset & Data Infrastructure**:
  - Created 39 Blockstate JSONs, 39 Block Models, 39 Item Models, 39 1.21.2+ Item Definition JSONs (`assets/entropica/items/*.json`), 39 Crafting Recipes, 39 Self-Drop Loot Tables, and updated all Minecraft block & item tags (`stairs`, `slabs`, `fences`, `fence_gates`, `buttons`, `pressure_plates`, `logs`, `logs_that_burn`).
- **24-Locale Translations & Wiki Notes**:
  - Added all 39 translation keys across all 24 language JSON files.
  - Updated OKF Obsidian Vault wiki notes for `astral_veil_willow.md`, `void_blight_mangrove.md`, `rubber_tree.md`, and `silver_pine.md`.
- **Multi-Loader Build & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 45s`**.

### Root Cause Diagnosed & Fixed
- **Root Cause**: `blockstates/cinder_grip_lichen.json` had inverted/incorrect rotation rules (`up: x=180`, `down: x=0`, `north: x=90`) relative to Vanilla's base `minecraft:block/glow_lichen` geometry model (which sits natively on the North face). This caused the visual model to rotate away from its actual collision hitbox.
- **Multiface Blockstate Fix**:
  - Rebuilt `blockstates/cinder_grip_lichen.json` and `blockstates/magma_grip_tendrils.json` to match Vanilla `glow_lichen.json` rotations exactly:
    - `north`: `y=0` (no rotation)
    - `east`: `y=90, uvlock=true`
    - `south`: `y=180, uvlock=true`
    - `west`: `y=270, uvlock=true`
    - `up`: `x=270, uvlock=true`
    - `down`: `x=90, uvlock=true`
- **Multi-Loader Build & Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 41s`**.

### Root Cause Diagnosed & Fixed
- **Root Cause**: `MagmaGripTendrilsBlock` extends `GlowLichenBlock` (`MultifaceBlock` with boolean state properties `up`, `down`, `north`, `south`, `east`, `west`). `blockstates/magma_grip_tendrils.json` erroneously used directional `variants` (`facing=up`) instead of `multipart` rules, causing Minecraft to fail to resolve the blockstate and render a purple-and-black checkerboard missing model cube in-world.
- **Asset Fixes Applied**:
  - Rebuilt `blockstates/magma_grip_tendrils.json` with standard `multipart` conditions for `up`, `down`, `north`, `south`, `east`, `west`.
  - Updated `models/block/magma_grip_tendrils.json` to extend `"parent": "minecraft:block/glow_lichen"` with `"glow_lichen"` texture references.
  - Updated `models/item/magma_grip_tendrils.json` to generated item format.
- **Multi-Loader Build & Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 42s`**.

### Root Cause Diagnosed & Fixed
- **Empirical Log Diagnosis**: Inspected CurseForge crash report (`crash-2026-08-11_13.42.20-client.txt`). Stack trace revealed `NullPointerException: Cannot invoke EntityRenderer.shouldRender(...) because entityrenderer is null` when `SporeCannonPuffballBlock` or `PyreThornLauncherBlock` fired projectiles into the world.
- **Entity Renderer Registration**:
  - Registered `ThrownItemRenderer::new` for `ModEntityTypes.SPORE_PROJECTILE` and `ModEntityTypes.MAGMA_THORN` in:
    - **Fabric**: `EntropicaClientFabric.java` (`EntityRendererRegistry.register`)
    - **NeoForge**: `ModClientEvents.java` (`event.registerEntityRenderer`)
- **Multi-Loader Build & Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 44s`**.

### Fixes Applied
- **Fixed Launcher Block Survival & Placement**:
  - Overrode `updateShape`, `getStateForPlacement`, and `canSurvive` in `SporeCannonPuffballBlock` and `PyreThornLauncherBlock` so launcher blocks can be placed and survive on any solid non-air support face without breaking into air upon placement.
- **Server Projectile Launching**:
  - Enforced `level.scheduleTick(pos, this, 20)` in `onPlace` and rescheduled server ticks every 35-40 ticks.
- **Multi-Loader Build & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 43s`**.

### Core Fix Applied
- **Generated 19 Item Definition JSONs under `assets/entropica/items/*.json`**:
  - In Minecraft 1.21.2+ / 1.21.10+, item registration requires 1.21.2+ Item Asset Definitions under `assets/entropica/items/<item_id>.json` mapping `minecraft:model` -> `entropica:item/<item_id>`.
  - Created 19 JSON item definitions for:
    - 6 Nether Flora Blocks (`spore_cannon_puffball`, `pyre_thorn_launcher`, `spore_bearing_pitcher_plump`, `blood_tendril_bramble`, `soot_veil_blight_cap`, `magma_grip_tendrils`).
    - 6 Astral-Veil Willow Set Items (`log`, `stripped_log`, `planks`, `leaves`, `sapling`, `vines`).
    - 7 Void-Blight Mangrove Set Items (`log`, `stripped_log`, `planks`, `leaves`, `root`, `sapling`, `pod`).
- **Multi-Loader Rebuild & Dev Deployment**: Executed `./gradlew --no-parallel build deploytoDev` with **`BUILD SUCCESSFUL in 43s`**.

### Fixes Applied
1. **Pyre-Thorn Launcher Locked Texture**:
   - Replaced `pyre_thorn_launcher.png` with the approved, locked-in `pyre_thorn_launcher_v9` artwork.
2. **Transparency Cutout Fix Across Fabric & NeoForge**:
   - Configured `RenderType.cutout()` in `EntropicaClientFabric.java` AND `ItemBlockRenderTypes.setRenderLayer(...)` in `EntropicaClient.java` (NeoForge `FMLClientSetupEvent`) for all 6 Nether flora blocks, tree leaves, saplings, vines, and roots, completely resolving black pixel transparency issues.
3. **Active Projectile Launching Logic**:
   - Implemented scheduled server tick loops (`onPlace` + `tick`) for `SporeCannonPuffballBlock` and `PyreThornLauncherBlock`.
   - `SporeCannonPuffballBlock`: Scans 8-block radius for players/mobs and fires `SporeProjectileEntity` every 35 ticks with slime impact sound.
   - `PyreThornLauncherBlock`: Scans 10-block radius for players/mobs and fires bursts of 3 `MagmaThornEntity` projectiles every 40 ticks with firecharge sound.
4. **3D Model for Spore-Bearing Pitcher-Plump**:
   - Built a multi-element 3D block model in `models/block/spore_bearing_pitcher_plump.json` featuring a stem, bulbous pitcher body, and swollen top rim with hollow opening.
5. **Translation Keys for All 24 Locales**:
   - Updated `en_us.json` and all 24 locale JSON files with translatable keys for `block.entropica.*` and `item.entropica.*`.
6. **Multi-Loader Build & Deploy**: Recompiled, built, and deployed to dev environment with **zero errors** (`BUILD SUCCESSFUL in 45s`).

### Fixes Applied
- **Inventory Item Models Fixed**:
  - Re-generated 2D generated flat item models for all 6 Nether flora blocks (`spore_cannon_puffball`, `pyre_thorn_launcher`, `spore_bearing_pitcher_plump`, `blood_tendril_bramble`, `soot_veil_blight_cap`, `magma_grip_tendrils`) pointing directly to their 16x16 plant block textures for crisp GUI rendering.
  - Linked tree block item models (`astral_veil_willow_log`, `void_blight_mangrove_log`, `planks`, `leaves`) directly to their respective 3D block models in `models/item/*.json`.
  - Configured 2D generated item models for `astral_veil_willow_sapling`, `void_blight_mangrove_sapling`, `astral_veil_willow_vines`, and `void_blight_pod`.
- **Registered Missing BlockItems**:
  - Registered missing `BlockItem` instances in `ModItems.java` for all Nether flora blocks and tree blocks.
  - Added all BlockItems explicitly to `ModCreativeTabs.java` under `WORLD_TAB`.
- **Build & Deployment**: Multi-loader build and dev deployment completed with **zero errors** (`BUILD SUCCESSFUL in 42s`).

### World Creative Tab Additions
- **Astral-Veil Willow Set Registered**:
  - `astral_veil_willow_log`, `stripped_astral_veil_willow_log`, `astral_veil_willow_planks`, `astral_veil_willow_leaves`, `astral_veil_willow_sapling`, `astral_veil_willow_vines`.
- **Void-Blight Mangrove Set Registered**:
  - `void_blight_mangrove_log`, `stripped_void_blight_mangrove_log`, `void_blight_mangrove_planks`, `void_blight_mangrove_leaves`, `void_blight_mangrove_root`, `void_blight_mangrove_sapling`, `void_blight_pod`.
- **6 Nether Flora Blocks Registered**:
  - `spore_cannon_puffball`, `pyre_thorn_launcher`, `spore_bearing_pitcher_plump`, `blood_tendril_bramble`, `soot_veil_blight_cap`, `magma_grip_tendrils`.
- **22 Spectral Dyes Registered**:
  - Dynamically iterated and added all 22 spectral dyes (`ModItems.SPECTRAL_DYES`) to `WORLD_TAB`.
- **Multi-Loader Rebuild & Deploy**: Executed `./gradlew build deploytoDev` with **zero errors** (`BUILD SUCCESSFUL in 43s`).

### Resolution & Verification
- **Root Cause Fix**: Removed accidental `common/src/main/java/ddraig.net` directory containing duplicate `RubberLogBlockEntity.java`.
- **Build & Deploy (`./gradlew build deploytoDev`)**: Successfully executed multi-loader build and deployment with **zero errors** (`BUILD SUCCESSFUL in 43s`, 16 up-to-date tasks).

### Clean Build & Deploy Status
- **Clean Build (`./gradlew clean build deploytoDev`)**: 
  - Stopped locked Gradle Daemons.
  - Performed clean rebuild of Common, Fabric, and NeoForge modules with **zero errors** (`BUILD SUCCESSFUL in 54s`, 22 executed tasks).
  - Deployed built jar artifacts to your development environment.

### B/V/C/D/G Execution Results
- **B (Build)**: Successfully executed `./gradlew build` with zero errors (`BUILD SUCCESSFUL in 44s`).
- **V (Verify)**: Executed `./gradlew :common:compileJava` with zero errors (`BUILD SUCCESSFUL in 10s`).
- **C (Codex)**: Registered 2 new research nodes in `CodexCategoryRegistry.java` under `ENVIRONMENT & NATURE`:
  - `env_astral_veil_willow` (Astral-Veil Willow Tree, starlight bark, indigo foliage & weeping vines).
  - `env_void_blight_mangrove` (Void-Blight Mangrove Tree, void-magenta bark, stilt roots & levitation fruit pods).
- **D (Deploy)**: Executed `./gradlew deploytoDev` with zero errors (`BUILD SUCCESSFUL in 10s`).
- **G (Git / Vault / Guidelines)**: Verified OKF Obsidian Vault notes (`astral_veil_willow.md` & `void_blight_mangrove.md`), updated 24-locale translations, and confirmed privacy rules.

### Features & Updates Added
- **Nether Flowers Wall, Ceiling & Floor Facing System**:
  - Implemented 6-way `FACING` directionality across `SporeCannonPuffballBlock`, `PyreThornLauncherBlock`, `SporeBearingPitcherPlumpBlock`, `BloodTendrilBrambleBlock`, and `SootVeilBlightCapBlock`.
  - Added 3D rotation variants (`facing=up/down/north/south/east/west`) supporting seamless floor, wall, and ceiling attachment.
- **Astral-Veil Willow Wood & Flora Family**:
  - Added 100% approved Bark Side & 1px Bark Ring Top Log textures (`astral_veil_willow_log.png`, `astral_veil_willow_log_top.png`).
  - Added 100% approved Stripped Side & 1px Shaved Edge Ring Top textures (`stripped_astral_veil_willow_log.png`, `stripped_astral_veil_willow_log_top.png`).
  - Added 1:1 official Minecraft `oak_planks` direct pixel recolor (`astral_veil_willow_planks.png`).
  - Added 1:1 official Minecraft `oak_leaves` direct pixel recolor (`astral_veil_willow_leaves.png`).
  - Added 16x16 interconnecting weeping vines (`astral_veil_willow_vines.png`).
  - Added Spruce-Jungle hybrid sapling with Perlin noise (`astral_veil_willow_sapling.png`).
- **Void-Blight Mangrove Wood & Flora Family**:
  - Added 100% approved Bark Side & 1px Bark Ring Top Log textures (`void_blight_mangrove_log.png`, `void_blight_mangrove_log_top.png`).
  - Added 100% approved Stripped Side & 1px Shaved Edge Ring Top textures (`stripped_void_blight_mangrove_log.png`, `stripped_void_blight_mangrove_log_top.png`).
  - Added 1:1 official Minecraft `oak_planks` direct pixel recolor (`void_blight_mangrove_planks.png`).
  - Added 1:1 official Minecraft `oak_leaves` direct pixel recolor (`void_blight_mangrove_leaves.png`).
  - Added Void-Blight Mangrove roots (`void_blight_mangrove_root.png`).
  - Added bioluminescent fruit pod item (`void_blight_pod.png` granting 4 hunger + Levitation II).
  - Added Spruce-Jungle hybrid sapling with Perlin noise (`void_blight_mangrove_sapling.png`).
- **Full Translations & Client Cutout Map**:
  - Registered 24-locale translations for all tree blocks and items.
  - Configured `RenderType.cutout()` for leaves, vines, saplings, and roots in `EntropicaClientFabric.java`.

# Changelog — Entropica Multi-Loader Migration Update

## Build 000-1-26-222

### 🌸 100% Complete Custom Flora Contact Effects Roster (29 Flora Blocks)
- **Pyre Sprout**: Ignites entities for 2 seconds and deals $0.5\text{hp}$ fire damage on contact.
- **Stardust Aloe**: Clears fire/burns, removes Poison, and grants Regeneration I ($2\text{s}$).
- **Stardust Bell**: Grants Night Vision ($6\text{s}$) & Glowing ($4\text{s}$) when passing through the tall flower.
- **Gale-Bloom Dandelion**: Imparts a soft upward wind draft ($y \mathrel{+}= 0.25$) lofting entities into the air.
- **Fulgurite Swamp Bloom**: Discharges an electric arc dealing $1.0\text{hp}$ lightning damage and applying Slowness I ($2\text{s}$).

### ⚡ Persistent Touch Step Accumulation
- **Static Fungal Shelf Mechanics**: Removed time-based resetting of `touch_count`. Every step on a static shelf cap (at least 5 ticks apart) now persistently accumulates towards the 3-step lightning strike threshold, regardless of how much time elapses between steps.

### ⚡ Persistent Entity NBT Touch Tracking
- **Static Fungal Shelf Mechanics**: Replaced static in-memory HashMaps with persistent entity NBT compound tags via `EntityHelper.getPersistentData(living)`.
- **NBT Data Structure**:
  - `entropica_static_shelf_touch_count`: Tracks the number of consecutive steps (resets if $>40$ ticks elapse between touches).
  - `entropica_static_shelf_last_touch_time`: Enforces a 5-tick minimum step interval.
  - `entropica_static_shelf_lightning_time`: Enforces a 100-tick (5-second) strike cooldown per entity.
- **Cross-Session Persistence**: Touch step progress and strike cooldowns now persist per entity across server reloads and dimension changes.

### 🐛 Bug Fixes & Rendering Corrections
- **Cinder-Grip Lichen Texture & Phase Alignment**: Restored the approved `cinder_grip_v2_option_a_32.png` texture asset. Synchronized `blockstates/cinder_grip_lichen.json` rotation properties (`up: x=180`, `down: 0`, etc.) to match vanilla `glow_lichen.json`, completely resolving in-world out-of-phase plane rendering.
- **Pyrocyst Algae In-World Model**: Fixed `models/block/pyrocyst_algae.json` to map `"wool": "entropica:block/pyrocyst_algae_top"` (the texture key required by `minecraft:block/carpet`), fixing the missing black/purple checkered texture in-world.
- **Shelf Mushrooms Wall-Edge Alignment**: Re-anchored model elements across all 9 shelf mushroom blocks (`barrow`, `spore`, `blight`, `frost`, `cinder`, `astral`, `dawn`, `sanguine`, `static`) to `[1, 6, 4] -> [15, 10, 16]` and updated `AbstractFungalShelfBlock` VoxelShapes so shelf caps attach flush to wall faces and hang out over the block edge.
- **Static Shelf Lightning Loop Cooldown**: Enforced a 5-tick step filter and a 100-tick (5-second) strike cooldown per entity in `StaticFungalShelfCapBlock.java` to prevent rapid lightning loops while walking over static shelf caps.

### 🎨 Un-minified Language JSON Assets (24 Files)
- **Pretty-Printing**: Formatted all 24 JSON language files in `common/src/main/resources/assets/entropica/lang/` with clean 2-space indentation and unescaped UTF-8 characters (`ensure_ascii=False`) for maximum human readability across international character sets.

### 🔍 100% Codebase Translation Key Coverage
- **Literal Refactoring**: Converted 125 hardcoded `Component.literal(...)` user-visible strings across block entity UI interaction messages, ritual activation alerts, machine feedback, and item tooltips into standardized `Component.translatable(...)` calls.
- **Key Population**: Added 106 new keys to `en_us.json` and synchronized all 24 language files (`en_us`, `de_de`, `es_es`, `es_mx`, `fr_fr`, `it_it`, `nl_nl`, `pt_br`, `pl_pl`, `ru_ru`, `uk_ua`, `cs_cz`, `hu_hu`, `sv_se`, `da_dk`, `no_no`, `fi_fi`, `tr_tr`, `ja_jp`, `ko_kr`, `zh_cn`, `zh_tw`, `th_th`, `vi_vn`).
- **Audit Result**: Automated audit verified **0 hardcoded literals** and **0 missing translation keys** across the entire Java codebase.

### 🌐 15 New Language Files Added (Total 24 Locales)
- **Localization Expansion**: Created 15 new JSON language files in `common/src/main/resources/assets/entropica/lang/`:
  - `zh_tw.json` (Traditional Chinese / 繁體中文)
  - `es_mx.json` (Spanish Mexico / Español México)
  - `it_it.json` (Italian / Italiano)
  - `nl_nl.json` (Dutch / Nederlands)
  - `pl_pl.json` (Polish / Polski)
  - `uk_ua.json` (Ukrainian / Українська)
  - `cs_cz.json` (Czech / Čeština)
  - `hu_hu.json` (Hungarian / Magyar)
  - `sv_se.json` (Swedish / Svenska)
  - `da_dk.json` (Danish / Dansk)
  - `no_no.json` (Norwegian / Norsk)
  - `fi_fi.json` (Finnish / Suomi)
  - `tr_tr.json` (Turkish / Türkçe)
  - `th_th.json` (Thai / ไทย)
  - `vi_vn.json` (Vietnamese / Tiếng Việt)
- **Key Synchronization**: Synchronized all newly registered items, blocks, potted flora, particles, effects, mechanics, tooltips, and JEI recipes across all 24 language files.

### 🪵 Fungal Shelf Placement Restrictions (`AbstractFungalShelfBlock`)
- **Placement Validation**: Updated `canSurvive` in `AbstractFungalShelfBlock.java` to restrict shelf-cap attachment strictly to sturdy faces of `BlockTags.LOGS` (Overworld logs/wood, Crimson/Warped stems, Hyphae, Stripped stems), `BlockTags.WART_BLOCKS`, and `BlockTags.MUSHROOM_GROW_BLOCK`.

### 🏺 19 Potted Flower Pot Block Variants (`FlowerPotBlock`)
- **Block Registrations**: Registered 19 `FlowerPotBlock` (`POTTED_<ID>`) variants in `ModBlocks.java` for all 1-tall Entropica flora (`potted_aura_drift_sedge`, `potted_spectral_lantern_flower`, `potted_cinder_spore_mushroom`, `potted_necrotic_rose_of_jericho`, `potted_void_stalker_orchid`, `potted_stardust_aloe`, `potted_blood_root_succulent`, `potted_aegis_spire_orchid`, `potted_spore_burst_puffball`, `potted_vitae_orchid`, `potted_sanguine_lily`, `potted_abyssal_weeproot`, `potted_fulgurite_swamp_bloom`, `potted_cryo_static_shrub`, `potted_vitreous_cactus`, `potted_soot_shroud_fungi`, `potted_static_spear_grass`, `potted_amber_nectar_blossom`, `potted_rimebloom`).
- **Assets & Client**: Created blockstates, 3D cross models, and registered cutout render layers in `EntropicaClientFabric.java`.

### 🌊 Rotatable & Timed Particle Physics Engine (`TimedTintableParticleOption`)
- **Engine Architecture**: Implemented `TimedTintableParticleOption.java` & `TimedTintableParticle.java` supporting RGB tinting, custom lifespan (`maxAge` in ticks), scale multiplier, 360° initial `roll` angle, and continuous `rollSpeed` spin velocity.
- **5 Motion Physics Modes**:
  - `LINEAR` (0): Trajectory with drag & gravity.
  - `TOWARDS` (1): Vector attraction acceleration towards target coordinate `(targetX, targetY, targetZ)`.
  - `AWAY` (2): Vector repulsion push away from origin.
  - `JITTER` (3): Erratic random velocity fluctuations.
  - `NOISE` (4): Perlin/Simplex noise wave turbulence over time.

### 🎨 42 Greyscale Tintable Particles
- **18 Base Particles**: Registered `tintable_sparkle`, `tintable_spore`, `tintable_mist`, `tintable_drip`, `tintable_ember`, `tintable_rune`, `tintable_blood`, `tintable_vortex`, `tintable_shield`, `tintable_arc`, `tintable_leaf`, `tintable_wisp`, `tintable_crystal`, `tintable_bubble`, `tintable_singularity`, `tintable_sigil`, `tintable_sunbeam`, `tintable_acid`.
- **24 Elder Futhark Rune Particles**: Registered `rune_fehu`, `rune_uruz`, `rune_thurisaz`, `rune_ansuz`, `rune_raido`, `rune_kenaz`, `rune_gebo`, `rune_wunjo`, `rune_hagalaz`, `rune_nauthiz`, `rune_isa`, `rune_jera`, `rune_eihwaz`, `rune_perthro`, `rune_algiz`, `rune_sowilo`, `rune_tiwaz`, `rune_berkano`, `rune_ehwaz`, `rune_mannaz`, `rune_laguz`, `rune_ingwaz`, `rune_dagaz`, `rune_othala`.

### 🩸 Bleeding Effect & Fungal Shelf Touch Refinements
- **`BleedingEffect`**: Updated `BleedingEffect.java` to send trailing `tintable_blood` particles around bleeding entities while dealing periodic magic damage.
- **🔥 Cinder Shelf-Cap**: Sets target on fire for 3s (`igniteForSeconds(3)`).
- **⚡ Static Shelf-Cap**: 3 rapid touches within 1.5s (30 ticks) summons a real `LightningBolt` entity.
- **🩸 Sanguine Shelf-Cap**: Applies `BLEEDING` status effect (5s) + Strength I.

### 🌿 3 Remaining Custom Flora Classes
- **Static Spear-Grass (`static_spear_grass`)**: Implemented `StaticSpearGrassBlock.java` (`BushBlock`, Light 3, static shock + Speed II on contact).
- **Amber Nectar Blossom (`amber_nectar_blossom`)**: Implemented `AmberNectarBlossomBlock.java` (`FlowerBlock`, Light 5, Glass Bottle interaction extracts `amber_nectar_bottle`).
- **Rimebloom (`rimebloom`)**: Implemented `RimebloomBlock.java` (`FlowerBlock`, Light 3, Slowness I + `tintable_mist` frostbite particles).

### 🍄 Abstract 3D Wall Bracket Architecture (`AbstractFungalShelfBlock`)
- **Parent Architecture**: Implemented `AbstractFungalShelfBlock` extending `HorizontalDirectionalBlock` with slender 3D wall-bracket VoxelShapes ($14 \times 4 \times 12$ pixels) dynamically rotated based on attached wall face (`NORTH`, `SOUTH`, `EAST`, `WEST`) with a solid stepping platform at $y=10.0$.
- **9 Essence-Aligned Species**:
  - **Barrow (`barrow_fungal_shelf_cap`)**: Grants Resistance I (4s) on contact. Drops `barrow_fungal_cap`, crafts `spectral_dye_barrow`.
  - **Spore (`spore_fungal_shelf_cap`)**: Grants Regeneration I (4s) on contact. Drops `spore_fungal_cap`, crafts `spectral_dye_spore`.
  - **Blight (`blight_fungal_shelf_cap`)**: Inflicts Nausea I (4s) + Poison I (3s) on contact. Drops `blight_fungal_cap`, crafts `spectral_dye_blight`.
  - **Frost (`frost_fungal_shelf_cap`)**: Inflicts Slowness I (4s) on contact. Drops `frost_fungal_cap`, crafts `spectral_dye_frost`.
  - **Cinder (`cinder_fungal_shelf_cap`)**: Extinguishes fire & grants Fire Resistance I (6s). Drops `cinder_fungal_cap`, crafts `spectral_dye_cinder`.
  - **Astral (`astral_fungal_shelf_cap`)**: Grants Jump Boost II (4s) on contact. Drops `astral_fungal_cap`, crafts `spectral_dye_stardust`.
  - **Dawn (`dawn_fungal_shelf_cap`)**: Cleanses debuffs & grants Glowing (6s) (Light 12). Drops `dawn_fungal_cap`, crafts `spectral_dye_amber`.
  - **Sanguine (`sanguine_fungal_shelf_cap`)**: Drains 0.5 heart, grants Strength I (4s). Drops `sanguine_fungal_cap`, crafts `spectral_dye_sanguine`.
  - **Static (`static_fungal_shelf_cap`)**: Delivers 0.5 lightning zap & grants Speed II (3s). Drops `static_fungal_cap`, crafts `spectral_dye_fulgurite`.

### 🎨 16x16 Pixel Art Suite & Assets
- **16x16 Textures**: Generated 27 dedicated 16x16 block textures (`top`, `bottom`, `edge`) and 18 item icons.
- **Assets & Client**: Registered blockstates, 3D cuboid block models, 1.21.4+ item definitions, shearing handlers in `FloraHarvestHelper.java`, cutout render layers in `EntropicaClientFabric.java`, and English localization strings.
- **Codex & Vault**: Registered 9 Codex nodes in `CodexCategoryRegistry.java` and created 9 OKF Obsidian Vault markdown notes in `wiki/entities/blocks/flora/`.

### 🌋 3 New Nether Flora Species Implemented
- **Cinder-Grip Lichen (`cinder_grip_lichen`)**: Implemented multi-directional wall/floor/ceiling lichen block (`CinderGripLichenBlock` extending `GlowLichenBlock`) with 100% seamless tiling 32x32 volcanic ember vein textures (Option A), Light Level 7, Speed I & 0.5 fire damage on contact, shearing right-click dropping `cinder_lichen_flakes`.
- **Soot-Shroud Fungi (`soot_shroud_fungi`)**: Implemented 1-tall mushroom flora (`SootShroudFungiBlock`) featuring locked dark violet parasol cap with glowing gold underside gills & spots (Option B), Light Level 8, sulfuric ash spore particle cloud, Nausea I + Blindness I debuffs, shearing right-click dropping `soot_shroud_cap`.
- **Pyrocyst Algae (`pyrocyst_algae`)**: Implemented fire-resistant thermal carpet flora (`PyrocystAlgaeBlock` extending `CarpetBlock`) with bioluminescent side vesicles & maroon thermal turf top (Option A), Light Level 11, 100% fire/lava item destruction immunity, fire-extinguishing + Fire Resistance I (10s) aura, right-click harvestable for `pyrocyst_vesicle` with 6,000-tick (5-minute) regrowth cooldown.

### 🧪 Special Harvest Drops & Spectral Dyes
- **Cinder Ember Spectral Dye (`spectral_dye_cinder`)**: Registered dye (`#F97316`) crafted via shapeless recipe from `cinder_lichen_flakes` + `glass_bottle`.
- **Soot Ash Spectral Dye (`spectral_dye_soot`)**: Registered dye (`#312E81`) crafted via shapeless recipe from `soot_shroud_cap` + `glass_bottle`.
- **Pyrocyst Crimson Spectral Dye (`spectral_dye_pyrocyst`)**: Registered dye (`#EF4444`) crafted via shapeless recipe from `pyrocyst_vesicle` + `glass_bottle`.
- **Dynamic 2-Layer Tinting**: Configured 2-layer bottle models (`spectral_dye_bottle` + `spectral_dye_fill`) for all 3 dyes.

### 🎨 Python Texture Generation & 2x2 Grid Tiling Proofs

- **Python Texture Pipeline**: Generated crisp 32x32 textures and item icons via Pillow scripts; verified 100% 2D seamless tiling for Cinder-Grip Lichen across 512x512 2x2 grid proofs.
- **Client & Assets**: Registered cutout render types in `EntropicaClientFabric.java`, blockstates, block models, item models, 1.21.4+ `items/` definitions, and localized strings in `en_us.json`.

### 📚 Codex & OKF Obsidian Vault Sync
- **Entropic Codex**: Registered `env_cinder_grip_lichen`, `env_soot_shroud_fungi`, and `env_pyrocyst_algae` under `ENVIRONMENT & NATURE` in `CodexCategoryRegistry.java`.
- **OKF Obsidian Vault**: Created notes `Cinder-Grip Lichen.md`, `Soot-Shroud Fungi.md`, and `Pyrocyst Algae.md` in `wiki/entities/blocks/flora/`.

### 🌸 Botanical Shearing JEI Category (`jei.entropica.flora_harvesting`)
- **New JEI Recipe Category**: Created `FloraHarvestingCategory` and `FloraHarvestingRecipe` registered under UID `entropica:flora_harvesting`.
- **Harvest Mapping Preview**: Displays all 1-tall and 2-tall flower and orchid shearing inputs, `Items.SHEARS` catalyst, and resulting petal item outputs directly inside Just Enough Items.
- **Localization**: Added `"jei.entropica.flora_harvesting": "Botanical Shearing"` in `en_us.json`.

### ⏱️ Extended 5-Minute Crop Harvest Cooldown
- **6,000-Tick (5-Minute) Cooldown**: Extended right-click harvest cooldowns across all botanical yield blocks (`AbyssalWeeprootBlock`, `SporeBurstPuffballBlock`, `MistVeilMarshmallowBlock`, `CryoStaticShrubBlock`, `BloodRootSucculentBlock`, `GaleBloomDandelionBlock`) to 6,000 ticks (5 minutes of real-time play), matching slow-growing crop & berry bush harvest mechanics.

### ✂️ Shears Petal Harvesting & Flower Destruction
- **`FloraHarvestHelper` Integration**: Implemented right-click shears interaction across all 1-tall and 2-tall flower and orchid blocks (`AegisRoseBlock`, `AegisSpireOrchidBlock`, `SoulFlameOrchidBlock`, `VitaeOrchidBlock`, `VoidStalkerOrchidBlock`, `NecroticRoseOfJerichoBlock`, `SanguineLilyBlock`, `AuroralButtercupBlock`, `StardustBellBlock`, `FulguriteSwampBloomBlock`, `GaleBloomDandelionBlock`, `CinderSporeMushroomBlock`, `SpectralLanternFlowerBlock`).
- **Petal Yield & Sound**: Right-clicking flowers with Shears drops 1–2 Petals (for 1-tall flowers) or 3–4 Petals (for 2-tall flowers), damages the shears by 1 durability, plays `SoundEvents.SHEEP_SHEAR`, and breaks the flower block in-world without dropping the flower itself.

### 🌿 Flora Right-Click Harvest Limits
- **Cooldown Enforcement**: Implemented a 100-tick (5-second) per-block harvest cooldown across all harvestable flora (`AbyssalWeeprootBlock`, `SporeBurstPuffballBlock`, `MistVeilMarshmallowBlock`, `CryoStaticShrubBlock`, `BloodRootSucculentBlock`, `GaleBloomDandelionBlock`), preventing infinite right-click duping of botanical items.

### 🪷 Auroral Lily Pad Water Placement
- **`WaterlilyBlock` Class**: Created `AuroralLilyPadBlock` extending Vanilla `WaterlilyBlock` with thin $16 \times 1.5 \times 16$ collision box.
- **`PlaceOnWaterBlockItem` Registration**: Updated `AURORAL_LILY_PAD_ITEM` in `ModItems.java` to `PlaceOnWaterBlockItem(ModBlocks.AURORAL_LILY_PAD.get(), properties)`, enabling placement on water surfaces.

### ✂️ Complete Cutout Transparency Fixes
- **Model JSON Audit & Repairs**: Added `"render_type": "minecraft:cutout"` across all missing 2-tall parent block models (`tall_aegis_rose`, `tall_aegis_spire_orchid`, `tall_auroral_lily_pad`, `tall_necrotic_rose_of_jericho`, `tall_sanguine_lily`, `tall_soul_flame_orchid`, `tall_vitae_orchid`, `tall_void_stalker_orchid`) and `barrow_moss_carpet.json`, eliminating black opaque boxes in-world.
- **Render Layer Maps**: Registered `ModBlocks.AURORAL_LILY_PAD.get()` in `EntropicaClientFabric.java` `BlockRenderLayerMap`.

### 🌺 Confirmed Option A Orchid Textures Applied
- **Vitae Orchid (`vitae_orchid`)**: Applied **Option A (Classic Handcrafted `vitae_v5` / `v6`)** for both 1-tall (`vitae_orchid_1tall_v5.png`) and 2-tall top/bottom pairs (`vitae_orchid_2tall_top_v6.png` / `vitae_orchid_2tall_bottom_v6.png`).
- **Soul-Flame Orchid (`soul_flame_orchid`)**: Applied **Option A (Organic Phalaenopsis Recolor `soul_phalaenopsis`)** for both 1-tall (`soul_flame_orchid_1tall_fresh.png`) and 2-tall top/bottom pairs (`soul_flame_orchid_2tall_top_fresh.png` / `soul_flame_orchid_2tall_bottom_fresh.png`).
- **Dev Deployment**: Executed `./gradlew deploytoDev` with clean multi-loader jar compilation and CurseForge instance deployment.

### 🌸 Exact Master Vitae Orchid Preview Synchronized
- **Vitae Orchid (`vitae_orchid`)**: Re-sampled directly from master preview `vitae_orchid_1tall_v5.png` (256x256 -> 32x32), restoring the unique handcrafted Vitae Orchid bloom architecture.
- **Tall Vitae Orchid Top (`tall_vitae_orchid_top`)**: Re-sampled directly from master preview `vitae_orchid_2tall_top_v6.png` (256x256 -> 32x32).
- **Tall Vitae Orchid Bottom (`tall_vitae_orchid_bottom`)**: Re-sampled directly from master preview `vitae_orchid_2tall_bottom_v6.png` (256x256 -> 32x32).

### 🌺 Authentic Phalaenopsis Orchid Structure Restored
- **Aegis-Spire Orchid**: Replaced incorrect circular flower / V-leaf placeholder textures with the authentic **Phalaenopsis Blue Orchid** (`phalaenopsis_blue_orchid_1tall_32.png`, `phalaenopsis_natural_leaf_top_sliced32.png`, and `phalaenopsis_natural_leaf_bottom_sliced32.png`), featuring cascading butterfly-wing petals (`#2563EB` / `#60A5FA` / `#DBEAFE`), white column dots, slender arching purplish-brown stems, and broad green basal strap leaves (`#166534` / `#15803D`).
- **Soul-Flame Orchid**: Recolored from authentic Phalaenopsis architecture into Soulfire Cyan (`#38BDF8` / `#06B6D4`).
- **Vitae Orchid**: Recolored from authentic Phalaenopsis architecture into Vitae Pink (`#FF6B9D` / `#E087EC`).
- **Void-Stalker Orchid**: Recolored from authentic Phalaenopsis architecture into Abyssal Purple (`#818CF8` / `#4C1D95`).

### 🌸 Approved Orchid Textures Restored
- **Aegis-Spire Orchid**: Re-sampled directly from locked preview `aegis_spire_orchid_1tall_authentic.png` and `tall_aegis_spire_orchid_top_sliced32.png` / `bottom_sliced32.png`.
- **Soul-Flame Orchid**: Re-sampled directly from locked preview `soul_flame_orchid_1tall_fresh.png` and `2tall_top_fresh.png` / `2tall_bottom_fresh.png`.
- **Vitae Orchid**: Re-sampled directly from locked preview `vitae_orchid_1tall_v5.png` and `2tall_top_v6.png` / `2tall_bottom_v6.png`.
- **Void-Stalker Orchid**: Re-sampled directly from locked preview `void_stalker_orchid_1tall_fresh.png` and `2tall_top_fresh.png` / `2tall_bottom_fresh.png`.

### 🧪 Spectral Dye Inventory Tinting Fix
- Added `"tints": [{"type": "minecraft:constant", "value": -1}, {"type": "entropica:spectral_dye_tint"}]` to all 18 Spectral Dye item models in `items/`.
- Dynamic RGBA color provider registered for layer1 tinting across both Fabric (`EntropicaClientFabric.java`) and NeoForge (`ModClientEvents.java`).

### 🌿 In-World Cutout Transparency Fix
- Added `"render_type": "minecraft:cutout"` to all 51 flora block models in `models/block/`.
- Registered all flora species, 2-tall flowers, saplings, and leaf blocks in `BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutout(), ...)` in `EntropicaClientFabric.java`.

### 🚀 `./gradlew deploytoDev` Execution
- **CurseForge Instance Deployment**: Executed `./gradlew deploytoDev` across all modules:
  - Fabric JAR copied to: `C:/Users/Ddraig__/curseforge/minecraft/Instances/entropicadevFAB/mods`
  - NeoForge JAR copied to: `C:/Users/Ddraig__/curseforge/minecraft/Instances/entropicadevNF/mods`
- **Stale Build Cleanup**: Removed legacy `Entropica-fabric-0.0.1-build-001-a.jar` from `entropicadevFAB/mods` to prevent CurseForge launcher conflicts.

### 🚀 Production JAR Compilation (`./gradlew build`)
- **Fabric Mod Production Jar**: Compiled `Entropica-fabric-1.0.0.jar` (10.0 MB) in `fabric/build/libs/`.
- **NeoForge Mod Production Jar**: Compiled `Entropica-neoforge-1.0.0.jar` (10.1 MB) in `neoforge/build/libs/`.
- **Deployment Status**: All 28 flora species, Amber-Wood tree set, log stripping mechanics, 1.21.4+ `items/` definitions, and localized strings are compiled and ready for dev server/client testing.

### 🌸 Gallery Texture Enforcement (Blocks & Items)
- **Synchronized Master Textures**: Re-sampled and synchronized all 28 flora species (1-tall and 2-tall top/bottom variants) from our master locked gallery previews directly into game block textures (`common/src/main/resources/assets/entropica/textures/block/`).
- **Unified Item Model Layer0 References**: Enforced that item models (`models/item/`) and 1.21.4+ asset definitions (`items/`) for all 28 flora species use `layer0` pointing directly to their corresponding master block/bloom textures (`entropica:block/<flower>` for 1-tall, `entropica:block/<tall_flower>_top` for 2-tall), ensuring both items in inventory/hand and placed blocks in-world share 100% identical visual art.

### 🌹 Necrotic Rose of Jericho Item Model Fix
- **Fixed Layer0 Texture Path**: Corrected `models/item/necrotic_rose_of_jericho.json` layer0 reference from non-existent `entropica:block/necrotic_rose_of_jericho_blooming` to existing `entropica:block/necrotic_rose_of_jericho`, resolving the black-and-purple missing texture checkerboard.
- **Re-Mirrored 1.21.4+ `items/`**: Re-mirrored `items/necrotic_rose_of_jericho.json` to reference the fixed item model.

### 🌐 Localization Expansion (`en_us.json`)
- **Added 30 Missing Translation Keys**: Added English translation keys for `item.entropica.necrotic_rose_of_jericho` ("Necrotic Rose of Jericho"), `tall_necrotic_rose_of_jericho`, `sanguine_lily`, `auroral_lily_pad`, `aegis_rose`, `amber_log`, `amber_wood`, `amber_planks`, `amber_leaves`, `amber_sapling`, and block variants.

### 🌹 Locked Necrotic Rose of Jericho Texture Restoration
- **Restored Organic Voronoi/Perlin Texture**: Restored 100% exact locked 32x32 textures for Necrotic Rose of Jericho:
  - `necrotic_rose_of_jericho.png` (from locked `necrotic_rose_of_jericho_perlin_voronoi_stem_preview.png`).
  - `tall_necrotic_rose_of_jericho_top.png` (from locked `necrotic_rose_2tall_top_fresh.png`).
  - `tall_necrotic_rose_of_jericho_bottom.png` (from locked `necrotic_rose_2tall_bottom_fresh.png`).

### 🏷️ Creative Tab World Isolation (`ModCreativeTabs.java`)
- **`isWorldItem()` Category Filter Expansion**: Expanded `isWorldItem(Item item)` filter method to include all flora block paths (`rose`, `lily`, `orchid`, `buttercup`, `bell`, `bloom`, `shrub`, `cactus`, `moss`, `weeproot`, `succulent`, `puffball`, `reed`, `thistle`, `marshmallow`, `sprout`, `sedge`, `lantern`, `mushroom`, `aloe`, `shimmerpetal`, `amber`, `silver_pine`, `rubber`).
- **Strict Tab Segregation**: Ensures all 28 flora species, botanical blocks, logs, wood, planks, leaves, and saplings are excluded from `BLOCKS_TAB` (Building Blocks) and placed **exclusively in `WORLD_TAB` (Entropica: Natural World)**.

### 📚 OKF Obsidian Vault & Codex Verification
- **Auroral Lily Pad Note**: Created `wiki/entities/blocks/flora/Auroral Lily Pad.md` in the Entropica OKF Obsidian Vault.
- **Entropic Codex Verification**: Verified node entries in `CodexCategoryRegistry.java` for all flora and materials.

### ⚙️ Git & Deployment
- **Clean Working Tree**: Verified clean git working tree with 0 unstaged/untracked changes.

### 📁 `items/` & `models/item/` Dual-Asset Directory Mirroring
- **1.21.4+ Item Model Mirroring**: Synchronized all missing 54 item model entries from `common/src/main/resources/assets/entropica/models/item/` into `common/src/main/resources/assets/entropica/items/` using the 1.21.4+ asset specification:
  ```json
  {
    "model": {
      "type": "minecraft:model",
      "model": "entropica:item/<item_id>"
    }
  }
  ```
- **100% Directory Parity**: Confirmed 0 missing items between `models/item/` and `items/` (both directories fully populated with 1802 item definitions).

### 📄 Blockstate & Block Model JSON Generation
- **1-Tall Flowers (`vitae_orchid`, `necrotic_rose_of_jericho`, `auroral_lily_pad`)**: Generated cross-parent block model JSONs (`models/block/`) and blockstate JSONs (`blockstates/`).
- **2-Tall Flowers & Bushes (`tall_aegis_rose`, `tall_necrotic_rose_of_jericho`, `tall_sanguine_lily`, `tall_auroral_lily_pad`, `tall_soul_flame_orchid`, `tall_vitae_orchid`, `tall_void_stalker_orchid`, `tall_aegis_spire_orchid`)**: Generated `half=lower` and `half=upper` variant blockstate JSONs and corresponding `_bottom.json` and `_top.json` cross block models.
- **100% Audit Coverage**: Verified 0 missing item models, 0 missing blockstates, and 0 missing block models across all 28 flora species, botanical collectibles, and tree sets.

### 🌿 Auroral Lily Pad Item Registration
- **`AURORAL_LILY_PAD_ITEM`**: Registered `auroral_lily_pad` BlockItem in `ModItems.java` and added to `WORLD_TAB` in `ModCreativeTabs.java`.
- **100% Audit Pass**: Confirmed all 28 flora species, aquatic plants, ceiling vines, botanical drops, and tree set blocks have registered items in `ModItems.java` and are present in `WORLD_TAB`.

### 🎨 Spectral Dye 2-Layer Dynamic Tinting Fix
- **Dynamic 2-Layer Item Models**: Fixed item models for all 18 Spectral Dyes (`spectral_dye_aegis.json`, `spectral_dye_amber.json`, `spectral_dye_auroral.json`, `spectral_dye_fulgurite.json`, `spectral_dye_gale.json`, `spectral_dye_necrotic.json`, `spectral_dye_sanguine.json`, `spectral_dye_soulfire.json`, `spectral_dye_stardust.json`, `spectral_dye_vitae.json`, `spectral_dye_void_stalker.json`, etc.) to point to `"layer0": "entropica:item/spectral_dye_bottle"` and `"layer1": "entropica:item/spectral_dye_fill"`.
- **Dynamic Luminescence Color Rendering**: Resolves missing texture fallback checkerboards by driving layer1 RGBA tinting via `ModItemColors.java` / `SpectralDyeItem`.

### 🪵 Minecraft 1.21.10 Amber-Wood Blockstates & Models Audit
- **Block Item Models**: Created `amber_log.json`, `stripped_amber_log.json`, `amber_wood.json`, `stripped_amber_wood.json`, `amber_planks.json`, `amber_leaves.json`, `amber_sapling.json`, and `auroral_lily_pad.json` under `models/item/`.
- **Blockstate & Block Models**: Generated complete blockstate JSONs and block model JSONs (`cube_column`, `cube_column_horizontal`, `cube_all`, `cross`) under `blockstates/` and `models/block/`.
- **Creative Tab Audit**: Confirmed all 7 Amber-Wood items, Amber Chunks, flora blocks, and Spectral Dyes are registered in `WORLD_TAB` in `ModCreativeTabs.java`.

### 📖 Entropic Codex Integration
- **`materials_amber_chunk` Research Node**: Registered Amber Chunk under `MATERIALS` in `CodexCategoryRegistry.java` with 3D item icon, orbit position (`a_ing + 0.20f`), and research details.

### 📚 OKF Obsidian Vault Synchronization
- **`Amber Chunk.md` Note**: Created OKF note in `wiki/entities/items/Amber Chunk.md` detailing obtaining mechanics via Axe log stripping and crafting uses in alchemical distills, wand foci, arcana book binding, and crystal alchemy.

### ⚙️ Git & Deployment Readiness
- **Clean Staging & Local Commit**: Verified all assets, handlers, recipes, language keys, and docs are staged and committed locally without including any AI/metadata files.

### 💎 Amber Chunk Texture & JEI Description Update
- **Crystalline Noise & Micro-Facets (`amber_chunk.png`)**: Enhanced 32x32 texture with micro-facet specular noise and internal refraction gradients (`#FDE047`, `#F59E0B`, `#D97706`, `#B45309`, `#4A2810`).
- **JEI Description Text Update (`en_us.json`)**: Updated `jei.entropica.info.amber_chunk` text per user directive:
  > *"Amber Chunks are harvested by stripping the bark off Amber Logs or Amber Wood using an Axe.\n\nUsed in alchemical distils, wand foci, arcana book binding, and crystal alchemy."*
- **Gallery Update**: Updated `amber_wood_gallery.md` with Amber Chunk V2 noise preview.

### 💎 Amber Chunk Item & Log Stripping Mechanic
- **`AMBER_CHUNK` Item Registration**: Registered `amber_chunk` in `ModItems.java` and added to `WORLD_TAB` in `ModCreativeTabs.java`.
- **32x32 Item Texture & Model**: Handcrafted 32x32 high-resolution pixel art texture `amber_chunk.png` (translucent resin crystal chunk with specular highlights and 100% transparent background) and item model `amber_chunk.json`.
- **Axe Log Stripping Mechanic (`AmberLogStrippingHandler.java`)**: Implemented in-world stripping interaction when right-clicking `AMBER_LOG` or `AMBER_WOOD` with an Axe:
  - Converts block state to `STRIPPED_AMBER_LOG` / `STRIPPED_AMBER_WOOD` (preserving pillar axis).
  - Plays `SoundEvents.AXE_STRIP` sound.
  - Damages Axe durability (-1).
  - Drops 1-2x `AMBER_CHUNK` items in-world at the block position. Wired in Fabric (`EntropicaFabric.java`) and NeoForge (`GlassCleansingNeoForgeEvents.java`).
- **JEI Description Info Registration**: Registered `jei.entropica.info.amber_chunk` in `EntropicaJEIPlugin.java` (`addIngredientInfo`) and added localization text in `en_us.json`.

### 🪵 Oak-Planks Texture Structure Remap (`amber_planks.png`)
- **Vanilla Minecraft Oak Planks Color-Mapping**: Extracted official `assets/minecraft/textures/block/oak_planks.png` texture map and mapped its board structure 1:1 into the darkened Golden Amber palette (`#D7B932`, `#CD8209`, `#B46205`, `#964407`, `#78320A`), eliminating harsh dark brick end-joints while preserving 100% horizontal and vertical seamless flow.
- **Gallery Update**: Updated `amber_wood_gallery.md` with V7 previews.

### 🪵 Palette Darkening
- **Darkened Stripped Log & Planks (`stripped_amber_log.png`, `amber_planks.png`)**: Darkened all color channels by ~15% (`#964407`, `#B46205`, `#CD8209`, `#D7B932`, `#78320A`), yielding a deeper, richer golden amber shade matching `amber_log_top.png` while maintaining 100% horizontal and vertical seamless flow.
- **Gallery Update**: Updated `amber_wood_gallery.md` with V6 previews and 2x2 grid proofs.

### 🔒 Locked Assets
- **`amber_log.png` & `amber_log_top.png` [LOCKED 🔒]**: Officially locked side log bark and top squarer-radial growth rings from future edits per user directive.
- **`amber_leaves.png` [LOCKED 🔒]**: Officially locked Oak-leaf architecture foliage from future edits per user directive.

### 🪵 Palette Desaturation & Spruce Sapling Architecture
- **Desaturated Stripped Log & Planks (`stripped_amber_log.png`, `amber_planks.png`)**: Re-sampled desaturated, natural golden amber tones directly from `amber_log_top.png` (`#B45309`, `#D97706`, `#F59E0B`, `#FDE047`), softening previous vibrancy while preserving 100% horizontal and vertical seamless tiling.
- **Spruce Sapling Architecture (`amber_sapling.png`)**: Extracted official `assets/minecraft/textures/block/spruce_sapling.png` texture from the Minecraft client JAR and color-mapped it 1:1 into 32x32 high-resolution pixel art featuring a chestnut stem base and tiered golden amber evergreen foliage (100% transparent background).
- **Gallery Update**: Updated `amber_wood_gallery.md` with V5 previews.

### 🪵 Amber-Wood Set Seamless Tiling & Vanilla Oak Integration
- **100% Horizontal & Vertical Seamless Tiling**:
  - `stripped_amber_log.png`: Engineered 2D periodic sinusoidal wave functions guaranteeing zero seam lines across both horizontal and vertical borders when placing log walls or pillars.
  - `amber_planks.png`: Restructured horizontal plank bevels ($y=7, 15, 23, 31$) and alternating vertical end-joints ($x=16, 8, 24, 0/32$) ensuring 100% seamless tiling across any 2D surface. Verified with 2x2 grid preview proofs.
- **Vanilla Minecraft Oak Leaf Color-Mapping (`amber_leaves.png`)**:
  - Extracted the exact official `assets/minecraft/textures/block/oak_leaves.png` grayscale texture map from the Minecraft 1.20.1/1.21.10 client jar.
  - Mapped vanilla Oak leaf cluster cutout shapes (`alpha == 0`), leaf blade highlights, and vein shadows directly into the Autumn Golden Amber palette (`#FDE047`, `#EAB308`, `#F59E0B`, `#D97706`, `#92400E`).
- **Gallery Update**: Updated `amber_wood_gallery.md` with V4 previews and 2x2 grid tiling proofs.

### 🪵 Amber-Wood Set Refinements
- **Smoothed Wood Stripped Log Side (`stripped_amber_log.png`)**: Replaced grid pixel noise with smooth, flowing, vertical wood grain striations in golden amber tones.
- **Rubber-Matched Amber Planks (`amber_planks.png`)**: Modeled 1:1 after `rubber_planks.png` layout — 4 horizontal plank rows with dark bevel grooves at $y=7, 15, 23, 31$, offset vertical end-joint seams at $x=22, 9, 27$, and smooth horizontal wood grain striations in Golden Amber.
- **Squarer Radial Log Tops (`amber_log_top.png` & `stripped_amber_log_top.png`)**: Increased Chebyshev squircle weighting ($0.25 \cdot \text{Euclidean} + 0.75 \cdot \text{Chebyshev}$) for squarer growth rings that remain smooth and radial near the center core.
- **Autumn Oak Amber Leaves (`amber_leaves.png`)**: Modeled after default Minecraft Oak Leaves cluster shape and outline — 3D leaf blade bunches, dark leaf outline borders, and natural transparent cutout gaps (`alpha == 0`), rendered in rich Autumn Golden Amber (`#FDE047`, `#EAB308`, `#F59E0B`, `#D97706`, `#92400E`).
- **Gallery Update**: Updated `amber_wood_gallery.md` displaying V3 previews.

### 🪵 Amber-Wood Wood Set (`amber_wood`)
- **Block Registrations**: Registered full wood family in `ModBlocks.java` and `ModItems.java`:
  - `AMBER_LOG` (`amber_log`)
  - `STRIPPED_AMBER_LOG` (`stripped_amber_log`)
  - `AMBER_WOOD` (`amber_wood`)
  - `STRIPPED_AMBER_WOOD` (`stripped_amber_wood`)
  - `AMBER_PLANKS` (`amber_planks`)
  - `AMBER_LEAVES` (`amber_leaves`)
  - `AMBER_SAPLING` (`amber_sapling`)
- **32x32 High-Resolution Textures**: Handcrafted 100% 32x32 pixel art textures for all 7 blocks:
  - Chestnut bark with glowing golden-amber resin seams (`#D97706`, `#F59E0B`, `#FDE047`).
  - Concentric top rings with a glowing amber resin core.
  - Smooth honeyed golden-orange stripped wood grain and planks (`#F97316`, `#F59E0B`).
  - Dense golden-amber foliage leaves and transparent compact sapling.
- **Creative Tab**: Added all 7 items to `WORLD_TAB` in `ModCreativeTabs.java`.
- **Codex & Vault Integration**: Registered `env_amber_wood` under `ENVIRONMENT & NATURE` in `CodexCategoryRegistry.java` and verified `Amber-Wood Wood Set.md` in the Obsidian Vault.
- **Gallery Artifact**: Created `amber_wood_gallery.md` displaying 100% transparent upscale previews.

### 🌹 1-Tall Aegis Rose (`aegis_rose.png`) & Necrotic Rose-of-Jericho (`necrotic_rose_of_jericho.png`)
- **1:1 Tea Rose Photo-Matched Architecture**: Modeled directly after the user's uploaded Tea Rose reference photo (`media__1786382960410.png`).
  - Features a 45° perspective view bloom head with central spiral swirl cone, middle cupped petal arcs, and wide outer petal wings flaring out horizontally and curling slightly downwards at the tips.
  - Aegis Rose uses the Electric Cerulean Royal Cyan Blue palette (`#E0F2FE`, `#00C8FF`, `#0080FF`, `#0040A0`, `#002060`).
  - Necrotic Rose uses the Jericho Decay Purple palette (`#F3E8FF`, `#C084FC`, `#A855F7`, `#7E22CE`, `#581C87`) with a Lime Throat Core (`#A3E635` / `#84CC16`).
- **Dark Petal Rim Shading**: Applied dark shadow rim pixels along the underside and top edges of individual petal arcs inside the bloom head to establish clear 3D depth and petal separation.
- **3D Cylindrical Stalk & Ambient Occlusion**: Applied 3D cylindrical stalk highlights (`#1DAD52`) and deep shadow grooves (`#105929`) along the stems, with ambient occlusion shading directly beneath the bloom head.
- **Perlin & Voronoi Stem Noise**: Enhanced stem columns and leaf blades with 2D Perlin organic noise and Voronoi cell distance noise for natural bark texturing and leaf veining.
- **Matched Bush Green Palette**: Stem and leaf colors matched 1:1 to the locked 2-tall rose bushes (`#147F3C`, `#147B3A`, `#1DAD52`, `#199748`, `#168941`).
- **100% Transparent Background**: Rendered with 100% transparent backgrounds (`alpha == 0`).

### 🔒 Complete Entropica Flora Locking
- **100% Approved & Locked 🔒**: Officially locked all flora block textures across Entropica:
  - 1-Tall Aegis Rose (`aegis_rose.png`)
  - 1-Tall Necrotic Rose-of-Jericho (`necrotic_rose_of_jericho.png`)
  - 1-Tall Sanguine Lily (`sanguine_lily.png`)
  - 2-Tall Aegis Rose Bush (`aegis_rose_top.png`, `aegis_rose_bottom.png`)
  - 2-Tall Necrotic Rose Bush (`necrotic_rose_top.png`, `necrotic_rose_bottom.png`)
- **Gallery Update**: Updated `flora_previews_gallery.md` to reflect all locked block textures.

### 🌸 Botanical Drops System (Petals, Nectars & Spectral Dyes)
- **Unified 36 Botanical Drop Triad**: Implemented full drop sets (**Petal**, **Nectar**, **Spectral Dye**) for all 12 flower species in Entropica.
  - Base petal teardrop shape derived from `stardust_bell_petal.png` and `auroral_petal.png`.
  - Base viscous nectar droplet derived from `vitae_nectar.png`, `stardust_nectar.png`, and `amber_nectar.png`.
  - Base spectral dye potion bottle derived from `spectral_dye_bottle.png` and `spectral_dye_aegis.png`.
- **ModItems Registration**: Registered all 36 items in `ModItems.java`.
- **Item Models**: Generated 36 item model JSON files in `common/src/main/resources/assets/entropica/models/item/`.
- **Visual Previews Artifact**: Created `botanical_drops_gallery.md` with 8x high-res previews of all 36 items.
- **Obsidian Vault Sync**: Synced 36 item markdown entries in `wiki/entities/items/`.

### 🪷 Sanguine Lily (1-Tall Stargazer Lily)
- **Approved & Locked**: Finalized 1-tall Sanguine Lily texture (`sanguine_lily.png`) with 30° rotated bloom head, wrapped crimson core (`#DC2626`) with 1+ pixel white margin (`#FFFFFF`), lime throat (`#84CC16`), and 4 compact botanical scoop leaves.
- **Registry Update**: Removed 2-tall Sanguine Lily from `ModBlocks.java` and `ModItems.java` (all Lilies are strictly 1-tall compact blocks).

## Build 000-1-26-221

### 🪻 Authentic Phalaenopsis Blue Orchid Redesign & 32x64 Sliced Continuity
- **Reference Image Alignment**: Modeled directly after the user's reference image of a Phalaenopsis Blue Orchid. Features a slender purplish-brown arching spike (`#2E1C14`), cascading cobalt blue Phalaenopsis blooms (`#2563EB`) with soft sky-blue/white margins (`#DBEAFE`), deep indigo 3-lobed lip/labellum (`#1E1B4B`), and smooth forest green basal strap leaves (`#166534`, `#15803D`).
- **32x64 Sliced Continuity**: Rendered as a continuous 32x64 pixel art masterpiece and sliced cleanly at $y=32$ into `tall_aegis_spire_orchid_top.png` ($y=0 \to 31$) and `tall_aegis_spire_orchid_bottom.png` ($y=32 \to 63$).
- **Centered Stem Attachment**: Stem rises straight through $x=15 \to 17$ and plugs directly into the throat center of the lower Phalaenopsis bloom (`cx=16, cy=25`) on the top block.
- **Natural Bezier Strap Leaves**: Rendered smooth tongue-shaped leather strap leaves on `tall_aegis_spire_orchid_bottom.png` using quadratic Bezier spines (`15,60 \to 6,58 \to 3,52` on left; `17,60 \to 26,58 \to 29,52` on right), with specular highlight rims and deep shadow bases.
- **Preserved 1-Tall Compact Block**: `aegis_spire_orchid.png` remains 100% UNTOUCHED in its exact approved compact form (`aegis_spire_orchid_1tall_perfect.png`).
- **Clean Bark Stem Spike**: Exposed clean slender bark-textured purplish-brown stem spike (`#2E1C14`) with organic dither noise ($\pm 14$) and zero green overlap on the stem spike.

### 🌸 Mist-Veil Marshmallow & Flora Aesthetic Polish
- **Clean Mist-Veil Marshmallow Bloom**: Re-rendered `mist_veil_marshmallow.png` with 100% clean pastel lavender and rose petals, zero stray floating pixels, and crisp botanical shading.
- **Pyre-Sprout Dither Noise**: Updated `pyre_sprout.png` with organic dither noise across the charcoal stem, basal leaves, and flame petals (`#B71C1C`, `#E65100`, `#FFD600`), matching Aegis Rose (`aegis_rose`).

### 🌾 Item Texture Consolidation & Climbable Ladder Mechanics
- **Abyssal Weeproot Item & Ladder Feature**: Fixed item model mapping to `abyssal_weeproot.png` and registered `abyssal_weeproot` & `abyssal_weeproot_plant` under `#minecraft:climbable` so players can freely climb them as vertical hanging ladders.
- **Fulgurite Reed Item Texture Mapping**: Consolidated `fulgurite_reed` BlockItem (`ModItems.FULGURITE_REED_ITEM`) to directly utilize the `fulgurite_stalk.png` copper-banded electric glass stalk item texture; removed duplicate `FULGURITE_STALK` item definition.
- **Blood-Root Succulent Item Fix**: Fixed item model mapping to `blood_root_succulent.png`.

### ⚡ Fulgurite Reed Overcharge Shock & Bonemeal Growth Mechanics
- **Fulgurite Reed Overcharge Shock**: Bonemealing `FulguriteReedBlock` at maximum height (3 blocks) emits a thunderclap sound, cyan lightning sparkle particle burst, inflicts 3.0 lightning damage, and applies **Paralyzed** (`ModEffects.PARALYZED`) for 3 seconds to entities within a 4-block radius, consuming the Bonemeal.
- **Flora Bonemeal Growth & Replication**: Bonemealing 1-tall flora variants with a 2-tall counterpart (`aegis_spire_orchid`) grows them into 2-tall variants (`tall_aegis_spire_orchid`). Bonemealing single-tall or 2-tall flora drops 1x item copy into the world.

### 🌟 Particle Transparency & Block Model Cutout Render Types
- **Custom Spectrum Sparkle Texture (`spectrum_sparkle.png`)**: Generated 16x16 alpha-blended sparkle star texture (`textures/particle/spectrum_sparkle.png`) and updated `particles/spectrum_sparkle.json` to `"entropica:spectrum_sparkle"`. Eliminates all solid black square particle artifacts.
- **Flora Model Cutout Render Types**: Added `"render_type": "minecraft:cutout"` across all 46 flora block model JSON definitions in `models/block/`.

### 🌸 Shimmerpetal / Shimmerblossom Ambient Essence Tinting
- **Dynamic Ambient Essence Tinting**: Created `AmbientEssenceTintRegistry` to dynamically calculate ambient Materia colors based on biome affinity (Nether, Void, Frozen, Water, Arid, Undead, Air) or continuous time-animated rainbow pastel shimmer wave.
- **Model Tint Index Correction**: Updated `models/block/shimmerpetal.json` to assign `tintindex: 0` exclusively to the `#head` blossom faces (leaving `#stem` natural green), and registered `entropica:ambient_essence_tint` ItemTintSource in `models/item/shimmerpetal.json` for item rendering.

### 🌿 100% 32x32 High-Resolution Flora Texture Milestone
- **Complete 32x32 Flora Coverage**: Re-rendered all remaining 16x16 flora textures (`soul_flame_orchid.png`, `auroral_buttercup.png`, `stardust_bell_top.png`, `stardust_bell_bottom.png`, `vitreous_cactus.png`) into 32x32 high-resolution botanical pixel art with organic dither noise ($\pm 12$), specular highlights, and rich color depth. 100% of all 24 flora block textures in Entropica now feature unified 32x32 resolution.
- **Soul-Flame Orchid Mechanics**: Updated `SoulFlameOrchidBlock.java` with `BonemealableBlock` item duplication and added Blackstone to valid Nether substrate placement rules.

### 📚 Codex Category Registry & OKF Obsidian Vault Synchronization
- **Codex Registration**: Updated `CodexCategoryRegistry.java` node entries (`env_abyssal_weeproot`, `env_fulgurite_reed`, etc.) to document climbable ladder mechanics, overcharge shock, and bonemeal behavior.
- **OKF Obsidian Vault Sync**: Updated notes in `C:\Users\Ddraig__\Downloads\OBSIDIAN WIKIS\Entropica\Entropica\` (`Aegis-Spire Orchid.md`, `Abyssal Weeproot.md`, `Fulgurite Reed.md`).

### 🌵 Vitreous Cactus & Rendering Fixes
- **Vitreous Cactus Solid Collision**: Overrode `getCollisionShape` in `VitreousCactusBlock.java` (`Block.box(1.0, 0.0, 1.0, 15.0, 15.0, 15.0)`), establishing solid physical collision for cacti and ensuring entities touch/collide with the cactus to trigger damage and bleeding debuffs.
- **In-World Flora Transparency (No Black Backgrounds)**: Registered all 25+ plant, flower, sapling, and leaf blocks on `RenderType.cutout()` in `EntropicaClientFabric.java` (including `vitae_orchid`, `spore_burst_puffball`, `fulgurite_reed`, `gale_thistle`, `mist_veil_marshmallow`, `fulgurite_swamp_bloom`, `gale_bloom_dandelion`, `shimmerpetal`, `aegis_rose`, `soul_flame_orchid`, `auroral_buttercup`, `stardust_bell`, `rimebloom`, `rubber_sapling`, `silver_pine_sapling`, etc.), removing black boxes around plant textures.
- **Particle Emissive Light & Color Fix**: Overrode `getLightColor(float partialTick)` to return `240` (full 15/15 emissive brightness) in `SpectrumSparkleParticle.java` and `GaleSwirlPuffParticle.java`. Fixed RGB initialization check so un-tinted particles render white at full brightness instead of pitch black.

### 🐛 Duplicate Item Registration Fix
- **Duplicate Registration Resolution**: Identified root-cause crash `java.lang.NullPointerException: Registry Object not present: entropica:spectral_dye_vitae`. `spectral_dye_vitae` was manually registered in `ModItems.java` (line 621) AND dynamically registered in `registerSpectralDyes()` loop, creating a duplicate unbound entry in `ModItems.ITEMS.getEntries()`. Removed manual duplicate call and assigned `SPECTRAL_DYE_VITAE = SPECTRAL_DYES.get("vitae")`.
- **Architectury `isBound()` Check**: Added `isBound()` guard to `ModItems.ITEMS` and `ModBlocks.BLOCKS` iteration in `ModCreativeTabs.java`, adhering to standard Architectury registry supplier contract patterns.

### 🐛 Root-Cause Registry & Creative Tab Cleanup
- **Stair Block Registration Fix**: Resolved premature `.get()` evaluation in `AestheticGlassRegistry.java` line 219 (`blockSup.get().defaultBlockState()`), passing static `Blocks.GLASS.defaultBlockState()` during block registration to eliminate uninitialized supplier crashes when loading glass stair families.
- **Clean Creative Tab Codebase**: Refactored `ModCreativeTabs.java` to use standard, direct `output.accept(ModItems.XYZ.get())` calls, removing intermediate `acceptSafe` wrapper functions while retaining full item registration across all creative tabs.

### 🐛 Creative Menu Crash & Item Asset Fixes
- **Minecraft 1.21.10 Item Definition Registry**: Generated 64 missing 1.21.10 item asset JSON files in `assets/entropica/items/` (including `rubber_log.json`, `rubber_wood.json`, `rubber_planks.json`, `rubber_leaves.json`, `rubber_sapling.json`, `stripped_rubber_log.json`, `stripped_rubber_wood.json`, `silver_pine_log.json`, `silver_pine_wood.json`, `silver_pine_planks.json`, `silver_pine_leaves.json`, `silver_pine_sapling.json`, `stripped_silver_pine_log.json`, `stripped_silver_pine_wood.json`, `vitae_petal.json`, `fulgurite_stalk.json`, etc.), restoring item rendering in inventory and creative tabs.
- **Creative Tab Null Protection**: Updated `ModCreativeTabs.java` with fail-safe `acceptSafe()` wrapper methods across all tabs (`WORLD_TAB`, `ITEMS_TAB`, `BLOCKS_TAB`, `TOOLS_TAB`, `WEAPONS_TAB`, `LOGISTICS_TAB`, `WEAPON_CRAFTING_TAB`, `AESTHETICA_TAB`, `MATERIA_ITEMS_TAB`), preventing `NullPointerException` crashes when opening the creative tab.
- **World Creative Tab Registration**: Added all 5 Round 2 flora block items (`vitae_orchid`, `spore_burst_puffball`, `fulgurite_reed`, `gale_thistle`, `mist_veil_marshmallow`) and harvested collectibles (`vitae_petal`, `spore_puff`, `fulgurite_stalk`, `gale_seed`, `mist_veil_marshmallow_pod`) to `WORLD_TAB`.

### 🌾 Round 2 Flora & Harvested Collectible Refinements
- **Vitae Petal (`vitae_petal.png`)**: Implemented Option 2N Rounded Tip Ribbon Strap 16x16 item texture with royal purple stem base (`#9333EA`) and gold center vein line (`#F59E0B`).
- **Vitae Orchid Silk Touch Protection**: Updated `vitae_orchid.json` loot table to drop 2x `vitae_petal` on normal break (preventing 2-tall block duplication) and 1x `vitae_orchid` block item only with Silk Touch.
- **Glass Bottle Nectar Harvesting System**: Implemented `HAS_NECTAR` blockstate property in `VitaeOrchidBlock.java`. Right-clicking with a Glass Bottle (`Items.GLASS_BOTTLE`) fills a custom **Vitae Nectar Potion** (`ModPotions.VITAE_NECTAR` - Regeneration I + Health Boost I) using native Minecraft potion bottle graphics, starting a random-tick nectar regeneration timer.
- **Fulgurite Stalk (`fulgurite_stalk.png`)**: Extracted exact 1-to-1 pixel layout of user's Sugar Cane image, recolored to Fulgurite Reed's Electric Cyan (`#06B6D4`) & Metallic Copper (`#D97706`) palette with a perpendicular metallic copper binding band and organic texture noise.
- **Spectral Dye Tint Alignment**: Aligned `VITAE` (`#FF6B9D` - Vitae Pink) and `SANGUINE` (`#8A0303` - Sanguine Crimson) dye colors in `SpectralDyeApi.java` with `EssenceType` color definitions.
- **Approved Item Drop Set**: Confirmed handcrafted 16x16 textures for `spore_puff.png` (Spore-Burst Puffball), `gale_seed.png` (Gale-Thistle), and `mist_veil_marshmallow_pod.png` (Mist-Veil Marshmallow).

### 🌿 Batch 2 Flora Implementation (5 Species)
- **Vitae Orchid (`vitae_orchid`)**: Implemented 2-tall flower block (`DoublePlantBlock` / `VitaeOrchidBlock`) with silky white orchid top blossom, royal purple lips, gold veining, 32x32 Voronoi/Perlin noise textures (`vitae_orchid_top.png` & `vitae_orchid_bottom.png`), Regeneration I aura (80 ticks), Light Level 8, and golden vitality sparkles (`#F59E0B`).
- **Spore-Burst Puffball (`spore_burst_puffball`)**: Implemented round cream puffball mushroom (`SporeBurstPuffballBlock`) with bioluminescent emerald green cracks (`#10B981`), procedural Voronoi cell crackle & Perlin skin noise, squishing burst audio on step/right-click/break, releasing 6-second Nausea I & Poison I spore clouds.
- **Fulgurite Reed (`fulgurite_reed`)**: Implemented 16x16 sugarcane-straight reed (`FulguriteReedBlock`) banded in metallic copper rings, filling full vertical height (`y=0` to `y=15`), water-adjacent riverbank placement, Speed I aura on contact, Light Level 6, and electric cyan lightning sparks (`#06B6D4`).
- **Gale-Thistle Bush (`gale_thistle`)**: Implemented dense metallic silver spiky bush (`GaleThistleBlock`) with central golden seed heads, Voronoi cell leaf spikes, 1.0 physical prick damage on collision, and swirling `GALE_SWIRL_PUFF` wind particles.
- **Mist-Veil Marshmallow (`mist_veil_marshmallow`)**: Implemented soft bouncy marshmallow pods (`MistVeilMarshmallowBlock`) growing on water-floating teal leaves, connected directly to bottom canvas edge (`y=31`), water/mud/clay placement, fall distance reset bounce mechanics, and right-click harvesting of **Mist-Veil Marshmallow Pods** (restores 4 Hunger & 6 Saturation).
- **Golden Vitae Amber Spectral Dye (`spectral_dye_vitae`)**: Registered 15th Spectral Dye (`#FF6B9D` - Vitae Pink) in `SpectralDyeApi.java` and `ModItems.java`, utilizing the standard 2-layer dye bottle model (`spectral_dye_bottle` + `spectral_dye_fill`) dynamically tinted to match `EssenceType.VITAE` (`0xFF6B9D`).

### 🌊 Subterranean Ceiling Vine & Abyssal Mechanics
- **Abyssal Weeproot (`abyssal_weeproot` & `abyssal_weeproot_plant`)**: Implemented deep navy subterranean ceiling root vine (`GrowingPlantHeadBlock` / `GrowingPlantBodyBlock`) attaching to ceiling stone, random ticking downward growth up to 26 blocks, **Slow Falling I** void tether mechanics (`entityInside`), and right-click tendril harvesting (`useWithoutItem`).
- **Abyssal Tendril (`abyssal_tendril`)**: Added 16x16 bioluminescent cyan item drop harvested from Weeproot vines.
- **Blood-Root Succulent (`blood_root_succulent`)**: Implemented deep crimson desert succulent (`BloodRootSucculentBlock`) plantable on Red Sand, Terracotta, Crimson Nylium, and Netherrack, with 1 life-drain damage on living entities (`stepOn`), Regeneration I nourishment for undead mobs, pulp harvesting, and bioluminescent crimson life sparkles.
- **Blood-Root Pulp (`blood_root_pulp`)**: Added 16x16 fleshy crimson item drop harvested from succulents.
- **Sanguine Spectral Dye (`spectral_dye_sanguine`)**: Registered 14th Spectral Dye (`#DC2626` - Sanguine Crimson) in `SpectralDyeApi.java` with 2-layer bottle model and shapeless crafting recipe.
- **Custom Bleeding Status Effect (`BLEEDING`)**: Registered custom harmful status effect `BleedingEffect.java` (`ModEffects.BLEEDING`) inflicting physical damage over time (every 30 ticks) and spawning blood droplet particles (`DAMAGE_INDICATOR`).
- **Refined Vitreous Cactus (`vitreous_cactus`)**: Updated block voxel shape and 3D JSON block model to a 12x16x12 cuboid (`Block.box(2, 0, 2, 14, 16, 14)` / `[2, 0, 2]` to `[14, 16, 14]`), applying 4 seconds of custom `BleedingEffect` on entity collision instead of Wither.

## Build 000-1-26-220

- **Entropic Codex & OKF Vault Sync**: Registered all 5 Round 3 flora nodes under `ENVIRONMENT & NATURE` in `CodexCategoryRegistry.java` and created corresponding wiki notes in the Obsidian Vault.

### 🌿 Botanical Flora Expansion (12 Species)
- **Shimmerpetal (`shimmerpetal`)**: 32x32 radiant flower emitting silver Materia sparkles.
- **Rimebloom (`rimebloom`)**: 32x32 glacial flower emitting frost blue sparkles.
- **Aegis Rose (`aegis_rose`)**: 32x32 protective rose dropping Aegis Crystal Petals.
- **Amber Nectar Blossom (`amber_nectar_blossom`)**: 32x32 golden flower emitting warm amber nectar droplets.
- **Soul-Flame Orchid (`soul_flame_orchid`)**: Nether-adapted orchid plantable on Soul Sand/Soul Soil emitting soulfire particles.
- **Auroral Buttercup (`auroral_buttercup`)**: 16x16 pastel purple flower emitting auroral shimmer particles.
- **Stardust Bell (`stardust_bell`)**: 2-tall flower block with upper/lower stem textures emitting astral indigo particles.
- **Fulgurite Swamp-Bloom (`fulgurite_swamp_bloom`)**: 32x32 underwater mud flower plantable in water on mud/clay emitting electric lightning sparkles.
- **Gale-Bloom Dandelion (`gale_bloom_dandelion`)**: 32x32 Voronoi cell + Perlin noise warm sand dandelion, emitting spinning `GALE_SWIRL_PUFF` wind particles and dropping Gale Popped Spores.
- **Cryo-Static Shrub (`cryo_static_shrub`)**: Topiary orb cross plant (`minecraft:block/cross`), deeply mixed Cryo (Glacial Cyan) & Static (Plasma Violet) leaves, Glacial Freezing ticks + Static Slowness I stun, dropping Cryo-Static Rootlings.
- **Vitreous Cactus (`vitreous_cactus`)**: Full 32x32 cuboid block (`minecraft:block/cube`), 5-high random ticking growth (`AGE_15`), horizontal solid neighbor block restrictions, cactus puncture damage + 4-second Bleeding (Wither), dropping Vitreous Needles and Vitreous Cactus Flesh.
- **Barrow Moss (`barrow_moss` & `barrow_moss_carpet`)**: Full moss block and thin carpet layer, 1 damage trade-off before clearing Wither/Poison/Weakness, 15-second regeneration cooldown (300 ticks), Bone Meal 3x3x3 spreading onto stone, and granting **Strength I & Speed I** to Undead Mobs.

### 💨 Animated Particle Systems
- **Gale Swirl Puff (`GALE_SWIRL_PUFF`)**: Created `GaleSwirlPuffParticle.java` implementing continuous rotational quad spinning (`roll += rotSpeed`), size expansion, buoyancy, and alpha fade-out across Fabric (`EntropicaClientFabric.java`) and NeoForge (`ModClientEvents.java`).

### 🧪 Spectral Dye System Expansion (10 Dye Colors)
- **Dynamic Spectral Dyes**: Updated `SpectralDyeApi.java` registering 10 bioluminescent dyes (`soulfire`, `aegis`, `amber`, `shimmer`, `frost`, `auroral`, `stardust`, `fulgurite`, `gale`, `cryo_static`, `vitreous`, `barrow`).
- **2-Layer Dynamic Tinting**: Rendered authentic glass bottle frame (`spectral_dye_bottle.png`) and liquid fill mask (`spectral_dye_fill.png`) tinted by `SpectralDyeTint`. Added 10 shapeless crafting recipes in `recipe/`.

### 📚 Entropic Codex Integration
- **Entropic Codex**: Registered 12 dedicated research sub-nodes under `ENVIRONMENT & NATURE` in `CodexCategoryRegistry.java` and updated the `materials_spectral_dyes` entry.

## Build 000-1-26-218

### Entropic Codex — Non-Technical Restructuring & Section Banners

* **Structured Field Guide Format**:
  - Restructured all codex research articles across all 7 categories into non-technical, readable field guide sections:
    1. **✦ Overview & Description**: Evocative visual and functional summary of looks and purpose.
    2. **🗺 Origin & Obtaining**: Clear locations for finding natural spawns, mob drops, geodes, or extractions.
    3. **🛠 Crafting & Synthesis**: Ritual recipes, circle tiers, runes, and inputs.
    4. **⚙ Crafting Uses & Applications**: Machine components, multiblocks, and transmutations created with the item.
    5. **✨ Special Properties**: Passive player auras, Essence injection into ritual bowls, color shifting, and spell mechanics.

* **UI Header Banners**:
  - Updated `EntropicCodexScreen` book reader to render section headers with colored section banners and icons (`✦ Overview`, `🗺 Origin`, `🛠 Crafting`, `⚙ Uses`, `✨ Special Properties`).

### Entropic Codex — Book-Like Article Reading, Index Directory & Clean Spatial Grid

* **Book-Like Codex View (`BOOK_CATEGORY`)**:
  - Clicking any category tab on the edge of the screen now opens a book-like codex view displaying category articles and sub-nodes with an article selector sidebar, title header, icons, category tags, and scissor-clipped scrollable reading text.

* **Top & Bottom Edge Tabs**:
  - Added **Top Tab: "Spatial Grid"** (Nether Star icon): Switches back to the full-screen spatial star map view with pan and zoom.
  - Added **Bottom Tab: "Index & Search"** (Knowledge Book icon): Opens the master Codex Index page featuring live search and a searchable directory of all research nodes.

* **Clean Spatial Grid & Relocated Search**:
  - Removed the left navigation controls box from the spatial node page to maximize screen space for the celestial map.
  - Moved the search bar from the spatial grid to the top of the **Index Page**, allowing live searching of all 32+ research entries. Clicking any index search result immediately opens its book article.

### Gebo Alternative Magic Circle Ritual Recipes (10% Materia Discount)

* **Materia Blessing Gebo Ritual (`data/entropica/recipe/materia_blessing_gebo.json`)**:
  - Registered alternative Tier 2 Magic Circle ritual using Rune of Gift (`entropica:rune_gebo`), 4x Materia Blessing Shards, and **180 Materia** (10% discount from 200). Outputs 1x Materia Blessing.

* **Greater Materia Blessing Gebo Ritual (`data/entropica/recipe/greater_materia_blessing_gebo.json`)**:
  - Registered alternative Tier 2 Magic Circle ritual using Rune of Gift (`entropica:rune_gebo`), 7x Materia Blessing Shards, and **360 Materia** (10% discount from 400). Outputs 1x Greater Materia Blessing.

* **Entropic Codex Documentation**:
  - Updated `env_materia_blessing` and `env_greater_materia_blessing` nodes in `CodexCategoryRegistry.java` documenting both Eihwaz (standard) and Gebo (harmonic 10% discount) ritual options.

### Tier 2 Magic Circle Ritual Recipes — Materia Blessing & Greater Materia Blessing

* **Materia Blessing Magic Circle Recipe (`data/entropica/recipe/materia_blessing.json`)**:
  - Created Tier 2 Magic Circle ritual requiring 4x Materia Blessing Shards (`entropica:materia_blessing_shard`), 1x Rune of Unity (`entropica:rune_eihwaz`), and 200 Materia of any type (`"regular": 200`). Outputs 1x Materia Blessing.

* **Greater Materia Blessing Magic Circle Recipe (`data/entropica/recipe/greater_materia_blessing.json`)**:
  - Created Tier 2 Magic Circle ritual requiring 7x Materia Blessing Shards (`entropica:materia_blessing_shard`), 1x Rune of Unity (`entropica:rune_eihwaz`), and 400 Materia of any type (`"regular": 400`). Outputs 1x Greater Materia Blessing.

* **Entropic Codex Documentation**:
  - Updated `env_materia_blessing` and `env_greater_materia_blessing` nodes in `CodexCategoryRegistry.java` with the new ritual crafting specifications.

### Materia Blessing — Inventory Color Tinting & Dynamic Shard Tint Source

* **Materia Blessing Item Model Tinting (`models/block/materia_blessing.json` & `models/block/greater_materia_blessing.json`)**:
  - Added `"tintindex": 0` to all element face definitions in both `materia_blessing.json` and `greater_materia_blessing.json`.
  - Added `"tints": [ { "type": "entropica:shard_tint" } ]` to `items/materia_blessing.json` and `items/greater_materia_blessing.json`.
  - The inventory slot, hotbar icon, held hands, ground entity, and item frame now dynamically sample and render the vibrant, glowing `EssenceType` RGB colors of the attuned crystal instead of plain un-tinted gray cuboids.

### Creative Tab "Entropica: World" (`WORLD_TAB`) — Strict Natural Block Filtering

* **Creative Tab Sorting Fix (`ModCreativeTabs.java`)**:
  - Fixed `isWorldItem` matching rule: replaced broad `path.contains("ore")` substring match (which accidentally matched machine cores `entropic_core`, `monad_core`, `athanor_core`) with strict `path.endsWith("_ore")`.
  - Added explicit exclusion `if (path.contains("core") || path.contains("glass") || path.contains("machine") || path.contains("pipe")) return false;` to guarantee machine cores and glass blocks never enter `WORLD_TAB`.
  - Explicitly registered only natural world blocks (Materia Blessings, Ores, Aeterium/Ignisite/Mortisite clusters & buds) and fauna mob spawn eggs in `WORLD_TAB`.
  - Restored machine cores (`entropic_core`, `monad_core`, `athanor_core`) to `BLOCKS_TAB` where all structural/machinery blocks reside.

### Materia Blessing — 3D Inventory Block Model & Essence Node Lore

* **3D Inventory Crystal Gem Model (`models/item/materia_blessing.json`)**:
  - Updated `models/item/materia_blessing.json` to parent `"entropica:block/materia_blessing"`.
  - Inventory, hotbar, hands, ground, and item frames now render the full 3D faceted crystal block model matching `greater_materia_blessing`.

* **Entropic Codex Lore Updates**:
  - Updated `env_materia_blessing` and `env_greater_materia_blessing` nodes in `CodexCategoryRegistry.java`.
  - Replaced legacy Entropic Core terminology with **Essence Node** as the primary attunement source, noting that if an Entropic Core is present nearby, the blessing crystal may resonate with it and display its colors depending on which energy source is denser / has higher capacity.

### Entropic Codex — Minimum Node Distance & Screen Edge Category Selection Tabs

* **Force Relaxation Node Layout Solver (`calculateNodePositions`)**:
  - Implemented an iterative force relaxation layout algorithm in `EntropicCodexScreen.java` enforcing strict minimum Euclidean separation distances (95 world units for Parent Hubs, 75 world units for standard nodes).
  - Automatically resolves node overlaps, line intersections, and label collisions across all sub-nodes branching dynamically from the 7 parent category hubs.

* **Screen Edge Category Selection Tabs (`renderEdgeCategoryTabs`)**:
  - Added a vertical array of 7 category selection tabs along the left screen edge below the header banner for all parent hubs (`GETTING STARTED`, `MATERIALS`, `MATERIA`, `MACHINERY`, `MULTIBLOCKS`, `ENVIRONMENT & NATURE`, `MAGIC`).
  - Sleek collapsible/expandable design (width `28px` collapsed, `145px` expanded on hover or active selection), displaying the category's colored accent bar, item icon, and title text.
  - Clicking any edge tab smoothly glides the camera (`targetPanX`, `targetPanY`, `targetZoom = 0.60f`) to center directly on that category's parent hub in World Space and opens its details panel.

## Build 000-1-26-217

### Materia Blessing Crystal — Renderer Reshape + Greater Variant

* **Renderer Overhaul**:
  - Replaced 9-spire cluster geometry with a single large **monolithic faceted crystal** silhouette.
  - Ring-based hexagonal geometry: 12 vertical rings defining the crystal profile (bottom point → wide mid-section → sharp apex).
  - Two-texture rendering system:
    - **Crystal face texture** (32×32 grayscale, `lesser/greater_materia_blessing_crystal.png`): UV-mapped per face with random UV rotation, tinted with `EssenceType` RGB via vertex color.
    - **Crack overlay texture** (32×32 grayscale, `lesser/greater_materia_blessing_cracks.png`): Rendered as a second cutout pass, tinted 35% darker than the EssenceType RGB.
  - Solid opaque rendering (`RenderType.entitySolid`) — no see-through. Full-bright emissive.
  - Per-face brightness variation for iridescent depth effect.
  - Slight 5°/3° X/Z tilt for natural, non-artificial appearance.

* **3D Inventory Block Models**:
  - Created full 3D element JSON block models for `materia_blessing.json` (tapered 5-cuboid crystal shape) and `greater_materia_blessing.json` (central crystal + 2 branching satellite crystals) with display transforms for GUI, hotbar, hands, ground, and item frames.

* **Branching Procedural Crystal Cluster (Greater Variant)**:
  - Procedural in-world renderer `MateriaBlessingRenderer.java` for Greater variant now renders 3 branching satellite crystal spires at tilted angles around the base of the main central crystal, sharing its rotation.

* **Glass Block Translation Keys (1,139 Keys Added)**:
  - Added full translation keys for `essence_enriched_glass`, `materia_fumus_strengthened_glass`, `materia_liquida_enriched_glass`, `fragment_lattice_glass`, clear glass, vanilla dyes, dyenamics colors, and all 75 essence types across all 10 shape variants (Full Blocks, Panes, Doors, Trapdoors, Slabs, Vertical Slabs, Quarter Slabs, Stairs, Horizontal Panes) across all 9 language files.

* **New Creative Tab: "Entropica: World" (`WORLD_TAB`)**:
  - Registered `WORLD_TAB` in `ModCreativeTabs.java` (`itemGroup.entropica.world`).
  - Houses all naturally occurring blocks (Materia Blessings, Ores, Aeterium / Ignisite / Mortisite clusters & buds) and fauna mob spawn eggs.

* **Indigenous Fauna Mob Spawn Eggs**:
  - Registered 8 spawn egg items in `ModItems.java` (`grot_spawn_egg`, `veil_fox_spawn_egg`, `ashen_stalker_spawn_egg`, `spore_drifter_spawn_egg`, `rime_back_ovis_spawn_egg`, `overgrowth_ovis_spawn_egg`, `rime_shepherd_spawn_egg`, `bloom_crawler_spawn_egg`).
  - Generated custom 32×32 pixel art textures, item model JSONs, and localized translations across all 9 language files.

* **Full Localization Audit & Translation Coverage**:
  - Performed a complete codebase audit across all 352 registered items and 136 registered blocks in `ModItems.java` and `ModBlocks.java`.
  - Added 51 missing item and block translation keys (including `materia_blessing`, `greater_materia_blessing`, `materia_blessing_shard`, ritual bowls, essences, ingots, buds, clusters, and ores) across all 9 supported language files (`en_us`, `de_de`, `es_es`, `fr_fr`, `ja_jp`, `ko_kr`, `pt_br`, `ru_ru`, `zh_cn`), achieving **100% translation coverage (0 missing keys)**.

* **Entropic Codex Registrations**:
  - Registered 3 new research nodes in `CodexCategoryRegistry.java`:
    - `env_materia_blessing` ("Materia Blessing Crystal") under `ENVIRONMENT & NATURE` (prerequisite: `env_biomes`).
    - `env_greater_materia_blessing` ("Greater Materia Blessing") under `ENVIRONMENT & NATURE` (prerequisite: `env_materia_blessing`).
    - `materials_blessing_shard` ("Materia Blessing Shard") under `MATERIALS` (prerequisite: `materials_crystals`).

* **Grayscale Shard Texture & ShardTint**:
  - Converted `materia_blessing_shard.png` to a 32×32 grayscale PNG.
  - Implemented `ShardTint` in `ModItemTintSources.java` registered under `entropica:shard_tint`. Dynamically tints the item in inventory/hand based on nearest Entropic Core node (24-block radius) or local biome `EssenceType`.

* **Node Attunement & Client Sync**:
  - Added Entropic Core node detection (64-block radius search): nearest node's dominant `EssenceType` takes precedence over biome attunement.
  - Implemented S2C block entity packet sync (`getUpdateTag`, `getUpdatePacket`, `sendBlockUpdated`) so RGB tint changes render immediately on the client.

* **Tinted Inward-Flowing Particles**:
  - Replaced generic untinted particles with `ColorParticleOption` entity effects and `DustParticleOptions` sparkling dust motes.
  - Particles are dynamically tinted with the exact RGB color of the crystal's attuned `EssenceType`.
  - Velocity vectors computed to draw particles inward from a surrounding sphere/cylinder directly towards the center of the floating crystal.

* **Dynamic Hitbox Fitting**:
  - Replaced full block hitboxes with height-aware `VoxelShape` bounds calculated per-part from the crystal's profile rings. Empty shapes applied to non-intersecting outer blocks.

* **Block Architecture Refactor**:
  - Created `AbstractMateriaBlessingBlock.java` — shared base class for multiblock logic (placement, cascading destruction, block entity management).
  - `MateriaBlessingBlock.java` now extends abstract base (2×2×3 / 12 blocks, PART 0-11).
  - Width/Height/Depth/Drops defined as abstract method overrides.

* **New Block: Greater Materia Blessing (`greater_materia_blessing`)**:
  - 3×3×4 multiblock (36 blocks), `PART` range 0-35.
  - Light level 12 (vs 8 for Lesser).
  - Destroy time 5.0, explosion resistance 10.0.
  - Drops 8-12 `materia_blessing_shard` (vs 4-6 for Lesser).
  - Rendered at 1.5× scale using the Greater texture set.

* **Enhanced Block Entity Effects (Greater variant)**:
  - Aura radius: 24 blocks (vs 16).
  - Essence injection radius: 12 blocks (vs 8).
  - Injection interval: 60 ticks (vs 100).
  - Healing: +2 HP per 40 ticks (vs +1).
  - Grants Regeneration I (100 ticks).
  - Faster rotation (0.6 vs 0.5 deg/tick).
  - Extra `ENCHANTED_HIT` particles.

* **Registrations**:
  - `GREATER_MATERIA_BLESSING` block in `ModBlocks.java`.
  - `GREATER_MATERIA_BLESSING_ITEM` in `ModItems.java`.
  - Both blocks share single `MATERIA_BLESSING_BE` block entity type.
  - Single `MateriaBlessingRenderer` handles both variants via `isGreater()` flag.

* **Textures Created (4× 32×32 grayscale PNGs)**:
  - `lesser_materia_blessing_crystal.png`, `lesser_materia_blessing_cracks.png`
  - `greater_materia_blessing_crystal.png`, `greater_materia_blessing_cracks.png`

### New Feature: Materia Blessing Crystal (Natural Phenomenon)

* **Materia Blessing Block (`materia_blessing`)**:
  - 2×2×3 multiblock natural crystal formation (12 blocks total) with `IntegerProperty PART (0-11)`.
  - Cascading destruction: breaking any 1 of the 12 blocks destroys all remaining blocks.
  - `RenderShape.INVISIBLE` — rendered entirely by procedural code renderer (no JSON model).
  - Ambient light emission capped at level 8.
  - Drops 4–6 `materia_blessing_shard` items when broken (non-creative).

* **Materia Blessing Block Entity (`MateriaBlessingBlockEntity`)**:
  - Biome-to-EssenceType ambient Materia sampling every 20 ticks (ocean→WATER, desert→ARID, forest→NATURE, etc.).
  - 16-block player aura field: Night Vision (200 ticks) + gradual heal every 40 ticks.
  - 8-block automated essence injection into nearby Granite Ritual Bowls every 100 ticks (5 seconds).
  - Client-side: continuous Y-axis rotation, vertical bobbing, ambient END_ROD + ENCHANT particles.

* **Materia Blessing Renderer (`MateriaBlessingRenderer`)**:
  - Procedural code-rendered crystal cluster with 9 spires (main + 8 secondary/tertiary/accent).
  - Hexagonal cross-sections (6 sides) with 8 vertical segments per spire.
  - Voronoi cell fracture vein lines via hash-based edge distance detection.
  - Simplex noise vertex displacement for organic crystal surface feel.
  - Dynamic `EssenceType.getCurrentRGB(time)` color cycling.
  - Full-bright emissive rendering (`RenderType.entityTranslucentEmissive`, `FULL_BRIGHT`).
  - 128-block view distance.
  - Floating, bobbing, and rotating animation driven by block entity state.

* **Registrations**:
  - `MATERIA_BLESSING` block registered in `ModBlocks.java`.
  - `MATERIA_BLESSING_ITEM` (BlockItem) and `MATERIA_BLESSING_SHARD` (standalone Item) registered in `ModItems.java`.
  - `MATERIA_BLESSING_BE` block entity registered in `ModBlockEntities.java`.
  - `MateriaBlessingRenderer` registered in Fabric (`EntropicaClientFabric.java`), NeoForge, and Forge client events.

* **Assets Created**:
  - `blockstates/materia_blessing.json`, `models/block/materia_blessing.json` (particle-only minimal model).
  - `models/item/materia_blessing.json`, `models/item/materia_blessing_shard.json`.
  - `items/materia_blessing.json`, `items/materia_blessing_shard.json` (1.21.4 item definitions).

### New Feature: Granite Ritual Bowl (Passive Essence Extractor & Essence Supplier)

* **Granite Ritual Bowl (`granite_ritual_bowl`)**:
  - Registered `GRANITE_RITUAL_BOWL` in `ModBlocks.java`, `ModItems.java`, `ModBlockEntities.java`, and `ModCreativeTabs.java`.
  - Uses the same 3D Blockbench model geometry as Marble/Basalt bowls, retextured with authentic Granite map textures (`ritual_bowl_granite.png`).
* **Passive Essence Burning & Supply System**:
  - **Granite Ritual Bowls** passively dissolve items placed into slot 0 into `Weak Essence` items stored in output slots 1..4.
  - Scribed Magic Circles and Magic Circuits within 3 blocks automatically pull required essence types directly from nearby Granite Ritual Bowls, executing parabolic floating essence particle trajectories upon recipe completion!
* **Complete Symmetrical Ritual Bowl Triad**:
  - **Marble Ritual Bowl**: Holds Item Inputs.
  - **Basalt Ritual Bowl**: Holds Runes.
  - **Granite Ritual Bowl**: Extracts & Supplies Essences.

---

### Fixed & Deployed: Ritual Bowl Item Storage (Disabled Passive Essence Burning)

* **Ritual Bowl Passive Burning Fix**:
  - Removed legacy passive essence burning tick logic from [RitualBowlBlockEntity.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/entity/RitualBowlBlockEntity.java).
  - Items (like Iron Ingots in Marble Bowls) and Runes (in Basalt Bowls) placed into Ritual Bowls are now safely preserved for Magic Circle/Circuit recipes without being destroyed/burned into Weak Essence items every 5 seconds. (Essence extraction remains strictly on the Crucible).

---

### Fixed & Deployed: 1.21.4 Item Definition JSON Registrations

* **Minecraft 1.21.4 Item Definition Registration Fix**:
  - Identified missing 1.21.4 item definition files in `common/src/main/resources/assets/entropica/items/`.
  - Created missing item definition JSON files (`assets/entropica/items/<name>.json`) pointing to `entropica:item/<name>` for:
    - `arkanist_monocle.json`
    - `aetheric_lens.json`
    - `propagation_lens.json`
    - `vitae_lens.json`
    - `materia_lens.json`
    - `sacrificial_knife.json`
    - `arcane_stencil.json`
    - `blank_stone.json`
    - `crawler_shell_fragment.json`
  - Created missing item model files (`assets/entropica/models/item/<name>.json`) for `arcane_stencil`, `blank_stone`, and `crawler_shell_fragment`.
  - Audited 100% of all 333 registered items in `ModItems.java`, ensuring all item models render cleanly in-game!

---

### Refined Feature: Arkanist Monocle Arcane Flame Plume & Viscanite Gunmetal Retexture

* **Arcane Elemental Flame Plume**:
  - Redesigned the feather accent in `arkanist_monocle.png` into a dynamic **Arcane Elemental Flame** plume blending emerald green at the base (`RGB(30, 220, 100)`), shifting into vis-cyan energy (`RGB(60, 230, 255)`), and culminating in shimmering gold/amber flame tips (`RGB(255, 210, 70)`).
* **Viscanite Gunmetal Frame & Dark Leather Finish**:
  - Retextured the 3D monocle metallic frame in authentic **Viscanite Gunmetal** sampled from `viscanite_ingot.png` (`RGB(55, 47, 52)` ➔ `RGB(121, 125, 133)`), paired with dark charcoal arcane leather and polished steel pins.

---

### New Feature: Lens Textures & 3D Arkanist Monocle Retexture Suite

* **Custom Lens Textures**:
  - Created high-quality pixel art textures for all 5 lens items in `common/src/main/resources/assets/entropica/textures/item/`:
    - **`aetheric_lens.png`**: Polished cyan glass lens with silver alloy rim, specular highlights, and cyan vis focus ring.
    - **`propagation_lens.png`**: Polished magenta/purple lens with viscanite alloy rim, energy lattice, and specular highlights.
    - **`vitae_lens.png`**: Polished emerald & crimson living energy lens in organic bronze rim.
    - **`materia_lens.png`**: Polished gold & amber elemental lens in viscanite gold rim with hex lattice.
    - **`focal_lens_assembly.png`**: Brass and gold dual-aperture mechanical housing with screws and cyan lens aperture.
* **3D Arkanist Monocle Retexture & Model Upgrade**:
  - Upgraded [arkanist_monocle.json](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/models/item/arkanist_monocle.json) from 2D item generated model to full 3D Blockbench model inheriting the Blockbench geometry suite of the Aetheric Monocle.
  - Crafted `arkanist_monocle.png` (32x32) as a rich retexture featuring royal Viscanite Gold frame, Arcana Purple leather strap, polished steel hardware, and multi-frequency prismatic crystal highlights.

---

## Build 000-1-26-216

### New Feature: Automatic Ritual Bowl Ingredient Pulling & Floating Particle Trajectory

* **Automatic Ritual Bowl Proximity Integration**:
  - Magic Circles and Magic Circuits now automatically scan for nearby **Ritual Bowls** within a 3-block radius of any chalk block in the circle/network via `findNearbyRitualBowls` in [ScribedChalkBlock.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/ScribedChalkBlock.java).
  - **Marble Ritual Bowls** (`!isBasalt`): Automatically supply recipe input items (`inputStacks`).
  - **Basalt Ritual Bowls** (`isBasalt`): Automatically supply recipe runes (`runeStacks`).
* **Visual Floating Item Particle Animation**:
  - Added `spawnFloatingItemEffect` producing a parabolic arc trajectory of `END_ROD`, `ENCHANTED_HIT`, and amethyst chimes traveling from the Ritual Bowl's position to the target circle/circuit node position upon recipe initiation/consumption.

---

### New Feature: Proximity Activation Node (`NodeType.ACTIVATION`)

* **Proximity Activation Node (`NodeType.ACTIVATION`)**:
  - Added `ACTIVATION` node type (`"activation"`) to `NodeType` enum in [ScribedChalkBlock.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/ScribedChalkBlock.java).
  - Integrated `ACTIVATION` node into chalk node cycling sequence in [ChalkItem.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/item/ChalkItem.java) (`ESSENCE_BANK -> ACTIVATION -> RUNE -> DEFAULT`).
  - Added 3-block radius proximity detection (`AABB.inflate(3.0)` / `distanceToSqr <= 9.0`) in [ScribedChalkBlockEntity.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/entity/ScribedChalkBlockEntity.java), scanning for any alive `LivingEntity` (players, mobs, or animals).
  - Emits magical enchanted hit particles, powers the node line (`propagationStrength = 16`), and automatically triggers ritual/circuit processing (`processCircuitTrigger`) with a 2-second cooldown when a mob or player enters within 3 blocks.
  - Added text symbol `"V"` label rendering in [ScribedChalkRenderer.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/client/renderer/ScribedChalkRenderer.java).

---

### Fixed & Deployed (Node Essence Capacity Expansion & Rich Diagnostic System)

* **Node Essence Capacity Doubled (8 -> 16 Essence per Node)**:
  - Increased base essence holding capacity for all standard nodes and source nodes in Magic Circuits from **8 to 16 Essence per node** in [ScribedChalkBlock.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/ScribedChalkBlock.java).
  - Updated `getCircuitCapacity()` and `addEssenceToCircuit()` to calculate and fill circuit node capacity at 16 essence per node, allowing compact 4-node circuits to hold up to 64 essence.

* **Rich Recipe Candidate Diagnostic System**:
  - Implemented `getDiagnosticReport()` and `getMatchScore()` in [MagicCircleRecipe.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/recipe/MagicCircleRecipe.java).
  - Upgraded failure diagnostic messages when right-clicking output nodes or sneak-clicking circuits to display:
    - **Detected Input Items** (e.g. `1x Iron Ingot` or `None`).
    - **Detected Runes** (e.g. `Rune of Uruz` or `None`).
    - **Circuit Essences & Total** (e.g. `12 NATURE (Total: 12)`).
    - **Closest Matching Recipe Candidate**: Step-by-step checklist of satisfied inputs/runes and exact essence deficit details (e.g. `❌ Essence Deficit: Have 12/50 Essence (Need 38 more Essence)`).

* **NodeType.INPUT Scope Correction**:
  - Confirmed and restricted item scanning strictly to `NodeType.INPUT` nodes; `NodeType.COLLECTION` nodes strictly collect passive ambient essence.

---

### Fixed & Deployed (Magic Circuit Processing, Essence Detection & Glass Culling)

* **Magic Circuit Recipe Trigger & Circuit Execution**:
  - Implemented `processCircuitTrigger` in [ScribedChalkBlock.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/ScribedChalkBlock.java) for free-form Magic Circuits (`CIRCUIT=true`).
  - Added **Sneak + Right-Click** (empty hand) trigger on ANY chalk block in a magic circuit, initiating processing by tracing connected circuit nodes via BFS (`getConnectedCircuit`).
  - Added empty-hand right-click handling on circuit **OUTPUT** nodes to bypass magic circle concentric ring pattern checks (`checkPatternStatic` / `validateNodeCountsStatic`), enabling circuits of any free-form layout to execute recipes.
  - Automatically locates the circuit's OUTPUT node for ritual processing and particle output; falls back to the clicked block if no OUTPUT node exists.
  - Gathers inputs, runes, essence, amplifiers, capacitors, and resonators across the BFS-connected circuit topology, matching against tier 1..4 recipes.

* **Circle vs Circuit Essence Detection & Consumption**:
  - **Magic Circles** (`CIRCUIT=false`): Essence is gathered from the union of tier ring perimeter nodes (`allChalkBlockEntities`) and BFS-reachable nodes.
  - **Magic Circuits** (`CIRCUIT=true`): Essence detection and consumption strictly use `getConnectedCircuit()` BFS, respecting force connect/disconnect overrides, diode directionality, and logic gates.

* **Stair Culling Fix**:
  - Updated [AestheticGlassStairBlock.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/AestheticGlassStairBlock.java) to remove blanket `skipRendering` between adjacent stairs, restoring full geometry rendering for complex L-shaped stair blocks.

---

## Build 000-1-26-215

### Fixed & Deployed (HorizontalPaneBlock System, Master Base Model Architecture, Aesthetic Glass Suite & Non-Glass Restoration)

* **HorizontalPaneBlock Architecture**:
  - Implemented `HorizontalPaneBlock.java` extending `Block` and implementing `SimpleWaterloggedBlock` (replacing `VerticalPaneBlock`).
  - Defined full 16x2x2 panel VoxelShapes lying flat on the X-Z plane at height `Y=7..9` (`PANEL_X`: `[0,7,7]` to `[16,9,9]` on East-West axis; `PANEL_Z`: `[7,7,0]` to `[9,9,16]` on North-South axis) matching placement facing.
  - Re-architected connection arms to **full 16-wide panel arms** (`PANEL_ARM_NORTH`: `[0,7,0]` to `[16,9,7]`; `PANEL_ARM_SOUTH`: `[0,7,9]` to `[16,9,16]`; `PANEL_ARM_WEST`: `[0,7,0]` to `[7,9,16]`; `PANEL_ARM_EAST`: `[9,7,0]` to `[16,9,16]`), bridging adjacent horizontal panes into continuous 16-wide flat glass surfaces.
  - Implemented full **UP and DOWN vertical connection panels** (`PANEL_ARM_UP_X`: `[0,9,7]` to `[16,16,9]`; `PANEL_ARM_DOWN_X`: `[0,0,7]` to `[16,7,9]`), enabling Horizontal Panes to connect vertically upwards (`Y=9..16`) and downwards (`Y=0..7`) to adjacent panes or solid ceilings/floors to build multi-tier glass roofs, domes, and stepped structures.
  - Implemented solid block connection detection (`canConnectTo(state, sideSolid)`) checking `BlockTags.IMPERMEABLE` and `isFaceSturdy()`, ensuring horizontal panes connect flush against solid blocks and walls in all 6 directions.

* **Generic Master Base Model Architecture & Asset Optimization**:
  - Registered dedicated Creative Mode Tab **`Entropica: Aesthetica`** (`AESTHETICA_TAB`) in [ModCreativeTabs.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/registry/ModCreativeTabs.java) displaying all 1,251 aesthetic glass blocks, panes, doors, trapdoors, slabs, vertical slabs, quarter slabs, stairs, and horizontal panes.
  - Filtered all aesthetic items out of the main `Entropica: Blocks` tab (`BLOCKS_TAB`), keeping functional blocks, machines, ores, and multiblocks cleanly organized.
  - Added translation key `"itemGroup.entropica.aesthetica": "Entropica: Aesthetica"` to `en_us.json`.
  - Organized all shape base models into generic, material-agnostic master base models in `models/block/base/` (`cube_base`, `slab_base`, `slab_top_base`, `vertical_slab_base`, `quarter_slab_base`, `stairs_base`, `stairs_inner_base`, `stairs_outer_base`, `horizontal_pane_base`, `horizontal_pane_side_base`, `horizontal_pane_up_base`, `horizontal_pane_down_base`). These master models can be reused by any material block family in Entropica (wood, stone, metal, crystal, materia, glass).
  - Deleted **1,583 redundant individual glass block model JSON files** in `models/block/` and updated 710 blockstate definitions to point directly to generic master base models (`entropica:block/base/*`), dramatically reducing asset footprint.
  - Completely removed `RampBlock` (Full/Half Ramps) and `WallBlock` (Glass Walls) across all 139 glass families per design directive.
  - Updated `AestheticGlassRegistry.SHAPES_PER_FAMILY = 9` (Block, Pane, Door, Trapdoor, Slab, Vertical Slab, Quarter Slab, Stairs, Horizontal Pane; total 1,251 registered blocks & items).
  - Extended asset generation across all **87 `EssenceType` enum constants**, 16 Vanilla dyes, 28 Dyenamics colors, 7 tech glasses, and clear glass.

* **Hitboxes, Rotations & Geometry Sync**:
  - **Doors**: Re-built master door models along the North face (`z=0..3`), matching Vanilla `door_bottom.json` geometry and bringing door models 100% in sync with `DoorBlock` VoxelShape hitboxes.
  - **Trapdoors**: Fixed open trapdoor models (`glass_trapdoor_open_base`) and removed erroneous `x=90` blockstate rotations, enabling open trapdoors to render vertically flush with block faces when opened.
  - **Stairs**: Re-aligned [glass_stairs_base.json](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/models/block/glass/glass_stairs_base.json) top step (`x=8..16`) and blockstate `y` rotation angles (`east=0`, `south=90`, `west=180`, `north=270`), resolving the 90° renderer-to-hitbox offset.
  - **Vertical Slabs**: Restored double vertical slab placement in [VerticalSlabBlock.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/java/ddraig/net/entropica/block/VerticalSlabBlock.java) (`DOUBLE_NS`, `DOUBLE_EW`), allowing players to combine two vertical slabs in one block space.

* **World Join Crash & Registry Fix**:
  - Resolved `IndexOutOfBoundsException` on world join in `AestheticGlassRegistry.populateColorMaps()`.

* **Pipes, Conduits, Pipelines & Agitators Optimization**:
  - Removed `WATERLOGGED` property across all 45 pipe/conduit classes and generated clean multipart blockstates pointing to shared master model `entropica:block/vapor_pneumatic_pipe`.

* **Dynamic BlockEntity & Log Warning Cleanups**:
  - **Scribed Chalk**: Replaced explicit variant blockstate in [scribed_chalk.json](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/blockstates/scribed_chalk.json) with wildcard catch-all key `""` mapping to `minecraft:block/air`, silencing 2,048+ dynamic model log warnings.
  - **Non-Glass Assets**: Fully restored 100% of non-glass blockstates, models, machines, chalks, ores, and item models from Git HEAD.

* **Performance & JEI Log Optimization**:
  - **Dynamic Glass Client Color Ticking**: Added a client tick section dirty handler in [ModClientEvents.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/neoforge/src/main/java/ddraig/net/entropica/neoforge/client/ModClientEvents.java) every 4 ticks (~200ms) to continuously update dynamic essence glass color cycles in real-time.
  - **Glass Texture Alpha Mipmapping**: Sharpened alpha contrast on glass PNG lines to prevent distance mipmap fading.
  - **JEI Research Log Spam**: Added `CURRENTLY_HIDDEN_ITEMS` state tracking in [EntropicaJEIPlugin.java](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/neoforge/src/main/java/ddraig/net/entropica/compat/jei/neoforge/EntropicaJEIPlugin.java), preventing JEI from logging ingredient removals every 2 seconds.

* **Complete Stage 1 Custom Glass Texture System Deployment**:
  - **Approved Texture Suite (30%–40% Alpha + Subtle Micro-Noise)**: Re-built all 6 Stage 1 Essence Enriched Glass texture PNG assets (`glass_essence_enriched.png`, `glass_essence_enriched_door_bottom.png`, `glass_essence_enriched_door_top.png`, `glass_essence_enriched_trapdoor.png`, `glass_essence_enriched_edge.png`, `glass_essence_enriched_slab_edge.png`) featuring 32% glass center fill, 38% frame borders & edge profiles, 40% black iron hinges & lever handles, and subtle micro-pixel noise (`±4 RGB`, `±2 alpha`) for an authentic Minecraft pixel-art texture feel.
  - **Radial Symmetry & Black Hardware**: Integrated 4-fold radial symmetry across Cube Glass and crisp black iron hardware (hinge plates, pin barrels, escutcheons, lever handles, strap hinges, catch latches) across Doors and Trapdoors.
  - **Master Base Model Bindings**: All master base models in `models/block/base/` (`door_bottom_base`, `door_top_base`, `door_bottom_rh_base`, `door_top_rh_base`, `slab_base`, `slab_top_base`, `quarter_slab_l0..l3`, `horizontal_pane_base`, `horizontal_pane_side_base`, `horizontal_pane_noside_base`) consume their dedicated texture assets with dynamic `tintindex: 0` essence color tinting.

* **Dynamic Color-Shifting & Matching Glass Edge Culling Implementation**:
  - **Model Face `tintindex: 0` Restoration**: Re-architected `cube_base.json`, `trapdoor_bottom_base.json`, `trapdoor_top_base.json`, `trapdoor_open_base.json`, `pane_post_base.json`, `pane_side_base.json`, `pane_side_alt_base.json`, `pane_noside_base.json`, `pane_noside_alt_base.json`, and `stairs_base.json` to explicitly include `"tintindex": 0` on every quad face. Blocks, double slabs, trapdoors, stairs, and vertical panes now dynamically calculate animated time-based RGB color shifting when using dynamic essence types.
  - **Exact Same-Block Face Culling**: Restructured `skipRendering` across all shape block classes (`AestheticGlassBlock`, `AestheticGlassSlabBlock`, `AestheticGlassPaneBlock`, `AestheticGlassDoorBlock`, `AestheticGlassTrapdoorBlock`, `AestheticGlassStairBlock`, `QuarterSlabBlock`, `HorizontalPaneBlock`, `VerticalSlabBlock`) to enforce strict `adjacentState.is(state.getBlock())` culling. Interior faces are now culled strictly when placed next to identical glass blocks of the same type.
  - **Door 180° Open Rotation & Stair Alignment Fix**: Re-aligned `door_bottom_base`, `door_bottom_rh_base`, `door_top_base`, `door_top_rh_base` along the Z-axis (`[0,0,0]` to `[3,16,16]` and `[13,0,0]` to `[16,16,16]`), resolving the 180° open rotation offset. Re-architected `stairs_base`, `stairs_inner_base`, `stairs_outer_base` facing East at `y=0` to match 100% of voxel shape hitboxes across all 16 rotations.
  - **Vertical Pane 2-Pixel Center Gap & Quarter Slab Stacking Culling**: Fixed UV mapping in `pane_side_base` (`Z=0..8`) and `pane_side_alt_base` (`Z=8..16`), eliminating the 2-pixel vertical gap down connected panes. Implemented bitmask layer culling in `QuarterSlabBlock.skipRendering`, eliminating interior quad seams when stacked vertically or connected horizontally.

* **Build & Deployment Verification**:
  - Successfully built and deployed to dev instances via `./gradlew deploytoDev`.

## Build 000-1-26-214

### Fixed & Deployed (Entropic Codex UI, Viewport Frustum Culling, Line Batching, Item Textures)

* **Entropic Codex UI & Render Performance**:
  - **Viewport Frustum Culling (`isNodeInViewport`)**: Calculates screen-projected bounds for all research nodes and connecting lines, skipping draw execution for off-screen elements during panning.
  - **12px Line Segment Batching (`drawLineWorld`)**: Batches linear quad fills into 12-pixel strides, reducing line draw calls by ~85%.
  - **Single 32x32 Alpha Mask & Background**: Created [entropic_codex_item.png](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/textures/item/entropic_codex_item.png) with precise pixel-art outline and pure 100% transparent background.
  - **Black Hole Background Seamless Expansion**: Created [codex_black_hole_bg.png](file:///c:/Users/Ddraig__/Downloads/MODS_CREATION/Entropica/common/src/main/resources/assets/entropica/textures/gui/codex_black_hole_bg.png) with edge vignette fading to pitch black (`#000000`), preventing hard edge clipping in 16:9 and ultrawide viewports.

## Build 000-1-26-213

### Added & Implemented (Phase 1: The Entropic Codex)
* **Entropic Codex Item & Crafting**:
  - Registered `entropica:entropic_codex` item (`Rare` rarity) with custom tooltip and right-click interactions.
  - Implemented Shapeless crafting recipe (`minecraft:book` + Any Essence Orb T1–T6).
  - Implemented auto-give event (`CodexJoinHandler`) granting 1x Entropic Codex on first world join.
  - Added right-click pairing to `Materia Terminal` blocks with particle and audio feedback.
* **16-Bit Pixel Art Frutiger Metro Draggable Canvas Screen**:
  - Created full-screen 2D spatial canvas (`EntropicCodexScreen`) with mouse panning and scroll zooming.
  - Rendered central animated 16-bit Black Hole & Accretion Disk with cosmic particle distortion.
  - Implemented orbiting Research Category Nodes along concentric rings.
  - Implemented smooth camera lerp focusing directly onto clicked nodes.
  - Built slide-out 16-bit Frutiger Metro glass reading panel with strict scissor clipping (`guiGraphics.enableScissor`) to prevent text bleed.
  - Added dynamic central gem header rendering matching inventory's highest-tier Essence Orb.
* **Interactive Tools & Blueprints**:
  - **Fusion Simulator Tab**: Built-in *Reaction Yield* sub-tab (fluid volume yield & stability %) and *Damage Scaling* sub-tab (Additive Resonance vs target mob affinity).
  - **Network Diagnostics Tab**: Displays remote telemetry for paired Materia Terminals (pressure bars, volume levels, red alert leak coordinates).
  - **2D Multiblock Blueprints Widget**: Layer-by-layer 2D grid rendering (Y=1, Y=2, Y=3) for multiblocks.
* **Progression, Observation & Restrictions**:
  - Implemented `MobObservationHandler` (partial silhouette unlock on looking at unknown mobs; full unlock on kill/tame) with HUD toast notifications.
  - Added `REQUIRE_RESEARCH_TO_CRAFT` config toggle in `EntropicaConfig` across common, fabric, and neoforge modules.
  - Intercepted crafting grid events to set output to `ItemStack.EMPTY` for unresearched items when research restriction is enabled.

## Build 000-1-26-210

### Fixed & Remediated
- Remediated all progression tier claims ("Tier 0/1/2/3", "10 chalk tiers") across `Essence Orb`, `Small Ampoule`, `Medium Ampoule`, `Large Ampoule`, and `Runic Arts & Scribing Index`.
- Refactored terminology non-compliance across 16 files, replacing legacy `liquid essence` with `Materia Liquida` and generic `fumes`/`void fumes` with `Materia Fumus`.

### Added
- Created missing entity note `Materia Filter` (`wiki/entities/blocks/logistics/Materia Filter.md`), resolving all broken `[[Materia Filter]]` wikilinks.

## Build 000-1-26-202

### Added & Polished
*   **Visual Overlays Restructured**:
    *   Restricted the shifting soap-scum/oil-slick iridescence screen overlay to ONLY active Aetheric Lens and Propagation Lens. The Aetheric Monocle, Vitae Lens, and Materia Lens no longer render this screen effect.
*   **Horizontal Runes Halo**:
    *   Corrected the rotating runes halo (floating text overlay) to always lie flat horizontally (`pitch = 90.0f`) around the nodes/spheres at all times instead of standing vertically.
    *   Removed all floating node text/runes when the circle is formed and idle, and when the circle is not formed. They now only render during active ritual processing.
*   **Aetheric Lens Nodes Visibility**:
    *   Implemented server-friendly `isAethericVisionActiveCommon` and `isLensActiveCommon` in `ArkanistMonocleItem`.
    *   Updated `EssenceNodeRenderer` (client) and `EssenceNodeEntity` (placement protection check) to use the new common helper, allowing the slotted **Aetheric Lens** to render hidden essence nodes and crystals correctly just like the legacy Aetheric Monocle does.
*   **Vitae Lens Health Bars**:
    *   Corrected the entity coordinate lookup logic on both NeoForge and Fabric by treating the render state coordinates as absolute world coordinates and using an expanded bounding box search radius (`0.5` blocks) to guarantee visual healthbar billboards render above all entities.
    *   Implemented `EntityRendererMixin` for Fabric to support identical Vitae Lens healthbar overlays natively on Fabric client.
*   **Materia Lens Tooltips**:
    *   Registered `ItemTooltipCallback` on Fabric to display the Materia Yield breakdown under tooltips when the Materia Lens is active (matching the NeoForge behavior).
*   **Polished Magic Circle Animations**:
    *   Extended magic circle ritual processing duration to `360` ticks (18 seconds) for a slow, elegant, and smooth animation progression.
    *   Added local progress and overclock ticking on the client side in `ScribedChalkBlockEntity` to ensure the animations interpolate smoothly in lockstep with the server without network overhead.
    *   Restricted floating spheres from rendering when creating a circle or when the circle is idle.
    *   Made the miniature magic circles appear alongside the spheres from Phase 1 (rising from ground/item level) instead of popping into existence at Phase 3.
    *   Implemented high-quality easing curves (smoothstep `easeInOut` for Phase 1 & 2, `easeOutCubic` for Phase 3, and accelerating `easeInCubic` for Phase 4) for extremely fluid, magical movement.
    *   Added an organic breathing/pulsing scale factor to the spheres during processing.
    *   Added a vertical translucent alchemical energy containment cylinder (stacked rings) at the center of the magic circle during Phase 2, 3, and 4.

### Added & Polished
*   **Magic Circle Aesthetics**:
    *   Replaced outer boundary ring alchemical symbols with supported Unicode glyphs (`☼`, `☾`, `✦`, `★`, `Ω`, etc.) to guarantee they render correctly in the standard Minecraft font.
    *   Node characters inside active ground-level magic circle vertex circles are now drawn in solid black (`0xFF000000`) for clear readability.
    *   Vertex circles on the ground now render node-specific 2D schematic layouts (Amplifier, Capacitor, Resonator, Diode, Essence Bank) instead of letters.
    *   Floating 3D spheres on perimeter nodes are replaced with flat horizontal miniature magic circles when not processing (or when fancy processing is disabled).
*   **Fancy Magic Circle Processing Animations**:
    *   Registered `"Fancy Magic Circle Processing"` boolean configuration spec for both Fabric and NeoForge.
    *   Implemented 4-stage processing animation sequence:
        *   **Phase 1 (f: 0.0 - 0.3)**: Spheres slowly raise from the item positions on the ground (`y = 0.35` to `y = 1.2`) and scale up vertically (growing from flat 2D discs into 3D spheres). Items shrink from scale `1.0` to `0.0`.
        *   **Phase 2 (f: 0.3 - 0.6)**: Spheres move horizontally towards the center and stack vertically in a 2.5-block span (`y = 0.5` to `y = 3.0`).
        *   **Phase 3 (f: 0.6 - 0.8)**: Spheres shrink to `30%` of their original size, and their miniature flat magic circles are revealed at their stacked heights.
        *   **Phase 4 (f: 0.8 - 1.0)**: The miniature magic circles and their smaller spheres cascade and smash down on top of each other towards the center ground where the final crafted item is spawned.

## Build 000-1-26-201

### Added & Polished
*   **Essence Bank Node System**:
    *   Registered `NodeType.ESSENCE_BANK` in both common and legacy registries.
    *   Implemented 128 essence capacity limit per bank node, adding 128 to the circuit's total capacity.
    *   Added interaction to extract 8 (small), 32 (medium), or 128 (large) essence from the bank node with an empty ampoule.
    *   Added 2-minute (`2400` ticks) `PARALYZED` effect on living entities in range when an essence-filled bank node is broken.
    *   Rendered bank nodes with a custom double-hexagon cage with 6 spokes and a "B" letter symbol.
*   **Arkanist Monocle 5-Slot System**:
    *   Implemented client-side keybinding (X by default) to cycle the active slot (0-4) and send a serverbound custom payload.
    *   Tooltips show active selection arrow (`  > `) and details: first 3 slots display slotted lens or empty `___`, last 2 slots display `?????`.
    *   Added in-inventory right-click combination slotting: drop lenses onto the monocle (or click monocle on a lens) to insert into slots 0, 1, 2, and right-click on empty slots to extract them.
*   **New Monocle Lenses & Visual Overlays**:
    *   Added **Propagation Lens** (Silver and Purple: handles circuit flow lines and decay rendering).
    *   Added **Aetheric Lens** (Gold and Blue-green: copies aetheric vision to show hidden nodes and crystals).
    *   Added **Vitae Lens** (Gold and Red: renders blue billboard and red healthbar above living entities).
    *   Added **Materia Lens** (Brass and Blue: renders item Materia yield overlays in world and tooltips).

### Added & Polished
*   **Essence Bank Node System**:
    *   Registered `NodeType.ESSENCE_BANK` in both common and legacy registries.
    *   Implemented 128 essence capacity limit per bank node, adding 128 to the circuit's total capacity.
    *   Added interaction to extract 8 (small), 32 (medium), or 128 (large) essence from the bank node with an empty ampoule.
    *   Added 2-minute (`2400` ticks) `PARALYZED` effect on living entities in range when an essence-filled bank node is broken.
    *   Rendered bank nodes with a custom double-hexagon cage with 6 spokes and a "B" letter symbol.
*   **Collection Node Decoupled Flow**:
    *   Decoupled collection node harvesting: now distributes collected essence to the entire circuit up to capacity instead of storing it locally.
    *   Empty collection and bank nodes behave like wires/repeaters, propagating signals downstream, while not accepting incoming signals when containing essence.
*   **Arkanist Monocle 5-Slot System**:
    *   Implemented client-side keybinding (V) to cycle the active slot (0-4) and send a serverbound custom payload.
    *   Tooltips show active selection arrow (`  > `) and details: first 3 slots display slotted lens or empty `___`, last 2 slots display `?????`.
    *   Added in-inventory right-click combination slotting: drop lenses onto the monocle (or click monocle on a lens) to insert into slots 0, 1, 2, and right-click on empty slots to extract them.
*   **New Monocle Lenses & Visual Overlays**:
    *   Added **Propagation Lens** (renamed from Alchemical Lens), **Aetheric Lens**, **Vitae Lens** (shows RPG-style healthbars above entities), and **Materia Lens** (shows item Materia yields on tooltip).
    *   Processed transparent 16x16 icon textures: purple and silver frame for Materia, deep blue for Vitae, teal for Aetheric.

### Added & Polished
*   **Alchemical Circuit Shared Essence Pool & Capacity Rules**:
    *   Implemented full alchemical circuit validation traversing all connected trace paths and nodes using a breadth-first search (BFS) starting from the output/source node.
    *   Essence requirements in recipes are now checked and consumed across the entire connected circuit as a single unified pool, instead of being limited to individual circle nodes.
    *   Enforced the alchemical circuit capacity rule: maximum essence limit of `8 * N` (where `N` is the total number of connected chalk blocks, including both nodes and paths).
    *   Clicking a `SOURCE` node with an empty hand now displays detailed circuit filling information in chat: `"Circuit Essence: X/Y (AFFINITY)"`.
    *   Added visual chat feedback upon manual essence insertion: `"Added A essence to circuit (B/C)"`.
    *   Protected `SOURCE` nodes and nodes containing active `Orbis Cells` from being cleared to 0 upon magic circle craft completions, preventing item essence reservoirs from being destroyed.
*   **Collection Node Harvesting & Isolation**:
    *   Refactored `NodeType.COLLECTION` logic to scan the circuit and add 1 essence of the local biome's element when the circuit is below its total capacity limit.
    *   Decoupled the collection node from accepting neighbor signal propagation, ensuring it never accepts any essence inwards.
    *   Allow the collection node to act as a direct signal source propagating at strength 32 downstream as long as it has any gathered essence.
    *   Updated extraction node traversal to detect and drain from both `SOURCE` and `COLLECTION` nodes.

### Added & Polished
*   **Paralyzed Event Handler Platform-Native Registration**:
    *   Completely decoupled event listeners for the `PARALYZED` effect from Architectury's shared `InteractionEvent` module to prevent loader-specific binary signature mismatches (e.g. `EventResult` vs `InteractionResult`).
    *   Emptyicized common `ParalyzedEventHandler` and implemented native event subscribers: registered standard NeoForge `PlayerInteractEvent` handlers (LeftClickBlock, RightClickBlock, RightClickItem, EntityInteract) in `NeoForgeEventSubscriber`, and standard Fabric API callbacks (`UseBlockCallback`, `AttackBlockCallback`) in `EntropicaFabric`. This eliminates any runtime class-transformation or remapping issues on NeoForge and Fabric, completely resolving `AbstractMethodError` crashes.
*   **BufferBuilder "Not building!" Rendering Fix**:
    *   Refactored the drawing sequence in `ScribedChalkRenderer` to guarantee that all line and ring geometry (using `VertexConsumer`) finishes building completely before any alchemical symbols, orbiting characters, or node labels (using `Font.drawInBatch`) start rendering. This prevents font draw calls from prematurely closing the active geometry buffer builder, eliminating the `IllegalStateException: Not building!` world-join crash.
*   **Magic Circle Always-Visible Rendering**:
    *   Overrode `shouldRenderOffScreen` inside `ScribedChalkRenderer` to return `true`, completely bypassing frustum culling for active chalk nodes. This ensures that the magic circle remains fully visible as long as any part of its lines are on-screen, even if the center output node is off-screen.
*   **Expanded Magic Circle Node Limits & X-Gon Scale**:
    *   Increased maximum perimeter node count limits and the drawn X-Gons to: Tier 1: 8 nodes (1 input, 1 source, 6 runes), Tier 2: 16 nodes, Tier 3: 24 nodes, Tier 4: 32 nodes.
    *   Updated concentric circle offset definitions and validation checks in `ScribedChalkBlock` to support the full larger rings (Octagon, 16-Gon, 24-Gon, 32-Gon configurations).
*   **Arcane Alchemical Symbols & Inverted Glowing Overlays**:
    *   Implemented flat, slowly rotating alchemical symbols (☉, ☽, ☿, ♀, ♂, ♃, ♄, ♾, etc.) rendered along the boundary of active magic circles.
    *   Dynamically scaled the number of outer perimeter symbols based on circle size/tier: Tier 1 (3x3): 18 symbols, Tier 2 (5x5): 30 symbols, Tier 3 (7x7): 42 symbols, Tier 4 (9x9): 54 symbols.
    *   Symbols render with full self-illumination (glow) in the dark and feature inverted colors matching the opposite spectrum of the circle's affinity color.
    *   Added four glowing, orbiting alchemical glyphs (α, δ, λ, Ω) around the perimeter circle of both standard circuit nodes and active magic circle vertex nodes.
*   **Magic Circle Wildcard Essence Matching**:
    *   Updated magic circle recipe matching logic (`MagicCircleRecipe.matches`) to sum all provided essences (including regular/plain and elemental) when checking wildcard (`REGULAR`) essence requirements.
    *   Fixed circle scan to include regular/plain essence levels inside the input essence map instead of explicitly skipping them.
*   **Paralyzed Status Effect**:
    *   Implemented status effect behavior restricting entity horizontal velocity, jumping, and interactions.
    *   Added beautiful client-side head/limb purple electric particle arcs in third-person.
    *   Added GUI viewport overlay with procedurally flashing neon lightning borders.
*   **Propagation Lens & Arkanist Monocle**:
    *   Custom crafted items registered and integrated with Curios monocle slot tags.
    *   Added viewport overlay rendering a chromatic oil-slick color cycle when equipped.
    *   Implemented 3D neon flow indicators/arrows projecting above chalk circuit paths to guide players in tracing signal decay and direction.
*   **Alchemical Containment Breach**:
    *   Breaking active circuit blocks triggers a gaseous cloud/essence particle burst.
    *   Applies Materia Toxicity and Paralyzed effects (durations proportional to block essence level).
    *   Moved logic to `dropStoredContents` to ensure multi-version signature compatibility.

### Added & Polished
*   **Vis-to-Materia Renaming Completeness**:
    *   Renamed all remaining standalone references of `Vis` or `vis` inside GUI overlay text, JEI recipe categories, tooltips, and chat feedback messages to `Materia` or `materia`.
    *   Renamed `VisFusionRecipeCategory.java` to `MateriaFusionRecipeCategory.java` and `VisReadoutRenderer.java` to `MateriaReadoutRenderer.java` across modules.
    *   Updated the global translation keys and target language files (`de_de.json`, `fr_fr.json`, etc.) to map these term changes across all 8 translation catalogs.
*   **Fehu Filter Tuning Fork Exclusion**:
    *   Removed the `VoidResonantTuningForkItem` from Fehu filter node interaction, aligning strictly with the directive that tuning forks are excluded from alchemical circuit operations (leaving empty hand interaction as the exclusive reset method).

### Added & Polished
*   **Full Multi-Language Support**:
    *   Added complete translation files for all 629+ keys (totaling 650 lines) of the mod's localization catalog.
    *   Supported languages include: German (`de_de.json`), French (`fr_fr.json`), Spanish (`es_es.json`), Portuguese (`pt_br.json`), Russian (`ru_ru.json`), Simplified Chinese (`zh_cn.json`), Japanese (`ja_jp.json`), and Korean (`ko_kr.json`).
    *   Fully preserved all layout formatting codes (e.g. `§a`, `§c`), placeholders (e.g. `%s`, `%d`), and newlines (`\n`) for clean rendering in-game.

### Added & Polished
*   **Global Renaming Alignment**:
    *   Renamed remaining occurrences of `Mana` or `mana` inside variables, fields, NBT methods, and comments to `Materia` or `materia` across the entire java codebase (including both `common/` and legacy `src/` modules).
    *   Renamed config comments and options (both Fabric and NeoForge configurations) to refer strictly to `Materia` instead of `Mana` to enforce consistent terminology.
    *   Updated comments in the language catalog `en_us.json` to reference `MATERIA AMPOULES` rather than `MANA AMPOULES`.
*   **Documentation Flowcharts & Reference**:
    *   Added detailed node-by-node setup diagrams for all 10 specialized alchemical circuit node types (Source, Input, Output, Rune, Amplifier, Capacitor, Collection, Extraction, Resonator, Diode, and Logic Gates).
    *   Omitted and replaced all legacy references to `Vis` or `vis` in document text, tooltips, and setup diagrams to strictly refer to `Materia` or `materia`.
    *   Removed references to `Tuning Forks` for starting/interacting with alchemical circuits (clarifying that rituals are triggered and Fehu filters are cleared via empty hand right-clicks).

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

## Build 000-1-26-200

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

### Fixed
*   **Magic Circle Ritual Recipes**:
    *   Fixed a bug where hardcoded fallback recipes (such as the Iron Ingot + Rune of Uruz + 50 Nether -> Arcanite Ingot placeholder) were not evaluated during ritual execution. Added a search fallback in `ScribedChalkBlock` to scan both registered datapack recipes and hardcoded fallback recipes.
    *   Added a datapack JSON recipe for the `arcanite_ingot` magic circle ritual to ensure native datapack support and correct JEI synchronization.
*   **Wildcard Essence Matching**:
    *   Enabled wildcard essence matching on magic circle recipes. Specifying `EssenceType.REGULAR` (serialized as `"regular"` in JSON) now dynamically matches and sums all active, infused essences in the circle.
    *   Updated the Arcanite Ingot placeholder recipe to require 50 of any essence (using `REGULAR` wildcard) instead of requiring Nether essence.
    *   Updated the JEI magic circle recipe category to render the wildcard requirement as "Any: <amount>".

---

## Build 000-1-26-199

### Fixed
*   **Magic Circle Rendering (Geometry & Timing)**:
    *   Resolved rendering geometry distortion and infinitely-stretched polygram lines by fixing float precision loss in rotation calculations. Swapped raw system time for a smooth sub-tick game time modulo calculation (`(level.getGameTime() % 360000L) + partialTick`).
    *   Overloaded `getCurrentRGB` in `EssenceType` to support `double` values, enabling smooth client-side alchemical color cycles.
*   **Node Input Lockout**:
    *   Restricted general item, essence, and rune insertion/extraction on `INPUT`, `SOURCE`, and `RUNE` nodes to active magic circles only (`isInActiveCircle() == true`). This prevents nodes from taking chalk or other materials out of the player's hand while they are cycling node types.

---

### Added
*   **Scribed Chalk & Magic Circles**:
    *   Implemented smooth magic circle rotations by using system time for rendering instead of game ticks.
    *   Added support for dynamic node counts, dynamically adjusting the circle's rendering, polygons, and star shapes based on the exact nodes placed (requiring a minimum of 1 input, 1 source, and 1 rune node, up to the maximum supported by the tier).
    *   Allowed general item insertion and extraction on `INPUT` nodes via right-clicking, and updated alchemical recipe matching/consumption to pull from these stored item slots before searching for floating entities.
    *   Hid individual chalk dots and connections once the magic circle is assembled.
    *   Implemented full stored item dropping behavior on circle breakdown (manual player break or programmatic cascade) for all stored items across input, rune, and output nodes, while destroying the essence.

---

### Fixed
*   **Scribed Chalk Blocks**:
    *   Resolved a potential null pointer / property lookup crash that occurs when placing, breaking, or ticking chalk lines on the ground. Added safety type checks to verify that the block state is a `ScribedChalkBlock` before attempting to retrieve its `NODE_TYPE` or `CIRCUIT` properties (protecting against air/fallback states during block state updates).

---

### Fixed
*   **Empty Ampoules**:
    *   Resolved missing textures for the base/empty ampoule items (`small_ampoule_base`, `medium_ampoule_base`, `large_ampoule_base`) by updating their item model JSON files to point directly to the existing empty ampoule texture files, avoiding files duplication.
*   **Decompression Coupling**:
    *   Completed the renaming refactor for `VAPOR_DECOMPRESSION_COUPLING_ITEM` to `DECOMPRESSION_COUPLING_ITEM` in `ModItems.java` and `ModModelProvider.java` to align with the block registry name, resolving compile errors.

---

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

### Fixed
*   **Recipe Parsing Compatibility (MC )**:
    *   Fixed data parsing errors where Minecraft  failed to load recipes because ingredients used the legacy `{"item": "..."}` syntax without a specified type. Updated `decompression_coupling`, `arcane_brick_block`, `arcane_clay_block`, and `smelt_arcane_clay` to use direct string values for ingredient keys (`"item_id"`) which parses correctly on the new engine.

---

### Added
*   **Decompression Coupling Smart Connection**:
    *   Implemented smart alignment logic on placement and neighbor changes. The Decompression Coupling now dynamically scans adjacent axes for any connected pipe, conduit, pipeline, valve, diverter, port, or agitator blocks and automatically aligns its axis (`AXIS`) to match them, allowing seamless connections to the pipe network.

---

### Changed
*   **Dynamic Weapon Models**:
    *   Mapped all 15 dynamic weapon/tool item models (`dynamic_sword`, `dynamic_axe`, `dynamic_pickaxe`, `dynamic_shovel`, `dynamic_adze`, `dynamic_paxel`, `dynamic_spear`, `dynamic_mace`, `dynamic_morning_star`, `dynamic_warhammer`, `dynamic_shortbow`, `dynamic_longbow`, `dynamic_war_bow`, `dynamic_crossbow`, `dynamic_repeater`) to display their corresponding 2D shape concept textures in-game instead of rendering missing textures.
*   **Decompression Coupling**:
    *   Renamed "Vapor Decompression Coupling" to "Decompression Coupling" across registry identifiers (`decompression_coupling`), localization keys, and datagen classes.
    *   Updated the block's item model to render the pipe core model (`decompression_coupling_core`) instead of the solid block model.
    *   Added a shaped crafting recipe (`ZXZ` where `X` is a pressure-graded gasket and `Z` is essence-enriched glass).

---

## Build 000-1-26-198

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

## Build 000-1-26-197

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
