package ddraig.net.entropica.client.gui;

import ddraig.net.entropica.block.entity.SecondaryAstralLensBlockEntity;
import ddraig.net.entropica.network.AstralLensAimPayload;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class RealtimeLensAdjustmentScreen extends Screen {

    private final BlockPos lensPos;
    private float yaw;
    private float pitch;
    private String targetBlockName = "Open Air";
    private float targetDistance = 32.0f;
    private BlockPos targetBlockPos = null;

    private Entity originalCameraEntity;
    private CameraType originalCameraType;
    private Marker cameraDummy;

    public RealtimeLensAdjustmentScreen(BlockPos lensPos) {
        super(Component.literal("Secondary Astral Lens Gimbal Calibration"));
        this.lensPos = lensPos;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.level.getBlockEntity(lensPos) instanceof SecondaryAstralLensBlockEntity lens) {
            this.yaw = lens.getYaw();
            this.pitch = lens.getPitch();
        } else if (mc.player != null) {
            this.yaw = Mth.wrapDegrees(mc.player.getYRot());
            if (this.yaw < 0) this.yaw += 360.0f;
            this.pitch = Mth.clamp(-mc.player.getXRot(), -85.0f, 85.0f);
        }
    }

    public static void open(BlockPos pos) {
        Minecraft.getInstance().setScreen(new RealtimeLensAdjustmentScreen(pos));
    }

    @Override
    protected void init() {
        super.init();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && mc.level != null) {
            if (this.originalCameraEntity == null || this.originalCameraEntity == this.cameraDummy) {
                this.originalCameraEntity = mc.player;
            }
            if (this.originalCameraType == null) {
                this.originalCameraType = mc.options.getCameraType();
            }

            double camX = lensPos.getX() + 0.5;
            double camY = lensPos.getY() + 0.5625;
            double camZ = lensPos.getZ() + 0.5;

            if (this.cameraDummy == null || this.cameraDummy.isRemoved()) {
                this.cameraDummy = new Marker(EntityType.MARKER, mc.level);
            }
            this.cameraDummy.setPos(camX, camY, camZ);
            this.cameraDummy.xo = camX;
            this.cameraDummy.yo = camY;
            this.cameraDummy.zo = camZ;
            this.cameraDummy.setYRot(this.yaw);
            this.cameraDummy.setXRot(-this.pitch);
            this.cameraDummy.yRotO = this.yaw;
            this.cameraDummy.xRotO = -this.pitch;

            mc.setCameraEntity(this.cameraDummy);
            mc.options.setCameraType(CameraType.FIRST_PERSON);
        }
    }

    @Override
    public void onClose() {
        restoreCamera();
        super.onClose();
    }

    @Override
    public void removed() {
        restoreCamera();
        super.removed();
    }

    private void restoreCamera() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null) {
            if (mc.player != null) {
                mc.setCameraEntity(mc.player);
            }
            if (this.originalCameraType != null) {
                mc.options.setCameraType(this.originalCameraType);
            }
        }
        if (this.cameraDummy != null) {
            this.cameraDummy.discard();
            this.cameraDummy = null;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        this.yaw = (float) Mth.wrapDegrees(this.yaw - dragX * 0.25);
        if (this.yaw < 0) this.yaw += 360.0f;

        this.pitch = (float) Mth.clamp(this.pitch - dragY * 0.25, -85.0, 85.0);

        if (this.cameraDummy != null) {
            this.cameraDummy.setYRot(this.yaw);
            this.cameraDummy.setXRot(-this.pitch);
            this.cameraDummy.yRotO = this.yaw;
            this.cameraDummy.xRotO = -this.pitch;
        }

        updateLocalBlockEntity();
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDown) {
        if (event.button() == 0 && isDown) { // Left click locks lens
            lockAndClose();
            return true;
        }
        return super.mouseClicked(event, isDown);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_SPACE) { // SPACEBAR locks lens
            lockAndClose();
            return true;
        }
        return super.keyPressed(event);
    }

    private void updateLocalBlockEntity() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            BlockEntity be = mc.level.getBlockEntity(lensPos);
            if (be instanceof SecondaryAstralLensBlockEntity lens) {
                lens.setFocus(this.yaw, this.pitch, this.targetBlockName, true);
                lens.setBeamDistance(this.targetDistance);
            }
        }
    }

    private void lockAndClose() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.playSound(SoundEvents.BEACON_POWER_SELECT, 0.9f, 1.3f);
            mc.player.playSound(SoundEvents.CHISELED_BOOKSHELF_INSERT, 0.8f, 1.1f);
            mc.player.displayClientMessage(Component.literal("§6✦ [Secondary Lens] §7Gimbal locked onto: §b" + this.targetBlockName), true);
        }

        // Send network packet to sync lens orientation to server
        NetworkManager.sendToServer(new AstralLensAimPayload(lensPos, this.yaw, this.pitch, this.targetBlockName, true));

        onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            // Raycast along (yaw, pitch) from lens position
            float yawRad = (float) Math.toRadians(this.yaw);
            float pitchRad = (float) Math.toRadians(this.pitch);

            double vx = -Math.sin(yawRad) * Math.cos(pitchRad);
            double vy = Math.sin(pitchRad);
            double vz = Math.cos(yawRad) * Math.cos(pitchRad);
            Vec3 lookDir = new Vec3(vx, vy, vz).normalize();

            Vec3 start = new Vec3(lensPos.getX() + 0.5, lensPos.getY() + 0.5625, lensPos.getZ() + 0.5);
            double maxDist = 32.0;
            Vec3 end = start.add(lookDir.scale(maxDist));

            // 1. Clip world using OUTLINE
            BlockHitResult hit = mc.level.clip(new ClipContext(
                    start, end,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    CollisionContext.empty()
            ));

            double closestDist = maxDist;
            BlockPos targetPosFound = null;
            String foundName = null;

            if (hit.getType() == HitResult.Type.BLOCK && !hit.getBlockPos().equals(lensPos) && !hit.getBlockPos().equals(lensPos.below())) {
                closestDist = start.distanceTo(hit.getLocation());
                targetPosFound = hit.getBlockPos();
                foundName = mc.level.getBlockState(targetPosFound).getBlock().getName().getString();
            }

            // 2. Continuous volumetric bounding box check along the ray for all blocks
            Set<BlockPos> checked = new HashSet<>();
            for (double step = 0.2; step <= closestDist; step += 0.2) {
                Vec3 pt = start.add(lookDir.scale(step));
                BlockPos p = BlockPos.containing(pt);
                if (checked.add(p)) {
                    if (p.equals(lensPos) || p.equals(lensPos.below())) continue;
                    BlockState st = mc.level.getBlockState(p);
                    if (!st.isAir()) {
                        VoxelShape shape = st.getShape(mc.level, p);
                        AABB box = shape.isEmpty()
                                ? new AABB(p.getX(), p.getY(), p.getZ(), p.getX() + 1.0, p.getY() + 1.0, p.getZ() + 1.0)
                                : shape.bounds().move(p);
                        Optional<Vec3> clip = box.clip(start, end);
                        if (clip.isPresent()) {
                            double d = start.distanceTo(clip.get());
                            if (d < closestDist) {
                                closestDist = d;
                                targetPosFound = p;
                                foundName = st.getBlock().getName().getString();
                            }
                        }
                    }
                }
            }

            if (foundName != null) {
                this.targetBlockName = foundName;
                this.targetDistance = (float) closestDist;
                this.targetBlockPos = targetPosFound;
            } else {
                this.targetBlockName = "Open Air";
                this.targetDistance = 32.0f;
                this.targetBlockPos = null;
            }
        }

        // Draw In-World Gimbal Crosshair Reticle
        int reticleColor = 0xFF40D0FF; // Cyan
        guiGraphics.fill(centerX - 14, centerY - 1, centerX - 4, centerY + 1, reticleColor);
        guiGraphics.fill(centerX + 4, centerY - 1, centerX + 14, centerY + 1, reticleColor);
        guiGraphics.fill(centerX - 1, centerY - 14, centerX + 1, centerY - 4, reticleColor);
        guiGraphics.fill(centerX - 1, centerY + 4, centerX + 1, centerY + 14, reticleColor);
        guiGraphics.fill(centerX - 1, centerY - 1, centerX + 1, centerY + 1, 0xFFFFFFFF);

        // Circular bracket corners
        guiGraphics.fill(centerX - 18, centerY - 18, centerX - 12, centerY - 17, 0x8040D0FF);
        guiGraphics.fill(centerX - 18, centerY - 18, centerX - 17, centerY - 12, 0x8040D0FF);

        guiGraphics.fill(centerX + 12, centerY - 18, centerX + 18, centerY - 17, 0x8040D0FF);
        guiGraphics.fill(centerX + 17, centerY - 18, centerX + 18, centerY - 12, 0x8040D0FF);

        guiGraphics.fill(centerX - 18, centerY + 17, centerX - 12, centerY + 18, 0x8040D0FF);
        guiGraphics.fill(centerX - 18, centerY + 12, centerX - 17, centerY + 18, 0x8040D0FF);

        guiGraphics.fill(centerX + 12, centerY + 17, centerX + 18, centerY + 18, 0x8040D0FF);
        guiGraphics.fill(centerX + 17, centerY + 12, centerX + 18, centerY + 18, 0x8040D0FF);

        // Header and Data Readouts
        guiGraphics.drawCenteredString(this.font, "§6§l✦ SECONDARY ASTRAL LENS GIMBAL ✦", centerX, 20, 0xFFFFD700);
        guiGraphics.drawCenteredString(this.font, String.format("§eAzimuth: §f%.1f°  §e|  Elevation: §f%+.1f°", this.yaw, this.pitch), centerX, 34, 0xFFE0E0E0);

        if (!"Open Air".equals(this.targetBlockName)) {
            guiGraphics.drawCenteredString(this.font, String.format("§6✦ TARGET: §b%s §7[Distance: §f%.1fm§7]", this.targetBlockName, this.targetDistance), centerX, centerY + 28, 0xFFC0E8FF);
        } else {
            guiGraphics.drawCenteredString(this.font, "§7[Aiming into Open Air - Max Range 32m]", centerX, centerY + 28, 0xFF88A0B0);
        }

        guiGraphics.drawCenteredString(this.font, "§a[Drag Mouse to Aim  •  SPACEBAR / Click: Lock Lens  •  ESC to Exit]", centerX, this.height - 28, 0xFF80FF80);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }
}
