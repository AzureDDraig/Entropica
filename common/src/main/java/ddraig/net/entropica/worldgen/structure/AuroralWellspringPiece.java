package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.material.Fluids;

/**
 * Programmatic generator for Structure 3: Auroral Wellspring (24 x 24 x 12 circular geothermal
 * sanctuary anchored at sea level Y=63 in {@code 2.2 Auroral Skerries} with three concentric
 * Smooth Quartz & Quartz Bricks ring basins, central Auroral Lens & Vis Vitae Anchor core,
 * opposing quartz loot niches, four converging quartz arches with oxidized/weathered copper domes,
 * and four hidden vapor-buff dispensers).
 */
public class AuroralWellspringPiece extends StructurePiece {

    private static final int[][] LILY_PAD_OFFSETS = {
            {7, 0}, {-7, 0}, {0, 7}, {0, -7},
            {5, 5}, {-5, 5}, {5, -5}, {-5, -5}
    };

    private static final int[][] DIAGONAL_PIERS = {
            {-4, -4}, {4, -4}, {-4, 4}, {4, 4}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;
    private transient long lastVaporPulseGameTime = -1L;

    public AuroralWellspringPiece(int originX, int baseY, int originZ) {
        super(ModStructures.AURORAL_WELLSPRING_PIECE.get(), 0,
                new BoundingBox(originX - 12, baseY - 5, originZ - 12, originX + 11, baseY + 11, originZ + 11));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public AuroralWellspringPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.AURORAL_WELLSPRING_PIECE.get(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX() + 1) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 5);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ() + 1) / 2);
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

    public BlockPos getCoreWaterPos() {
        return new BlockPos(this.originX, this.baseY, this.originZ);
    }

    public BlockPos getCoreLensPos() {
        return new BlockPos(this.originX, this.baseY + 2, this.originZ);
    }

    public boolean tryClaimVaporPulseTick(long gameTime) {
        if (this.lastVaporPulseGameTime == gameTime) {
            return false;
        }
        this.lastVaporPulseGameTime = gameTime;
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
        buildConcentricRingBasins(level, chunkBox);
        buildCorePedestalAndChests(level, chunkBox, random);
        buildArchedGazeboAndDispensers(level, chunkBox);
    }

    private void buildFoundationAndClearAir(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState quartzBricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        BlockState coarseGravel = ModBlocks.COARSE_GRAVEL.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -12; dx <= 11; dx++) {
            for (int dz = -12; dz <= 11; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 11.6D) {
                    continue;
                }

                // Clear open sky headroom above sea level (dy = 0..11)
                for (int dy = 0; dy <= 11; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }

                // Watertight sub-floor seal at dy = -4 and solid downward columns into seabed
                setBlock(level, chunkBox, dx, -4, dz, (r > 10.6D) ? quartzBricks : smoothQuartz);
                BlockState colState = (r > 10.4D) ? quartzBricks
                        : (((dx + dz) & 3) == 0 ? smoothQuartz : quartzBricks);
                fillFoundationDown(level, chunkBox, dx, -5, dz, colState);

                // Skirt coarse_gravel aprons around the outer rim at dy = -2..-1 where bordering open seabed
                if (r > 11.0D && r <= 11.6D) {
                    setBlock(level, chunkBox, dx, -3, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -2, dz, ((dx ^ dz) & 1) == 0 ? quartzBricks : coarseGravel);
                }
            }
        }
    }

    private void buildConcentricRingBasins(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState quartzBricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        BlockState coarseGravel = ModBlocks.COARSE_GRAVEL.get().defaultBlockState();
        BlockState glimmerIce = ModBlocks.GLIMMER_ICE.get().defaultBlockState();
        BlockState featherMoss = ModBlocks.AURORAL_FEATHER_MOSS.get().defaultBlockState();
        BlockState lilyPad = ModBlocks.AURORAL_LILY_PAD.get().defaultBlockState();
        BlockState pottedFern = ModBlocks.POTTED_MIST_VEIL_FERN.get().defaultBlockState();
        BlockState mistFern = ModBlocks.MIST_VEIL_FERN.get().defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState magma = Blocks.MAGMA_BLOCK.defaultBlockState();
        BlockState soulSand = Blocks.SOUL_SAND.defaultBlockState();
        BlockState risingBubbles = Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN, false);
        BlockState magmaBubbles = Blocks.BUBBLE_COLUMN.defaultBlockState().setValue(BubbleColumnBlock.DRAG_DOWN, true);

        for (int dx = -12; dx <= 11; dx++) {
            for (int dz = -12; dz <= 11; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 11.5D || r < 1.75D) {
                    continue;
                }

                boolean onCardinalBridge = (Math.abs(dx) <= 1 && Math.abs(dz) >= 5 && Math.abs(dz) <= 11)
                        || (Math.abs(dz) <= 1 && Math.abs(dx) >= 5 && Math.abs(dx) <= 11);
                boolean bridgeCenterLine = (dx == 0 && Math.abs(dz) >= 5 && Math.abs(dz) <= 10)
                        || (dz == 0 && Math.abs(dx) >= 5 && Math.abs(dx) <= 10);

                if (r >= 10.8D) {
                    // Outer Rim Curb (r = 10.8..11.5)
                    setBlock(level, chunkBox, dx, -3, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -2, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -1, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, 0, dz, ((dx + dz) & 1) == 0 ? smoothQuartz : quartzBricks);
                } else if (r >= 9.0D) {
                    // Outer Ring Basin (r = 9..11): Cold channel water
                    setBlock(level, chunkBox, dx, -3, dz, coarseGravel);
                    if (onCardinalBridge) {
                        setBlock(level, chunkBox, dx, -2, dz, bridgeCenterLine ? water : quartzBricks);
                        setBlock(level, chunkBox, dx, -1, dz, quartzBricks);
                        setBlock(level, chunkBox, dx, 0, dz, smoothQuartz);
                        if (bridgeCenterLine) {
                            setBlock(level, chunkBox, dx, 1, dz, featherMoss);
                        }
                    } else {
                        setBlock(level, chunkBox, dx, -2, dz, water);
                        setBlock(level, chunkBox, dx, -1, dz, water);
                    }
                } else if (r > 8.0D) {
                    // Outer-Middle Dividing Curb Walkway (r = 8..9)
                    setBlock(level, chunkBox, dx, -3, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -2, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -1, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, 0, dz, smoothQuartz);
                    if (((dx + dz) & 1) == 0) {
                        setBlock(level, chunkBox, dx, 1, dz, featherMoss);
                    }
                } else if (r >= 6.0D) {
                    // Middle Ring Basin (r = 6..8): Glimmer-Ice & Auroral Lily-Pads
                    setBlock(level, chunkBox, dx, -3, dz, glimmerIce);
                    if (onCardinalBridge) {
                        setBlock(level, chunkBox, dx, -2, dz, bridgeCenterLine ? water : glimmerIce);
                        setBlock(level, chunkBox, dx, -1, dz, quartzBricks);
                        setBlock(level, chunkBox, dx, 0, dz, smoothQuartz);
                        if (bridgeCenterLine) {
                            setBlock(level, chunkBox, dx, 1, dz, featherMoss);
                        }
                    } else {
                        boolean glimmerEdge = r < 6.45D || r > 7.55D;
                        setBlock(level, chunkBox, dx, -2, dz, glimmerEdge ? glimmerIce : water);
                        boolean surfaceIceShelf = glimmerEdge && (((dx * 3 + dz * 5) & 7) == 0);
                        setBlock(level, chunkBox, dx, -1, dz, surfaceIceShelf ? glimmerIce : water);
                    }
                } else if (r > 4.35D) {
                    // Inner Sanctuary Terrace Walkway (r = 4.35..6.0)
                    setBlock(level, chunkBox, dx, -3, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -2, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, -1, dz, quartzBricks);
                    setBlock(level, chunkBox, dx, 0, dz, (r > 5.4D) ? quartzBricks : smoothQuartz);
                    if (((dx ^ dz) & 1) == 0 && !(Math.abs(dx) >= 3 && Math.abs(dz) >= 3)) {
                        setBlock(level, chunkBox, dx, 1, dz, featherMoss);
                    }
                } else {
                    // Inner Thermal Pool (r = 1.75..4.35): Warm bubbling Magma / Soul Sand floor under water
                    boolean isRimWall = r > 3.95D;
                    if (isRimWall) {
                        setBlock(level, chunkBox, dx, -3, dz, quartzBricks);
                        setBlock(level, chunkBox, dx, -2, dz, quartzBricks);
                        setBlock(level, chunkBox, dx, -1, dz, water);
                    } else {
                        boolean useSoulSand = ((dx + dz) & 1) == 0;
                        setBlock(level, chunkBox, dx, -3, dz, useSoulSand ? soulSand : magma);
                        BlockState col = useSoulSand ? risingBubbles : magmaBubbles;
                        setBlock(level, chunkBox, dx, -2, dz, col);
                        setBlock(level, chunkBox, dx, -1, dz, col);
                        BlockPos bedPos = new BlockPos(this.originX + dx, this.baseY - 3, this.originZ + dz);
                        if (chunkBox.isInside(bedPos)) {
                            level.scheduleTick(bedPos, useSoulSand ? Blocks.SOUL_SAND : Blocks.MAGMA_BLOCK, 2);
                        }
                    }
                }
            }
        }

        // Welcoming outer cardinal Smooth Quartz Stairs at r = 11
        for (int s = -1; s <= 1; s++) {
            setBlock(level, chunkBox, s, 0, -11, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.BOTTOM));
            setBlock(level, chunkBox, s, 0, 11, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.BOTTOM));
            setBlock(level, chunkBox, -11, 0, s, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.BOTTOM));
            setBlock(level, chunkBox, 11, 0, s, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.BOTTOM));
        }

        // Submerged waterlogged Smooth Quartz steps leading smoothly out of the Inner Thermal Pool onto the Y=63 terrace
        setBlock(level, chunkBox, 0, -1, -4, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.NORTH).setValue(StairBlock.HALF, Half.BOTTOM).setValue(StairBlock.WATERLOGGED, true));
        setBlock(level, chunkBox, 0, -1, 4, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.BOTTOM).setValue(StairBlock.WATERLOGGED, true));
        setBlock(level, chunkBox, -4, -1, 0, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST).setValue(StairBlock.HALF, Half.BOTTOM).setValue(StairBlock.WATERLOGGED, true));
        setBlock(level, chunkBox, 4, -1, 0, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST).setValue(StairBlock.HALF, Half.BOTTOM).setValue(StairBlock.WATERLOGGED, true));

        // Floating Auroral Lily-Pads on the Middle Ring water (dy = 0 above dy = -1 water)
        for (int[] lp : LILY_PAD_OFFSETS) {
            BlockPos waterBelow = new BlockPos(this.originX + lp[0], this.baseY - 1, this.originZ + lp[1]);
            BlockPos padPos = waterBelow.above();
            if (chunkBox.isInside(padPos)) {
                level.setBlock(waterBelow, water, 2);
                level.setBlock(padPos, lilyPad, 2);
            }
        }

        // Inner Sanctuary Promenade accents: Glimmer-Ice nodes & Potted Mist-Veil Ferns
        int[][] wellPositions = {{5, 2}, {-5, -2}, {-2, 5}, {2, -5}};
        for (int[] wp : wellPositions) {
            setBlock(level, chunkBox, wp[0], 0, wp[1], smoothQuartz);
            setBlock(level, chunkBox, wp[0], 1, wp[1], ModBlocks.GLIMMER_ICE.get().defaultBlockState());
        }

        int[][] pottedPositions = {{-5, 2}, {5, -2}, {2, 5}, {-2, -5}};
        for (int[] pp : pottedPositions) {
            setBlock(level, chunkBox, pp[0], 0, pp[1], smoothQuartz);
            setBlock(level, chunkBox, pp[0], 1, pp[1], pottedFern);
        }

        // Outer diagonal Mist-Veil Fern & Glimmer-Ice shrine pads
        int[][] outerDiagonalShrines = {{-8, -7}, {8, -7}, {-8, 7}, {8, 7}};
        for (int i = 0; i < outerDiagonalShrines.length; i++) {
            int sx = outerDiagonalShrines[i][0];
            int sz = outerDiagonalShrines[i][1];
            setBlock(level, chunkBox, sx, -2, sz, quartzBricks);
            setBlock(level, chunkBox, sx, -1, sz, quartzBricks);
            setBlock(level, chunkBox, sx, 0, sz, (i % 2 == 0) ? smoothQuartz : coarseGravel);
            setBlock(level, chunkBox, sx, 1, sz, (i % 2 == 0) ? glimmerIce : mistFern);
        }
    }

    private void buildCorePedestalAndChests(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState chiseledQuartz = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();
        BlockState quartzPillarY = Blocks.QUARTZ_PILLAR.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState glimmerCore = ModBlocks.GLIMMER_ICE.get().defaultBlockState();
        BlockState auroralLens = ModBlocks.AURORAL_LENS_BLOCK.get().defaultBlockState();

        // Central core spring & submerged Glimmer-Ice basin floor at (0, -2, 0)
        setBlock(level, chunkBox, 0, -3, 0, smoothQuartz);
        setBlock(level, chunkBox, 0, -2, 0, glimmerCore);
        setBlock(level, chunkBox, 0, -1, 0, water);
        setBlock(level, chunkBox, 0, 0, 0, water);
        BlockPos coreSpringPos = new BlockPos(this.originX, this.baseY, this.originZ);
        if (chunkBox.isInside(coreSpringPos)) {
            level.scheduleTick(coreSpringPos, Fluids.WATER, 2);
        }

        // Diagonal channels at (+/-1, -2..-1, +/-1) stay open warm water so core spring connects to Inner Thermal Pool
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                setBlock(level, chunkBox, dx, -3, dz, Blocks.SOUL_SAND.defaultBlockState());
                setBlock(level, chunkBox, dx, -2, dz, water);
                setBlock(level, chunkBox, dx, -1, dz, water);
            }
        }

        // North & South Smooth Quartz pedestal columns supporting the raised central pedestal cap at (0, 1, 0)
        for (int dz : new int[]{-1, 1}) {
            setBlock(level, chunkBox, 0, -3, dz, smoothQuartz);
            setBlock(level, chunkBox, 0, -2, dz, quartzPillarY);
            setBlock(level, chunkBox, 0, -1, dz, quartzPillarY);
            setBlock(level, chunkBox, 0, 0, dz, chiseledQuartz);
            setBlock(level, chunkBox, 0, 1, dz, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, dz < 0 ? Direction.SOUTH : Direction.NORTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
        }

        // Raised Smooth Quartz central pedestal holding Auroral Lens Block (Light Level 12) directly above the core water
        setBlock(level, chunkBox, 0, 1, 0, smoothQuartz);
        setBlock(level, chunkBox, 0, 2, 0, auroralLens);

        // Opposing West & East Smooth Quartz niches at (-1, 1, 0) and (+1, 1, 0) housing Structure 3 Loot Chests
        setBlock(level, chunkBox, -1, -3, 0, smoothQuartz);
        setBlock(level, chunkBox, -1, -2, 0, quartzPillarY);
        setBlock(level, chunkBox, -1, -1, 0, quartzPillarY);
        setBlock(level, chunkBox, -1, 0, 0, chiseledQuartz);
        placeLootChest(level, chunkBox, random, -1, 1, 0, Direction.WEST);
        setBlock(level, chunkBox, -1, 2, 0, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST)
                .setValue(StairBlock.HALF, Half.TOP));

        setBlock(level, chunkBox, 1, -3, 0, smoothQuartz);
        setBlock(level, chunkBox, 1, -2, 0, quartzPillarY);
        setBlock(level, chunkBox, 1, -1, 0, quartzPillarY);
        setBlock(level, chunkBox, 1, 0, 0, chiseledQuartz);
        placeLootChest(level, chunkBox, random, 1, 1, 0, Direction.EAST);
        setBlock(level, chunkBox, 1, 2, 0, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST)
                .setValue(StairBlock.HALF, Half.TOP));
    }

    private void buildArchedGazeboAndDispensers(WorldGenLevel level, BoundingBox chunkBox) {
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState quartzBricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        BlockState quartzPillarY = Blocks.QUARTZ_PILLAR.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState oxidizedCopper = Blocks.OXIDIZED_CUT_COPPER.defaultBlockState();
        BlockState weatheredCopperSlabBottom = Blocks.WEATHERED_CUT_COPPER_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        BlockState weatheredCopperSlabTop = Blocks.WEATHERED_CUT_COPPER_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.TOP);
        BlockState waterloggedQuartzSlab = Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                .setValue(SlabBlock.TYPE, SlabType.BOTTOM)
                .setValue(SlabBlock.WATERLOGGED, true);
        BlockState auroralLantern = ModBlocks.AURORAL_LANTERN.get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 4 Diagonal Gazebo Arch Piers + Hidden Dispensers paired with waterlogged-slab Observers
        for (int[] pier : DIAGONAL_PIERS) {
            int px = pier[0];
            int pz = pier[1];
            int sx = Integer.signum(px); // -1 or +1
            int sz = Integer.signum(pz); // -1 or +1
            Direction inwardX = sx > 0 ? Direction.WEST : Direction.EAST;

            // Waterlogged slab at dy=-2 observed by Observer at dy=-1 -> powers Smooth Quartz at dy=0 -> Dispenser at dy=1
            setBlock(level, chunkBox, px, -3, pz, quartzBricks);
            setBlock(level, chunkBox, px, -2, pz, waterloggedQuartzSlab);
            setBlock(level, chunkBox, px, -1, pz, Blocks.OBSERVER.defaultBlockState()
                    .setValue(ObserverBlock.FACING, Direction.DOWN)
                    .setValue(ObserverBlock.POWERED, false));
            setBlock(level, chunkBox, px, 0, pz, smoothQuartz);

            // Hidden Vapor Buff Dispenser tucked inside the pier base facing inward, flanked by Quartz Bricks casing
            placeAndStockVaporDispenser(level, chunkBox, px, 1, pz, inwardX);
            setBlock(level, chunkBox, px + sx, 1, pz, quartzBricks);
            setBlock(level, chunkBox, px, 1, pz + sz, quartzBricks);
            setBlock(level, chunkBox, px + sx, 2, pz, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, inwardX)
                    .setValue(StairBlock.HALF, Half.BOTTOM));
            setBlock(level, chunkBox, px, 2, pz + sz, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, sz > 0 ? Direction.NORTH : Direction.SOUTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM));

            // Ensure nozzle aperture & subtle Stone Pressure Plate step inward so bathers also trigger vapor mist
            setBlock(level, chunkBox, px - sx, 0, pz, smoothQuartz);
            setBlock(level, chunkBox, px - sx, 1, pz, Blocks.STONE_PRESSURE_PLATE.defaultBlockState());
            setBlock(level, chunkBox, px - sx, 2, pz, air);

            // Main Smooth Quartz & Quartz Pillar column rising to dy = 5, capped with Copper Cupola at dy = 6..7
            for (int dy = 2; dy <= 5; dy++) {
                setBlock(level, chunkBox, px, dy, pz, (dy == 5) ? smoothQuartz : quartzPillarY);
            }
            setBlock(level, chunkBox, px, 6, pz, oxidizedCopper);
            setBlock(level, chunkBox, px, 7, pz, weatheredCopperSlabBottom);

            // Grand Diagonal Arch sweeping from (px, 5, pz) inward to (0, 9, 0)
            // Step 1: (3*sx, 6..7, 3*sz) with Auroral Lantern perched on Smooth Quartz corbel at dy = 5
            setBlock(level, chunkBox, 3 * sx, 5, 3 * sz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.TOP));
            setBlock(level, chunkBox, 3 * sx, 6, 3 * sz, quartzBricks);
            setBlock(level, chunkBox, 3 * sx, 7, 3 * sz, smoothQuartz);
            setBlock(level, chunkBox, 3 * sx, 8, 3 * sz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.BOTTOM));
            // Perched Auroral Lantern beside the arch rib
            setBlock(level, chunkBox, 3 * sx, 4, 3 * sz, air);
            setBlock(level, chunkBox, 3 * sx - sx, 6, 3 * sz, Blocks.SMOOTH_QUARTZ_SLAB.defaultBlockState()
                    .setValue(SlabBlock.TYPE, SlabType.TOP));
            setBlock(level, chunkBox, 3 * sx - sx, 7, 3 * sz, auroralLantern);

            // Step 2: (2*sx, 7..8, 2*sz)
            setBlock(level, chunkBox, 2 * sx, 7, 2 * sz, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, sx > 0 ? Direction.EAST : Direction.WEST)
                    .setValue(StairBlock.HALF, Half.TOP));
            setBlock(level, chunkBox, 2 * sx, 8, 2 * sz, smoothQuartz);
            setBlock(level, chunkBox, 2 * sx, 9, 2 * sz, quartzBricks);

            // Step 3: (1*sx, 8..9, 1*sz) converging into central dome ring
            setBlock(level, chunkBox, sx, 8, sz, Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, sx > 0 ? Direction.EAST : Direction.WEST)
                    .setValue(StairBlock.HALF, Half.TOP));
            setBlock(level, chunkBox, sx, 9, sz, smoothQuartz);
        }

        // Central Gazebo Keystone & Oxidized / Weathered Cut Copper Dome (dy = 9..11)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                boolean isCenter = (dx == 0 && dz == 0);
                setBlock(level, chunkBox, dx, 9, dz, isCenter ? oxidizedCopper : smoothQuartz);
            }
        }
        // Copper eaves around dome base at dy = 9
        setBlock(level, chunkBox, -2, 9, 0, weatheredCopperSlabTop);
        setBlock(level, chunkBox, 2, 9, 0, weatheredCopperSlabTop);
        setBlock(level, chunkBox, 0, 9, -2, weatheredCopperSlabTop);
        setBlock(level, chunkBox, 0, 9, 2, weatheredCopperSlabTop);

        // Dome drum at dy = 10
        setBlock(level, chunkBox, 0, 10, 0, oxidizedCopper);
        setBlock(level, chunkBox, -1, 10, 0, oxidizedCopper);
        setBlock(level, chunkBox, 1, 10, 0, oxidizedCopper);
        setBlock(level, chunkBox, 0, 10, -1, oxidizedCopper);
        setBlock(level, chunkBox, 0, 10, 1, oxidizedCopper);
        for (int dx : new int[]{-1, 1}) {
            for (int dz : new int[]{-1, 1}) {
                setBlock(level, chunkBox, dx, 10, dz, weatheredCopperSlabBottom);
            }
        }

        // Apex finial at dy = 11 (total height 0..11 = 12 blocks above sea level)
        setBlock(level, chunkBox, 0, 11, 0, weatheredCopperSlabBottom);
    }

    private void placeLootChest(
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
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.STRUCTURE_3_CHEST_LOOT);
    }

    private void placeAndStockVaporDispenser(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState dispenserState = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, facing);
        level.setBlock(pos, dispenserState, 2);

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof DispenserBlockEntity dispenser) {
            for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
                Item vessel = (slot % 2 == 0) ? Items.SPLASH_POTION : Items.LINGERING_POTION;
                Holder<Potion> potion = ((slot / 2) % 2 == 0) ? Potions.REGENERATION : Potions.INVISIBILITY;
                ItemStack potionStack = PotionContents.createItemStack(vessel, potion);
                dispenser.setItem(slot, potionStack);
            }
        }
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
