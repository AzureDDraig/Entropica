package ddraig.net.entropica.client;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.item.EssenceAmpouleItem;
import ddraig.net.entropica.item.EssenceItem;
import ddraig.net.entropica.item.VisFumeAmpouleItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import ddraig.net.entropica.block.entity.EntropicCoreBlockEntity;
import java.util.Map;
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

    public record ShardTint() implements ItemTintSource {
        public static final MapCodec<ShardTint> MAP_CODEC = MapCodec.unit(new ShardTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            ClientLevel clientLevel = level != null ? level : Minecraft.getInstance().level;
            LivingEntity living = entity != null ? entity : Minecraft.getInstance().player;

            EssenceType type = EssenceType.REGULAR;
            if (clientLevel != null && living != null) {
                BlockPos pos = living.blockPosition();

                // 1. Search for nearest Entropic Core node within smaller radius (24 blocks)
                EssenceType nodeType = findNearestNodeEssence(clientLevel, pos, 24);
                if (nodeType != null) {
                    type = nodeType;
                } else {
                    // 2. Biome attunement fallback
                    type = getBiomeEssence(clientLevel, pos);
                }
            }

            int[] rgb = type.getCurrentRGB(System.currentTimeMillis() / 50);
            return (0xFF << 24) | (rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }

        private static EssenceType findNearestNodeEssence(Level level, BlockPos pos, int radius) {
            BlockPos.MutableBlockPos scanPos = new BlockPos.MutableBlockPos();
            EntropicCoreBlockEntity nearestCore = null;
            double nearestDistSq = Double.MAX_VALUE;

            for (int x = -radius; x <= radius; x += 3) {
                for (int y = -radius; y <= radius; y += 3) {
                    for (int z = -radius; z <= radius; z += 3) {
                        for (int dx = 0; dx < 3 && (x + dx) <= radius; dx++) {
                            for (int dy = 0; dy < 3 && (y + dy) <= radius; dy++) {
                                for (int dz = 0; dz < 3 && (z + dz) <= radius; dz++) {
                                    scanPos.setWithOffset(pos, x + dx, y + dy, z + dz);
                                    double distSq = pos.distSqr(scanPos);
                                    if (distSq > (long) radius * radius || distSq >= nearestDistSq) continue;

                                    if (level.getBlockEntity(scanPos) instanceof EntropicCoreBlockEntity core) {
                                        nearestCore = core;
                                        nearestDistSq = distSq;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (nearestCore != null) {
                Map<EssenceType, Integer> pool = nearestCore.getEssencePool();
                EssenceType dominant = null;
                int maxAmount = 0;
                for (Map.Entry<EssenceType, Integer> entry : pool.entrySet()) {
                    if (entry.getValue() > maxAmount) {
                        maxAmount = entry.getValue();
                        dominant = entry.getKey();
                    }
                }
                return dominant;
            }
            return null;
        }

        private static EssenceType getBiomeEssence(Level level, BlockPos pos) {
            net.minecraft.core.Holder<net.minecraft.world.level.biome.Biome> biomeHolder = level.getBiome(pos);
            if (biomeHolder.isBound()) {
                net.minecraft.resources.ResourceLocation biomeKey = biomeHolder.unwrapKey().map(net.minecraft.resources.ResourceKey::location).orElse(null);
                if (biomeKey != null) {
                    String path = biomeKey.getPath();
                    if (path.contains("nether") || path.contains("basalt") || path.contains("crimson") || path.contains("warped")) return EssenceType.NETHER;
                    if (path.contains("end_") || path.contains("the_end")) return EssenceType.VOID;
                    if (path.contains("desert") || path.contains("badlands") || path.contains("savanna")) return EssenceType.ARID;
                    if (path.contains("snow") || path.contains("frozen") || path.contains("ice")) return EssenceType.FROZEN;
                    if (path.contains("ocean") || path.contains("river") || path.contains("beach")) return EssenceType.WATER;
                    if (path.contains("forest") || path.contains("jungle") || path.contains("plains") || path.contains("meadow") || path.contains("cherry")) return EssenceType.NATURE;
                    if (path.contains("swamp") || path.contains("mangrove")) return EssenceType.UNDEAD;
                    if (path.contains("slopes") || path.contains("peaks") || path.contains("windswept") || path.contains("jagged")) return EssenceType.AIR;
                }
            }
            return EssenceType.REGULAR;
        }
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

    public record AestheticGlassTint() implements ItemTintSource {
        public static final MapCodec<AestheticGlassTint> MAP_CODEC = MapCodec.unit(new AestheticGlassTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            return ddraig.net.entropica.registry.AestheticGlassRegistry.getGlassColor(stack.getItem());
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }

    public record SpectralDyeTint() implements ItemTintSource {
        public static final MapCodec<SpectralDyeTint> MAP_CODEC = MapCodec.unit(new SpectralDyeTint());

        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
            if (stack.getItem() instanceof ddraig.net.entropica.item.SpectralDyeItem dyeItem) {
                return (0xFF << 24) | dyeItem.getColor();
            }
            return 0xFFFFFFFF;
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() { return MAP_CODEC; }
    }
}

