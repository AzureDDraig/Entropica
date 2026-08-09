package ddraig.net.entropica.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Supplier;

public class ModSpawnEggItem extends Item {

    private final Supplier<? extends EntityType<?>> entityTypeSupplier;

    public ModSpawnEggItem(Supplier<? extends EntityType<?>> entityTypeSupplier, Item.Properties properties) {
        super(properties);
        this.entityTypeSupplier = entityTypeSupplier;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockState state = level.getBlockState(pos);

        BlockPos spawnPos = state.getCollisionShape(level, pos).isEmpty() ? pos : pos.relative(direction);

        EntityType<?> type = entityTypeSupplier.get();
        if (type != null && level instanceof ServerLevel serverLevel) {
            type.spawn(serverLevel, context.getItemInHand(), context.getPlayer(), spawnPos, EntitySpawnReason.SPAWN_ITEM_USE, true, !pos.equals(spawnPos) && direction == Direction.UP);
            context.getItemInHand().consume(1, context.getPlayer());
        }

        return InteractionResult.CONSUME;
    }

    public EntityType<?> getType() {
        return entityTypeSupplier.get();
    }
}
