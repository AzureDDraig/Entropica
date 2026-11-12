package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.item.DynamicVisWeaponItem;
import ddraig.net.entropica.network.WeaponNamingPayload;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.component.VisWeaponState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class WeaponNamingScreen extends Screen {

    private EditBox nameField;
    private boolean submitted = false;

    public WeaponNamingScreen() {
        super(Component.translatable("msg.entropica.name_weapon"));
    }

    @Override
    protected void init() {
        super.init();

        this.nameField = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 20, 200, 20, Component.translatable("msg.entropica.weapon_name"));
        this.nameField.setMaxLength(50);
        this.addRenderableWidget(this.nameField);

        this.addRenderableWidget(Button.builder(Component.translatable("msg.entropica.confirm_naming"), button -> {
            this.submitName();
        }).bounds(this.width / 2 - 60, this.height / 2 + 15, 120, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0xCC000000);
        guiGraphics.drawCenteredString(this.font, "Name this crafted tool/weapon", this.width / 2, this.height / 2 - 40, 0xFFFFFF);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        this.submitName();
    }

    private void submitName() {
        if (!this.submitted) {
            this.submitted = true;

            if (this.minecraft != null && this.minecraft.player != null) {
                // 1. Flip the flag locally to prevent it reopening immediately
                for (int i = 0; i < this.minecraft.player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = this.minecraft.player.getInventory().getItem(i);
                    if (stack.getItem() instanceof DynamicVisWeaponItem && stack.has(ModDataComponents.VIS_WEAPON_STATE.get())) {
                        VisWeaponState state = stack.get(ModDataComponents.VIS_WEAPON_STATE.get());
                        if (state != null && !state.hasInitializedName()) {
                            stack.set(ModDataComponents.VIS_WEAPON_STATE.get(), state.withInitializedName(true));
                            break;
                        }
                    }
                }

                // 2. Bulletproof vanilla fallback for sending packets!
                if (this.minecraft.getConnection() != null) {
                    this.minecraft.getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                            new WeaponNamingPayload(this.nameField.getValue())
                    ));
                }

                // 3. Close UI
                this.minecraft.setScreen(null);
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}