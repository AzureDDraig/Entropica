# 🌌 Create and Texture These for Astral Materia

This document tracks all new blocks, multiblock components, functional tile entities, instruments, tools, weapons, fluids, and textures required for the **Astral Materia & Constellation System** in Entropica.

---

## 🏛️ 1. Architectural & Decorative Blocks (Astral Marble & Starlight Suite)

| Block Registry ID | Block Name | Description / Visual Aesthetic |
| :--- | :--- | :--- |
| `astral_marble` | Astral Marble | Polished celestial white marble with subtle crystalline veins. |
| `astral_marble_bricks` | Astral Marble Bricks | Classical Greek-style temple bricks. |
| `astral_marble_slab` | Astral Marble Slab | Half-slab architectural variant. |
| `astral_marble_stairs` | Astral Marble Stairs | Stair architectural variant. |
| `astral_marble_wall` | Astral Marble Wall | Decorative perimeter balustrade. |
| `sooty_marble` | Sooty Marble | Dark, charred volcanic marble with matte obsidian tones. |
| `runed_astral_marble` | Runed Astral Marble | Marble engraved with glowing celestial cyan Materia rune inlays. |
| `engraved_astral_slate` | Engraved Astral Slate | High-detail astronomical floor and altar tile. |
| `chiseled_astral_marble` | Chiseled Astral Marble | Ornate pillar base and capital trim. |
| `starlight_pillar` | Starlight Pillar | Hybrid column blending *Starlight Aether-Birch* timber core with *Astral Marble* fluting. |
| `astral_mirror_block` | Astral Mirror Block | Real-time planar reflective mirror block used for the 7x7x6 Observatory Celestial Pool floor. |
| `budding_astral_marble` | Budding Astral Marble | Natural underground geode block sprouting wild raw Astral Crystals. |

---

## ⚙️ 2. Workstations, Altars & Multiblock Devices

| Block Registry ID | Block Name | Description / Functionality |
| :--- | :--- | :--- |
| `astral_altar_core` | Astral Altar Core | Single upgradable modular crafting core block with freeform & blueprint detection. |
| `resonance_pylon` | Resonance Pylon | Modular pillar block placed near Altar Core to increase tier and Materia capacity. |
| `attunement_pedestal` | Attunement Pedestal | Modular pedestal holding reagent items for rituals and player attunement. |
| `focal_lens_mount` | Focal Lens Mount | Modular mount holding crystal lenses to channel starlight into the altar core. |
| `astral_infusion_pedestal` | Astral Infusion Pedestal | Solid dry plinth for optical starlight item and flora transmutation. |
| `astral_collector` | Astral Collector | In-world condensing basin; attunes to a star via reticle GUI and auto-pushes Materia into pipes/cells. |
| `celestial_beacon_controller` | Celestial Beacon Controller | Core block of the 3x3x3 Multiblock Celestial Beacon (wide-area chunk field generator). |
| `dormant_crystal_relic` | Dormant Crystal Relic | Worldgen shrine pillar crystal awakened by piping/channeling Materia into it. |
| `stationary_brass_telescope` | Stationary Brass Telescope | Tier 3 ground-mounted high-magnification astronomical telescope block. |
| `celestial_armillary_controller` | Celestial Armillary Controller | Central tracking core of the 7x7x6 Grand Circular Astral Observatory. |
| `stone_hopper_basin` | Stone-Hopper Basin | Automated stone hopper basin beneath Mineralis beacons transmuting stone into ores. |

---

## 🔭 3. Optical Routing & Conduit Logistics

| Block Registry ID | Block Name | Description / Functionality |
| :--- | :--- | :--- |
| `refractive_astral_lens` | Refractive Astral Lens | Stand-mounted optical lens redirecting focused starlight beams through the air up to 32 blocks. |
| `beam_splitter_prism` | Beam Splitter Prism | Optical crystal prism splitting 1 incoming starlight beam into up to 4 sub-beams. |
| `pure_optic_fiber` | Pure Optic Fiber | Enclosed glass-core light-pipe conduit encased in Arcanite/Starlight Birch cladding (1% decay/16b). |
| `optical_transmitter_port` | Optical Transmitter Port | Wall-mounted port sending internal machine light into optic fibers. |
| `optical_receiver_port` | Optical Receiver Port | Wall-mounted port receiving fiber starlight into pressurized machines. |
| `optical_booster_amplifier` | Optical Booster Amplifier | In-line crystal repeater restoring fiber-optic starlight back to 100% full signal strength. |

