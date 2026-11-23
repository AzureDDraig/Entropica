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
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.GlowLichenBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.material.Fluids;

/**
 * Programmatic 3D generator for Biome #10's core-free sanctuary:
 * {@code Radiant Spring Pavilion} ({@code x = -10..10}, {@code z = -10..10}, {@code y = -4..14}
 * relative to inland surface {@code anchorY}):
 * <ul>
 *   <li><b>Rounded Circular Oasis Terrace ({@code r <= 10.5} at {@code y = 0})</b>:
 *       blends {@code entropica:radiant_snow_block} and golden-tinted {@link Blocks#GRASS_BLOCK}
 *       with {@link Blocks#SHORT_GRASS} over a natural {@code vitae_spring_stone} &amp;
 *       {@link Blocks#CALCITE} foundation skirt anchored into uneven terrain.</li>
 *   <li><b>Subterranean Golden Reliquary Vault ({@code x = -3..3, z = -3..3, y = -4..-1})</b>:
 *       walled in {@code entropica:vitae_spring_stone} &amp; {@link Blocks#QUARTZ_BRICKS},
 *       crowned with a central {@code entropica:radiant_glass} skylight at {@code (0, -1, 0)},
 *       lit by {@code entropica:sun_glazed_lantern} at {@code (0, -3, -2)}, housing {@code 2}
 *       loot chests at {@code (-2, -3, 0)} and {@code (2, -3, 0)} pointing to
 *       {@code entropica:chests/radiant_spring_pavilion}, and accessed via a watertight stepped
 *       side stairwell descending from the {@code -Z} terrace.</li>
 *   <li><b>Open Tiered Vitae Spring Fountain Basin ({@code r <= 7.8} at {@code y = 1})</b>:
 *       outer rim of {@code vitae_spring_stone} &amp; {@code smooth_quartz}, inner shelf of
 *       {@code radiant_glaze_ice} &amp; {@code smooth_quartz}, warm {@link Blocks#WATER} pool
 *       over {@code vitae_spring_stone}, {@code 4} corner {@code grass_block} +
 *       {@code entropica:vitae_orchid} planters at {@code (+-5, 1..2, +-5)}, and a pure
 *       {@code smooth_quartz} central column {@code (0, 1..2, 0)} topped by cascading
 *       {@link Blocks#WATER} at {@code (0, 3, 0)}.</li>
 *   <li><b>Four Amber-Log &amp; Smooth-Quartz Columns + Radiant Glass Skylight Dome</b>:
 *       columns at {@code (+-6, 1..7, +-6)} draped with {@code entropica:golden_lichen} on outer
 *       faces, {@code entropica:glazed_amber_leaves} capitals at {@code y = 8}, hanging
 *       {@code entropica:sun_glazed_lantern}s at {@code (+-5, 6, +-5)}, stepped
 *       {@code smooth_quartz}, {@code amber_planks}, and {@code entropica:radiant_glass} dome
 *       ({@code y = 8..12}), crowned by {@code smooth_quartz} {@code (0, 13, 0)} and
 *       {@code entropica:qilin_light_refractor} at {@code (0, 14, 0)}.</li>
 * </ul>
 */
public class RadiantSpringPavilionPiece extends StructurePiece {

    private static final int[][] COLUMN_COORDS = {
            {-6, -6}, {6, -6}, {-6, 6}, {6, 6}
    };

    private static final int[][] PLANTER_AND_LANTERN_COORDS = {
            {-5, -5}, {5, -5}, {-5, 5}, {5, 5}
    };

    private final int originX;
    private final int anchorY;
    private final int originZ;

    public RadiantSpringPavilionPiece(int originX, int anchorY, int originZ) {
        super(resolvePieceType(), 0, new BoundingBox(
                originX - 11,
                anchorY - 4,
                originZ - 11,
                originX + 10,
                anchorY + 14,
                originZ + 10
        ));
        this.originX = originX;
        this.anchorY = anchorY;
        this.originZ = originZ;
    }

