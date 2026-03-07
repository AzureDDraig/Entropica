package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.config.EntropicaConfig;
import ddraig.net.entropica.recipe.DilutedEssenceRecipe;
import ddraig.net.entropica.recipe.FluidCraftingInput;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DilutedEssenceFluidBlockEntity extends BlockEntity {

    private int charge = 0;
    private int tintColor = 0xFFB200FF;

    public DilutedEssenceFluidBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DILUTED_ESSENCE_FLUID_BE.get(), pos, state);
    }

    public void addCharge(int amount) {
        int maxCharge = EntropicaConfig.DILUTED_ESSENCE_MAX_CHARGE.get();
        this.charge += amount;
        if (this.charge > maxCharge) this.charge = maxCharge;
        this.setChanged();
    }

    public int getCharge() { return charge; }

    public void consumeCharge(int amount) {
        this.charge -= amount;
        if (this.charge < 0) this.charge = 0;
        this.setChanged();
    }

    public int getTintColor() { return tintColor; }

    public void setTintColor(int color) {
        this.tintColor = color;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public void tick() {
        if (this.level == null || this.level.isClientSide()) return;

        ServerLevel serverLevel = (ServerLevel) this.level;
        BlockPos pos = this.getBlockPos();

        // Expanded the detection box upwards to account for items bobbing on the surface
        AABB bounds = new AABB(pos).expandTowards(0, 0.5D, 0);
        List<ItemEntity> items = serverLevel.getEntitiesOfClass(ItemEntity.class, bounds);

        if (items.isEmpty()) return;

        List<ItemStack> stacks = items.stream().map(ItemEntity::getItem).filter(s -> !s.isEmpty()).toList();
        if (stacks.isEmpty()) return;

        FluidCraftingInput input = new FluidCraftingInput(stacks);

        Optional<RecipeHolder<DilutedEssenceRecipe>> recipeOpt = serverLevel.getServer().getRecipeManager()
                .getRecipeFor(ModRecipes.DILUTED_ESSENCE_FLUID_TYPE.get(), input, serverLevel);

        if (recipeOpt.isPresent()) {
            DilutedEssenceRecipe recipe = recipeOpt.get().value();

            if (this.charge >= recipe.chargeCost()) {
                List<ItemEntity> matchedEntities = new ArrayList<>();
                List<ItemEntity> availableEntities = new ArrayList<>(items);

                for (Ingredient ing : recipe.inputs()) {
                    for (int i = 0; i < availableEntities.size(); i++) {
                        ItemEntity ent = availableEntities.get(i);
                        if (ing.test(ent.getItem())) {
                            matchedEntities.add(ent);
                            availableEntities.remove(i);
                            break;
                        }
                    }
                }

                boolean allReady = true;

                for (ItemEntity ent : matchedEntities) {
                    // Dampen movement to counteract fluid buoyancy
                    ent.setDeltaMovement(ent.getDeltaMovement().multiply(0.95D, 0.5D, 0.95D));

                    int timeInFluid = ent.getPersistentData().getInt("EntropicaFluidTime").orElse(0);
                    timeInFluid++;

                    if (timeInFluid % 10 == 0) {
                        serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, ent.getX(), ent.getY() + 0.2, ent.getZ(), 1, 0.1, 0.1, 0.1, 0.02);
                    }

                    if (timeInFluid < recipe.processingTime()) {
                        allReady = false;
                    }
                    ent.getPersistentData().putInt("EntropicaFluidTime", timeInFluid);
                }

                if (allReady) {
                    int batchLimit = EntropicaConfig.DILUTED_ESSENCE_BATCH_LIMIT.get();
                    int maxCrafts = Math.min(this.charge / recipe.chargeCost(), batchLimit);

                    for (ItemEntity ent : matchedEntities) {
                        maxCrafts = Math.min(maxCrafts, ent.getItem().getCount());
                    }

                    if (maxCrafts > 0) {
                        this.consumeCharge(maxCrafts * recipe.chargeCost());

                        ItemStack outputStack = recipe.output().copy();
                        outputStack.setCount(outputStack.getCount() * maxCrafts);

                        ItemEntity resultEntity = new ItemEntity(serverLevel, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, outputStack);
                        resultEntity.setDeltaMovement(0, 0.2, 0);
                        serverLevel.addFreshEntity(resultEntity);

                        serverLevel.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.0F);
                        serverLevel.sendParticles(ParticleTypes.WITCH, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 30, 0.3, 0.3, 0.3, 0.1);

                        for (ItemEntity ent : matchedEntities) {
                            ent.getItem().shrink(maxCrafts);
                            if (ent.getItem().isEmpty()) {
                                ent.discard();
                            } else {
                                ent.setItem(ent.getItem());
                                ent.getPersistentData().putInt("EntropicaFluidTime", 0);
                            }
                        }
                    }
                }
            } else {
                if (serverLevel.getGameTime() % 60 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5, 2, 0.1, 0.1, 0.1, 0.01);
                }

                // Still dampen movement while waiting for power
                for (ItemEntity ent : items) {
                    ent.setDeltaMovement(ent.getDeltaMovement().multiply(0.95D, 0.5D, 0.95D));
                }
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("TintColor", this.tintColor);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Charge", this.charge);
        output.putInt("TintColor", this.tintColor);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.charge = input.getIntOr("Charge", 0);
        this.tintColor = input.getIntOr("TintColor", 0xFFB200FF);
    }
}