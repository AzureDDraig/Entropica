package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BeamSplitterPrismBlockEntity extends BlockEntity {

    private boolean isReceiving = false;
    private String activeStarName = "Uncalibrated";
    private int receivingTicksLeft = 0;
    private float rotationAngle = 0.0f;
    private float prevRotationAngle = 0.0f;

    // Two 90-degree split beam angles (in degrees) and distances
    private float leftSplitYaw = 90.0f;
    private float rightSplitYaw = 270.0f;
    private float leftRayDistance = 16.0f;
    private float rightRayDistance = 16.0f;

    // Explicit linked target positions
    private final List<BlockPos> linkedTargets = new ArrayList<>();

    public BeamSplitterPrismBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BEAM_SPLITTER_PRISM_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(64.0, 64.0, 64.0);
    }

    public boolean isReceiving() {
        return isReceiving;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public float getInterpolatedRotation(float partialTick) {
        return prevRotationAngle + (rotationAngle - prevRotationAngle) * partialTick;
    }

    public float getLeftSplitYaw() {
        return leftSplitYaw;
    }

    public float getRightSplitYaw() {
        return rightSplitYaw;
    }

    public float getLeftRayDistance() {
        return leftRayDistance;
    }

    public float getRightRayDistance() {
        return rightRayDistance;
    }

    public void receiveRelayedBeam(String starName) {
        receiveRelayedBeam(null, starName);
    }

    public void receiveRelayedBeam(BlockPos sourcePos, String starName) {
        boolean wasReceiving = this.isReceiving;
        this.isReceiving = true;
        this.activeStarName = starName != null ? starName : "Uncalibrated";
        this.receivingTicksLeft = 20;

        if (sourcePos != null) {
            double dx = worldPosition.getX() - sourcePos.getX();
            double dz = worldPosition.getZ() - sourcePos.getZ();
            if (dx != 0 || dz != 0) {
                float inputTravelYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
                if (inputTravelYaw < 0) inputTravelYaw += 360.0f;

                this.leftSplitYaw = (inputTravelYaw - 90.0f + 360.0f) % 360.0f;
                this.rightSplitYaw = (inputTravelYaw + 90.0f) % 360.0f;
            }
        } else if (level != null) {
            BlockState st = getBlockState();
            Direction facing = st.hasProperty(HorizontalDirectionalBlock.FACING)
                    ? st.getValue(HorizontalDirectionalBlock.FACING)
                    : Direction.NORTH;
            float facingYaw = facing.toYRot();
            this.leftSplitYaw = (facingYaw - 90.0f + 360.0f) % 360.0f;
            this.rightSplitYaw = (facingYaw + 90.0f) % 360.0f;
        }

        setChanged();
        if (!wasReceiving && level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void addLinkedTarget(BlockPos targetPos) {
        if (!linkedTargets.contains(targetPos)) {
            linkedTargets.add(targetPos);
            setChanged();
        }
    }

    public void clearLinkedTargets() {
        linkedTargets.clear();
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BeamSplitterPrismBlockEntity be) {
        be.prevRotationAngle = be.rotationAngle;

        if (level.isClientSide()) {
            if (be.isReceiving) {
                be.rotationAngle = (be.rotationAngle + 2.0f) % 360.0f;
            }
            return;
        }

        boolean wasReceiving = be.isReceiving;
        if (be.receivingTicksLeft > 0) {
            be.receivingTicksLeft--;
            be.isReceiving = true;
            be.rotationAngle = (be.rotationAngle + 2.0f) % 360.0f;
        } else {
            be.isReceiving = false;
        }

        if (wasReceiving != be.isReceiving) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }

        if (be.isReceiving) {
            EssenceType starEssence = RefractiveAstralLensBlockEntity.resolveStarEssence(be.activeStarName);
            float[] splitYaws = new float[]{be.leftSplitYaw, be.rightSplitYaw};

            for (int k = 0; k < 2; k++) {
                float splitYaw = splitYaws[k];
                float yawRad = (float) Math.toRadians(splitYaw);

                double vx = -Math.sin(yawRad);
                double vy = 0.0;
                double vz = Math.cos(yawRad);

                Vec3 start = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                double maxRayDist = 24.0;
                Vec3 end = start.add(vx * maxRayDist, vy, vz * maxRayDist);

                BlockHitResult hit = level.clip(new ClipContext(
                        start, end,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                ));

                BlockPos hitBlockPos = null;
                if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(pos)) {
                    hitBlockPos = hit.getBlockPos();
                    maxRayDist = start.distanceTo(hit.getLocation());
                }

                // Check for living entities intercepting the split ray
                AABB rayBounds = new AABB(start, start.add(vx * maxRayDist, 0, vz * maxRayDist)).inflate(0.4);
                LivingEntity blockingEntity = null;
                for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, rayBounds, e -> !e.isSpectator() && e.isAlive())) {
                    AABB entityBox = living.getBoundingBox().inflate(0.1);
                    Optional<Vec3> optHit = entityBox.clip(start, end);
                    if (optHit.isPresent()) {
                        double entityDist = start.distanceTo(optHit.get());
                        if (entityDist < maxRayDist) {
                            maxRayDist = entityDist;
                            blockingEntity = living;
                            hitBlockPos = null;
                        }
                    }
                }

                if (k == 0) {
                    be.leftRayDistance = (float) maxRayDist;
                } else {
                    be.rightRayDistance = (float) maxRayDist;
                }

                if (blockingEntity != null) {
                    blockingEntity.addEffect(new MobEffectInstance(ModEffects.MATERIA_TOXICITY, 100, 0, false, true, true));
                    ModAttachments.setToxicitySource(blockingEntity, starEssence.name());
                    if (level instanceof ServerLevel serverLevel && serverLevel.getGameTime() % 10 == 0) {
                        serverLevel.playSound(null, blockingEntity.getX(), blockingEntity.getY() + 0.5, blockingEntity.getZ(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.4f, 1.8f);
                        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, blockingEntity.getX(), blockingEntity.getY() + 1.0, blockingEntity.getZ(), 5, 0.2, 0.2, 0.2, 0.05);
                    }
                } else if (hitBlockPos != null) {
                    BlockEntity targetBE = level.getBlockEntity(hitBlockPos);
                    if (targetBE instanceof RefractiveAstralLensBlockEntity lens && !hitBlockPos.equals(pos)) {
                        lens.receiveRelayBeam(pos, be.activeStarName);
                    } else if (targetBE instanceof SecondaryAstralLensBlockEntity secondaryLens && !hitBlockPos.equals(pos)) {
                        secondaryLens.receiveRelayBeam(pos, be.activeStarName);
                    } else if (targetBE instanceof BeamSplitterPrismBlockEntity otherPrism && !hitBlockPos.equals(pos)) {
                        otherPrism.receiveRelayedBeam(pos, be.activeStarName);
                    } else if (targetBE instanceof AstralInfusionPedestalBlockEntity pedestal) {
                        pedestal.receiveIrradiation(be.activeStarName);
                    } else if (targetBE instanceof OpticalTransmitterPortBlockEntity transmitter) {
                        transmitter.receiveOpticalBeam(be.activeStarName);
                    } else if (targetBE instanceof AstralCollectorBlockEntity collector) {
                        collector.receiveStarlightBeam(be.activeStarName, starEssence);
                    }
                }
            }

            if (level.getGameTime() % 20 == 0) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsReceiving", Codec.BOOL, this.isReceiving);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("LeftSplitYaw", Codec.FLOAT, this.leftSplitYaw);
        output.store("RightSplitYaw", Codec.FLOAT, this.rightSplitYaw);
        output.store("LeftRayDistance", Codec.FLOAT, this.leftRayDistance);
        output.store("RightRayDistance", Codec.FLOAT, this.rightRayDistance);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("IsReceiving", Codec.BOOL).ifPresent(r -> this.isReceiving = r);
        input.read("ActiveStarName", Codec.STRING).ifPresent(s -> this.activeStarName = s);
        input.read("LeftSplitYaw", Codec.FLOAT).ifPresent(y -> this.leftSplitYaw = y);
        input.read("RightSplitYaw", Codec.FLOAT).ifPresent(y -> this.rightSplitYaw = y);
        input.read("LeftRayDistance", Codec.FLOAT).ifPresent(d -> this.leftRayDistance = d);
        input.read("RightRayDistance", Codec.FLOAT).ifPresent(d -> this.rightRayDistance = d);
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
