package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.MateriaFluxDistributorBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MateriaFluxDistributorBlock extends BaseEntityBlock {
    public static final MapCodec<MateriaFluxDistributorBlock> CODEC = simpleCodec(MateriaFluxDistributorBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1.0, 0.0, 1.0, 15.0, 4.0, 15.0),
            Block.box(3.0, 4.0, 3.0, 13.0, 12.0, 13.0),
            Block.box(2.0, 12.0, 2.0, 14.0, 16.0, 14.0)
    );

    public MateriaFluxDistributorBlock(Properties properties) {
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
        return new MateriaFluxDistributorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.MATERIA_FLUX_DISTRIBUTOR_BE.get(), MateriaFluxDistributorBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(net.minecraft.world.item.ItemStack itemStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof MateriaFluxDistributorBlockEntity distributor) {
                // Check if shift-clicking or right-clicking with upgrade catalyst
                if (player.isShiftKeyDown() || itemStack.is(ddraig.net.entropica.registry.ModItems.ASTRAL_CRYSTAL.get())) {
                    if (distributor.getDistributorTier() < 3) {
                        if (!player.isCreative()) itemStack.shrink(1);
                        distributor.upgradeTier();
                        player.displayClientMessage(Component.literal("§6✦ Materia-Flux Distributor upgraded to §eTier " + distributor.getDistributorTier() + " §7(Range: " + distributor.getDistributionRadius() + "m)"), true);
                        return InteractionResult.SUCCESS;
                    }
                }

                int flux = distributor.getStoredFlux();
                int maxCap = distributor.getMaxCapacity();
                player.displayClientMessage(
                        Component.literal("§6[Materia-Flux Distributor] §eTier " + distributor.getDistributorTier() + " §7| Range: §b" + distributor.getDistributionRadius() + "m §7| Stored Starlight Flux: §f" + flux + " / " + maxCap + " Flux"),
                        true
                );
            }
        }
        return InteractionResult.SUCCESS;
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
