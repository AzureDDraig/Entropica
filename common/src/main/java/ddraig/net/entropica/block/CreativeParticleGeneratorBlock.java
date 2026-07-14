package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.CreativeParticleGeneratorBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class CreativeParticleGeneratorBlock extends BaseEntityBlock {
    public static final MapCodec<CreativeParticleGeneratorBlock> CODEC = simpleCodec(CreativeParticleGeneratorBlock::new);

    public CreativeParticleGeneratorBlock(Properties properties) {
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
        return new CreativeParticleGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CREATIVE_PARTICLE_GENERATOR_BE.get(),
                (lvl, pos, st, be) -> be.tick(lvl, pos, st));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof CreativeParticleGeneratorBlockEntity generator) {

                // Right-Click goes forwards, Shift-Right-Click goes backwards!
                int delta = player.isShiftKeyDown() ? -1 : 1;
                generator.cycleParticle(delta);

                if (generator.getCurrentParticle() != null) {
                    player.displayClientMessage(
                            Component.literal("§dCreative Particle: §f" + generator.getCurrentParticle().toString()),
                            true
                    );
                }

                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.5F, 1.2F);
            }
        }
        return InteractionResult.SUCCESS;
    }
}