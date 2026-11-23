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
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Programmatic generator for Biome #3 Structure: Icebound Blight-Wreck ({@code 16 x 22 x 14}):
 * <ul>
 *   <li>Irregular sea-level pressure ridge collar ({@code dy = -2..0}) of {@code blighted_packed_ice},
 *       {@code packed_ice}, and treacherous {@code rotten_ice} patches, anchored down to the seabed
 *       exclusively beneath solid central keel ribs so the ice floe sits naturally in water.</li>
 *   <li>Curved raised bow ({@code dz = -9..-5}) with spruce hull ribs, {@code silver_pine_planks}
 *       gunwales, forward spruce bowsprit ({@code dz = -11..-9}) sprouting {@code frozen_spruce_branch}
 *       icicles, and {@code rimebloom} on the flanking ice ridge.</li>
 *   <li>Crushed half-flooded amidships hold ({@code dz = -4..+2}) with water bilge at {@code dy = -1},
 *       {@code rotten_ice} trap floorboards, {@code necrotic_slime_block} &amp; {@code necrotic_slime_patch}
 *       rot growth, and a tucked smuggler's {@code silver_pine_barrel} at {@code (-2, 0, -5)}.</li>
 *   <li>Snapped ice-crusted central mast at {@code (0, -1..+10, -1)} with {@code silver_pine_fence}
 *       shrouds, {@code dy = 7} cross-yardarm, hanging Soul Lantern at {@code (-3, 6, -1)}, and jagged
 *       {@code frozen_spruce_branch} crown at {@code dy = 10}.</li>
 *   <li>Elevated stern Captain's Cabin ({@code dz = +3..+8}) with dry {@code silver_pine_planks} &amp;
 *       {@code silver_pine_stairs} quarterdeck, open doorway at {@code dz = +3}, windows, Lectern at
 *       {@code (-2, 2, 6)}, Soul Lantern, and Captain's Loot Chest at {@code (0, 2, 7)} facing North.</li>
 * </ul>
 */
public class IceboundBlightWreckPiece extends StructurePiece {

    private static final int[] KEEL_FOUNDATION_Z = {-6, -3, 0, 3, 6};

