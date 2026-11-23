package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModStructures;
import ddraig.net.entropica.worldgen.feature.AmberWeepingWillowFeature;
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
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.Optional;

/**
 * Programmatic 3D generator for Biome #14's core-free sanctuary:
 * {@code Amber-Willow Thermal Bathhouse} ({@code 22 x 22 x 18},
 * {@code dx = -11..10, dy = 0..17, dz = -11..10} — strictly ZERO Shrine Cores or Anchors):
 * <ul>
 *   <li><b>1. Outer Snowy Terrace &amp; Boardwalk Decks ({@code dy = 0..3})</b>:
 *       Snowy outer terrace ({@link Blocks#SNOW_BLOCK}) stepping up via {@code silver_pine_stairs}
 *       onto warm {@code entropica:amber_planks} &amp; {@code entropica:amber_slab} boardwalk decks,
 *       with lush green {@link Blocks#GRASS_BLOCK} planters blooming with
 *       {@code entropica:amber_nectar_blossom} and {@code entropica:mist_veil_marshmallow}.</li>
 *   <li><b>2. Steaming Central Thermal Pool &amp; Arched Footbridge ({@code dy = 0..4})</b>:
 *       Watertight {@code entropica:vitae_spring_stone} basin filled with unfrozen {@link Blocks#WATER}
 *       and floating {@code entropica:auroral_lily_pad}s, crossed N-S by an {@code amber_planks},
 *       {@code amber_slab}, &amp; {@code amber_fence} footbridge.</li>
 *   <li><b>3. Four Amber-Log Corner Pillars, Tiered Silver-Pine &amp; Amber Pagoda Roof &amp; Skylight ({@code dy = 2..17})</b>:
 *       {@code 4} {@code entropica:amber_log} corner pillars (draped in {@code entropica:vapor_weaver_lichen})
 *       supporting a two-tier {@code entropica:silver_pine_planks} / {@code silver_pine_stairs} /
 *       {@code silver_pine_slab} &amp; {@code entropica:amber_planks} pagoda roof with a wide central
 *       {@code entropica:oasis_glass} skylight, {@code entropica:amber_resin_block} corner lanterns,
 *       and {@code entropica:amber_torch} posts.</li>
 *   <li><b>4. Poolside Weeping Amber-Willow, Dew Basins &amp; Treasure Chests</b>:
 *       Graceful poolside Amber-Willow ({@code amber_log} + {@code frost_willow_leaves} + supported
 *       {@code frost_willow_vines}), {@code 2} sculpted {@code entropica:oasis_dew_basin}s
 *       ({@code infused=true} on one!), and {@code 2} loot chests
 *       ({@code entropica:chests/amber_willow_bathhouse}).</li>
 * </ul>
 */
public class AmberWillowBathhousePiece extends StructurePiece {

