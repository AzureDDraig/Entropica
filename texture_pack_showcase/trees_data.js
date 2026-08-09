/**
 * 32x32 Modded Tree Texture Pack Showcase - Trees Metadata Registry
 * Complete specification for 10 premier modded tree species.
 */
const TREES_DATA = [
  {
    id: "greatwood",
    displayName: "Greatwood",
    modOrigin: "Thaumcraft",
    description: "Ancient, heavy, dark magical oak with thick umber bark, mossy deep crevices, glowing golden Materia vein accents, rich bronze heartwood, and dense emerald canopy foliage.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/greatwood_log.png",
      log_top: "assets/modded_trees/textures/block/greatwood_log_top.png",
      leaves: "assets/modded_trees/textures/block/greatwood_leaves.png"
    },
    colorPalette: [
      "#5A3D28", "#442B1A", "#311E11", "#21130A", "#130B05",
      "#2D4518", "#7FA532", "#D9A229",
      "#C49233", "#8C5E23", "#613F17", "#3B240C", "#21130A",
      "#73A832", "#4D7D23", "#315416", "#1E360D", "#101F06"
    ],
    paletteDetails: {
      bark: [
        { hex: "#5A3D28", role: "Bark Highlight Step 1" },
        { hex: "#442B1A", role: "Bark Mid-High Step 2" },
        { hex: "#311E11", role: "Bark Base Midtone Step 3" },
        { hex: "#21130A", role: "Bark Shadow Step 4" },
        { hex: "#130B05", role: "Deep Crevice Step 5" },
        { hex: "#7FA532", role: "Olive Moss Accent" },
        { hex: "#D9A229", role: "Materia Vein Glow" }
      ],
      logTop: [
        { hex: "#C49233", role: "Heartwood Core Glow Step 1" },
        { hex: "#8C5E23", role: "Heartwood Mid Step 2" },
        { hex: "#613F17", role: "Inner Ring Step 3" },
        { hex: "#3B240C", role: "Outer Ring Step 4" },
        { hex: "#21130A", role: "Bark Perimeter Step 5" }
      ],
      leaves: [
        { hex: "#73A832", role: "Canopy Highlight Step 1" },
        { hex: "#4D7D23", role: "Mid Leaves Step 2" },
        { hex: "#315416", role: "Base Midtone Step 3" },
        { hex: "#1E360D", role: "Foliage Shadow Step 4" },
        { hex: "#101F06", role: "Canopy Void Step 5" }
      ]
    }
  },
  {
    id: "silverwood",
    displayName: "Silverwood",
    modOrigin: "Thaumcraft",
    description: "Shimmering, ethereal silvery-white magical birch with pale slate bark, glowing cyan Materia aura streaks, iridescent aquamarine heartwood rings, and luminous pale cyan leaves.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/silverwood_log.png",
      log_top: "assets/modded_trees/textures/block/silverwood_log_top.png",
      leaves: "assets/modded_trees/textures/block/silverwood_leaves.png"
    },
    colorPalette: [
      "#F0F4F8", "#C8D4E0", "#96A9BD", "#63788E", "#3B4C5E",
      "#7CE8FF", "#BCEEFF",
      "#99E6FF", "#66C2EC", "#408EB4", "#B0C2D4", "#E2EAF2",
      "#E0FFFF", "#78ECFE", "#3CBBD7", "#1F7E97", "#0E4B5B"
    ],
    paletteDetails: {
      bark: [
        { hex: "#F0F4F8", role: "Silver Highlight Step 1" },
        { hex: "#C8D4E0", role: "Soft Silver Step 2" },
        { hex: "#96A9BD", role: "Base Midtone Step 3" },
        { hex: "#63788E", role: "Groove Shadow Step 4" },
        { hex: "#3B4C5E", role: "Deep Slate Seam Step 5" },
        { hex: "#7CE8FF", role: "Cyan Materia Vein" },
        { hex: "#BCEEFF", role: "Aura Highlight" }
      ],
      logTop: [
        { hex: "#99E6FF", role: "Iridescent Core Step 1" },
        { hex: "#66C2EC", role: "Inner Ring Step 2" },
        { hex: "#408EB4", role: "Mid Ring Step 3" },
        { hex: "#B0C2D4", role: "Outer Grain Step 4" },
        { hex: "#E2EAF2", role: "Silver Crust Step 5" }
      ],
      leaves: [
        { hex: "#E0FFFF", role: "Icy Glow Tip Step 1" },
        { hex: "#78ECFE", role: "High Canopy Step 2" },
        { hex: "#3CBBD7", role: "Base Midtone Step 3" },
        { hex: "#1F7E97", role: "Foliage Shadow Step 4" },
        { hex: "#0E4B5B", role: "Abyssal Void Step 5" }
      ]
    }
  },
  {
    id: "twilight_oak",
    displayName: "Twilight Oak",
    modOrigin: "Twilight Forest",
    description: "Mythic, giant twilight canopy tree featuring mossy warm oak bark, glowing amber heartwood rings, and rich twilight-green foliage.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/twilight_oak_log.png",
      log_top: "assets/modded_trees/textures/block/twilight_oak_log_top.png",
      leaves: "assets/modded_trees/textures/block/twilight_oak_leaves.png"
    },
    colorPalette: [
      "#6B4728", "#4F3219", "#37210E", "#241407", "#170D03",
      "#4A7426", "#7DAE37",
      "#E29D32", "#B8741E", "#824F13", "#543209", "#321C06",
      "#8FC637", "#5A9422", "#3B6913", "#24450A", "#122604"
    ],
    paletteDetails: {
      bark: [
        { hex: "#6B4728", role: "Warm Bark Highlight Step 1" },
        { hex: "#4F3219", role: "Mid-High Bark Step 2" },
        { hex: "#37210E", role: "Base Midtone Step 3" },
        { hex: "#241407", role: "Crevice Shadow Step 4" },
        { hex: "#170D03", role: "Void Bark Shadow Step 5" },
        { hex: "#7DAE37", role: "Twilight Moss Highlight" }
      ],
      logTop: [
        { hex: "#E29D32", role: "Amber Heartwood Core Step 1" },
        { hex: "#B8741E", role: "Inner Ring Step 2" },
        { hex: "#824F13", role: "Mid Wood Ring Step 3" },
        { hex: "#543209", role: "Outer Wood Step 4" },
        { hex: "#321C06", role: "Bark Perimeter Step 5" }
      ],
      leaves: [
        { hex: "#8FC637", role: "Twilight Sunlit Tip Step 1" },
        { hex: "#5A9422", role: "Canopy Mid-High Step 2" },
        { hex: "#3B6913", role: "Base Midtone Step 3" },
        { hex: "#24450A", role: "Foliage Shadow Step 4" },
        { hex: "#122604", role: "Deep Canopy Void Step 5" }
      ]
    }
  },
  {
    id: "canopy_tree",
    displayName: "Canopy Tree",
    modOrigin: "Twilight Forest",
    description: "Forest giant with smooth teal-grey wood, soft olive-lime log top rings, and broad flat teal canopy leaves forming high twilight ceilings.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/canopy_tree_log.png",
      log_top: "assets/modded_trees/textures/block/canopy_tree_log_top.png",
      leaves: "assets/modded_trees/textures/block/canopy_tree_leaves.png"
    },
    colorPalette: [
      "#5C7C75", "#425E58", "#2C433E", "#1B2B28", "#0E1715",
      "#9DB863", "#738E42", "#4C622B", "#31421B", "#1D2D29",
      "#61CBB3", "#389B84", "#226C5A", "#13473B", "#082720"
    ],
    paletteDetails: {
      bark: [
        { hex: "#5C7C75", role: "Teal Bark Highlight Step 1" },
        { hex: "#425E58", role: "Mid-High Teal Step 2" },
        { hex: "#2C433E", role: "Base Midtone Step 3" },
        { hex: "#1B2B28", role: "Groove Shadow Step 4" },
        { hex: "#0E1715", role: "Deep Seam Step 5" }
      ],
      logTop: [
        { hex: "#9DB863", role: "Lime-Green Core Step 1" },
        { hex: "#738E42", role: "Inner Rings Step 2" },
        { hex: "#4C622B", role: "Mid Ring Wood Step 3" },
        { hex: "#31421B", role: "Outer Ring Step 4" },
        { hex: "#1D2D29", role: "Teal Bark Boundary Step 5" }
      ],
      leaves: [
        { hex: "#61CBB3", role: "Mint-Teal Highlight Step 1" },
        { hex: "#389B84", role: "Canopy Top Step 2" },
        { hex: "#226C5A", role: "Base Midtone Step 3" },
        { hex: "#13473B", role: "Foliage Shadow Step 4" },
        { hex: "#082720", role: "Midnight Void Step 5" }
      ]
    }
  },
  {
    id: "redwood",
    displayName: "Redwood",
    modOrigin: "Natura / BOP",
    description: "Titan woodland redwood with fibrous, shredded rust-red bark, massive pinkish-red multi-ringed log tops, and rich crimson evergreen needle foliage.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/redwood_log.png",
      log_top: "assets/modded_trees/textures/block/redwood_log_top.png",
      leaves: "assets/modded_trees/textures/block/redwood_leaves.png"
    },
    colorPalette: [
      "#964536", "#722E21", "#521E14", "#38110B", "#210905",
      "#DB6F5E", "#B04737", "#842E20", "#591C12", "#3A1009",
      "#DF4736", "#AC2819", "#7A170C", "#500C05", "#2E0502"
    ],
    paletteDetails: {
      bark: [
        { hex: "#964536", role: "Rust-Red Highlight Step 1" },
        { hex: "#722E21", role: "Mid Redwood Step 2" },
        { hex: "#521E14", role: "Base Midtone Step 3" },
        { hex: "#38110B", role: "Fibrous Shadow Step 4" },
        { hex: "#210905", role: "Maroon Seam Step 5" }
      ],
      logTop: [
        { hex: "#DB6F5E", role: "Salmon-Red Core Step 1" },
        { hex: "#B04737", role: "Inner Ring Step 2" },
        { hex: "#842E20", role: "Mid Ring Step 3" },
        { hex: "#591C12", role: "Outer Ring Step 4" },
        { hex: "#3A1009", role: "Rust Bark Border Step 5" }
      ],
      leaves: [
        { hex: "#DF4736", role: "Crimson Sunlit Tip Step 1" },
        { hex: "#AC2819", role: "High Foliage Step 2" },
        { hex: "#7A170C", role: "Base Midtone Step 3" },
        { hex: "#500C05", role: "Burgundy Shadow Step 4" },
        { hex: "#2E0502", role: "Abyssal Maroon Void Step 5" }
      ]
    }
  },
  {
    id: "rubber_wood",
    displayName: "Rubber Wood",
    modOrigin: "IC2 / TechReborn",
    description: "Smooth ash-grey industrial utility timber marked with golden latex resin spots, yellow-tinted log rings, and pale lime-yellow translucent leaves.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/rubber_wood_log.png",
      log_top: "assets/modded_trees/textures/block/rubber_wood_log_top.png",
      leaves: "assets/modded_trees/textures/block/rubber_wood_leaves.png"
    },
    colorPalette: [
      "#A6A298", "#807C73", "#5F5B53", "#413E38", "#262420",
      "#FFD13B", "#C49615", "#6E5004",
      "#F2E27B", "#C9B64B", "#9E8D31", "#706320", "#524C41",
      "#A5D936", "#7DB51F", "#558810", "#385C08", "#1E3503"
    ],
    paletteDetails: {
      bark: [
        { hex: "#A6A298", role: "Pale Ash Highlight Step 1" },
        { hex: "#807C73", role: "Mid Stone Grey Step 2" },
        { hex: "#5F5B53", role: "Base Midtone Step 3" },
        { hex: "#413E38", role: "Shadow Groove Step 4" },
        { hex: "#262420", role: "Charcoal Seam Step 5" },
        { hex: "#FFD13B", role: "Latex Resin Spot" }
      ],
      logTop: [
        { hex: "#F2E27B", role: "Latex Core Highlight Step 1" },
        { hex: "#C9B64B", role: "Inner Ring Step 2" },
        { hex: "#9E8D31", role: "Olive-Tinted Ring Step 3" },
        { hex: "#706320", role: "Outer Wood Step 4" },
        { hex: "#524C41", role: "Ash Bark Rim Step 5" }
      ],
      leaves: [
        { hex: "#A5D936", role: "Yellow-Lime Tip Step 1" },
        { hex: "#7DB51F", role: "High Foliage Step 2" },
        { hex: "#558810", role: "Base Midtone Step 3" },
        { hex: "#385C08", role: "Olive Shadow Step 4" },
        { hex: "#1E3503", role: "Canopy Void Step 5" }
      ]
    }
  },
  {
    id: "sacred_oak",
    displayName: "Sacred Oak",
    modOrigin: "Biomes O' Plenty",
    description: "Regal ancient tree with golden-bronze ridged bark, radiant golden sunburst log rings, and lush shimmering emerald-gold leaves.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/sacred_oak_log.png",
      log_top: "assets/modded_trees/textures/block/sacred_oak_log_top.png",
      leaves: "assets/modded_trees/textures/block/sacred_oak_leaves.png"
    },
    colorPalette: [
      "#8E6433", "#6E4921", "#4E3113", "#331E09", "#1F1004",
      "#F7D24D", "#C69A28", "#916A16", "#614309", "#3E2905",
      "#A3D636", "#6CB51B", "#42870F", "#285908", "#133003"
    ],
    paletteDetails: {
      bark: [
        { hex: "#8E6433", role: "Golden Bark Highlight Step 1" },
        { hex: "#6E4921", role: "Mid Bronze Step 2" },
        { hex: "#4E3113", role: "Base Midtone Step 3" },
        { hex: "#331E09", role: "Crevice Shadow Step 4" },
        { hex: "#1F1004", role: "Deep Seam Step 5" }
      ],
      logTop: [
        { hex: "#F7D24D", role: "Sunburst Core Step 1" },
        { hex: "#C69A28", role: "Golden Ring Step 2" },
        { hex: "#916A16", role: "Amber Ring Step 3" },
        { hex: "#614309", role: "Outer Ring Step 4" },
        { hex: "#3E2905", role: "Bronze Bark Rim Step 5" }
      ],
      leaves: [
        { hex: "#A3D636", role: "Emerald-Gold Tip Step 1" },
        { hex: "#6CB51B", role: "Lush High Foliage Step 2" },
        { hex: "#42870F", role: "Base Midtone Step 3" },
        { hex: "#285908", role: "Emerald Shadow Step 4" },
        { hex: "#133003", role: "Abyssal Sacred Void Step 5" }
      ]
    }
  },
  {
    id: "archwood",
    displayName: "Archwood",
    modOrigin: "Ars Nouveau",
    description: "High-magic arcane tree with deep indigo/violet rough bark, glowing magenta/violet heartwood rings, pulsating mana veins, and shimmering star-lit deep violet and magenta leaf clusters.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/archwood_log.png",
      log_top: "assets/modded_trees/textures/block/archwood_log_top.png",
      leaves: "assets/modded_trees/textures/block/archwood_leaves.png"
    },
    colorPalette: [
      "#52437B", "#3B2C5C", "#271C40", "#170F28", "#0B0616",
      "#E052DF", "#9C22AC",
      "#F26DF7", "#BC39CE", "#802196", "#4C1261", "#29133D",
      "#F77BEE", "#C43DBE", "#862187", "#521056", "#2C0530"
    ],
    paletteDetails: {
      bark: [
        { hex: "#52437B", role: "Indigo Highlight Step 1" },
        { hex: "#3B2C5C", role: "Royal Indigo Step 2" },
        { hex: "#271C40", role: "Base Midtone Step 3" },
        { hex: "#170F28", role: "Groove Shadow Step 4" },
        { hex: "#0B0616", role: "Midnight Purple Seam Step 5" },
        { hex: "#E052DF", role: "Glowing Mana Vein" }
      ],
      logTop: [
        { hex: "#F26DF7", role: "Mana Core Glow Step 1" },
        { hex: "#BC39CE", role: "Vibrant Violet Ring Step 2" },
        { hex: "#802196", role: "Deep Purple Step 3" },
        { hex: "#4C1261", role: "Outer Ring Step 4" },
        { hex: "#29133D", role: "Indigo Perimeter Step 5" }
      ],
      leaves: [
        { hex: "#F77BEE", role: "Starlight Magenta Tip Step 1" },
        { hex: "#C43DBE", role: "Purple Canopy Step 2" },
        { hex: "#862187", role: "Base Midtone Step 3" },
        { hex: "#521056", role: "Foliage Shadow Step 4" },
        { hex: "#2C0530", role: "Arcane Void Step 5" }
      ]
    }
  },
  {
    id: "driftwood",
    displayName: "Driftwood",
    modOrigin: "Betweenlands / Ocean",
    description: "Sun-bleached, salt-cured weathered grey-blue timber with smooth worn contours, pale washed ringed log tops, and sparse sea-moss olive foliage.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/driftwood_log.png",
      log_top: "assets/modded_trees/textures/block/driftwood_log_top.png",
      leaves: "assets/modded_trees/textures/block/driftwood_leaves.png"
    },
    colorPalette: [
      "#9AA5AB", "#75838B", "#536068", "#38434A", "#21282D",
      "#DCE6EB",
      "#CBD7DE", "#A2B0BA", "#788690", "#52606A", "#3B464F",
      "#84A36B", "#5F7E49", "#415B2D", "#2A3F1B", "#16260C"
    ],
    paletteDetails: {
      bark: [
        { hex: "#9AA5AB", role: "Bleached Highlight Step 1" },
        { hex: "#75838B", role: "Weathered Slate Step 2" },
        { hex: "#536068", role: "Base Midtone Step 3" },
        { hex: "#38434A", role: "Groove Shadow Step 4" },
        { hex: "#21282D", role: "Deep Seam Step 5" },
        { hex: "#DCE6EB", role: "Salt Deposit Line" }
      ],
      logTop: [
        { hex: "#CBD7DE", role: "Bleached Core Step 1" },
        { hex: "#A2B0BA", role: "Inner Ring Step 2" },
        { hex: "#788690", role: "Mid Ring Step 3" },
        { hex: "#52606A", role: "Outer Ring Step 4" },
        { hex: "#3B464F", role: "Washed Bark Rim Step 5" }
      ],
      leaves: [
        { hex: "#84A36B", role: "Sea Moss Tip Step 1" },
        { hex: "#5F7E49", role: "Weathered Olive Step 2" },
        { hex: "#415B2D", role: "Base Midtone Step 3" },
        { hex: "#2A3F1B", role: "Kelp Shadow Step 4" },
        { hex: "#16260C", role: "Abyssal Marine Void Step 5" }
      ]
    }
  },
  {
    id: "entropic_elder",
    displayName: "Entropic Elder",
    modOrigin: "Entropica",
    description: "Cosmic, void-infused primordial tree with obsidian charcoal bark split by pulsating purple Entropic Materia crystal fissures, glowing void-magenta rings, and swirling dark void-materia leaves with sparkling magenta/violet energy embers.",
    texturePaths: {
      log: "assets/modded_trees/textures/block/entropic_elder_log.png",
      log_top: "assets/modded_trees/textures/block/entropic_elder_log_top.png",
      leaves: "assets/modded_trees/textures/block/entropic_elder_leaves.png"
    },
    colorPalette: [
      "#3B3548", "#272133", "#191424", "#0E0A17", "#05030A",
      "#E040FB", "#AA00FF", "#5E00A3",
      "#F500FF", "#B000DB", "#720096", "#410059", "#1B1226",
      "#FF70F8", "#C820DB", "#7A0C91", "#460354", "#22002B"
    ],
    paletteDetails: {
      bark: [
        { hex: "#3B3548", role: "Obsidian Bark Highlight Step 1" },
        { hex: "#272133", role: "Void Charcoal Step 2" },
        { hex: "#191424", role: "Base Midtone Step 3" },
        { hex: "#0E0A17", role: "Abyssal Groove Step 4" },
        { hex: "#05030A", role: "Void Seam Step 5" },
        { hex: "#E040FB", role: "Entropic Materia Rift" },
        { hex: "#AA00FF", role: "Violet Energy Glow" }
      ],
      logTop: [
        { hex: "#F500FF", role: "Void-Magenta Core Glow Step 1" },
        { hex: "#B000DB", role: "Entropic Ring Step 2" },
        { hex: "#720096", role: "Deep Violet Ring Step 3" },
        { hex: "#410059", role: "Outer Void Boundary Step 4" },
        { hex: "#1B1226", role: "Charcoal Bark Rim Step 5" }
      ],
      leaves: [
        { hex: "#FF70F8", role: "Entropic Ember Tip Step 1" },
        { hex: "#C820DB", role: "Void-Magenta Canopy Step 2" },
        { hex: "#7A0C91", role: "Base Midtone Step 3" },
        { hex: "#460354", role: "Void Foliage Shadow Step 4" },
        { hex: "#22002B", role: "Abyssal Entropic Void Step 5" }
      ]
    }
  }
];

if (typeof window !== "undefined") {
  window.TREES_DATA = TREES_DATA;
}
if (typeof module !== "undefined" && module.exports) {
  module.exports = { TREES_DATA };
}
