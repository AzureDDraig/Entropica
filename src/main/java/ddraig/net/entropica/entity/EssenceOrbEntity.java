package ddraig.net.entropica.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.entity.DilutedEssenceFluidBlockEntity;
import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EssenceOrbEntity extends ItemEntity {

    private int waterTimer = 0;
    private int rejectCooldown = 0;
    private boolean checkedThrower = false;
    private boolean thrownByPlayer = false;

    public EssenceOrbEntity(EntityType<? extends ItemEntity> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public EssenceOrbEntity(Level level, double x, double y, double z, ItemStack stack) {
        super(ModEntityTypes.ESSENCE_ORB.get(), level);
        this.setPos(x, y, z);
        this.setItem(stack);
        this.setNoGravity(true);
    }

    public void markThrownByPlayer() {
        this.thrownByPlayer = true;
    }

    public int getChargeAmount() {
        int tier = getTier();
        return switch (tier) {
            case 3 -> 32;
            case 2 -> 8;
            default -> 2;
        };
    }

    // --- NEW: Gets an ARGB Hex color based on the type of essence ---
    public int getEssenceColor() {
        return switch (getEssenceType()) {
            case UNDEAD -> 0xFF2B4A2B;    // Dark Green
            case WATER -> 0xFF3366FF;     // Blue
            case CHIMERA -> 0xFF8B4513;   // Brown
            case ARID -> 0xFFEEDD82;      // Sand/Gold
            case VOID -> 0xFF220044;      // Deep Purple/Black
            case AIR -> 0xFFAADDFF;       // Light Blue/Cyan
            case EARTH -> 0xFF556B2F;     // Earth Green
            case NATURE -> 0xFF32CD32;    // Bright Green
            case LIGHTNING -> 0xFFEEEE00; // Yellow
            case RADIANT -> 0xFFFFFFCC;   // Bright White
            case UMBRAL -> 0xFF1A1A1A;    // Near Black
            default -> 0xFFB200FF;        // Magic Purple
        };
    }

    private void rejectOrb() {
        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT, this.getX(), this.getY() + 0.2, this.getZ(), 15, 0.2, 0.2, 0.2, 0.1);
            serverLevel.playSound(null, this.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.5F, 2.0F);
        }

        Player nearestPlayer = null;
        double closestDist = 15.0D * 15.0D;

        for (Player player : this.level().players()) {
            double dist = player.distanceToSqr(this);
            if (dist < closestDist) {
                closestDist = dist;
                nearestPlayer = player;
            }
        }

        if (nearestPlayer != null) {
            double dX = nearestPlayer.getX() - this.getX();
            double dY = (nearestPlayer.getY() + nearestPlayer.getEyeHeight() / 2.0) - this.getY();
            double dZ = nearestPlayer.getZ() - this.getZ();

            double distance = Math.sqrt(dX * dX + dY * dY + dZ * dZ);
            if (distance > 0) {
                this.setDeltaMovement(dX / distance * 0.5, 0.4, dZ / distance * 0.5);
            } else {
                this.setDeltaMovement(0, 0.5, 0);
            }
        } else {
            this.setDeltaMovement(0, 0.5, 0);
        }

        this.rejectCooldown = 40;
        this.waterTimer = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.rejectCooldown > 0) {
            this.rejectCooldown--;
        }

        if (!this.isNoGravity()) this.setNoGravity(true);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.95D, 0.95D, 0.95D));

        boolean inFluid = this.isInWater() || this.level().getBlockState(this.blockPosition()).is(ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get());

        if (inFluid && this.rejectCooldown == 0) {
            this.setDeltaMovement(this.getDeltaMovement().x, -0.03D, this.getDeltaMovement().z);
        }

        if (!this.level().isClientSide()) {

            if (!this.thrownByPlayer && this.getOwner() instanceof Player) {
                this.thrownByPlayer = true;
            }

            if (this.thrownByPlayer && inFluid) {

                BlockPos pos = this.blockPosition();
                if (!this.level().getBlockState(pos).getFluidState().isSource()) {
                    pos = pos.below();
                }

                BlockState targetState = this.level().getBlockState(pos);
                EssenceType type = this.getEssenceType();

                // --- SCENARIO 1: RECHARGING AN EXISTING POOL ---
                if (targetState.is(ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get()) && targetState.getFluidState().isSource()) {
                    if (this.level().getBlockEntity(pos) instanceof DilutedEssenceFluidBlockEntity be) {

                        int maxCharge = EntropicaConfig.DILUTED_ESSENCE_MAX_CHARGE.get();

                        if (be.getCharge() >= maxCharge || type == EssenceType.NETHER || type == EssenceType.FROZEN) {
                            if (this.rejectCooldown == 0) rejectOrb();
                            return;
                        }

                        if (this.rejectCooldown == 0) {
                            be.addCharge(this.getChargeAmount());
                            be.setTintColor(this.getEssenceColor()); // Update the pool's color!

                            this.level().playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                            if (this.level() instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.WITCH, this.getX(), this.getY() + 0.2, this.getZ(), 10, 0.2, 0.2, 0.2, 0.05);
                            }
                            consumeOrb();
                        }
                    }
                    return;
                }

                // --- SCENARIO 2: CONVERTING PLAIN WATER ---
                if (targetState.is(Blocks.WATER) && targetState.getFluidState().isSource()) {
                    this.waterTimer++;

                    int bubbleRate = 20;
                    int bubbleAmount = 2;

                    if (this.waterTimer > 500) {
                        bubbleRate = 2;
                        bubbleAmount = 6;
                    } else if (this.waterTimer > 400) {
                        bubbleRate = 5;
                        bubbleAmount = 4;
                    } else if (this.waterTimer > 200) {
                        bubbleRate = 10;
                        bubbleAmount = 3;
                    }

                    if (this.waterTimer % bubbleRate == 0 && this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY() + 0.2, this.getZ(), bubbleAmount, 0.15, 0.15, 0.15, 0.02);
                    }

                    if (this.waterTimer >= 600) {
                        BlockState resultState;

                        switch (type) {
                            case NETHER -> {
                                resultState = Blocks.AIR.defaultBlockState();
                                this.level().playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0F, 1.0F);
                                if (this.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.05);
                                }
                            }
                            case FROZEN -> {
                                resultState = Blocks.BLUE_ICE.defaultBlockState();
                                this.level().playSound(null, pos, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
                                if (this.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.4, 0.4, 0.4, 0.1);
                                }
                            }
                            default -> {
                                resultState = ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get().defaultBlockState();
                                this.level().playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1.0F, 1.0F);
                                if (this.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 15, 0.2, 0.2, 0.2, 0.05);
                                    serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 20, 0.3, 0.3, 0.3, 0.1);
                                }
                            }
                        }

                        this.level().setBlock(pos, resultState, 3);

                        if (resultState.is(ModBlocks.DILUTED_ESSENCE_FLUID_BLOCK.get())) {
                            if (this.level().getBlockEntity(pos) instanceof DilutedEssenceFluidBlockEntity be) {
                                be.addCharge(this.getChargeAmount());
                                be.setTintColor(this.getEssenceColor()); // Set initial color!
                            }
                        }

                        consumeOrb();
                    }
                }
            } else if (!inFluid) {
                if (this.waterTimer > 0) {
                    this.waterTimer--;
                }
            }
        }
    }

    private void consumeOrb() {
        ItemStack stack = this.getItem();
        stack.shrink(1);

        if (stack.isEmpty()) {
            this.discard();
        } else {
            this.setItem(stack);
            this.waterTimer = 0;
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("WaterTimer", this.waterTimer);
        output.putInt("RejectCooldown", this.rejectCooldown);
        output.putBoolean("CheckedThrower", this.checkedThrower);
        output.putBoolean("ThrownByPlayer", this.thrownByPlayer);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.waterTimer = input.getIntOr("WaterTimer", 0);
        this.rejectCooldown = input.getIntOr("RejectCooldown", 0);
        this.checkedThrower = input.getBooleanOr("CheckedThrower", false);
        this.thrownByPlayer = input.getBooleanOr("ThrownByPlayer", false);
    }

    public int getTier() {
        String name = BuiltInRegistries.ITEM.getKey(this.getItem().getItem()).getPath();
        if (name.contains("strong")) return 3;
        if (name.contains("average")) return 2;
        return 1;
    }

    public EssenceType getEssenceType() {
        String name = BuiltInRegistries.ITEM.getKey(this.getItem().getItem()).getPath().toUpperCase();
        for (EssenceType type : EssenceType.values()) {
            if (name.contains(type.name())) {
                return type;
            }
        }
        return EssenceType.REGULAR;
    }
}