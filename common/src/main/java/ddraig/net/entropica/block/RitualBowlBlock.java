package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ddraig.net.entropica.block.entity.RitualBowlBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
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
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class RitualBowlBlock extends BaseEntityBlock {
    public enum BowlVariant implements net.minecraft.util.StringRepresentable {
        MARBLE("marble"),
        BASALT("basalt"),
        GRANITE("granite");

        private final String name;
        BowlVariant(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public final BowlVariant variant;
    public final boolean isBasalt;

    public static final MapCodec<RitualBowlBlock> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    com.mojang.serialization.Codec.BOOL.fieldOf("is_basalt").forGetter(b -> b.isBasalt),
                    propertiesCodec()
            ).apply(instance, RitualBowlBlock::new)
    );

    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 5.0D, 14.0D);

    public RitualBowlBlock(boolean isBasalt, Properties properties) {
        this(isBasalt ? BowlVariant.BASALT : BowlVariant.MARBLE, properties);
    }

    public RitualBowlBlock(BowlVariant variant, Properties properties) {
        super(properties);
        this.variant = variant;
        this.isBasalt = (variant == BowlVariant.BASALT);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RitualBowlBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.RITUAL_BOWL_BE.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof RitualBowlBlockEntity be) {
            if (!level.isClientSide()) {
                be.interactWithPlayer(player, InteractionHand.MAIN_HAND);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof RitualBowlBlockEntity be) {
            Containers.dropContents(level, pos, be.inventory);
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}