package ddraig.net.entropica.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.Entropica;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.client.renderer.spell.BlockbenchCustomMesh;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import ddraig.net.entropica.spell.assembly.SpellAssembly;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

/**
 * Unified 3D {@link SpecialModelRenderer} for Entropica's custom Blockbench spellcrafting items
 * and workstation block-items:
 * <ul>
 *     <li>{@code Arcanum Focus}, {@code Wand}, {@code Staff}, {@code Grimoire} (with live per-socket Spellgem tinting)</li>
 *     <li>{@code Blank Spellgem} & {@code Inscribed Spellgem} (with primary Materia crystal tinting)</li>
 *     <li>{@code Arcane Scribing Table}, {@code Runic Anvil}, and all 4 {@code Autocast Pedestal} variants</li>
 * </ul>
 */
public class SpellCustomItemRenderer implements SpecialModelRenderer<SpellCustomItemRenderer.RenderData> {

    private static final int EMPTY_SOCKET_RGB = 0x454E68;

    public enum ModelKind {
        ARCANUM_FOCUS("arcanum_focus", "textures/item/arcanum_focus.png"),
        WAND("wand", "textures/item/wand.png"),
        STAFF("staff", "textures/item/staff.png"),
        GRIMOIRE("grimoire", "textures/item/grimoire.png"),
        BLANK_SPELLGEM("spellgem", "textures/item/blank_spellgem.png"),
        INSCRIBED_SPELLGEM("spellgem", "textures/item/inscribed_spellgem.png"),
        ARCANE_SCRIBING_TABLE("arcane_scribing_table", "textures/block/arcane_scribing_table.png"),
        RUNIC_ANVIL("runic_anvil", "textures/block/runic_anvil.png"),
        PEDESTAL_STONE("autocast_pedestal", "textures/block/autocast_pedestal_stone.png"),
        PEDESTAL_MARBLE("autocast_pedestal", "textures/block/autocast_pedestal_marble.png"),
        PEDESTAL_QUARTZ("autocast_pedestal", "textures/block/autocast_pedestal_quartz.png"),
        PEDESTAL_ASTRAL_SLATE("autocast_pedestal", "textures/block/autocast_pedestal_astral_slate.png");

        public final String meshName;
        public final ResourceLocation texture;

        ModelKind(String meshName, String texturePath) {
            this.meshName = meshName;
            this.texture = ResourceLocation.fromNamespaceAndPath(Entropica.MODID, texturePath);
        }
    }

    public record RenderData(ModelKind kind, int[] socketRgb) {
        public static final RenderData DEFAULT = new RenderData(ModelKind.BLANK_SPELLGEM, new int[]{0xFFFFFF});
    }

    @Override
    public void getExtents(Set<Vector3f> extents) {
        extents.add(new Vector3f(-0.75F, -0.5F, -0.75F));
        extents.add(new Vector3f(1.75F, 1.75F, 1.75F));
    }

