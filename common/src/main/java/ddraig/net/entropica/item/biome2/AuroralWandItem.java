package ddraig.net.entropica.item.biome2;

import ddraig.net.entropica.block.biome2.AuroralMoteLightBlock;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Auroral Wand — copper wand tipped with a Charged Glow-Tentacle and Aurora Dust.
 * Fires a 32-block ray of cold cyan-emerald Aurora Materia that conjures a floating
 * Auroral Mote Light in air or water adjacent to the struck surface.
 */
public class AuroralWandItem extends Item {
    public static final double MAX_RANGE = 32.0D;
    private static final int COOLDOWN_TICKS = 20;

    public AuroralWandItem(Properties properties) {
        super(properties.stacksTo(1).durability(128));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0F);
        Vec3 reachPos = eyePos.add(look.scale(MAX_RANGE));

        BlockHitResult hit = level.clip(new ClipContext(
                eyePos, reachPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player
        ));

        if (hit.getType() != HitResult.Type.BLOCK || !ModBlocks.AURORAL_MOTE_LIGHT.isBound()) {
            return InteractionResult.PASS;
        }

        BlockPos hitPos = hit.getBlockPos();
        BlockState hitState = level.getBlockState(hitPos);
        BlockPos placePos = (hitState.canBeReplaced() && !hitState.is(ModBlocks.AURORAL_MOTE_LIGHT.get())
                && (hitState.isAir() || level.getFluidState(hitPos).getType() == Fluids.WATER))
                ? hitPos
                : hitPos.relative(hit.getDirection());

        if (!level.isInsideBuildHeight(placePos.getY())) {
            return InteractionResult.FAIL;
        }

        BlockState targetState = level.getBlockState(placePos);
        boolean isWater = level.getFluidState(placePos).getType() == Fluids.WATER
                && level.getFluidState(placePos).isSource();
        boolean canPlace = targetState.isAir()
                || (targetState.canBeReplaced()
                    && !targetState.is(ModBlocks.AURORAL_MOTE_LIGHT.get())
                    && (level.getFluidState(placePos).isEmpty() || isWater));

        if (!canPlace) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            // Spawn cold cyan-emerald particle ray along the beam
            Vec3 start = eyePos.add(0.0D, -0.12D, 0.0D);
            Vec3 end = Vec3.atCenterOf(placePos);
            Vec3 delta = end.subtract(start);
            double dist = delta.length();
            int steps = Math.max(4, (int) (dist * 2.5D));
            for (int i = 1; i <= steps; i++) {
                double t = (double) i / (double) steps;
                Vec3 pt = start.add(delta.scale(t));
                int color = (i % 2 == 0) ? 0x22D3EE : 0x10B981;
                serverLevel.sendParticles(new DustParticleOptions(color, 0.75F),
                        pt.x, pt.y, pt.z, 1, 0.02D, 0.02D, 0.02D, 0.0D);
                if (i % 4 == 0) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            pt.x, pt.y, pt.z, 1, 0.01D, 0.01D, 0.01D, 0.005D);
                }
            }

            BlockState moteState = ModBlocks.AURORAL_MOTE_LIGHT.get().defaultBlockState()
                    .setValue(AuroralMoteLightBlock.WATERLOGGED, isWater);
            serverLevel.setBlock(placePos, moteState, 3);

            serverLevel.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.9F, 1.35F);
            serverLevel.playSound(null, placePos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 0.85F, 1.4F);

            player.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
            EquipmentSlot equipSlot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            stack.hurtAndBreak(1, player, equipSlot);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("Right-Click: Projects a 32-block aurora ray that conjures a cold glowing mote light.")
                .withStyle(ChatFormatting.AQUA));
    }
}
