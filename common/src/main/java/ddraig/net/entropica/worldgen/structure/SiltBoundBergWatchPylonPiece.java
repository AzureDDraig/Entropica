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
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

/**
 * Programmatic 3D generator for Biome #15's core-free sanctuary:
 * {@code Silt-Bound Berg Watch-Pylon} ({@code 20 x 20 x 22},
 * {@code dx = -10..9, dy = -7..14, dz = -10..9} — strictly ZERO Shrine Cores or Anchors):
 * <ul>
 *   <li><b>1. Compressed Flat-Topped Table-Iceberg Plinth ({@code dy = -7..0, r ~ 9.5})</b>:
 *       {@link Blocks#BLUE_ICE} &amp; {@link Blocks#PACKED_ICE} lower cliffs extending down to
 *       {@code seaLevel - 5} (or solid seabed so it NEVER floats!), {@code entropica:dust_crusted_ice}
 *       upper rim &amp; sub-cap, and a flat plateau topped with supported {@code entropica:grey_sand},
 *       {@code entropica:coarse_gravel} scree, {@link Blocks#TUFF_BRICKS} courtyard pad, and
 *       scattered {@code entropica:wind_blown_dust_scrub}.</li>
 *   <li><b>2. Three Exterior Hanging {@code tough_fiber_rope} Boarding Lines</b>:
 *       Anchored to weathered {@link Blocks#MUD_BRICKS} cliff bollards on the South, South-East,
 *       and East rims, hanging down the sheer iceberg face to {@code seaLevel} / ocean water so
 *       players can climb straight aboard from the sea.</li>
 *   <li><b>3. Octagonal {@code mud_bricks} &amp; {@code tuff_bricks} Windbreak Shelter ({@code dy = 1..5})</b>:
 *       North/South arched doorways, {@code entropica:dust_glass} storm-slit observation windows
 *       ({@code dy = 3}), ceiling {@code entropica:wind_baffle_lantern} beacon at {@code (0, 4, 0)},
 *       and Prospector Loot Chest #1.</li>
 *   <li><b>4. Interior {@code tough_fiber_rope} Central Climbing Line ({@code dy = 1..11}) &amp;
 *       Upper Watch-Pylon ({@code dy = 6..12})</b>:
 *       Ascends through the stairwell opening into the Watch-Pylon gallery &amp; crow's-nest loft
 *       with {@code entropica:dust_glass} lookout slits, {@code 4} Cardinal {@link Blocks#CUT_COPPER}
 *       Wind-Vane Sails ({@code dy = 9..11}), Prospector Loot Chest #2, and a crowning
 *       {@code entropica:wind_baffle_lantern} beacon atop the copper finial at {@code (0, 14, 0)}.</li>
 * </ul>
 */
public class SiltBoundBergWatchPylonPiece extends StructurePiece {