    private static final int[][] PILLAR_COORDS = {
            {-6, -6}, {6, -6}, {-6, 6}, {6, 6}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public AmberWillowBathhousePiece(int originX, int baseY, int originZ) {
        // Exact 22 (X: -11..+10) x 18 (Y: 0..+17) x 22 (Z: -11..+10) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 11,
                baseY,
                originZ - 11,
                originX + 10,
                baseY + 17,
                originZ + 10
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public AmberWillowBathhousePiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 11);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY());
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 11);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.AMBER_WILLOW_BATHHOUSE_PIECE.isPresent()) {
            return ModStructures.AMBER_WILLOW_BATHHOUSE_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "amber_willow_bathhouse_piece");
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
        buildSnowyTerraceBoardwalkAndSpringPool(level, chunkBox);
        buildPlantersFootbridgeAndPoolsideFurnishings(level, chunkBox, random);
        buildCornerPillarsPagodaRoofAndSkylight(level, chunkBox);
        buildPoolsideWeepingAmberWillow(level, chunkBox);
    }

    private void buildSnowyTerraceBoardwalkAndSpringPool(WorldGenLevel level, BoundingBox chunkBox) {
        Block springStoneBlock = resolveVitaeSpringStone();
        Block amberPlanksBlock = resolveBlock("amber_planks", Blocks.OAK_PLANKS);
        Block amberSlabBlock = resolveBlock("amber_slab", Blocks.OAK_SLAB);
        Block amberStairsBlock = resolveBlock("amber_stairs", Blocks.OAK_STAIRS);
        Block amberFenceBlock = resolveBlock("amber_fence", Blocks.OAK_FENCE);
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block silverPineStairsBlock = resolveBlock("silver_pine_stairs", Blocks.SPRUCE_STAIRS);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        BlockState snowBlock = Blocks.SNOW_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState packedIce = Blocks.PACKED_ICE.defaultBlockState();
        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState amberPlanks = amberPlanksBlock.defaultBlockState();
        BlockState amberBottomSlab = slabState(amberSlabBlock, SlabType.BOTTOM);
        BlockState silverPinePlanks = silverPinePlanksBlock.defaultBlockState();

        // Clear sky headroom across the entire 22 x 22 x 18 footprint
        for (int dx = -11; dx <= 10; dx++) {
            for (int dz = -11; dz <= 10; dz++) {
                for (int dy = 1; dy <= 17; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }
            }
        }

        // 1. Outer Snowy Terrace Ring & Solid Foundation Skirt (dy = 0..1)
        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 10.5D) {
                    continue;
                }
                int maxAxis = Math.max(Math.abs(dx), Math.abs(dz));
                boolean insideBoardwalk = maxAxis <= 8 && r <= 9.6D;

                BlockState subBase = insideBoardwalk ? springStone : dirt;
                setRel(level, chunkBox, dx, 0, dz, subBase);
                fillFoundationDown(level, chunkBox, dx, -1, dz, insideBoardwalk ? springStone : packedIce);

                if (insideBoardwalk) {
                    setRel(level, chunkBox, dx, 1, dz, springStone);
                } else {
                    setRel(level, chunkBox, dx, 1, dz, snowBlock);
                }
            }
        }

        // 2. Warm Amber-Planks & Amber-Slab Boardwalk Deck (dy = 2 across max(|dx|,|dz|) <= 8)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 9.6D) {
                    continue;
                }
                int maxAxis = Math.max(Math.abs(dx), Math.abs(dz));
                boolean isOuterEdge = (maxAxis == 8);
                boolean isCardinalApproach = (Math.abs(dx) <= 1 && Math.abs(dz) == 8)
                        || (Math.abs(dz) <= 1 && Math.abs(dx) == 8);

                if (isOuterEdge && !isCardinalApproach) {
                    setRel(level, chunkBox, dx, 2, dz, amberBottomSlab);
                } else {
                    setRel(level, chunkBox, dx, 2, dz, amberPlanks);
                }
            }
        }

        // 3. 4 Cardinal Stepped Entrances (Silver-Pine & Amber Stairs ascending from outer snowy terrace onto dy = 2 deck)
        for (int s = -1; s <= 1; s++) {
            // North (-Z) entrance
            setRel(level, chunkBox, s, 1, -9, silverPinePlanks);
            setRel(level, chunkBox, s, 2, -9, stairState(silverPineStairsBlock, Direction.SOUTH, Half.BOTTOM));
            setRel(level, chunkBox, s, 1, -10, snowBlock);

            // South (+Z) entrance
            setRel(level, chunkBox, s, 1, 9, silverPinePlanks);
            setRel(level, chunkBox, s, 2, 9, stairState(silverPineStairsBlock, Direction.NORTH, Half.BOTTOM));
            setRel(level, chunkBox, s, 1, 10, snowBlock);

            // West (-X) entrance
            setRel(level, chunkBox, -9, 1, s, silverPinePlanks);
            setRel(level, chunkBox, -9, 2, s, stairState(silverPineStairsBlock, Direction.EAST, Half.BOTTOM));
            setRel(level, chunkBox, -10, 1, s, snowBlock);

            // East (+X) entrance
            setRel(level, chunkBox, 9, 1, s, silverPinePlanks);
            setRel(level, chunkBox, 9, 2, s, stairState(silverPineStairsBlock, Direction.WEST, Half.BOTTOM));
            setRel(level, chunkBox, 10, 1, s, snowBlock);
        }

        // 4. Steaming Central Vitae-Spring-Stone Pool (dx = -4..4, dz = -4..4, octagonal basin, ZERO Cores/Anchors!)
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (ax == 5 && az == 5) {
                    continue;
                }

                boolean insideWater = (ax <= 4 && az <= 4) && (ax + az <= 6)
                        && !(dx <= -3 && dz <= -3); // Reserve NW corner island for poolside Weeping Amber-Willow!

                if (insideWater) {
                    boolean deepPocket = (ax <= 2 && az <= 2);
                    setRel(level, chunkBox, dx, 0, dz, springStone);
                    if (deepPocket) {
                        setRel(level, chunkBox, dx, 1, dz, water);
                    } else {
                        setRel(level, chunkBox, dx, 1, dz, springStone);
                    }
                    setRel(level, chunkBox, dx, 2, dz, water);
                } else if (ax <= 5 && az <= 5 && (ax + az <= 8)) {
                    // Watertight vitae_spring_stone pool coping rim around the warm water
                    setRel(level, chunkBox, dx, 0, dz, springStone);
                    setRel(level, chunkBox, dx, 1, dz, springStone);
                    boolean innerStep = (dz == 0 && ax == 4) || (dx == 0 && az == 4);
                    if (innerStep) {
                        Direction stepFace = (dx == -4) ? Direction.WEST
                                : (dx == 4) ? Direction.EAST
                                : (dz == -4) ? Direction.NORTH : Direction.SOUTH;
                        setRel(level, chunkBox, dx, 2, dz, stairState(amberStairsBlock, stepFace, Half.BOTTOM));
                    } else {
                        setRel(level, chunkBox, dx, 2, dz, springStone);
                    }
                }
            }
        }

        // 5. Perimeter Boardwalk Fence Railings along non-entrance flanks (dy = 3)
        int[][] boardwalkFences = {
                {-4, -7}, {-3, -7}, {-2, -7}, {2, -7}, {3, -7}, {4, -7},
                {-4, 7}, {-3, 7}, {-2, 7}, {2, 7}, {3, 7}, {4, 7},
                {-7, -4}, {-7, -3}, {-7, -2}, {-7, 2}, {-7, 3}, {-7, 4},
                {7, -4}, {7, -3}, {7, -2}, {7, 2}, {7, 3}, {7, 4}
        };
        for (int[] f : boardwalkFences) {
            setRel(level, chunkBox, f[0], 2, f[1], amberPlanks);
            placeConnectedFence(level, chunkBox, f[0], 3, f[1], amberFenceBlock);
        }
        for (int[] f : boardwalkFences) {
            placeConnectedFence(level, chunkBox, f[0], 3, f[1], amberFenceBlock);
        }
    }

    private void buildPlantersFootbridgeAndPoolsideFurnishings(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block amberLogBlock = resolveBlock("amber_log", Blocks.OAK_LOG);
        Block amberPlanksBlock = resolveBlock("amber_planks", Blocks.OAK_PLANKS);
        Block amberSlabBlock = resolveBlock("amber_slab", Blocks.OAK_SLAB);
        Block amberFenceBlock = resolveBlock("amber_fence", Blocks.OAK_FENCE);
        Block lilyPadBlock = resolveBlock("auroral_lily_pad", Blocks.LILY_PAD);
        Block nectarBlossomBlock = resolveBlock("amber_nectar_blossom", Blocks.DANDELION);
        Block marshmallowBlock = resolveBlock("mist_veil_marshmallow", Blocks.FERN);
        Block amberTorchBlock = resolveBlock("amber_torch", Blocks.TORCH);
        Block dewBasinBlock = resolveBlock("oasis_dew_basin", Blocks.CAULDRON);

        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState grassBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState fern = Blocks.FERN.defaultBlockState();
        BlockState amberLogY = logWithAxis(amberLogBlock, Direction.Axis.Y);
        BlockState amberPlanks = amberPlanksBlock.defaultBlockState();
        BlockState amberBottomSlab = slabState(amberSlabBlock, SlabType.BOTTOM);
        BlockState lilyPad = lilyPadBlock.defaultBlockState();
        BlockState nectarBlossom = nectarBlossomBlock.defaultBlockState();
        BlockState marshmallow = marshmallowBlock.defaultBlockState();
        BlockState standingTorch = standingTorchState(amberTorchBlock);

        // 1. Lush Green Blocks.GRASS_BLOCK Planters in all 4 Quadrants!
        //    - NW (-5..-3, -5..-3): Weeping Amber-Willow sanctuary island planter
        //    - NE (4..5, -5..-4), SW (-5..-4, 4..5), SE (4..5, 4..5): Amber Nectar Blossom & Marshmallow planters
        int[][][] quadrantPlanters = {
                // NE
                {{4, -5}, {5, -5}, {4, -4}, {5, -4}},
                // SW
                {{-5, 4}, {-4, 4}, {-5, 5}, {-4, 5}},
                // SE
                {{4, 4}, {5, 4}, {4, 5}, {5, 5}}
        };
        for (int q = 0; q < quadrantPlanters.length; q++) {
            int[][] cells = quadrantPlanters[q];
            for (int[] c : cells) {
                setRel(level, chunkBox, c[0], 1, c[1], dirt);
                setRel(level, chunkBox, c[0], 2, c[1], grassBlock);
            }
            setRel(level, chunkBox, cells[0][0], 3, cells[0][1], nectarBlossom);
            setRel(level, chunkBox, cells[1][0], 3, cells[1][1], marshmallow);
            setRel(level, chunkBox, cells[2][0], 3, cells[2][1], fern);
            setRel(level, chunkBox, cells[3][0], 3, cells[3][1], nectarBlossom);
        }

        // NW Weeping Willow Island Planter (-5..-3, -5..-3)
        for (int dx = -5; dx <= -3; dx++) {
            for (int dz = -5; dz <= -3; dz++) {
                if (dx == -3 && dz == -3) {
                    continue; // Keep (-3, 2, -3) pool rim / spring stone
                }
                setRel(level, chunkBox, dx, 1, dz, dirt);
                setRel(level, chunkBox, dx, 2, dz, grassBlock);
            }
        }
        setRel(level, chunkBox, -5, 3, -3, nectarBlossom);
        setRel(level, chunkBox, -3, 3, -5, nectarBlossom);

        // 2. Floating Auroral Lily-Pads on the Steaming Unfrozen Pool (dy = 3 above Blocks.WATER at dy = 2)
        int[][] lilyCoords = {
                {-2, -2}, {2, -2}, {-2, 2}, {2, 2}
        };
        for (int[] lp : lilyCoords) {
            setRel(level, chunkBox, lp[0], 3, lp[1], lilyPad);
        }

        // 3. Arched Amber-Planks, Amber-Slab & Amber-Fence Footbridge crossing N-S (dx = -1..1, dz = -5..5)
        //    Leaves dy = 2 water completely open underneath the bridge!
        setRel(level, chunkBox, 0, 3, -5, amberBottomSlab);
        setRel(level, chunkBox, 0, 3, 5, amberBottomSlab);
        setRel(level, chunkBox, 0, 3, -4, amberBottomSlab);
        setRel(level, chunkBox, 0, 3, 4, amberBottomSlab);

        for (int dz = -3; dz <= 3; dz++) {
            setRel(level, chunkBox, 0, 3, dz, amberPlanks);
        }

        // Side bridge stringers & connected amber_fence handrails along dx = ±1, dz = -4..4
        for (int sideX : new int[]{-1, 1}) {
            for (int dz = -4; dz <= 4; dz++) {
                setRel(level, chunkBox, sideX, 3, dz, amberPlanks);
            }
            // Bridge corner newel posts in amber_log topped with glowing amber_torch!
            setRel(level, chunkBox, sideX, 4, -4, amberLogY);
            setRel(level, chunkBox, sideX, 5, -4, standingTorch);
            setRel(level, chunkBox, sideX, 4, 4, amberLogY);
            setRel(level, chunkBox, sideX, 5, 4, standingTorch);
        }
        for (int sideX : new int[]{-1, 1}) {
            for (int dz = -3; dz <= 3; dz++) {
                placeConnectedFence(level, chunkBox, sideX, 4, dz, amberFenceBlock);
            }
        }
        for (int sideX : new int[]{-1, 1}) {
            for (int dz = -3; dz <= 3; dz++) {
                placeConnectedFence(level, chunkBox, sideX, 4, dz, amberFenceBlock);
            }
        }

        // 4. Four Cardinal Boardwalk Amber-Torch Posts (standing on amber_fence posts!)
        int[][] torchPosts = {
                {-2, -7}, {2, 7}, {-7, 2}, {7, -2}
        };
        for (int[] tp : torchPosts) {
            placeConnectedFence(level, chunkBox, tp[0], 3, tp[1], amberFenceBlock);
            setRel(level, chunkBox, tp[0], 4, tp[1], standingTorch);
        }

        // 5. Two Sculpted Oasis Dew Basins beside the Pool (infused = true on the West basin!)
        setRel(level, chunkBox, -5, 3, 0, oasisDewBasinState(dewBasinBlock, true));
        setRel(level, chunkBox, 5, 3, 0, oasisDewBasinState(dewBasinBlock, false));

        // 6. Two Bathhouse Sanctuary Loot Chests (entropica:chests/amber_willow_bathhouse)
        placeBathhouseChest(level, chunkBox, random, -6, 3, 2, Direction.EAST);
        placeBathhouseChest(level, chunkBox, random, 6, 3, -2, Direction.WEST);
    }

    private void buildCornerPillarsPagodaRoofAndSkylight(WorldGenLevel level, BoundingBox chunkBox) {
        Block amberLogBlock = resolveBlock("amber_log", Blocks.OAK_LOG);
        Block amberPlanksBlock = resolveBlock("amber_planks", Blocks.OAK_PLANKS);
        Block amberFenceBlock = resolveBlock("amber_fence", Blocks.OAK_FENCE);
        Block silverPineLogBlock = resolveBlock("silver_pine_log", Blocks.SPRUCE_LOG);
        Block silverPinePlanksBlock = resolveBlock("silver_pine_planks", Blocks.SPRUCE_PLANKS);
        Block silverPineSlabBlock = resolveBlock("silver_pine_slab", Blocks.SPRUCE_SLAB);
        Block silverPineStairsBlock = resolveBlock("silver_pine_stairs", Blocks.SPRUCE_STAIRS);
        Block oasisGlassBlock = resolveBlock("oasis_glass", Blocks.CYAN_STAINED_GLASS);
        Block amberResinBlock = resolveBlock("amber_resin_block", Blocks.HONEYCOMB_BLOCK);
        Block vaporLichenBlock = resolveBlock("vapor_weaver_lichen", Blocks.GLOW_LICHEN);

        BlockState amberLogY = logWithAxis(amberLogBlock, Direction.Axis.Y);
        BlockState amberLogX = logWithAxis(amberLogBlock, Direction.Axis.X);
        BlockState amberLogZ = logWithAxis(amberLogBlock, Direction.Axis.Z);
        BlockState silverPineLogX = logWithAxis(silverPineLogBlock, Direction.Axis.X);
        BlockState silverPineLogZ = logWithAxis(silverPineLogBlock, Direction.Axis.Z);
        BlockState amberPlanks = amberPlanksBlock.defaultBlockState();
        BlockState silverPinePlanks = silverPinePlanksBlock.defaultBlockState();
        BlockState pineBottomSlab = slabState(silverPineSlabBlock, SlabType.BOTTOM);
        BlockState pineTopSlab = slabState(silverPineSlabBlock, SlabType.TOP);
        BlockState oasisGlass = nonWaterlogged(oasisGlassBlock.defaultBlockState());
        BlockState amberResin = nonWaterlogged(amberResinBlock.defaultBlockState());
        BlockState fenceChain = nonWaterlogged(amberFenceBlock.defaultBlockState());

        // 1. 4 Sturdy Amber-Log Corner Pillars at (±6, 2..9, ±6) + Vapor-Weaver Lichen Accents
        for (int[] col : PILLAR_COORDS) {
            int cx = col[0];
            int cz = col[1];
            for (int dy = 2; dy <= 9; dy++) {
                setRel(level, chunkBox, cx, dy, cz, amberLogY);
            }
            int outX = (cx < 0) ? -1 : 1;
            int outZ = (cz < 0) ? -1 : 1;
            Direction attachX = (outX < 0) ? Direction.EAST : Direction.WEST;
            Direction attachZ = (outZ < 0) ? Direction.SOUTH : Direction.NORTH;
            BlockState lichenX = AmberWeepingWillowFeature.makeSingleFaceLichenState(vaporLichenBlock, attachX);
            BlockState lichenZ = AmberWeepingWillowFeature.makeSingleFaceLichenState(vaporLichenBlock, attachZ);
            if (lichenX != null) {
                setRelIfAir(level, chunkBox, cx + outX, 4, cz, lichenX);
            }
            if (lichenZ != null) {
                setRelIfAir(level, chunkBox, cx, 4, cz + outZ, lichenZ);
            }
        }

        // Horizontal Ring Tie-Beams at dy = 9 connecting the 4 corner pillars
        for (int s = -5; s <= 5; s++) {
            setRel(level, chunkBox, s, 9, -6, amberLogX);
            setRel(level, chunkBox, s, 9, 6, amberLogX);
            setRel(level, chunkBox, -6, 9, s, amberLogZ);
            setRel(level, chunkBox, 6, 9, s, amberLogZ);
        }

        // 2. Lower Pagoda Roof Tier (dy = 10..11, spanning -8..8) + 4 Hanging Amber-Resin Corner Lanterns!
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                int maxAxis = Math.max(ax, az);

                if (maxAxis == 8) {
                    if (ax == 8 && az == 8) {
                        // Upturned pagoda wingtip corners!
                        setRel(level, chunkBox, dx, 10, dz, pineTopSlab);
                        setRel(level, chunkBox, dx, 11, dz, pineBottomSlab);
                    } else {
                        Direction eaveFacing = outerRingStairFacing(dx, dz, 8);
                        setRel(level, chunkBox, dx, 10, dz, stairState(silverPineStairsBlock, eaveFacing, Half.BOTTOM));
                    }
                } else if (maxAxis == 7) {
                    setRel(level, chunkBox, dx, 10, dz, silverPinePlanks);
                } else if (maxAxis >= 5) {
                    setRel(level, chunkBox, dx, 10, dz, amberPlanks);
                    if (maxAxis == 6) {
                        Direction tierFacing = outerRingStairFacing(dx, dz, 6);
                        setRel(level, chunkBox, dx, 11, dz, stairState(silverPineStairsBlock, tierFacing, Half.BOTTOM));
                    } else {
                        setRel(level, chunkBox, dx, 11, dz, (ax == 0 || az == 0) ? silverPinePlanks : amberPlanks);
                    }
                } else if (maxAxis == 4) {
                    // Inner ceiling ring framing the central skylight atrium
                    setRel(level, chunkBox, dx, 11, dz, (az == 4) ? silverPineLogX : silverPineLogZ);
                }
            }
        }

        // 4 Hanging Amber-Resin-Block Corner Lanterns under the 4 corner pagoda eaves at (±7, 8..9, ±7)
        for (int sx : new int[]{-1, 1}) {
            for (int sz : new int[]{-1, 1}) {
                setRel(level, chunkBox, sx * 7, 9, sz * 7, fenceChain);
                setRel(level, chunkBox, sx * 7, 8, sz * 7, amberResin);
            }
        }

        // 3. Clerestory Lantern Drum (dy = 12..13, max(|dx|,|dz|) == 4) with Oasis Glass Windows
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                if (Math.max(ax, az) != 4) {
                    continue;
                }
                boolean isCorner = (ax == 4 && az == 4);
                boolean isMullion = (ax == 0 || az == 0);
                for (int dy = 12; dy <= 13; dy++) {
                    if (isCorner || isMullion) {
                        setRel(level, chunkBox, dx, dy, dz, amberLogY);
                    } else {
                        setRel(level, chunkBox, dx, dy, dz, oasisGlass);
                    }
                }
            }
        }

        // 4. Upper Pagoda Tier & Wide Central Oasis-Glass Skylight (dy = 14..17)
        for (int dx = -5; dx <= 5; dx++) {
            for (int dz = -5; dz <= 5; dz++) {
                int ax = Math.abs(dx);
                int az = Math.abs(dz);
                int maxAxis = Math.max(ax, az);

                if (maxAxis == 5) {
                    if (ax == 5 && az == 5) {
                        setRel(level, chunkBox, dx, 14, dz, pineTopSlab);
                        setRel(level, chunkBox, dx, 15, dz, pineBottomSlab);
                    } else {
                        Direction eaveFacing = outerRingStairFacing(dx, dz, 5);
                        setRel(level, chunkBox, dx, 14, dz, stairState(silverPineStairsBlock, eaveFacing, Half.BOTTOM));
                    }
                } else if (maxAxis == 4) {
                    setRel(level, chunkBox, dx, 14, dz, silverPinePlanks);
                } else if (maxAxis == 3) {
                    setRel(level, chunkBox, dx, 14, dz, amberPlanks);
                    Direction upperStairFacing = outerRingStairFacing(dx, dz, 3);
                    setRel(level, chunkBox, dx, 15, dz, stairState(silverPineStairsBlock, upperStairFacing, Half.BOTTOM));
                } else {
                    // Wide 5x5 Central Oasis-Glass Skylight at dy = 15!
                    setRel(level, chunkBox, dx, 15, dz, oasisGlass);
                }
            }
        }

        // Crown Lantern Oculus & Finial (dy = 16..17)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    setRel(level, chunkBox, 0, 16, 0, amberResin);
                    setRel(level, chunkBox, 0, 17, 0, pineBottomSlab);
                } else {
                    setRel(level, chunkBox, dx, 16, dz, oasisGlass);
                }
            }
        }
    }

    private void buildPoolsideWeepingAmberWillow(WorldGenLevel level, BoundingBox chunkBox) {
        Block amberLogBlock = resolveBlock("amber_log", Blocks.OAK_LOG);
        Block willowLeavesBlock = resolveBlock("frost_willow_leaves", Blocks.AZALEA_LEAVES);
        Block willowVinesBlock = resolveBlock("frost_willow_vines", Blocks.VINE);

        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState amberLogY = logWithAxis(amberLogBlock, Direction.Axis.Y);
        BlockState amberLogX = logWithAxis(amberLogBlock, Direction.Axis.X);
        BlockState amberLogZ = logWithAxis(amberLogBlock, Direction.Axis.Z);
        BlockState leavesState = makePersistentLeaves(willowLeavesBlock);

        // Trunk rooted at (-4, 2, -4) on NW island planter, leaning gracefully over the pool to (-3, 5..7, -3)
        setRel(level, chunkBox, -4, 2, -4, dirt);
        setRel(level, chunkBox, -4, 3, -4, amberLogY);
        setRel(level, chunkBox, -4, 4, -4, amberLogY);
        setRel(level, chunkBox, -4, 5, -4, amberLogY);
        setRel(level, chunkBox, -3, 5, -3, amberLogY);
        setRel(level, chunkBox, -3, 6, -3, amberLogY);
        setRel(level, chunkBox, -3, 7, -3, amberLogY);

        // Arching upper boughs at dy = 7
        setRel(level, chunkBox, -4, 7, -3, amberLogX);
        setRel(level, chunkBox, -2, 7, -3, amberLogX);
        setRel(level, chunkBox, -3, 7, -4, amberLogZ);
        setRel(level, chunkBox, -3, 7, -2, amberLogZ);

        // Lush weeping Frost-Willow Canopy centered at (-3, 6..8, -3), tucked cleanly beneath dy = 10 roof
        // Crown cap at dy = 8
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                if (Math.hypot(ox, oz) <= 1.45D) {
                    setRelIfAir(level, chunkBox, -3 + ox, 8, -3 + oz, leavesState);
                }
            }
        }
        // Wide overhang canopy at dy = 7 (radius 2 around (-3, 7, -3) -> dx = -5..-1, dz = -5..-1)
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                if (Math.hypot(ox, oz) <= 2.15D) {
                    setRelIfAir(level, chunkBox, -3 + ox, 7, -3 + oz, leavesState);
                }
            }
        }
        // Inner support leaf ring at dy = 6 (3x3 around (-3, 6, -3) -> dx = -4..-2, dz = -4..-2)
        for (int ox = -1; ox <= 1; ox++) {
            for (int oz = -1; oz <= 1; oz++) {
                setRelIfAir(level, chunkBox, -3 + ox, 6, -3 + oz, leavesState);
            }
        }

        // Hang 2..3-block supported Frost-Willow Vine curtains around the outer edge of the dy = 6 leaf core!
        // Every top vine cell at dy = 6 has BOTH an overhanging leaf directly above at dy = 7 (UP = true)
        // AND a solid inner leaf block in inwardDir at dy = 6 (VineBlock.getPropertyForFace(inwardDir) = true)!
        hangStructureVineCurtain(level, chunkBox, -2, 6, -5, Direction.SOUTH, willowVinesBlock, 2);
        hangStructureVineCurtain(level, chunkBox, -3, 6, -5, Direction.SOUTH, willowVinesBlock, 3);
        hangStructureVineCurtain(level, chunkBox, -5, 6, -3, Direction.EAST, willowVinesBlock, 3);
        hangStructureVineCurtain(level, chunkBox, -5, 6, -2, Direction.EAST, willowVinesBlock, 2);
        hangStructureVineCurtain(level, chunkBox, -1, 6, -3, Direction.WEST, willowVinesBlock, 2);
        hangStructureVineCurtain(level, chunkBox, -3, 6, -1, Direction.NORTH, willowVinesBlock, 3);
        hangStructureVineCurtain(level, chunkBox, -4, 6, -1, Direction.NORTH, willowVinesBlock, 2);
    }

    private void hangStructureVineCurtain(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int topDy,
            int dz,
            Direction inwardDir,
            Block vineBlock,
            int length
    ) {
        BooleanProperty inwardProp = VineBlock.getPropertyForFace(inwardDir);
        BlockState baseVine = AmberWeepingWillowFeature.cleanVineState(vineBlock);
        if (!baseVine.hasProperty(inwardProp)) {
            return;
        }

        for (int i = 0; i < length; i++) {
            BlockPos pos = toWorld(dx, topDy - i, dz);
            if (!chunkBox.isInside(pos)) {
                continue;
            }
            if (!level.getBlockState(pos).isAir()) {
                break;
            }
            BlockState seg = baseVine.setValue(inwardProp, true);
            if (i == 0 && seg.hasProperty(VineBlock.UP)
                    && VineBlock.isAcceptableNeighbour(level, pos.above(), Direction.DOWN)) {
                seg = seg.setValue(VineBlock.UP, true);
            }
            level.setBlock(pos, seg, 2);
        }
    }

    private void placeBathhouseChest(
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
                level, random, pos, ModStructures.AMBER_WILLOW_BATHHOUSE_CHEST_LOOT);
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

    private static Direction outerRingStairFacing(int dx, int dz, int ringRadius) {
        // Stair Block FACING points toward the full back step so stairs slope downward toward the exterior!
        if (dz == -ringRadius) {
            return Direction.SOUTH;
        }
        if (dz == ringRadius) {
            return Direction.NORTH;
        }
        if (dx == -ringRadius) {
            return Direction.EAST;
        }
        return Direction.WEST;
    }

    private static BlockState oasisDewBasinState(Block basinBlock, boolean infused) {
        BlockState state = nonWaterlogged(basinBlock.defaultBlockState());
        for (Property<?> prop : state.getProperties()) {
            if ("infused".equals(prop.getName()) && prop instanceof BooleanProperty boolProp) {
                state = state.setValue(boolProp, infused);
            }
        }
        return state;
    }

    private static BlockState standingTorchState(Block torchBlock) {
        return nonWaterlogged(torchBlock.defaultBlockState());
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

    private static BlockState makePersistentLeaves(Block leavesBlock) {
        BlockState state = nonWaterlogged(leavesBlock.defaultBlockState());
        if (state.hasProperty(LeavesBlock.PERSISTENT)) {
            return state.setValue(LeavesBlock.PERSISTENT, true);
        }
        if (state.hasProperty(BlockStateProperties.PERSISTENT)) {
            return state.setValue(BlockStateProperties.PERSISTENT, true);
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static Block resolveVitaeSpringStone() {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "vitae_spring_stone");
        Optional<Block> opt = BuiltInRegistries.BLOCK.getOptional(id);
        if (opt.isPresent()) {
            return opt.get();
        }
        try {
            if (ModBlocks.VITAE_SPRING_STONE.isPresent()) {
                return ModBlocks.VITAE_SPRING_STONE.get();
            }
        } catch (Throwable ignored) {
        }
        return Blocks.CALCITE;
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

    private void setRelIfAir(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState state) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir()) {
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
