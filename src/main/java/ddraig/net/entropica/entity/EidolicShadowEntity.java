package ddraig.net.entropica.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.item.RuneItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EidolicShadowEntity extends Entity {

    public static final EntityDataAccessor<ItemStack> CARRIED_ITEM = SynchedEntityData.defineId(EidolicShadowEntity.class, EntityDataSerializers.ITEM_STACK);
    public static final EntityDataAccessor<String> RUNE_1 = SynchedEntityData.defineId(EidolicShadowEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> RUNE_2 = SynchedEntityData.defineId(EidolicShadowEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> RUNE_3 = SynchedEntityData.defineId(EidolicShadowEntity.class, EntityDataSerializers.STRING);
    public static final EntityDataAccessor<String> RUNE_4 = SynchedEntityData.defineId(EidolicShadowEntity.class, EntityDataSerializers.STRING);

    private List<BlockPos> waypoints = new ArrayList<>();
    private int currentTargetIndex = 0;
    private boolean movingForward = true;
    private int waitTimer = 0;

    public EidolicShadowEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(CARRIED_ITEM, ItemStack.EMPTY);
        builder.define(RUNE_1, "");
        builder.define(RUNE_2, "");
        builder.define(RUNE_3, "");
        builder.define(RUNE_4, "");
    }

    public void setWaypoints(List<BlockPos> path) {
        this.waypoints = new ArrayList<>(path);
    }

    // --- NEW GETTER FOR THE HOLOGRAPHIC RENDERER ---
    public List<BlockPos> getWaypoints() {
        return this.waypoints;
    }

    @Override
    public boolean shouldBeSaved() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    public boolean hasRune(String unicodeChar) {
        return entityData.get(RUNE_1).equals(unicodeChar) ||
                entityData.get(RUNE_2).equals(unicodeChar) ||
                entityData.get(RUNE_3).equals(unicodeChar) ||
                entityData.get(RUNE_4).equals(unicodeChar);
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (stack.getItem() instanceof RuneItem runeItem) {
            if (!level().isClientSide()) {
                String r = runeItem.getUnicodeChar();

                if (hasRune(r)) return InteractionResult.FAIL;

                if (entityData.get(RUNE_1).isEmpty()) entityData.set(RUNE_1, r);
                else if (entityData.get(RUNE_2).isEmpty()) entityData.set(RUNE_2, r);
                else if (entityData.get(RUNE_3).isEmpty()) entityData.set(RUNE_3, r);
                else if (entityData.get(RUNE_4).isEmpty()) entityData.set(RUNE_4, r);
                else return InteractionResult.FAIL;

                if (!player.isCreative()) stack.shrink(1);

                this.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f, 1.5f);
            }
            return InteractionResult.SUCCESS;
        }
        return super.interact(player, hand);
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide() || waypoints.isEmpty()) return;

        if (waitTimer > 0) {
            waitTimer--;
            return;
        }

        BlockPos targetPos = waypoints.get(currentTargetIndex);
        Vec3 targetVec = new Vec3(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5);
        Vec3 currentVec = this.position();
        double dist = currentVec.distanceTo(targetVec);

        if (dist < 0.2) {
            if (currentTargetIndex == 0) {
                if (entityData.get(CARRIED_ITEM).isEmpty()) {
                    interactWithChest(targetPos, true);

                    if (entityData.get(CARRIED_ITEM).isEmpty()) {
                        if (!hasRune("ᚱ")) {
                            this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.0f, 0.5f);
                            this.discard();
                            return;
                        }
                    }
                }
                movingForward = true;
                currentTargetIndex++;
            } else if (currentTargetIndex == waypoints.size() - 1) {
                if (!entityData.get(CARRIED_ITEM).isEmpty()) {
                    interactWithChest(targetPos, false);
                }

                if (!entityData.get(CARRIED_ITEM).isEmpty()) {
                    if (!hasRune("ᚱ")) {
                        Containers.dropItemStack(level(), currentVec.x, currentVec.y, currentVec.z, entityData.get(CARRIED_ITEM));
                        this.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.0f, 0.5f);
                        this.discard();
                        return;
                    }
                }
                movingForward = false;
                currentTargetIndex--;

            } else {
                currentTargetIndex += movingForward ? 1 : -1;
            }
        } else {
            Vec3 move = targetVec.subtract(currentVec).normalize().scale(0.15);
            this.setPos(currentVec.add(move));

            float targetYRot = (float)(Math.atan2(move.z, move.x) * (180 / Math.PI)) - 90.0f;
            this.setYRot(targetYRot);
        }
    }

    private void interactWithChest(BlockPos pos, boolean isExtracting) {
        BlockEntity be = level().getBlockEntity(pos);
        if (be instanceof Container container) {
            if (isExtracting) {
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (!container.getItem(i).isEmpty()) {
                        ItemStack extracted = container.removeItem(i, 1);
                        entityData.set(CARRIED_ITEM, extracted);
                        container.setChanged();
                        this.playSound(SoundEvents.SOUL_ESCAPE.value(), 0.5f, 2.0f);
                        waitTimer = 10;
                        break;
                    }
                }
            } else {
                ItemStack stack = entityData.get(CARRIED_ITEM).copy();
                for (int i = 0; i < container.getContainerSize(); i++) {
                    if (container.canPlaceItem(i, stack)) {
                        ItemStack slot = container.getItem(i);
                        if (slot.isEmpty() || (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize())) {
                            if (slot.isEmpty()) container.setItem(i, stack);
                            else slot.grow(1);

                            entityData.set(CARRIED_ITEM, ItemStack.EMPTY);
                            container.setChanged();
                            this.playSound(SoundEvents.SOUL_ESCAPE.value(), 0.5f, 0.5f);
                            waitTimer = 10;
                            break;
                        }
                    }
                }
            }
        }
    }

    // ==========================================
    // MODERN 1.21.10 SERIALIZATION
    // ==========================================

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        int size = input.read("PathSize", Codec.INT).orElse(0);
        waypoints.clear();
        for (int i = 0; i < size; i++) {
            waypoints.add(BlockPos.of(input.read("Path_" + i, Codec.LONG).orElse(0L)));
        }

        currentTargetIndex = input.read("PathIndex", Codec.INT).orElse(0);
        movingForward = input.read("Forward", Codec.BOOL).orElse(true);

        entityData.set(CARRIED_ITEM, input.read("Item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        entityData.set(RUNE_1, input.read("R1", Codec.STRING).orElse(""));
        entityData.set(RUNE_2, input.read("R2", Codec.STRING).orElse(""));
        entityData.set(RUNE_3, input.read("R3", Codec.STRING).orElse(""));
        entityData.set(RUNE_4, input.read("R4", Codec.STRING).orElse(""));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.store("PathSize", Codec.INT, waypoints.size());
        for (int i = 0; i < waypoints.size(); i++) {
            output.store("Path_" + i, Codec.LONG, waypoints.get(i).asLong());
        }

        output.store("PathIndex", Codec.INT, currentTargetIndex);
        output.store("Forward", Codec.BOOL, movingForward);

        output.store("Item", ItemStack.OPTIONAL_CODEC, entityData.get(CARRIED_ITEM));
        output.store("R1", Codec.STRING, entityData.get(RUNE_1));
        output.store("R2", Codec.STRING, entityData.get(RUNE_2));
        output.store("R3", Codec.STRING, entityData.get(RUNE_3));
        output.store("R4", Codec.STRING, entityData.get(RUNE_4));
    }
}