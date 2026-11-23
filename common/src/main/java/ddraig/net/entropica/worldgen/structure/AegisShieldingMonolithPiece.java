package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;

import java.util.function.Supplier;

/**
 * Programmatic generator for Structure 2: Aegis Shielding Monolith (15 x 15 x 22 sanctuary):
 * <ul>
 *   <li>Triangular Stepped Dais ({@code dy = 0..2}, {@code 15x15} footprint) of Obsidian, Crying Obsidian,
 *       and Aegis-Plated Stone, ringed around the base with Aegis Spire Orchids and guarded on outer step
 *       corners by Shielding Bramble.</li>
 *   <li>3 Outer Obsidian Pylon Towers ({@code 3x3}, {@code 12} blocks tall at {@code (0,-6)}, {@code (-5,+4)},
 *       {@code (+5,+4)}), framed in Obsidian & Crying Obsidian with Bismuth Block trim and a central
 *       Bismuth Block focus at player eye height ({@code dy = 4}).</li>
 *   <li>Central Shrine & Floating Octahedron ({@code dy = 7..13}): 3 curving Copper Block / Cut Copper Stairs
 *       magnetic arches converging toward a floating octahedron of Aegis Crystal Block.</li>
 *   <li>Under-Dais Maintenance Room ({@code dy = -4..-1}): accessed by rear stairs, lined with Aegis-Plated
 *       Stone, Copper Blocks, Bismuth Block capacitors, Redstone Wire, and a locked Bismuth Key Pedestal
 *       directly adjacent to an Iron Door sealing off the Inner Vault Room & Loot Chest.</li>
 * </ul>
 */
public class AegisShieldingMonolithPiece extends StructurePiece {

    private static final int[][] OUTER_PYLON_CENTERS = {
            {0, -6},
            {-5, 4},
            {5, 4}
    };

