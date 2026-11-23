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
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
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

import java.util.Optional;

/**
 * Programmatic 3D generator for Biome #12's core-free landmark:
 * {@code Glazed Mycelial Conservatory} ({@code 20 x 20 x 18}, {@code dx = -10..9, dy = 0..17, dz = -10..9}
 * — strictly ZERO Shrine Cores or Anchors):
 * <ul>
 *   <li><b>1. Coastal Pack-Ice Terrace Foundation ({@code dy = 0..1})</b>:
 *       Thick {@link Blocks#BLUE_ICE}, {@link Blocks#PACKED_ICE}, and glowing
 *       {@code entropica:spore_glazed_ice} coastal terrace with {@code entropica:spore_wood_planks}
 *       &amp; {@code entropica:spore_wood_stairs} boardwalk steps on all 4 cardinal sides and
 *       {@code entropica:spore_wood_fence} perimeter railings.</li>
 *   <li><b>2. Biodome Shell ({@code dy = 2..15})</b>:
 *       {@code 4} sweeping parabolic arches of all-bark {@code entropica:spore_wood} ribs meeting at a
 *       {@code 15}-block-tall apex skylight, infilled with curved {@code entropica:glaze_cap_mushroom_block}
 *       roof panels and wide green-tinted {@code entropica:spore_glass} skylight windows.</li>
 *   <li><b>3. Air-Purified Interior (ZERO CORES OR ANCHORS!)</b>:
 *       {@code 4} hanging/standing {@code entropica:cryo_spore_censer} lanterns, {@code 4} quadrant
 *       raised {@code entropica:spore_wood_slab} planter beds filled with {@code entropica:frozen_spore_moss},
 *       cultivated {@code entropica:spore_bearing_pitcher_plump} ({@code FACING = UP}),
 *       {@code entropica:spore_burst_puffball}, and {@code entropica:frozen_spore_moss_carpet} tended by
 *       {@code 2} inward-facing {@code entropica:spore_bellows}, central {@code spore_glazed_ice} &amp;
 *       {@link Blocks#WATER} cryo-irrigation font, {@link Blocks#BREWING_STAND} apothecary counter, and
 *       {@code 2} {@link Blocks#CHEST} containers ({@code entropica:chests/glazed_mycelial_conservatory}).</li>
 *   <li><b>4. Exterior Corner Accent</b>:
 *       Scenic Giant Frozen Glaze-Cap Mushroom rising right beside the dome on the NE terrace corner,
 *       built strictly with a vanilla {@link Blocks#MUSHROOM_STEM} trunk &amp; root flares and a
 *       {@code entropica:glaze_cap_mushroom_block} umbrella cap.</li>
 * </ul>
 */
public class GlazedMycelialConservatoryPiece extends StructurePiece {