    @Override
    public @Nullable RenderData extractArgument(ItemStack stack) {
        if (stack.isEmpty()) return RenderData.DEFAULT;
        Item item = stack.getItem();

        if (item == ModItems.ARCANUM_FOCUS.get()) {
            return new RenderData(ModelKind.ARCANUM_FOCUS, extractSocketColors(stack, 1));
        }
        if (item == ModItems.SPELL_WAND.get()) {
            return new RenderData(ModelKind.WAND, extractSocketColors(stack, 2));
        }
        if (item == ModItems.SPELL_STAFF.get()) {
            return new RenderData(ModelKind.STAFF, extractSocketColors(stack, 4));
        }
        if (item == ModItems.SPELL_GRIMOIRE.get()) {
            return new RenderData(ModelKind.GRIMOIRE, extractSocketColors(stack, 9));
        }
        if (item == ModItems.BLANK_SPELLGEM.get()) {
            return new RenderData(ModelKind.BLANK_SPELLGEM, new int[]{0xFFFFFF});
        }
        if (item == ModItems.SPELL_GEM.get()) {
            Optional<SpellAssembly> opt = SpellgemItem.getSpell(stack);
            if (opt.isPresent()) {
                return new RenderData(ModelKind.INSCRIBED_SPELLGEM, new int[]{resolveSpellBrightRgb(opt.get())});
            }
            return new RenderData(ModelKind.BLANK_SPELLGEM, new int[]{0xFFFFFF});
        }
        if (item == ModBlocks.ARCANE_SCRIBING_TABLE.get().asItem()) {
            return new RenderData(ModelKind.ARCANE_SCRIBING_TABLE, new int[0]);
        }
        if (item == ModBlocks.RUNIC_ANVIL.get().asItem()) {
            return new RenderData(ModelKind.RUNIC_ANVIL, new int[0]);
        }
        if (item == ModBlocks.AUTOCAST_PEDESTAL_STONE.get().asItem()) {
            return new RenderData(ModelKind.PEDESTAL_STONE, new int[0]);
        }
        if (item == ModBlocks.AUTOCAST_PEDESTAL_MARBLE.get().asItem()) {
            return new RenderData(ModelKind.PEDESTAL_MARBLE, new int[0]);
        }
        if (item == ModBlocks.AUTOCAST_PEDESTAL_QUARTZ.get().asItem()) {
            return new RenderData(ModelKind.PEDESTAL_QUARTZ, new int[0]);
        }
        if (item == ModBlocks.AUTOCAST_PEDESTAL_ASTRAL_SLATE.get().asItem()) {
            return new RenderData(ModelKind.PEDESTAL_ASTRAL_SLATE, new int[0]);
        }
        return RenderData.DEFAULT;
    }

    private static int[] extractSocketColors(ItemStack stack, int count) {
        int[] colors = new int[count];
        for (int i = 0; i < count; i++) {
            colors[i] = EMPTY_SOCKET_RGB;
        }
        CastingApparatusState state = stack.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (state == null) return colors;

        int max = Math.min(count, state.sockets().size());
        for (int i = 0; i < max; i++) {
            SocketedSpellgem sock = state.sockets().get(i);
            if (!sock.isEmpty() && sock.spell().isPresent()) {
                colors[i] = resolveSpellBrightRgb(sock.spell().get());
            }
        }
        return colors;
    }

    public static int resolveSpellBrightRgb(SpellAssembly spell) {
        EssenceType chosen = EssenceType.REGULAR;
        for (EssenceType t : spell.getDistinctMateriaTypes()) {
            if (t != null && t != EssenceType.REGULAR) {
                chosen = t;
                break;
            }
        }
        int base = chosen.getColorInt() & 0xFFFFFF;
        int r = ((base >> 16) & 0xFF);
        int g = ((base >> 8) & 0xFF);
        int b = (base & 0xFF);
        // Brighten 25% toward pure white so crystalline facets stay luminous
        r = Math.min(255, (r * 3 + 255) / 4);
        g = Math.min(255, (g * 3 + 255) / 4);
        b = Math.min(255, (b * 3 + 255) / 4);
        return (r << 16) | (g << 8) | b;
    }

    @Override
    public void submit(
            @Nullable RenderData state,
            ItemDisplayContext context,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            boolean hasFoil,
            int seed
    ) {
        final RenderData data = state != null ? state : RenderData.DEFAULT;
        final BlockbenchCustomMesh mesh = BlockbenchCustomMesh.getOrLoad(data.kind().meshName);

        poseStack.pushPose();
        applyDisplayTransform(data.kind(), context, poseStack);

        final Predicate<Set<String>> visFilter = switch (data.kind()) {
            case PEDESTAL_STONE, PEDESTAL_MARBLE, PEDESTAL_QUARTZ, PEDESTAL_ASTRAL_SLATE ->
                    groups -> !groups.contains("Slotted_Spellgem");
            default -> null;
        };

        final ToIntFunction<Set<String>> tintResolver = buildTintResolver(data);

        collector.submitCustomGeometry(
                poseStack,
                RenderType.entityCutoutNoCull(data.kind().texture),
                (pose, consumer) -> mesh.emit(pose, consumer, light, overlay, visFilter, tintResolver)
        );

        poseStack.popPose();
    }

