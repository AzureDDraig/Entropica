package ddraig.net.entropica.worldgen.structure;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.item.biome1.AegisShieldingLiningItem;
import ddraig.net.entropica.item.biome1.ThermalInsulatedBlanketItem;
import ddraig.net.entropica.item.biome3.CryoInsulatedArmorItem;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.registry.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

/**
 * Structure 1: Frost-Veiled Glacial Runic Spire (18 x 18 x 45 hexagonal runic blue-ice tower
 * with 3 packed-ice flying buttresses, Y+20 Frost Scriptorium, waterfall-concealed Runic Vault
 * trap basement, Y+42 Aegis Crystal apex, and interior freezing/hypothermia hazard).
 */
public class FrostVeiledRunicSpireStructure extends Structure {

    public static final MapCodec<FrostVeiledRunicSpireStructure> CODEC =
            simpleCodec(FrostVeiledRunicSpireStructure::new);

    public FrostVeiledRunicSpireStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        int centerX = chunkPos.getMiddleBlockX();
        int centerZ = chunkPos.getMiddleBlockZ();

        int surfaceY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
        int oceanFloorY = context.chunkGenerator().getFirstOccupiedHeight(
                centerX, centerZ, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        // Reject deep open water columns; require a solid glacier shelf or cliff terrace above sea level
        if (oceanFloorY < 62 || surfaceY - oceanFloorY > 3) {
            return Optional.empty();
        }

        int hNW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 6, centerZ - 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hNE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 6, centerZ - 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSW = context.chunkGenerator().getFirstOccupiedHeight(
                centerX - 6, centerZ + 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());
        int hSE = context.chunkGenerator().getFirstOccupiedHeight(
                centerX + 6, centerZ + 6, Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor(), context.randomState());

        int minCornerY = Math.min(Math.min(hNW, hNE), Math.min(hSW, hSE));
        int maxCornerY = Math.max(Math.max(hNW, hNE), Math.max(hSW, hSE));
        // Allow up to 28 blocks corner relief so Spires can anchor onto Silent Fjord basalt cliff terraces (Y=63..120) as well as Blue-Glacier Shelves
        if (minCornerY < 58 || maxCornerY - minCornerY > 28) {
            return Optional.empty();
        }

        int baseY = Mth.clamp(Math.min(surfaceY, (minCornerY + maxCornerY) / 2), 63, 120);
        if (baseY + 48 >= context.heightAccessor().getMaxY()) {
            return Optional.empty();
        }

        BlockPos anchorPos = new BlockPos(centerX, baseY, centerZ);
        return Optional.of(new GenerationStub(anchorPos, builder ->
                builder.addPiece(new FrostVeiledRunicSpirePiece(centerX, baseY, centerZ))));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.FROST_VEILED_GLACIAL_RUNIC_SPIRE.get();
    }

    /**
     * Evaluates the Spire's interior freezing/hypothermia hazard every 100 ticks (5 seconds)
     * for survival players standing inside a generated {@link FrostVeiledRunicSpireStructure}.
     */
    public static void tickSpireHypothermiaHazard(ServerPlayer player) {
        if (!player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.tickCount <= 0 || player.tickCount % 100 != 0) {
            return;
        }

        ServerLevel serverLevel = player.level();
        var structureRegistryOpt = serverLevel.registryAccess().lookup(Registries.STRUCTURE);
        if (structureRegistryOpt.isEmpty()) {
            return;
        }
        var spireHolderOpt = structureRegistryOpt.get().get(ModStructures.FROST_VEILED_GLACIAL_RUNIC_SPIRE_KEY);
        if (spireHolderOpt.isEmpty()) {
            return;
        }

        BlockPos playerPos = player.blockPosition();
        StructureStart start = serverLevel.structureManager().getStructureWithPieceAt(playerPos, spireHolderOpt.get().value());
        if (!start.isValid()) {
            return;
        }

        if (hasAegisFrostProtection(player)) {
            return;
        }

        player.setTicksFrozen(Math.min(player.getTicksRequiredToFreeze() + 40, player.getTicksFrozen() + 90));
        player.hurtServer(serverLevel, player.damageSources().freeze(), 2.0F);
    }

    public static boolean hasAegisFrostProtection(Player player) {
        if (player == null) {
            return false;
        }
        if (CryoInsulatedArmorItem.hasAnyCryoInsulation(player)) {
            return true;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(Items.LEATHER_HELMET)
                || player.getItemBySlot(EquipmentSlot.CHEST).is(Items.LEATHER_CHESTPLATE)
                || player.getItemBySlot(EquipmentSlot.LEGS).is(Items.LEATHER_LEGGINGS)
                || player.getItemBySlot(EquipmentSlot.FEET).is(Items.LEATHER_BOOTS)) {
            return true;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof AegisShieldingLiningItem
                    || stack.getItem() instanceof ThermalInsulatedBlanketItem
                    || (ModItems.AEGIS_SHIELDING_LINING.isBound() && stack.is(ModItems.AEGIS_SHIELDING_LINING.get()))
                    || (ModItems.THERMAL_INSULATED_BLANKET.isBound() && stack.is(ModItems.THERMAL_INSULATED_BLANKET.get()))) {
                return true;
            }
        }
        return false;
    }
}