    private static final int[][] QUADRANT_SIGNS = {
            {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public GlazedMycelialConservatoryPiece(int originX, int baseY, int originZ) {
        // Exact 20 (X: -10..+9) x 18 (Y: 0..+17) x 20 (Z: -10..+9) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 10,
                baseY,
                originZ - 10,
                originX + 9,
                baseY + 17,
                originZ + 9
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public GlazedMycelialConservatoryPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 10);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY());
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 10);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.GLAZED_MYCELIAL_CONSERVATORY_PIECE.isPresent()) {
            return ModStructures.GLAZED_MYCELIAL_CONSERVATORY_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "glazed_mycelial_conservatory_piece");
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
        buildPackIceTerraceAndBoardwalks(level, chunkBox);
        buildParabolicBiodomeShell(level, chunkBox);
        buildAirPurifiedInterior(level, chunkBox, random);
        buildExteriorCornerGlazeCapMushroom(level, chunkBox);
    }

    private void buildPackIceTerraceAndBoardwalks(WorldGenLevel level, BoundingBox chunkBox) {
        Block sporeGlazedIceBlock = resolveBlock("spore_glazed_ice", Blocks.PACKED_ICE);
        Block frozenSporeMossBlock = resolveBlock("frozen_spore_moss", Blocks.MOSS_BLOCK);
        Block sporeWoodPlanksBlock = resolveBlock("spore_wood_planks", Blocks.SPRUCE_PLANKS);
        Block sporeWoodStairsBlock = resolveBlock("spore_wood_stairs", Blocks.SPRUCE_STAIRS);
        Block sporeWoodFenceBlock = resolveBlock("spore_wood_fence", Blocks.SPRUCE_FENCE);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState sporeGlazedIce = sporeGlazedIceBlock.defaultBlockState();
        BlockState frozenSporeMoss = frozenSporeMossBlock.defaultBlockState();
        BlockState planks = sporeWoodPlanksBlock.defaultBlockState();

        // Clear sky headroom across the 20 x 20 footprint (dy = 1..17)
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                for (int dy = 1; dy <= 17; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // 1. Thick Coastal Pack-Ice Terrace (dy = 0..1) + NE Mushroom Outcropping Pad
        for (int dx = -9; dx <= 9; dx++) {
            for (int dz = -9; dz <= 9; dz++) {
                double r = Math.hypot(dx, dz);
                boolean inMushroomOutcrop = (dx >= 5 && dx <= 8 && dz >= -8 && dz <= -5
                        && Math.hypot(dx - 6.5D, dz + 6.5D) <= 2.3D);

                if (r > 9.2D && !inMushroomOutcrop) {
                    continue;
                }

                // Anchor downward into shallow bay water / soft ground
                int hash = Math.abs(dx * 31 + dz * 17);
                BlockState baseIce = (hash % 3 == 0) ? blueIce : ((hash % 5 == 0) ? sporeGlazedIce : packedIce);
                setRel(level, chunkBox, dx, 0, dz, baseIce);
                fillFoundationDown(level, chunkBox, dx, -1, dz, (r <= 6.5D) ? blueIce : packedIce);

                // Upper terrace deck at dy = 1
                if (inMushroomOutcrop && r > 7.2D) {
                    BlockState outcropTop = ((dx + dz) % 2 == 0) ? frozenSporeMoss : sporeGlazedIce;
                    setRel(level, chunkBox, dx, 1, dz, outcropTop);
                } else if (r <= 8.5D) {
                    boolean isCruciformWalkway = (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) && r <= 8.2D;
                    if (isCruciformWalkway) {
                        BlockState pathState = (Math.abs(dx) == 0 && Math.abs(dz) == 0)
                                ? sporeGlazedIce
                                : ((Math.abs(dx) + Math.abs(dz)) % 4 == 0 ? sporeGlazedIce : planks);
                        setRel(level, chunkBox, dx, 1, dz, pathState);
                    } else if (r <= 6.8D) {
                        BlockState interiorSubfloor = ((dx + dz) & 1) == 0 ? planks : sporeGlazedIce;
                        setRel(level, chunkBox, dx, 1, dz, interiorSubfloor);
                    } else {
                        BlockState rimIce = (hash % 3 == 0) ? sporeGlazedIce : ((hash % 2 == 0) ? blueIce : packedIce);
                        setRel(level, chunkBox, dx, 1, dz, rimIce);
                    }
                }
            }
        }

        // 2. Spore-Wood Planks & Stairs Boardwalk Steps on all 4 Cardinal Sides
        for (int s = -1; s <= 1; s++) {
            // North (-Z) boardwalk stairs ascending South onto dy = 1
            setRel(level, chunkBox, s, 0, -9, planks);
            setRel(level, chunkBox, s, 1, -9, stairState(sporeWoodStairsBlock, Direction.SOUTH, Half.BOTTOM));
            setRel(level, chunkBox, s, 1, -8, planks);

            // South (+Z) boardwalk stairs ascending North onto dy = 1
            setRel(level, chunkBox, s, 0, 9, planks);
            setRel(level, chunkBox, s, 1, 9, stairState(sporeWoodStairsBlock, Direction.NORTH, Half.BOTTOM));
            setRel(level, chunkBox, s, 1, 8, planks);

            // West (-X) boardwalk stairs ascending East onto dy = 1
            setRel(level, chunkBox, -9, 0, s, planks);
            setRel(level, chunkBox, -9, 1, s, stairState(sporeWoodStairsBlock, Direction.EAST, Half.BOTTOM));
            setRel(level, chunkBox, -8, 1, s, planks);

            // East (+X) boardwalk stairs ascending West onto dy = 1
            setRel(level, chunkBox, 9, 0, s, planks);
            setRel(level, chunkBox, 9, 1, s, stairState(sporeWoodStairsBlock, Direction.WEST, Half.BOTTOM));
            setRel(level, chunkBox, 8, 1, s, planks);
        }

        // 3. Spore-Wood Fence Perimeter Railings along the boardwalk flanks & terrace arcs (dy = 2)
        int[][] fenceRuns = {
                // North boardwalk flank railings
                {-2, -8}, {-3, -8}, {2, -8}, {3, -8},
                // South boardwalk flank railings
                {-2, 8}, {-3, 8}, {2, 8}, {3, 8},
                // West boardwalk flank railings
                {-8, -2}, {-8, -3}, {-8, 2}, {-8, 3},
                // East boardwalk flank railings
                {8, -2}, {8, -3}, {8, 2}, {8, 3},
                // NW, SW, SE diagonal terrace posts (NE left open for scenic Giant Glaze-Cap Mushroom!)
                {-6, -5}, {-5, -6},
                {-6, 5}, {-5, 6},
                {6, 5}, {5, 6}
        };
        for (int[] pos : fenceRuns) {
            placeConnectedFence(level, chunkBox, pos[0], 2, pos[1], sporeWoodFenceBlock);
        }
        // Refresh connections once all posts in the run are placed
        for (int[] pos : fenceRuns) {
            placeConnectedFence(level, chunkBox, pos[0], 2, pos[1], sporeWoodFenceBlock);
        }
    }

    private void buildParabolicBiodomeShell(WorldGenLevel level, BoundingBox chunkBox) {
        Block sporeWoodBlock = resolveBlock("spore_wood", Blocks.SPRUCE_WOOD);
        Block sporeWoodStairsBlock = resolveBlock("spore_wood_stairs", Blocks.SPRUCE_STAIRS);
        Block glazeCapBlock = resolveBlock("glaze_cap_mushroom_block", Blocks.BROWN_MUSHROOM_BLOCK);
        Block sporeGlassBlock = resolveBlock("spore_glass", Blocks.GREEN_STAINED_GLASS);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState sporeWoodY = logWithAxis(sporeWoodBlock, Direction.Axis.Y);
        BlockState sporeWoodX = logWithAxis(sporeWoodBlock, Direction.Axis.X);
        BlockState sporeWoodZ = logWithAxis(sporeWoodBlock, Direction.Axis.Z);
        BlockState glazeCap = nonWaterlogged(glazeCapBlock.defaultBlockState());
        BlockState sporeGlass = nonWaterlogged(sporeGlassBlock.defaultBlockState());

        // Parabolic dome rising from dy = 2 up to the 15-block-tall apex skylight at dy = 15
        for (int dy = 2; dy <= 15; dy++) {
            if (dy == 15) {
                // 15-block-tall Apex Skylight Crown: wide green-tinted spore_glass oculus framed by spore_wood arch crown
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        double r = Math.hypot(dx, dz);
                        if (r > 1.85D) {
                            continue;
                        }
                        boolean isArchMeetingRib = (Math.abs(dx) == 1 && Math.abs(dz) == 1);
                        setRel(level, chunkBox, dx, 15, dz, isArchMeetingRib ? sporeWoodY : sporeGlass);
                    }
                }
                continue;
            }

            double hFrac = (dy - 2.0D) / 13.0D;
            double centerR = 6.5D * Math.sqrt(Math.max(0.04D, 1.0D - Math.pow(hFrac, 1.75D)));
            double minR = Math.max(0.75D, centerR - 0.62D);
            double maxR = centerR + 0.58D;

            for (int dx = -7; dx <= 7; dx++) {
                for (int dz = -7; dz <= 7; dz++) {
                    double r = Math.hypot(dx, dz);
                    if (r < minR || r > maxR) {
                        continue;
                    }

                    // 4 Cardinal Arched Doorways (dy = 2..4)
                    boolean inNorthPortal = (dz < -4 && Math.abs(dx) <= 1 && dy <= 4);
                    boolean inSouthPortal = (dz > 4 && Math.abs(dx) <= 1 && dy <= 4);
                    boolean inWestPortal = (dx < -4 && Math.abs(dz) <= 1 && dy <= 4);
                    boolean inEastPortal = (dx > 4 && Math.abs(dz) <= 1 && dy <= 4);
                    if (inNorthPortal || inSouthPortal || inWestPortal || inEastPortal) {
                        if (dy == 4 && Math.abs(dx) == 1 && (inNorthPortal || inSouthPortal)) {
                            Direction archFace = (dx < 0) ? Direction.EAST : Direction.WEST;
                            setRel(level, chunkBox, dx, 4, dz, stairState(sporeWoodStairsBlock, archFace, Half.TOP));
                        } else if (dy == 4 && Math.abs(dz) == 1 && (inWestPortal || inEastPortal)) {
                            Direction archFace = (dz < 0) ? Direction.SOUTH : Direction.NORTH;
                            setRel(level, chunkBox, dx, 4, dz, stairState(sporeWoodStairsBlock, archFace, Half.TOP));
                        } else {
                            setRel(level, chunkBox, dx, dy, dz, air);
                        }
                        continue;
                    }

                    // Portal jambs & arch lintels in all-bark entropica:spore_wood
                    boolean isPortalFrame = (dy <= 5) && (
                            (Math.abs(dz) >= 5 && Math.abs(dx) == 2)
                                    || (Math.abs(dx) >= 5 && Math.abs(dz) == 2)
                                    || (dy == 5 && ((Math.abs(dz) >= 5 && Math.abs(dx) <= 1) || (Math.abs(dx) >= 5 && Math.abs(dz) <= 1)))
                    );

                    // 4 Sweeping Diagonal Parabolic Arches of all-bark entropica:spore_wood meeting at apex skylight!
                    boolean isParabolicArchRib = Math.abs(Math.abs(dx) - Math.abs(dz)) == 0
                            || (dy >= 12 && Math.abs(Math.abs(dx) - Math.abs(dz)) <= 1 && Math.abs(dx) >= 1 && Math.abs(dz) >= 1);

                    if (isParabolicArchRib) {
                        setRel(level, chunkBox, dx, dy, dz, sporeWoodY);
                    } else if (isPortalFrame) {
                        BlockState frameState = (dy == 5)
                                ? (Math.abs(dz) >= 5 ? sporeWoodX : sporeWoodZ)
                                : sporeWoodY;
                        setRel(level, chunkBox, dx, dy, dz, frameState);
                    } else {
                        // Infill: curved glaze_cap_mushroom_block lower/structural roof panels + wide spore_glass skylights!
                        boolean isSkylightWindow = (dy >= 5 && dy <= 11 && (Math.abs(dx) <= 2 || Math.abs(dz) <= 2))
                                || (dy >= 12 && (dx == 0 || dz == 0));
                        setRel(level, chunkBox, dx, dy, dz, isSkylightWindow ? sporeGlass : glazeCap);
                    }
                }
            }
        }
    }

    private void buildAirPurifiedInterior(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block sporeGlazedIceBlock = resolveBlock("spore_glazed_ice", Blocks.PACKED_ICE);
        Block frozenSporeMossBlock = resolveBlock("frozen_spore_moss", Blocks.MOSS_BLOCK);
        Block frozenSporeMossCarpetBlock = resolveBlock("frozen_spore_moss_carpet", Blocks.MOSS_CARPET);
        Block sporeWoodBlock = resolveBlock("spore_wood", Blocks.SPRUCE_WOOD);
        Block sporeWoodPlanksBlock = resolveBlock("spore_wood_planks", Blocks.SPRUCE_PLANKS);
        Block sporeWoodSlabBlock = resolveBlock("spore_wood_slab", Blocks.SPRUCE_SLAB);
        Block sporeGlassBlock = resolveBlock("spore_glass", Blocks.GREEN_STAINED_GLASS);
        Block cryoSporeCenserBlock = resolveBlock("cryo_spore_censer", Blocks.LANTERN);
        Block sporeBellowsBlock = resolveBlock("spore_bellows", Blocks.DISPENSER);
        Optional<Block> pitcherPlumpOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_bearing_pitcher_plump"));
        Optional<Block> puffballOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_burst_puffball"));

        BlockState sporeGlazedIce = sporeGlazedIceBlock.defaultBlockState();
        BlockState frozenSporeMoss = frozenSporeMossBlock.defaultBlockState();
        BlockState mossCarpet = nonWaterlogged(frozenSporeMossCarpetBlock.defaultBlockState());
        BlockState sporeWoodY = logWithAxis(sporeWoodBlock, Direction.Axis.Y);
        BlockState planks = sporeWoodPlanksBlock.defaultBlockState();
        BlockState bottomSlab = bottomSlabState(sporeWoodSlabBlock);
        BlockState sporeGlass = nonWaterlogged(sporeGlassBlock.defaultBlockState());
        BlockState water = Blocks.WATER.defaultBlockState();

        // 1. Central Spore-Glazed Ice & Water Cryo-Irrigation Font (ZERO Cores or Anchors!)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                setRel(level, chunkBox, dx, 0, dz, sporeGlazedIce);
                if (dx == 0 && dz == 0) {
                    setRel(level, chunkBox, 0, 1, 0, sporeGlazedIce);
                    setRel(level, chunkBox, 0, 2, 0, sporeGlazedIce);
                    setRel(level, chunkBox, 0, 3, 0, sporeGlass);
                } else if (Math.abs(dx) + Math.abs(dz) == 1) {
                    // Cardinal cryo-water basins sealed on all sides at dy = 1
                    setRel(level, chunkBox, dx, 1, dz, water);
                } else {
                    // Diagonal basin corners of glowing spore_glazed_ice topped with slabs
                    setRel(level, chunkBox, dx, 1, dz, sporeGlazedIce);
                    setRel(level, chunkBox, dx, 2, dz, bottomSlab);
                }
            }
        }

        // 2. 4 Quadrant Raised Spore-Wood Slab Planter Beds filled with Frozen Spore Moss & Cultivated Flora
        for (int q = 0; q < QUADRANT_SIGNS.length; q++) {
            int sx = QUADRANT_SIGNS[q][0];
            int sz = QUADRANT_SIGNS[q][1];

            // Inner walkway curb of raised spore_wood_slab at dy = 2
            setRel(level, chunkBox, sx * 2, 2, sz * 2, bottomSlab);
            setRel(level, chunkBox, sx * 3, 2, sz * 2, bottomSlab);
            setRel(level, chunkBox, sx * 2, 2, sz * 3, bottomSlab);
            setRel(level, chunkBox, sx * 4, 2, sz * 2, bottomSlab);
            setRel(level, chunkBox, sx * 2, 2, sz * 4, bottomSlab);

            // Cultivated frozen_spore_moss soil bed (2x2 in each quadrant at dy = 2)
            int[][] mossCells = {
                    {sx * 3, sz * 3},
                    {sx * 4, sz * 3},
                    {sx * 3, sz * 4},
                    {sx * 4, sz * 4}
            };
            for (int[] cell : mossCells) {
                setRel(level, chunkBox, cell[0], 2, cell[1], frozenSporeMoss);
            }

            // Cultivate spore_bearing_pitcher_plump (FACING = UP), spore_burst_puffball & frozen_spore_moss_carpet at dy = 3
            if (pitcherPlumpOpt.isPresent()) {
                BlockState pitcherUp = orientUpward(pitcherPlumpOpt.get());
                setRel(level, chunkBox, sx * 3, 3, sz * 3, pitcherUp);
            } else {
                setRel(level, chunkBox, sx * 3, 3, sz * 3, Blocks.BROWN_MUSHROOM.defaultBlockState());
            }

            if (puffballOpt.isPresent()) {
                BlockState puffball = nonWaterlogged(puffballOpt.get().defaultBlockState());
                setRel(level, chunkBox, sx * 4, 3, sz * 3, puffball);
            } else {
                setRel(level, chunkBox, sx * 4, 3, sz * 3, mossCarpet);
            }

            setRel(level, chunkBox, sx * 3, 3, sz * 4, mossCarpet);
            if ((q & 1) == 0 && pitcherPlumpOpt.isPresent()) {
                setRel(level, chunkBox, sx * 4, 3, sz * 4, orientUpward(pitcherPlumpOpt.get()));
            } else {
                setRel(level, chunkBox, sx * 4, 3, sz * 4, mossCarpet);
            }
        }

        // 3. 2 Inward-Facing Spore Bellows tending the quadrant planter beds
        setRel(level, chunkBox, -3, 2, -2, orientHorizontalMachine(sporeBellowsBlock, Direction.SOUTH));
        setRel(level, chunkBox, 3, 2, 2, orientHorizontalMachine(sporeBellowsBlock, Direction.NORTH));

        // 4. 4 Hanging & Standing Cryo-Spore Censer Lanterns purifying the conservatory air!
        //    - 2 Hanging Cryo-Spore Censers suspended underneath solid spore_wood corbels at dy = 8 -> censer at dy = 7
        setRel(level, chunkBox, -3, 8, -3, sporeWoodY);
        setRel(level, chunkBox, -3, 7, -3, censerState(cryoSporeCenserBlock, true));
        setRel(level, chunkBox, 3, 8, 3, sporeWoodY);
        setRel(level, chunkBox, 3, 7, 3, censerState(cryoSporeCenserBlock, true));

        //    - 2 Standing Cryo-Spore Censers atop solid spore_wood pedestals along the NE/SW interior piers
        setRel(level, chunkBox, 2, 2, -2, sporeWoodY);
        setRel(level, chunkBox, 2, 3, -2, censerState(cryoSporeCenserBlock, false));
        setRel(level, chunkBox, -2, 2, 2, sporeWoodY);
        setRel(level, chunkBox, -2, 3, 2, censerState(cryoSporeCenserBlock, false));

        // 5. Apothecary Counter (BrewingStand) + 2 Loot Chests (entropica:chests/glazed_mycelial_conservatory)
        setRel(level, chunkBox, -5, 2, -2, planks);
        setRel(level, chunkBox, -5, 3, -2, Blocks.BREWING_STAND.defaultBlockState());
        setRel(level, chunkBox, -5, 2, 2, planks);
        setRel(level, chunkBox, -5, 3, 2, Blocks.POTTED_BROWN_MUSHROOM.defaultBlockState());

        placeConservatoryChest(level, chunkBox, random, 5, 2, -2, Direction.WEST);
        placeConservatoryChest(level, chunkBox, random, 5, 2, 2, Direction.WEST);
    }

    private void buildExteriorCornerGlazeCapMushroom(WorldGenLevel level, BoundingBox chunkBox) {
        // STRICT USER RULE: Exterior Giant Frozen Glaze-Cap Mushroom MUST use vanilla Blocks.MUSHROOM_STEM!
        BlockState mushroomStem = Blocks.MUSHROOM_STEM.defaultBlockState();
        Block glazeCapBlock = resolveBlock("glaze_cap_mushroom_block", Blocks.BROWN_MUSHROOM_BLOCK);
        BlockState glazeCap = nonWaterlogged(glazeCapBlock.defaultBlockState());
        Optional<Block> shelfCapOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "spore_fungal_shelf_cap"));

        // Root flares & curved stalk rising from NE terrace outcrop (6, 2, -6) -> (7, 9, -7)
        setRel(level, chunkBox, 6, 2, -6, mushroomStem);
        setRel(level, chunkBox, 7, 2, -6, mushroomStem);
        setRel(level, chunkBox, 6, 2, -7, mushroomStem);

        for (int dy = 3; dy <= 5; dy++) {
            setRel(level, chunkBox, 6, dy, -6, mushroomStem);
        }
        // Smooth bend elbow at dy = 6 stepping out to (7, *, -7) clear of the biodome shell
        setRel(level, chunkBox, 6, 6, -6, mushroomStem);
        setRel(level, chunkBox, 7, 6, -7, mushroomStem);
        for (int dy = 7; dy <= 9; dy++) {
            setRel(level, chunkBox, 7, dy, -7, mushroomStem);
        }

        // Attach a horizontal spore_fungal_shelf_cap bracket to the Blocks.MUSHROOM_STEM stalk
        if (shelfCapOpt.isPresent()) {
            setRel(level, chunkBox, 7, 4, -6, orientHorizontalShelf(shelfCapOpt.get(), Direction.EAST));
        }

        // Umbrella Dome centered at (7, 9..10, -7) with radius 2 (dx = 5..9, dz = -9..-5 — 100% inside -10..9!)
        int capCx = 7;
        int capCz = -7;
        // Upper crown tier (dy = 10): 3x3 solid crown
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                setRel(level, chunkBox, capCx + ox, 10, capCz + oz, glazeCap);
            }
        }
        // Main umbrella bell skirt (dy = 9): radius 2 outer ring (omitting extreme corners so hollow & clear of dome!)
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                if (ox == 0 && oz == 0) {
                    continue; // Central Blocks.MUSHROOM_STEM
                }
                int maxAxis = Math.max(Math.abs(ox), Math.abs(oz));
                boolean isCorner = Math.abs(ox) == 2 && Math.abs(oz) == 2;
                if (maxAxis == 2 && !isCorner) {
                    setRel(level, chunkBox, capCx + ox, 9, capCz + oz, glazeCap);
                }
            }
        }
        // Drooping outer rim tabs (dy = 8) on away-from-dome sides (+X and -Z)
        setRel(level, chunkBox, capCx + 2, 8, capCz, glazeCap);
        setRel(level, chunkBox, capCx, 8, capCz - 2, glazeCap);
        setRel(level, chunkBox, capCx + 2, 8, capCz - 2, glazeCap);
    }

    private void placeConservatoryChest(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random,
            int dx,
            int dy,
            int dz,
            Direction facing
    ) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState chestState = Blocks.CHEST.defaultBlockState()
                .setValue(ChestBlock.FACING, facing)
                .setValue(ChestBlock.WATERLOGGED, false);
        level.setBlock(pos, chestState, 2);
        RandomizableContainer.setBlockEntityLootTable(
                level, random, pos, ModStructures.GLAZED_MYCELIAL_CONSERVATORY_CHEST_LOOT);
    }

    private void placeConnectedFence(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int dy,
            int dz,
            Block fenceBlock
    ) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState state = nonWaterlogged(fenceBlock.defaultBlockState());
        for (Direction horiz : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = pos.relative(horiz);
            if (!chunkBox.isInside(neighborPos)) {
                continue;
            }
            BlockState neighbor = level.getBlockState(neighborPos);
            boolean connects = neighbor.is(fenceBlock)
                    || neighbor.isFaceSturdy(level, neighborPos, horiz.getOpposite());
            state = setFenceSide(state, horiz, connects);
        }
        level.setBlock(pos, state, 2);
    }

    private static BlockState setFenceSide(BlockState state, Direction dir, boolean value) {
        return switch (dir) {
            case NORTH -> state.hasProperty(CrossCollisionBlock.NORTH)
                    ? state.setValue(CrossCollisionBlock.NORTH, value) : state;
            case SOUTH -> state.hasProperty(CrossCollisionBlock.SOUTH)
                    ? state.setValue(CrossCollisionBlock.SOUTH, value) : state;
            case WEST -> state.hasProperty(CrossCollisionBlock.WEST)
                    ? state.setValue(CrossCollisionBlock.WEST, value) : state;
            case EAST -> state.hasProperty(CrossCollisionBlock.EAST)
                    ? state.setValue(CrossCollisionBlock.EAST, value) : state;
            default -> state;
        };
    }

    private static BlockState stairState(Block stairBlock, Direction facing, Half half) {
        BlockState state = nonWaterlogged(stairBlock.defaultBlockState());
        if (state.hasProperty(StairBlock.FACING)) {
            state = state.setValue(StairBlock.FACING, facing);
        }
        if (state.hasProperty(StairBlock.HALF)) {
            state = state.setValue(StairBlock.HALF, half);
        }
        return state;
    }

    private static BlockState bottomSlabState(Block slabBlock) {
        BlockState state = nonWaterlogged(slabBlock.defaultBlockState());
        if (state.hasProperty(SlabBlock.TYPE)) {
            state = state.setValue(SlabBlock.TYPE, SlabType.BOTTOM);
        }
        return state;
    }

    private static BlockState censerState(Block censerBlock, boolean hanging) {
        BlockState state = nonWaterlogged(censerBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.HANGING)) {
            state = state.setValue(BlockStateProperties.HANGING, hanging);
        }
        return state;
    }

    private static BlockState orientUpward(Block block) {
        BlockState state = nonWaterlogged(block.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.FACING)
                && BlockStateProperties.FACING.getPossibleValues().contains(Direction.UP)) {
            state = state.setValue(BlockStateProperties.FACING, Direction.UP);
        }
        return state;
    }

    private static BlockState orientHorizontalMachine(Block machineBlock, Direction facing) {
        BlockState state = nonWaterlogged(machineBlock.defaultBlockState());
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, facing);
        }
        if (state.hasProperty(BlockStateProperties.FACING)
                && BlockStateProperties.FACING.getPossibleValues().contains(facing)) {
            return state.setValue(BlockStateProperties.FACING, facing);
        }
        return state;
    }

    private static BlockState orientHorizontalShelf(Block shelfBlock, Direction outwardHoriz) {
        BlockState state = nonWaterlogged(shelfBlock.defaultBlockState());
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.setValue(HorizontalDirectionalBlock.FACING, outwardHoriz);
        }
        if (state.hasProperty(BlockStateProperties.FACING)
                && BlockStateProperties.FACING.getPossibleValues().contains(outwardHoriz)) {
            return state.setValue(BlockStateProperties.FACING, outwardHoriz);
        }
        return state;
    }

    private static BlockState logWithAxis(Block logBlock, Direction.Axis axis) {
        BlockState state = logBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, axis);
        }
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            return state.setValue(BlockStateProperties.AXIS, axis);
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private BlockPos toWorld(int dx, int dy, int dz) {
        return new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
    }

    private void setRel(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (chunkBox.isInside(pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillFoundationDown(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int startDy,
            int dz,
            BlockState state
    ) {
        int wx = this.originX + dx;
        int wz = this.originZ + dz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 16);
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
