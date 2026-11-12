package ddraig.net.entropica.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;

public class GaleSwirlPuffParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float rotSpeed;
    private final float baseQuadSize;

    public GaleSwirlPuffParticle(ClientLevel level, double x, double y, double z, double r, double g, double b, SpriteSet sprites) {
        super(level, x, y, z, (level.random.nextDouble() - 0.5) * 0.05, 0.02 + level.random.nextDouble() * 0.03, (level.random.nextDouble() - 0.5) * 0.05, sprites.first());
        this.sprites = sprites;
        this.friction = 0.98F;
        this.gravity = -0.015F;
        this.baseQuadSize = 0.12F + level.random.nextFloat() * 0.08F;
        this.quadSize = this.baseQuadSize;
        this.lifetime = 40 + this.random.nextInt(30);
        this.hasPhysics = false;

        this.rotSpeed = (this.random.nextBoolean() ? 1.0f : -1.0f) * (0.05f + this.random.nextFloat() * 0.08f);
        this.roll = this.random.nextFloat() * (float) (Math.PI * 2);
        this.oRoll = this.roll;

        if (r > 0 || g > 0 || b > 0) {
            this.rCol = (float) r;
            this.gCol = (float) g;
            this.bCol = (float) b;
        } else {
            this.rCol = 0.99f;
            this.gCol = 0.90f;
            this.bCol = 0.54f;
        }

        this.alpha = 1.0f;
        this.setSpriteFromAge(sprites);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240;
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.oRoll = this.roll;
        this.roll += this.rotSpeed;

        super.tick();

        float ageProgress = (float) this.age / (float) this.lifetime;
        this.quadSize = this.baseQuadSize * (1.0f + ageProgress * 0.6f);
        this.alpha = 1.0f - ageProgress;

        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double r, double g, double b, RandomSource random) {
            return new GaleSwirlPuffParticle(level, x, y, z, r, g, b, this.sprites);
        }
    }
}
