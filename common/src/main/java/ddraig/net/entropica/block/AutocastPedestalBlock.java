package ddraig.net.entropica.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.block.entity.AutocastPedestalBlockEntity;
import ddraig.net.entropica.inventory.AutocastPedestalMenu;
import ddraig.net.entropica.registry.ModBlockEntities;
import dev.architectury.registry.menu.ExtendedMenuProvider;
import dev.architectury.registry.menu.MenuRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Autocast Pedestal — stationary spell automation turret & utility node available in
 * Stone, Marble, Quartz, and Astral Slate variants (Spellcrafting Guide Sections 2.1.7, 8.4).
 */
public class AutocastPedestalBlock extends BaseEntityBlock {

    public enum PedestalVariant implements StringRepresentable {
        STONE("stone"),
        MARBLE("marble"),
        QUARTZ("quartz"),
        ASTRAL_SLATE("astral_slate");

        public static final Codec<PedestalVariant> CODEC = StringRepresentable.fromEnum(PedestalVariant::values);

        private final String serializedName;

        PedestalVariant(String serializedName) {
            this.serializedName = serializedName;
        }

        @Override
        public String getSerializedName() {
            return this.serializedName;
        }

        public String getTexturePath() {
            return "textures/block/autocast_pedestal_" + this.serializedName + ".png";
        }
    }

    public static final MapCodec<AutocastPedestalBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PedestalVariant.CODEC.fieldOf("variant").forGetter(AutocastPedestalBlock::getVariant),
            propertiesCodec()
    ).apply(instance, AutocastPedestalBlock::new));

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 16.0D, 14.0D);

    private final PedestalVariant variant;

    public AutocastPedestalBlock(PedestalVariant variant, Properties properties) {
        super(properties);
        this.variant = variant;
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public PedestalVariant getVariant() {
        return this.variant;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        // Aim forward in the direction the player is looking when placing, or toward the player?
        // Facing = horizontal look direction of placer so placing a turret aims where the player looks!
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer != null && level.getBlockEntity(pos) instanceof AutocastPedestalBlockEntity be) {
            be.setOwnerUuid(placer.getUUID());
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AutocastPedestalBlockEntity be) {
            Containers.dropContents(level, pos, be.inventory);
            be.inventory.clearContent();
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AutocastPedestalBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, ModBlockEntities.AUTOCAST_PEDESTAL_BE.get(), AutocastPedestalBlockEntity::tick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (level.getBlockEntity(pos) instanceof AutocastPedestalBlockEntity be && player instanceof ServerPlayer serverPlayer) {
            MenuRegistry.openExtendedMenu(serverPlayer, new ExtendedMenuProvider() {
                @Override
                public void saveExtraData(FriendlyByteBuf buf) {
                    buf.writeBlockPos(pos);
                    buf.writeVarInt(be.getIntervalTicks());
                    buf.writeFloat(be.getAimPitch());
                    buf.writeVarInt(be.getRedstoneMode());
                    List<BlockPos> links = be.getLinkedInventories();
                    buf.writeVarInt(links.size());
                    for (BlockPos linked : links) {
                        buf.writeBlockPos(linked);
                    }
                }

                @Override
                public Component getDisplayName() {
                    return Component.translatable(AutocastPedestalBlock.this.getDescriptionId());
                }

                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player p) {
                    return new AutocastPedestalMenu(
                            syncId,
                            inv,
                            pos,
                            be.getIntervalTicks(),
                            be.getAimPitch(),
                            be.getRedstoneMode(),
                            be.getLinkedInventories()
                    );
                }
            });
        }
        return InteractionResult.SUCCESS;
    }
}