    private static final int[][] CORNER_BRAMBLE_OFFSETS = {
            {-2, -6}, {2, -6},
            {-6, 2}, {-6, 5},
            {6, 2}, {6, 5}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;
    private transient long lastBeamGameTime = -1L;

    public AegisShieldingMonolithPiece(int originX, int baseY, int originZ) {
        // Exact 15 (W: -7..+7) x 22 (H: -5..+16) x 15 (L: -7..+7) bounding box
        super(ModStructures.AEGIS_SHIELDING_MONOLITH_PIECE.get(), 0,
                new BoundingBox(originX - 7, baseY - 5, originZ - 7, originX + 7, baseY + 16, originZ + 7));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public AegisShieldingMonolithPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.AEGIS_SHIELDING_MONOLITH_PIECE.get(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX()) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 5);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ()) / 2);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("BaseY", this.baseY);
        tag.putInt("OriginZ", this.originZ);
    }

    public int getOriginX() {
        return this.originX;
    }

    public int getBaseY() {
        return this.baseY;
    }

    public int getOriginZ() {
        return this.originZ;
    }

    public BlockPos getOctahedronCorePos() {
        return new BlockPos(this.originX, this.baseY + 10, this.originZ);
    }

    public BlockPos[] getOuterPylonCorePositions() {
        return new BlockPos[]{
                new BlockPos(this.originX + OUTER_PYLON_CENTERS[0][0], this.baseY + 4, this.originZ + OUTER_PYLON_CENTERS[0][1]),
                new BlockPos(this.originX + OUTER_PYLON_CENTERS[1][0], this.baseY + 4, this.originZ + OUTER_PYLON_CENTERS[1][1]),
                new BlockPos(this.originX + OUTER_PYLON_CENTERS[2][0], this.baseY + 4, this.originZ + OUTER_PYLON_CENTERS[2][1])
        };
    }

    public boolean tryClaimBeamTick(long gameTime) {
        if (this.lastBeamGameTime == gameTime) {
            return false;
        }
        this.lastBeamGameTime = gameTime;
        return true;
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
        buildFoundationAndClearAir(level, chunkBox);
        buildTriangularSteppedDais(level, chunkBox);
        buildUnderDaisMaintenanceAndVault(level, chunkBox, random);
        buildOuterObsidianPylons(level, chunkBox);
        buildCentralArchesAndFloatingOctahedron(level, chunkBox);
    }

    private static double triangleMetric(int dx, int dz) {
        return Math.max(dz - 0.5D, -0.5D * dz + 0.88D * Math.abs(dx) - 0.8D);
    }

    private static boolean isInPylonFootprint(int dx, int dz) {
        for (int[] p : OUTER_PYLON_CENTERS) {
            if (Math.abs(dx - p[0]) <= 1 && Math.abs(dz - p[1]) <= 1) {
                return true;
            }
        }
        return false;
    }

    private void buildFoundationAndClearAir(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState platedStone = resolveBlock("aegis_plated_stone", () -> ModBlocks.AEGIS_PLATED_STONE.get(), Blocks.DEEPSLATE_TILES)
                .defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double tri = triangleMetric(dx, dz);
                boolean inSanctuary = tri <= 6.6D || isInPylonFootprint(dx, dz);
                if (!inSanctuary) {
                    continue;
                }

                // Clear open sky headroom above ground across the sanctuary (dy = 0..16)
                for (int dy = 0; dy <= 16; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }

                // Solid obsidian / aegis_plated_stone sub-structure & downward terrain anchoring
                if (tri <= 5.6D || isInPylonFootprint(dx, dz)) {
                    BlockState foundationState = (((dx + dz) & 1) == 0) ? obsidian : platedStone;
                    for (int dy = -5; dy <= -1; dy++) {
                        setBlock(level, chunkBox, dx, dy, dz, foundationState);
                    }
                    fillFoundationDown(level, chunkBox, dx, -6, dz, foundationState);
                }
            }
        }
    }

    private void buildTriangularSteppedDais(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState cryingObsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState platedStone = resolveBlock("aegis_plated_stone", () -> ModBlocks.AEGIS_PLATED_STONE.get(), Blocks.DEEPSLATE_TILES)
                .defaultBlockState();
        BlockState bismuthBlock = resolveBlock("bismuth_block", () -> ModBlocks.BISMUTH_BLOCK.get(), Blocks.AMETHYST_BLOCK)
                .defaultBlockState();
        BlockState quartzBlock = resolveBlock("aegis_quartz_block", () -> ModBlocks.AEGIS_QUARTZ_BLOCK.get(), Blocks.QUARTZ_BLOCK)
                .defaultBlockState();
        BlockState glimmerIce = resolveBlock("glimmer_ice", () -> ModBlocks.GLIMMER_ICE.get(), Blocks.PACKED_ICE)
                .defaultBlockState();
        BlockState orchid = resolveBlock("aegis_spire_orchid", () -> ModBlocks.AEGIS_SPIRE_ORCHID.get(), Blocks.BLUE_ORCHID)
                .defaultBlockState();
        BlockState bramble = resolveBlock("shielding_bramble", () -> ModBlocks.SHIELDING_BRAMBLE.get(), Blocks.DEAD_BUSH)
                .defaultBlockState();

        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double tri = triangleMetric(dx, dz);

                // Tier 0 (dy = 0): Broad 15x15 triangular base platform
                if (tri <= 5.5D || isInPylonFootprint(dx, dz)) {
                    boolean rim0 = tri > 4.6D;
                    BlockState tier0State = rim0
                            ? ((((dx ^ dz) & 3) == 0) ? cryingObsidian : obsidian)
                            : platedStone;
                    setBlock(level, chunkBox, dx, 0, dz, tier0State);
                }

                // Tier 1 (dy = 1): Stepped middle terrace
                if (tri <= 3.8D) {
                    boolean rim1 = tri > 2.9D;
                    BlockState tier1State = rim1
                            ? (((Math.abs(dx) + Math.abs(dz)) % 4 == 0) ? cryingObsidian : obsidian)
                            : platedStone;
                    setBlock(level, chunkBox, dx, 1, dz, tier1State);
                }

                // Tier 2 (dy = 2): Upper central sanctuary altar deck
                if (tri <= 2.2D) {
                    boolean isFocalCenter = (dx == 0 && dz == 0);
                    boolean isFocalRing = (Math.abs(dx) <= 1 && Math.abs(dz) <= 1 && !isFocalCenter);
                    BlockState tier2State;
                    if (isFocalCenter) {
                        tier2State = bismuthBlock;
                    } else if (isFocalRing) {
                        tier2State = ((dx == 0 || dz == 0) ? cryingObsidian : platedStone);
                    } else if (tri > 1.5D) {
                        tier2State = obsidian;
                    } else {
                        tier2State = platedStone;
                    }
                    setBlock(level, chunkBox, dx, 2, dz, tier2State);
                }

                // Ring around the outer base of the dais with Aegis Spire Orchids
                if (tri > 5.55D && tri <= 6.45D && !isInPylonFootprint(dx, dz) && !(Math.abs(dx) <= 1 && dz >= 5)) {
                    if (((dx * 3 + dz * 5) & 1) == 0) {
                        BlockState substrate = (((dx + dz) & 3) == 0) ? quartzBlock
                                : ((((dx ^ dz) & 1) == 0) ? Blocks.SNOW_BLOCK.defaultBlockState() : glimmerIce);
                        setBlock(level, chunkBox, dx, -1, dz, substrate);
                        fillFoundationDown(level, chunkBox, dx, -2, dz, Blocks.PACKED_ICE.defaultBlockState());
                        BlockPos flowerPos = new BlockPos(this.originX + dx, this.baseY, this.originZ + dz);
                        if (chunkBox.isInside(flowerPos) && orchid.canSurvive(level, flowerPos)) {
                            level.setBlock(flowerPos, orchid, 2);
                        }
                    }
                }
            }
        }

        // Approach steps on the NW and NE flanks so players climb smoothly onto the dy = 2 deck
        setBlock(level, chunkBox, -3, 1, -2, Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.BOTTOM));
        setBlock(level, chunkBox, -2, 2, -1, Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.BOTTOM));

        setBlock(level, chunkBox, 3, 1, -2, Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.BOTTOM));
        setBlock(level, chunkBox, 2, 2, -1, Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.BOTTOM));

        // Guard outer step corners with Shielding Bramble
        for (int[] corner : CORNER_BRAMBLE_OFFSETS) {
            int bx = corner[0];
            int bz = corner[1];
            if (isInPylonFootprint(bx, bz)) {
                continue;
            }
            setBlock(level, chunkBox, bx, 0, bz, obsidian);
            BlockPos bramblePos = new BlockPos(this.originX + bx, this.baseY + 1, this.originZ + bz);
            if (chunkBox.isInside(bramblePos) && bramble.canSurvive(level, bramblePos)) {
                level.setBlock(bramblePos, bramble, 2);
            }
        }
    }

    private void buildOuterObsidianPylons(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState cryingObsidian = Blocks.CRYING_OBSIDIAN.defaultBlockState();
        BlockState platedStone = resolveBlock("aegis_plated_stone", () -> ModBlocks.AEGIS_PLATED_STONE.get(), Blocks.DEEPSLATE_TILES)
                .defaultBlockState();
        BlockState bismuthBlock = resolveBlock("bismuth_block", () -> ModBlocks.BISMUTH_BLOCK.get(), Blocks.AMETHYST_BLOCK)
                .defaultBlockState();
        Block pylonCoreBlock = resolveBlock("bismuth_block", () -> ModBlocks.BISMUTH_BLOCK.get(), Blocks.COPPER_BLOCK);
        BlockState activePylonCore = pylonCoreBlock.defaultBlockState();

        Block clusterBlock = resolveBlock("aegis_quartz_cluster", () -> ModBlocks.AEGIS_QUARTZ_CLUSTER.get(), Blocks.AMETHYST_CLUSTER);
        BlockState topCluster = clusterBlock.defaultBlockState();
        if (topCluster.hasProperty(BlockStateProperties.FACING)) {
            topCluster = topCluster.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        if (topCluster.hasProperty(BlockStateProperties.WATERLOGGED)) {
            topCluster = topCluster.setValue(BlockStateProperties.WATERLOGGED, false);
        }

        BlockState air = Blocks.AIR.defaultBlockState();

        // Each outer pylon is 3x3 wide and 12 blocks tall (dy = 1..12)
        for (int[] center : OUTER_PYLON_CENTERS) {
            int px = center[0];
            int pz = center[1];

            // Ensure solid plinth at dy = 0..2 beneath the pylon
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    boolean isCorner = (Math.abs(ox) == 1 && Math.abs(oz) == 1);
                    setBlock(level, chunkBox, px + ox, 0, pz + oz, obsidian);
                    setBlock(level, chunkBox, px + ox, 1, pz + oz, isCorner ? obsidian : platedStone);
                    setBlock(level, chunkBox, px + ox, 2, pz + oz, isCorner ? bismuthBlock : obsidian);
                }
            }

            // Eye-height core chamber (dy = 3..5): open cardinal alcoves so players can click the dy = 4 core!
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    boolean isCorner = (Math.abs(ox) == 1 && Math.abs(oz) == 1);
                    boolean isCenter = (ox == 0 && oz == 0);
                    if (isCorner) {
                        setBlock(level, chunkBox, px + ox, 3, pz + oz, obsidian);
                        setBlock(level, chunkBox, px + ox, 4, pz + oz, cryingObsidian);
                        setBlock(level, chunkBox, px + ox, 5, pz + oz, obsidian);
                    } else if (isCenter) {
                        setBlock(level, chunkBox, px, 3, pz, platedStone);
                        setBlock(level, chunkBox, px, 4, pz, activePylonCore);
                        setBlock(level, chunkBox, px, 5, pz, bismuthBlock);
                    } else {
                        // Open cardinal windows at dy = 3..4 for unobstructed eye-level interaction
                        setBlock(level, chunkBox, px + ox, 3, pz + oz, air);
                        setBlock(level, chunkBox, px + ox, 4, pz + oz, air);
                        Direction outward = (ox != 0)
                                ? (ox > 0 ? Direction.EAST : Direction.WEST)
                                : (oz > 0 ? Direction.SOUTH : Direction.NORTH);
                        setBlock(level, chunkBox, px + ox, 5, pz + oz, Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, outward.getOpposite())
                                .setValue(StairBlock.HALF, Half.TOP));
                    }
                }
            }

            // Upper Obsidian & Crying Obsidian shaft with Bismuth Block trim (dy = 6..10)
            for (int dy = 6; dy <= 10; dy++) {
                for (int ox = -1; ox <= 1; ox++) {
                    for (int oz = -1; oz <= 1; oz++) {
                        boolean isCorner = (Math.abs(ox) == 1 && Math.abs(oz) == 1);
                        boolean isCenter = (ox == 0 && oz == 0);
                        if (isCenter) {
                            setBlock(level, chunkBox, px, dy, pz, cryingObsidian);
                        } else if (isCorner) {
                            setBlock(level, chunkBox, px + ox, dy, pz + oz, (dy == 6 || dy == 10) ? bismuthBlock : obsidian);
                        } else {
                            setBlock(level, chunkBox, px + ox, dy, pz + oz, (dy == 8) ? cryingObsidian : platedStone);
                        }
                    }
                }
            }

            // Tapered Bismuth & Obsidian Pylon Crown (dy = 11..12 -> 12 blocks tall total from dy = 1..12)
            setBlock(level, chunkBox, px, 11, pz, bismuthBlock);
            for (Direction card : Direction.Plane.HORIZONTAL) {
                setBlock(level, chunkBox, px + card.getStepX(), 11, pz + card.getStepZ(), obsidian);
            }
            setBlock(level, chunkBox, px, 12, pz, obsidian);
            // Optional crystal finial at dy = 13 above the 12-block tower
            setBlock(level, chunkBox, px, 13, pz, topCluster);
        }
    }

    private void buildCentralArchesAndFloatingOctahedron(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState copperBlock = Blocks.COPPER_BLOCK.defaultBlockState();
        Block wardingBlock = resolveBlock("aegis_crystal_block", () -> ModBlocks.AEGIS_CRYSTAL_BLOCK.get(), Blocks.AMETHYST_BLOCK);
        BlockState octahedronState = wardingBlock.defaultBlockState();

        // 1. Three Curving Copper Block / Cut Copper Stairs Magnetic Arches (dy = 7..13)
        // North Arch (along dx = 0, dz = -4..-1)
        setBlock(level, chunkBox, 0, 7, -4, copperBlock);
        setBlock(level, chunkBox, 0, 8, -4, copperStair(Direction.NORTH, Half.BOTTOM));
        setBlock(level, chunkBox, 0, 9, -3, copperBlock);
        setBlock(level, chunkBox, 0, 10, -3, copperStair(Direction.NORTH, Half.BOTTOM));
        setBlock(level, chunkBox, 0, 11, -2, copperBlock);
        setBlock(level, chunkBox, 0, 12, -2, copperStair(Direction.NORTH, Half.BOTTOM));
        setBlock(level, chunkBox, 0, 13, -1, copperStair(Direction.SOUTH, Half.TOP));

        // South-West Arch (along (-4, +3) -> (-1, +1))
        setBlock(level, chunkBox, -4, 7, 3, copperBlock);
        setBlock(level, chunkBox, -4, 8, 3, copperStair(Direction.WEST, Half.BOTTOM));
        setBlock(level, chunkBox, -3, 9, 2, copperBlock);
        setBlock(level, chunkBox, -3, 10, 2, copperStair(Direction.WEST, Half.BOTTOM));
        setBlock(level, chunkBox, -2, 11, 2, copperBlock);
        setBlock(level, chunkBox, -2, 12, 2, copperStair(Direction.SOUTH, Half.BOTTOM));
        setBlock(level, chunkBox, -1, 13, 1, copperStair(Direction.EAST, Half.TOP));

        // South-East Arch (along (+4, +3) -> (+1, +1))
        setBlock(level, chunkBox, 4, 7, 3, copperBlock);
        setBlock(level, chunkBox, 4, 8, 3, copperStair(Direction.EAST, Half.BOTTOM));
        setBlock(level, chunkBox, 3, 9, 2, copperBlock);
        setBlock(level, chunkBox, 3, 10, 2, copperStair(Direction.EAST, Half.BOTTOM));
        setBlock(level, chunkBox, 2, 11, 2, copperBlock);
        setBlock(level, chunkBox, 2, 12, 2, copperStair(Direction.SOUTH, Half.BOTTOM));
        setBlock(level, chunkBox, 1, 13, 1, copperStair(Direction.WEST, Half.TOP));

        // 2. Floating 3D Octahedron of Aegis Crystal Block at dy = 8..12
        // Suspended cleanly in mid-air inside the 3 magnetic copper arches!
        for (int dy = 8; dy <= 12; dy++) {
            int maxManhattan = 2 - Math.abs(dy - 10); // 0 at dy=8,12; 1 at dy=9,11; 2 at dy=10
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    int horizDist = Math.abs(dx) + Math.abs(dz);
                    // Keep dy=10 as a crisp 3x3 cross/diamond (|dx|<=1 && |dz|<=1 && horizDist <= 2, excluding (+/-2,0)/(0,+/-2) so arches never touch!)
                    if (horizDist <= maxManhattan && Math.abs(dx) <= 1 && Math.abs(dz) <= 1) {
                        BlockPos wPos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
                        if (chunkBox.isInside(wPos)) {
                            level.setBlock(wPos, octahedronState, 2);
                        }
                    }
                }
            }
        }
    }

    private void buildUnderDaisMaintenanceAndVault(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        BlockState obsidian = Blocks.OBSIDIAN.defaultBlockState();
        BlockState platedStone = resolveBlock("aegis_plated_stone", () -> ModBlocks.AEGIS_PLATED_STONE.get(), Blocks.DEEPSLATE_TILES)
                .defaultBlockState();
        BlockState copperBlock = Blocks.COPPER_BLOCK.defaultBlockState();
        BlockState bismuthBlock = resolveBlock("bismuth_block", () -> ModBlocks.BISMUTH_BLOCK.get(), Blocks.AMETHYST_BLOCK)
                .defaultBlockState();
        BlockState quartzBlock = resolveBlock("aegis_quartz_block", () -> ModBlocks.AEGIS_QUARTZ_BLOCK.get(), Blocks.QUARTZ_BLOCK)
                .defaultBlockState();

        Block clusterBlock = resolveBlock("aegis_quartz_cluster", () -> ModBlocks.AEGIS_QUARTZ_CLUSTER.get(), Blocks.AMETHYST_CLUSTER);
        BlockState upCluster = clusterBlock.defaultBlockState();
        if (upCluster.hasProperty(BlockStateProperties.FACING)) {
            upCluster = upCluster.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        if (upCluster.hasProperty(BlockStateProperties.WATERLOGGED)) {
            upCluster = upCluster.setValue(BlockStateProperties.WATERLOGGED, false);
        }

        Block pedestalBlock = resolveBlock("bismuth_key_pedestal", () -> ModBlocks.BISMUTH_KEY_PEDESTAL.get(), Blocks.CHISELED_STONE_BRICKS);
        BlockState lockedPedestal = withBooleanProperty(pedestalBlock.defaultBlockState(), "unlocked", false);

        Block aegisPlatingBlock = resolveBlock("conductive_aegis_plating", () -> ModBlocks.CONDUCTIVE_AEGIS_PLATING.get(), Blocks.COPPER_BLOCK);
        BlockState rearVaultPlating = aegisPlatingBlock.defaultBlockState();

        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Sealed Outer Shell for Maintenance Room + Inner Vault (dx = -4..+4, dz = -6..+2, dy = -5..-1)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -6; dz <= 2; dz++) {
                setBlock(level, chunkBox, dx, -5, dz, platedStone);
                setBlock(level, chunkBox, dx, -1, dz, platedStone);
                for (int dy = -4; dy <= -2; dy++) {
                    boolean isOuterWall = (Math.abs(dx) == 4 || dz == -6 || dz == 2);
                    setBlock(level, chunkBox, dx, dy, dz, isOuterWall ? platedStone : air);
                }
            }
        }

        // 2. Partition Bulkhead at dz = -2 separating Maintenance Room (dz = -1..+1) from Inner Vault (dz = -5..-3)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -4; dy <= -2; dy++) {
                setBlock(level, chunkBox, dx, dy, -2, (Math.abs(dx) <= 1) ? obsidian : platedStone);
            }
        }

        // Backing wall behind the Bismuth Key Pedestal niche at (-1, -4..-2, -3) so the vault stays 100% sealed!
        for (int dy = -4; dy <= -2; dy++) {
            setBlock(level, chunkBox, -1, dy, -3, obsidian);
            setBlock(level, chunkBox, -2, dy, -3, platedStone);
        }

        // Iron Door (lower + upper halves, closed & unpowered) at (0, -4..-3, -2)
        BlockState doorLower = Blocks.IRON_DOOR.defaultBlockState()
                .setValue(DoorBlock.FACING, Direction.SOUTH)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.HINGE, DoorHingeSide.LEFT)
                .setValue(DoorBlock.OPEN, false)
                .setValue(DoorBlock.POWERED, false);
        BlockState doorUpper = doorLower.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER);
        setBlock(level, chunkBox, 0, -4, -2, doorLower);
        setBlock(level, chunkBox, 0, -3, -2, doorUpper);
        setBlock(level, chunkBox, 0, -2, -2, obsidian);

        // Bismuth Key Pedestal (unlocked = false) directly adjacent at (-1, -4, -2) powering the Iron Door!
        setBlock(level, chunkBox, -1, -4, -2, lockedPedestal);
        setBlock(level, chunkBox, -1, -3, -2, obsidian);
        setBlock(level, chunkBox, -1, -2, -2, obsidian);

        // 3. Maintenance Room Capacitors, Copper Busbars & Redstone Wire Conduits (dz = -1..+1, dx = -3..+3)
        for (int side : new int[]{-3, 3}) {
            setBlock(level, chunkBox, side, -4, 1, copperBlock);
            setBlock(level, chunkBox, side, -3, 1, copperBlock);
            setBlock(level, chunkBox, side, -4, 0, bismuthBlock);
            setBlock(level, chunkBox, side, -3, 0, bismuthBlock);
            setBlock(level, chunkBox, side, -2, 0, upCluster);
            setBlock(level, chunkBox, side, -4, -1, copperBlock);
        }

        // Connected Redstone Wire floor traces linking West & East capacitor banks to the Bismuth Key Pedestal
        BlockState wireEW = Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE);
        BlockState wireWestCorner = Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE);
        BlockState wirePedestalFeed = Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE);
        BlockState wireEastCorner = Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(RedStoneWireBlock.EAST, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.NORTH, RedstoneSide.SIDE);
        BlockState wireEastNorth = Blocks.REDSTONE_WIRE.defaultBlockState()
                .setValue(RedStoneWireBlock.SOUTH, RedstoneSide.SIDE)
                .setValue(RedStoneWireBlock.WEST, RedstoneSide.SIDE);

        setBlock(level, chunkBox, -2, -4, 0, wireEW);
        setBlock(level, chunkBox, -1, -4, 0, wireWestCorner);
        setBlock(level, chunkBox, -1, -4, -1, wirePedestalFeed);
        setBlock(level, chunkBox, 2, -4, 0, wireEW);
        setBlock(level, chunkBox, 1, -4, 0, wireEastCorner);
        setBlock(level, chunkBox, 1, -4, -1, wireEastNorth);

        // 4. Inner Vault Room (dz = -5..-3) housing the Structure 2 Loot Chest
        setBlock(level, chunkBox, 0, -5, -5, bismuthBlock);
        placeVaultLootChest(level, chunkBox, random, 0, -4, -5, Direction.SOUTH);

        // Crystalline vault sconces & Conductive Aegis Plating accent in the rear wall
        setBlock(level, chunkBox, -2, -4, -5, quartzBlock);
        setBlock(level, chunkBox, -2, -3, -5, upCluster);
        setBlock(level, chunkBox, 2, -4, -5, quartzBlock);
        setBlock(level, chunkBox, 2, -3, -5, upCluster);
        setBlock(level, chunkBox, 0, -3, -6, rearVaultPlating);

        // 5. Rear Stairwell from the South edge of the Dais (dz = +6 -> +2) descending cleanly into (0, -4, +1)
        // Portal frame at the rear of the dais (dz = +6, dx = -1..+1)
        setBlock(level, chunkBox, -1, 0, 6, obsidian);
        setBlock(level, chunkBox, 1, 0, 6, obsidian);
        setBlock(level, chunkBox, -1, 1, 6, platedStone);
        setBlock(level, chunkBox, 1, 1, 6, platedStone);
        setBlock(level, chunkBox, -1, 2, 6, obsidian);
        setBlock(level, chunkBox, 0, 2, 6, bismuthBlock);
        setBlock(level, chunkBox, 1, 2, 6, obsidian);
        setBlock(level, chunkBox, 0, 0, 6, platedStone);
        setBlock(level, chunkBox, 0, 1, 6, air);

        // Enclose stairwell side walls (dx = -1, +1 for dz = +2..+5)
        for (int sz = 2; sz <= 5; sz++) {
            for (int sy = -5; sy <= 1; sy++) {
                if (sz >= 4 || sy <= -1) {
                    setBlock(level, chunkBox, -1, sy, sz, platedStone);
                    setBlock(level, chunkBox, 1, sy, sz, platedStone);
                }
            }
        }

        // 4 descending steps with 3 full blocks of headroom above each step!
        for (int step = 0; step < 4; step++) {
            int sz = 5 - step;       // 5, 4, 3, 2
            int stairY = -1 - step;  // -1, -2, -3, -4
            setBlock(level, chunkBox, 0, stairY - 1, sz, platedStone);
            setBlock(level, chunkBox, 0, stairY, sz, copperStair(Direction.SOUTH, Half.BOTTOM));
            setBlock(level, chunkBox, 0, stairY + 1, sz, air);
            setBlock(level, chunkBox, 0, stairY + 2, sz, air);
            setBlock(level, chunkBox, 0, stairY + 3, sz, air);
        }
        // Clear doorway from bottom step (0, -4, +2) into Maintenance Room (0, -4..-2, +1)
        setBlock(level, chunkBox, 0, -3, 2, air);
        setBlock(level, chunkBox, 0, -2, 2, air);
    }

    private void placeVaultLootChest(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, false);
        level.setBlock(pos, chestState, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.STRUCTURE_2_CHEST_LOOT);
    }

    private static BlockState copperStair(Direction facing, Half half) {
        return Blocks.CUT_COPPER_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
    }

    private static BlockState withBooleanProperty(BlockState state, String propName, boolean value) {
        Property<?> prop = state.getBlock().getStateDefinition().getProperty(propName);
        if (prop instanceof BooleanProperty boolProp && state.hasProperty(boolProp)) {
            return state.setValue(boolProp, value);
        }
        return state;
    }

    private static Block resolveBlock(String path, Supplier<Block> modSupplier, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        var opt = BuiltInRegistries.BLOCK.getOptional(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        try {
            Block b = modSupplier.get();
            if (b != null) {
                return b;
            }
        } catch (Throwable ignored) {
        }
        return fallback;
    }

    private void setBlock(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillFoundationDown(WorldGenLevel level, BoundingBox chunkBox, int dx, int startDy, int dz, BlockState state) {
        int wx = this.originX + dx;
        int wz = this.originZ + dz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 18);
        for (int wy = this.baseY + startDy; wy >= minAllowedY; wy--) {
            cursor.set(wx, wy, wz);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir()
                    || existing.is(Blocks.WATER)
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
