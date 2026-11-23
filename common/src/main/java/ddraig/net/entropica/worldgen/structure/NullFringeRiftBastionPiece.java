package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Programmatic 3D generator for Biome #9's Legendary Sanctuary: {@code Null-Fringe Rift Bastion}
 * ({@code 24} wide {@code X: 0..23}, {@code 26} tall {@code Y: 0..25}, {@code 24} long {@code Z: 0..23},
 * anchored at {@code startY = surfaceY - 5} so the Sunken Reliquary sits {@code 4} blocks below the
 * bay/shore surface while the Rift Crown rises {@code 20} blocks overhead):
 * <ul>
 *   <li><b>Causeway Pier &amp; Sunken {@code Void Glass} Reliquary ({@code Y = 0..6})</b>:
 *       {@code obsidian_bricks} &amp; {@code void_stone} pier leading down dry sealed interior stairs
 *       into an underwater/sub-surface Reliquary Vault walled and roofed with {@code entropica:void_glass},
 *       inlaid with {@code void_crystal_block} floor runes, and housing {@code 2} alcove chests.</li>
 *   <li><b>Four Crystal-Crowned Obsidian Buttress Pylons ({@code Y = 0..14})</b>:
 *       {@code obsidian_bricks} &amp; {@code crying_obsidian} corner towers with downward seabed
 *       foundation columns, crowned with {@code void_crystal_block} &amp; {@code void_crystal_cluster}.</li>
 *   <li><b>Low-Gravity Floating Stepping-Stone Spiral ({@code Y = 7..16})</b>:
 *       {@code 2x2} &amp; {@code 3x3} floating {@code obsidian_bricks} &amp; {@code void_stone} steps
 *       spiraling upward around the bastion plus {@code 4} drifting ambient obsidian/crystal shards.</li>
 *   <li><b>Central Sanctuary Dais &amp; Rift Crown ({@code Y = 15..25})</b>:
 *       Floating diamond {@code void_stone} &amp; {@code obsidian_bricks} platform holding
 *       {@code entropica:void_crystal_block} floating {@code 2} blocks above a
 *       {@code void_crystal_block} pedestal, arched over by {@code 4} sweeping {@code obsidian_bricks}
 *       &amp; {@code entropica:void_glass} ribs converging at {@code entropica:spatial_phase_anchor}
 *       ({@code Y = 24}) with the main Bastion treasure chest on the dais.</li>
 * </ul>
 */
public class NullFringeRiftBastionPiece extends StructurePiece {

    public static final int WIDTH_X = 24;
    public static final int HEIGHT_Y = 26;
    public static final int LENGTH_Z = 24;

    private static final int[][] CORNER_PYLONS = {
            {3, 3}, {21, 3}, {3, 21}, {21, 21}
    };

    private static final int[][] SPIRAL_STEPS_3X3 = {
            {18, 7, 6},
            {19, 9, 13},
            {14, 11, 19},
            {6, 13, 15}
    };

    private static final int[][] SPIRAL_STEPS_2X2 = {
            {19, 8, 9},
            {17, 10, 16},
            {9, 12, 18},
            {5, 14, 11}
    };

    private static final int[][] AMBIENT_DRIFTING_SHARDS = {
            {1, 16, 8},
            {16, 18, 2},
            {22, 17, 16},
            {8, 18, 22}
    };

    private final int startX;
    private final int startY;
    private final int startZ;

    public NullFringeRiftBastionPiece(int centerX, int surfaceY, int centerZ) {
        // Exact 24 (X: 0..23) x 26 (Y: 0..25, startY = surfaceY - 5) x 24 (Z: 0..23)
        super(resolvePieceType(), 0, new BoundingBox(
                centerX - 12,
                surfaceY - 5,
                centerZ - 12,
                centerX + 11,
                surfaceY + 20,
                centerZ + 11
        ));
        this.startX = centerX - 12;
        this.startY = surfaceY - 5;
        this.startZ = centerZ - 12;
    }

