package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public class EnrichmentTableBlockEntity extends BlockEntity {

    private ItemStack heldItem = ItemStack.EMPTY;
    private float glowIntensity = 0.0f;
    private int glowColor = 0xFFFFFF;
    private boolean isFadingOut = false;

    public EnrichmentTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENRICHMENT_TABLE_BE.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide() && this.glowIntensity > 0.0f) {
            if (level.random.nextFloat() < this.glowIntensity) {
                float r = ((this.glowColor >> 16) & 0xFF) / 255.0f;
                float g = ((this.glowColor >> 8) & 0xFF) / 255.0f;
                float b = (this.glowColor & 0xFF) / 255.0f;

                level.addParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, r, g, b),
                        pos.getX() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.3D,
                        pos.getY() + 1.2D + (level.random.nextDouble() * 0.3D),
                        pos.getZ() + 0.5D + (level.random.nextDouble() - 0.5D) * 0.3D,
                        0.0D, 0.01D, 0.0D);
            }

            if (this.isFadingOut) {
                this.glowIntensity -= 0.015f;
                if (this.glowIntensity <= 0.0f) {
                    this.glowIntensity = 0.0f;
                    this.isFadingOut = false;
                }
            }
        }
    }

    public void setProcessingGlow(float intensity, int color) {
        this.glowIntensity = intensity;
        this.glowColor = color;
        this.isFadingOut = false;
        if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public void triggerCompletionGlow(int color) {
        this.glowIntensity = 1.0f;
        this.glowColor = color;
        this.isFadingOut = true;
        if (this.level != null && !this.level.isClientSide()) this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    public float getGlowIntensity() {
        return glowIntensity;
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack stack) {
        this.heldItem = stack;
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (!this.heldItem.isEmpty() && this.level != null) {
            Block.popResource(this.level, pos, this.heldItem);
            this.heldItem = ItemStack.EMPTY;
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.heldItem.isEmpty()) {
            output.store("HeldItem", ItemStack.OPTIONAL_CODEC, this.heldItem);
        }
        output.store("GlowIntensity", Codec.FLOAT, this.glowIntensity);
        output.store("GlowColor", Codec.INT, this.glowColor);
        output.store("IsFading", Codec.BOOL, this.isFadingOut);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.heldItem = input.read("HeldItem", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.glowIntensity = input.read("GlowIntensity", Codec.FLOAT).orElse(0.0f);
        this.glowColor = input.read("GlowColor", Codec.INT).orElse(0xFFFFFF);
        this.isFadingOut = input.read("IsFading", Codec.BOOL).orElse(false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}