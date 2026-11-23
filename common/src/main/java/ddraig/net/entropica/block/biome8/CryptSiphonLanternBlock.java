package ddraig.net.entropica.block.biome8;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Crypt-Siphon Lantern — warding cage lantern forged from Barrow Stone Bricks and a Grave-Glow Feather.
 * Casts lavender spectral light (13) and establishes a 16-block warding aura that blocks natural Undead spawns.
 */
public class CryptSiphonLanternBlock extends LanternBlock {
    public static final MapCodec<CryptSiphonLanternBlock> CODEC = simpleCodec(CryptSiphonLanternBlock::new);
    public static final double DEFAULT_WARD_RADIUS = 16.0D;

    protected static final VoxelShape STANDING_AABB = Block.box(4.5D, 0.0D, 4.5D, 11.5D, 9.5D, 11.5D);
    protected static final VoxelShape HANGING_AABB = Block.box(4.5D, 1.0D, 4.5D, 11.5D, 10.5D, 11.5D);

    private static final Map<ResourceKey<Level>, Set<BlockPos>> ACTIVE_LANTERNS = new ConcurrentHashMap<>();

    public CryptSiphonLanternBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends LanternBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(HANGING) ? HANGING_AABB : STANDING_AABB;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide()) {
            registerLantern(level.dimension(), pos);
        }
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean isMoving) {
        super.affectNeighborsAfterRemoval(state, level, pos, isMoving);
        unregisterLantern(level.dimension(), pos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        registerLantern(level.dimension(), pos);
    }

    private static void registerLantern(ResourceKey<Level> dimension, BlockPos pos) {
        ACTIVE_LANTERNS.computeIfAbsent(dimension, k -> ConcurrentHashMap.newKeySet()).add(pos.immutable());
    }

    private static void unregisterLantern(ResourceKey<Level> dimension, BlockPos pos) {
        Set<BlockPos> set = ACTIVE_LANTERNS.get(dimension);
        if (set != null) {
            set.remove(pos);
        }
    }

    /**
     * Checks whether {@code pos} lies within {@code radius} blocks of an active Crypt-Siphon Lantern
     * in {@code level}. Uses a thread-safe position cache backed by chunk-section palette verification.
     */
    public static boolean isWithinUndeadWardRadius(ServerLevel level, BlockPos pos, double radius) {
        if (level == null || pos == null || radius <= 0.0D || !ModBlocks.CRYPT_SIPHON_LANTERN.isBound()) {
            return false;
        }

        Block lanternBlock = ModBlocks.CRYPT_SIPHON_LANTERN.get();
        double maxDistSq = radius * radius;
        ResourceKey<Level> dim = level.dimension();

        Set<BlockPos> tracked = ACTIVE_LANTERNS.get(dim);
        if (tracked != null && !tracked.isEmpty()) {
            Iterator<BlockPos> it = tracked.iterator();
            while (it.hasNext()) {
                BlockPos p = it.next();
                if (p.distSqr(pos) <= maxDistSq) {
                    if (!level.isLoaded(p)) {
                        continue;
                    }
                    if (level.getBlockState(p).is(lanternBlock)) {
                        return true;
                    } else {
                        it.remove();
                    }
                }
            }
        }

        int r = Mth.ceil(radius);
        int minChunkX = (pos.getX() - r) >> 4;
        int maxChunkX = (pos.getX() + r) >> 4;
        int minChunkZ = (pos.getZ() - r) >> 4;
        int maxChunkZ = (pos.getZ() + r) >> 4;
        int minY = Math.max(level.getMinY(), pos.getY() - r);
        int maxY = Math.min(level.getMaxY() - 1, pos.getY() + r);
        if (minY > maxY) {
            return false;
        }
        int minSecIdx = level.getSectionIndex(minY);
        int maxSecIdx = level.getSectionIndex(maxY);

        for (int cx = minChunkX; cx <= maxChunkX; cx++) {
            for (int cz = minChunkZ; cz <= maxChunkZ; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                LevelChunk chunk = level.getChunk(cx, cz);
                LevelChunkSection[] sections = chunk.getSections();
                for (int secIdx = Math.max(0, minSecIdx); secIdx <= Math.min(sections.length - 1, maxSecIdx); secIdx++) {
                    LevelChunkSection section = sections[secIdx];
                    if (section == null || section.hasOnlyAir() || !section.maybeHas(s -> s.is(lanternBlock))) {
                        continue;
                    }
                    int baseX = cx << 4;
                    int baseY = level.getSectionYFromSectionIndex(secIdx) << 4;
                    int baseZ = cz << 4;
                    boolean foundInRadius = false;
                    for (int ly = 0; ly < 16; ly++) {
                        for (int lx = 0; lx < 16; lx++) {
                            for (int lz = 0; lz < 16; lz++) {
                                if (section.getBlockState(lx, ly, lz).is(lanternBlock)) {
                                    BlockPos foundPos = new BlockPos(baseX + lx, baseY + ly, baseZ + lz);
                                    registerLantern(dim, foundPos);
                                    if (foundPos.distSqr(pos) <= maxDistSq) {
                                        foundInRadius = true;
                                    }
                                }
                            }
                        }
                    }
                    if (foundInRadius) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(4) == 0) {
            double x = pos.getX() + 0.35D + random.nextDouble() * 0.3D;
            double y = pos.getY() + (state.getValue(HANGING) ? 0.35D : 0.3D) + random.nextDouble() * 0.3D;
            double z = pos.getZ() + 0.35D + random.nextDouble() * 0.3D;
            int roll = random.nextInt(3);
            if (roll == 0) {
                level.addParticle(new DustParticleOptions(0xC084FC, 0.65F), x, y, z, 0.0D, 0.012D, 0.0D);
            } else if (roll == 1) {
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.01D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.008D, 0.0D);
            }
        }
    }
}