    public NullFringeRiftBastionPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.startX = tag.getIntOr("StartX", this.boundingBox.minX());
        this.startY = tag.getIntOr("StartY", this.boundingBox.minY());
        this.startZ = tag.getIntOr("StartZ", this.boundingBox.minZ());
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.NULL_FRINGE_RIFT_BASTION_PIECE.isPresent()) {
            return ModStructures.NULL_FRINGE_RIFT_BASTION_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "null_fringe_rift_bastion_piece");
        return BuiltInRegistries.STRUCTURE_PIECE.getOptional(id).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("StartX", this.startX);
        tag.putInt("StartY", this.startY);
        tag.putInt("StartZ", this.startZ);
    }

    public BlockPos getSpatialPhaseAnchorPos() {
        return toWorld(12, 24, 12);
    }

    public BlockPos getDaisChestPos() {
        return toWorld(12, 16, 9);
    }

    public BlockPos getReliquaryWestChestPos() {
        return toWorld(8, 1, 12);
    }

    public BlockPos getReliquaryEastChestPos() {
        return toWorld(16, 1, 12);
    }

    @Override
    public void postProcess(
            WorldGenLevel level,
            StructureManager structureManager,
            ChunkGenerator generator,
            RandomSource random,
            BoundingBox chunkBox,
            ChunkPos chunkPos,
            BlockPos pivot
    ) {
        clearAboveSurfaceAirVolume(level, chunkBox);
        buildSunkenVoidGlassReliquaryAndPier(level, chunkBox, random);
        buildFourButtressPylons(level, chunkBox);
        buildFloatingSteppingStoneSpiralAndShards(level, chunkBox);
        buildCentralSanctuaryDaisAndRiftCrown(level, chunkBox, random);
    }

    private void clearAboveSurfaceAirVolume(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState air = Blocks.AIR.defaultBlockState();
        // Clear above-water/shoreline headroom (ly = 6..25) while preserving natural bay water at ly = 0..5
        for (int lx = 0; lx < WIDTH_X; lx++) {
            for (int lz = 0; lz < LENGTH_Z; lz++) {
                for (int ly = 6; ly < HEIGHT_Y; ly++) {
                    setLocal(level, chunkBox, lx, ly, lz, air);
                }
            }
        }
    }

    private void buildSunkenVoidGlassReliquaryAndPier(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block obsidianBricksBlock = resolveBlock("obsidian_bricks", Blocks.POLISHED_BLACKSTONE_BRICKS);
        Block voidStoneBlock = resolveBlock("void_stone", Blocks.DEEPSLATE);
        Block voidGlassBlock = resolveBlock("void_glass", Blocks.TINTED_GLASS);
        Block voidCrystalBlock = resolveBlock("void_crystal_block", Blocks.AMETHYST_BLOCK);
        Block voidCrystalClusterBlock = resolveBlock("void_crystal_cluster", Blocks.AMETHYST_CLUSTER);
        Block voidCrystalBudBlock = resolveBlock("void_crystal_bud", Blocks.LARGE_AMETHYST_BUD);

        BlockState obsidianBricks = obsidianBricksBlock.defaultBlockState();
        BlockState voidStone = voidStoneBlock.defaultBlockState();
        BlockState voidGlass = voidGlassBlock.defaultBlockState();
        BlockState voidCrystal = voidCrystalBlock.defaultBlockState();
        BlockState cryingObsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState clusterUp = orientCrystal(voidCrystalClusterBlock.defaultBlockState(), Direction.UP, false);
        BlockState budUp = orientCrystal(voidCrystalBudBlock.defaultBlockState(), Direction.UP, false);
        BlockState hangingSoulLantern = Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Sunken Void-Glass Reliquary Vault (X = 7..17, Z = 7..17, Y = 0..5)
        //    Walkable floor at ly = 1 (surfaceY - 4), panoramic underwater Void Glass walls & ceiling!
        for (int lx = 7; lx <= 17; lx++) {
            for (int lz = 7; lz <= 17; lz++) {
                boolean isPerimeter = (lx == 7 || lx == 17 || lz == 7 || lz == 17);
                boolean isCorner = ((lx == 7 || lx == 17) && (lz == 7 || lz == 17));
                boolean isWallRib = (lx == 10 || lx == 14 || lz == 10 || lz == 14);

                // Solid seabed footings beneath the Reliquary perimeter & central spine
                if (isCorner || (isPerimeter && isWallRib) || (lx == 12 && lz == 12)) {
                    fillFoundationDown(level, chunkBox, lx, -1, lz, voidStone);
                }

                // Floor (ly = 0): obsidian_bricks rim + void_stone tiles + void_crystal_block rune inlay
                int manhattanCenter = Math.abs(lx - 12) + Math.abs(lz - 12);
                boolean crystalFloorInlay = !isPerimeter && (manhattanCenter <= 2
                        || ((lx == 9 || lx == 15) && (lz == 9 || lz == 15))
                        || (lz == 12 && (lx == 9 || lx == 15))
                        || (lx == 12 && (lz == 9 || lz == 15)));
                BlockState floorState = isPerimeter ? obsidianBricks
                        : (crystalFloorInlay ? voidCrystal : (((lx + lz) & 1) == 0 ? obsidianBricks : voidStone));
                setLocal(level, chunkBox, lx, 0, lz, floorState);

                // Sealed Walls & Dry Interior (ly = 1..4)
                for (int ly = 1; ly <= 4; ly++) {
                    if (isPerimeter) {
                        if (isCorner) {
                            setLocal(level, chunkBox, lx, ly, lz, (ly == 2 || ly == 3) ? cryingObsidian : obsidianBricks);
                        } else if (isWallRib || ly == 1) {
                            setLocal(level, chunkBox, lx, ly, lz, (ly == 1) ? voidStone : obsidianBricks);
                        } else {
                            // Panoramic underwater entropica:void_glass viewing windows!
                            setLocal(level, chunkBox, lx, ly, lz, voidGlass);
                        }
                    } else {
                        setLocal(level, chunkBox, lx, ly, lz, air);
                    }
                }

                // Sealed Underwater/Surface Skylight Roof (ly = 5) of entropica:void_glass & obsidian_bricks ribs
                boolean roofFrame = isPerimeter || lx == 10 || lx == 14 || lz == 10 || lz == 14;
                setLocal(level, chunkBox, lx, 5, lz, roofFrame ? obsidianBricks : voidGlass);
            }
        }

        // Reliquary Interior Alcoves, Lighting & 2 Alcove Chests (NULL_FRINGE_RIFT_BASTION_CHEST_LOOT)
        // West Alcove Chest at (8, 1, 12) facing EAST
        setLocal(level, chunkBox, 8, 1, 11, voidCrystal);
        setLocal(level, chunkBox, 8, 2, 11, budUp);
        setLocal(level, chunkBox, 8, 1, 13, voidCrystal);
        setLocal(level, chunkBox, 8, 2, 13, budUp);
        placeBastionChest(level, chunkBox, random, 8, 1, 12, Direction.EAST);

        // East Alcove Chest at (16, 1, 12) facing WEST
        setLocal(level, chunkBox, 16, 1, 11, voidCrystal);
        setLocal(level, chunkBox, 16, 2, 11, budUp);
        setLocal(level, chunkBox, 16, 1, 13, voidCrystal);
        setLocal(level, chunkBox, 16, 2, 13, budUp);
        placeBastionChest(level, chunkBox, random, 16, 1, 12, Direction.WEST);

        // South Focal Crystal Shrine inside the Reliquary (lx = 11..13, lz = 16)
        setLocal(level, chunkBox, 11, 1, 16, obsidianBricks);
        setLocal(level, chunkBox, 12, 1, 16, voidCrystal);
        setLocal(level, chunkBox, 12, 2, 16, clusterUp);
        setLocal(level, chunkBox, 13, 1, 16, obsidianBricks);

        // Hanging Soul Lanterns anchored to the interior obsidian_bricks roof intersections
        int[][] reliquaryLanterns = {{10, 10}, {14, 10}, {10, 14}, {14, 14}};
        for (int[] lt : reliquaryLanterns) {
            setLocal(level, chunkBox, lt[0], 4, lt[1], hangingSoulLantern);
        }

        // 2. Causeway Pier & Watertight Descending Stairwell (X = 10..14, Z = 0..7, Y = 0..8)
        int[][] pierPilings = {{10, 1}, {14, 1}, {10, 4}, {14, 4}};
        for (int[] piling : pierPilings) {
            for (int py = 0; py <= 4; py++) {
                setLocal(level, chunkBox, piling[0], py, piling[1], obsidianBricks);
            }
            fillFoundationDown(level, chunkBox, piling[0], -1, piling[1], voidStone);
        }

        // Z = 0: Shoreline / Swimmer boarding stairs ascending South onto the dry Y = 5 Pier Deck
        for (int lx = 10; lx <= 14; lx++) {
            setLocal(level, chunkBox, lx, 4, 0, voidStone);
            setLocal(level, chunkBox, lx, 5, 0, stair(Direction.SOUTH, Half.BOTTOM));
            setLocal(level, chunkBox, lx, 6, 0, air);
        }

        // Z = 1..2: Dry Causeway Pier Deck (Y = 5) & Watertight Front Bulkhead Wall (Z = 2, Y = 0..5)
        for (int lz = 1; lz <= 2; lz++) {
            for (int lx = 10; lx <= 14; lx++) {
                if (lz == 2) {
                    for (int ly = 0; ly <= 4; ly++) {
                        setLocal(level, chunkBox, lx, ly, 2, voidStone);
                    }
                    fillFoundationDown(level, chunkBox, lx, -1, 2, voidStone);
                } else {
                    setLocal(level, chunkBox, lx, 4, 1, voidStone);
                }
                BlockState deckTile = (lx == 12) ? obsidianBricks : (((lx + lz) & 1) == 0 ? obsidianBricks : voidStone);
                setLocal(level, chunkBox, lx, 5, lz, deckTile);

                if (lx == 10 || lx == 14) {
                    setLocal(level, chunkBox, lx, 6, lz, obsidianBricks);
                    if (lz == 1) {
                        setLocal(level, chunkBox, lx, 7, 1, cryingObsidian);
                        setLocal(level, chunkBox, lx, 8, 1, clusterUp);
                    }
                } else {
                    setLocal(level, chunkBox, lx, 6, lz, air);
                }
            }
        }

        // Z = 3..7: Sealed Dry Descending Stairwell (X = 11..13) flanked by Void-Glass & Obsidian Bulkheads (X = 10, 14)
        for (int lz = 3; lz <= 7; lz++) {
            int stepY = 7 - lz; // lz=3 -> y=4, lz=4 -> y=3, lz=5 -> y=2, lz=6 -> y=1, lz=7 -> y=0 doorway
            int hoodY = (lz == 3) ? 8 : Math.max(5, 11 - lz); // lz=3->8, lz=4->7, lz=5->6, lz=6->5, lz=7->5

            // Watertight side cheek bulkheads at X = 10 and X = 14
            for (int sideX : new int[]{10, 14}) {
                fillFoundationDown(level, chunkBox, sideX, -1, lz, voidStone);
                for (int ly = 0; ly <= hoodY; ly++) {
                    boolean porthole = (lz == 5 && (ly == 3 || ly == 4));
                    BlockState wallState = porthole ? voidGlass
                            : ((lz == 3 && (ly == 6 || ly == 7)) ? cryingObsidian : obsidianBricks);
                    setLocal(level, chunkBox, sideX, ly, lz, wallState);
                }
            }

            // Center 3-wide dry stair flight (X = 11..13)
            for (int lx = 11; lx <= 13; lx++) {
                fillFoundationDown(level, chunkBox, lx, -1, lz, voidStone);
                for (int ly = 0; ly < Math.max(1, stepY); ly++) {
                    setLocal(level, chunkBox, lx, ly, lz, (ly == 0) ? obsidianBricks : voidStone);
                }

                if (lz <= 6) {
                    setLocal(level, chunkBox, lx, stepY, lz, stair(Direction.NORTH, Half.BOTTOM));
                    for (int airY = stepY + 1; airY < hoodY; airY++) {
                        setLocal(level, chunkBox, lx, airY, lz, air);
                    }
                    // Sealed stairwell roof hood (void_glass center skylight strip framed in obsidian_bricks)
                    BlockState hoodBlock = (lx == 12 && lz >= 4) ? voidGlass : obsidianBricks;
                    setLocal(level, chunkBox, lx, hoodY, lz, hoodBlock);
                } else {
                    // Z = 7: Open dry doorway arch through North Reliquary wall (Y = 1..3 air, Y = 4..5 lintel)
                    setLocal(level, chunkBox, lx, 0, 7, obsidianBricks);
                    setLocal(level, chunkBox, lx, 1, 7, air);
                    setLocal(level, chunkBox, lx, 2, 7, air);
                    setLocal(level, chunkBox, lx, 3, 7, air);
                    setLocal(level, chunkBox, lx, 4, 7, obsidianBricks);
                    setLocal(level, chunkBox, lx, 5, 7, obsidianBricks);
                }
            }
        }
        // Portal Arch Crown Finial at (12, 9, 3)
        setLocal(level, chunkBox, 12, 9, 3, clusterUp);

        // East Pier Launch Terrace (X = 15..16, Z = 4..5, Y = 5..6) feeding the Low-Gravity Stepping-Stone Spiral
        setLocal(level, chunkBox, 14, 6, 3, stair(Direction.SOUTH, Half.BOTTOM));
        for (int lx = 15; lx <= 16; lx++) {
            for (int lz = 4; lz <= 5; lz++) {
                setLocal(level, chunkBox, lx, 5, lz, voidStone);
                setLocal(level, chunkBox, lx, 6, lz, (lx == 16 && lz == 5) ? cryingObsidian : obsidianBricks);
            }
        }
        fillFoundationDown(level, chunkBox, 16, 4, 5, voidStone);
    }

    private void buildFourButtressPylons(WorldGenLevel level, BoundingBox chunkBox) {
        Block obsidianBricksBlock = resolveBlock("obsidian_bricks", Blocks.POLISHED_BLACKSTONE_BRICKS);
        Block voidStoneBlock = resolveBlock("void_stone", Blocks.DEEPSLATE);
        Block voidCrystalBlock = resolveBlock("void_crystal_block", Blocks.AMETHYST_BLOCK);
        Block voidCrystalClusterBlock = resolveBlock("void_crystal_cluster", Blocks.AMETHYST_CLUSTER);
        Block voidCrystalBudBlock = resolveBlock("void_crystal_bud", Blocks.LARGE_AMETHYST_BUD);

        BlockState obsidianBricks = obsidianBricksBlock.defaultBlockState();
        BlockState voidStone = voidStoneBlock.defaultBlockState();
        BlockState voidCrystal = voidCrystalBlock.defaultBlockState();
        BlockState cryingObsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState clusterUp = orientCrystal(voidCrystalClusterBlock.defaultBlockState(), Direction.UP, false);

        for (int[] pylon : CORNER_PYLONS) {
            int px = pylon[0];
            int pz = pylon[1];
            int inX = (px < 12) ? 1 : -1;
            int inZ = (pz < 12) ? 1 : -1;

            // 1. Heavy 3x3 Buttressed Base (ly = 0..5) + Downward Seabed Foundation Columns
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    int bx = px + dx;
                    int bz = pz + dz;
                    fillFoundationDown(level, chunkBox, bx, -1, bz, voidStone);

                    boolean isCorner = (Math.abs(dx) == 1 && Math.abs(dz) == 1);
                    int topBaseY = isCorner ? 3 : 5;
                    for (int ly = 0; ly <= topBaseY; ly++) {
                        BlockState state = (dx == 0 && dz == 0) ? cryingObsidian
                                : (ly <= 1 ? voidStone : obsidianBricks);
                        setLocal(level, chunkBox, bx, ly, bz, state);
                    }
                }
            }

            // 2. Mid Buttress Tower Shaft (ly = 6..9, plus-cross profile)
            for (int ly = 6; ly <= 9; ly++) {
                setLocal(level, chunkBox, px, ly, pz, cryingObsidian);
                for (Direction horiz : Direction.Plane.HORIZONTAL) {
                    BlockState fin = (ly == 9) ? voidStone : obsidianBricks;
                    setLocal(level, chunkBox, px + horiz.getStepX(), ly, pz + horiz.getStepZ(), fin);
                }
            }

            // 3. Upper Tapered Spire & Inward Buttress Ribs (ly = 10..12)
            for (int ly = 10; ly <= 12; ly++) {
                setLocal(level, chunkBox, px, ly, pz, (ly == 11) ? cryingObsidian : obsidianBricks);
            }
            setLocal(level, chunkBox, px + inX, 10, pz, obsidianBricks);
            setLocal(level, chunkBox, px, 10, pz + inZ, obsidianBricks);
            setLocal(level, chunkBox, px + inX, 11, pz + inZ, obsidianBricks);

            // 4. Crystal Crown (ly = 13..14): void_crystal_block + upward void_crystal_cluster + side buds
            setLocal(level, chunkBox, px, 13, pz, voidCrystal);
            setLocal(level, chunkBox, px, 14, pz, clusterUp);

            Direction outXDir = (px < 12) ? Direction.WEST : Direction.EAST;
            Direction outZDir = (pz < 12) ? Direction.NORTH : Direction.SOUTH;
            placeCrystalIfAir(level, chunkBox,
                    px + outXDir.getStepX(), 13, pz,
                    orientCrystal(voidCrystalBudBlock.defaultBlockState(), outXDir, false));
            placeCrystalIfAir(level, chunkBox,
                    px, 13, pz + outZDir.getStepZ(),
                    orientCrystal(voidCrystalBudBlock.defaultBlockState(), outZDir, false));
        }
    }

    private void buildFloatingSteppingStoneSpiralAndShards(WorldGenLevel level, BoundingBox chunkBox) {
        Block obsidianBricksBlock = resolveBlock("obsidian_bricks", Blocks.POLISHED_BLACKSTONE_BRICKS);
        Block voidStoneBlock = resolveBlock("void_stone", Blocks.DEEPSLATE);
        Block voidCrystalBlock = resolveBlock("void_crystal_block", Blocks.AMETHYST_BLOCK);
        Block voidCrystalClusterBlock = resolveBlock("void_crystal_cluster", Blocks.AMETHYST_CLUSTER);
        Block voidCrystalBudBlock = resolveBlock("void_crystal_bud", Blocks.LARGE_AMETHYST_BUD);

        BlockState obsidianBricks = obsidianBricksBlock.defaultBlockState();
        BlockState voidStone = voidStoneBlock.defaultBlockState();
        BlockState voidCrystal = voidCrystalBlock.defaultBlockState();
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState cryingObsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState budDown = orientCrystal(voidCrystalBudBlock.defaultBlockState(), Direction.DOWN, false);
        BlockState clusterUp = orientCrystal(voidCrystalClusterBlock.defaultBlockState(), Direction.UP, false);
        BlockState clusterDown = orientCrystal(voidCrystalClusterBlock.defaultBlockState(), Direction.DOWN, false);

        // 1. Alternating 3x3 & 2x2 Low-Gravity Floating Stepping Stones (ly = 7..14)
        for (int[] step : SPIRAL_STEPS_3X3) {
            int cx = step[0];
            int cy = step[1];
            int cz = step[2];
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean center = (dx == 0 && dz == 0);
                    boolean corner = (Math.abs(dx) == 1 && Math.abs(dz) == 1);
                    BlockState deck = center ? voidCrystal : (corner ? voidStone : obsidianBricks);
                    setLocal(level, chunkBox, cx + dx, cy, cz + dz, deck);
                }
            }
            // Tapered floating keel crystal underneath the 3x3 platform
            setLocal(level, chunkBox, cx, cy - 1, cz, cryingObsidian);
            placeCrystalIfAir(level, chunkBox, cx, cy - 2, cz, budDown);
        }

        for (int[] step : SPIRAL_STEPS_2X2) {
            int sx = step[0];
            int sy = step[1];
            int sz = step[2];
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    BlockState deck = ((dx + dz) == 1) ? obsidianBricks : voidStone;
                    setLocal(level, chunkBox, sx + dx, sy, sz + dz, deck);
                }
            }
            setLocal(level, chunkBox, sx, sy - 1, sz, voidStone);
        }

        // West Spiral Bridge Landing (lx = 6, ly = 15, lz = 11..13) joining the Central Diamond Dais
        for (int lz = 11; lz <= 13; lz++) {
            setLocal(level, chunkBox, 6, 15, lz, (lz == 12) ? obsidianBricks : voidStone);
        }
        setLocal(level, chunkBox, 6, 14, 12, obsidianBricks);

        // 2. Four Drifting Ambient Obsidian / Void-Crystal Shards around the Upper Perimeter
        for (int i = 0; i < AMBIENT_DRIFTING_SHARDS.length; i++) {
            int sx = AMBIENT_DRIFTING_SHARDS[i][0];
            int sy = AMBIENT_DRIFTING_SHARDS[i][1];
            int sz = AMBIENT_DRIFTING_SHARDS[i][2];

            setLocal(level, chunkBox, sx, sy, sz, voidStone);
            setLocal(level, chunkBox, sx, sy + 1, sz, (i % 2 == 0) ? cryingObsidian : obsidianBricks);
            setLocal(level, chunkBox, sx, sy + 2, sz, obsidian);
            placeCrystalIfAir(level, chunkBox, sx, sy + 3, sz, clusterUp);
            placeCrystalIfAir(level, chunkBox, sx, sy - 1, sz, (i % 2 == 0) ? budDown : clusterDown);
        }
    }

    private void buildCentralSanctuaryDaisAndRiftCrown(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block obsidianBricksBlock = resolveBlock("obsidian_bricks", Blocks.POLISHED_BLACKSTONE_BRICKS);
        Block voidStoneBlock = resolveBlock("void_stone", Blocks.DEEPSLATE);
        Block voidGlassBlock = resolveBlock("void_glass", Blocks.TINTED_GLASS);
        Block voidCrystalBlock = resolveBlock("void_crystal_block", Blocks.AMETHYST_BLOCK);
        Block voidCrystalClusterBlock = resolveBlock("void_crystal_cluster", Blocks.AMETHYST_CLUSTER);
        Block voidCrystalBudBlock = resolveBlock("void_crystal_bud", Blocks.LARGE_AMETHYST_BUD);
        Block focusBlock = resolveBlock("void_crystal_block", Blocks.CRYING_OBSIDIAN);
        Block phaseAnchorBlock = resolveBlock("spatial_phase_anchor", Blocks.LODESTONE);

        BlockState obsidianBricks = obsidianBricksBlock.defaultBlockState();
        BlockState voidStone = voidStoneBlock.defaultBlockState();
        BlockState voidGlass = voidGlassBlock.defaultBlockState();
        BlockState voidCrystal = voidCrystalBlock.defaultBlockState();
        BlockState cryingObsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState centralFocus = focusBlock.defaultBlockState();
        BlockState spatialPhaseAnchor = phaseAnchorBlock.defaultBlockState();
        BlockState clusterUp = orientCrystal(voidCrystalClusterBlock.defaultBlockState(), Direction.UP, false);
        BlockState clusterDown = orientCrystal(voidCrystalClusterBlock.defaultBlockState(), Direction.DOWN, false);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Floating Diamond Sanctuary Dais (X = 7..17, Z = 7..17, Y = 13..15)
        for (int lx = 7; lx <= 17; lx++) {
            for (int lz = 7; lz <= 17; lz++) {
                int dist = Math.abs(lx - 12) + Math.abs(lz - 12);
                if (dist > 5) {
                    continue;
                }

                boolean isRim = (dist == 5);
                boolean isVoidGlassOculus = (dist == 2 && (lx == 12 || lz == 12));
                boolean isCrystalRing = (dist == 2 && !isVoidGlassOculus) || dist == 0;

                BlockState deckState;
                if (isRim) {
                    deckState = obsidianBricks;
                } else if (isVoidGlassOculus) {
                    deckState = voidGlass;
                } else if (isCrystalRing) {
                    deckState = voidCrystal;
                } else {
                    deckState = (((lx + lz) & 1) == 0) ? obsidianBricks : voidStone;
                }
                setLocal(level, chunkBox, lx, 15, lz, deckState);

                // Tapered inverted floating obelisk keel (ly = 14 for dist <= 3, ly = 13 for dist <= 1)
                if (dist <= 3 && !isVoidGlassOculus) {
                    setLocal(level, chunkBox, lx, 14, lz, (dist == 3) ? obsidianBricks : voidStone);
                }
                if (dist <= 1) {
                    setLocal(level, chunkBox, lx, 13, lz, (dist == 0) ? voidCrystal : cryingObsidian);
                }
            }
        }
        // Suspended keel crystal tip pointing down toward the Reliquary skylight below
        placeCrystalIfAir(level, chunkBox, 12, 12, 12, clusterDown);

        // 2. Central Pedestal (12, 16, 12), Floating Void Crystal Focus (12, 18, 12),
        //    and Main Bastion Treasure Chest (12, 16, 9)
        setLocal(level, chunkBox, 12, 16, 12, voidCrystal);
        setLocal(level, chunkBox, 12, 17, 12, air);
        setLocal(level, chunkBox, 12, 18, 12, centralFocus);
        scheduleLocalTick(level, chunkBox, 12, 18, 12, focusBlock);

        // Main Sanctuary Treasure Chest on the North Dais facing SOUTH toward the central focus
        placeBastionChest(level, chunkBox, random, 12, 16, 9, Direction.SOUTH);

        // 3. Four Sweeping Obsidian-Bricks & Void-Glass Crown Ribs (Y = 16..24)
        //    converging at Spatial Phase Anchor (12, 24, 12) + Crystal Finial (12, 25, 12)
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            int dx = dir.getStepX();
            int dz = dir.getStepZ();

            // Radius 5 (Dais cardinal tips): Y = 16..18 obsidian_bricks + outward crystal bud
            setLocal(level, chunkBox, 12 + dx * 5, 16, 12 + dz * 5, obsidianBricks);
            setLocal(level, chunkBox, 12 + dx * 5, 17, 12 + dz * 5, cryingObsidian);
            setLocal(level, chunkBox, 12 + dx * 5, 18, 12 + dz * 5, obsidianBricks);
            placeCrystalIfAir(level, chunkBox,
                    12 + dx * 6, 17, 12 + dz * 6,
                    orientCrystal(voidCrystalBudBlock.defaultBlockState(), dir, false));

            // Radius 4: Y = 18..20 (Void Glass under-rib + Obsidian Bricks arch back)
            setLocal(level, chunkBox, 12 + dx * 4, 18, 12 + dz * 4, voidGlass);
            setLocal(level, chunkBox, 12 + dx * 4, 19, 12 + dz * 4, obsidianBricks);
            setLocal(level, chunkBox, 12 + dx * 4, 20, 12 + dz * 4, obsidianBricks);

            // Radius 3: Y = 20..22
            setLocal(level, chunkBox, 12 + dx * 3, 20, 12 + dz * 3, voidGlass);
            setLocal(level, chunkBox, 12 + dx * 3, 21, 12 + dz * 3, obsidianBricks);
            setLocal(level, chunkBox, 12 + dx * 3, 22, 12 + dz * 3, obsidianBricks);

            // Radius 2: Y = 22..23
            setLocal(level, chunkBox, 12 + dx * 2, 22, 12 + dz * 2, voidGlass);
            setLocal(level, chunkBox, 12 + dx * 2, 23, 12 + dz * 2, obsidianBricks);

            // Radius 1: Y = 23..24collar framing the Spatial Phase Anchor
            setLocal(level, chunkBox, 12 + dx, 23, 12 + dz, voidGlass);
            setLocal(level, chunkBox, 12 + dx, 24, 12 + dz, obsidianBricks);
        }

        // Apex Spatial Phase Anchor at (12, 24, 12) & Void Crystal Crown Tip at (12, 25, 12)
        setLocal(level, chunkBox, 12, 24, 12, spatialPhaseAnchor);
        scheduleLocalTick(level, chunkBox, 12, 24, 12, phaseAnchorBlock);
        setLocal(level, chunkBox, 12, 25, 12, clusterUp);
    }

    private void placeBastionChest(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int lx,
            int ly,
            int lz,
            Direction facing
    ) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, false);
        level.setBlock(pos, chestState, 2);
        RandomizableContainer.setBlockEntityLootTable(
                level, random, pos, ModStructures.NULL_FRINGE_RIFT_BASTION_CHEST_LOOT);
    }

    private void placeCrystalIfAir(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int lx,
            int ly,
            int lz,
            BlockState crystalState
    ) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir()) {
            level.setBlock(pos, crystalState, 2);
        }
    }

    private void scheduleLocalTick(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int lx,
            int ly,
            int lz,
            Block block
    ) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (chunkBox.isInside(pos)) {
            level.scheduleTick(pos, block, 2);
        }
    }

    private static BlockState stair(Direction facing, Half half) {
        return Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
    }

    private static BlockState orientCrystal(BlockState base, Direction facing, boolean waterlogged) {
        BlockState state = base;
        if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return state;
    }

    private static BlockState withBooleanProperty(BlockState state, String propName, boolean value) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty(propName);
        if (prop instanceof BooleanProperty boolProp && state.hasProperty(boolProp)) {
            return state.setValue(boolProp, value);
        }
        return state;
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private BlockPos toWorld(int lx, int ly, int lz) {
        return new BlockPos(this.startX + lx, this.startY + ly, this.startZ + lz);
    }

    private void setLocal(WorldGenLevel level, BoundingBox chunkBox, int lx, int ly, int lz, BlockState state) {
        BlockPos pos = toWorld(lx, ly, lz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillFoundationDown(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int lx,
            int startLy,
            int lz,
            BlockState state
    ) {
        int wx = this.startX + lx;
        int wz = this.startZ + lz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minAllowedY = Math.max(level.getMinY() + 2, this.startY + startLy - 24);
        for (int wy = this.startY + startLy; wy >= minAllowedY; wy--) {
            cursor.set(wx, wy, wz);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir()
                    || existing.is(Blocks.WATER)
                    || existing.is(Blocks.BUBBLE_COLUMN)
                    || existing.is(Blocks.SNOW)
                    || existing.is(Blocks.POWDER_SNOW)
                    || existing.is(Blocks.ICE)
                    || existing.canBeReplaced()) {
                level.setBlock(cursor, state, 2);
            } else {
                break;
            }
        }
    }
}