    public RadiantSpringPavilionPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", this.boundingBox.minX() + 11);
        this.anchorY = tag.getIntOr("AnchorY", this.boundingBox.minY() + 4);
        this.originZ = tag.getIntOr("OriginZ", this.boundingBox.minZ() + 11);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.RADIANT_SPRING_PAVILION_PIECE.isPresent()) {
            return ModStructures.RADIANT_SPRING_PAVILION_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(
                Entropica.MODID, "radiant_spring_pavilion_piece");
        return BuiltInRegistries.STRUCTURE_PIECE.getOptional(id).orElseThrow();
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        tag.putInt("OriginX", this.originX);
        tag.putInt("AnchorY", this.anchorY);
        tag.putInt("OriginZ", this.originZ);
    }

    public int getOriginX() {
        return this.originX;
    }

    public int getAnchorY() {
        return this.anchorY;
    }

    public int getOriginZ() {
        return this.originZ;
    }

    public BlockPos getRefractorPos() {
        return toWorld(0, 14, 0);
    }

    public BlockPos getFountainTopWaterPos() {
        return toWorld(0, 3, 0);
    }

    public BlockPos getVaultWestChestPos() {
        return toWorld(-2, -3, 0);
    }

    public BlockPos getVaultEastChestPos() {
        return toWorld(2, -3, 0);
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
        buildCircularTerraceAndFoundationSkirt(level, chunkBox);
        buildSubterraneanReliquaryVaultAndStairwell(level, chunkBox, random);
        buildTieredVitaeSpringFountainBasin(level, chunkBox);
        buildColumnsAndRadiantGlassSkylightDome(level, chunkBox);
    }

    private void buildCircularTerraceAndFoundationSkirt(WorldGenLevel level, BoundingBox chunkBox) {
        Block radiantSnowBlock = resolveBlock("radiant_snow_block", Blocks.SNOW_BLOCK);
        Block springStoneBlock = resolveBlock("vitae_spring_stone", Blocks.CALCITE);
        Block featherMossBlock = resolveBlock("auroral_feather_moss", Blocks.MOSS_CARPET);

        BlockState radiantSnow = radiantSnowBlock.defaultBlockState();
        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState grassBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState dirt = Blocks.DIRT.defaultBlockState();
        BlockState calcite = Blocks.CALCITE.defaultBlockState();
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();
        BlockState featherMoss = featherMossBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        for (int dx = -10; dx <= 10; dx++) {
            for (int dz = -10; dz <= 10; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 10.5D) {
                    continue;
                }

                // Clear sky headroom above terrace (dy = 1..14)
                for (int dy = 1; dy <= 14; dy++) {
                    setRel(level, chunkBox, dx, dy, dz, air);
                }

                boolean insideVaultFootprint = (Math.abs(dx) <= 3 && Math.abs(dz) <= 3);
                boolean insideStairwellFootprint = (dx >= -2 && dx <= 0 && dz >= -9 && dz <= -4);

                if (!insideVaultFootprint && !insideStairwellFootprint) {
                    // Solid natural stone / spring-stone foundation skirt into uneven terrain
                    BlockState skirtState = (((dx + dz) & 1) == 0) ? springStone : calcite;
                    setRel(level, chunkBox, dx, -1, dz, (r > 8.2D) ? dirt : springStone);
                    fillFoundationDown(level, chunkBox, dx, -2, dz, skirtState);
                }

                // Terrace surface at dy = 0 (fountain interior r < 6.8 will refine dy = 0 to springStone/skylight)
                if (r <= 7.8D) {
                    setRel(level, chunkBox, dx, 0, dz, springStone);
                } else {
                    boolean cardinalPath = (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) && r <= 9.6D;
                    boolean grassMeadowPatch = (((dx * 5 + dz * 3) & 3) != 0);
                    BlockState terraceTop = cardinalPath
                            ? (((dx + dz) & 1) == 0 ? smoothQuartz : springStone)
                            : (grassMeadowPatch ? grassBlock : radiantSnow);
                    setRel(level, chunkBox, dx, 0, dz, terraceTop);

                    // Scatter golden-tinted short_grass and auroral_feather_moss on outer terrace ring
                    if (!cardinalPath && r >= 8.2D && r <= 10.2D && !(dx >= -2 && dx <= 0 && dz <= -7)) {
                        int hash = Math.abs(dx * 31 + dz * 17);
                        if (grassMeadowPatch && hash % 3 == 0) {
                            setRel(level, chunkBox, dx, 1, dz, shortGrass);
                        } else if (!grassMeadowPatch && hash % 5 == 0) {
                            setRel(level, chunkBox, dx, 1, dz, featherMoss);
                        }
                    }
                }
            }
        }
    }

    private void buildSubterraneanReliquaryVaultAndStairwell(
            WorldGenLevel level,
            BoundingBox chunkBox,
            RandomSource random
    ) {
        Block springStoneBlock = resolveBlock("vitae_spring_stone", Blocks.CALCITE);
        Block radiantGlassBlock = resolveBlock("radiant_glass", Blocks.YELLOW_STAINED_GLASS);
        Block sunLanternBlock = resolveBlock("sun_glazed_lantern", Blocks.LANTERN);

        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState quartzBricks = Blocks.QUARTZ_BRICKS.defaultBlockState();
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState radiantGlass = nonWaterlogged(radiantGlassBlock.defaultBlockState());
        BlockState floorLantern = lanternState(sunLanternBlock, false);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Subterranean Golden Reliquary Vault (x = -3..3, z = -3..3, y = -4..-1)
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                boolean isPerimeter = (Math.abs(dx) == 3 || Math.abs(dz) == 3);
                boolean isCorner = (Math.abs(dx) == 3 && Math.abs(dz) == 3);

                if (isCorner || (dx == 0 && dz == 0)) {
                    fillFoundationDown(level, chunkBox, dx, -5, dz, springStone);
                }

                // Floor at dy = -4
                BlockState floorTile = isPerimeter ? quartzBricks
                        : (((dx + dz) & 1) == 0 ? springStone : quartzBricks);
                setRel(level, chunkBox, dx, -4, dz, floorTile);

                // Walls & interior headroom (dy = -3..-2)
                for (int dy = -3; dy <= -2; dy++) {
                    if (isPerimeter) {
                        BlockState wallState = (isCorner || dy == -2) ? quartzBricks : springStone;
                        setRel(level, chunkBox, dx, dy, dz, wallState);
                    } else {
                        setRel(level, chunkBox, dx, dy, dz, air);
                    }
                }

                // Vault ceiling at dy = -1 with entropica:radiant_glass skylight at (0, -1, 0)
                if (dx == 0 && dz == 0) {
                    setRel(level, chunkBox, 0, -1, 0, radiantGlass);
                } else {
                    BlockState ceilingTile = (Math.abs(dx) + Math.abs(dz) == 1) ? smoothQuartz
                            : (isPerimeter ? quartzBricks : springStone);
                    setRel(level, chunkBox, dx, -1, dz, ceilingTile);
                }
            }
        }

        // Sun-Glazed Lantern at (0, -3, -2)
        setRel(level, chunkBox, 0, -3, -2, floorLantern);

        // 2 Loot Chests at (-2, -3, 0) and (2, -3, 0) pointing to entropica:chests/radiant_spring_pavilion
        placePavilionChest(level, chunkBox, random, -2, -3, 0, Direction.EAST);
        placePavilionChest(level, chunkBox, random, 2, -3, 0, Direction.WEST);

        // 2. Watertight Stepped Side Stairwell descending from the -Z Terrace (dx = -1, dz = -9..-3)
        //    Flanked by watertight cheek walls at dx = -2 and dx = 0 so fountain water never enters!
        fillFoundationDown(level, chunkBox, -1, -5, -4, springStone);
        for (int dz = -9; dz <= -4; dz++) {
            for (int sideX : new int[]{-2, 0}) {
                fillFoundationDown(level, chunkBox, sideX, -5, dz, springStone);
            }
        }

        // dz = -9: -Z terrace approach tile
        setRel(level, chunkBox, -1, 0, -9, smoothQuartz);
        setRel(level, chunkBox, -1, 1, -9, air);
        setRel(level, chunkBox, -1, 2, -9, air);

        // dz = -8: top step at dy = 0 on -Z terrace
        for (int sideX : new int[]{-2, 0}) {
            setRel(level, chunkBox, sideX, -1, -8, springStone);
            setRel(level, chunkBox, sideX, 0, -8, smoothQuartz);
            setRel(level, chunkBox, sideX, 1, -8, smoothQuartz);
        }
        setRel(level, chunkBox, -1, -1, -8, quartzBricks);
        setRel(level, chunkBox, -1, 0, -8, quartzStair(Direction.NORTH, Half.BOTTOM));
        setRel(level, chunkBox, -1, 1, -8, air);
        setRel(level, chunkBox, -1, 2, -8, air);
        setRel(level, chunkBox, -1, 3, -8, air);

        // dz = -7: step at dy = -1 passing beneath outer fountain rim portal
        for (int sideX : new int[]{-2, 0}) {
            for (int dy = -2; dy <= 2; dy++) {
                setRel(level, chunkBox, sideX, dy, -7, (dy >= 1) ? smoothQuartz : quartzBricks);
            }
        }
        setRel(level, chunkBox, -1, -2, -7, springStone);
        setRel(level, chunkBox, -1, -1, -7, quartzStair(Direction.NORTH, Half.BOTTOM));
        setRel(level, chunkBox, -1, 0, -7, air);
        setRel(level, chunkBox, -1, 1, -7, air);
        setRel(level, chunkBox, -1, 2, -7, smoothQuartz);

        // dz = -6: step at dy = -2 under watertight radiant_glass skylight hood
        for (int sideX : new int[]{-2, 0}) {
            for (int dy = -3; dy <= 1; dy++) {
                setRel(level, chunkBox, sideX, dy, -6, (dy == 1) ? smoothQuartz : quartzBricks);
            }
        }
        setRel(level, chunkBox, -1, -3, -6, springStone);
        setRel(level, chunkBox, -1, -2, -6, quartzStair(Direction.NORTH, Half.BOTTOM));
        setRel(level, chunkBox, -1, -1, -6, air);
        setRel(level, chunkBox, -1, 0, -6, air);
        setRel(level, chunkBox, -1, 1, -6, radiantGlass);

        // dz = -5: step at dy = -3 sealed under fountain shelf/floor
        for (int sideX : new int[]{-2, 0}) {
            for (int dy = -4; dy <= 1; dy++) {
                setRel(level, chunkBox, sideX, dy, -5, (dy >= 0) ? springStone : quartzBricks);
            }
        }
        setRel(level, chunkBox, -1, -4, -5, quartzBricks);
        setRel(level, chunkBox, -1, -3, -5, quartzStair(Direction.NORTH, Half.BOTTOM));
        setRel(level, chunkBox, -1, -2, -5, air);
        setRel(level, chunkBox, -1, -1, -5, air);
        setRel(level, chunkBox, -1, 0, -5, springStone);
        setRel(level, chunkBox, -1, 1, -5, smoothQuartz);

        // dz = -4: dry vestibule landing at dy = -4 beneath fountain pool floor
        for (int sideX : new int[]{-2, 0}) {
            for (int dy = -4; dy <= 0; dy++) {
                setRel(level, chunkBox, sideX, dy, -4, (dy == 0) ? springStone : quartzBricks);
            }
        }
        setRel(level, chunkBox, -1, -4, -4, quartzBricks);
        setRel(level, chunkBox, -1, -3, -4, air);
        setRel(level, chunkBox, -1, -2, -4, air);
        setRel(level, chunkBox, -1, -1, -4, quartzBricks);
        setRel(level, chunkBox, -1, 0, -4, springStone);

        // dz = -3: 2-high doorway through North Vault wall into the Golden Reliquary Vault
        setRel(level, chunkBox, -1, -3, -3, air);
        setRel(level, chunkBox, -1, -2, -3, air);
    }

    private void buildTieredVitaeSpringFountainBasin(WorldGenLevel level, BoundingBox chunkBox) {
        Block springStoneBlock = resolveBlock("vitae_spring_stone", Blocks.CALCITE);
        Block glazeIceBlock = resolveBlock("radiant_glaze_ice", Blocks.PACKED_ICE);
        Block radiantGlassBlock = resolveBlock("radiant_glass", Blocks.YELLOW_STAINED_GLASS);
        Block vitaeOrchidBlock = resolveBlock("vitae_orchid", Blocks.DANDELION);

        BlockState springStone = springStoneBlock.defaultBlockState();
        BlockState glazeIce = glazeIceBlock.defaultBlockState();
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState radiantGlass = nonWaterlogged(radiantGlassBlock.defaultBlockState());
        BlockState grassBlock = Blocks.GRASS_BLOCK.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();

        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                double r = Math.hypot(dx, dz);
                if (r > 7.8D) {
                    continue;
                }

                // Preserve the sealed -Z side stairwell vestibule (dx = -2..0, dz = -8..-5)
                if (dx >= -2 && dx <= 0 && dz <= -5) {
                    continue;
                }

                if (r >= 6.6D) {
                    // Outer rim curb (6.6 <= r <= 7.8) at y = 1: vitae_spring_stone & smooth_quartz
                    BlockState outerRim = (((dx + dz) & 1) == 0) ? smoothQuartz : springStone;
                    setRel(level, chunkBox, dx, 0, dz, springStone);
                    setRel(level, chunkBox, dx, 1, dz, outerRim);
                } else if (r >= 5.3D) {
                    // Inner shelf (5.3 <= r < 6.6) at y = 1: radiant_glaze_ice & smooth_quartz + shallow wading pockets
                    setRel(level, chunkBox, dx, 0, dz, springStone);
                    boolean wadingChannel = (Math.abs(dx) <= 1 || Math.abs(dz) <= 1) && !(dz < 0 && Math.abs(dx) <= 2);
                    BlockState innerShelf = wadingChannel ? water
                            : ((((dx * 3 + dz) & 1) == 0) ? glazeIce : smoothQuartz);
                    setRel(level, chunkBox, dx, 1, dz, innerShelf);
                } else {
                    // Warm Vitae Spring pool (r < 5.3) at y = 1 over vitae_spring_stone floor (radiant_glass skylight at 0,0,0)
                    setRel(level, chunkBox, dx, 0, dz, (dx == 0 && dz == 0) ? radiantGlass : springStone);
                    setRel(level, chunkBox, dx, 1, dz, water);
                }
            }
        }

        // Welcoming Smooth-Quartz steps on East, West, and South sides of the outer basin rim
        for (int s = -1; s <= 1; s++) {
            setRel(level, chunkBox, s, 1, 7, quartzStair(Direction.NORTH, Half.BOTTOM));
            setRel(level, chunkBox, -7, 1, s, quartzStair(Direction.EAST, Half.BOTTOM));
            setRel(level, chunkBox, 7, 1, s, quartzStair(Direction.WEST, Half.BOTTOM));
        }

        // 4 Corner Planters at (+-5, 1..2, +-5): grass_block at y = 1 + entropica:vitae_orchid at y = 2
        for (int[] planter : PLANTER_AND_LANTERN_COORDS) {
            int px = planter[0];
            int pz = planter[1];
            setRel(level, chunkBox, px, 0, pz, smoothQuartz);
            setRel(level, chunkBox, px, 1, pz, grassBlock);
            placeOrchidAt(level, chunkBox, px, 2, pz, vitaeOrchidBlock);
        }

        // Pure Smooth-Quartz Central Fountain Column at (0, 1..2, 0) topped by cascading Blocks.WATER at (0, 3, 0)
        setRel(level, chunkBox, 0, 1, 0, smoothQuartz);
        setRel(level, chunkBox, 0, 2, 0, smoothQuartz);
        setRel(level, chunkBox, 0, 3, 0, water);
        BlockPos topWaterWorld = toWorld(0, 3, 0);
        if (chunkBox.isInside(topWaterWorld)) {
            level.scheduleTick(topWaterWorld, Fluids.WATER, 2);
        }
    }

    private void buildColumnsAndRadiantGlassSkylightDome(WorldGenLevel level, BoundingBox chunkBox) {
        Block amberLogBlock = resolveBlock("amber_log", Blocks.OAK_LOG);
        Block amberPlanksBlock = resolveBlock("amber_planks", Blocks.OAK_PLANKS);
        Block glazedLeavesBlock = resolveBlock("glazed_amber_leaves", Blocks.OAK_LEAVES);
        Block goldenLichenBlock = resolveBlock("golden_lichen", Blocks.GLOW_LICHEN);
        Block sunLanternBlock = resolveBlock("sun_glazed_lantern", Blocks.LANTERN);
        Block radiantGlassBlock = resolveBlock("radiant_glass", Blocks.YELLOW_STAINED_GLASS);
        Block qilinRefractorBlock = resolveBlock("qilin_light_refractor", Blocks.SEA_LANTERN);

        BlockState verticalAmberLog = logWithAxis(amberLogBlock, Direction.Axis.Y);
        BlockState amberPlanks = amberPlanksBlock.defaultBlockState();
        BlockState glazedLeaves = makePersistentLeaves(glazedLeavesBlock);
        BlockState hangingSunLantern = lanternState(sunLanternBlock, true);
        BlockState radiantGlass = nonWaterlogged(radiantGlassBlock.defaultBlockState());
        BlockState smoothQuartz = Blocks.SMOOTH_QUARTZ.defaultBlockState();
        BlockState qilinRefractor = nonWaterlogged(qilinRefractorBlock.defaultBlockState());

        // 1. 4 Amber-Log & Smooth-Quartz Columns at (+-6, 1..7, +-6)
        for (int[] col : COLUMN_COORDS) {
            int cx = col[0];
            int cz = col[1];
            int outStepX = (cx < 0) ? -1 : 1;
            int outStepZ = (cz < 0) ? -1 : 1;
            int inStepX = -outStepX;
            int inStepZ = -outStepZ;

            // Smooth-quartz column plinth & outer buttress blocks at y = 0..1
            setRel(level, chunkBox, cx, 0, cz, smoothQuartz);
            setRel(level, chunkBox, cx + outStepX, 1, cz, smoothQuartz);
            setRel(level, chunkBox, cx, 1, cz + outStepZ, smoothQuartz);

            // Amber-Log pillar shaft (y = 1..7)
            for (int dy = 1; dy <= 7; dy++) {
                setRel(level, chunkBox, cx, dy, cz, verticalAmberLog);
            }

            // Cling entropica:golden_lichen onto outer X and outer Z faces of the amber_log shaft (y = 2..6)
            Direction attachFromOutX = (outStepX < 0) ? Direction.EAST : Direction.WEST;
            Direction attachFromOutZ = (outStepZ < 0) ? Direction.SOUTH : Direction.NORTH;
            BlockState lichenOutX = makeSingleFaceLichen(goldenLichenBlock, attachFromOutX);
            BlockState lichenOutZ = makeSingleFaceLichen(goldenLichenBlock, attachFromOutZ);
            for (int dy = 2; dy <= 6; dy++) {
                if (lichenOutX != null) {
                    setRelIfAir(level, chunkBox, cx + outStepX, dy, cz, lichenOutX);
                }
                if (lichenOutZ != null) {
                    setRelIfAir(level, chunkBox, cx, dy, cz + outStepZ, lichenOutZ);
                }
            }

            // Glazed Amber Leaves capitals at y = 8 atop (+-6, 8, +-6) + outer leafy eaves
            setRel(level, chunkBox, cx, 8, cz, glazedLeaves);
            setRel(level, chunkBox, cx + outStepX, 8, cz, glazedLeaves);
            setRel(level, chunkBox, cx, 8, cz + outStepZ, glazedLeaves);

            // Inward Amber-Planks corbel bracket at (+-5, 7, +-5) supporting Sun-Glazed Lantern at (+-5, 6, +-5)
            int lx = cx + inStepX;
            int lz = cz + inStepZ;
            setRel(level, chunkBox, lx, 7, lz, amberPlanks);
            setRel(level, chunkBox, lx, 6, lz, hangingSunLantern);
        }

        // 2. Stepped Smooth-Quartz, Amber-Planks, and Radiant-Glass Skylight Dome (y = 8..12)
        // Tier y = 8: Eaves ring & arch beam band (6.3 <= r <= 8.1, preserving (+-6, 8, +-6) leaf capitals)
        for (int dx = -8; dx <= 8; dx++) {
            for (int dz = -8; dz <= 8; dz++) {
                if (Math.abs(dx) >= 6 && Math.abs(dz) >= 6) {
                    continue; // Preserve glazed_amber_leaves column capitals
                }
                double r = Math.hypot(dx, dz);
                if (r >= 6.3D && r <= 8.1D) {
                    boolean isCardinalKeystone = (Math.abs(dx) <= 1 || Math.abs(dz) <= 1);
                    BlockState ringState = (r >= 7.2D || isCardinalKeystone) ? smoothQuartz : amberPlanks;
                    setRel(level, chunkBox, dx, 8, dz, ringState);
                }
            }
        }

        // Tier y = 9: Lower Radiant Glass Skylight & Rib Tier (5.0 <= r <= 6.6)
        for (int dx = -7; dx <= 7; dx++) {
            for (int dz = -7; dz <= 7; dz++) {
                double r = Math.hypot(dx, dz);
                if (r >= 5.0D && r <= 6.6D) {
                    boolean isRib = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                    BlockState domeState = isRib ? smoothQuartz : ((r > 6.1D) ? amberPlanks : radiantGlass);
                    setRel(level, chunkBox, dx, 9, dz, domeState);
                }
            }
        }

        // Tier y = 10: Mid Radiant Glass Skylight Dome Tier (3.6 <= r <= 5.2)
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -6; dz <= 6; dz++) {
                double r = Math.hypot(dx, dz);
                if (r >= 3.6D && r <= 5.2D) {
                    boolean isRib = (dx == 0 || dz == 0 || Math.abs(dx) == Math.abs(dz));
                    BlockState domeState = isRib ? smoothQuartz : ((r > 4.8D) ? amberPlanks : radiantGlass);
                    setRel(level, chunkBox, dx, 10, dz, domeState);
                }
            }
        }

        // Tier y = 11: Upper Radiant Glass Dome Tier (2.0 <= r <= 3.8)
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double r = Math.hypot(dx, dz);
                if (r >= 2.0D && r <= 3.8D) {
                    boolean isCardinalRib = (dx == 0 || dz == 0);
                    boolean isDiagTrim = (Math.abs(dx) == Math.abs(dz));
                    BlockState domeState = isCardinalRib ? smoothQuartz : (isDiagTrim ? amberPlanks : radiantGlass);
                    setRel(level, chunkBox, dx, 11, dz, domeState);
                }
            }
        }

        // Tier y = 12: Dome Crown Oculus Cap (r <= 2.3)
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                double r = Math.hypot(dx, dz);
                if (r <= 2.3D) {
                    boolean isOuterCardinal = (Math.abs(dx) == 2 && dz == 0) || (dx == 0 && Math.abs(dz) == 2);
                    BlockState capState = isOuterCardinal ? smoothQuartz : radiantGlass;
                    setRel(level, chunkBox, dx, 12, dz, capState);
                }
            }
        }

        // 3. Apex Pedestal & Qilin Light Refractor Finial at (0, 13..14, 0)
        setRel(level, chunkBox, 0, 13, 0, smoothQuartz);
        setRel(level, chunkBox, 0, 14, 0, qilinRefractor);
        BlockPos refractorWorld = toWorld(0, 14, 0);
        if (chunkBox.isInside(refractorWorld)) {
            level.scheduleTick(refractorWorld, qilinRefractorBlock, 2);
        }
    }

    private void placePavilionChest(
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
                level, random, pos, ModStructures.RADIANT_SPRING_PAVILION_CHEST_LOOT);
    }

    private void placeOrchidAt(
            WorldGenLevel level,
            BoundingBox chunkBox,
            int dx,
            int dy,
            int dz,
            Block orchidBlock
    ) {
        BlockState base = orchidBlock.defaultBlockState();
        if (orchidBlock instanceof DoublePlantBlock) {
            setRel(level, chunkBox, dx, dy, dz, base.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER));
            setRel(level, chunkBox, dx, dy + 1, dz, base.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER));
        } else {
            setRel(level, chunkBox, dx, dy, dz, base);
        }
    }

    private static BlockState makeSingleFaceLichen(Block lichenBlock, Direction attachFace) {
        BlockState state = lichenBlock.defaultBlockState();
        for (Direction dir : Direction.values()) {
            BooleanProperty prop = GlowLichenBlock.getFaceProperty(dir);
            if (state.hasProperty(prop)) {
                state = state.setValue(prop, false);
            }
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        BooleanProperty target = GlowLichenBlock.getFaceProperty(attachFace);
        if (!state.hasProperty(target)) {
            return null;
        }
        return state.setValue(target, true);
    }

    private static BlockState lanternState(Block block, boolean hanging) {
        BlockState state = nonWaterlogged(block.defaultBlockState());
        if (state.hasProperty(BlockStateProperties.HANGING)) {
            state = state.setValue(BlockStateProperties.HANGING, hanging);
        }
        return state;
    }

    private static BlockState nonWaterlogged(BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static BlockState quartzStair(Direction facing, Half half) {
        return Blocks.SMOOTH_QUARTZ_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
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

    private static Block resolveBlock(String path, Block fallback) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, path);
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }

    private BlockPos toWorld(int dx, int dy, int dz) {
        return new BlockPos(this.originX + dx, this.anchorY + dy, this.originZ + dz);
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
        int minAllowedY = Math.max(level.getMinY() + 2, this.anchorY + startDy - 24);
        for (int wy = this.anchorY + startDy; wy >= minAllowedY; wy--) {
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
