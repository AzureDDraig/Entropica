package ddraig.net.entropica.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class SpectrumSparkleParticle extends SingleQuadParticle {
    private final SpriteSet sprites;

    public SpectrumSparkleParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet sprites) {
        super(level, x, y, z, (level.random.nextDouble() - 0.5) * 0.02, 0.02 + level.random.nextDouble() * 0.02, (level.random.nextDouble() - 0.5) * 0.02, sprites.first());
        this.sprites = sprites;
        this.friction = 0.96F;
        this.gravity = -0.01F; // Slow gentle floating upward like EndRod
        this.quadSize *= 0.75F;
        this.lifetime = 30 + this.random.nextInt(20);
        this.hasPhysics = false;

        // Custom RGB color tint
        this.rCol = (float) r;
        this.gCol = (float) g;
        this.bCol = (float) b;

        this.setSpriteFromAge(sprites);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double r, double g, double b, RandomSource random) {
            return new SpectrumSparkleParticle(level, x, y, z, r, g, b, this.sprites);
        }
    }
}
