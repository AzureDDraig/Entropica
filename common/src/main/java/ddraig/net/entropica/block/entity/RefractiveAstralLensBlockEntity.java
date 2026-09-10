package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.astral.CelestialStarHelper;
import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.astral.ModConstellations;
import ddraig.net.entropica.astral.SupernovaEvent;
import ddraig.net.entropica.astral.SupernovaManager;
import ddraig.net.entropica.astral.SupernovaPhase;
import ddraig.net.entropica.block.PureOpticFiberBlock;
import ddraig.net.entropica.block.RefractiveAstralLensBlock;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RefractiveAstralLensBlockEntity extends BlockEntity {

    private float yaw = 0.0f;
    private float pitch = 0.0f;
    private float prevYaw = 0.0f;
    private float prevPitch = 0.0f;
    private String targetName = "Uncalibrated";
    private boolean isFocused = false;
    private boolean isRelaying = false;
    private String relayedStarName = null;
    private int relayTimeout = 0;
    private int incomingLinksCount = 0;
    private float beamDistance = 16.0f;
    private float prevBeamDistance = 16.0f;

    // Calcite Transmutation Tracking (5 seconds / 100 ticks)
    private BlockPos calciteBlockPos = null;
    private int calciteTransmuteTicks = 0;
    private int calciteItemEntityId = -1;
    private int calciteItemTicks = 0;

    public RefractiveAstralLensBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFRACTIVE_ASTRAL_LENS_BE.get(), pos, state);
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

    public String getTargetName() {
        return targetName;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public boolean isBeamActive() {
        return isFocused && (beamDistance > 0.1f);
    }

    public boolean isRelaying() {
        return isRelaying;
    }

    public String getActiveStarName() {
        if (this.isRelaying && this.relayedStarName != null) {
            return this.relayedStarName;
        }
        return this.targetName;
    }

    public int getIncomingLinksCount() {
        return incomingLinksCount;
    }

    public float getBeamDistance() {
        return beamDistance;
    }

    public void setFocus(float yaw, float pitch, String targetName, boolean isFocused) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.yaw = (yaw % 360.0f + 360.0f) % 360.0f;
        this.pitch = Math.max(-85.0f, Math.min(85.0f, pitch));
        this.targetName = targetName != null ? targetName : "Uncalibrated";
        this.isFocused = isFocused;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void receiveRelayBeam(BlockPos sourcePos, String starName) {
        this.isRelaying = true;
        this.relayedStarName = starName;
        this.relayTimeout = 10;
        this.isFocused = true;
        setChanged();
    }

    public void setIncomingLinksCount(int count) {
        this.incomingLinksCount = count;
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RefractiveAstralLensBlockEntity be) {
        be.prevYaw = be.yaw;
        be.prevPitch = be.pitch;

        if (be.relayTimeout > 0) {
            be.relayTimeout--;
            if (be.relayTimeout == 0) {
                be.isRelaying = false;
                be.relayedStarName = null;
                be.setChanged();
                if (!level.isClientSide()) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
        }

        if (level.isClientSide()) {
            return;
        }

        boolean hasActiveBeam = be.isFocused;
        String currentStar = be.getActiveStarName();

        if (hasActiveBeam) {
            float yawRad = (float) Math.toRadians(be.yaw);
            float pitchRad = (float) Math.toRadians(-be.pitch);
            Vec3 lookDir = new Vec3(
                    -Mth.sin(yawRad) * Mth.cos(pitchRad),
                    -Mth.sin(pitchRad),
                    Mth.cos(yawRad) * Mth.cos(pitchRad)
            ).normalize();

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
                    OpticalTransmitterPortBlockEntity.propagateFiberNetwork(level, p, currentStar);
                    break;
                }

                if (isOpticalPassthrough(level, p, blockState)) {
                    continue;
                }

                net.minecraft.world.phys.shapes.VoxelShape shape = blockState.getCollisionShape(level, p);
                if (shape.isEmpty()) shape = blockState.getShape(level, p);
                BlockHitResult hit = shape.clip(start, end, p);
                if (hit != null && hit.getType() == HitResult.Type.BLOCK) {
                    closestDist = start.distanceTo(hit.getLocation());
                } else {
                    closestDist = step;
                }
                closestBE = null;
                hitSolidPos = p;
                break;
            }

            // 3. Scan for Living Entities intercepting the beam
            AABB beamBounds = new AABB(start, start.add(lookDir.scale(closestDist))).inflate(0.4);
            LivingEntity blockingEntity = null;
            for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, beamBounds, e -> !e.isSpectator() && e.isAlive())) {
                AABB entityBox = living.getBoundingBox().inflate(0.12);
                Optional<Vec3> entityHit = entityBox.clip(start, end);
                if (entityHit.isPresent()) {
                    double entityDist = start.distanceTo(entityHit.get());
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
                                EssenceType starEssence = resolveStarEssence(currentStar);
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
                            EssenceType starEssence = resolveStarEssence(currentStar);
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
            if (blockingEntity != null) {
                EssenceType starEssence = resolveStarEssence(currentStar);
                blockingEntity.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 100, 0, false, true, true));
                ModAttachments.setToxicitySource(blockingEntity, starEssence.name());
                if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 10 == 0) {
                    serverLevel.playSound(null, blockingEntity.getX(), blockingEntity.getY() + 0.5, blockingEntity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.8f);
                    serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, blockingEntity.getX(), blockingEntity.getY() + 1.0, blockingEntity.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                }
            } else if (closestBE != null) {
                if (closestBE instanceof RefractiveAstralLensBlockEntity otherLens) {
                    otherLens.receiveRelayBeam(pos, currentStar);
                } else if (closestBE instanceof SecondaryAstralLensBlockEntity otherSecondaryLens) {
                    otherSecondaryLens.receiveRelayBeam(pos, currentStar);
                } else if (closestBE instanceof BeamSplitterPrismBlockEntity prism) {
                    prism.receiveRelayedBeam(pos, currentStar);
                } else if (closestBE instanceof AstralInfusionPedestalBlockEntity pedestal) {
                    pedestal.receiveIrradiation(currentStar);
                } else if (closestBE instanceof OpticalTransmitterPortBlockEntity transmitter) {
                    transmitter.receiveOpticalBeam(currentStar);
                } else if (closestBE instanceof OpticReceiverBlockEntity receiver) {
                    receiver.receiveOpticalBeam(currentStar);
                } else if (closestBE instanceof AstralCollectorBlockEntity collector) {
                    EssenceType starEssence = resolveStarEssence(currentStar);
                    collector.receiveStarlightBeam(currentStar, starEssence);
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
            if (be.beamDistance != 0.0f) {
                be.beamDistance = 0.0f;
                be.prevBeamDistance = 0.0f;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    public static boolean isOpticalPassthrough(Level level, BlockPos p, BlockState state) {
        if (state.isAir()) return true;
        net.minecraft.world.level.block.Block block = state.getBlock();
        if (block instanceof net.minecraft.world.level.block.TransparentBlock) return true; // Glass, tinted glass
        if (block instanceof net.minecraft.world.level.block.IronBarsBlock) return true; // Glass panes, iron bars
        if (state.is(net.minecraft.tags.BlockTags.IMPERMEABLE)) return true;
        if (block.getDescriptionId().contains("glass")) return true;
        if (state.getCollisionShape(level, p).isEmpty()) return true; // Foliage, torches, string, etc.
        return false;
    }

    public static EssenceType resolveStarEssence(String starName) {
        if (starName == null || starName.isEmpty() || starName.equals("Uncalibrated")) {
            return EssenceType.ASTRAL;
        }
        String lower = starName.toLowerCase();

        // 1. Supernova Remnants & Instabilities
        for (SupernovaEvent se : SupernovaManager.getAllEvents()) {
            if (se.remnantTitle().equalsIgnoreCase(starName) || se.starNodeId().equalsIgnoreCase(starName)) {
                return (se.phase() == SupernovaPhase.REMNANT) ? se.remnantEssence() : se.progenitorEssence();
            }
        }

        // 2. Wandering Planets (Locked)
        if (lower.contains("sylva")) return EssenceType.VITAE;
        if (lower.contains("tartarus")) return EssenceType.MAGMA;
        if (lower.contains("aurelia")) return EssenceType.RADIANT;
        if (lower.contains("noxus")) return EssenceType.UMBRAL;
        if (lower.contains("chiron")) return EssenceType.EARTH;

        // 3. Comets (Locked)
        if (lower.contains("zephyros")) return EssenceType.STORM;
        if (lower.contains("borealis")) return EssenceType.GLACIAL;
        if (lower.contains("ouroboros")) return EssenceType.PYRE;

        // 4. Constellations & Constellation Star Nodes (Locked)
        for (Constellation c : ModConstellations.getAllConstellations()) {
            if (lower.contains(c.getId().getPath().replace("_", " ").toLowerCase()) ||
                lower.contains(c.getId().getPath().toLowerCase())) {
                return c.getEssenceType();
            }
        }

        // 5. Landmark Stars (Randomized)
        for (CelestialStarHelper.LandmarkStar ls : CelestialStarHelper.LANDMARK_STARS) {
            if (ls.name().equalsIgnoreCase(starName)) {
                return ls.essenceType();
            }
        }

        // 6. Ambient Stars (960 stars with randomly chosen essence types)
        for (CelestialStarHelper.AmbientStar as : CelestialStarHelper.AMBIENT_STARS) {
            if (as.name().equalsIgnoreCase(starName)) {
                return as.essenceType();
            }
        }

        return EssenceType.ASTRAL;
    }

    public float getInterpolatedYaw(float partialTick) {
        return Mth.rotLerp(partialTick, this.prevYaw, this.yaw);
    }

    public float getInterpolatedPitch(float partialTick) {
        return Mth.lerp(partialTick, this.prevPitch, this.pitch);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Yaw", Codec.FLOAT, this.yaw);
        output.store("Pitch", Codec.FLOAT, this.pitch);
        output.store("TargetName", Codec.STRING, this.targetName);
        output.store("IsFocused", Codec.BOOL, this.isFocused);
        output.store("IsRelaying", Codec.BOOL, this.isRelaying);
        output.store("IncomingLinksCount", Codec.INT, this.incomingLinksCount);
        output.store("BeamDistance", Codec.FLOAT, this.beamDistance);
        if (this.relayedStarName != null) {
            output.store("RelayedStarName", Codec.STRING, this.relayedStarName);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("Yaw", Codec.FLOAT).ifPresent(y -> {
            this.yaw = y;
            this.prevYaw = y;
        });
        input.read("Pitch", Codec.FLOAT).ifPresent(p -> {
            this.pitch = p;
            this.prevPitch = p;
        });
        input.read("TargetName", Codec.STRING).ifPresent(tn -> this.targetName = tn);
        input.read("IsFocused", Codec.BOOL).ifPresent(f -> this.isFocused = f);
        input.read("IsRelaying", Codec.BOOL).ifPresent(r -> this.isRelaying = r);
        input.read("IncomingLinksCount", Codec.INT).ifPresent(c -> this.incomingLinksCount = c);
        input.read("BeamDistance", Codec.FLOAT).ifPresent(d -> {
            this.beamDistance = d;
            this.prevBeamDistance = d;
        });
        input.read("RelayedStarName", Codec.STRING).ifPresent(rn -> this.relayedStarName = rn);
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
