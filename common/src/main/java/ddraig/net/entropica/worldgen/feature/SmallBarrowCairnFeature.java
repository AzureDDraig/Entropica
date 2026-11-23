package ddraig.net.entropica.worldgen.feature;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.Entropica;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Generates weathered surface burial cairns / mini-barrows ({@code 5x5} to {@code 7x7} low dome/arch)
 * in {@code 2.8 Barrow-Mound Tundra}, built from {@code barrow_stone_bricks}, {@code cracked_barrow_bricks},
 * and {@code mossy_barrow_bricks}, capped with {@code necrotic_soil}, {@code snow}, and {@code grave_moss},
 * housing {@code 1} {@code rime_cap_mushroom} or {@code crypt_grave_root} inside and a {@code 25%} chance of
 * a weathered {@code crypt_siphon_lantern} / {@code soul_lantern} / {@code decorated_pot} / {@code bone_block} marker.
 */
public class SmallBarrowCairnFeature extends Feature<NoneFeatureConfiguration> {

    private static final int MIN_DRY_SURFACE_Y = 62;

    private static final ResourceLocation BARROW_STONE_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_stone_bricks");
    private static final ResourceLocation CRACKED_BARROW_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "cracked_barrow_bricks");
    private static final ResourceLocation MOSSY_BARROW_BRICKS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "mossy_barrow_bricks");
    private static final ResourceLocation NECROTIC_SOIL_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "necrotic_soil");
    private static final ResourceLocation BARROW_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "barrow_moss");
    private static final ResourceLocation GRAVE_MOSS_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "grave_moss");
    private static final ResourceLocation RIME_CAP_MUSHROOM_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "rime_cap_mushroom");
    private static final ResourceLocation CRYPT_GRAVE_ROOT_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crypt_grave_root");
    private static final ResourceLocation CRYPT_SIPHON_LANTERN_ID =
            ResourceLocation.fromNamespaceAndPath(Entropica.MODID, "crypt_siphon_lantern");

    public SmallBarrowCairnFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();
        RandomSource random = context.random();

        Block barrowBricksBlock = resolveBlock(BARROW_STONE_BRICKS_ID, Blocks.DEEPSLATE_BRICKS);
        Block crackedBarrowBlock = resolveBlock(CRACKED_BARROW_BRICKS_ID, Blocks.CRACKED_DEEPSLATE_BRICKS);
        Block mossyBarrowBlock = resolveBlock(MOSSY_BARROW_BRICKS_ID, Blocks.MOSSY_STONE_BRICKS);
        Block necroticSoilBlock = resolveBlock(NECROTIC_SOIL_ID, Blocks.PODZOL);
        Block barrowMossBlock = resolveBlock(BARROW_MOSS_ID, Blocks.MOSS_BLOCK);
        Block graveMossBlock = resolveBlock(GRAVE_MOSS_ID, Blocks.MOSS_CARPET);
        Block rimeCapBlock = resolveBlock(RIME_CAP_MUSHROOM_ID, Blocks.BROWN_MUSHROOM);
        Block graveRootBlock = resolveBlock(CRYPT_GRAVE_ROOT_ID, Blocks.DEAD_BUSH);
        Block siphonLanternBlock = resolveBlock(CRYPT_SIPHON_LANTERN_ID, Blocks.SOUL_LANTERN);

        BlockPos groundCenter = findSolidTundraSurface(level, origin.getX(), origin.getZ(), origin.getY(),
                necroticSoilBlock, barrowMossBlock);
        if (groundCenter == null) {
            return false;
        }

        int cx = groundCenter.getX();
        int groundY = groundCenter.getY();
        int cz = groundCenter.getZ();

        if (groundY < MIN_DRY_SURFACE_Y || groundY + 7 >= level.getMaxY() - 1) {
            return false;
        }

        // Choose 5x5 (halfR = 2) or 7x7 / 5x7 (halfX, halfZ in 2..3) mini-barrow footprint
        Direction frontDir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        int halfAlong = random.nextFloat() < 0.45F ? 3 : 2;
        int halfAcross = (halfAlong == 3 && random.nextBoolean()) ? 3 : 2;
        int halfX = (frontDir.getAxis() == Direction.Axis.X) ? halfAlong : halfAcross;
        int halfZ = (frontDir.getAxis() == Direction.Axis.Z) ? halfAlong : halfAcross;

        // Verify entire footprint sits on solid dry land (no water, lava, air overhangs, or structure bricks)
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dx = -halfX - 1; dx <= halfX + 1; dx++) {
            for (int dz = -halfZ - 1; dz <= halfZ + 1; dz++) {
                for (int dy = -2; dy <= 4; dy++) {
                    cursor.set(cx + dx, groundY + dy, cz + dz);
                    BlockState st = level.getBlockState(cursor);
                    if (st.is(Blocks.WATER) || st.is(Blocks.LAVA) || st.is(Blocks.BUBBLE_COLUMN)
                            || isStructureMasonry(st, barrowBricksBlock, crackedBarrowBlock, mossyBarrowBlock)) {
                        return false;
                    }
                }
            }
        }

        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                if (!hasSolidFootingWithin(level, cx + dx, groundY, cz + dz, necroticSoilBlock, barrowMossBlock)) {
                    return false;
                }
                for (int dy = 1; dy <= 4; dy++) {
                    cursor.set(cx + dx, groundY + dy, cz + dz);
                    if (!isReplaceableAboveGround(level.getBlockState(cursor))) {
                        return false;
                    }
                }
            }
        }

        BlockState necroticSoil = necroticSoilBlock.defaultBlockState();
        BlockState barrowMoss = barrowMossBlock.defaultBlockState();
        BlockState graveMoss = graveMossBlock.defaultBlockState();
        BlockState coarseDirt = Blocks.COARSE_DIRT.defaultBlockState();
        BlockState polishedBasaltY = Blocks.POLISHED_BASALT.defaultBlockState()
                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
        BlockState air = Blocks.AIR.defaultBlockState();

        // 1. Anchor foundation downward and build elliptical low stone arch + necrotic soil turf dome
        for (int dx = -halfX; dx <= halfX; dx++) {
            for (int dz = -halfZ; dz <= halfZ; dz++) {
                double normR = Math.hypot(dx / (halfX + 0.45D), dz / (halfZ + 0.45D));
                if (normR > 1.02D) {
                    continue;
                }

                int wx = cx + dx;
                int wz = cz + dz;

                // Extend solid footing downward into sloped snow/soil so no corner ever floats
                fillDownToSolid(level, wx, groundY - 1, wz, normR < 0.72D
                        ? pickBarrowMasonry(random, barrowBricksBlock, crackedBarrowBlock, mossyBarrowBlock)
                        : coarseDirt);

                // Clear snow/plants above footprint
                for (int dy = 1; dy <= 4; dy++) {
                    level.setBlock(new BlockPos(wx, groundY + dy, wz), air, 2);
                }

                boolean isCenterChamber = (Math.abs(dx) <= 1 && Math.abs(dz) <= 1)
                        && (Math.abs(dx) == 0 || Math.abs(dz) == 0);
                boolean isFrontMouth = (dx == frontDir.getStepX() * halfX && dz == frontDir.getStepZ() * halfZ)
                        || (dx == frontDir.getStepX() * (halfX - 1) && dz == frontDir.getStepZ() * (halfZ - 1));

                // Base floor at groundY
                if (dx == 0 && dz == 0) {
                    level.setBlock(new BlockPos(wx, groundY, wz), necroticSoil, 2);
                } else if (normR <= 0.78D) {
                    level.setBlock(new BlockPos(wx, groundY, wz),
                            pickBarrowMasonry(random, barrowBricksBlock, crackedBarrowBlock, mossyBarrowBlock), 2);
                } else {
                    level.setBlock(new BlockPos(wx, groundY, wz),
                            random.nextFloat() < 0.30F ? barrowMoss : necroticSoil, 2);
                }

                // Mound height profile: 3 blocks above groundY at crown, tapering to 1 at skirt
                int domeTopDy = (normR <= 0.42D) ? 3 : ((normR <= 0.78D) ? 2 : 1);

                for (int dy = 1; dy <= domeTopDy; dy++) {
                    BlockPos placePos = new BlockPos(wx, groundY + dy, wz);
                    if ((dx == 0 && dz == 0 && dy <= 2) || (isFrontMouth && dy <= 2) || (isCenterChamber && dy == 1)) {
                        // Hollow interior burial niche and weathered portal tunnel
                        level.setBlock(placePos, air, 2);
                    } else if (dy < domeTopDy || normR <= 0.58D && dy == 2) {
                        // Inner barrow stone masonry arch
                        level.setBlock(placePos,
                                pickBarrowMasonry(random, barrowBricksBlock, crackedBarrowBlock, mossyBarrowBlock), 2);
                    } else {
                        // Outer turf mantle of necrotic_soil and barrow_moss
                        BlockState turf = (random.nextFloat() < 0.22F) ? barrowMoss : necroticSoil;
                        level.setBlock(placePos, turf, 2);
                    }
                }

                // Cap exposed mound top with snow layers or grave_moss
                BlockPos topBlockPos = new BlockPos(wx, groundY + domeTopDy, wz);
                if (!level.getBlockState(topBlockPos).isAir()) {
                    BlockPos capPos = topBlockPos.above();
                    if (random.nextFloat() < 0.38F && graveMoss.canSurvive(level, capPos)) {
                        level.setBlock(capPos, graveMoss, 2);
                    } else if (random.nextFloat() < 0.65F) {
                        int layers = (domeTopDy >= 3 && random.nextFloat() < 0.40F) ? 2 : 1;
                        BlockState snowCap = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, layers);
                        if (snowCap.canSurvive(level, capPos)) {
                            level.setBlock(capPos, snowCap, 2);
                        }
                    }
                }
            }
        }

        // 2. Frame the low cairn portal with weathered basalt jambs & stone lintel
        Direction sideDir = frontDir.getClockWise();
        int mouthDist = Math.max(1, (frontDir.getAxis() == Direction.Axis.X ? halfX : halfZ) - 1);
        BlockPos portalCenter = new BlockPos(
                cx + frontDir.getStepX() * mouthDist,
                groundY + 1,
                cz + frontDir.getStepZ() * mouthDist);
        level.setBlock(portalCenter, air, 2);
        level.setBlock(portalCenter.above(), air, 2);
        level.setBlock(portalCenter.relative(frontDir), air, 2);
        level.setBlock(portalCenter.relative(frontDir).above(), air, 2);

        for (Direction jambSide : new Direction[]{sideDir, sideDir.getOpposite()}) {
            BlockPos jamb = portalCenter.relative(jambSide);
            level.setBlock(jamb, polishedBasaltY, 2);
            level.setBlock(jamb.above(),
                    pickBarrowMasonry(random, barrowBricksBlock, crackedBarrowBlock, mossyBarrowBlock), 2);
        }
        level.setBlock(portalCenter.above(2), barrowBricksBlock.defaultBlockState(), 2);

        // 3. Place 1 rime_cap_mushroom or crypt_grave_root inside the central burial niche
        BlockPos centerFloor = new BlockPos(cx, groundY, cz);
        BlockPos centerNiche = centerFloor.above();
        level.setBlock(centerFloor, necroticSoil, 2);
        level.setBlock(centerNiche, air, 2);
        level.setBlock(centerNiche.above(), air, 2);
        // Solid stone keystone above central niche so hanging lanterns or turf are supported
        BlockPos ceilingKeystone = centerFloor.above(3);
        if (level.getBlockState(ceilingKeystone).isAir()) {
            level.setBlock(ceilingKeystone, barrowBricksBlock.defaultBlockState(), 2);
        }

        BlockState innerFlora = (random.nextBoolean() ? rimeCapBlock : graveRootBlock).defaultBlockState();
        if (innerFlora.canSurvive(level, centerNiche)) {
            level.setBlock(centerNiche, innerFlora, 2);
        } else {
            level.setBlock(centerFloor, Blocks.COARSE_DIRT.defaultBlockState(), 2);
            if (innerFlora.canSurvive(level, centerNiche)) {
                level.setBlock(centerNiche, innerFlora, 2);
            } else if (Blocks.BROWN_MUSHROOM.defaultBlockState().canSurvive(level, centerNiche)) {
                level.setBlock(centerNiche, Blocks.BROWN_MUSHROOM.defaultBlockState(), 2);
            }
        }

        // 4. 25% chance of a weathered burial marker (crypt_siphon_lantern / soul_lantern / decorated_pot / bone_block)
        if (random.nextFloat() < 0.25F) {
            int roll = random.nextInt(4);
            BlockPos backNiche = new BlockPos(
                    cx - frontDir.getStepX(),
                    groundY + 1,
                    cz - frontDir.getStepZ());
            if (roll == 0) {
                // Hanging crypt_siphon_lantern from the central keystone overhead
                BlockPos hangPos = centerNiche.above();
                BlockState lanternState = makeLanternState(siphonLanternBlock, true);
                if (lanternState.canSurvive(level, hangPos)) {
                    level.setBlock(hangPos, lanternState, 2);
                } else {
                    level.setBlock(backNiche, makeLanternState(Blocks.SOUL_LANTERN, false), 2);
                }
            } else if (roll == 1) {
                BlockPos hangPos = centerNiche.above();
                BlockState soulLantern = makeLanternState(Blocks.SOUL_LANTERN, true);
                if (soulLantern.canSurvive(level, hangPos)) {
                    level.setBlock(hangPos, soulLantern, 2);
                }
            } else if (roll == 2) {
                level.setBlock(backNiche.below(), barrowBricksBlock.defaultBlockState(), 2);
                level.setBlock(backNiche, Blocks.DECORATED_POT.defaultBlockState(), 2);
            } else {
                level.setBlock(backNiche.below(), barrowBricksBlock.defaultBlockState(), 2);
                level.setBlock(backNiche, Blocks.BONE_BLOCK.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y), 2);
            }
        }

        return true;
    }

    private static BlockState pickBarrowMasonry(
            RandomSource random,
            Block barrowBricks,
            Block crackedBricks,
            Block mossyBricks
    ) {
        float r = random.nextFloat();
        if (r < 0.48F) {
            return barrowBricks.defaultBlockState();
        }
        if (r < 0.78F) {
            return mossyBricks.defaultBlockState();
        }
        return crackedBricks.defaultBlockState();
    }

    private static BlockState makeLanternState(Block block, boolean hanging) {
        BlockState state = block.defaultBlockState();
        Property<?> hangProp = state.getBlock().getStateDefinition().getProperty("hanging");
        if (hangProp instanceof BooleanProperty boolHang && state.hasProperty(boolHang)) {
            state = state.setValue(boolHang, hanging);
        } else if (state.hasProperty(LanternBlock.HANGING)) {
            state = state.setValue(LanternBlock.HANGING, hanging);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return state;
    }

    private static BlockPos findSolidTundraSurface(
            WorldGenLevel level,
            int x,
            int z,
            int hintY,
            Block necroticSoil,
            Block barrowMoss
    ) {
        int topY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        int startY = Math.max(topY, hintY + 3);
        int minY = Math.max(MIN_DRY_SURFACE_Y, startY - 8);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA) || state.is(Blocks.ICE)) {
                return null;
            }
            if (isValidTundraGround(state, necroticSoil, barrowMoss)) {
                BlockState above = level.getBlockState(cursor.above());
                if (isReplaceableAboveGround(above)) {
                    return cursor.immutable();
                }
            }
        }
        return null;
    }

    private static boolean hasSolidFootingWithin(
            WorldGenLevel level,
            int x,
            int refY,
            int z,
            Block necroticSoil,
            Block barrowMoss
    ) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = 1; dy >= -2; dy--) {
            cursor.set(x, refY + dy, z);
            BlockState state = level.getBlockState(cursor);
            if (state.is(Blocks.WATER) || state.is(Blocks.LAVA) || state.is(Blocks.ICE)) {
                return false;
            }
            if (isValidTundraGround(state, necroticSoil, barrowMoss)) {
                return true;
            }
        }
        return false;
    }

    private static void fillDownToSolid(WorldGenLevel level, int x, int startY, int z, BlockState fillState) {
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int minY = Math.max(level.getMinY() + 2, startY - 5);
        for (int y = startY; y >= minY; y--) {
            cursor.set(x, y, z);
            BlockState existing = level.getBlockState(cursor);
            if (existing.isAir() || existing.is(Blocks.SNOW) || existing.is(Blocks.POWDER_SNOW) || existing.canBeReplaced()) {
                level.setBlock(cursor, fillState, 2);
            } else {
                break;
            }
        }
    }

    private static boolean isValidTundraGround(BlockState state, Block necroticSoil, Block barrowMoss) {
        return state.is(necroticSoil)
                || state.is(barrowMoss)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(BlockTags.DIRT)
                || state.is(Blocks.GRAVEL)
                || state.is(Blocks.PACKED_ICE)
                || state.is(BlockTags.BASE_STONE_OVERWORLD);
    }

    private static boolean isStructureMasonry(
            BlockState state,
            Block barrowBricks,
            Block crackedBricks,
            Block mossyBricks
    ) {
        return state.is(barrowBricks)
                || state.is(crackedBricks)
                || state.is(mossyBricks)
                || state.is(Blocks.DEEPSLATE_BRICKS)
                || state.is(Blocks.POLISHED_BASALT)
                || state.is(Blocks.CHEST)
                || state.is(Blocks.SPAWNER);
    }

    private static boolean isReplaceableAboveGround(BlockState state) {
        return !state.is(Blocks.WATER)
                && !state.is(Blocks.LAVA)
                && (state.isAir() || state.is(Blocks.SNOW) || state.is(Blocks.POWDER_SNOW) || state.canBeReplaced());
    }

    private static Block resolveBlock(ResourceLocation id, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(id).orElse(fallback);
    }
}
