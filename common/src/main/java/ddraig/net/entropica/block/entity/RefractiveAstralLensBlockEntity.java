package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class RefractiveAstralLensBlockEntity extends BlockEntity {

    private float yaw = 0.0f;
    private float pitch = 45.0f;
    private float prevYaw = 0.0f;
    private float prevPitch = 45.0f;
    private String targetName = "Uncalibrated";
    private boolean isFocused = false;

    // Optical Relay & Beam Passing
    private boolean isRelaying = false;
    private String relayedStarName = null;
    private int relayTicksLeft = 0;
    private float beamDistance = 16.0f;

    public RefractiveAstralLensBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REFRACTIVE_ASTRAL_LENS_BE.get(), pos, state);
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

    public boolean isRelaying() {
        return isRelaying;
    }

    public float getBeamDistance() {
        return beamDistance;
    }

    public boolean isBeamActive() {
        return isFocused || (isRelaying && relayedStarName != null);
    }

    public String getActiveStarName() {
        if (isFocused && targetName != null && !targetName.isBlank()) {
            return targetName;
        }
        if (isRelaying && relayedStarName != null && !relayedStarName.isBlank()) {
            return relayedStarName;
        }
        return "Uncalibrated";
    }

    public void setFocus(float yaw, float pitch, String targetName, boolean isFocused) {
        this.prevYaw = this.yaw;
        this.prevPitch = this.pitch;
        this.yaw = yaw;
        this.pitch = pitch;
        this.targetName = (targetName != null && !targetName.isBlank()) ? targetName : "Uncalibrated";
        this.isFocused = isFocused;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public void receiveRelayBeam(BlockPos sourcePos, String starName) {
        if (this.isFocused) return; // Manual sky focus takes precedence
        this.isRelaying = true;
        this.relayedStarName = starName;
        this.relayTicksLeft = 10;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, RefractiveAstralLensBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        if (be.relayTicksLeft > 0) {
            be.relayTicksLeft--;
            if (be.relayTicksLeft == 0) {
                be.isRelaying = false;
                be.relayedStarName = null;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        boolean active = be.isBeamActive();
        if (active) {
            String currentStar = be.getActiveStarName();
            float yawRad = (float) Math.toRadians(be.yaw);
            float pitchRad = (float) Math.toRadians(-be.pitch);
            Vec3 lookDir = new Vec3(
                    -Mth.sin(yawRad) * Mth.cos(pitchRad),
                    -Mth.sin(pitchRad),
                    Mth.cos(yawRad) * Mth.cos(pitchRad)
            );
            Vec3 start = Vec3.atCenterOf(pos).add(0, 0.4375, 0);
            Vec3 end = start.add(lookDir.scale(16.0));
            BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, net.minecraft.world.phys.shapes.CollisionContext.empty()));
            double dist = 16.0;
            if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(pos) && !hit.getBlockPos().equals(pos.below())) {
                dist = start.distanceTo(hit.getLocation());
                BlockPos hitPos = hit.getBlockPos();
                if (level.getBlockEntity(hitPos) instanceof RefractiveAstralLensBlockEntity nextLens) {
                    nextLens.receiveRelayBeam(pos, currentStar);
                }
            }

            if (Math.abs(be.beamDistance - (float) dist) > 0.1f) {
                be.beamDistance = (float) dist;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        } else {
            if (be.beamDistance != 0.0f) {
                be.beamDistance = 0.0f;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }
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
        input.read("BeamDistance", Codec.FLOAT).ifPresent(d -> this.beamDistance = d);
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