---

## 🧭 4. Handheld Astronomy Instruments & Scribing Items

| Item Registry ID | Item Name | Description / Tier Progression |
| :--- | :--- | :--- |
| `looking_glass` | Looking Glass | Tier 1 handheld brass spyglass for discovering fundamental constellations. |
| `astrolabe` | Astrolabe | Tier 2 celestial coordinate tool & in-world 3D holographic blueprint projector. |
| `drafting_compass` | Drafting Compass | Tier 2 drafting tool used alongside astrolabe for precision sector mapping. |
| `star_chart_blank` | Blank Star Chart | Celestial parchment crafted from *Starlight Aether-Birch* pulp. |
| `star_chart_completed` | Completed Star Chart | Scribed constellation map item storing star links and lore in the Codex. |
| `astral_linking_wand` | Astral Linking Wand | 2-click node linking tool used to connect collectors, lenses, prisms, and altars. |
| `mortar_and_pestle` | Mortar and Pestle | Crafting tool used to grind raw Astral Crystals into Astral Crystal Seeds. |
| `celestial_tome` | Celestial Tome | High-density 2-page spread readable lore grimoire (5 volumes) with bookmarks, star charts, and ~300 words/page. |

---

## 💎 5. Astral Crystals, Equipment, Baubles & Materials

| Item Registry ID | Item Name | Description / Mechanics |
| :--- | :--- | :--- |
| `astral_crystal` | Astral Crystal | Dual-stat crystal (Size 1..5, Constellation Tuned); slowly regrows on the Astral Altar at night. |
| `astral_crystal_seed` | Astral Crystal Seed | Seed item soaked for 5 mins in Essence-Diluted Fluid to grow into a Size-1 crystal. |
| `essence_diluted_fluid_bucket` | Essence-Diluted Fluid Bucket | Fluid carrier used in cauldrons/basins to cultivate crystal seeds. |
| `starlight_silk` | Starlight Silk | Woven celestial silk thread derived from Starlight Aether-Birch fibers. |
| `astral_crystal_thread` | Astral Crystal Thread | Crystal-infused thread for weaving celestial mantles. |
| `crystal_sword` | Crystal Sword | Size-scaling attack damage; night starlight self-repair (1 dur/5s); drains at 0 durability. |
| `crystal_pickaxe` | Crystal Pickaxe | Size-scaling mining speed; night starlight self-repair; drains at 0 durability. |
| `crystal_axe` | Crystal Axe | Size-scaling chop speed; night starlight self-repair; drains at 0 durability. |
| `crystal_shovel` | Crystal Shovel | Size-scaling dig speed; night starlight self-repair; drains at 0 durability. |
| `drained_crystal_tool` | Drained Crystal Tool | Depleted tool item restorable on the Astral Altar (40% risk of losing 1 enchantment). |
| `resplendent_prism` | Resplendent Prism | Celestial bauble (-2 penalty when empty; scales +1 per 1.5 nights basking up to +4 overcharge). |
| `mantle_of_the_stars` | Mantle of the Stars | Constellation-attuned chestpiece powered by Orbis Cells (5 Materia/s, drops to 1/3s under starlight). |

---

## 🎆 6. Custom Particle Types
- `entropica:starlight_sparkle` (Twinkling cyan/white stardust particles).
- `entropica:constellation_node_flare` (Radiant glowing burst at star vertices).
- `entropica:starlight_beam_ray` (Luminous pulse particles traveling along starlight beams).
- `entropica:astral_infusion_spark` (Vibrant elemental sparks during optical infusion).
- `entropica:celestial_beacon_ring` (Large expanding horizontal ring particle from the 3x3x3 beacon core).

---

## 🎵 7. Custom Sound Events
- `entropica:astral_telescope_pan` & `entropica:astral_telescope_zoom` (Brass gears and optical lens slides).
- `entropica:astral_trace_line` (Resonant crystalline singing bowl chime).
- `entropica:astral_constellation_unlock` (Celestial choir swell with bell strike).
- `entropica:astral_altar_craft` (Harmonic starlight chime).
- `entropica:astral_beacon_loop` (Pulsating low cosmic hum).
