package ddraig.net.entropica.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class ShadowSlashParticle extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final float baseQuadSize;

    public ShadowSlashParticle(ClientLevel level, double x, double y, double z, double vx, double vy, double vz, SpriteSet sprites) {
        super(level, x, y, z, vx, vy, vz, sprites.first());
        this.sprites = sprites;
        this.xd = vx;
        this.yd = vy;
        this.zd = vz;
        this.friction = 0.92F;
        this.gravity = 0.0F;
        this.baseQuadSize = 0.45F + level.random.nextFloat() * 0.15F;
        this.quadSize = this.baseQuadSize;
        this.lifetime = 12 + this.random.nextInt(6);
        this.hasPhysics = false;

        this.rCol = 1.0f;
        this.gCol = 1.0f;
        this.bCol = 1.0f;
        this.alpha = 1.0f;

        this.roll = (float) (Math.atan2(vz, vx) + (level.random.nextFloat() - 0.5f) * 0.3f);
        this.oRoll = this.roll;

        this.setSpriteFromAge(sprites);
    }

    @Override
    public int getLightColor(float partialTick) {
        return 240; // Emissive glow in darkness
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        float progress = (float) this.age / (float) this.lifetime;
        // Expands slightly as it strikes, then dissolves
        this.quadSize = this.baseQuadSize * (1.0f + progress * 0.4f);
        this.alpha = (float) Math.pow(1.0f - progress, 1.5);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ShadowSlashParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprites);
        }
    }
}
