package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.AstralCollectorBlockEntity;
import ddraig.net.entropica.item.AstralCrystalItem;
import ddraig.net.entropica.item.AstralLinkingWandItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof AstralCollectorBlockEntity collector)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }

        ItemStack heldStack = player.getItemInHand(InteractionHand.MAIN_HAND);

        // Linking Wand passes through to item useOn
        if (heldStack.getItem() instanceof AstralLinkingWandItem) {
            return InteractionResult.PASS;
        }

        // Insert Crystal
        if (collector.getSocketedCrystal().isEmpty() && !heldStack.isEmpty()) {
            if (heldStack.getItem() instanceof AstralCrystalItem || heldStack.is(ModBlocks.ASTRAL_CRYSTAL_CLUSTER.get().asItem()) || heldStack.is(ModBlocks.ASTRAL_CRYSTAL_BLOCK.get().asItem())) {
                if (!level.isClientSide()) {
                    ItemStack inserted = heldStack.split(1);
                    collector.setSocketedCrystal(inserted);
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.4f);
                    player.displayClientMessage(Component.literal("§a[Astral Collection Altar] §7Socketed §b" + inserted.getHoverName().getString()), true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        // Shift + Click -> Extract Socketed Crystal
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide()) {
                if (!collector.getSocketedCrystal().isEmpty()) {
                    ItemStack crystal = collector.getSocketedCrystal().copy();
                    collector.setSocketedCrystal(ItemStack.EMPTY);
                    if (!player.getInventory().add(crystal)) {
                        player.drop(crystal, false);
                    }
                    level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f);
                    player.displayClientMessage(Component.literal("§e[Astral Collection Altar] §7Extracted §b" + crystal.getHoverName().getString()), true);
                    return InteractionResult.SUCCESS;
                }
            }
            return InteractionResult.SUCCESS;
        }

        if (!level.isClientSide()) {
            collector.displayCollectorStatus(player);
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
