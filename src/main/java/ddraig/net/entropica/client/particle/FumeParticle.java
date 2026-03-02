package ddraig.net.entropica.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FumeParticle extends SingleQuadParticle {

    private final SpriteSet sprites;

    protected FumeParticle(ClientLevel level, double x, double y, double z,
                           double xSpeed, double ySpeed, double zSpeed,
                           FumeParticleOption options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());

        this.sprites = sprites;

        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;

        this.rCol = options.r();
        this.gCol = options.g();
        this.bCol = options.b();

        this.quadSize *= 0.5F + this.random.nextFloat() * 0.5F;
        this.lifetime = 15 + this.random.nextInt(10);
        this.hasPhysics = false;
        this.gravity = 0.0F;

        this.setSpriteFromAge(this.sprites);
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.move(this.xd, this.yd, this.zd);
            this.setSpriteFromAge(this.sprites);
            this.alpha = 1.0F - ((float) this.age / (float) this.lifetime);
        }
    }

    @Override
    protected SingleQuadParticle.Layer getLayer() {
        return SingleQuadParticle.Layer.TRANSLUCENT;
    }


    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 cameraPos = camera.getPosition();

        float px = (float) (Mth.lerp(partialTick, this.xo, this.x) - cameraPos.x());
        float py = (float) (Mth.lerp(partialTick, this.yo, this.y) - cameraPos.y());
        float pz = (float) (Mth.lerp(partialTick, this.zo, this.z) - cameraPos.z());

        float half = this.quadSize / 2.0F;
        float minX = px - half, maxX = px + half;
        float minY = py - half, maxY = py + half;
        float minZ = pz - half, maxZ = pz + half;

        float u0 = this.getU0(), u1 = this.getU1();
        float v0 = this.getV0(), v1 = this.getV1();

        int light = this.getLightColor(partialTick);

        // --- ARTIFICIAL 3D SHADING MULTIPLIERS ---
        // Top is 100%, Z sides are 80%, X sides are 60%, Bottom is 50%
        float rTop = this.rCol, gTop = this.gCol, bTop = this.bCol;
        float rZ = this.rCol * 0.8F, gZ = this.gCol * 0.8F, bZ = this.bCol * 0.8F;
        float rX = this.rCol * 0.6F, gX = this.gCol * 0.6F, bX = this.bCol * 0.6F;
        float rBot = this.rCol * 0.5F, gBot = this.gCol * 0.5F, bBot = this.bCol * 0.5F;

        // Y- (Down)
        buffer.addVertex(minX, minY, maxZ).setColor(rBot, gBot, bBot, this.alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(minX, minY, minZ).setColor(rBot, gBot, bBot, this.alpha).setUv(u0, v0).setLight(light);
        buffer.addVertex(maxX, minY, minZ).setColor(rBot, gBot, bBot, this.alpha).setUv(u1, v0).setLight(light);
        buffer.addVertex(maxX, minY, maxZ).setColor(rBot, gBot, bBot, this.alpha).setUv(u1, v1).setLight(light);

        // Y+ (Up)
        buffer.addVertex(minX, maxY, minZ).setColor(rTop, gTop, bTop, this.alpha).setUv(u0, v0).setLight(light);
        buffer.addVertex(minX, maxY, maxZ).setColor(rTop, gTop, bTop, this.alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(maxX, maxY, maxZ).setColor(rTop, gTop, bTop, this.alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(maxX, maxY, minZ).setColor(rTop, gTop, bTop, this.alpha).setUv(u1, v0).setLight(light);

        // Z- (North)
        buffer.addVertex(maxX, maxY, minZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u1, v0).setLight(light);
        buffer.addVertex(maxX, minY, minZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(minX, minY, minZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(minX, maxY, minZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u0, v0).setLight(light);

        // Z+ (South)
        buffer.addVertex(minX, maxY, maxZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u0, v0).setLight(light);
        buffer.addVertex(minX, minY, maxZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(maxX, minY, maxZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(maxX, maxY, maxZ).setColor(rZ, gZ, bZ, this.alpha).setUv(u1, v0).setLight(light);

        // X- (West)
        buffer.addVertex(minX, maxY, minZ).setColor(rX, gX, bX, this.alpha).setUv(u0, v0).setLight(light);
        buffer.addVertex(minX, minY, minZ).setColor(rX, gX, bX, this.alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(minX, minY, maxZ).setColor(rX, gX, bX, this.alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(minX, maxY, maxZ).setColor(rX, gX, bX, this.alpha).setUv(u1, v0).setLight(light);

        // X+ (East)
        buffer.addVertex(maxX, maxY, maxZ).setColor(rX, gX, bX, this.alpha).setUv(u1, v0).setLight(light);
        buffer.addVertex(maxX, minY, maxZ).setColor(rX, gX, bX, this.alpha).setUv(u1, v1).setLight(light);
        buffer.addVertex(maxX, minY, minZ).setColor(rX, gX, bX, this.alpha).setUv(u0, v1).setLight(light);
        buffer.addVertex(maxX, maxY, minZ).setColor(rX, gX, bX, this.alpha).setUv(u0, v0).setLight(light);
    }

    public static class Provider implements ParticleProvider<FumeParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(FumeParticleOption options, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed,
                                       RandomSource random) {
            return new FumeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, options, sprites);
        }
    }
}