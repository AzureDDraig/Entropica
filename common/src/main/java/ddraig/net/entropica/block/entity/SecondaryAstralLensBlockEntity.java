package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.PureOpticFiberBlock;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import ddraig.net.entropica.entity.EssenceOrbEntity;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SecondaryAstralLensBlockEntity extends BlockEntity {

    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private boolean isReceiving = false;
    private String activeStarName = "Uncalibrated";
    private int receivingTicksLeft = 0;
    private float beamDistance = 16.0f;
    private float prevBeamDistance = 16.0f;
    private String targetName = "Uncalibrated";
    private boolean isFocused = false;
    private BlockPos targetPos = null;

    // Calcite Transmutation Tracking (5 seconds / 100 ticks)
    private BlockPos calciteBlockPos = null;
    private int calciteTransmuteTicks = 0;
    private int calciteItemEntityId = -1;
    private int calciteItemTicks = 0;

    public SecondaryAstralLensBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SECONDARY_ASTRAL_LENS_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(-30000000.0, -30000000.0, -30000000.0, 30000000.0, 30000000.0, 30000000.0);
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public float getBeamDistance() {
        return beamDistance;
    }

    public void setBeamDistance(float dist) {
        this.beamDistance = dist;
    }

    public String getTargetName() {
        return targetName;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocus(float yaw, float pitch, String targetName, boolean isFocused) {
        this.yaw = (yaw % 360.0f + 360.0f) % 360.0f;
        this.pitch = Math.max(-85.0f, Math.min(85.0f, pitch));
        this.targetName = targetName != null ? targetName : "Manual";
        this.isFocused = isFocused;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void setTargetPos(BlockPos targetPos) {
        this.targetPos = targetPos;
        if (targetPos != null) {
            double targetCenterY = ddraig.net.entropica.item.AstralLinkingWandItem.getTargetCenterY(level, targetPos);
            double dx = (targetPos.getX() + 0.5) - (worldPosition.getX() + 0.5);
            double dy = (targetPos.getY() + targetCenterY) - (worldPosition.getY() + 0.5625);
            double dz = (targetPos.getZ() + 0.5) - (worldPosition.getZ() + 0.5);
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float targetYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            if (targetYaw < 0) targetYaw += 360.0f;
            float targetPitch = (float) Math.toDegrees(Math.atan2(dy, distXZ));

            setFocus(targetYaw, targetPitch, "Target: " + targetPos.toShortString(), true);
        }
    }

    public void receiveRelayBeam(BlockPos sourcePos, String starName) {
        boolean wasReceiving = this.isReceiving;
        this.isReceiving = true;
        this.activeStarName = starName != null ? starName : "Uncalibrated";
        this.receivingTicksLeft = 20;
        if (!wasReceiving && level != null && !level.isClientSide()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SecondaryAstralLensBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        boolean wasReceiving = be.isReceiving;
        if (be.receivingTicksLeft > 0) {
            be.receivingTicksLeft--;
            be.isReceiving = true;
        } else {
            be.isReceiving = false;
        }

        if (wasReceiving != be.isReceiving) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (be.isReceiving) {
            // Calculate pointing direction vector from yaw and pitch
            float yawRad = (float) Math.toRadians(be.yaw);
            float pitchRad = (float) Math.toRadians(be.pitch);

            double vx = -Math.sin(yawRad) * Math.cos(pitchRad);
            double vy = Math.sin(pitchRad);
            double vz = Math.cos(yawRad) * Math.cos(pitchRad);
            Vec3 lookDir = new Vec3(vx, vy, vz).normalize();

            Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5625, pos.getZ() + 0.5);
            double maxDist = 32.0;
            Vec3 end = start.add(lookDir.scale(maxDist));

            double closestDist = maxDist;
            BlockEntity closestBE = null;
            BlockPos hitSolidPos = null;
            Set<BlockPos> checkedPositions = new HashSet<>();

            for (double step = 0.5; step <= maxDist; step += 0.15) {
                Vec3 samplePoint = start.add(lookDir.scale(step));
                BlockPos p = BlockPos.containing(samplePoint);
                if (!checkedPositions.add(p)) continue;
                if (p.equals(pos) || p.equals(pos.below())) continue;

                BlockState blockState = level.getBlockState(p);
                net.minecraft.world.level.block.Block block = blockState.getBlock();
                BlockEntity targetBE = level.getBlockEntity(p);

                // Check if beam intersects an Astral Collector's floating crystal (centered at collector.getY() + 1.20)
                BlockPos collectorPos = p.below();
                BlockEntity belowBE = level.getBlockEntity(collectorPos);
                if (belowBE instanceof AstralCollectorBlockEntity collector) {
                    Vec3 crystalCenter = new Vec3(collectorPos.getX() + 0.5, collectorPos.getY() + 1.20, collectorPos.getZ() + 0.5);
                    if (samplePoint.distanceTo(crystalCenter) <= 0.45 || (samplePoint.y <= collectorPos.getY() + 1.25 && Math.abs(samplePoint.x - crystalCenter.x) <= 0.35 && Math.abs(samplePoint.z - crystalCenter.z) <= 0.35)) {
                        closestDist = start.distanceTo(crystalCenter);
                        closestBE = collector;
                        break;
                    }
                }

                if (targetBE != null) {
                    if (targetBE instanceof AstralCollectorBlockEntity collector) {
                        Vec3 crystalCenter = new Vec3(p.getX() + 0.5, p.getY() + 1.20, p.getZ() + 0.5);
                        closestDist = start.distanceTo(crystalCenter);
                        closestBE = targetBE;
                        break;
                    }

                    boolean isOptic = (targetBE instanceof SecondaryAstralLensBlockEntity ||
                                       targetBE instanceof RefractiveAstralLensBlockEntity ||
                                       targetBE instanceof BeamSplitterPrismBlockEntity ||
                                       targetBE instanceof AstralInfusionPedestalBlockEntity ||
                                       targetBE instanceof OpticalTransmitterPortBlockEntity ||
                                       targetBE instanceof OpticReceiverBlockEntity);

                    if (isOptic) {
                        double centerY = ((targetBE instanceof SecondaryAstralLensBlockEntity || targetBE instanceof RefractiveAstralLensBlockEntity) ? 0.5625 : 
                                         (targetBE instanceof AstralInfusionPedestalBlockEntity ? 1.1 : 0.5));
                        Vec3 targetCenter = new Vec3(p.getX() + 0.5, p.getY() + centerY, p.getZ() + 0.5);
                        closestDist = Math.min(step, start.distanceTo(targetCenter));
                        closestBE = targetBE;
                        break;
                    }
                }

                if (block instanceof PureOpticFiberBlock) {
                    Vec3 fiberCenter = new Vec3(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5);
                    closestDist = Math.min(step, start.distanceTo(fiberCenter));
                    closestBE = null;
                    OpticalTransmitterPortBlockEntity.propagateFiberNetwork(level, p, be.activeStarName);
                    break;
                }

                if (RefractiveAstralLensBlockEntity.isOpticalPassthrough(level, p, blockState)) {
                    continue;
                }

                net.minecraft.world.phys.shapes.VoxelShape shape = blockState.getCollisionShape(level, p);
                if (shape.isEmpty()) shape = blockState.getShape(level, p);
                BlockHitResult bHit = shape.clip(start, end, p);
                if (bHit != null && bHit.getType() == HitResult.Type.BLOCK) {
                    closestDist = start.distanceTo(bHit.getLocation());
                } else {
                    closestDist = step;
                }
                closestBE = null;
                hitSolidPos = p;
                break;
            }

            // 3. Raycast for Living Entities intersecting the beam
            AABB beamBounds = new AABB(start, start.add(lookDir.scale(closestDist))).inflate(0.4);
            LivingEntity blockingEntity = null;
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, beamBounds, e -> !e.isSpectator() && e.isAlive())) {
                AABB entityBox = living.getBoundingBox().inflate(0.1);
                Optional<Vec3> optHit = entityBox.clip(start, end);
                if (optHit.isPresent()) {
                    double entityDist = start.distanceTo(optHit.get());
                    if (entityDist < closestDist) {
                        closestDist = entityDist;
                        blockingEntity = living;
                        closestBE = null;
                    }
                }
            }

            // 4. Check for Calcite Block Transmutation (after 5 seconds / 100 ticks of direct beam contact)
            if (blockingEntity == null && closestBE == null && hitSolidPos != null) {
                BlockPos hitBlockPos = hitSolidPos;
                if (level.getBlockState(hitBlockPos).is(Blocks.CALCITE)) {
                    if (hitBlockPos.equals(be.calciteBlockPos)) {
                        be.calciteTransmuteTicks++;
                        if (level instanceof ServerLevel serverLevel) {
                            if (be.calciteTransmuteTicks % 4 == 0) {
                                serverLevel.sendParticles(ParticleTypes.ENCHANT, hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.05);
                                serverLevel.sendParticles(ParticleTypes.END_ROD, hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5, 1, 0.1, 0.1, 0.1, 0.02);
                            }
                            if (be.calciteTransmuteTicks >= 100) { // 5.0 seconds (100 ticks)
                                level.destroyBlock(hitBlockPos, false);
                                EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(be.activeStarName);
                                ItemStack orbStack = new ItemStack(ModItems.AVERAGE_ESSENCE.get());
                                EssenceItem.setEssenceType(orbStack, starEssence);
                                EssenceOrbEntity orb = new EssenceOrbEntity(
                                        level, hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5, orbStack
                                );
                                level.addFreshEntity(orb);
                                serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5, 24, 0.4, 0.4, 0.4, 0.15);
                                serverLevel.sendParticles(ParticleTypes.END_ROD, hitBlockPos.getX() + 0.5, hitBlockPos.getY() + 0.5, hitBlockPos.getZ() + 0.5, 12, 0.3, 0.3, 0.3, 0.1);
                                serverLevel.playSound(null, hitBlockPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.3f, 1.0f);
                                serverLevel.playSound(null, hitBlockPos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.9f, 1.5f);
                                be.calciteTransmuteTicks = 0;
                                be.calciteBlockPos = null;
                            }
                        }
                    } else {
                        be.calciteBlockPos = hitBlockPos;
                        be.calciteTransmuteTicks = 1;
                    }
                } else {
                    be.calciteTransmuteTicks = 0;
                    be.calciteBlockPos = null;
                }
            } else {
                be.calciteTransmuteTicks = 0;
                be.calciteBlockPos = null;
            }

            // 5. Check for Dropped Calcite ItemEntity Transmutation (after 5 seconds / 100 ticks)
            ItemEntity targetCalciteItem = null;
            for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, beamBounds, e -> e.isAlive() && e.getItem().is(Items.CALCITE))) {
                AABB itemBox = itemEntity.getBoundingBox().inflate(0.15);
                Optional<Vec3> hitOpt = itemBox.clip(start, end);
                if (hitOpt.isPresent() && start.distanceTo(hitOpt.get()) <= closestDist) {
                    targetCalciteItem = itemEntity;
                    break;
                }
            }

            if (targetCalciteItem != null) {
                if (be.calciteItemEntityId == targetCalciteItem.getId()) {
                    be.calciteItemTicks++;
                    if (level instanceof ServerLevel serverLevel) {
                        if (be.calciteItemTicks % 4 == 0) {
                            serverLevel.sendParticles(ParticleTypes.ENCHANT, targetCalciteItem.getX(), targetCalciteItem.getY() + 0.2, targetCalciteItem.getZ(), 3, 0.2, 0.2, 0.2, 0.05);
                        }
                        if (be.calciteItemTicks >= 100) {
                            ItemStack stack = targetCalciteItem.getItem();
                            stack.shrink(1);
                            if (stack.isEmpty()) {
                                targetCalciteItem.discard();
                            } else {
                                targetCalciteItem.setItem(stack);
                            }
                            EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(be.activeStarName);
                            ItemStack orbStack = new ItemStack(ModItems.AVERAGE_ESSENCE.get());
                            EssenceItem.setEssenceType(orbStack, starEssence);
                            EssenceOrbEntity orb = new EssenceOrbEntity(
                                    level, targetCalciteItem.getX(), targetCalciteItem.getY() + 0.2, targetCalciteItem.getZ(), orbStack
                            );
                            level.addFreshEntity(orb);
                            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, targetCalciteItem.getX(), targetCalciteItem.getY() + 0.2, targetCalciteItem.getZ(), 16, 0.3, 0.3, 0.3, 0.1);
                            serverLevel.playSound(null, targetCalciteItem.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.2f, 1.0f);
                            serverLevel.playSound(null, targetCalciteItem.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.8f, 1.4f);
                            be.calciteItemTicks = 0;
                            be.calciteItemEntityId = -1;
                        }
                    }
                } else {
                    be.calciteItemEntityId = targetCalciteItem.getId();
                    be.calciteItemTicks = 1;
                }
            } else {
                be.calciteItemTicks = 0;
                be.calciteItemEntityId = -1;
            }

            // 6. Propagate optical effects exclusively to the closest interceptor
            EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(be.activeStarName);
            if (blockingEntity != null) {
                blockingEntity.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 100, 0, false, true, true));
                ModAttachments.setToxicitySource(blockingEntity, starEssence.name());
                if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 10 == 0) {
                    serverLevel.playSound(null, blockingEntity.getX(), blockingEntity.getY() + 0.5, blockingEntity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.8f);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, blockingEntity.getX(), blockingEntity.getY() + 1.0, blockingEntity.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                }
            } else if (closestBE != null) {
                if (closestBE instanceof RefractiveAstralLensBlockEntity lens) {
                    lens.receiveRelayBeam(pos, be.activeStarName);
                } else if (closestBE instanceof SecondaryAstralLensBlockEntity otherSecondaryLens) {
                    otherSecondaryLens.receiveRelayBeam(pos, be.activeStarName);
                } else if (closestBE instanceof BeamSplitterPrismBlockEntity prism) {
                    prism.receiveRelayedBeam(pos, be.activeStarName);
                } else if (closestBE instanceof AstralInfusionPedestalBlockEntity pedestal) {
                    pedestal.receiveIrradiation(be.activeStarName);
                } else if (closestBE instanceof OpticalTransmitterPortBlockEntity transmitter) {
                    transmitter.receiveOpticalBeam(be.activeStarName);
                } else if (closestBE instanceof OpticReceiverBlockEntity receiver) {
                    receiver.receiveOpticalBeam(be.activeStarName);
                } else if (closestBE instanceof AstralCollectorBlockEntity collector) {
                    collector.receiveStarlightBeam(be.activeStarName, starEssence);
                }
            }

            be.beamDistance = (float) closestDist;
            if (Math.abs(be.prevBeamDistance - be.beamDistance) > 0.05f || level.getGameTime() % 10 == 0) {
                be.prevBeamDistance = be.beamDistance;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        } else {
            be.calciteTransmuteTicks = 0;
            be.calciteBlockPos = null;
            be.calciteItemTicks = 0;
            be.calciteItemEntityId = -1;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Yaw", Codec.FLOAT, this.yaw);
        output.store("Pitch", Codec.FLOAT, this.pitch);
        output.store("IsReceiving", Codec.BOOL, this.isReceiving);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("ReceivingTicksLeft", Codec.INT, this.receivingTicksLeft);
        output.store("BeamDistance", Codec.FLOAT, this.beamDistance);
        output.store("TargetName", Codec.STRING, this.targetName);
        output.store("IsFocused", Codec.BOOL, this.isFocused);
        if (this.targetPos != null) {
            output.store("TargetPos", BlockPos.CODEC, this.targetPos);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Yaw", Codec.FLOAT).ifPresent(y -> this.yaw = y);
        input.read("Pitch", Codec.FLOAT).ifPresent(p -> this.pitch = p);
        input.read("IsReceiving", Codec.BOOL).ifPresent(r -> this.isReceiving = r);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("ReceivingTicksLeft", Codec.INT).ifPresent(t -> this.receivingTicksLeft = t);
        input.read("BeamDistance", Codec.FLOAT).ifPresent(d -> {
            this.beamDistance = d;
            this.prevBeamDistance = d;
        });
        input.read("TargetName", Codec.STRING).ifPresent(n -> this.targetName = n);
        input.read("IsFocused", Codec.BOOL).ifPresent(f -> this.isFocused = f);
        input.read("TargetPos", BlockPos.CODEC).ifPresent(tp -> this.targetPos = tp);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) {
        return this.saveWithoutMetadata(p);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
