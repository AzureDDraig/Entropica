package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AstralCollectorBlock extends BaseEntityBlock {
    public static final MapCodec<AstralCollectorBlock> CODEC = simpleCodec(AstralCollectorBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.5, 0.0, 0.5, 15.5, 4.0, 15.5),
            Block.box(2.5, 4.0, 2.5, 13.5, 8.0, 13.5),
            Block.box(0.0, 8.0, 0.0, 16.0, 16.0, 16.0)
    );

    public AstralCollectorBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AstralCollectorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.ASTRAL_COLLECTOR_BE.get(), AstralCollectorBlockEntity::tick);
    }

    @Override
    protected net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AstralCollectorBlockEntity collector) {
                String essenceTitle = collector.getStoredEssence().getFormattedName();
                player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("§b[Astral Collector] §7Stored Materia: §f" + collector.getStoredMateria() + "/" + AstralCollectorBlockEntity.MAX_CAPACITY + " §7[" + essenceTitle + "§7]"),
                        true
                );
            }
        }
        return net.minecraft.world.InteractionResult.SUCCESS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
