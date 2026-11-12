package ddraig.net.entropica.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class HazeEffect extends MobEffect {
    public HazeEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF459A); // Psychedelic pink/purple color
    }

    @Override
    public boolean applyEffectTick(ServerLevel level, LivingEntity entity, int amplifier) {
        // Spawn subtle spores/bubbles around the player's head as a simple ambient hint
        if (entity.tickCount % 2 == 0) {
            level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, 
                entity.getX(), entity.getEyeY() + 0.1, entity.getZ(), 
                2, 0.3, 0.3, 0.3, 0.01);
        }
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}