    @Nullable
    private static ToIntFunction<Set<String>> buildTintResolver(RenderData data) {
        int[] cols = data.socketRgb();
        return switch (data.kind()) {
            case INSCRIBED_SPELLGEM -> groups -> cols.length > 0 ? cols[0] : 0xFFFFFF;
            case ARCANUM_FOCUS -> groups -> groups.contains("Focus_Spellgem") && cols.length > 0 ? cols[0] : 0xFFFFFF;
            case WAND -> groups -> {
                if (groups.contains("Pommel_Spellgem_Orb_1") && cols.length > 0) return cols[0];
                if (groups.contains("Collar_Spellgem_2") && cols.length > 1) return cols[1];
                return 0xFFFFFF;
            };
            case STAFF -> groups -> {
                if (groups.contains("Staff_Spellgem_1") && cols.length > 0) return cols[0];
                if (groups.contains("Staff_Spellgem_2") && cols.length > 1) return cols[1];
                if (groups.contains("Staff_Spellgem_3") && cols.length > 2) return cols[2];
                if (groups.contains("Staff_Spellgem_4") && cols.length > 3) return cols[3];
                return 0xFFFFFF;
            };
            case GRIMOIRE -> groups -> {
                if (groups.contains("Spine_Spellgem_1") && cols.length > 0) return cols[0];
                if (groups.contains("Left_Cover_Spellgem_2") && cols.length > 1) return cols[1];
                if (groups.contains("Left_Cover_Spellgem_3") && cols.length > 2) return cols[2];
                if (groups.contains("Left_Cover_Spellgem_4") && cols.length > 3) return cols[3];
                if (groups.contains("Left_Cover_Spellgem_5") && cols.length > 4) return cols[4];
                if (groups.contains("Right_Cover_Spellgem_6") && cols.length > 5) return cols[5];
                if (groups.contains("Right_Cover_Spellgem_7") && cols.length > 6) return cols[6];
                if (groups.contains("Right_Cover_Spellgem_8") && cols.length > 7) return cols[7];
                if (groups.contains("Right_Cover_Spellgem_9") && cols.length > 8) return cols[8];
                return 0xFFFFFF;
            };
            default -> null;
        };
    }

