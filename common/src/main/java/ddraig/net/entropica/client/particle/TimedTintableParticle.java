package ddraig.net.entropica.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

public class TimedTintableParticle extends SingleQuadParticle {

    private final SpriteSet sprites;
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
        this.rCol = options.r();
        this.gCol = options.g();
        this.bCol = options.b();
        this.lifetime = options.maxAge() > 0 ? options.maxAge() : 20 + this.random.nextInt(10);
        this.quadSize *= (options.scale() > 0 ? options.scale() : 1.0f);
        this.oRoll = options.roll();
        this.roll = options.roll();
        this.rollSpeed = options.rollSpeed();

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
                double force = 0.05;
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
            double time = (this.age + this.hashCode() % 100) * 0.1;
            this.xd += Math.sin(time + this.x * 0.5) * 0.02;
            this.yd += Math.cos(time + this.y * 0.5) * 0.02;
            this.zd += Math.sin(time + this.z * 0.5) * 0.02;
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.setSpriteFromAge(this.sprites);
        }
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
