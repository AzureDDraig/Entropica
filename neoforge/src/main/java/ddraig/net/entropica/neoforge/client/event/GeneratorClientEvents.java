package ddraig.net.entropica.neoforge.client.event;

import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.block.entity.CreativeMateriaGeneratorBlockEntity;
import ddraig.net.entropica.network.GeneratorScrollPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = Entropica.MODID, value = Dist.CLIENT)
public class GeneratorClientEvents {

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.level != null && mc.hitResult != null) {
            if (mc.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult bhr = (BlockHitResult) mc.hitResult;

                if (mc.level.getBlockEntity(bhr.getBlockPos()) instanceof CreativeMateriaGeneratorBlockEntity gen) {

                    double scrollAmount = event.getScrollDeltaY();
                    if (scrollAmount != 0) {
                        int delta = scrollAmount > 0 ? -1 : 1;

                        // 1. INSTANT CLIENT PREDICTION: Instantly changes the local block entity
                        gen.cycleType(delta);

                        // 2. FORCE RENDERING: Tells the chunk to update the block color immediately
                        mc.level.sendBlockUpdated(bhr.getBlockPos(), gen.getBlockState(), gen.getBlockState(), 8);

                        // 3. SECURE VANILLA PACKET: Sends the change to the server to make it official
                        dev.architectury.networking.NetworkManager.sendToServer(
                                new GeneratorScrollPayload(bhr.getBlockPos(), delta)
                        );

                        mc.player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.3F, 1.2F + (delta * 0.1f));
                        event.setCanceled(true);
                    }
                }
            }
        }
    }
}