package ddraig.net.entropica.entity;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
        this.checkedThrower = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.isNoGravity()) this.setNoGravity(true);
        this.setDeltaMovement(this.getDeltaMovement().multiply(0.95D, 0.95D, 0.95D));

        if (this.isInWater()) {
            this.setDeltaMovement(this.getDeltaMovement().x, -0.03D, this.getDeltaMovement().z);
        }

        if (!this.level().isClientSide()) {

            // Auto-detect if thrown by a player on first tick
            if (!this.checkedThrower) {
                this.checkedThrower = true;
                if (this.getOwner() instanceof Player) {
                    this.thrownByPlayer = true;
                }
            }

            if (this.thrownByPlayer && this.isInWater()) {
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
                    BlockPos pos = this.blockPosition();

                    if (!this.level().getBlockState(pos).is(Blocks.WATER)) {
                        pos = pos.below();
                    }

                    BlockState state = this.level().getBlockState(pos);

                    if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {

                        EssenceType type = this.getEssenceType();
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

                        ItemStack stack = this.getItem();
                        stack.shrink(1);

                        if (stack.isEmpty()) {
                            this.discard();
                        } else {
                            this.setItem(stack);
                            this.waterTimer = 0;
                        }
                    }
                }
            } else if (!this.isInWater()) {
                if (this.waterTimer > 0) {
                    this.waterTimer--;
                }
            }
        }
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt("WaterTimer", this.waterTimer);
        output.putBoolean("CheckedThrower", this.checkedThrower);
        output.putBoolean("ThrownByPlayer", this.thrownByPlayer);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.waterTimer = input.getIntOr("WaterTimer", 0);
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