    private static void applyDisplayTransform(ModelKind kind, ItemDisplayContext context, PoseStack poseStack) {
        switch (kind) {
            case ARCANE_SCRIBING_TABLE, RUNIC_ANVIL -> {
                // 2-block-wide workstation: model X spans [-0.5..1.5] (center X=0.5), Y [0..1.1], Z [0..1.0] (center Z=0.5)
                poseStack.translate(0.5F, 0.5F, 0.5F);
                if (context == ItemDisplayContext.GUI) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                    poseStack.scale(0.42F, 0.42F, 0.42F);
                } else if (context == ItemDisplayContext.GROUND) {
                    poseStack.translate(0.0F, -0.2F, 0.0F);
                    poseStack.scale(0.28F, 0.28F, 0.28F);
                } else {
                    poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                    poseStack.scale(0.32F, 0.32F, 0.32F);
                }
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case PEDESTAL_STONE, PEDESTAL_MARBLE, PEDESTAL_QUARTZ, PEDESTAL_ASTRAL_SLATE -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                if (context == ItemDisplayContext.GUI) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(30.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(225.0F));
                    poseStack.scale(0.62F, 0.62F, 0.62F);
                } else if (context == ItemDisplayContext.GROUND) {
                    poseStack.translate(0.0F, -0.2F, 0.0F);
                    poseStack.scale(0.35F, 0.35F, 0.35F);
                } else {
                    poseStack.mulPose(Axis.YP.rotationDegrees(45.0F));
                    poseStack.scale(0.45F, 0.45F, 0.45F);
                }
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case GRIMOIRE -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                switch (context) {
                    case GUI -> {
                        poseStack.mulPose(Axis.XP.rotationDegrees(22.0F));
                        poseStack.mulPose(Axis.YP.rotationDegrees(155.0F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-12.0F));
                        poseStack.scale(0.88F, 0.88F, 0.88F);
                    }
                    case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                        poseStack.translate(0.0F, 0.29F, 0.0F);
                        poseStack.mulPose(Axis.YP.rotationDegrees(-180.0F));
                        poseStack.mulPose(Axis.ZP.rotationDegrees(39.0F));
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                    case FIRST_PERSON_RIGHT_HAND -> {
                        poseStack.mulPose(Axis.YP.rotationDegrees(154.0F));
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                    case FIRST_PERSON_LEFT_HAND -> {
                        poseStack.mulPose(Axis.YP.rotationDegrees(156.0F));
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                    case GROUND -> {
                        poseStack.translate(0.0F, -0.2F, 0.0F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(79.0F));
                        poseStack.scale(0.60F, 0.60F, 0.60F);
                    }
                    default -> poseStack.scale(0.75F, 0.75F, 0.75F);
                }
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case STAFF -> {
                // Y spans [-0.12..1.52] (center Y ~ 0.70)
                poseStack.translate(0.5F, 0.5F, 0.5F);
                switch (context) {
                    case GUI -> {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-42.0F));
                        poseStack.mulPose(Axis.YP.rotationDegrees(25.0F));
                        poseStack.scale(0.62F, 0.62F, 0.62F);
                    }
                    case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                        poseStack.translate(0.05F, 0.05F, 0.0F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(-12.0F));
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                    case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                        poseStack.translate(0.0F, 0.12F, 0.0F);
                        poseStack.scale(0.85F, 0.85F, 0.85F);
                    }
                    case GROUND -> {
                        poseStack.translate(0.0F, -0.15F, 0.0F);
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                        poseStack.scale(0.50F, 0.50F, 0.50F);
                    }
                    default -> poseStack.scale(0.60F, 0.60F, 0.60F);
                }
                poseStack.translate(-0.5F, -0.70F, -0.5F);
            }
            case WAND -> {
                // Y spans [0.07..1.36] (center Y ~ 0.72)
                poseStack.translate(0.5F, 0.5F, 0.5F);
                switch (context) {
                    case GUI -> {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(-42.0F));
                        poseStack.mulPose(Axis.YP.rotationDegrees(25.0F));
                        poseStack.scale(0.82F, 0.82F, 0.82F);
                    }
                    case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> {
                        poseStack.translate(0.05F, 0.05F, 0.0F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
                        poseStack.scale(0.80F, 0.80F, 0.80F);
                    }
                    case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> {
                        poseStack.translate(0.0F, 0.10F, 0.0F);
                        poseStack.scale(0.80F, 0.80F, 0.80F);
                    }
                    case GROUND -> {
                        poseStack.translate(0.0F, -0.2F, 0.0F);
                        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                        poseStack.scale(0.60F, 0.60F, 0.60F);
                    }
                    default -> poseStack.scale(0.75F, 0.75F, 0.75F);
                }
                poseStack.translate(-0.5F, -0.72F, -0.5F);
            }
            case ARCANUM_FOCUS -> {
                poseStack.translate(0.5F, 0.5F, 0.5F);
                if (context == ItemDisplayContext.GUI) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(25.0F));
                    poseStack.mulPose(Axis.YP.rotationDegrees(40.0F));
                    poseStack.scale(1.45F, 1.45F, 1.45F);
                } else if (context == ItemDisplayContext.GROUND) {
                    poseStack.translate(0.0F, -0.15F, 0.0F);
                    poseStack.scale(0.90F, 0.90F, 0.90F);
                } else {
                    poseStack.mulPose(Axis.YP.rotationDegrees(35.0F));
                    poseStack.scale(1.05F, 1.05F, 1.05F);
                }
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
            case BLANK_SPELLGEM, INSCRIBED_SPELLGEM -> {
                // Elongated Z crystal [Z=2.8..13.2] -> rotate upright in GUI & hands!
                poseStack.translate(0.5F, 0.5F, 0.5F);
                if (context == ItemDisplayContext.GUI) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(65.0F));
                    poseStack.mulPose(Axis.ZP.rotationDegrees(25.0F));
                    poseStack.scale(1.30F, 1.30F, 1.30F);
                } else if (context == ItemDisplayContext.GROUND) {
                    poseStack.translate(0.0F, -0.18F, 0.0F);
                    poseStack.scale(0.85F, 0.85F, 0.85F);
                } else {
                    poseStack.mulPose(Axis.XP.rotationDegrees(75.0F));
                    poseStack.scale(0.95F, 0.95F, 0.95F);
                }
                poseStack.translate(-0.5F, -0.5F, -0.5F);
            }
        }
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public SpecialModelRenderer<?> bake(SpecialModelRenderer.BakingContext context) {
            return new SpellCustomItemRenderer();
        }
    }
}
