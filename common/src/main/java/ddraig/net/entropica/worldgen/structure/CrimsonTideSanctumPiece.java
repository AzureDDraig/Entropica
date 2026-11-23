package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModStructures;
import ddraig.net.entropica.worldgen.feature.RedAlgaeMarginFeature;
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
import java.util.function.Supplier;

/**
 * Programmatic 3D generator for Biome #16's core-free sanctuary:
 * {@code Crimson Tide Sanctum} ({@code 20 x 20 x 18},
 * {@code dx = -10..9, dy = -3..14, dz = -10..9} — strictly ZERO Shrine Cores or Anchors):
 * <ul>
 *   <li><b>1. Sea-Level Anchoring &amp; Seabed Pilings ({@code baseY = Y=62, dy <= -3})</b>:
 *       {@link Blocks#DEEPSLATE_TILES} structural pilings and {@code entropica:red_clay_sand} +
 *       {@code entropica:sanguine_clay_bricks} foundation beds seal cleanly down into the coastal seabed.</li>
 *   <li><b>2. Sunken Central Crimson Tidal Basin ({@code 8 x 8, dx = -4..3, dz = -4..3}) &amp;
 *       4 Cardinal Tide-Archways</b>:
 *       Bay water flows through all 4 cardinal tide-archways directly into the central tidal pool
 *       lined with {@code Red Clay Sand}, translucent {@code Sanguine Ice} walkways ({@code dy = 1}),
 *       floating {@code entropica:sanguine_lily} pads, and submerged {@code entropica:red_algae}.</li>
 *   <li><b>3. Coastal Corner Buttresses, East/West Elevated Dry Alcoves &amp; Arched Blood-Glass Windows</b>:
 *       Stepped {@code Sanguine Clay Bricks} (+ Stairs &amp; Slabs), {@link Blocks#DEEPSLATE_TILES},
 *       and {@link Blocks#POLISHED_BASALT} piers frame arched {@code entropica:blood_glass} storm windows
 *       and 2 elevated dry reliquary alcoves ({@code dy = 3..7}) housing 2 chests
 *       ({@code entropica:chests/crimson_tide_sanctum}).</li>
 *   <li><b>4. Vaulted Roof, Overhead Blood-Glass Oculus Dome &amp; 5 {@code crimson_tide_lantern} Beacons</b>:
 *       4 corner archway beacons ({@code (-5,6,-5), (4,6,-5), (-5,6,4), (4,6,4)}) + 1 central oculus beacon
 *       ({@code (0,13,0)}) beneath the crowning {@code entropica:blood_glass} dome, each scheduled with
 *       {@code level.scheduleTick(..., 20)} so its 16-block warming/cleansing Materia aura activates immediately.</li>
 * </ul>
 */
public class CrimsonTideSanctumPiece extends StructurePiece {

    private static final int[][] BASIN_LILY_COORDS = {
            {-2, -2}, {1, -2}, {-2, 1}, {1, 1}
    };

    private static final int[][] BASIN_SUBMERGED_ALGAE_COORDS = {
            {-4, -3}, {-3, -4}, {3, -3}, {2, -4},
            {-4, 2}, {-3, 3}, {3, 2}, {2, 3}
    };

