package ddraig.net.entropica.client;

import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import java.util.Random;

public class ParalyzedParticleHandler {
    private static final Random RAND = new Random();

    public static void clientTick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null) return;

        for (net.minecraft.world.entity.Entity entity : level.entitiesForRendering()) {
            if (entity instanceof LivingEntity living && living.hasEffect(ModEffects.PARALYZED)) {
                // If it is the current player in first person, don't spawn around their head so it doesn't block vision
                if (living == mc.player && mc.options.getCameraType().isFirstPerson()) {
                    continue;
                }

                double x = living.getX();
                double y = living.getY();
                double z = living.getZ();
                double height = living.getBbHeight();
                double width = living.getBbWidth();

                // 1. Around the head
                if (RAND.nextFloat() < 0.35f) {
                    double px = x + (RAND.nextFloat() - 0.5D) * width;
                    double py = y + height + (RAND.nextFloat() - 0.5D) * 0.25D;
                    double pz = z + (RAND.nextFloat() - 0.5D) * width;
                    level.addParticle(ParticleTypes.WITCH, px, py, pz, 0, 0, 0);
                }

                // 2. Arcing to limbs/body
                if (RAND.nextFloat() < 0.45f) {
                    double px = x + (RAND.nextFloat() - 0.5D) * width * 1.2D;
                    double py = y + RAND.nextFloat() * height;
                    double pz = z + (RAND.nextFloat() - 0.5D) * width * 1.2D;
                    level.addParticle(ParticleTypes.WITCH, px, py, pz, 0, 0, 0);
                }
                
                if (RAND.nextFloat() < 0.2f) {
                    double px = x + (RAND.nextFloat() - 0.5D) * width * 1.2D;
                    double py = y + RAND.nextFloat() * height;
                    double pz = z + (RAND.nextFloat() - 0.5D) * width * 1.2D;
                    level.addParticle(ParticleTypes.PORTAL, px, py, pz, 0, 0.02, 0);
                }
            }
        }
    }
}
