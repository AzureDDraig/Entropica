package ddraig.net.entropica.worldgen.structure;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.level.block.TripWireHookBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

/**
 * Programmatic generator for Biome #5 Structure: Eclipse Fjord Watch-Sanctum ({@code 15 x 17 x 16}):
 * <ul>
 *   <li>Cliff-anchored columnar {@code basalt_column} &amp; {@code basalt} foundation + forecourt terrace
 *       ({@code dz = +4..+8, dx = -6..+6}) of {@code smooth_basalt} &amp; {@code blue_frozen_moss},
 *       carpeted with {@code blue_frozen_moss_carpet} &amp; {@code shadowed_eclipse_moss}, welcomed by
 *       {@code deepslate_brick_stairs} at {@code dz = +8}, and flanked by twin 8-block hexagonal
 *       {@code basalt_column} watch-obelisks at {@code (-5, 0..7, +6)} and {@code (+5, 0..7, +6)} crowned
 *       with {@code chiseled_eclipse_basalt} &amp; {@code soul_lantern}.</li>
 *   <li>Vaulted Eclipse Nave ({@code dx = -5..+5, dz = -6..+3, dy = 0..+9}) with Deepslate Brick,
 *       Chiseled Deepslate, and {@code basalt_column} rib walls under a steep Nordic gabled roof
 *       ({@code deepslate_brick_stairs} + {@code chiseled_eclipse_basalt} ridge crest at {@code dy = 9}),
 *       rear glowing purple {@code chiseled_eclipse_basalt} rose oculus window ({@code dz = -6, dy = 4..6}),
 *       hanging {@code glacial_heart_root} from inner rafters, central Eclipse Ritual Pedestal at
 *       {@code (0, 1..2, -2)} ({@code chiseled_eclipse_basalt} + {@code amethyst_cluster}), and Nave
 *       Loot Chest at {@code (0, 1, -4)} facing South.</li>
 *   <li>Sunken Shadow Crypt ({@code dx = -4..+4, dz = -5..+2, dy = -5..-1}) reached via an interior
 *       West stairwell ({@code dx = -3, dz = +2..-1}) carpeted at the landing with
 *       {@code shadowed_eclipse_moss}, guarded at {@code dz = -2} by a {@code tripwire} +
 *       {@code tripwire_hook} line triggering hidden {@code dispenser} traps stocked with Splash Potions
 *       of Blindness &amp; Slowness, protecting the Crypt Reliquary Chest at {@code (0, -4, -4)} facing
 *       South and secondary {@code silver_pine_barrel} at {@code (2, -4, -4)}.</li>
 * </ul>
 */
public class EclipseFjordSanctumPiece extends StructurePiece {

    private static final int[] NAVE_RIB_Z = {-6, -3, 0, 3};

    private static final int[][] FORECOURT_BLUE_CARPETS = {
            {-3, 5}, {-2, 6}, {3, 5}, {2, 6}, {-4, 7}, {4, 7}
    };

    private static final int[][] FORECOURT_ECLIPSE_MOSS = {
            {-3, 7}, {3, 7}, {-2, 4}, {2, 4}
    };

    private static final int[][] RAFTER_HEART_ROOTS = {
            {-2, 6, -4}, {2, 6, -4},
            {-2, 6, -1}, {2, 6, -1},
            {0, 7, -3}, {0, 7, 0}
    };

    private final int originX;
    private final int baseY;
    private final int originZ;

    public EclipseFjordSanctumPiece(int originX, int baseY, int originZ) {
        // Exact 15 (W: -7..+7) x 16 (H: -5..+10) x 17 (L: -8..+8) bounding box
        super(resolvePieceType(), 0,
                new BoundingBox(originX - 7, baseY - 5, originZ - 8, originX + 7, baseY + 10, originZ + 8));
        this.originX = originX;
        this.baseY = baseY;
        this.originZ = originZ;
    }

