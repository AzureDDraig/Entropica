package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.AstralMirrorBlock;
import ddraig.net.entropica.block.GlowpegBlock;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
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
 * Programmatic 3D generator for Biome #17's core-free &amp; anchor-free observatory:
 * {@code Stardust Skerry Orrery} ({@code 20 x 20 x 22},
 * {@code dx = -10..9, dy = -4..17, dz = -10..9} — strictly ZERO Life-Force Cores or Anchors):
 * <ul>
 *   <li><b>1. Tapered Skerry Foundation &amp; 4 Diagonal Stardust Spikes ({@code dy <= 0..8})</b>:
 *       Organic {@code astral_impactite} + {@code starlight_ice} + {@code coarse_gravel} skerry foundation
 *       extending down to solid seafloor so it never floats over channels, ringed at the 4 diagonal
 *       corners by natural {@code stardust_crystal_block} + {@code stardust_crystal_cluster} Stardust Spikes.</li>
 *   <li><b>2. Raised Octagonal Observatory Platform &amp; Sunken Concave {@code astral_mirror_block} Dish ({@code dy = 0..2})</b>:
 *       {@code astral_marble_bricks}, {@code astral_marble}, {@code chiseled_astral_marble},
 *       {@code runed_astral_marble}, and {@code engraved_astral_slate} coordinate rim surrounding a
 *       stepped concave {@code astral_mirror_block} sky-reflecting floor dish ({@code dy = 1} outer ring,
 *       {@code dy = 0} inner focal mirror) directly beneath the open zenith sighting slit.</li>
 *   <li><b>3. Arched {@code astral_glass} + {@code astral_marble} Observatory Dome, {@code 5}
 *       {@code celestial_orrery_lantern}s, {@code soul_glowpeg} Accents &amp; {@code 2} Loot Chests</b>:
 *       8 octagonal pillars, 4 cardinal entry arches, 4 diagonal {@code astral_glass} windows, vaulted
 *       {@code astral_glass} dome crowned by an open zenith sighting slit, {@code 5} hanging
 *       {@code celestial_orrery_lantern}s (4 cardinal dome arches + 1 sighting ring), and {@code 2}
 *       star-charting reliquary chests ({@code entropica:chests/stardust_skerry_orrery}).</li>
 *   <li><b>4. Wild Observatory Guardians</b>:
 *       Spawns {@code 2} persistent wild {@code StarlightNautilusEntity} gliding above the observatory deck.</li>
 * </ul>
 */
public class StardustSkerryOrreryPiece extends StructurePiece {