    private static final int[][] ROTTEN_FLOORBOARD_SLOTS = {
            {-1, -2}, {1, -3}, {-2, 0}, {1, 1}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public IceboundBlightWreckPiece(int originX, int baseY, int originZ) {
        // Exact 16 (W: -8..+7) x 14 (H: -3..+10) x 22 (L: -11..+10) bounding box
        super(resolvePieceType(), 0,
                new BoundingBox(originX - 8, baseY - 3, originZ - 11, originX + 7, baseY + 10, originZ + 10));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public IceboundBlightWreckPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX() + 1) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 3);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ() + 1) / 2);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.ICEBOUND_BLIGHT_WRECK_PIECE.isPresent()) {
            return ModStructures.ICEBOUND_BLIGHT_WRECK_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "icebound_blight_wreck_piece");
        return BuiltInRegistries.STRUCTURE_PIECE.getOptional(id).orElseThrow();
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

    public BlockPos getCaptainsChestPos() {
        return new BlockPos(this.originX, this.baseY + 2, this.originZ + 7);
    }

    public BlockPos getSmugglerBarrelPos() {
        return new BlockPos(this.originX - 2, this.baseY, this.originZ - 5);
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
        buildIcePackPressureRidgeCollar(level, chunkBox);
        buildRaisedBowAndBowsprit(level, chunkBox, random);
        buildCrushedAmidshipsHold(level, chunkBox);
        buildElevatedSternCaptainsCabin(level, chunkBox, random);
        buildSnappedCentralMastAndRigging(level, chunkBox);
    }

    private void buildIcePackPressureRidgeCollar(WorldGenLevel level, BoundingBox chunkBox) {
        Block blightedIceBlock = resolveBlock("blighted_packed_ice", Blocks.PACKED_ICE);
        Block rottenIceBlock = resolveBlock("rotten_ice", Blocks.ICE);
        Block rimebloomBlock = resolveBlock("rimebloom", Blocks.BLUE_ORCHID);

        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState blightedIce = blightedIceBlock.defaultBlockState();
        BlockState rottenIce = rottenIceBlock.defaultBlockState();
        BlockState rimebloom = rimebloomBlock.defaultBlockState();
        BlockState keelWood = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Irregular oval pressure-ridge ice floe (dy = -2..0) locking the ship into sea level
        for (int dx = -8; dx <= 7; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                double wobble = 0.08D * Math.sin(dx * 1.25D + dz * 0.85D) + 0.05D * Math.cos(dz * 0.7D - dx);
                double normR = Math.hypot((dx + 0.3D) / 7.5D, dz / 10.2D) + wobble;
                if (normR > 1.0D) {
                    continue;
                }

                // Clear snow/iceberg clutter above the floe so hull, mast, and bowsprit have clean air
                for (int dy = 1; dy <= 10; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }

                // Submerged ice keel shelf at dy = -2 beneath core pressure collar
                if (normR <= 0.76D) {
                    setBlock(level, chunkBox, dx, -2, dz, ((dx + dz) & 1) == 0 ? blightedIce : packedIce);
                }

                // Sea-surface ice floe sheet at dy = -1 (with treacherous outer Rotten Ice leads)
                boolean outerRottenPatch = normR > 0.74D && (((dx * 3 + dz * 5) & 3) == 0);
                BlockState floeSurface = outerRottenPatch ? rottenIce
                        : ((((dx * 5 + dz * 7) & 3) <= 1) ? blightedIce : packedIce);
                setBlock(level, chunkBox, dx, -1, dz, floeSurface);

                // Buckled pressure-ridge collar at dy = 0 heaved up against the hull flanks (0.42..0.76)
                boolean inHullFootprint = Math.abs(dx) <= hullHalfBeam(dz) && dz >= -8 && dz <= 8;
                if (!inHullFootprint && normR <= 0.78D) {
                    boolean rottenPocket = normR > 0.62D && (((dx + dz * 3) & 3) == 0);
                    BlockState ridgeState = rottenPocket ? rottenIce
                            : ((((dx + dz) & 1) == 0) ? blightedIce : packedIce);
                    setBlock(level, chunkBox, dx, 0, dz, ridgeState);
                } else if (!inHullFootprint) {
                    setBlock(level, chunkBox, dx, 0, dz, air);
                }
            }
        }

        // Jagged pressure-ridge ice teeth wedged up against the canted amidships timber
        setBlock(level, chunkBox, -5, 1, -2, blightedIce);
        setBlock(level, chunkBox, -5, 1, -1, packedIce);
        setBlock(level, chunkBox, 5, 1, 0, blightedIce);
        setBlock(level, chunkBox, -5, 1, 3, blightedIce);

        // Anchor ONLY solid central keel ribs downward to seabed so the surrounding ice floe floats naturally in water
        for (int kz : KEEL_FOUNDATION_Z) {
            setBlock(level, chunkBox, 0, -2, kz, keelWood);
            fillFoundationDown(level, chunkBox, 0, -3, kz, blightedIce);
        }

        // Glowing Rimeblooms sprouting on the frozen pressure ridge collar
        placePlantIfSurvives(level, chunkBox, -5, 1, -5, rimebloom);
        placePlantIfSurvives(level, chunkBox, 5, 1, -4, rimebloom);
    }

    private void buildRaisedBowAndBowsprit(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block silverPineSlabBlock = resolveBlock("silver_pine_slab", Blocks.SPRUCE_SLAB);
        Block silverPineBarrelBlock = resolveBlock("silver_pine_barrel", Blocks.BARREL);
        Block frozenBranchBlock = resolveBlock("frozen_spruce_branch", Blocks.SPRUCE_FENCE);

        BlockState sprucePlanks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState ribY = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState keelZ = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState silverPlanks = silverPinePlanksBlock.defaultBlockState();
        BlockState bowspritLog = Blocks.SPRUCE_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Curved Raised Bow Hull (dz = -9..-5, rising dy = 0..+4)
        for (int dz = -9; dz <= -5; dz++) {
            int halfW = hullHalfBeam(dz); // 0 at -9, 1 at -8, 2 at -7, 3 at -6..-5
            int deckY = (dz <= -8) ? 3 : 2;
            boolean isRibZ = (dz == -7 || dz == -5);

            for (int dx = -halfW; dx <= halfW; dx++) {
                boolean isOuterShell = (Math.abs(dx) == halfW) || (dz == -9);
                setBlock(level, chunkBox, dx, -1, dz, (dx == 0) ? keelZ : sprucePlanks);

                if (isOuterShell) {
                    for (int dy = 0; dy <= deckY; dy++) {
                        BlockState hullWall = (dx == 0 && dz == -9) ? ribY
                                : ((isRibZ && dy <= deckY - 1) ? ribY : sprucePlanks);
                        setBlock(level, chunkBox, dx, dy, dz, hullWall);
                    }
                    // Silver-Pine gunwales crowning the raised prow (dy = 3..4)
                    int gunwaleY = deckY + 1;
                    if (dz <= -7) {
                        setBlock(level, chunkBox, dx, gunwaleY, dz, silverPlanks);
                    } else if (silverPineSlabBlock.defaultBlockState().hasProperty(SlabBlock.TYPE)) {
                        setBlock(level, chunkBox, dx, gunwaleY, dz, silverPineSlabBlock.defaultBlockState()
                                .setValue(SlabBlock.TYPE, SlabType.BOTTOM)
                                .setValue(SlabBlock.WATERLOGGED, false));
                    } else {
                        setBlock(level, chunkBox, dx, gunwaleY, dz, silverPlanks);
                    }
                } else {
                    // Tucked smuggler's forecastle space underneath the raised bow deck (dy = 0..1)
                    for (int dy = 0; dy < deckY; dy++) {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                    setBlock(level, chunkBox, dx, deckY, dz, (dz <= -7) ? silverPlanks : sprucePlanks);
                }
            }

            // Flared upside-down Spruce Stairs cutwater & bow flare
            if (dz >= -8 && dz <= -6 && halfW >= 1) {
                setBlock(level, chunkBox, -halfW, 0, dz, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.EAST)
                        .setValue(StairBlock.HALF, Half.TOP));
                setBlock(level, chunkBox, halfW, 0, dz, Blocks.SPRUCE_STAIRS.defaultBlockState()
                        .setValue(StairBlock.FACING, Direction.WEST)
                        .setValue(StairBlock.HALF, Half.TOP));
            }
        }
        // Cutwater prow stair under stempost at (0, 2, -10) & (0, 3, -9)
        setBlock(level, chunkBox, 0, 2, -10, Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH)
                .setValue(StairBlock.HALF, Half.TOP));
        setBlock(level, chunkBox, 0, 4, -9, silverPlanks);

        // 2. Forward Spruce Log Bowsprit (dz = -11..-9, dy = 4..5) sprouting Frozen Spruce Branch icicles
        setBlock(level, chunkBox, 0, 4, -9, bowspritLog);
        setBlock(level, chunkBox, 0, 4, -10, bowspritLog);
        setBlock(level, chunkBox, 0, 5, -10, Blocks.SPRUCE_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.SOUTH)
                .setValue(StairBlock.HALF, Half.BOTTOM));
        setBlock(level, chunkBox, 0, 5, -11, bowspritLog);

        placeFrozenBranch(level, chunkBox, frozenBranchBlock, 0, 3, -10, Direction.DOWN);
        placeFrozenBranch(level, chunkBox, frozenBranchBlock, 0, 4, -11, Direction.DOWN);
        placeFrozenBranch(level, chunkBox, frozenBranchBlock, 1, 4, -10, Direction.EAST);

        // 3. Tucked Smuggler's Silver-Pine Barrel under the bow deck at (-2, 0, -5)
        setBlock(level, chunkBox, -2, -1, -5, sprucePlanks);
        placeLootBarrel(level, chunkBox, random, silverPineBarrelBlock, -2, 0, -5, Direction.EAST);
        setBlock(level, chunkBox, -1, 0, -5, air);
        setBlock(level, chunkBox, -1, 1, -5, air);
    }

    private void buildCrushedAmidshipsHold(WorldGenLevel level, BoundingBox chunkBox) {
        Block blightedIceBlock = resolveBlock("blighted_packed_ice", Blocks.PACKED_ICE);
        Block rottenIceBlock = resolveBlock("rotten_ice", Blocks.ICE);
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block silverPineSlabBlock = resolveBlock("silver_pine_slab", Blocks.SPRUCE_SLAB);
        Block necroticSlimeBlock = resolveBlock("necrotic_slime_block", Blocks.SLIME_BLOCK);
        Block necroticPatchBlock = resolveBlock("necrotic_slime_patch", Blocks.MOSS_CARPET);

        BlockState sprucePlanks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState ribY = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState ribX = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState keelZ = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Z);
        BlockState silverPlanks = silverPinePlanksBlock.defaultBlockState();
        BlockState blightedIce = blightedIceBlock.defaultBlockState();
        BlockState rottenIce = rottenIceBlock.defaultBlockState();
        BlockState slimeBlock = necroticSlimeBlock.defaultBlockState();
        BlockState slimePatch = necroticPatchBlock.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Half-flooded Amidships Hold (dz = -4..+2, dx = -4..+4, dy = -2..+2)
        for (int dz = -4; dz <= 2; dz++) {
            boolean isRibFrame = (dz == -3 || dz == 0 || dz == 2);
            for (int dx = -4; dx <= 4; dx++) {
                // Solid hull bottom at dy = -2
                if (dx == 0) {
                    setBlock(level, chunkBox, dx, -2, dz, keelZ);
                } else if (Math.abs(dx) <= 3) {
                    setBlock(level, chunkBox, dx, -2, dz, isRibFrame ? ribX : sprucePlanks);
                }

                if (dx == -4) {
                    // Port hull (canted lower side, stove-in by Blight-packed ice at dz = -1..0)
                    setBlock(level, chunkBox, dx, -1, dz, sprucePlanks);
                    if (dz == -1 || dz == 0) {
                        setBlock(level, chunkBox, dx, 0, dz, blightedIce);
                        setBlock(level, chunkBox, dx, 1, dz, blightedIce);
                    } else {
                        setBlock(level, chunkBox, dx, 0, dz, isRibFrame ? ribY : sprucePlanks);
                        setBlock(level, chunkBox, dx, 1, dz, (dz == -3 || dz == 2) ? silverPlanks : Blocks.SPRUCE_STAIRS.defaultBlockState()
                                .setValue(StairBlock.FACING, Direction.EAST)
                                .setValue(StairBlock.HALF, Half.BOTTOM));
                    }
                } else if (dx == 4) {
                    // Starboard hull (canted higher side rising to dy = +2 Silver-Pine gunwale)
                    setBlock(level, chunkBox, dx, -1, dz, sprucePlanks);
                    setBlock(level, chunkBox, dx, 0, dz, isRibFrame ? ribY : sprucePlanks);
                    setBlock(level, chunkBox, dx, 1, dz, isRibFrame ? ribY : sprucePlanks);
                    setBlock(level, chunkBox, dx, 2, dz, silverPlanks);
                } else {
                    // Flooded lower bilge water at dy = -1 and open crushed hold above
                    setBlock(level, chunkBox, dx, -1, dz, water);
                    for (int dy = 0; dy <= 3; dy++) {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                }
            }
        }

        // 2. Broken Hull Deck Planking & Treacherous Rotten Ice Floorboards at dy = 0 over bilge water
        BlockState brokenPlankSlab = silverPineSlabBlock.defaultBlockState().hasProperty(SlabBlock.TYPE)
                ? silverPineSlabBlock.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.TOP).setValue(SlabBlock.WATERLOGGED, false)
                : sprucePlanks;
        setBlock(level, chunkBox, 0, 0, -4, sprucePlanks);
        setBlock(level, chunkBox, -1, 0, -4, sprucePlanks);
        setBlock(level, chunkBox, 0, 0, -3, brokenPlankSlab);
        setBlock(level, chunkBox, 2, 0, -2, sprucePlanks);
        setBlock(level, chunkBox, 2, 0, -1, brokenPlankSlab);
        setBlock(level, chunkBox, 0, 0, 1, sprucePlanks);
        setBlock(level, chunkBox, 0, 0, 2, sprucePlanks);
        setBlock(level, chunkBox, 1, 0, 2, sprucePlanks);
        setBlock(level, chunkBox, -1, 0, 2, sprucePlanks);

        for (int[] slot : ROTTEN_FLOORBOARD_SLOTS) {
            setBlock(level, chunkBox, slot[0], 0, slot[1], rottenIce);
        }

        // Blight ice breach spilling into the port hold
        setBlock(level, chunkBox, -3, 0, -1, blightedIce);

        // 3. Festering Necrotic Slime Blocks & Necrotic Slime Patches in the bilge/planking
        setBlock(level, chunkBox, -3, 0, -3, slimeBlock);
        setBlock(level, chunkBox, 3, 0, 0, slimeBlock);
        setBlock(level, chunkBox, -3, 0, 1, slimeBlock);

        setBlock(level, chunkBox, -2, 0, -3, sprucePlanks);
        placePlantIfSurvives(level, chunkBox, -2, 1, -3, slimePatch);
        setBlock(level, chunkBox, 3, 0, -2, sprucePlanks);
        placePlantIfSurvives(level, chunkBox, 3, 1, -2, slimePatch);
        setBlock(level, chunkBox, -2, 0, 2, sprucePlanks);
        placePlantIfSurvives(level, chunkBox, -2, 1, 2, slimePatch);
    }

    private void buildElevatedSternCaptainsCabin(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block silverPineStairsBlock = resolveBlock("silver_pine_stairs", Blocks.SPRUCE_STAIRS);
        Block silverPineFenceBlock = resolveBlock("silver_pine_fence", Blocks.SPRUCE_FENCE);

        BlockState sprucePlanks = Blocks.SPRUCE_PLANKS.defaultBlockState();
        BlockState ribY = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState silverPlanks = silverPinePlanksBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Dry Stern Hull Base (dy = -1..0) & Quarterdeck / Cabin Floor (dy = 1) across dz = +3..+8, dx = -3..+3
        for (int dz = 3; dz <= 8; dz++) {
            int halfW = (dz == 8) ? 2 : 3;
            for (int dx = -halfW; dx <= halfW; dx++) {
                boolean isCorner = (Math.abs(dx) == halfW && (dz == 3 || dz == 7 || dz == 8));
                setBlock(level, chunkBox, dx, -1, dz, sprucePlanks);
                setBlock(level, chunkBox, dx, 0, dz, isCorner ? ribY : sprucePlanks);
                setBlock(level, chunkBox, dx, 1, dz, silverPlanks);

                for (int dy = 2; dy <= 5; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // Quarterdeck approach step from amidships at (0, 1, 2) leading into open cabin doorway at dz = +3
        if (silverPineStairsBlock.defaultBlockState().hasProperty(StairBlock.FACING)) {
            setBlock(level, chunkBox, 0, 1, 2, silverPineStairsBlock.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM)
                    .setValue(StairBlock.WATERLOGGED, false));
        } else {
            setBlock(level, chunkBox, 0, 1, 2, silverPlanks);
        }

        // 2. Enclosed Spruce & Silver-Pine Captain's Cabin Walls (dz = +3..+8, dy = 2..4) & Roof (dy = 5)
        for (int dz = 3; dz <= 8; dz++) {
            int halfW = (dz == 8) ? 2 : 3;
            for (int dx = -halfW; dx <= halfW; dx++) {
                boolean isFrontWall = (dz == 3);
                boolean isRearTransom = (dz == 8 || (dz == 7 && Math.abs(dx) == 3));
                boolean isSideWall = (Math.abs(dx) == halfW);

                if (isFrontWall || isRearTransom || isSideWall) {
                    boolean isDoorway = isFrontWall && dx == 0;
                    if (isDoorway) {
                        setBlock(level, chunkBox, 0, 2, 3, air);
                        setBlock(level, chunkBox, 0, 3, 3, air);
                        setBlock(level, chunkBox, 0, 4, 3, silverPlanks);
                    } else {
                        boolean isCornerPost = (Math.abs(dx) == halfW && (dz == 3 || dz == 7 || dz == 8))
                                || (isFrontWall && Math.abs(dx) == 1);
                        boolean isSideWindow = isSideWall && (dz == 5 || dz == 6);
                        boolean isSternWindow = (dz == 8 && Math.abs(dx) <= 1);

                        for (int dy = 2; dy <= 4; dy++) {
                            if (dy == 3 && (isSideWindow || isSternWindow)) {
                                BlockState mullion = isSternWindow && dx == 0
                                        ? connectFence(silverPineFenceBlock.defaultBlockState(), false, false, true, true)
                                        : air;
                                setBlock(level, chunkBox, dx, dy, dz, mullion);
                            } else if (isCornerPost) {
                                setBlock(level, chunkBox, dx, dy, dz, ribY);
                            } else {
                                setBlock(level, chunkBox, dx, dy, dz, (dy == 2) ? sprucePlanks : silverPlanks);
                            }
                        }
                    }
                }

                // Quarterdeck roof at dy = 5 with Silver-Pine Stairs cornice along the sides
                if (Math.abs(dx) == halfW && dz < 8 && silverPineStairsBlock.defaultBlockState().hasProperty(StairBlock.FACING)) {
                    setBlock(level, chunkBox, dx, 5, dz, silverPineStairsBlock.defaultBlockState()
                            .setValue(StairBlock.FACING, dx < 0 ? Direction.EAST : Direction.WEST)
                            .setValue(StairBlock.HALF, Half.BOTTOM)
                            .setValue(StairBlock.WATERLOGGED, false));
                } else {
                    setBlock(level, chunkBox, dx, 5, dz, silverPlanks);
                }
            }
        }

        // 3. Captain's Cabin Interior: Lectern at (-2, 2, 6), Hanging Soul Lantern, & Captain's Loot Chest at (0, 2, 7)
        setBlock(level, chunkBox, -2, 2, 6, Blocks.LECTERN.defaultBlockState()
                .setValue(LecternBlock.FACING, Direction.EAST));
        setBlock(level, chunkBox, 0, 4, 5, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));
        placeLootChest(level, chunkBox, random, 0, 2, 7, Direction.NORTH);
    }

    private void buildSnappedCentralMastAndRigging(WorldGenLevel level, BoundingBox chunkBox) {
        Block blightedIceBlock = resolveBlock("blighted_packed_ice", Blocks.PACKED_ICE);
        Block silverPineFenceBlock = resolveBlock("silver_pine_fence", Blocks.SPRUCE_FENCE);
        Block frozenBranchBlock = resolveBlock("frozen_spruce_branch", Blocks.SPRUCE_FENCE);

        BlockState mastLogY = Blocks.SPRUCE_LOG.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState yardarmX = Blocks.STRIPPED_SPRUCE_WOOD.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.X);
        BlockState blightedIce = blightedIceBlock.defaultBlockState();

        // 1. Vertical Spruce Log Mast at (0, -1..+10, -1) collared at deck level with Blighted Packed Ice
        setBlock(level, chunkBox, 0, -2, -1, mastLogY);
        for (int dy = -1; dy <= 10; dy++) {
            setBlock(level, chunkBox, 0, dy, -1, mastLogY);
        }
        setBlock(level, chunkBox, -1, 0, -1, blightedIce);
        setBlock(level, chunkBox, 1, 0, -1, blightedIce);
        setBlock(level, chunkBox, 0, 0, -2, blightedIce);
        setBlock(level, chunkBox, 0, 0, 0, blightedIce);
        setBlock(level, chunkBox, -1, 1, -1, blightedIce);

        // 2. Silver-Pine Fence Rigging Shrouds climbing from hull gunwales toward the yardarm
        BlockState postFence = connectFence(silverPineFenceBlock.defaultBlockState(), false, false, false, false);
        setBlock(level, chunkBox, -3, 2, -1, postFence);
        setBlock(level, chunkBox, -3, 3, -1, postFence);
        setBlock(level, chunkBox, -2, 4, -1, postFence);
        setBlock(level, chunkBox, -2, 5, -1, postFence);

        setBlock(level, chunkBox, 3, 3, -1, postFence);
        setBlock(level, chunkBox, 3, 4, -1, postFence);
        setBlock(level, chunkBox, 2, 5, -1, postFence);
        setBlock(level, chunkBox, 2, 6, -1, postFence);

        // 3. Cross-Yardarm at dy = 7 (dx = -3..+3, dz = -1) with Hanging Soul Lantern at (-3, 6, -1)
        for (int dx = -3; dx <= 3; dx++) {
            if (dx != 0) {
                setBlock(level, chunkBox, dx, 7, -1, yardarmX);
            }
        }
        setBlock(level, chunkBox, -3, 6, -1, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));

        // 4. Snapped Jagged Mast Top at dy = 10 sprouting Frozen Spruce Branch icicles
        placeFrozenBranch(level, chunkBox, frozenBranchBlock, 1, 10, -1, Direction.EAST);
        placeFrozenBranch(level, chunkBox, frozenBranchBlock, -1, 9, -1, Direction.WEST);
        placeFrozenBranch(level, chunkBox, frozenBranchBlock, 0, 10, -2, Direction.NORTH);
    }

    private static int hullHalfBeam(int dz) {
        if (dz <= -9) {
            return 0;
        }
        if (dz == -8) {
            return 1;
        }
        if (dz == -7) {
            return 2;
        }
        if (dz <= -5) {
            return 3;
        }
        if (dz <= 2) {
            return 4;
        }
        if (dz <= 7) {
            return 3;
        }
        return 2;
    }

    private void placeFrozenBranch(
            WorldGenLevel level,
            BoundingBox chunkBox,
            Block branchBlock,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockState state = branchBlock.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        setBlock(level, chunkBox, dx, dy, dz, state);
    }

    private void placePlantIfSurvives(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState plantState) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir() && plantState.canSurvive(level, pos)) {
            level.setBlock(pos, plantState, 2);
        }
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
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.ICEBOUND_BLIGHT_WRECK_CHEST_LOOT);
    }

    private void placeLootBarrel(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            Block barrelBlock,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState barrelState = barrelBlock.defaultBlockState();
        if (barrelState.hasProperty(BarrelBlock.FACING)) {
            barrelState = barrelState.setValue(BarrelBlock.FACING, facing);
        }
        level.setBlock(pos, barrelState, 2);
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.ICEBOUND_BLIGHT_WRECK_CHEST_LOOT);
    }

    private static BlockState connectFence(BlockState base, boolean north, boolean south, boolean east, boolean west) {
        BlockState s = base;
        if (s.hasProperty(CrossCollisionBlock.NORTH)) {
            s = s.setValue(CrossCollisionBlock.NORTH, north);
        }
        if (s.hasProperty(CrossCollisionBlock.SOUTH)) {
            s = s.setValue(CrossCollisionBlock.SOUTH, south);
        }
        if (s.hasProperty(CrossCollisionBlock.EAST)) {
            s = s.setValue(CrossCollisionBlock.EAST, east);
        }
        if (s.hasProperty(CrossCollisionBlock.WEST)) {
            s = s.setValue(CrossCollisionBlock.WEST, west);
        }
        if (s.hasProperty(CrossCollisionBlock.WATERLOGGED)) {
            s = s.setValue(CrossCollisionBlock.WATERLOGGED, false);
        }
        return s;
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
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
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 22);
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
