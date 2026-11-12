package ddraig.net.entropica.client.gravity;

import ddraig.net.entropica.block.entity.GravityCenterBlockEntity;
import ddraig.net.entropica.gravity.GravityApi;
import ddraig.net.entropica.network.GravityFlipPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class GravitonSolesClientHandler {

    private static long lastFlipGameTime = 0L;
    private static net.minecraft.core.Direction currentWallDir = null;

    public static net.minecraft.core.Direction getCurrentWallDir() {
        return currentWallDir;
    }

    public static net.minecraft.core.Direction getWallDirection(net.minecraft.world.level.Level level, net.minecraft.world.entity.LivingEntity entity) {
        if (level == null || entity == null) return null;
        AABB box = entity.getBoundingBox();
        for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
            AABB testBox = box.move(dir.getStepX() * 0.12, 0, dir.getStepZ() * 0.12);
            if (!level.noCollision(entity, testBox)) {
                return dir;
            }
        }
        return null;
    }

    public static void clientTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        boolean hasSoles = GravityApi.hasGravitonSoles(player);
        GravityCenterBlockEntity activeCenter = GravityCenterBlockEntity.getAffectingCenter(mc.level, player.position());
        boolean inGravityCenter = (activeCenter != null);

        if (!hasSoles && !inGravityCenter) {
            if (GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID())) {
                GravityApi.resetGravity(player);
                NetworkManager.sendToServer(new GravityFlipPayload(GravityApi.VANILLA_BASE_GRAVITY, false));
            }
            return;
        }

        // 1. Total fall damage negation
        player.resetFallDistance();

        long gameTime = mc.level.getGameTime();
        Vec3 motion = player.getDeltaMovement();
        AABB box = player.getBoundingBox();

        // 2. Wall Contact, Sticking & Traversal
        net.minecraft.core.Direction wallDir = getWallDirection(mc.level, player);
        boolean touchingWall = (wallDir != null) || player.horizontalCollision;
        currentWallDir = hasSoles && !GravityApi.isInverted(player) ? wallDir : null;

        net.minecraft.world.entity.player.Input kp = player.input != null ? player.input.keyPresses : net.minecraft.world.entity.player.Input.EMPTY;
        boolean isSneaking = kp.shift() || player.isCrouching();
        boolean isClimbingUp = kp.jump() || (kp.forward() && player.getXRot() < -25.0f);
        boolean isClimbingDown = kp.backward() || (kp.forward() && player.getXRot() > 25.0f);

        if (touchingWall) {
            if (hasSoles) {
                if (isSneaking) {
                    // Wall Lock: completely freeze vertical velocity, stick in place
                    player.setDeltaMovement(motion.x * 0.4, 0.0, motion.z * 0.4);
                    player.resetFallDistance();
                    player.hasImpulse = true;
                } else if (isClimbingUp) {
                    // Climb up
                    player.setDeltaMovement(motion.x, 0.26, motion.z);
                    player.resetFallDistance();
                    player.hasImpulse = true;
                } else if (isClimbingDown) {
                    // Climb down
                    player.setDeltaMovement(motion.x, -0.22, motion.z);
                    player.resetFallDistance();
                    player.hasImpulse = true;
                } else {
                    // Horizontal wall walking / sticking: cancel downward gravity slide!
                    double newY = motion.y < 0.0 ? 0.0 : motion.y;
                    // Gentle inward adhesive pull towards the wall surface
                    double pullX = (wallDir != null) ? wallDir.getStepX() * 0.02 : 0.0;
                    double pullZ = (wallDir != null) ? wallDir.getStepZ() * 0.02 : 0.0;
                    player.setDeltaMovement(motion.x + pullX, newY, motion.z + pullZ);
                    player.resetFallDistance();
                    player.hasImpulse = true;
                }
            } else if (inGravityCenter) {
                // Gravity Center adhesion: holds entity firmly against the surface towards center
                Vec3 corePos = activeCenter.getBlockPos().getCenter();
                Vec3 toCore = corePos.subtract(player.position());
                if (toCore.lengthSqr() > 1e-4) {
                    Vec3 pull = toCore.normalize().scale(0.04);
                    player.setDeltaMovement(motion.x * 0.8 + pull.x, motion.y * 0.8 + pull.y, motion.z * 0.8 + pull.z);
                    player.resetFallDistance();
                    player.hasImpulse = true;
                }
            }
        }

        // 3. Ceiling Adhesion & Auto-Inversion
        boolean inverted = GravityApi.isInverted(player);
        if (inGravityCenter && !hasSoles) {
            // Gravity Center directional down: down is strictly towards the core
            Vec3 corePos = activeCenter.getBlockPos().getCenter();
            Vec3 toCenter = corePos.subtract(player.getEyePosition());
            Vec3 pullDir = toCenter.normalize();
            if (pullDir.y > 0.45) {
                // Core is above player -> pull is upwards -> Inverted
                if (!inverted && (gameTime - lastFlipGameTime > 15L)) {
                    lastFlipGameTime = gameTime;
                    GravityApi.SOLES_INVERTED_ENTITIES.add(player.getUUID());
                    GravityApi.setGravity(player, GravityApi.INVERTED_GRAVITY);
                    NetworkManager.sendToServer(new GravityFlipPayload(GravityApi.INVERTED_GRAVITY, true));
                    mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f, false);
                }
            } else if (pullDir.y < -0.20) {
                // Core is below player -> pull is downwards -> Normal
                if (inverted && (gameTime - lastFlipGameTime > 15L)) {
                    lastFlipGameTime = gameTime;
                    GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID());
                    GravityApi.resetGravity(player);
                    NetworkManager.sendToServer(new GravityFlipPayload(GravityApi.VANILLA_BASE_GRAVITY, false));
                    mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f, false);
                }
            }
        } else if (hasSoles && !inverted) {
            BlockPos headPos = BlockPos.containing(player.getX(), box.maxY + 0.15, player.getZ());
            boolean ceilingAbove = mc.level.getBlockState(headPos).isSolid();
            boolean upwardHit = (player.verticalCollision && motion.y >= -0.05) || (ceilingAbove && motion.y >= 0.0);

            if (!player.onGround() && upwardHit && (gameTime - lastFlipGameTime > 10L)) {
                lastFlipGameTime = gameTime;
                GravityApi.SOLES_INVERTED_ENTITIES.add(player.getUUID());
                GravityApi.setGravity(player, GravityApi.INVERTED_GRAVITY);
                NetworkManager.sendToServer(new GravityFlipPayload(GravityApi.INVERTED_GRAVITY, true));

                mc.level.playLocalSound(player.getX(), player.getY(), player.getZ(),
                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f, false);
            }
        }

        // 4. Ceiling Traction & Surface Lock
        if (inverted) {
            BlockPos ceilPos = BlockPos.containing(player.getX(), box.maxY + 0.15, player.getZ());
            boolean touchingCeiling = player.verticalCollision || mc.level.getBlockState(ceilPos).isSolid();
            if (touchingCeiling) {
                if (motion.y > 0.0) {
                    player.setDeltaMovement(motion.x, 0.0, motion.z);
                }
                player.resetFallDistance();
                boolean isMoving = kp.forward() || kp.backward() || kp.left() || kp.right();
                if (!isMoving) {
                    player.setDeltaMovement(motion.x * 0.6, 0.0, motion.z * 0.6);
                }
                player.hasImpulse = true;
            }
        }

        // 5. 360° Edge-Wrapping around block faces (Walking and Sneaking)
        boolean isMoving = kp.forward() || kp.backward() || kp.left() || kp.right();
        if ((isMoving || isSneaking) && !kp.jump() && (gameTime - lastFlipGameTime > 10L)) {
            float forwardInput = kp.forward() ? 1.0f : (kp.backward() ? -1.0f : 0.0f);
            float strafeInput = kp.left() ? 1.0f : (kp.right() ? -1.0f : 0.0f);

            if (forwardInput != 0.0f || strafeInput != 0.0f) {
                Vec3 look = player.getLookAngle();
                Vec3 forward = new Vec3(look.x, 0.0, look.z);
                if (forward.lengthSqr() > 1e-4) {
                    forward = forward.normalize();
                } else {
                    forward = new Vec3(0, 0, 1);
                }
                Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
                Vec3 moveDir = forward.scale(forwardInput).add(right.scale(strafeInput)).normalize();

                Vec3 pPos = player.position();

                if (!inverted) {
                    // Standing on floor -> check for ledge drop-off
                    BlockPos standPos = BlockPos.containing(pPos.x, pPos.y - 0.2, pPos.z);
                    BlockState standState = mc.level.getBlockState(standPos);
                    if (!standState.isSolid()) {
                        standPos = BlockPos.containing(pPos.x, pPos.y - 0.05, pPos.z);
                        standState = mc.level.getBlockState(standPos);
                    }

                    if (standState.isSolid()) {
                        BlockPos aheadBelow = BlockPos.containing(pPos.x + moveDir.x * 0.35, pPos.y - 0.5, pPos.z + moveDir.z * 0.35);
                        if (!mc.level.getBlockState(aheadBelow).isSolid()) {
                            BlockPos underside = standPos.below();
                            if (!mc.level.getBlockState(underside).isSolid()) {
                                // Wrap around corner to underside of platform!
                                lastFlipGameTime = gameTime;
                                GravityApi.SOLES_INVERTED_ENTITIES.add(player.getUUID());
                                GravityApi.setGravity(player, GravityApi.INVERTED_GRAVITY);
                                NetworkManager.sendToServer(new GravityFlipPayload(GravityApi.INVERTED_GRAVITY, true));

                                double destX = pPos.x + moveDir.x * 0.25;
                                double destZ = pPos.z + moveDir.z * 0.25;
                                double destY = standPos.getY() - player.getBbHeight() - 0.05;
                                player.setPos(destX, destY, destZ);
                                player.setDeltaMovement(0.0, 0.05, 0.0);

                                mc.level.playLocalSound(destX, destY, destZ,
                                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f, false);
                            } else {
                                // Solid wall/cliff below: lock onto wall to prevent freefall
                                player.setDeltaMovement(motion.x * 0.2, 0.0, motion.z * 0.2);
                                player.resetFallDistance();
                                player.hasImpulse = true;
                            }
                        }
                    }
                } else {
                    // Inverted on ceiling -> check for ledge edge to flip back to floor
                    BlockPos ceilPos = BlockPos.containing(pPos.x, box.maxY + 0.2, pPos.z);
                    BlockState ceilState = mc.level.getBlockState(ceilPos);
                    if (!ceilState.isSolid()) {
                        ceilPos = BlockPos.containing(pPos.x, box.maxY + 0.05, pPos.z);
                        ceilState = mc.level.getBlockState(ceilPos);
                    }

                    if (ceilState.isSolid()) {
                        BlockPos aheadCeil = BlockPos.containing(pPos.x + moveDir.x * 0.35, box.maxY + 0.2, pPos.z + moveDir.z * 0.35);
                        if (!mc.level.getBlockState(aheadCeil).isSolid()) {
                            BlockPos topFloor = ceilPos.above();
                            if (!mc.level.getBlockState(topFloor).isSolid()) {
                                // Wrap around corner to top floor of platform!
                                lastFlipGameTime = gameTime;
                                GravityApi.SOLES_INVERTED_ENTITIES.remove(player.getUUID());
                                GravityApi.resetGravity(player);
                                NetworkManager.sendToServer(new GravityFlipPayload(GravityApi.VANILLA_BASE_GRAVITY, false));

                                double destX = pPos.x + moveDir.x * 0.25;
                                double destZ = pPos.z + moveDir.z * 0.25;
                                double destY = ceilPos.getY() + 1.05;
                                player.setPos(destX, destY, destZ);
                                player.setDeltaMovement(0.0, -0.05, 0.0);

                                mc.level.playLocalSound(destX, destY, destZ,
                                        SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.3f, false);
                            } else {
                                // Solid wall above: lock onto wall
                                player.setDeltaMovement(motion.x * 0.2, 0.0, motion.z * 0.2);
                                player.resetFallDistance();
                                player.hasImpulse = true;
                            }
                        }
                    }
                }
            }
        }
    }
}
