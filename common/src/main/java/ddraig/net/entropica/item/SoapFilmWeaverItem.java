package ddraig.net.entropica.item;

import ddraig.net.entropica.entity.forcefield.ForcefieldBarrierEntity;
import ddraig.net.entropica.forcefield.BarrierFilterMode;
import ddraig.net.entropica.forcefield.BarrierShape;
import ddraig.net.entropica.registry.ModEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

/**
 * Handheld instrument to weave, shape, and dispel soap-film forcefield barriers in mid-air.
 */
public class SoapFilmWeaverItem extends Item {

    public SoapFilmWeaverItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (player.isShiftKeyDown()) {
            // Cycle active shape mode
            if (!level.isClientSide()) {
                BarrierShape next = cycleShape(stack);
                player.displayClientMessage(Component.literal("§d[Soap-Film Weaver] §fShape: §b" + next.getDisplayName()), true);
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.PLAYERS, 0.8F, 1.4F);
            }
            return InteractionResult.SUCCESS;
        }

        // Raycast up to 6 blocks, or place 3.5 blocks in front of eyes
        Vec3 eyePos = player.getEyePosition();
        Vec3 lookVec = player.getViewVector(1.0F);
        Vec3 targetPos = eyePos.add(lookVec.scale(6.0));

        BlockHitResult blockHit = level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        Vec3 spawnPos = (blockHit.getType() != HitResult.Type.MISS) ? blockHit.getLocation() : eyePos.add(lookVec.scale(3.5));

        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            BarrierShape currentShape = getShape(stack);
            BarrierFilterMode currentFilter = getFilter(stack);

            ForcefieldBarrierEntity barrier = new ForcefieldBarrierEntity(ModEntityTypes.FORCEFIELD_BARRIER.get(), level);
            barrier.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            barrier.setYRot(player.getYRot());
            barrier.setXRot(currentShape == BarrierShape.PLANAR_QUAD ? 0.0F : player.getXRot()); // Flat vertical plane by default for quads
            barrier.setShape(currentShape);
            barrier.setFilterMode(currentFilter);
            barrier.setOwnerUUID(player.getUUID());
            barrier.setOwnerName(player.getName().getString());

            // Default dimensions
            switch (currentShape) {
                case PLANAR_QUAD -> { barrier.setWidth(4.0F); barrier.setHeight(3.5F); }
                case CIRCULAR_DISC -> barrier.setRadius(3.5F);
                case HEMISPHERICAL_DOME -> barrier.setRadius(5.0F);
                case SPHERICAL_BUBBLE -> barrier.setRadius(4.0F);
                case CYLINDER -> { barrier.setRadius(3.0F); barrier.setHeight(6.0F); }
                case CONVEX_POLYGON -> { barrier.setWidth(5.0F); barrier.setHeight(4.0F); }
            }

            serverLevel.addFreshEntity(barrier);

            // Audio & particles
            level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.END_ROD, spawnPos.x, spawnPos.y, spawnPos.z, 20, 0.5, 0.5, 0.5, 0.05);

            player.displayClientMessage(
                    Component.literal("§d[Soap-Film Weaver] §fFormed §b" + currentShape.getDisplayName() + " §8(§e" + currentFilter.getDisplayName() + "§8)"),
                    true
            );
        }

        return InteractionResult.SUCCESS;
    }

    public static BarrierShape getShape(ItemStack stack) {
        // Default to planar quad
        return BarrierShape.PLANAR_QUAD;
    }

    public static BarrierShape cycleShape(ItemStack stack) {
        BarrierShape current = getShape(stack);
        int nextOrd = (current.ordinal() + 1) % BarrierShape.values().length;
        return BarrierShape.fromOrdinal(nextOrd);
    }

    public static BarrierFilterMode getFilter(ItemStack stack) {
        return BarrierFilterMode.ALL_ENTITIES;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, display, tooltipComponents, tooltipFlag);
        tooltipComponents.accept(Component.literal("§7Weaves paper-thin soap-film barriers in mid-air."));
        tooltipComponents.accept(Component.literal("§eRight-Click§7: Cast barrier"));
        tooltipComponents.accept(Component.literal("§eShift + Right-Click§7: Cycle shape"));
        tooltipComponents.accept(Component.literal("§cInteract with Barrier§7: Dispel (creator only) / Shift: Cycle filter"));
        tooltipComponents.accept(Component.literal("§8Does not occupy block space. Bounces entities on arrival."));
    }
}