    private static final int[][] CORNER_LANTERN_COORDS = {
            {-5, 6, -5},
            {4, 6, -5},
            {-5, 6, 4},
            {4, 6, 4}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public CrimsonTideSanctumPiece(int originX, int baseY, int originZ) {
        // Exact 20 (X: -10..+9) x 18 (Y: -3..+14) x 20 (Z: -10..+9) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 10,
                baseY - 3,
                originZ - 10,
                originX + 9,
                baseY + 14,
                originZ + 9
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public CrimsonTideSanctumPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 10);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 3);
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 10);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.CRIMSON_TIDE_SANCTUM_PIECE.isPresent()) {
            return ModStructures.CRIMSON_TIDE_SANCTUM_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "crimson_tide_sanctum_piece");
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

    public BlockPos getOculusLanternPos() {
        return toWorld(0, 13, 0);
    }

    public BlockPos getWestAlcoveChestPos() {
        return toWorld(-8, 4, 0);
    }

    public BlockPos getEastAlcoveChestPos() {
        return toWorld(7, 4, 0);
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
        buildFoundationBasinAndSanguineWalkways(level, chunkBox);
        buildCornerButtressesTideArchwaysAndAlcoves(level, chunkBox, random);
        buildVaultedRoofBloodGlassOculusAndLanterns(level, chunkBox);
    }

    private void buildFoundationBasinAndSanguineWalkways(WorldGenLevel level, BoundingBox chunkBox) {
        Block sanguineIceBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_ICE.isPresent() ? ModBlocks.SANGUINE_ICE.get() : null,
                "sanguine_ice", Blocks.PACKED_ICE);
        Block redClaySandBlock = resolveBlock(
                () -> ModBlocks.RED_CLAY_SAND.isPresent() ? ModBlocks.RED_CLAY_SAND.get() : null,
                "red_clay_sand", Blocks.RED_SAND);
        Block clayBricksBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICKS.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICKS.get() : null,
                "sanguine_clay_bricks", Blocks.MUD_BRICKS);
        Block clayBrickSlabBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICK_SLAB.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICK_SLAB.get() : null,
                "sanguine_clay_brick_slab", Blocks.MUD_BRICK_SLAB);

        Optional<Block> sanguineLilyOpt = resolveOptionalBlock(
                () -> ModBlocks.SANGUINE_LILY.isPresent() ? ModBlocks.SANGUINE_LILY.get() : null,
                "sanguine_lily");
        Optional<Block> redAlgaeOpt = resolveOptionalBlock(
                () -> ModBlocks.RED_ALGAE.isPresent() ? ModBlocks.RED_ALGAE.get() : null,
                "red_algae");

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState deepslateTiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState sanguineIce = sanguineIceBlock.defaultBlockState();
        BlockState redClaySand = redClaySandBlock.defaultBlockState();
        BlockState clayBricks = clayBricksBlock.defaultBlockState();
        BlockState clayBottomSlab = slabState(clayBrickSlabBlock, SlabType.BOTTOM);

        // 1. Clear above-water interior & outer tide-channel headroom (dy = 1..14)
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                boolean inSanctumCore = (dx >= -9 && dx <= 8 && dz >= -9 && dz <= 8);
                boolean inOuterTideMouth = ((dx >= -2 && dx <= 1) && (dz == -10 || dz == 9))
                        || ((dz >= -1 && dz <= 0) && (dx == -10 || dx == 9));
                if (!inSanctumCore && !inOuterTideMouth) {
                    continue;
                }
                for (int dy = 1; dy <= 14; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // 2. Sub-foundation (dy = -3), Seabed Pilings (dy <= -4), Tidal Basin & 4 Cardinal Tide Sluices (dy = -2..0)
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                boolean inSanctumCore = (dx >= -9 && dx <= 8 && dz >= -9 && dz <= 8);
                boolean inNorthSouthChannel = (dx >= -2 && dx <= 1) && (dz <= -5 || dz >= 4);
                boolean inWestEastChannel = (dz >= -1 && dz <= 0) && (dx <= -5 || dx >= 4);
                boolean inCentralBasin = (dx >= -4 && dx <= 3 && dz >= -4 && dz <= 3);

                if (!inSanctumCore && !inNorthSouthChannel && !inWestEastChannel) {
                    continue;
                }

                boolean isPerimeterPiling = (dx <= -9 || dx >= 8 || dz <= -9 || dz >= 8)
                        || ((dx <= -7 || dx >= 6) && (dz <= -7 || dz >= 6));

                // Solid sub-foundation at dy = -3 and downward seabed pilings so the sanctum NEVER floats
                BlockState subBase = isPerimeterPiling ? deepslateTiles : clayBricks;
                setRel(level, chunkBox, dx, -3, dz, subBase);
                fillPilingsAndSeabedDown(level, chunkBox, dx, -4, dz, isPerimeterPiling, deepslateTiles, redClaySand, clayBricks);

                if (inCentralBasin || inNorthSouthChannel || inWestEastChannel) {
                    // Sunken central Crimson Tidal Basin (8x8) & 4 cardinal tide channels:
                    // dy = -2: Red Clay Sand floor accented with central Sanguine Ice veins
                    boolean centerIceVein = inCentralBasin
                            && (((dx == -1 || dx == 0) && (dz >= -3 && dz <= 2))
                            || ((dz == -1 || dz == 0) && (dx >= -3 && dx <= 2)));
                    setRel(level, chunkBox, dx, -2, dz, centerIceVein ? sanguineIce : redClaySand);
                    // dy = -1..0: Unfrozen sea-level water flowing from all 4 sides into the 8x8 basin
                    setRel(level, chunkBox, dx, -1, dz, water);
                    setRel(level, chunkBox, dx, 0, dz, water);
                } else {
                    // Solid retaining masonry around the basin & under walkways/alcoves/buttresses
                    setRel(level, chunkBox, dx, -2, dz, deepslateTiles);
                    setRel(level, chunkBox, dx, -1, dz, ((dx + dz) & 1) == 0 ? clayBricks : deepslateTiles);
                    setRel(level, chunkBox, dx, 0, dz, clayBricks);
                }
            }
        }

        // 3. Translucent Sanguine Ice Inlaid Walkways (dy = 1) wrapping the 8x8 Crimson Tidal Basin (dx = -6..5, dz = -6..5)
        for (int dx = -6; dx <= 5; dx++) {
            for (int dz = -6; dz <= 5; dz++) {
                boolean inBasin = (dx >= -4 && dx <= 3 && dz >= -4 && dz <= 3);
                if (inBasin) {
                    continue;
                }
                // Keep the 2-wide central North & South tide-archway boat/swimmer channels open at dy = 1
                boolean openNorthSouthMouth = (dx == -1 || dx == 0) && (dz <= -5 || dz >= 4);
                if (openNorthSouthMouth) {
                    continue;
                }

                boolean isCornerWalkway = (dx <= -5 || dx >= 4) && (dz <= -5 || dz >= 4);
                if (isCornerWalkway) {
                    setRel(level, chunkBox, dx, 1, dz, clayBricks);
                } else {
                    // Translucent Sanguine Ice inlaid walkway (including arched sea-level span over E/W 2-wide sluices!)
                    setRel(level, chunkBox, dx, 1, dz, sanguineIce);
                }
            }
        }

        // Outer North & South Tide-Quay Aprons (dx = -2 and dx = 1 along dz = -9..-7 and dz = 6..8)
        for (int dz : new int[]{-9, -8, -7, 6, 7, 8}) {
            setRel(level, chunkBox, -2, 1, dz, dz == -9 || dz == 8 ? clayBottomSlab : sanguineIce);
            setRel(level, chunkBox, 1, 1, dz, dz == -9 || dz == 8 ? clayBottomSlab : sanguineIce);
        }

        // 4. Thriving Basin Flora: submerged Red Algae (dy = -1) & floating Sanguine Lilies (dy = 1)
        if (redAlgaeOpt.isPresent()) {
            BlockState wetAlgae = RedAlgaeMarginFeature.prepareRedAlgaeState(redAlgaeOpt.get(), true);
            for (int[] pos : BASIN_SUBMERGED_ALGAE_COORDS) {
                BlockPos algaeWorld = toWorld(pos[0], -1, pos[1]);
                if (chunkBox.isInside(algaeWorld) && wetAlgae.canSurvive(level, algaeWorld)) {
                    level.setBlock(algaeWorld, wetAlgae, 2);
                }
            }
        }

        if (sanguineLilyOpt.isPresent()) {
            BlockState lilyState = nonWaterlogged(sanguineLilyOpt.get().defaultBlockState());
            for (int[] pos : BASIN_LILY_COORDS) {
                BlockPos lilyWorld = toWorld(pos[0], 1, pos[1]);
                if (chunkBox.isInside(lilyWorld)
                        && level.getBlockState(lilyWorld).isAir()
                        && lilyState.canSurvive(level, lilyWorld)) {
                    level.setBlock(lilyWorld, lilyState, 2);
                }
            }
        }
    }

    private void buildCornerButtressesTideArchwaysAndAlcoves(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block sanguineIceBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_ICE.isPresent() ? ModBlocks.SANGUINE_ICE.get() : null,
                "sanguine_ice", Blocks.PACKED_ICE);
        Block clayBricksBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICKS.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICKS.get() : null,
                "sanguine_clay_bricks", Blocks.MUD_BRICKS);
        Block clayStairsBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICK_STAIRS.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICK_STAIRS.get() : null,
                "sanguine_clay_brick_stairs", Blocks.MUD_BRICK_STAIRS);
        Block claySlabBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICK_SLAB.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICK_SLAB.get() : null,
                "sanguine_clay_brick_slab", Blocks.MUD_BRICK_SLAB);
        Block bloodGlassBlock = resolveBlock(() -> null, "blood_glass", Blocks.RED_STAINED_GLASS);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState deepslateTiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState sanguineIce = sanguineIceBlock.defaultBlockState();
        BlockState clayBricks = clayBricksBlock.defaultBlockState();
        BlockState clayBottomSlab = slabState(claySlabBlock, SlabType.BOTTOM);
        BlockState clayTopSlab = slabState(claySlabBlock, SlabType.TOP);
        BlockState bloodGlass = nonWaterlogged(bloodGlassBlock.defaultBlockState());

        // 1. Four Coastal Corner Buttresses (NW: -9..-7,-9..-7; NE: 6..8,-9..-7; SW: -9..-7,6..8; SE: 6..8,6..8)
        int[][] cornerQuadrants = {
                {-1, -1}, {1, -1}, {-1, 1}, {1, 1}
        };
        for (int[] quad : cornerQuadrants) {
            int sx = quad[0];
            int sz = quad[1];
            int innerPierX = (sx < 0) ? -7 : 6;
            int innerPierZ = (sz < 0) ? -7 : 6;
            int midButtX = (sx < 0) ? -8 : 7;
            int midButtZ = (sz < 0) ? -8 : 7;
            int outerButtX = (sx < 0) ? -9 : 8;
            int outerButtZ = (sz < 0) ? -9 : 8;
            Direction outXDir = (sx < 0) ? Direction.WEST : Direction.EAST;
            Direction outZDir = (sz < 0) ? Direction.NORTH : Direction.SOUTH;

            // Main Polished Basalt structural corner column (dy = 1..9)
            for (int dy = 1; dy <= 9; dy++) {
                setRel(level, chunkBox, innerPierX, dy, innerPierZ, polishedBasaltY);
            }

            // Flanking Sanguine Clay Bricks & Deepslate Tiles buttress piers (dy = 1..8)
            for (int dy = 1; dy <= 8; dy++) {
                BlockState pierMat = (dy <= 2 || dy == 8) ? deepslateTiles : clayBricks;
                setRel(level, chunkBox, midButtX, dy, innerPierZ, pierMat);
                setRel(level, chunkBox, innerPierX, dy, midButtZ, pierMat);
                if (dy <= 6) {
                    setRel(level, chunkBox, midButtX, dy, midButtZ, (dy <= 2) ? deepslateTiles : polishedBasaltY);
                }
            }
            setRel(level, chunkBox, midButtX, 7, midButtZ, stairState(clayStairsBlock, outXDir.getOpposite(), Half.BOTTOM));

            // Stepped outer coastal buttress spurs (dy = 1..4)
            for (int dy = 1; dy <= 3; dy++) {
                setRel(level, chunkBox, outerButtX, dy, innerPierZ, (dy == 1) ? deepslateTiles : clayBricks);
                setRel(level, chunkBox, innerPierX, dy, outerButtZ, (dy == 1) ? deepslateTiles : clayBricks);
            }
            setRel(level, chunkBox, outerButtX, 4, innerPierZ, stairState(clayStairsBlock, outXDir.getOpposite(), Half.BOTTOM));
            setRel(level, chunkBox, innerPierX, 4, outerButtZ, stairState(clayStairsBlock, outZDir.getOpposite(), Half.BOTTOM));
            setRel(level, chunkBox, outerButtX, 1, midButtZ, deepslateTiles);
            setRel(level, chunkBox, midButtX, 1, outerButtZ, deepslateTiles);
            setRel(level, chunkBox, outerButtX, 2, midButtZ, clayBottomSlab);
            setRel(level, chunkBox, midButtX, 2, outerButtZ, clayBottomSlab);

            // Diagonal interior arch rib stepping inward to the 4 corner lantern keystones at (-5,-5), (4,-5), (-5,4), (4,4)
            int ribMidX = (sx < 0) ? -6 : 5;
            int ribMidZ = (sz < 0) ? -6 : 5;
            int keyX = (sx < 0) ? -5 : 4;
            int keyZ = (sz < 0) ? -5 : 4;
            setRel(level, chunkBox, ribMidX, 7, ribMidZ, deepslateTiles);
            setRel(level, chunkBox, ribMidX, 8, ribMidZ, clayBricks);
            setRel(level, chunkBox, keyX, 7, keyZ, polishedBasaltY);
            setRel(level, chunkBox, keyX, 8, keyZ, clayBricks);
        }

        // 2. North & South Grand Tide-Archways & Arched Blood-Glass Storm Windows (dz = -8 and dz = 7)
        for (int sz : new int[]{-1, 1}) {
            int wallZ = (sz < 0) ? -8 : 7;
            int outerApronZ = (sz < 0) ? -9 : 8;
            Direction outward = (sz < 0) ? Direction.NORTH : Direction.SOUTH;

            // Tide-Archway piers at dx = -3 and dx = 2 (dy = 1..8)
            for (int dy = 1; dy <= 8; dy++) {
                BlockState pierMat = (dy == 1 || dy == 8) ? deepslateTiles : polishedBasaltY;
                setRel(level, chunkBox, -3, dy, wallZ, pierMat);
                setRel(level, chunkBox, 2, dy, wallZ, pierMat);
            }

            // Grand Tide-Archway vault overhead (dx = -2..1, dy = 5..8) — leaves dy = -1..4 open above sea-level water!
            setRel(level, chunkBox, -2, 5, wallZ, stairState(clayStairsBlock, Direction.WEST, Half.TOP));
            setRel(level, chunkBox, 1, 5, wallZ, stairState(clayStairsBlock, Direction.EAST, Half.TOP));
            for (int dx = -2; dx <= 1; dx++) {
                setRel(level, chunkBox, dx, 6, wallZ, (dx == -1 || dx == 0) ? deepslateTiles : clayBricks);
                setRel(level, chunkBox, dx, 7, wallZ, (dx == -1 || dx == 0) ? bloodGlass : clayBricks);
                setRel(level, chunkBox, dx, 8, wallZ, deepslateTiles);
            }
            // Stepped outer tide-arch hood at outerApronZ
            setRel(level, chunkBox, -2, 6, outerApronZ, stairState(clayStairsBlock, outward.getOpposite(), Half.BOTTOM));
            setRel(level, chunkBox, -1, 6, outerApronZ, clayBottomSlab);
            setRel(level, chunkBox, 0, 6, outerApronZ, clayBottomSlab);
            setRel(level, chunkBox, 1, 6, outerApronZ, stairState(clayStairsBlock, outward.getOpposite(), Half.BOTTOM));

            // Flanking North/South spandrel walls (dx = -6..-4 and dx = 3..5) with arched Blood-Glass storm windows!
            for (int dx : new int[]{-6, -5, -4, 3, 4, 5}) {
                boolean windowCol = (dx == -5 || dx == -4 || dx == 3 || dx == 4);
                for (int dy = 2; dy <= 8; dy++) {
                    if (windowCol && dy >= 4 && dy <= 6) {
                        setRel(level, chunkBox, dx, dy, wallZ, bloodGlass);
                    } else {
                        setRel(level, chunkBox, dx, dy, wallZ, (dy == 2 || dy == 8) ? deepslateTiles : clayBricks);
                    }
                }
            }
        }

        // 3. West & East Tide-Archways (dy = -1..2) + Elevated Dry Reliquary Alcoves (dy = 3..8) + 2 Chests!
        for (int sx : new int[]{-1, 1}) {
            int outerWallX = (sx < 0) ? -9 : 8;
            int alcoveMidX = (sx < 0) ? -8 : 7;
            int alcoveFrontX = (sx < 0) ? -7 : 6;
            int stairStepX = (sx < 0) ? -6 : 5;
            Direction inward = (sx < 0) ? Direction.EAST : Direction.WEST;
            Direction outward = inward.getOpposite();

            // Lower sea-level Tide-Sluice Archway (dz = -1..0, dy = 1..2) letting bay water flow under the dry alcove!
            for (int x : new int[]{outerWallX, alcoveMidX, alcoveFrontX}) {
                setRel(level, chunkBox, x, 1, -1, air);
                setRel(level, chunkBox, x, 1, 0, air);
                setRel(level, chunkBox, x, 2, -1, stairState(clayStairsBlock, Direction.NORTH, Half.TOP));
                setRel(level, chunkBox, x, 2, 0, stairState(clayStairsBlock, Direction.SOUTH, Half.TOP));
                // Solid side piers flanking the lower tide-sluice (dz = -3..-2 and dz = 1..2, dy = 1..2)
                for (int dz : new int[]{-3, -2, 1, 2}) {
                    setRel(level, chunkBox, x, 1, dz, deepslateTiles);
                    setRel(level, chunkBox, x, 2, dz, clayBricks);
                }
            }

            // Elevated Dry Alcove Floor at dy = 3 (Y = 65, 100% dry above the tide-sluice vault!)
            for (int x : new int[]{alcoveMidX, alcoveFrontX}) {
                for (int dz = -2; dz <= 1; dz++) {
                    boolean iceInlay = (x == alcoveFrontX && (dz == -1 || dz == 0));
                    setRel(level, chunkBox, x, 3, dz, iceInlay ? sanguineIce : clayBricks);
                    // Clear dry alcove interior headroom (dy = 4..7)
                    for (int dy = 4; dy <= 7; dy++) {
                        setRel(level, chunkBox, x, dy, dz, air);
                    }
                }
            }

            // Twin access stairs stepping cleanly from the dy = 1 Sanguine Ice walkway up to the dy = 3 dry alcove!
            setRel(level, chunkBox, stairStepX, 2, -2, stairState(clayStairsBlock, outward, Half.BOTTOM));
            setRel(level, chunkBox, stairStepX, 2, 1, stairState(clayStairsBlock, outward, Half.BOTTOM));
            // Decorative protective balustrade slab at center front of the alcove overlooking the basin
            setRel(level, chunkBox, alcoveFrontX, 4, -1, clayBottomSlab);
            setRel(level, chunkBox, alcoveFrontX, 4, 0, clayBottomSlab);

            // Alcove side framing piers (dz = -3 and dz = 2, dy = 3..8) & flanking walls (dz = -6..-4 and dz = 3..5)
            for (int dy = 3; dy <= 8; dy++) {
                BlockState pierMat = (dy == 3 || dy == 8) ? deepslateTiles : polishedBasaltY;
                setRel(level, chunkBox, outerWallX, dy, -3, pierMat);
                setRel(level, chunkBox, outerWallX, dy, 2, pierMat);
                setRel(level, chunkBox, alcoveFrontX, dy, -3, pierMat);
                setRel(level, chunkBox, alcoveFrontX, dy, 2, pierMat);
                if (dy == 8) {
                    setRel(level, chunkBox, alcoveMidX, dy, -3, deepslateTiles);
                    setRel(level, chunkBox, alcoveMidX, dy, 2, deepslateTiles);
                }
            }
            // Arched alcove ceiling hood at dy = 7..8
            setRel(level, chunkBox, alcoveFrontX, 7, -2, stairState(clayStairsBlock, Direction.NORTH, Half.TOP));
            setRel(level, chunkBox, alcoveFrontX, 7, 1, stairState(clayStairsBlock, Direction.SOUTH, Half.TOP));
            for (int dz = -2; dz <= 1; dz++) {
                setRel(level, chunkBox, alcoveFrontX, 8, dz, deepslateTiles);
                setRel(level, chunkBox, alcoveMidX, 8, dz, clayBricks);
            }

            // Outer Alcove Back Wall (outerWallX, dz = -2..1, dy = 3..8) with Arched entropica:blood_glass Storm Window!
            for (int dz = -2; dz <= 1; dz++) {
                setRel(level, chunkBox, outerWallX, 3, dz, deepslateTiles);
                setRel(level, chunkBox, outerWallX, 4, dz, clayBricks);
                boolean centerLancet = (dz == -1 || dz == 0);
                setRel(level, chunkBox, outerWallX, 5, dz, bloodGlass);
                setRel(level, chunkBox, outerWallX, 6, dz, bloodGlass);
                setRel(level, chunkBox, outerWallX, 7, dz, centerLancet ? bloodGlass : clayBricks);
                setRel(level, chunkBox, outerWallX, 8, dz, deepslateTiles);
            }

            // Flanking East/West gallery walls (dz = -6..-4 and dz = 3..5) at x = alcoveMidX with Blood-Glass slits
            for (int dz : new int[]{-6, -5, -4, 3, 4, 5}) {
                boolean slitCol = (dz == -5 || dz == 4);
                for (int dy = 2; dy <= 8; dy++) {
                    if (slitCol && dy >= 4 && dy <= 6) {
                        setRel(level, chunkBox, alcoveMidX, dy, dz, bloodGlass);
                    } else {
                        setRel(level, chunkBox, alcoveMidX, dy, dz, (dy == 2 || dy == 8) ? deepslateTiles : clayBricks);
                    }
                }
                setRel(level, chunkBox, outerWallX, 2, dz, clayTopSlab);
            }

            // Place Reliquary Chest in the Elevated Dry Alcove:
            // West chest at (-8, 4, 0) facing EAST; East chest at (7, 4, 0) facing WEST
            setRel(level, chunkBox, alcoveMidX, 4, -1, polishedBasaltY);
            placeSanctumChest(level, chunkBox, random, alcoveMidX, 4, 0, inward);
        }
    }

    private void buildVaultedRoofBloodGlassOculusAndLanterns(WorldGenLevel level, BoundingBox chunkBox) {
        Block clayBricksBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICKS.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICKS.get() : null,
                "sanguine_clay_bricks", Blocks.MUD_BRICKS);
        Block clayStairsBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICK_STAIRS.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICK_STAIRS.get() : null,
                "sanguine_clay_brick_stairs", Blocks.MUD_BRICK_STAIRS);
        Block claySlabBlock = resolveBlock(
                () -> ModBlocks.SANGUINE_CLAY_BRICK_SLAB.isPresent() ? ModBlocks.SANGUINE_CLAY_BRICK_SLAB.get() : null,
                "sanguine_clay_brick_slab", Blocks.MUD_BRICK_SLAB);
        Block bloodGlassBlock = resolveBlock(() -> null, "blood_glass", Blocks.RED_STAINED_GLASS);
        Block crimsonLanternBlock = resolveBlock(
                () -> ModBlocks.CRIMSON_TIDE_LANTERN.isPresent() ? ModBlocks.CRIMSON_TIDE_LANTERN.get() : null,
                "crimson_tide_lantern", Blocks.LANTERN);

        BlockState deepslateTiles = Blocks.DEEPSLATE_TILES.defaultBlockState();
        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState clayBricks = clayBricksBlock.defaultBlockState();
        BlockState clayBottomSlab = slabState(claySlabBlock, SlabType.BOTTOM);
        BlockState bloodGlass = nonWaterlogged(bloodGlassBlock.defaultBlockState());

        // 1. Lower Vaulted Roof Cornice (dy = 9, dx = -8..7, dz = -8..7, hollow inside dx = -6..5, dz = -6..5)
        for (int dx = -8; dx <= 7; dx++) {
            for (int dz = -8; dz <= 7; dz++) {
                boolean outerRing = (dx == -8 || dx == 7 || dz == -8 || dz == 7);
                boolean midRing = (dx == -7 || dx == 6 || dz == -7 || dz == 6);
                if (outerRing) {
                    setRel(level, chunkBox, dx, 9, dz, clayBottomSlab);
                } else if (midRing) {
                    boolean rib = (dx == -1 || dx == 0 || dz == -1 || dz == 0);
                    setRel(level, chunkBox, dx, 9, dz, rib ? deepslateTiles : clayBricks);
                }
            }
        }

        // 2. Stepped Mid-Vault Roof (dy = 10, dx = -7..6, dz = -7..6, hollow inside dx = -5..4, dz = -5..4)
        for (int dx = -7; dx <= 6; dx++) {
            for (int dz = -7; dz <= 6; dz++) {
                boolean outerStep = (dx == -7 || dx == 6 || dz == -7 || dz == 6);
                boolean innerVault = (dx == -6 || dx == 5 || dz == -6 || dz == 5);
                if (outerStep) {
                    Direction stairDir = roofStepFacing(dx, dz, -7, 6);
                    setRel(level, chunkBox, dx, 10, dz,
                            stairDir != null ? stairState(clayStairsBlock, stairDir, Half.BOTTOM) : clayBottomSlab);
                } else if (innerVault) {
                    boolean rib = (dx == -1 || dx == 0 || dz == -1 || dz == 0);
                    setRel(level, chunkBox, dx, 10, dz, rib ? deepslateTiles : clayBricks);
                }
            }
        }

        // 3. Upper Vault & Oculus Drum Collar (dy = 11, dx = -5..4, dz = -5..4, open 6x6 oculus dx = -3..2, dz = -3..2)
        for (int dx = -5; dx <= 4; dx++) {
            for (int dz = -5; dz <= 4; dz++) {
                boolean inOculusOpening = (dx >= -3 && dx <= 2 && dz >= -3 && dz <= 2);
                if (inOculusOpening) {
                    continue;
                }
                boolean outerCollar = (dx == -5 || dx == 4 || dz == -5 || dz == 4);
                if (outerCollar) {
                    Direction stairDir = roofStepFacing(dx, dz, -5, 4);
                    setRel(level, chunkBox, dx, 11, dz,
                            stairDir != null ? stairState(clayStairsBlock, stairDir, Half.BOTTOM) : clayBottomSlab);
                } else {
                    setRel(level, chunkBox, dx, 11, dz, deepslateTiles);
                }
            }
        }

        // 4. Overhead Blood-Glass Oculus Dome (dy = 12..14)
        // dy = 12: 6x6 entropica:blood_glass lower dome shell (dx = -3..2, dz = -3..2), framed at -4..3 by slabs
        for (int dx = -4; dx <= 3; dx++) {
            for (int dz = -4; dz <= 3; dz++) {
                if (dx == -4 || dx == 3 || dz == -4 || dz == 3) {
                    setRel(level, chunkBox, dx, 12, dz, clayBottomSlab);
                } else if (dx == -3 || dx == 2 || dz == -3 || dz == 2) {
                    setRel(level, chunkBox, dx, 12, dz, bloodGlass);
                }
            }
        }
        // dy = 13: 4x4 entropica:blood_glass upper dome shell (dx = -2..1, dz = -2..1 perimeter)
        for (int dx = -2; dx <= 1; dx++) {
            for (int dz = -2; dz <= 1; dz++) {
                if (dx == -2 || dx == 1 || dz == -2 || dz == 1) {
                    setRel(level, chunkBox, dx, 13, dz, bloodGlass);
                }
            }
        }
        // dy = 14: 2x2 Blood-Glass oculus cap with solid Polished Basalt keystone at (0, 14, 0) supporting the central lantern
        setRel(level, chunkBox, -1, 14, -1, bloodGlass);
        setRel(level, chunkBox, 0, 14, -1, bloodGlass);
        setRel(level, chunkBox, -1, 14, 0, bloodGlass);
        setRel(level, chunkBox, 0, 14, 0, polishedBasaltY);

        // 5. Place all 5 Hanging ModBlocks.CRIMSON_TIDE_LANTERN Beacons & Schedule Immediate Aura Ticks!
        for (int[] corner : CORNER_LANTERN_COORDS) {
            placeAndScheduleCrimsonLantern(level, chunkBox, crimsonLanternBlock, corner[0], corner[1], corner[2]);
        }
        placeAndScheduleCrimsonLantern(level, chunkBox, crimsonLanternBlock, 0, 13, 0);
    }

    private void placeAndScheduleCrimsonLantern(
            WorldGenLevel level,
            BoundingBox chunkBox,
            Block lanternBlock,
            int dx,
            int dy,
            int dz
    ) {
        BlockPos lanternPos = toWorld(dx, dy, dz);
        if (!chunkBox.isInside(lanternPos)) {
            return;
        }
        BlockState state = nonWaterlogged(lanternBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.HANGING)) {
            state = state.setValue(BlockStateProperties.HANGING, true);
        }
        level.setBlock(lanternPos, state, 2);
        if (ModBlocks.CRIMSON_TIDE_LANTERN.isPresent()) {
            level.scheduleTick(lanternPos, ModBlocks.CRIMSON_TIDE_LANTERN.get(), 20);
        } else {
            level.scheduleTick(lanternPos, lanternBlock, 20);
        }
    }

    private void placeSanctumChest(
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
                level, random, pos, ModStructures.CRIMSON_TIDE_SANCTUM_CHEST_LOOT);
    }

    private void fillPilingsAndSeabedDown(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int startDy,
            int dz,
            boolean isPerimeterPiling,
            BlockState deepslateTiles,
            BlockState redClaySand,
            BlockState clayBricks
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
                    || existing.is(Blocks.ICE)
                    || existing.is(Blocks.SNOW)
                    || existing.is(Blocks.POWDER_SNOW)
                    || existing.canBeReplaced()) {
                if (isPerimeterPiling || wy == minAllowedY) {
                    level.setBlock(cursor, deepslateTiles, 2);
                } else {
                    // Seal interior seabed embankment in Red Clay Sand backed at bottom by Sanguine Clay Bricks
                    BlockPos belowPos = cursor.below();
                    BlockState below = level.getBlockState(belowPos);
                    boolean solidBelow = !below.isAir()
                            && !below.is(Blocks.WATER)
                            && !below.canBeReplaced();
                    level.setBlock(cursor, solidBelow ? redClaySand : clayBricks, 2);
                }
            } else {
                break;
            }
        }
    }

    private static Direction roofStepFacing(int dx, int dz, int minBound, int maxBound) {
        boolean west = (dx == minBound);
        boolean east = (dx == maxBound);
        boolean north = (dz == minBound);
        boolean south = (dz == maxBound);
        if ((west || east) && (north || south)) {
            return null;
        }
        if (north) return Direction.SOUTH;
        if (south) return Direction.NORTH;
        if (west) return Direction.EAST;
        if (east) return Direction.WEST;
        return null;
    }

    private static Block resolveBlock(Supplier<Block> modSupplier, String path, Block fallback) {
        try {
            Block fromMod = modSupplier.get();
            if (fromMod != null) {
                return fromMod;
            }
        } catch (Throwable ignored) {
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private static Optional<Block> resolveOptionalBlock(Supplier<Block> modSupplier, String path) {
        try {
            Block fromMod = modSupplier.get();
            if (fromMod != null) {
                return Optional.of(fromMod);
            }
        } catch (Throwable ignored) {
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id);
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
}
