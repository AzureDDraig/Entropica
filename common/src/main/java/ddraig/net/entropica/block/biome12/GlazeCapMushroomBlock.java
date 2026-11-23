package ddraig.net.entropica.block.biome12;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Glaze-Cap Mushroom Block — frost-glazed amber fungal cap block of giant polar mushrooms.
 * Cushions fall damage by 40% and releases a puff of golden spore motes on impact.
 */
public class GlazeCapMushroomBlock extends Block {
    public static final MapCodec<GlazeCapMushroomBlock> CODEC = simpleCodec(GlazeCapMushroomBlock::new);

    public GlazeCapMushroomBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        entity.causeFallDamage(fallDistance, 0.6F, level.damageSources().fall());
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && fallDistance > 1.0D) {
            serverLevel.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR,
                    entity.getX(), pos.getY() + 1.05D, entity.getZ(),
                    10, 0.35D, 0.1D, 0.35D, 0.04D);
        }
    }
}