    public EclipseFjordSanctumPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(resolvePieceType(), tag);
        this.originX = tag.getIntOr("OriginX", (this.boundingBox.minX() + this.boundingBox.maxX()) / 2);
        this.baseY = tag.getIntOr("BaseY", this.boundingBox.minY() + 5);
        this.originZ = tag.getIntOr("OriginZ", (this.boundingBox.minZ() + this.boundingBox.maxZ()) / 2);
    }

    private static StructurePieceType resolvePieceType() {
        if (ModStructures.ECLIPSE_FJORD_SANCTUM_PIECE.isPresent()) {
            return ModStructures.ECLIPSE_FJORD_SANCTUM_PIECE.get();
        }
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "eclipse_fjord_sanctum_piece");
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

    public BlockPos getPedestalClusterPos() {
        return new BlockPos(this.originX, this.baseY + 2, this.originZ - 2);
    }

    public BlockPos getNaveChestPos() {
        return new BlockPos(this.originX, this.baseY + 1, this.originZ - 4);
    }

    public BlockPos getCryptChestPos() {
        return new BlockPos(this.originX, this.baseY - 4, this.originZ - 4);
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
        buildCliffFoundationAndForecourt(level, chunkBox);
        buildSunkenShadowCrypt(level, chunkBox, random);
        buildVaultedEclipseNaveAndGabledRoof(level, chunkBox, random);
        buildInteriorCryptStairwell(level, chunkBox);
    }

    private void buildCliffFoundationAndForecourt(WorldGenLevel level, BoundingBox chunkBox) {
        Block basaltColumnBlock = resolveBlock("basalt_column", Blocks.BASALT);
        Block chiseledEclipseBlock = resolveBlock("chiseled_eclipse_basalt", Blocks.CHISELED_DEEPSLATE);
        Block blueFrozenMossBlock = resolveBlock("blue_frozen_moss", Blocks.MOSS_BLOCK);
        Block blueMossCarpetBlock = resolveBlock("blue_frozen_moss_carpet", Blocks.MOSS_CARPET);
        Block eclipseMossBlock = resolveBlock("shadowed_eclipse_moss", Blocks.MOSS_CARPET);

        BlockState basaltColumnY = columnY(basaltColumnBlock);
        BlockState basaltY = Blocks.BASALT.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState smoothBasalt = Blocks.SMOOTH_BASALT.defaultBlockState();
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState chiseledEclipse = chiseledEclipseBlock.defaultBlockState();
        BlockState blueFrozenMoss = blueFrozenMossBlock.defaultBlockState();
        BlockState blueCarpet = blueMossCarpetBlock.defaultBlockState();
        BlockState eclipseMoss = eclipseMossBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Solid Downward Basalt Column & Basalt Cliff Anchoring across entire Sanctum + Forecourt footprint
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -7; dz <= 8; dz++) {
                // Trim extreme outer corners of the forecourt for a faceted hexagonal cliff terrace silhouette
                if (dz >= 7 && Math.abs(dx) == 6) {
                    continue;
                }
                boolean inCryptFootprint = (Math.abs(dx) <= 4 && dz >= -5 && dz <= 2);
                int startDy = inCryptFootprint ? -6 : -1;
                boolean isOuterEdge = (Math.abs(dx) >= 5 || dz <= -6 || dz >= 7 || ((dx + dz) & 1) == 0);
                BlockState pillarState = isOuterEdge ? basaltColumnY : basaltY;

                if (!inCryptFootprint && dz >= -6 && dz <= 8) {
                    setBlock(level, chunkBox, dx, -1, dz, pillarState);
                }
                fillFoundationDown(level, chunkBox, dx, startDy, dz, pillarState);
            }
        }

        // 2. Forecourt Terrace Floor & Headroom (dz = +4..+8, dx = -6..+6)
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = 4; dz <= 8; dz++) {
                if (dz >= 7 && Math.abs(dx) == 6) {
                    continue;
                }
                for (int dy = 1; dy <= 10; dy++) {
                    setBlock(level, chunkBox, dx, dy, dz, air);
                }

                if (dz == 8 && Math.abs(dx) <= 2) {
                    // Welcoming Deepslate Brick Stairs at dz = +8
                    setBlock(level, chunkBox, dx, 0, 8, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                            .setValue(StairBlock.FACING, Direction.NORTH)
                            .setValue(StairBlock.HALF, Half.BOTTOM)
                            .setValue(StairBlock.WATERLOGGED, false));
                } else if (Math.abs(dx) <= 1) {
                    // Central processional walkway to the Nave portal
                    setBlock(level, chunkBox, dx, 0, dz, (dx == 0 && dz == 6) ? chiseledDeepslate : deepslateBricks);
                } else {
                    // Smooth Basalt & Blue Frozen Moss cliff terrace
                    boolean mossTile = ((Math.abs(dx) + dz) % 3 != 0) && Math.abs(dx) <= 4;
                    setBlock(level, chunkBox, dx, 0, dz, mossTile ? blueFrozenMoss : smoothBasalt);
                }
            }
        }

        // 3. Two 8-Block Hexagonal Basalt Column Watch-Obelisks at (-5, 0..7, +6) and (+5, 0..7, +6)
        for (int obeliskX : new int[]{-5, 5}) {
            // Hexagonal-style clustered basal spurs around the obelisk foot (dy = 0..2)
            setBlock(level, chunkBox, obeliskX - 1, 0, 6, basaltColumnY);
            setBlock(level, chunkBox, obeliskX - 1, 1, 6, basaltColumnY);
            setBlock(level, chunkBox, obeliskX + 1, 0, 6, basaltColumnY);
            setBlock(level, chunkBox, obeliskX + 1, 1, 6, basaltColumnY);
            setBlock(level, chunkBox, obeliskX, 0, 5, basaltColumnY);
            setBlock(level, chunkBox, obeliskX, 1, 5, basaltColumnY);
            setBlock(level, chunkBox, obeliskX, 0, 7, basaltColumnY);
            setBlock(level, chunkBox, obeliskX, 1, 7, basaltColumnY);

            // 8-block obelisk column (dy = 0..6 basalt_column + dy = 7 chiseled_eclipse_basalt crown)
            for (int dy = 0; dy <= 6; dy++) {
                setBlock(level, chunkBox, obeliskX, dy, 6, basaltColumnY);
            }
            setBlock(level, chunkBox, obeliskX, 7, 6, chiseledEclipse);
            setBlock(level, chunkBox, obeliskX, 8, 6, Blocks.SOUL_LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, false)
                    .setValue(LanternBlock.WATERLOGGED, false));

            // Inward lantern bracket overlooking the forecourt walkway
            int inwardX = obeliskX - Integer.signum(obeliskX); // -4 and +4
            setBlock(level, chunkBox, inwardX, 6, 6, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, obeliskX < 0 ? Direction.WEST : Direction.EAST)
                    .setValue(StairBlock.HALF, Half.TOP)
                    .setValue(StairBlock.WATERLOGGED, false));
            setBlock(level, chunkBox, inwardX, 5, 6, Blocks.SOUL_LANTERN.defaultBlockState()
                    .setValue(LanternBlock.HANGING, true)
                    .setValue(LanternBlock.WATERLOGGED, false));
        }

        // 4. Forecourt Blue Frozen Moss Carpets & Shadowed Eclipse-Moss patches
        for (int[] pos : FORECOURT_BLUE_CARPETS) {
            placeCarpetIfSurvives(level, chunkBox, pos[0], 1, pos[1], blueCarpet);
        }
        for (int[] pos : FORECOURT_ECLIPSE_MOSS) {
            placeCarpetIfSurvives(level, chunkBox, pos[0], 1, pos[1], eclipseMoss);
        }
    }

    private void buildSunkenShadowCrypt(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block basaltColumnBlock = resolveBlock("basalt_column", Blocks.BASALT);
        Block chiseledEclipseBlock = resolveBlock("chiseled_eclipse_basalt", Blocks.CHISELED_DEEPSLATE);
        Block silverPineBarrelBlock = resolveBlock("silver_pine_barrel", Blocks.BARREL);

        BlockState basaltColumnY = columnY(basaltColumnBlock);
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState crackedBricks = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState smoothBasalt = Blocks.SMOOTH_BASALT.defaultBlockState();
        BlockState chiseledEclipse = chiseledEclipseBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Crypt Masonry Shell & Floor (dx = -4..+4, dz = -5..+2, dy = -5..-1)
        for (int dy = -5; dy <= -1; dy++) {
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -5; dz <= 2; dz++) {
                    boolean isPerimeterWall = (Math.abs(dx) == 4 || dz == -5 || dz == 2);
                    if (dy == -5) {
                        BlockState floorTile = (dx == 0 && dz >= -4 && dz <= 0) ? chiseledDeepslate
                                : ((((dx + dz) & 1) == 0) ? smoothBasalt : deepslateBricks);
                        setBlock(level, chunkBox, dx, -5, dz, floorTile);
                    } else if (isPerimeterWall) {
                        boolean isRib = (Math.abs(dx) == 4 && (dz == -5 || dz == -2 || dz == 2))
                                || (dz == -5 && Math.abs(dx) == 2);
                        if (dz == -5 && dx == 0 && (dy == -4 || dy == -3)) {
                            setBlock(level, chunkBox, dx, dy, dz, chiseledEclipse);
                        } else if (isRib) {
                            setBlock(level, chunkBox, dx, dy, dz, basaltColumnY);
                        } else {
                            setBlock(level, chunkBox, dx, dy, dz, ((dx + dy + dz) & 3) == 0 ? crackedBricks : deepslateBricks);
                        }
                    } else {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                }
            }
        }

        // Dim Soul Lantern behind the reliquary chest
        setBlock(level, chunkBox, 0, -2, -4, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));

        // 2. Tripwire + Tripwire Hook Line across dz = -2 triggering Hidden Dispensers (Blindness & Slowness)
        setBlock(level, chunkBox, -4, -4, -2, chiseledDeepslate);
        setBlock(level, chunkBox, 4, -4, -2, chiseledDeepslate);

        setBlock(level, chunkBox, -3, -4, -2, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.EAST)
                .setValue(TripWireHookBlock.ATTACHED, true));
        setBlock(level, chunkBox, 3, -4, -2, Blocks.TRIPWIRE_HOOK.defaultBlockState()
                .setValue(TripWireHookBlock.FACING, Direction.WEST)
                .setValue(TripWireHookBlock.ATTACHED, true));

        BlockState connectedWire = Blocks.TRIPWIRE.defaultBlockState()
                .setValue(TripWireBlock.ATTACHED, true)
                .setValue(TripWireBlock.EAST, true)
                .setValue(TripWireBlock.WEST, true);
        for (int dx = -2; dx <= 2; dx++) {
            setBlock(level, chunkBox, dx, -4, -2, connectedWire);
        }

        // Hidden wall dispensers directly above the hook-powered anchor blocks (-4, -3, -2) and (+4, -3, -2)
        placeAndStockCryptDispenser(level, chunkBox, -4, -3, -2, Direction.EAST);
        setBlock(level, chunkBox, -3, -3, -2, air);
        placeAndStockCryptDispenser(level, chunkBox, 4, -3, -2, Direction.WEST);
        setBlock(level, chunkBox, 3, -3, -2, air);

        // 3. Crypt Reliquary Chest at (0, -4, -4) facing SOUTH & Secondary Silver-Pine Barrel at (2, -4, -4)
        placeLootChest(level, chunkBox, random, 0, -4, -4, Direction.SOUTH);
        placeLootBarrel(level, chunkBox, random, silverPineBarrelBlock, 2, -4, -4, Direction.UP);
    }

    private void buildVaultedEclipseNaveAndGabledRoof(WorldGenLevel level, BoundingBox chunkBox, RandomSource random) {
        Block basaltColumnBlock = resolveBlock("basalt_column", Blocks.BASALT);
        Block chiseledEclipseBlock = resolveBlock("chiseled_eclipse_basalt", Blocks.CHISELED_DEEPSLATE);
        Block heartRootBlock = resolveBlock("glacial_heart_root", Blocks.HANGING_ROOTS);

        BlockState basaltColumnY = columnY(basaltColumnBlock);
        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState crackedBricks = Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState();
        BlockState chiseledDeepslate = Blocks.CHISELED_DEEPSLATE.defaultBlockState();
        BlockState smoothBasalt = Blocks.SMOOTH_BASALT.defaultBlockState();
        BlockState chiseledEclipse = chiseledEclipseBlock.defaultBlockState();
        BlockState heartRoot = heartRootBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Clear Nave Volume & Lay Nave Floor at dy = 0 (dx = -5..+5, dz = -6..+3)
        for (int dx = -6; dx <= 6; dx++) {
            for (int dz = -7; dz <= 4; dz++) {
                if (dz <= 3) {
                    for (int dy = 1; dy <= 10; dy++) {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                    }
                }
                if (Math.abs(dx) <= 5 && dz >= -6 && dz <= 3) {
                    BlockState floorState;
                    if (dx == 0 && dz >= -5 && dz <= 2) {
                        floorState = chiseledDeepslate;
                    } else if (Math.abs(dx) == 1 && Math.abs(dz + 2) <= 1) {
                        floorState = chiseledEclipse;
                    } else if (Math.abs(dx) >= 4) {
                        floorState = smoothBasalt;
                    } else {
                        floorState = deepslateBricks;
                    }
                    setBlock(level, chunkBox, dx, 0, dz, floorState);
                }
            }
        }

        // 2. Nave Masonry & Basalt Column Rib Walls (dx = -5..+5, dz = -6..+3, dy = 1..5)
        for (int dy = 1; dy <= 5; dy++) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -6; dz <= 3; dz++) {
                    boolean isSideWall = (Math.abs(dx) == 5);
                    boolean isNorthWall = (dz == -6);
                    boolean isSouthWall = (dz == 3);
                    if (!isSideWall && !isNorthWall && !isSouthWall) {
                        continue;
                    }

                    // Grand South Portal Archway at dz = +3 (dx = -1..+1, dy = 1..4)
                    if (isSouthWall && Math.abs(dx) <= 1) {
                        if (dy <= (dx == 0 ? 4 : 3)) {
                            setBlock(level, chunkBox, dx, dy, dz, air);
                        } else if (dx == 0 && dy == 5) {
                            setBlock(level, chunkBox, dx, dy, dz, chiseledEclipse);
                        } else {
                            setBlock(level, chunkBox, dx, dy, dz, chiseledDeepslate);
                        }
                        continue;
                    }

                    // Portal flanking Basalt Column jambs
                    if (isSouthWall && Math.abs(dx) == 2 && dy <= 4) {
                        setBlock(level, chunkBox, dx, dy, dz, basaltColumnY);
                        continue;
                    }

                    // Narrow lancet side windows along dx = +/-5
                    if (isSideWall && (dz == -4 || dz == -1 || dz == 1) && (dy == 3 || dy == 4)) {
                        setBlock(level, chunkBox, dx, dy, dz, air);
                        continue;
                    }

                    if (isSideWall && isRibZ(dz)) {
                        setBlock(level, chunkBox, dx, dy, dz, basaltColumnY);
                    } else if (dy == 5) {
                        setBlock(level, chunkBox, dx, dy, dz, chiseledDeepslate);
                    } else {
                        setBlock(level, chunkBox, dx, dy, dz, ((dx + dy + dz) & 3) == 0 ? crackedBricks : deepslateBricks);
                    }
                }
            }
        }

        // 3. Steep Nordic Gabled Roof (dz = -7..+4, rising dy = 5 at eaves dx = +/-6 to dy = 9 ridge crest)
        for (int dz = -7; dz <= 4; dz++) {
            boolean isGableWall = (dz == -6 || dz == 3);

            // Eaves overhang at dx = +/-6, dy = 5
            setBlock(level, chunkBox, -6, 5, dz, roofSlopeStair(Direction.EAST, Half.BOTTOM));
            setBlock(level, chunkBox, 6, 5, dz, roofSlopeStair(Direction.WEST, Half.BOTTOM));

            // Stepped steep gable slopes: (dx=+/-4, dy=6), (dx=+/-3, dy=7), (dx=+/-2, dy=8), (dx=+/-1, dy=9)
            int[][] slopeSteps = {{4, 6}, {3, 7}, {2, 8}, {1, 9}};
            for (int[] step : slopeSteps) {
                int sx = step[0];
                int sy = step[1];
                setBlock(level, chunkBox, -sx, sy, dz, roofSlopeStair(Direction.EAST, Half.BOTTOM));
                setBlock(level, chunkBox, sx, sy, dz, roofSlopeStair(Direction.WEST, Half.BOTTOM));

                // Solid inner rafter backing directly underneath each roof slope step
                if (dz >= -6 && dz <= 3) {
                    BlockState backing = isRibZ(dz) ? basaltColumnY : deepslateBricks;
                    setBlock(level, chunkBox, -sx, sy - 1, dz, backing);
                    setBlock(level, chunkBox, sx, sy - 1, dz, backing);
                }
            }

            // Ridge crest at (0, 9, dz) crowned with glowing Chiseled Eclipse Basalt (+ solid ridge beam at dy = 8)
            setBlock(level, chunkBox, 0, 9, dz, chiseledEclipse);
            if (dz >= -6 && dz <= 3) {
                setBlock(level, chunkBox, 0, 8, dz, deepslateBricks);
            }

            // Fill North & South gable triangles
            if (isGableWall) {
                for (int dy = 6; dy <= 8; dy++) {
                    int maxFillX = 4 - (dy - 5); // dy=6 -> 3, dy=7 -> 2, dy=8 -> 1
                    for (int dx = -maxFillX; dx <= maxFillX; dx++) {
                        setBlock(level, chunkBox, dx, dy, dz, (dx == 0) ? chiseledDeepslate : deepslateBricks);
                    }
                }
            }
        }

        // 4. Rear Glowing Purple Chiseled Eclipse Basalt Rose / Oculus Window (dz = -6, dx = -1..+1, dy = 4..6)
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 4; dy <= 6; dy++) {
                boolean isCenterOculus = (dx == 0 && dy == 5);
                if (isCenterOculus) {
                    setBlock(level, chunkBox, 0, 5, -6, Blocks.PURPLE_STAINED_GLASS_PANE.defaultBlockState());
                } else {
                    setBlock(level, chunkBox, dx, dy, -6, chiseledEclipse);
                }
            }
        }

        // 5. Hanging Glacial Heart-Roots from the solid inner roof rafters
        for (int[] hr : RAFTER_HEART_ROOTS) {
            // Ensure sturdy rafter block directly above each hanging root
            setBlock(level, chunkBox, hr[0], hr[1] + 1, hr[2], deepslateBricks);
            placeHeartRootIfSurvives(level, chunkBox, hr[0], hr[1], hr[2], heartRoot);
        }

        // Atmospheric Soul Lanterns flanking the Nave sanctuary
        setBlock(level, chunkBox, -3, 5, -4, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));
        setBlock(level, chunkBox, 3, 5, -4, Blocks.SOUL_LANTERN.defaultBlockState()
                .setValue(LanternBlock.HANGING, true)
                .setValue(LanternBlock.WATERLOGGED, false));

        // 6. Central Eclipse Ritual Pedestal at (0, 1..2, -2) & Nave Loot Chest at (0, 1, -4) facing SOUTH
        setBlock(level, chunkBox, 0, 1, -2, chiseledEclipse);
        setBlock(level, chunkBox, 0, 2, -2, Blocks.AMETHYST_CLUSTER.defaultBlockState()
                .setValue(AmethystClusterBlock.FACING, Direction.UP)
                .setValue(AmethystClusterBlock.WATERLOGGED, false));

        placeLootChest(level, chunkBox, random, 0, 1, -4, Direction.SOUTH);
    }

    private void buildInteriorCryptStairwell(WorldGenLevel level, BoundingBox chunkBox) {
        Block eclipseMossBlock = resolveBlock("shadowed_eclipse_moss", Blocks.MOSS_CARPET);

        BlockState deepslateBricks = Blocks.DEEPSLATE_BRICKS.defaultBlockState();
        BlockState smoothBasalt = Blocks.SMOOTH_BASALT.defaultBlockState();
        BlockState eclipseMoss = eclipseMossBlock.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();

        // Interior West Stairwell descending along dx = -3, dz = +2..-1 from Nave (dy = 0) to Crypt (dy = -4)
        // Step 1 at (-3, 0, +2) entered from (-2, 1, +2)
        setBlock(level, chunkBox, -3, -1, 2, deepslateBricks);
        setBlock(level, chunkBox, -3, 0, 2, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.EAST)
                .setValue(StairBlock.HALF, Half.BOTTOM)
                .setValue(StairBlock.WATERLOGGED, false));
        for (int h = 1; h <= 3; h++) {
            setBlock(level, chunkBox, -3, h, 2, air);
        }

        // Steps 2..4 descending North along dx = -3: dz = +1 (dy = -1), dz = 0 (dy = -2), dz = -1 (dy = -3)
        int[][] northSteps = {{1, -1}, {0, -2}, {-1, -3}};
        for (int[] step : northSteps) {
            int sz = step[0];
            int sy = step[1];
            setBlock(level, chunkBox, -3, sy - 1, sz, deepslateBricks);
            setBlock(level, chunkBox, -3, sy, sz, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                    .setValue(StairBlock.FACING, Direction.SOUTH)
                    .setValue(StairBlock.HALF, Half.BOTTOM)
                    .setValue(StairBlock.WATERLOGGED, false));
            for (int h = 1; h <= 3; h++) {
                setBlock(level, chunkBox, -3, sy + h, sz, air);
            }
        }

        // Safety balustrade wall along the open stairwell edge inside the Nave (dx = -2, dy = 1, dz = -1..+1)
        for (int dz = -1; dz <= 1; dz++) {
            setBlock(level, chunkBox, -2, 1, dz, Blocks.DEEPSLATE_BRICK_WALL.defaultBlockState());
        }

        // Final bottom step turning East at (-2, -4, -1) onto the Shadowed Eclipse-Moss Crypt Landing!
        setBlock(level, chunkBox, -2, -5, -1, smoothBasalt);
        setBlock(level, chunkBox, -2, -4, -1, Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, Direction.WEST)
                .setValue(StairBlock.HALF, Half.BOTTOM)
                .setValue(StairBlock.WATERLOGGED, false));
        setBlock(level, chunkBox, -2, -3, -1, air);
        setBlock(level, chunkBox, -2, -2, -1, air);
        setBlock(level, chunkBox, -2, -1, -1, air);

        // Carpet the Crypt Landing (dx = -1..+1, dz = -1..0, dy = -4) with Shadowed Eclipse-Moss
        int[][] landingMossCoords = {{-1, -1}, {0, -1}, {1, -1}, {-1, 0}, {0, 0}};
        for (int[] lm : landingMossCoords) {
            setBlock(level, chunkBox, lm[0], -5, lm[1], smoothBasalt);
            placeCarpetIfSurvives(level, chunkBox, lm[0], -4, lm[1], eclipseMoss);
        }
    }

    private static boolean isRibZ(int dz) {
        for (int rz : NAVE_RIB_Z) {
            if (dz == rz) {
                return true;
            }
        }
        return false;
    }

    private static BlockState columnY(Block pillarBlock) {
        BlockState state = pillarBlock.defaultBlockState();
        if (state.hasProperty(RotatedPillarBlock.AXIS)) {
            return state.setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        }
        return state;
    }

    private static BlockState roofSlopeStair(Direction facing, Half half) {
        return Blocks.DEEPSLATE_BRICK_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, facing)
                .setValue(StairBlock.HALF, half)
                .setValue(StairBlock.WATERLOGGED, false);
    }

    private void placeCarpetIfSurvives(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState carpetState) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir() && carpetState.canSurvive(level, pos)) {
            level.setBlock(pos, carpetState, 2);
        }
    }

    private void placeHeartRootIfSurvives(WorldGenLevel level, BoundingBox chunkBox, int dx, int dy, int dz, BlockState rootState) {
        BlockPos pos = new BlockPos(this.originX + dx, this.baseY + dy, this.originZ + dz);
        if (chunkBox.isInside(pos) && level.getBlockState(pos).isAir() && rootState.canSurvive(level, pos)) {
            level.setBlock(pos, rootState, 2);
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
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.ECLIPSE_FJORD_SANCTUM_CHEST_LOOT);
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
        RandomizableContainer.setBlockEntityLootTable(level, random, pos, ModStructures.ECLIPSE_FJORD_SANCTUM_CHEST_LOOT);
    }

    private void placeAndStockCryptDispenser(
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
                if (slot % 2 == 0) {
                    ItemStack splashBlindness = new ItemStack(Items.SPLASH_POTION);
                    splashBlindness.set(DataComponents.POTION_CONTENTS,
                            PotionContents.EMPTY.withEffectAdded(new MobEffectInstance(MobEffects.BLINDNESS, 180, 0)));
                    dispenser.setItem(slot, splashBlindness);
                } else {
                    ItemStack splashSlowness = PotionContents.createItemStack(Items.SPLASH_POTION, Potions.SLOWNESS);
                    dispenser.setItem(slot, splashSlowness);
                }
            }
        }
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
        int minAllowedY = Math.max(level.getMinY() + 2, this.baseY + startDy - 26);
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
