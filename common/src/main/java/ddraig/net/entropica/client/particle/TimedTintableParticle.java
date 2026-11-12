package ddraig.net.entropica.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class TimedTintableParticle extends SingleQuadParticle {

    private static final int FULL_LIGHT = 15728880;

    private final SpriteSet sprites;
    private final float baseQuadSize;
    private final float rollSpeed;
    private final int motionMode;
    private final float param1;
    private final float param2;
    private final float param3;

    public TimedTintableParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed,
                                 TimedTintableParticleOption options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());

        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.rCol = Mth.clamp(options.r(), 0.0f, 1.0f);
        this.gCol = Mth.clamp(options.g(), 0.0f, 1.0f);
        this.bCol = Mth.clamp(options.b(), 0.0f, 1.0f);
        this.alpha = 0.88f;
        this.lifetime = options.maxAge() > 0 ? options.maxAge() : 20 + this.random.nextInt(10);
        this.quadSize *= (options.scale() > 0 ? options.scale() : 1.0f);
        this.baseQuadSize = this.quadSize;
        this.oRoll = options.roll();
        this.roll = options.roll();
        this.rollSpeed = options.rollSpeed();
        this.friction = 0.94f;

        this.motionMode = options.motionMode();
        this.param1 = options.param1();
        this.param2 = options.param2();
        this.param3 = options.param3();

        this.setSpriteFromAge(sprites);
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return FULL_LIGHT;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        this.oRoll = this.roll;
        this.roll += this.rollSpeed;

        if (this.motionMode == TimedTintableParticleOption.MODE_TOWARDS) {
            double dx = this.param1 - this.x;
            double dy = this.param2 - this.y;
            double dz = this.param3 - this.z;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.05) {
                double speed = 0.08;
                this.xd += (dx / dist) * speed;
                this.yd += (dy / dist) * speed;
                this.zd += (dz / dist) * speed;
            }
        } else if (this.motionMode == TimedTintableParticleOption.MODE_AWAY) {
            double dx = this.x - this.param1;
            double dy = this.y - this.param2;
            double dz = this.z - this.param3;
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0.001) {
                double force = 0.045;
                this.xd += (dx / dist) * force;
                this.yd += (dy / dist) * force;
                this.zd += (dz / dist) * force;
            }
        } else if (this.motionMode == TimedTintableParticleOption.MODE_JITTER) {
            float jitter = this.param1 > 0 ? this.param1 : 0.04f;
            this.xd += (this.random.nextFloat() - 0.5f) * jitter;
            this.yd += (this.random.nextFloat() - 0.5f) * jitter;
            this.zd += (this.random.nextFloat() - 0.5f) * jitter;
        } else if (this.motionMode == TimedTintableParticleOption.MODE_NOISE) {
            double time = (this.age + (this.hashCode() & 0x7F)) * 0.12;
            this.xd += Math.sin(time + this.x * 0.5) * 0.016;
            this.yd += Math.cos(time + this.y * 0.5) * 0.016;
            this.zd += Math.sin(time + this.z * 0.5) * 0.016;
        } else if (this.motionMode == TimedTintableParticleOption.MODE_SPIRAL) {
            double dx = this.x - this.param1;
            double dz = this.z - this.param3;
            double dist = Math.sqrt(dx * dx + dz * dz);
            if (dist > 0.02) {
                double tangX = -dz / dist;
                double tangZ = dx / dist;
                double pullX = -dx / dist;
                double pullZ = -dz / dist;
                this.xd = tangX * 0.11 + pullX * 0.015;
                this.zd = tangZ * 0.11 + pullZ * 0.015;
            }
            this.yd = this.param2 != 0.0f ? this.param2 : 0.035;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        float lifeProgress = (float) this.age / (float) Math.max(1, this.lifetime);
        if (lifeProgress < 0.15f) {
            float t = lifeProgress / 0.15f;
            this.alpha = Mth.lerp(t, 0.25f, 0.94f);
            this.quadSize = this.baseQuadSize * Mth.lerp(t, 0.65f, 1.0f);
        } else if (lifeProgress > 0.62f) {
            float t = (lifeProgress - 0.62f) / 0.38f;
            this.alpha = Mth.lerp(t, 0.94f, 0.0f);
            this.quadSize = this.baseQuadSize * Mth.lerp(t, 1.0f, 0.45f);
        } else {
            this.alpha = 0.94f;
            this.quadSize = this.baseQuadSize;
        }

        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;
        this.move(this.xd, this.yd, this.zd);
        this.setSpriteFromAge(this.sprites);
    }

    public static class Provider implements ParticleProvider<TimedTintableParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(TimedTintableParticleOption options, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new TimedTintableParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options, this.sprites);
        }
    }
}
