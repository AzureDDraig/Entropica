package ddraig.net.entropica.item;

import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class AstralCrystalSeedItem extends Item {

    public AstralCrystalSeedItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();

        BlockState state = level.getBlockState(pos);
        // If clicking on water or starlight pool
        if (state.is(Blocks.WATER) || state.getBlock() instanceof ddraig.net.entropica.block.AstralMirrorBlock) {
            if (!level.isClientSide()) {
                stack.shrink(1);
                ItemStack crystal = new ItemStack(ModItems.ASTRAL_CRYSTAL.get());
                AstralCrystalItem.setSize(crystal, 1);
                AstralCrystalItem.setPurity(crystal, 90 + level.random.nextInt(11));
                AstralCrystalItem.setCut(crystal, 85 + level.random.nextInt(16));

                ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, crystal);
                level.addFreshEntity(entity);

                level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.4f);
                if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 12, 0.2, 0.3, 0.2, 0.05);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§6◆ Crystal Cultivation Seed"));
        tooltipComponents.accept(Component.literal("§7  Produced by grinding Astral Crystals in a Mortar & Pestle."));
        tooltipComponents.accept(Component.literal("§7  Soak in starlight pools or liquid Materia to grow into a Size I Crystal."));
    }
}
