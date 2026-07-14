package ddraig.net.entropica.client;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class ModItemTintSources {

    public record AmpouleTint() implements ItemTintSource {
        public static final MapCodec<AmpouleTint> MAP_CODEC = MapCodec.unit(new AmpouleTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType type = null;
            if (stack.getItem() instanceof VisFumeAmpouleItem) {
                type = VisFumeAmpouleItem.getEssenceType(stack);
            }
            if (type != null) {
                int[] rgb = type.getCurrentRGB(System.currentTimeMillis() / 50);
                return (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record EssenceTint() implements ItemTintSource {
        public static final MapCodec<EssenceTint> MAP_CODEC = MapCodec.unit(new EssenceTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType type = null;
            if (stack.getItem() instanceof EssenceItem) {
                type = EssenceItem.getEssenceType(stack);
            } else if (stack.getItem() instanceof EssenceAmpouleItem) {
                type = EssenceAmpouleItem.getEssenceType(stack);
            }
            if (type != null) {
                int[] rgb = type.getCurrentRGB(System.currentTimeMillis() / 50);
                return (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record GeneratorTint() implements ItemTintSource {
        public static final MapCodec<GeneratorTint> MAP_CODEC = MapCodec.unit(new GeneratorTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            EssenceType[] types = EssenceType.values();
            int index = (int) ((System.currentTimeMillis() / 1000) % types.length);
            return 0xFF000000 | types[index].getColorInt();
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record FumeGlassTint() implements ItemTintSource {
        public static final MapCodec<FumeGlassTint> MAP_CODEC = MapCodec.unit(new FumeGlassTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }
}
