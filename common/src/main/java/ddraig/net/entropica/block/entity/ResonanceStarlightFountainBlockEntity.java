package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.starlight.IWirelessFluxReceiver;
import ddraig.net.entropica.block.ResonanceStarlightFountainBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public class ResonanceStarlightFountainBlockEntity extends BlockEntity implements IWirelessFluxReceiver {

    private String activeStarName = "Astral";
    private EssenceType activeEssence = EssenceType.ASTRAL;
    private int pulseTicksRemaining = 0;
    private int animationTicks = 0;

    public ResonanceStarlightFountainBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RESONANCE_STARLIGHT_FOUNTAIN_BE.get(), pos, state);
    }

    @Override
    public void receiveFluxPulse(String starName, EssenceType essence) {
        this.activeStarName = starName != null ? starName : "Astral";
        this.activeEssence = essence != null ? essence : EssenceType.ASTRAL;
        this.pulseTicksRemaining = 35;
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            if (current.hasProperty(ResonanceStarlightFountainBlock.LIT) && !current.getValue(ResonanceStarlightFountainBlock.LIT)) {
                level.setBlock(worldPosition, current.setValue(ResonanceStarlightFountainBlock.LIT, true), 3);
            }
        }
    }

    @Override
    public boolean isFluxPowered() {
        if (pulseTicksRemaining > 0) return true;
        BlockState state = getBlockState();
        return state.hasProperty(ResonanceStarlightFountainBlock.LIT) && state.getValue(ResonanceStarlightFountainBlock.LIT);
    }

    @Override
    public int getPulseTicksRemaining() {
        return pulseTicksRemaining;
    }

    @Override
    public BlockPos getReceiverPos() {
        return worldPosition;
    }

    public String getActiveStarName() {
        return activeStarName;
    }

    public EssenceType getActiveEssence() {
        return activeEssence;
    }

    public int getAnimationTicks() {
        return animationTicks;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ResonanceStarlightFountainBlockEntity be) {
        be.animationTicks++;

        if (be.pulseTicksRemaining > 0) {
            be.pulseTicksRemaining--;

            if (level.isClientSide()) {
                // Client-side ambient bubbling
                if (level.random.nextInt(35) == 0) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                            SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.4f, 1.4f, false);
                }
                if (level.random.nextInt(3) == 0) {
                    double px = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.20;
                    double py = pos.getY() + 1.55;
                    double pz = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.20;
                    level.addParticle(net.minecraft.core.particles.ParticleTypes.END_ROD, px, py, pz,
                            (level.random.nextDouble() - 0.5) * 0.02, -0.02, (level.random.nextDouble() - 0.5) * 0.02);
                }
            } else {
                // Server-side: Soothe entities standing in the basin
                if (be.animationTicks % 20 == 0) {
                    AABB basinBox = new AABB(pos).inflate(0.1, 0.5, 0.1);
                    for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, basinBox)) {
                        // Soothe harmful status effects
                        if (living.hasEffect(net.minecraft.world.effect.MobEffects.POISON)) {
                            living.removeEffect(net.minecraft.world.effect.MobEffects.POISON);
                        }
                        if (living.hasEffect(net.minecraft.world.effect.MobEffects.WITHER)) {
                            living.removeEffect(net.minecraft.world.effect.MobEffects.WITHER);
                        }
                    }
                }

                // Pathway A (Section 8.1): Starlight Condensation onto Glass submerged in powered Starlight Fountain -> Blank Spellgem
                AABB itemBasinBox = new AABB(pos).inflate(0.35, 0.75, 0.35);
                for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, itemBasinBox, ItemEntity::isAlive)) {
                    ItemStack stack = itemEntity.getItem();
                    if (stack.is(Items.GLASS) || stack.is(Items.GLASS_PANE) || stack.is(Items.TINTED_GLASS)) {
                        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
                        int nextTicks = tag.getInt("StarlightCondenseTicks").orElse(0) + 1;
                        if (nextTicks >= 100) {
                            itemEntity.setItem(new ItemStack(ModItems.BLANK_SPELLGEM.get(), stack.getCount()));
                            level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.45f);
                            if (level instanceof ServerLevel serverLevel) {
                                serverLevel.sendParticles(ParticleTypes.END_ROD,
                                        itemEntity.getX(), itemEntity.getY() + 0.2, itemEntity.getZ(),
                                        12, 0.15, 0.15, 0.15, 0.03);
                            }
                        } else {
                            tag.putInt("StarlightCondenseTicks", nextTicks);
                            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
                            itemEntity.setItem(stack);
                        }
                    }
                }
            }

            if (be.pulseTicksRemaining <= 0 && !level.isClientSide()) {
                if (state.hasProperty(ResonanceStarlightFountainBlock.LIT) && state.getValue(ResonanceStarlightFountainBlock.LIT)) {
                    level.setBlock(pos, state.setValue(ResonanceStarlightFountainBlock.LIT, false), 3);
                }
                be.setChanged();
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("ActiveStarName", Codec.STRING, this.activeStarName);
        output.store("ActiveEssence", Codec.STRING, this.activeEssence.name());
        output.store("PulseTicksRemaining", Codec.INT, this.pulseTicksRemaining);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.activeStarName = input.read("ActiveStarName", Codec.STRING).orElse("Astral");
        String essStr = input.read("ActiveEssence", Codec.STRING).orElse("ASTRAL");
        try {
            this.activeEssence = EssenceType.valueOf(essStr);
        } catch (Exception e) {
            this.activeEssence = EssenceType.ASTRAL;
        }
        this.pulseTicksRemaining = input.read("PulseTicksRemaining", Codec.INT).orElse(0);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putString("ActiveStarName", activeStarName);
        tag.putString("ActiveEssence", activeEssence.name());
        tag.putInt("PulseTicksRemaining", pulseTicksRemaining);
        return tag;
    }
}
