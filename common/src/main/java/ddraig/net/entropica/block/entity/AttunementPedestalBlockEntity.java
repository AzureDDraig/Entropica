package ddraig.net.entropica.block.entity;

import ddraig.net.entropica.astral.Constellation;
import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AttunementPedestalBlockEntity extends BlockEntity {

    public float clientRotation = 0.0f;

    public final SimpleContainer inventory = new SimpleContainer(2) {
        @Override
        public void setChanged() {
            super.setChanged();
            AttunementPedestalBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    public AttunementPedestalBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATTUNEMENT_PEDESTAL_BE.get(), pos, state);
    }

    public ItemStack getHeldItem() {
        return inventory.getItem(0);
    }

    public void setHeldItem(ItemStack stack) {
        inventory.setItem(0, stack);
        setChanged();
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);

        // Extract
        if (heldItem.isEmpty()) {
            for (int i = 1; i >= 0; i--) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot.copy());
                    inventory.setItem(i, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        // Insert
        else {
            for (int i = 0; i < 2; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty() && ItemStack.isSameItemSameComponents(stackInSlot, heldItem)) {
                    if (stackInSlot.getCount() < stackInSlot.getMaxStackSize()) {
                        stackInSlot.grow(1);
                        heldItem.shrink(1);
                        this.setChanged();
                        return true;
                    }
                }
            }

            for (int i = 0; i < 2; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (stackInSlot.isEmpty()) {
                    inventory.setItem(i, heldItem.copyWithCount(1));
                    heldItem.shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AttunementPedestalBlockEntity be) {
        if (level.isClientSide()) {
            be.clientRotation += 0.03f;
            ItemStack stack = be.getHeldItem();
            if (stack.getItem() instanceof AstralCrystalItem) {
                Constellation ritual = AstralCrystalItem.getRitual(stack);
                if (ritual != null && level.random.nextFloat() < 0.25f) {
                    level.addParticle(ParticleTypes.END_ROD,
                            pos.getX() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4,
                            pos.getY() + 1.2 + level.random.nextDouble() * 0.3,
                            pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5) * 0.4,
                            0, 0.01, 0);
                }
            }
            return;
        }

        // Server Sanctuary Field Execution
        ItemStack mainStack = be.getHeldItem();
        if (!(mainStack.getItem() instanceof AstralCrystalItem)) return;

        Constellation ritual = AstralCrystalItem.getRitual(mainStack);
        if (ritual == null) return;

        int size = AstralCrystalItem.getSize(mainStack);
        int radius = 8 + size * 4; // 12 to 28 blocks

        long gameTime = level.getGameTime();

        // 1. Arbor Vitae: Crop Growth Acceleration
        if ("arbor_vitae".equals(ritual.getId().getPath()) && gameTime % 40 == 0) {
            int cx = pos.getX() + level.random.nextInt(radius * 2 + 1) - radius;
            int cz = pos.getZ() + level.random.nextInt(radius * 2 + 1) - radius;
            for (int cy = pos.getY() - 2; cy <= pos.getY() + 2; cy++) {
                BlockPos target = new BlockPos(cx, cy, cz);
                BlockState targetState = level.getBlockState(target);
                if (targetState.getBlock() instanceof BonemealableBlock bonemealable && level instanceof ServerLevel sl) {
                    if (bonemealable.isValidBonemealTarget(level, target, targetState)) {
                        bonemealable.performBonemeal(sl, level.random, target, targetState);
                        sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, target.getX() + 0.5, target.getY() + 0.5, target.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.02);
                        break;
                    }
                }
            }
        }

        // 2. Lucerna Radialis: Hostile Mob Pacification & Anti-Spawn Field
        else if ("lucerna_radialis".equals(ritual.getId().getPath()) && gameTime % 20 == 0) {
            AABB aabb = new AABB(pos).inflate(radius);
            List<Monster> monsters = level.getEntitiesOfClass(Monster.class, aabb);
            for (Monster m : monsters) {
                m.igniteForSeconds(2);
                Vec3 push = m.position().subtract(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5).normalize().scale(0.5);
                m.setDeltaMovement(push.x, 0.2, push.z);
            }
        }

        // 3. Scutum Aegis: Player Shield & Resistance
        else if ("scutum_aegis".equals(ritual.getId().getPath()) && gameTime % 60 == 0) {
            AABB aabb = new AABB(pos).inflate(radius);
            List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
            for (Player p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 1, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 120, 0, false, false));
            }
        }

        // 4. Glacies Crystalline: Frost Slowdown Field
        else if ("glacies_crystalline".equals(ritual.getId().getPath()) && gameTime % 20 == 0) {
            AABB aabb = new AABB(pos).inflate(radius);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> !(e instanceof Player));
            for (LivingEntity e : entities) {
                e.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 40, 2, false, true));
                e.setTicksFrozen(Math.min(e.getTicksRequiredToFreeze() + 60, e.getTicksFrozen() + 40));
            }
        }

        // 5. Vorago Blighti: Vacuum Magnetic Attraction
        else if ("vorago_blighti".equals(ritual.getId().getPath()) && gameTime % 5 == 0) {
            AABB aabb = new AABB(pos).inflate(radius);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, aabb);
            for (ItemEntity item : items) {
                Vec3 dir = new Vec3(pos.getX() + 0.5 - item.getX(), pos.getY() + 1.0 - item.getY(), pos.getZ() + 0.5 - item.getZ()).normalize().scale(0.35);
                item.setDeltaMovement(dir);
            }
            List<ExperienceOrb> orbs = level.getEntitiesOfClass(ExperienceOrb.class, aabb);
            for (ExperienceOrb orb : orbs) {
                Vec3 dir = new Vec3(pos.getX() + 0.5 - orb.getX(), pos.getY() + 1.0 - orb.getY(), pos.getZ() + 0.5 - orb.getZ()).normalize().scale(0.35);
                orb.setDeltaMovement(dir);
            }
        }

        // 6. Ulteria Viatoris & Penna Aetheris: Movement & Slow Falling
        else if ("ulteria_viatoris".equals(ritual.getId().getPath()) && gameTime % 60 == 0) {
            AABB aabb = new AABB(pos).inflate(radius);
            List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
            for (Player p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.SPEED, 120, 1, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 120, 0, false, false));
            }
        }
        else if ("penna_aetheris".equals(ritual.getId().getPath()) && gameTime % 60 == 0) {
            AABB aabb = new AABB(pos).inflate(radius);
            List<Player> players = level.getEntitiesOfClass(Player.class, aabb);
            for (Player p : players) {
                p.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 120, 0, false, false));
                p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, 120, 1, false, false));
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            output.store("PedestalStack_" + i, ItemStack.OPTIONAL_CODEC, inventory.getItem(i));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int finalI = i;
            input.read("PedestalStack_" + i, ItemStack.OPTIONAL_CODEC).ifPresent(stack -> inventory.setItem(finalI, stack));
        }
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