    private static final int[][] PLATEAU_SCRUB_CANDIDATES = {
            {-7, -4}, {-6, 5}, {-5, -6}, {5, -6}, {-7, 3}, {3, -7}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public SiltBoundBergWatchPylonPiece(int originX, int baseY, int originZ) {
        // Exact 20 (X: -10..+9) x 22 (Y: -7..+14) x 20 (Z: -10..+9) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 10,
                baseY - 7,
                originZ - 10,
                originX + 9,
                baseY + 14,
                originZ + 9
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public SiltBoundBergWatchPylonPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 10);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 7);
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 10);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.SILT_BOUND_BERG_WATCH_PYLON_PIECE.isPresent()) {
            return ModStructures.SILT_BOUND_BERG_WATCH_PYLON_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "sbbwp");
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

    public BlockPos getShelterBeaconPos() {
        return toWorld(0, 4, 0);
    }

    public BlockPos getFinialBeaconPos() {
        return toWorld(0, 14, 0);
    }

    public BlockPos getShelterChestPos() {
        return toWorld(-3, 1, 0);
    }

    public BlockPos getLookoutChestPos() {
        return toWorld(-2, 10, 0);
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
        buildTableIcebergPlinthAndCourtyard(level, chunkBox);
        buildOctagonalWindbreakShelter(level, chunkBox, random);
        buildUpperWatchPylonWindVanesAndClimbingLine(level, chunkBox, random);
        buildExteriorHangingBoardingLines(level, chunkBox);
    }

    private void buildTableIcebergPlinthAndCourtyard(WorldGenLevel level, BoundingBox chunkBox) {
        Block dustCrustedIceBlock = resolveBlock("dust_crusted_ice", Blocks.PACKED_ICE);
        Block greySandBlock = resolveBlock("grey_sand", Blocks.SAND);
        Block coarseGravelBlock = resolveBlock("coarse_gravel", Blocks.GRAVEL);
        Optional<Block> scrubOpt = BuiltInRegistries.BLOCK.getOptional(
                ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "wind_blown_dust_scrub"));

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState dustCrustedIce = dustCrustedIceBlock.defaultBlockState();
        BlockState greySand = greySandBlock.defaultBlockState();
        BlockState coarseGravel = coarseGravelBlock.defaultBlockState();
        BlockState tuffBricks = Blocks.TUFF_BRICKS.defaultBlockState();
        BlockState mudBricks = Blocks.MUD_BRICKS.defaultBlockState();

        // Clear sky headroom across the entire 20 x 20 above-plateau volume (dy = 1..14)
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                for (int dy = 1; dy <= 14; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // 1. Compressed Flat-Topped Table-Iceberg Plinth (dy = -7..0, radius ~9.5 centered at (-0.4, -0.4))
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                if (!isInsidePlinthFootprint(dx, dz)) {
                    continue;
                }

                boolean isRim = isPlinthRimCell(dx, dz);

                // Lower sheer cliffs & submerged keel (dy = -7..-2): blue_ice & packed_ice
                for (int dy = -7; dy <= -2; dy++) {
                    boolean slightlyTaperedKeel = (dy <= -6) && isRim && (((dx + dz) & 1) != 0);
                    if (slightlyTaperedKeel) {
                        continue;
                    }
                    boolean useBlueIce = !isRim || dy <= -4 || (((dx * 3 + dz * 5 + dy) & 3) == 0);
                    setRel(level, chunkBox, dx, dy, dz, useBlueIce ? blueIce : packedIce);
                }

                // Anchor any air or shallow coastal seabed beneath dy = -7 so the plinth NEVER floats
                if (!isRim) {
                    fillFoundationDown(level, chunkBox, dx, -8, dz, ((dx + dz) & 1) == 0 ? blueIce : packedIce);
                }

                // Upper cliff rim sub-cap (dy = -1): 100% solid dust_crusted_ice so grey_sand NEVER falls!
                setRel(level, chunkBox, dx, -1, dz, dustCrustedIce);

                // Flat plateau top (dy = 0):
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                boolean inCentralCourtyard = (ax <= 5 && az <= 5 && (ax + az <= 7));
                boolean inNorthSouthWalkway = (ax <= 1 && dz >= -8 && dz <= 8);
                boolean inEastWalkway = (az <= 1 && dx >= 4 && dx <= 8);
                boolean inSeWalkway = (dx >= 3 && dz >= 3 && Math.abs(dx - dz) <= 1 && dx <= 6 && dz <= 6);

                if (inCentralCourtyard || inNorthSouthWalkway || inEastWalkway || inSeWalkway) {
                    BlockState courtTile = ((dx + dz) & 3) == 0 ? mudBricks : tuffBricks;
                    setRel(level, chunkBox, dx, 0, dz, courtTile);
                } else if (isRim) {
                    setRel(level, chunkBox, dx, 0, dz, dustCrustedIce);
                } else {
                    boolean screePocket = (((dx * 5 - dz * 3) & 7) <= 1);
                    setRel(level, chunkBox, dx, 0, dz, screePocket ? coarseGravel : greySand);
                }
            }
        }

        // Scatter hardy wind_blown_dust_scrub shrubs STRICTLY on grey_sand or dust_crusted_ice on the plateau
        if (scrubOpt.isPresent()) {
            BlockState scrubState = nonWaterlogged(scrubOpt.get().defaultBlockState());
            for (int[] pos : PLATEAU_SCRUB_CANDIDATES) {
                int sx = pos[0];
                int sz = pos[1];
                BlockPos groundWorld = toWorld(sx, 0, sz);
                BlockPos plantWorld = toWorld(sx, 1, sz);
                if (!chunkBox.isInside(plantWorld)) {
                    continue;
                }
                BlockState ground = level.getBlockState(groundWorld);
                if ((ground.is(greySandBlock) || ground.is(dustCrustedIceBlock))
                        && level.getBlockState(plantWorld).isAir()
                        && scrubState.canSurvive(level, plantWorld)) {
                    level.setBlock(plantWorld, scrubState, 2);
                }
            }
        }
    }

    private void buildOctagonalWindbreakShelter(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block dustGlassBlock = resolveBlock("dust_glass", Blocks.TINTED_GLASS);
        Block lanternBlock = resolveBlock("wind_baffle_lantern", Blocks.LANTERN);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState mudBricks = Blocks.MUD_BRICKS.defaultBlockState();
        BlockState tuffBricks = Blocks.TUFF_BRICKS.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState dustGlass = nonWaterlogged(dustGlassBlock.defaultBlockState());
        BlockState mudBottomSlab = slabState(Blocks.MUD_BRICK_SLAB, SlabType.BOTTOM);
        BlockState tuffTopSlab = slabState(Blocks.TUFF_BRICK_SLAB, SlabType.TOP);
        BlockState craftingTable = Blocks.CRAFTING_TABLE.defaultBlockState();

        // 1. Octagonal Windbreak Shelter Walls (dy = 1..4, max(|dx|,|dz|) <= 4 and |dx|+|dz| <= 6)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax + az > 6) {
                    continue;
                }

                boolean isWall = (ax == 4 || az == 4 || (ax + az == 6));
                if (!isWall) {
                    for (int dy = 1; dy <= 4; dy++) {
                        setRel(level, chunkBox, dx, dy, dz, air);
                    }
                    continue;
                }

                boolean isQuoin = (ax == 3 && az == 3)
                        || (ax == 2 && az == 4)
                        || (ax == 4 && az == 2);

                for (int dy = 1; dy <= 4; dy++) {
                    // North & South Arched Doorways (dz = +-4, dx = -1..1)
                    if (az == 4 && ax <= 1) {
                        if (dx == 0 && dy <= 3) {
                            setRel(level, chunkBox, dx, dy, dz, air);
                            continue;
                        }
                        if (ax == 1 && dy <= 2) {
                            setRel(level, chunkBox, dx, dy, dz, air);
                            continue;
                        }
                        if (ax == 1 && dy == 3) {
                            Direction archFace = (dx < 0) ? Direction.WEST : Direction.EAST;
                            setRel(level, chunkBox, dx, dy, dz,
                                    stairState(Blocks.TUFF_BRICK_STAIRS, archFace, Half.TOP));
                            continue;
                        }
                    }

                    // Storm-Slit Observation Windows in entropica:dust_glass at dy = 3!
                    boolean eastWestSlit = (dy == 3 && ax == 4 && az <= 1);
                    boolean diagonalSlit = (dy == 3 && ax == 3 && az == 3);
                    if (eastWestSlit || diagonalSlit) {
                        setRel(level, chunkBox, dx, dy, dz, dustGlass);
                        continue;
                    }

                    BlockState wallBlock = (dy == 1 || dy == 4 || isQuoin) ? tuffBricks : mudBricks;
                    setRel(level, chunkBox, dx, dy, dz, wallBlock);
                }
            }
        }

        // 2. Shelter Ceiling / Watch-Pylon Gallery Floor Deck (dy = 5) + Wind-Baffle Cornice Eaves
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax <= 4 && az <= 4 && (ax + az <= 6)) {
                    // Stairwell / central climbing rope hatch opening at dx = 1..2, dz = 0
                    if ((dx == 1 || dx == 2) && dz == 0) {
                        setRel(level, chunkBox, dx, 5, dz, air);
                    } else if (dx == 0 && dz == 0) {
                        setRel(level, chunkBox, 0, 5, 0, cutCopper);
                    } else {
                        setRel(level, chunkBox, dx, 5, dz, (ax + az <= 3) ? mudBricks : tuffBricks);
                    }
                } else if (ax <= 5 && az <= 5 && (ax + az <= 7)) {
                    setRel(level, chunkBox, dx, 5, dz, tuffTopSlab);
                }
            }
        }

        // 3. Wind-Baffle Lantern Beacon #1 in the ground windbreak shelter ceiling at exact (0, 4, 0)
        setRel(level, chunkBox, 0, 4, 0, lanternState(lanternBlock, true));

        // 4. Prospector Shelter Furnishings & Clean Loot Chest #1 at (-3, 1, 0)
        setRel(level, chunkBox, -3, 1, -1, craftingTable);
        setRel(level, chunkBox, -3, 1, 1, mudBottomSlab);
        placeWatchPylonChest(level, chunkBox, random, -3, 1, 0, Direction.EAST);
    }

    private void buildUpperWatchPylonWindVanesAndClimbingLine(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block dustGlassBlock = resolveBlock("dust_glass", Blocks.TINTED_GLASS);
        Block lanternBlock = resolveBlock("wind_baffle_lantern", Blocks.LANTERN);
        Block ropeBlock = resolveRopeBlock();

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState mudBricks = Blocks.MUD_BRICKS.defaultBlockState();
        BlockState tuffBricks = Blocks.TUFF_BRICKS.defaultBlockState();
        BlockState cutCopper = Blocks.CUT_COPPER.defaultBlockState();
        BlockState dustGlass = nonWaterlogged(dustGlassBlock.defaultBlockState());
        BlockState tuffBottomSlab = slabState(Blocks.TUFF_BRICK_SLAB, SlabType.BOTTOM);
        BlockState copperBottomSlab = slabState(Blocks.CUT_COPPER_SLAB, SlabType.BOTTOM);
        BlockState copperTopSlab = slabState(Blocks.CUT_COPPER_SLAB, SlabType.TOP);

        // 1. Upper Watch-Pylon Tower Walls (dy = 6..11, chamfered 7x7: max(|dx|,|dz|) <= 3, |dx|+|dz| <= 5)
        for (int dy = 6; dy <= 11; dy++) {
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    int ax = Math.abs(dx);
                    int az = Math.abs(dz);
                    if (ax + az > 5) {
                        continue;
                    }

                    boolean isOuterWall = (ax == 3 || az == 3 || (ax + az == 5));
                    if (!isOuterWall) {
                        // Upper Crow's-Nest Loft Floor at dy = 9 on West side (dx <= 0), leaving East climbing shaft open!
                        if (dy == 9 && dx <= 0) {
                            setRel(level, chunkBox, dx, dy, dz, (dx == 0) ? tuffBricks : mudBricks);
                        } else {
                            setRel(level, chunkBox, dx, dy, dz, air);
                        }
                        continue;
                    }

                    // Dust-Glass Lookout Slits:
                    // - Mid gallery lookout slits at dy = 7..8 on all 4 cardinal centers
                    // - Upper loft panoramic lookout slits at dy = 10..11 flanking the cardinal wind-vane mounts
                    boolean lowerCardinalSlit = (dy == 7 || dy == 8)
                            && ((ax == 3 && dz == 0) || (az == 3 && dx == 0));
                    boolean upperFlankingSlit = (dy == 10)
                            && ((ax == 3 && az == 1) || (az == 3 && ax == 1));
                    if (lowerCardinalSlit || upperFlankingSlit) {
                        setRel(level, chunkBox, dx, dy, dz, dustGlass);
                        continue;
                    }

                    // Copper Wind-Vane Hub Mounts on the 4 cardinal faces at dy = 9..11
                    boolean cardinalVaneMount = (dy >= 9 && dy <= 11)
                            && ((ax == 3 && dz == 0) || (az == 3 && dx == 0));
                    if (cardinalVaneMount) {
                        setRel(level, chunkBox, dx, dy, dz, cutCopper);
                        continue;
                    }

                    boolean isTowerQuoin = (ax + az == 5) || dy == 6 || dy == 11;
                    setRel(level, chunkBox, dx, dy, dz, isTowerQuoin ? tuffBricks : mudBricks);
                }
            }
        }

        // 2. Four Cardinal cut_copper Wind-Vane Sails (dy = 9..11) projecting outward to radius 4..5
        // North (-Z) & South (+Z) Wind-Vane Sails
        for (int sz : new int[]{-1, 1}) {
            setRel(level, chunkBox, 0, 9, sz * 4, copperTopSlab);
            setRel(level, chunkBox, 0, 10, sz * 4, cutCopper);
            setRel(level, chunkBox, 0, 11, sz * 4, cutCopper);
            setRel(level, chunkBox, 0, 10, sz * 5, cutCopper);
            setRel(level, chunkBox, 0, 11, sz * 5, copperBottomSlab);
        }
        // West (-X) & East (+X) Wind-Vane Sails
        for (int sx : new int[]{-1, 1}) {
            setRel(level, chunkBox, sx * 4, 9, 0, copperTopSlab);
            setRel(level, chunkBox, sx * 4, 10, 0, cutCopper);
            setRel(level, chunkBox, sx * 4, 11, 0, cutCopper);
            setRel(level, chunkBox, sx * 5, 10, 0, cutCopper);
            setRel(level, chunkBox, sx * 5, 11, 0, copperBottomSlab);
        }

        // 3. Watch-Pylon Cap Roof (dy = 12), Copper Finial (dy = 13) & Crowning Beacon #2 at exact (0, 14, 0)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax + az > 5) {
                    continue;
                }
                if (ax == 3 || az == 3 || (ax + az == 5)) {
                    setRel(level, chunkBox, dx, 12, dz, tuffBottomSlab);
                } else if (ax <= 1 && az <= 1) {
                    setRel(level, chunkBox, dx, 12, dz, cutCopper);
                } else {
                    setRel(level, chunkBox, dx, 12, dz, tuffBricks);
                }
            }
        }
        setRel(level, chunkBox, 0, 13, 0, cutCopper);
        setRel(level, chunkBox, -1, 13, 0, copperBottomSlab);
        setRel(level, chunkBox, 1, 13, 0, copperBottomSlab);
        setRel(level, chunkBox, 0, 13, -1, copperBottomSlab);
        setRel(level, chunkBox, 0, 13, 1, copperBottomSlab);
        setRel(level, chunkBox, 0, 14, 0, lanternState(lanternBlock, false));

        // 4. Clean Prospector Loot Chest #2 on the Upper Crow's-Nest Loft at (-2, 10, 0)
        placeWatchPylonChest(level, chunkBox, random, -2, 10, 0, Direction.EAST);

        // 5. Interior tough_fiber_rope Central Climbing Line (dy = 1..11) at (1, 1..11, 0)
        //    Placed TOP-DOWN beneath solid (1, 12, 0) cut_copper roof beam down to (1, 0, 0) tuff_bricks floor!
        BlockState dryRope = ropeState(ropeBlock, false);
        for (int dy = 11; dy >= 1; dy--) {
            setRel(level, chunkBox, 1, dy, 0, dryRope);
        }
    }

    private void buildExteriorHangingBoardingLines(WorldGenLevel level, BoundingBox chunkBox) {
        Block ropeBlock = resolveRopeBlock();
        BlockState mudBricks = Blocks.MUD_BRICKS.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int seaLevel = level.getSeaLevel() > 0 ? level.getSeaLevel() : 63;

        // 3 Exterior Boarding Lines on South, South-East, and East cliff rims:
        // { rimDx, rimDz, pierDx, pierDz, ropeDx, ropeDz }
        int[][] boardingStations = {
                // 1. South Rim (aligned with South arched shelter doorway)
                {0, 8, -1, 8, 0, 9},
                // 2. South-East Rim (faceted diagonal corner)
                {6, 6, 6, 5, 7, 6},
                // 3. East Rim
                {8, 0, 8, -1, 9, 0}
        };

        for (int[] st : boardingStations) {
            int rimDx = st[0];
            int rimDz = st[1];
            int pierDx = st[2];
            int pierDz = st[3];
            int ropeDx = st[4];
            int ropeDz = st[5];

            // Weathered mud_bricks cliff threshold & 3-block side bollard pier + overhanging davit arm at dy = 3
            // Keeps (rimDx, 1..2, rimDz) open air so climbers walk straight off the rope onto the plateau!
            setRel(level, chunkBox, rimDx, 0, rimDz, mudBricks);
            setRel(level, chunkBox, rimDx, 1, rimDz, air);
            setRel(level, chunkBox, rimDx, 2, rimDz, air);
            setRel(level, chunkBox, pierDx, 0, pierDz, mudBricks);
            for (int dy = 1; dy <= 3; dy++) {
                setRel(level, chunkBox, pierDx, dy, pierDz, mudBricks);
            }
            setRel(level, chunkBox, rimDx, 3, rimDz, mudBricks);
            setRel(level, chunkBox, ropeDx, 3, ropeDz, mudBricks);

            // Hang tough_fiber_rope top-down from dy = 2 down the sheer iceberg face to seaLevel - 1 (water surface)
            int targetBottomWy = Math.min(this.baseY - 1, seaLevel - 1);
            int minRopeDy = Math.max(-7, targetBottomWy - this.baseY);

            for (int dy = 2; dy >= minRopeDy; dy--) {
                BlockPos ropeWorld = toWorld(ropeDx, dy, ropeDz);
                if (!chunkBox.isInside(ropeWorld)) {
                    continue;
                }
                BlockState existing = level.getBlockState(ropeWorld);
                boolean inWater = existing.is(Blocks.WATER)
                        || existing.getFluidState().getType() == Fluids.WATER
                        || (ropeWorld.getY() <= seaLevel - 1 && existing.is(Blocks.ICE));
                if (dy < 0 && !existing.isAir() && !inWater && !existing.is(Blocks.SNOW) && !existing.canBeReplaced()) {
                    break;
                }
                level.setBlock(ropeWorld, ropeState(ropeBlock, inWater), 2);
            }
        }
    }

    private static boolean isInsidePlinthFootprint(int dx, int dz) {
        // Keep the 3 exterior hanging boarding-rope columns strictly outside the sheer cliff wall!
        if ((dx == 0 && dz >= 9)
                || (dx >= 9 && dz == 0)
                || (dx >= 7 && dz >= 6)
                || (dx >= 6 && dz >= 7)) {
            return false;
        }
        // Ensure the 3 boarding rim cells & piers are solidly inside the table-iceberg plinth
        if ((Math.abs(dx) <= 1 && dz == 8)
                || (dx == 8 && Math.abs(dz) <= 1)
                || (dx == 6 && (dz == 5 || dz == 6))
                || (dz == 6 && (dx == 5 || dx == 6))) {
            return true;
        }

        double cx = dx + 0.4D;
        double cz = dz + 0.4D;
        double r = Math.hypot(cx, cz);
        double angle = Math.atan2(cz, cx);
        double facet = 9.05D + 0.35D * Math.cos(angle * 4.0D);
        return r <= facet && dx >= -9 && dx <= 8 && dz >= -9 && dz <= 8;
    }

    private static boolean isPlinthRimCell(int dx, int dz) {
        for (Direction horiz : Direction.Plane.HORIZONTAL) {
            if (!isInsidePlinthFootprint(dx + horiz.getStepX(), dz + horiz.getStepZ())) {
                return true;
            }
        }
        return false;
    }

    private void placeWatchPylonChest(
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
                level, random, pos, ModStructures.SILT_BOUND_BERG_WATCH_PYLON_CHEST_LOOT);
    }

    private static Block resolveRopeBlock() {
        ResourceLocation ropeId = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "tough_fiber_rope");
        Optional<Block> ropeOpt = BuiltInRegistries.BLOCK.getOptional(ropeId);
        if (ropeOpt.isPresent()) {
            return ropeOpt.get();
        }
        return BuiltInRegistries.BLOCK.getOptional(ResourceLocation.withDefaultNamespace("iron_chain"))
                .orElse(Blocks.OAK_FENCE);
    }

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static BlockState ropeState(Block ropeBlock, boolean waterlogged) {
        BlockState state = ropeBlock.defaultBlockState();
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        return state;
    }

    private static BlockState lanternState(Block lanternBlock, boolean hanging) {
        BlockState state = nonWaterlogged(lanternBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.HANGING)) {
            state = state.setValue(BlockStateProperties.HANGING, hanging);
        }
        return state;
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

    private static BlockState slabState(Block slabBlock, SlabType type) {
        BlockState state = nonWaterlogged(slabBlock.defaultBlockState());
        if (state.hasProperty(SlabBlock.TYPE)) {
            state = state.setValue(SlabBlock.TYPE, type);
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
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
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 12);
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