    private static final ResourceLocation STARLIGHT_NAUTILUS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "starlight_nautilus");

    private static final int[][] OCTAGON_PILLARS = {
            {-6, -3}, {-3, -6}, {2, -6}, {5, -3},
            {-6, 2}, {-3, 5}, {2, 5}, {5, 2}
    };

    private static final int[][] DIAGONAL_SPIKE_ROOTS = {
            {-8, -8, -1, -1},
            {7, -8, 1, -1},
            {-8, 7, -1, 1},
            {7, 7, 1, 1}
    };

    private static final int[][] CARDINAL_ARCH_LANTERNS = {
            {0, 8, -6},
            {-1, 8, 5},
            {-6, 8, -1},
            {5, 8, 0}
    };

    private static final int[][] RIM_GLOWPEG_POSTS = {
            {-8, -4}, {-4, -8}, {3, -8}, {7, -4},
            {-8, 3}, {-4, 7}, {3, 7}, {7, 3}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;
    private boolean spawnedFirstNautilus;
    private boolean spawnedSecondNautilus;

    public StardustSkerryOrreryPiece(int originX, int baseY, int originZ) {
        // Exact 20 (X: -10..+9) x 22 (Y: -4..+17) x 20 (Z: -10..+9) bounding box
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 10,
                baseY - 4,
                originZ - 10,
                originX + 9,
                baseY + 17,
                originZ + 9
        ));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
        this.spawnedFirstNautilus = false;
        this.spawnedSecondNautilus = false;
    }

    public StardustSkerryOrreryPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 10);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 4);
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 10);
        this.spawnedFirstNautilus = tag.getBooleanOr("SpawnedFirstNautilus", false);
        this.spawnedSecondNautilus = tag.getBooleanOr("SpawnedSecondNautilus", false);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.STARDUST_SKERRY_ORRERY_PIECE.isPresent()) {
            return ModStructures.STARDUST_SKERRY_ORRERY_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "stardust_skerry_orrery_piece");
        return BuiltInRegistries.STRUCTURE_PIECE.getOptional(id).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("BaseY", this.baseY);
        tag.putInt("OriginZ", this.originZ);
        tag.putBoolean("SpawnedFirstNautilus", this.spawnedFirstNautilus);
        tag.putBoolean("SpawnedSecondNautilus", this.spawnedSecondNautilus);
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

    public BlockPos getSightingRingLanternPos() {
        return toWorld(0, 14, -2);
    }

    public BlockPos getNorthWestReliquaryChestPos() {
        return toWorld(-4, 3, -4);
    }

    public BlockPos getSouthEastReliquaryChestPos() {
        return toWorld(3, 3, 3);
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
        buildTaperedSkerryFoundationAndCornerSpikes(level, chunkBox);
        buildOctagonalPlatformAndConcaveMirrorDish(level, chunkBox);
        buildObservatoryColonadeDomeLanternsAndChests(level, chunkBox, random);
        spawnObservatoryStarlightNautiluses(level, chunkBox);
    }

    private void buildTaperedSkerryFoundationAndCornerSpikes(WorldGenLevel level, BoundingBox chunkBox) {
        Block impactiteBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_IMPACTITE.isPresent() ? ModBlocks.ASTRAL_IMPACTITE.get() : null,
                "astral_impactite", Blocks.DEEPSLATE);
        Block buddingImpactiteBlock = resolveBlock(
                () -> ModBlocks.BUDDING_ASTRAL_IMPACTITE.isPresent() ? ModBlocks.BUDDING_ASTRAL_IMPACTITE.get() : null,
                "budding_astral_impactite", impactiteBlock);
        Block starlightIceBlock = resolveBlock(
                () -> ModBlocks.STARLIGHT_ICE.isPresent() ? ModBlocks.STARLIGHT_ICE.get() : null,
                "starlight_ice", Blocks.BLUE_ICE);
        Block coarseGravelBlock = resolveBlock(
                () -> ModBlocks.COARSE_GRAVEL.isPresent() ? ModBlocks.COARSE_GRAVEL.get() : null,
                "coarse_gravel", Blocks.GRAVEL);
        Block stardustCrystalBlock = resolveBlock(
                () -> ModBlocks.STARDUST_CRYSTAL_BLOCK.isPresent() ? ModBlocks.STARDUST_CRYSTAL_BLOCK.get() : null,
                "stardust_crystal_block", Blocks.AMETHYST_BLOCK);
        Optional<Block> stardustClusterOpt = resolveOptionalBlock(
                () -> ModBlocks.STARDUST_CRYSTAL_CLUSTER.isPresent() ? ModBlocks.STARDUST_CRYSTAL_CLUSTER.get() : null,
                "stardust_crystal_cluster");

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState impactite = impactiteBlock.defaultBlockState();
        BlockState buddingImpactite = buddingImpactiteBlock.defaultBlockState();
        BlockState starlightIce = starlightIceBlock.defaultBlockState();
        BlockState blueIce = Blocks.BLUE_ICE.defaultBlockState();
        BlockState coarseGravel = coarseGravelBlock.defaultBlockState();
        BlockState stardustCrystal = stardustCrystalBlock.defaultBlockState();

        // 1. Clear above-surface volume across the skerry observatory footprint (dy = 1..17)
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                double rx = dx + 0.5D;
                double rz = dz + 0.5D;
                if (rx * rx + rz * rz <= 105.0D) {
                    for (int dy = 1; dy <= 17; dy++) {
                        setRel(level, chunkBox, dx, dy, dz, air);
                    }
                }
            }
        }

        // 2. Tapered natural Astral Impactite + Starlight Ice + Coarse Gravel skerry island foundation
        //    (dy = -4..0) extending cleanly down to the solid seafloor so the Orrery NEVER floats over water
        for (int dx = -10; dx <= 9; dx++) {
            for (int dz = -10; dz <= 9; dz++) {
                double rx = dx + 0.5D;
                double rz = dz + 0.5D;
                double distSq = rx * rx + rz * rz;
                double angle = Math.atan2(rz, rx);
                double lobeMod = 1.0D + 0.08D * Math.cos(angle * 4.0D) + 0.05D * Math.sin(angle * 6.0D);
                double maxRadiusSq = 92.0D * lobeMod;

                if (distSq > maxRadiusSq) {
                    continue;
                }

                boolean outerTalusMargin = distSq > 66.0D * lobeMod;
                boolean starlightVein = (((dx + dz) & 3) == 0) || (Math.abs(dx - dz) <= 1 && distSq > 28.0D);

                for (int dy = -4; dy <= 0; dy++) {
                    // Taper outer margin slightly near dy = 0 so shoreline steps gently into the water
                    if (dy == 0 && distSq > 84.0D * lobeMod) {
                        continue;
                    }
                    BlockState layerState;
                    if (dy == 0 && outerTalusMargin) {
                        layerState = (((dx * 31 + dz * 17) & 3) == 0) ? coarseGravel : (starlightVein ? starlightIce : impactite);
                    } else if (dy >= -2 && starlightVein) {
                        layerState = (dy == -2 && ((dx + dz) & 1) == 0) ? blueIce : starlightIce;
                    } else {
                        layerState = impactite;
                    }
                    setRel(level, chunkBox, dx, dy, dz, layerState);
                }

                // Seal downward from dy = -5 to solid seafloor with tapered skerry pilings
                boolean isCoreSupport = distSq <= 64.0D;
                fillSkerryFoundationDown(
                        level, chunkBox, dx, -5, dz,
                        isCoreSupport, starlightVein,
                        impactite, starlightIce, blueIce, coarseGravel
                );
            }
        }

        // 3. Four Diagonal Corner Natural Stardust Spikes (NW, NE, SW, SE)
        for (int[] corner : DIAGONAL_SPIKE_ROOTS) {
            int rootX = corner[0];
            int rootZ = corner[1];
            int stepOutX = corner[2];
            int stepOutZ = corner[3];

            // Fractured astral_impactite + budding_astral_impactite collar around the spike base (dy = 1..2)
            for (int ox = -1; ox <= 1; ox++) {
                for (int oz = -1; oz <= 1; oz++) {
                    int cx = rootX + ox;
                    int cz = rootZ + oz;
                    if (cx < -10 || cx > 9 || cz < -10 || cz > 9) {
                        continue;
                    }
                    int adx2 = Math.abs(2 * cx + 1);
                    int adz2 = Math.abs(2 * cz + 1);
                    // Do not intrude into the octagonal marble platform
                    if (adx2 <= 15 && adz2 <= 15 && (adx2 + adz2) <= 24) {
                        continue;
                    }
                    setRel(level, chunkBox, cx, 1, cz, (ox == 0 || oz == 0) ? buddingImpactite : impactite);
                    if (ox == 0 && oz == 0) {
                        setRel(level, chunkBox, cx, 2, cz, impactite);
                    }
                }
            }

            // Tapering 8-block Stardust Crystal Spike angling 1 block outward near the upper tip (dy = 1..8)
            for (int dy = 1; dy <= 5; dy++) {
                setRel(level, chunkBox, rootX, dy, rootZ, stardustCrystal);
                if (dy <= 3) {
                    setRel(level, chunkBox, rootX + stepOutX, dy, rootZ, stardustCrystal);
                    setRel(level, chunkBox, rootX, dy, rootZ + stepOutZ, stardustCrystal);
                }
            }
            int outerTipX = rootX + stepOutX;
            int outerTipZ = rootZ + stepOutZ;
            setRel(level, chunkBox, rootX, 6, rootZ, stardustCrystal);
            setRel(level, chunkBox, outerTipX, 5, outerTipZ, stardustCrystal);
            for (int dy = 6; dy <= 8; dy++) {
                setRel(level, chunkBox, outerTipX, dy, outerTipZ, stardustCrystal);
            }

            // Crown and stud the corner Stardust Spike with oriented stardust_crystal_cluster buds
            if (stardustClusterOpt.isPresent()) {
                Block clusterBlock = stardustClusterOpt.get();
                Direction outHorizX = stepOutX < 0 ? Direction.WEST : Direction.EAST;
                Direction outHorizZ = stepOutZ < 0 ? Direction.NORTH : Direction.SOUTH;

                placeClusterRel(level, chunkBox, outerTipX, 9, outerTipZ, Direction.UP, clusterBlock);
                placeClusterRel(level, chunkBox, rootX, 7, rootZ, Direction.UP, clusterBlock);
                placeClusterRel(level, chunkBox, rootX + stepOutX, 4, rootZ, Direction.UP, clusterBlock);
                placeClusterRel(level, chunkBox, rootX, 4, rootZ + stepOutZ, Direction.UP, clusterBlock);
                if (outerTipX + stepOutX >= -10 && outerTipX + stepOutX <= 9) {
                    placeClusterRel(level, chunkBox, outerTipX + stepOutX, 7, outerTipZ, outHorizX, clusterBlock);
                }
                if (outerTipZ + stepOutZ >= -10 && outerTipZ + stepOutZ <= 9) {
                    placeClusterRel(level, chunkBox, outerTipX, 6, outerTipZ + stepOutZ, outHorizZ, clusterBlock);
                }
            }
        }
    }

    private void buildOctagonalPlatformAndConcaveMirrorDish(WorldGenLevel level, BoundingBox chunkBox) {
        Block marbleBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE.isPresent() ? ModBlocks.ASTRAL_MARBLE.get() : null,
                "astral_marble", Blocks.QUARTZ_BLOCK);
        Block marbleBricksBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE_BRICKS.isPresent() ? ModBlocks.ASTRAL_MARBLE_BRICKS.get() : null,
                "astral_marble_bricks", Blocks.QUARTZ_BRICKS);
        Block chiseledMarbleBlock = resolveBlock(
                () -> ModBlocks.CHISELED_ASTRAL_MARBLE.isPresent() ? ModBlocks.CHISELED_ASTRAL_MARBLE.get() : null,
                "chiseled_astral_marble", Blocks.CHISELED_QUARTZ_BLOCK);
        Block runedMarbleBlock = resolveBlock(
                () -> ModBlocks.RUNED_ASTRAL_MARBLE.isPresent() ? ModBlocks.RUNED_ASTRAL_MARBLE.get() : null,
                "runed_astral_marble", chiseledMarbleBlock);
        Block engravedSlateBlock = resolveBlock(
                () -> ModBlocks.ENGRAVED_ASTRAL_SLATE.isPresent() ? ModBlocks.ENGRAVED_ASTRAL_SLATE.get() : null,
                "engraved_astral_slate", Blocks.POLISHED_DEEPSLATE);
        Block marbleStairsBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE_STAIRS.isPresent() ? ModBlocks.ASTRAL_MARBLE_STAIRS.get() : null,
                "astral_marble_stairs", Blocks.QUARTZ_STAIRS);
        Block astralMirrorBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MIRROR_BLOCK.isPresent() ? ModBlocks.ASTRAL_MIRROR_BLOCK.get() : null,
                "astral_mirror_block", Blocks.SEA_LANTERN);

        BlockState air = Blocks.AIR.defaultBlockState();
        BlockState marble = marbleBlock.defaultBlockState();
        BlockState marbleBricks = marbleBricksBlock.defaultBlockState();
        BlockState chiseledMarble = chiseledMarbleBlock.defaultBlockState();
        BlockState runedMarble = runedMarbleBlock.defaultBlockState();
        BlockState engravedSlate = engravedSlateBlock.defaultBlockState();
        BlockState mirrorDefault = astralMirrorBlock.defaultBlockState();

        // 1. Raised Octagonal Observatory Platform (dy = 1..2, dx = -8..7, dz = -8..7, adx2 + adz2 <= 24)
        for (int dx = -8; dx <= 7; dx++) {
            for (int dz = -8; dz <= 7; dz++) {
                int adx2 = Math.abs(2 * dx + 1);
                int adz2 = Math.abs(2 * dz + 1);
                if (adx2 + adz2 > 24) {
                    continue;
                }

                // Skip central 6x6 sunken concave mirror dish zone (dx = -3..2, dz = -3..2 -> adx2 <= 5 && adz2 <= 5)
                if (adx2 <= 5 && adz2 <= 5) {
                    continue;
                }

                // Solid sub-deck at dy = 1
                setRel(level, chunkBox, dx, 1, dz, marbleBricks);

                // Cardinal boarding steps at the outer rim (dy = 2)
                if (dz == -8 && dx >= -2 && dx <= 1) {
                    setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.SOUTH, Half.BOTTOM));
                    continue;
                }
                if (dz == 7 && dx >= -2 && dx <= 1) {
                    setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.NORTH, Half.BOTTOM));
                    continue;
                }
                if (dx == -8 && dz >= -2 && dz <= 1) {
                    setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.EAST, Half.BOTTOM));
                    continue;
                }
                if (dx == 7 && dz >= -2 && dz <= 1) {
                    setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.WEST, Half.BOTTOM));
                    continue;
                }

                // Engraved Astral Slate & Runed Astral Marble outer coordinate azimuth rim
                boolean isCoordinateRim = (adx2 == 15 || adz2 == 15 || (adx2 + adz2) >= 22);
                if (isCoordinateRim) {
                    boolean degreeTick = (adx2 == 7 || adz2 == 7 || (adx2 == 11 && adz2 == 11));
                    setRel(level, chunkBox, dx, 2, dz, degreeTick ? runedMarble : engravedSlate);
                    continue;
                }

                // Inner observatory deck with cardinal & diagonal meridian lines
                boolean cardinalMeridian = (dx == -1 || dx == 0 || dz == -1 || dz == 0);
                boolean runedRing = (adx2 == 9 && adz2 <= 3) || (adz2 == 9 && adx2 <= 3);
                if (runedRing) {
                    setRel(level, chunkBox, dx, 2, dz, runedMarble);
                } else if (cardinalMeridian) {
                    setRel(level, chunkBox, dx, 2, dz, chiseledMarble);
                } else {
                    setRel(level, chunkBox, dx, 2, dz, ((dx + dz) & 1) == 0 ? marble : marbleBricks);
                }
            }
        }

        // Lower shoreline boarding stairs at dy = 1 on all 4 cardinal approaches
        for (int d = -2; d <= 1; d++) {
            setRel(level, chunkBox, d, 1, -9, stairState(marbleStairsBlock, Direction.SOUTH, Half.BOTTOM));
            setRel(level, chunkBox, d, 1, 8, stairState(marbleStairsBlock, Direction.NORTH, Half.BOTTOM));
            setRel(level, chunkBox, -9, 1, d, stairState(marbleStairsBlock, Direction.EAST, Half.BOTTOM));
            setRel(level, chunkBox, 8, 1, d, stairState(marbleStairsBlock, Direction.WEST, Half.BOTTOM));
        }

        // 2. Sunken Concave Sky-Reflecting astral_mirror_block Floor Dish (dx = -3..2, dz = -3..2)
        for (int dx = -3; dx <= 2; dx++) {
            for (int dz = -3; dz <= 2; dz++) {
                int adx2 = Math.abs(2 * dx + 1);
                int adz2 = Math.abs(2 * dz + 1);

                if (adx2 == 5 || adz2 == 5) {
                    // Stepped concave marble collar at dy = 2 framing the 4x4 sunken mirror basin
                    setRel(level, chunkBox, dx, 0, dz, marbleBricks);
                    setRel(level, chunkBox, dx, 1, dz, engravedSlate);
                    if (adx2 == 5 && adz2 == 5) {
                        setRel(level, chunkBox, dx, 2, dz, chiseledMarble);
                    } else if (dz == -3) {
                        setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.NORTH, Half.BOTTOM));
                    } else if (dz == 2) {
                        setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.SOUTH, Half.BOTTOM));
                    } else if (dx == -3) {
                        setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.WEST, Half.BOTTOM));
                    } else {
                        setRel(level, chunkBox, dx, 2, dz, stairState(marbleStairsBlock, Direction.EAST, Half.BOTTOM));
                    }
                } else if (adx2 == 3 || adz2 == 3) {
                    // Outer concave Astral Mirror dish ring sunken 1 block below deck (dy = 1)
                    setRel(level, chunkBox, dx, -1, dz, marbleBricks);
                    setRel(level, chunkBox, dx, 0, dz, runedMarble);
                    setRel(level, chunkBox, dx, 1, dz, mirrorDefault);
                    setRel(level, chunkBox, dx, 2, dz, air);
                } else {
                    // Innermost 2x2 focal Astral Mirror dish well sunken 2 blocks below deck (dy = 0)
                    setRel(level, chunkBox, dx, -1, dz, runedMarble);
                    setRel(level, chunkBox, dx, 0, dz, mirrorDefault);
                    setRel(level, chunkBox, dx, 1, dz, air);
                    setRel(level, chunkBox, dx, 2, dz, air);
                }
            }
        }

        // Update connected-block properties on all placed AstralMirrorBlock tiles in the concave dish
        refreshConnectedAstralMirrors(level, chunkBox, astralMirrorBlock);
    }

    private void buildObservatoryColonadeDomeLanternsAndChests(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block marbleBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE.isPresent() ? ModBlocks.ASTRAL_MARBLE.get() : null,
                "astral_marble", Blocks.QUARTZ_BLOCK);
        Block marbleBricksBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE_BRICKS.isPresent() ? ModBlocks.ASTRAL_MARBLE_BRICKS.get() : null,
                "astral_marble_bricks", Blocks.QUARTZ_BRICKS);
        Block chiseledMarbleBlock = resolveBlock(
                () -> ModBlocks.CHISELED_ASTRAL_MARBLE.isPresent() ? ModBlocks.CHISELED_ASTRAL_MARBLE.get() : null,
                "chiseled_astral_marble", Blocks.CHISELED_QUARTZ_BLOCK);
        Block runedMarbleBlock = resolveBlock(
                () -> ModBlocks.RUNED_ASTRAL_MARBLE.isPresent() ? ModBlocks.RUNED_ASTRAL_MARBLE.get() : null,
                "runed_astral_marble", chiseledMarbleBlock);
        Block engravedSlateBlock = resolveBlock(
                () -> ModBlocks.ENGRAVED_ASTRAL_SLATE.isPresent() ? ModBlocks.ENGRAVED_ASTRAL_SLATE.get() : null,
                "engraved_astral_slate", Blocks.POLISHED_DEEPSLATE);
        Block marbleStairsBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE_STAIRS.isPresent() ? ModBlocks.ASTRAL_MARBLE_STAIRS.get() : null,
                "astral_marble_stairs", Blocks.QUARTZ_STAIRS);
        Block marbleSlabBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE_SLAB.isPresent() ? ModBlocks.ASTRAL_MARBLE_SLAB.get() : null,
                "astral_marble_slab", Blocks.QUARTZ_SLAB);
        Block marbleWallBlock = resolveBlock(
                () -> ModBlocks.ASTRAL_MARBLE_WALL.isPresent() ? ModBlocks.ASTRAL_MARBLE_WALL.get() : null,
                "astral_marble_wall", Blocks.COBBLESTONE_WALL);
        Block soulGlowpegBlock = resolveBlock(
                () -> ModBlocks.SOUL_GLOWPEG.isPresent() ? ModBlocks.SOUL_GLOWPEG.get() : null,
                "soul_glowpeg", Blocks.END_ROD);
        Block astralGlassBlock = resolveBlock(() -> null, "astral_glass", Blocks.PURPLE_STAINED_GLASS);
        Block orreryLanternBlock = resolveBlock(
                () -> ModBlocks.CELESTIAL_ORRERY_LANTERN.isPresent() ? ModBlocks.CELESTIAL_ORRERY_LANTERN.get() : null,
                "celestial_orrery_lantern", Blocks.SOUL_LANTERN);

        BlockState marble = marbleBlock.defaultBlockState();
        BlockState marbleBricks = marbleBricksBlock.defaultBlockState();
        BlockState chiseledMarble = chiseledMarbleBlock.defaultBlockState();
        BlockState runedMarble = runedMarbleBlock.defaultBlockState();
        BlockState engravedSlate = engravedSlateBlock.defaultBlockState();
        BlockState marbleBottomSlab = slabState(marbleSlabBlock, SlabType.BOTTOM);
        BlockState marbleWall = nonWaterlogged(marbleWallBlock.defaultBlockState());
        BlockState astralGlass = nonWaterlogged(astralGlassBlock.defaultBlockState());
        BlockState floorGlowpeg = glowpegState(soulGlowpegBlock, AttachFace.FLOOR, Direction.NORTH);
        BlockState ceilingGlowpeg = glowpegState(soulGlowpegBlock, AttachFace.CEILING, Direction.NORTH);

        // 1. Outer Coordinate Rim Sight-Posts & Soul Glowpeg Accents (dy = 3..4)
        for (int[] post : RIM_GLOWPEG_POSTS) {
            setRel(level, chunkBox, post[0], 3, post[1], marbleWall);
            setRel(level, chunkBox, post[0], 4, post[1], floorGlowpeg);
        }

        // 2. Eight Octagonal Observatory Pillars (dy = 3..9)
        for (int[] p : OCTAGON_PILLARS) {
            int px = p[0];
            int pz = p[1];
            for (int dy = 3; dy <= 9; dy++) {
                BlockState colMat;
                if (dy == 3 || dy == 9) {
                    colMat = chiseledMarble;
                } else if (dy == 6) {
                    colMat = runedMarble;
                } else {
                    colMat = marbleBricks;
                }
                setRel(level, chunkBox, px, dy, pz, colMat);
            }
        }

        // 3. Four Diagonal Arched Starlight Windows (astral_glass + engraved_astral_slate dado, dy = 3..9)
        int[][] diagonalWindowCols = {
                {-5, -4}, {-4, -5},
                {4, -4}, {3, -5},
                {-5, 3}, {-4, 4},
                {4, 3}, {3, 4}
        };
        for (int[] wc : diagonalWindowCols) {
            setRel(level, chunkBox, wc[0], 3, wc[1], engravedSlate);
            for (int dy = 4; dy <= 8; dy++) {
                setRel(level, chunkBox, wc[0], dy, wc[1], astralGlass);
            }
            setRel(level, chunkBox, wc[0], 9, wc[1], marbleBricks);
        }

        // 4. Four Cardinal Observatory Entry Archways (wide open dy = 3..7; vaulted arch lintels dy = 8..9)
        // North (dz = -6) & South (dz = 5)
        for (int archZ : new int[]{-6, 5}) {
            setRel(level, chunkBox, -2, 8, archZ, stairState(marbleStairsBlock, Direction.WEST, Half.TOP));
            setRel(level, chunkBox, 1, 8, archZ, stairState(marbleStairsBlock, Direction.EAST, Half.TOP));
            for (int dx = -2; dx <= 1; dx++) {
                boolean centerKey = (dx == -1 || dx == 0);
                setRel(level, chunkBox, dx, 9, archZ, centerKey ? chiseledMarble : marbleBricks);
            }
        }
        // West (dx = -6) & East (dx = 5)
        for (int archX : new int[]{-6, 5}) {
            setRel(level, chunkBox, archX, 8, -2, stairState(marbleStairsBlock, Direction.NORTH, Half.TOP));
            setRel(level, chunkBox, archX, 8, 1, stairState(marbleStairsBlock, Direction.SOUTH, Half.TOP));
            for (int dz = -2; dz <= 1; dz++) {
                boolean centerKey = (dz == -1 || dz == 0);
                setRel(level, chunkBox, archX, 9, dz, centerKey ? chiseledMarble : marbleBricks);
            }
        }

        // 5. Two Star-Charting Reliquary Chests & Astrolabe Plinths on the Dry Observatory Deck (dy = 3)
        // NW Reliquary Chest at (-4, 3, -4) facing SOUTH; SE Reliquary Chest at (3, 3, 3) facing NORTH
        setRel(level, chunkBox, -5, 3, -3, chiseledMarble);
        setRel(level, chunkBox, -5, 4, -3, floorGlowpeg);
        placeOrreryChest(level, chunkBox, random, -4, 3, -4, Direction.SOUTH);

        setRel(level, chunkBox, 4, 3, 2, chiseledMarble);
        setRel(level, chunkBox, 4, 4, 2, floorGlowpeg);
        placeOrreryChest(level, chunkBox, random, 3, 3, 3, Direction.NORTH);

        // Additional NE & SW symmetry star-sighting pedestals with Soul Glowpegs
        setRel(level, chunkBox, 3, 3, -4, chiseledMarble);
        setRel(level, chunkBox, 3, 4, -4, floorGlowpeg);
        setRel(level, chunkBox, -4, 3, 3, chiseledMarble);
        setRel(level, chunkBox, -4, 4, 3, floorGlowpeg);

        // 6. Arched astral_glass + astral_marble Observatory Dome (dy = 10..14)
        for (int dy = 10; dy <= 14; dy++) {
            double minR;
            double maxR;
            switch (dy) {
                case 10 -> { minR = 5.15D; maxR = 6.65D; }
                case 11 -> { minR = 4.45D; maxR = 5.75D; }
                case 12 -> { minR = 3.75D; maxR = 4.95D; }
                case 13 -> { minR = 3.00D; maxR = 4.15D; }
                default -> { minR = 2.25D; maxR = 3.35D; }
            }
            for (int dx = -7; dx <= 6; dx++) {
                for (int dz = -7; dz <= 6; dz++) {
                    double r = Math.hypot(dx + 0.5D, dz + 0.5D);
                    if (r < minR || r > maxR) {
                        continue;
                    }
                    boolean cardinalRib = (dx == -1 || dx == 0 || dz == -1 || dz == 0);
                    if (dy == 10) {
                        setRel(level, chunkBox, dx, dy, dz, cardinalRib ? runedMarble : engravedSlate);
                    } else if (dy == 14) {
                        setRel(level, chunkBox, dx, dy, dz, cardinalRib ? chiseledMarble : marbleBricks);
                    } else if (cardinalRib) {
                        setRel(level, chunkBox, dx, dy, dz, (dy == 12) ? runedMarble : marble);
                    } else {
                        setRel(level, chunkBox, dx, dy, dz, astralGlass);
                    }
                }
            }
        }

        // Ceiling Soul Glowpeg accents under the 4 diagonal dome ribs (dy = 10)
        setRel(level, chunkBox, -4, 10, -4, ceilingGlowpeg);
        setRel(level, chunkBox, 3, 10, -4, ceilingGlowpeg);
        setRel(level, chunkBox, -4, 10, 3, ceilingGlowpeg);
        setRel(level, chunkBox, 3, 10, 3, ceilingGlowpeg);

        // 7. Upper Zenith Sighting Ring & Open Zenith Sighting Slit (dy = 15..17)
        //    Central 4x4 / 2x2 zenith aperture stays 100% open to the sky right above the concave Astral Mirror dish!
        for (int dx = -3; dx <= 2; dx++) {
            for (int dz = -3; dz <= 2; dz++) {
                int adx2 = Math.abs(2 * dx + 1);
                int adz2 = Math.abs(2 * dz + 1);
                boolean isOuterCorner = (adx2 == 5 && adz2 == 5);
                boolean isRingPerimeter = (adx2 == 5 || adz2 == 5) && !isOuterCorner;

                if (isRingPerimeter) {
                    // Keep a crisp North-South zenith meridian sighting slit notched at dy = 16 while ringing dy = 15
                    boolean meridianSlit = (dx == -1 || dx == 0);
                    setRel(level, chunkBox, dx, 15, dz, meridianSlit ? engravedSlate : chiseledMarble);
                    if (!meridianSlit) {
                        setRel(level, chunkBox, dx, 16, dz, marbleBottomSlab);
                    }
                }
            }
        }

        // Sighting bracket at (0, 15, -2) supporting the 5th Celestial Orrery Lantern at (0, 14, -2),
        // keeping (-1..0, -1..0) 100% open to the zenith sky!
        setRel(level, chunkBox, 0, 15, -2, chiseledMarble);

        // Four cardinal astrolabe finials reaching dy = 17 (full 22-block structure height -4..+17)
        int[][] zenithFinials = {
                {-3, 0}, {2, -1}, {0, -3}, {-1, 2}
        };
        for (int[] fin : zenithFinials) {
            setRel(level, chunkBox, fin[0], 16, fin[1], chiseledMarble);
            setRel(level, chunkBox, fin[0], 17, fin[1], floorGlowpeg);
        }

        // 8. Five Celestial Orrery Lanterns (4 hanging from cardinal dome arches + 1 at zenith sighting ring)
        for (int[] archLantern : CARDINAL_ARCH_LANTERNS) {
            placeAndScheduleOrreryLantern(
                    level, chunkBox, orreryLanternBlock,
                    archLantern[0], archLantern[1], archLantern[2]
            );
        }
        placeAndScheduleOrreryLantern(level, chunkBox, orreryLanternBlock, 0, 14, -2);
    }

    private void spawnObservatoryStarlightNautiluses(WorldGenLevel level, BoundingBox chunkBox) {
        BlockPos firstPos = toWorld(-2, 5, 0);
        if (!this.spawnedFirstNautilus && chunkBox.isInside(firstPos)) {
            this.spawnedFirstNautilus = true;
            spawnPersistentStarlightNautilus(level, firstPos, this.originX - 1.5D, this.baseY + 5.2D, this.originZ + 0.5D, 45.0F);
        }

        BlockPos secondPos = toWorld(1, 6, -1);
        if (!this.spawnedSecondNautilus && chunkBox.isInside(secondPos)) {
            this.spawnedSecondNautilus = true;
            spawnPersistentStarlightNautilus(level, secondPos, this.originX + 1.5D, this.baseY + 6.2D, this.originZ - 0.5D, 225.0F);
        }
    }

    private static void spawnPersistentStarlightNautilus(
            WorldGenLevel level,
            BlockPos spawnBlockPos,
            double wx,
            double wy,
            double wz,
            float yaw
    ) {
        EntityType<?> nautilusType = resolveStarlightNautilusType();
        if (nautilusType == null) {
            return;
        }
        Entity entity = nautilusType.create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return;
        }
        entity.setPos(wx, wy, wz);
        entity.setYRot(yaw);
        entity.setXRot(0.0F);
        if (entity instanceof Mob mob) {
            mob.yBodyRot = yaw;
            mob.setYHeadRot(yaw);
            mob.setPersistenceRequired();
            mob.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnBlockPos), EntitySpawnReason.STRUCTURE, null);
        }
        level.addFreshEntityWithPassengers(entity);
    }

    private static EntityType<?> resolveStarlightNautilusType() {
        try {
            if (ModEntityTypes.STARLIGHT_NAUTILUS.isPresent()) {
                return ModEntityTypes.STARLIGHT_NAUTILUS.get();
            }
        } catch (Throwable ignored) {
        }
        return BuiltInRegistries.ENTITY_TYPE.getOptional(STARLIGHT_NAUTILUS_ID).orElse(null);
    }

    private void refreshConnectedAstralMirrors(WorldGenLevel level, BoundingBox chunkBox, Block mirrorBlock) {
        for (int dx = -2; dx <= 1; dx++) {
            for (int dz = -2; dz <= 1; dz++) {
                int adx2 = Math.abs(2 * dx + 1);
                int adz2 = Math.abs(2 * dz + 1);
                int dy = (adx2 == 1 && adz2 == 1) ? 0 : 1;
                BlockPos pos = toWorld(dx, dy, dz);
                if (!chunkBox.isInside(pos)) {
                    continue;
                }
                BlockState state = level.getBlockState(pos);
                if (!state.is(mirrorBlock) || !(mirrorBlock instanceof AstralMirrorBlock)) {
                    continue;
                }
                state = state
                        .setValue(AstralMirrorBlock.UP, level.getBlockState(pos.above()).is(mirrorBlock))
                        .setValue(AstralMirrorBlock.DOWN, level.getBlockState(pos.below()).is(mirrorBlock))
                        .setValue(AstralMirrorBlock.NORTH, level.getBlockState(pos.north()).is(mirrorBlock))
                        .setValue(AstralMirrorBlock.SOUTH, level.getBlockState(pos.south()).is(mirrorBlock))
                        .setValue(AstralMirrorBlock.EAST, level.getBlockState(pos.east()).is(mirrorBlock))
                        .setValue(AstralMirrorBlock.WEST, level.getBlockState(pos.west()).is(mirrorBlock));
                level.setBlock(pos, state, 2);
            }
        }
    }

    private void placeAndScheduleOrreryLantern(
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
        try {
            if (ModBlocks.CELESTIAL_ORRERY_LANTERN.isPresent()) {
                level.scheduleTick(lanternPos, ModBlocks.CELESTIAL_ORRERY_LANTERN.get(), 20);
                return;
            }
        } catch (Throwable ignored) {
        }
        level.scheduleTick(lanternPos, lanternBlock, 20);
    }

    private void placeOrreryChest(
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
                level, random, pos, ModStructures.STARDUST_SKERRY_ORRERY_CHEST_LOOT);
    }

    private void placeClusterRel(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int dy,
            int dz,
            Direction facing,
            Block clusterBlock
    ) {
        BlockPos pos = toWorld(dx, dy, dz);
        if (!chunkBox.isInside(pos)) {
            return;
        }
        BlockState existing = level.getBlockState(pos);
        if (!existing.isAir() && !existing.is(Blocks.SNOW)) {
            return;
        }
        BlockState state = nonWaterlogged(clusterBlock.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.FACING)) {
            state = state.setValue(BlockStateProperties.FACING, facing);
        }
        if (state.canSurvive(level, pos)) {
            level.setBlock(pos, state, 2);
        }
    }

    private void fillSkerryFoundationDown(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int startDy,
            int dz,
            boolean isCoreSupport,
            boolean starlightVein,
            BlockState impactite,
            BlockState starlightIce,
            BlockState blueIce,
            BlockState coarseGravel
    ) {
        int wx = this.originX + dx;
        int wz = this.originZ + dz;
        if (wx < chunkBox.minX() || wx > chunkBox.maxX() || wz < chunkBox.minZ() || wz > chunkBox.maxZ()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 20);

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
                if (wy == minAllowedY || isCoreSupport) {
                    level.setBlock(cursor, starlightVein ? blueIce : impactite, 2);
                } else {
                    BlockState below = level.getBlockState(cursor.below());
                    boolean solidBelow = !below.isAir() && !below.is(Blocks.WATER) && !below.canBeReplaced();
                    if (solidBelow && ((dx + dz + wy) & 3) == 0) {
                        level.setBlock(cursor, coarseGravel, 2);
                    } else {
                        level.setBlock(cursor, starlightVein ? starlightIce : impactite, 2);
                    }
                }
            } else {
                break;
            }
        }
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

    private static BlockState glowpegState(Block pegBlock, AttachFace face, Direction facing) {
        BlockState state = nonWaterlogged(pegBlock.defaultBlockState());
        if (state.hasProperty(GlowpegBlock.FACE)) {
            state = state.setValue(GlowpegBlock.FACE, face);
        }
        if (state.hasProperty(GlowpegBlock.FACING)) {
            state = state.setValue(GlowpegBlock.FACING, facing);
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
