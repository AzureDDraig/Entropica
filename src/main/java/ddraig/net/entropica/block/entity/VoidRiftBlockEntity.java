package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.VoidRiftBlock;
import ddraig.net.entropica.registry.ModAttachments;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class VoidRiftBlockEntity extends BlockEntity {

    private BlockPos targetPos = null;
    public final List<BlockPos> attachedInventories = new ArrayList<>();

    public static final int MAX_TRANSIT_CAPACITY = 64;

    private final Map<BlockPos, List<Vec3>> pathCache = new HashMap<>();
    private final Map<BlockPos, Long> pathCacheTime = new HashMap<>();

    public static class TransitItem {
        public ItemStack stack;
        public int tick;
        public int maxTicks;
        public boolean fromGround;
        public float startX, startY, startZ;
        public boolean hasInvTarget;
        public float invOffsetX, invOffsetY, invOffsetZ;

        public TransitItem(ItemStack stack, int tick, int maxTicks, boolean fromGround, float sx, float sy, float sz, boolean hasInvTarget, float ix, float iy, float iz) {
            this.stack = stack;
            this.tick = tick;
            this.maxTicks = maxTicks;
            this.fromGround = fromGround;
            this.startX = sx; this.startY = sy; this.startZ = sz;
            this.hasInvTarget = hasInvTarget;
            this.invOffsetX = ix; this.invOffsetY = iy; this.invOffsetZ = iz;
        }
    }

    public final List<TransitItem> transitItems = new ArrayList<>();
    private int extractCooldown = 0;
    private final Map<BlockPos, Integer> lidKeepOpenTicks = new HashMap<>();

    public VoidRiftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VOID_RIFT_BE.get(), pos, state);
    }

    public void setTarget(BlockPos pos) {
        this.targetPos = pos;
        setChanged();
    }

    public void setAttachedInventory(BlockPos pos) {
        this.attachedInventories.clear();
        this.attachedInventories.add(pos);
        this.pathCache.clear();
        setChanged();
    }

    public void addAttachedInventory(BlockPos pos) {
        if (!this.attachedInventories.contains(pos)) {
            this.attachedInventories.add(pos);
            this.pathCache.clear();
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public List<Vec3> getPathToInventory(BlockPos invPos) {
        if (level == null) return new ArrayList<>();
        long time = level.getGameTime();

        if (pathCache.containsKey(invPos) && (time - pathCacheTime.getOrDefault(invPos, 0L) < 600)) {
            return pathCache.get(invPos);
        }

        List<Vec3> path = calculatePath(this.worldPosition, invPos);
        pathCache.put(invPos, path);
        pathCacheTime.put(invPos, time);
        return path;
    }

    private List<Vec3> calculatePath(BlockPos start, BlockPos end) {
        Queue<BlockPos> queue = new LinkedList<>();
        Map<BlockPos, BlockPos> cameFrom = new HashMap<>();
        Set<BlockPos> visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        boolean found = false;
        while (!queue.isEmpty()) {
            if (visited.size() > 500) {
                break;
            }

            BlockPos current = queue.poll();
            if (current.equals(end)) {
                found = true;
                break;
            }

            for (Direction dir : Direction.values()) {
                BlockPos neighbor = current.relative(dir);

                if (Math.abs(neighbor.getX() - start.getX()) > 4 ||
                        Math.abs(neighbor.getY() - start.getY()) > 4 ||
                        Math.abs(neighbor.getZ() - start.getZ()) > 4) continue;

                if (!visited.contains(neighbor)) {
                    if (neighbor.equals(end) || level.getBlockState(neighbor).getCollisionShape(level, neighbor).isEmpty()) {
                        visited.add(neighbor);
                        cameFrom.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }

        List<Vec3> waypoints = new ArrayList<>();
        if (found) {
            BlockPos curr = end;
            while (curr != null) {
                waypoints.add(new Vec3(curr.getX() - start.getX() + 0.5, curr.getY() - start.getY() + 0.5, curr.getZ() - start.getZ() + 0.5));
                curr = cameFrom.get(curr);
            }
            Collections.reverse(waypoints);
        } else {
            waypoints.add(new Vec3(0.5, 0.5, 0.5));
            waypoints.add(new Vec3(end.getX() - start.getX() + 0.5, end.getY() - start.getY() + 0.5, end.getZ() - start.getZ() + 0.5));
        }

        return smoothPath(waypoints, 3);
    }

    private List<Vec3> smoothPath(List<Vec3> points, int iterations) {
        if (points.size() <= 2) return points;
        List<Vec3> current = points;
        for (int i = 0; i < iterations; i++) {
            List<Vec3> smoothed = new ArrayList<>();
            smoothed.add(current.get(0));
            for (int j = 0; j < current.size() - 1; j++) {
                Vec3 p0 = current.get(j);
                Vec3 p1 = current.get(j + 1);
                smoothed.add(p0.lerp(p1, 0.25));
                smoothed.add(p0.lerp(p1, 0.75));
            }
            smoothed.add(current.get(current.size() - 1));
            current = smoothed;
        }
        return current;
    }

    public void receiveItem(ItemStack stack) {
        if (this.transitItems.size() < MAX_TRANSIT_CAPACITY) {
            BlockPos predicted = null;
            if (level != null) {
                for (BlockPos pos : attachedInventories) {
                    BlockEntity invBE = level.getBlockEntity(pos);
                    if (invBE instanceof Container container) {
                        for (int i = 0; i < container.getContainerSize(); i++) {
                            if (container.canPlaceItem(i, stack)) {
                                ItemStack slot = container.getItem(i);
                                if (slot.isEmpty() || (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() + stack.getCount() <= slot.getMaxStackSize())) {
                                    predicted = pos;
                                    break;
                                }
                            }
                        }
                    }
                    if (predicted != null) break;
                }
            }

            boolean hasInv = predicted != null;
            float ox = hasInv ? predicted.getX() - worldPosition.getX() : 0;
            float oy = hasInv ? predicted.getY() - worldPosition.getY() : -1;
            float oz = hasInv ? predicted.getZ() - worldPosition.getZ() : 0;

            int dist = (int) (Math.abs(ox) + Math.abs(oy) + Math.abs(oz));
            int maxTicks = Math.max(40, dist * 5);

            this.transitItems.add(new TransitItem(stack.copy(), 0, maxTicks, false, 0f, 0f, 0f, hasInv, ox, oy, oz));
            setChanged();
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void openLid(BlockPos pos) {
        if (!this.lidKeepOpenTicks.containsKey(pos)) setContainerOpen(level, pos, true);
        this.lidKeepOpenTicks.put(pos, 20);
    }

    private void setContainerOpen(Level level, BlockPos pos, boolean open) {
        if (level == null || pos == null) return;
        BlockState state = level.getBlockState(pos);
        if (state.hasProperty(BlockStateProperties.OPEN)) {
            if (state.getValue(BlockStateProperties.OPEN) != open) {
                level.setBlock(pos, state.setValue(BlockStateProperties.OPEN, open), 3);
                level.playSound(null, pos, open ? SoundEvents.BARREL_OPEN : SoundEvents.BARREL_CLOSE, SoundSource.BLOCKS, 0.5f, 1.0f);
            }
        }
        level.blockEvent(pos, state.getBlock(), 1, open ? 1 : 0);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide() && level.getGameTime() % 10 == 0) {
            AABB riftHitbox = new AABB(pos);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, riftHitbox);
            for (LivingEntity living : entities) {
                living.addEffect(new MobEffectInstance(ModEffects.VOID_TEAR, 80, 0, false, true, true));
                living.addEffect(new MobEffectInstance(ModEffects.VIS_TOXICITY, 200, 0, false, true, true));
                // FIXED: Use the specific LivingEntity instance and explicitly pass VOID
                living.setData(ModAttachments.TOXICITY_SOURCE, EssenceType.VOID.name());
            }
        }

        Iterator<Map.Entry<BlockPos, Integer>> lidIt = lidKeepOpenTicks.entrySet().iterator();
        while (lidIt.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = lidIt.next();
            int ticks = entry.getValue() - 1;
            if (ticks <= 0) {
                setContainerOpen(level, entry.getKey(), false);
                lidIt.remove();
            } else {
                entry.setValue(ticks);
            }
        }

        if (state.getValue(VoidRiftBlock.IS_INPUT)) handleInput(level, pos);
        else handleOutput(level, pos);
    }

    private void handleInput(Level level, BlockPos pos) {
        if (extractCooldown > 0) extractCooldown--;

        if (!level.isClientSide() && extractCooldown == 0 && this.transitItems.size() < MAX_TRANSIT_CAPACITY && targetPos != null) {
            BlockEntity targetBE = level.getBlockEntity(targetPos);

            if (targetBE instanceof VoidRiftBlockEntity outputRift && outputRift.transitItems.size() < MAX_TRANSIT_CAPACITY) {
                boolean anyItemPulled = false;

                AABB suckBox = new AABB(pos).inflate(1.0);
                List<ItemEntity> groundItems = level.getEntitiesOfClass(ItemEntity.class, suckBox);

                for (ItemEntity entityItem : groundItems) {
                    ItemStack stack = entityItem.getItem();
                    if (!stack.isEmpty()) {
                        ItemStack pulled = stack.split(1);
                        if (stack.isEmpty()) entityItem.discard();
                        else entityItem.setItem(stack);

                        float sx = (float) (entityItem.getX() - pos.getX());
                        float sy = (float) (entityItem.getY() - pos.getY());
                        float sz = (float) (entityItem.getZ() - pos.getZ());

                        this.transitItems.add(new TransitItem(pulled, 0, 40, true, sx, sy, sz, false, 0, 0, 0));
                        anyItemPulled = true;
                        break;
                    }
                }

                if (!anyItemPulled) {
                    for (BlockPos invPos : attachedInventories) {
                        if (this.transitItems.size() >= MAX_TRANSIT_CAPACITY) break;

                        BlockEntity invBE = level.getBlockEntity(invPos);
                        if (invBE instanceof Container container) {
                            for (int i = 0; i < container.getContainerSize(); i++) {
                                ItemStack stack = container.getItem(i);
                                if (!stack.isEmpty()) {
                                    ItemStack pulled = container.removeItem(i, 1);
                                    container.setChanged();

                                    float ox = invPos.getX() - pos.getX();
                                    float oy = invPos.getY() - pos.getY();
                                    float oz = invPos.getZ() - pos.getZ();

                                    int dist = (int) (Math.abs(ox) + Math.abs(oy) + Math.abs(oz));
                                    int maxTicks = Math.max(40, dist * 5);

                                    this.transitItems.add(new TransitItem(pulled, 0, maxTicks, false, 0, 0, 0, true, ox, oy, oz));
                                    openLid(invPos);
                                    anyItemPulled = true;
                                    break;
                                }
                            }
                        }
                        if (anyItemPulled) break;
                    }
                }

                if (anyItemPulled) {
                    this.extractCooldown = 8;
                    setChanged();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                }
            }
        }

        Iterator<TransitItem> it = this.transitItems.iterator();
        boolean changed = false;
        while (it.hasNext()) {
            TransitItem item = it.next();
            item.tick++;

            if (item.tick >= item.maxTicks) {
                if (!level.isClientSide()) {
                    if (targetPos != null) {
                        BlockEntity be = level.getBlockEntity(targetPos);
                        if (be instanceof VoidRiftBlockEntity outputBE) {
                            outputBE.receiveItem(item.stack);
                        } else {
                            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, item.stack);
                        }
                    }
                    changed = true;
                }
                it.remove();
            }
        }

        if (changed && !level.isClientSide()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private void handleOutput(Level level, BlockPos pos) {
        Iterator<TransitItem> it = this.transitItems.iterator();
        boolean changed = false;

        while (it.hasNext()) {
            TransitItem item = it.next();
            item.tick++;

            if (item.tick >= item.maxTicks) {
                if (!level.isClientSide()) {
                    boolean inserted = false;
                    for (BlockPos invPos : attachedInventories) {
                        BlockEntity invBE = level.getBlockEntity(invPos);
                        if (invBE instanceof Container container) {
                            for (int i = 0; i < container.getContainerSize(); i++) {
                                if (container.canPlaceItem(i, item.stack)) {
                                    ItemStack slotStack = container.getItem(i);
                                    if (slotStack.isEmpty() || (ItemStack.isSameItemSameComponents(slotStack, item.stack) && slotStack.getCount() + item.stack.getCount() <= slotStack.getMaxStackSize())) {
                                        if (slotStack.isEmpty()) container.setItem(i, item.stack);
                                        else slotStack.grow(item.stack.getCount());
                                        container.setChanged();
                                        inserted = true;
                                        openLid(invPos);
                                        break;
                                    }
                                }
                            }
                        }
                        if (inserted) break;
                    }
                    if (!inserted) Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() - 0.2, pos.getZ() + 0.5, item.stack);
                    changed = true;
                }
                it.remove();
            }
        }

        if (changed && !level.isClientSide()) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.targetPos != null) output.store("TargetPos", BlockPos.CODEC, this.targetPos);

        output.store("InvCount", Codec.INT, this.attachedInventories.size());
        for (int i = 0; i < this.attachedInventories.size(); i++) {
            output.store("InvPos_" + i, BlockPos.CODEC, this.attachedInventories.get(i));
        }

        output.store("TransitCount", Codec.INT, this.transitItems.size());
        for (int i = 0; i < this.transitItems.size(); i++) {
            output.store("TransitItem_" + i, ItemStack.OPTIONAL_CODEC, this.transitItems.get(i).stack);
            output.store("TransitTick_" + i, Codec.INT, this.transitItems.get(i).tick);
            output.store("TransitMaxTick_" + i, Codec.INT, this.transitItems.get(i).maxTicks);
            output.store("TransitGround_" + i, Codec.BOOL, this.transitItems.get(i).fromGround);
            output.store("TransitSX_" + i, Codec.FLOAT, this.transitItems.get(i).startX);
            output.store("TransitSY_" + i, Codec.FLOAT, this.transitItems.get(i).startY);
            output.store("TransitSZ_" + i, Codec.FLOAT, this.transitItems.get(i).startZ);
            output.store("TransitHasInv_" + i, Codec.BOOL, this.transitItems.get(i).hasInvTarget);
            output.store("TransitIX_" + i, Codec.FLOAT, this.transitItems.get(i).invOffsetX);
            output.store("TransitIY_" + i, Codec.FLOAT, this.transitItems.get(i).invOffsetY);
            output.store("TransitIZ_" + i, Codec.FLOAT, this.transitItems.get(i).invOffsetZ);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.read("TargetPos", BlockPos.CODEC).ifPresent(p -> this.targetPos = p);

        this.attachedInventories.clear();
        int invCount = input.read("InvCount", Codec.INT).orElse(0);
        for (int i = 0; i < invCount; i++) {
            input.read("InvPos_" + i, BlockPos.CODEC).ifPresent(this.attachedInventories::add);
        }

        this.transitItems.clear();
        int count = input.read("TransitCount", Codec.INT).orElse(0);
        for (int i = 0; i < count; i++) {
            ItemStack stack = input.read("TransitItem_" + i, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
            int tick = input.read("TransitTick_" + i, Codec.INT).orElse(0);
            int maxTicks = input.read("TransitMaxTick_" + i, Codec.INT).orElse(40);
            boolean fromGround = input.read("TransitGround_" + i, Codec.BOOL).orElse(false);
            float sx = input.read("TransitSX_" + i, Codec.FLOAT).orElse(0f);
            float sy = input.read("TransitSY_" + i, Codec.FLOAT).orElse(0f);
            float sz = input.read("TransitSZ_" + i, Codec.FLOAT).orElse(0f);
            boolean hasInv = input.read("TransitHasInv_" + i, Codec.BOOL).orElse(false);
            float ix = input.read("TransitIX_" + i, Codec.FLOAT).orElse(0f);
            float iy = input.read("TransitIY_" + i, Codec.FLOAT).orElse(0f);
            float iz = input.read("TransitIZ_" + i, Codec.FLOAT).orElse(0f);

            if (!stack.isEmpty()) {
                this.transitItems.add(new TransitItem(stack, tick, maxTicks, fromGround, sx, sy, sz, hasInv, ix, iy, iz));
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}