package ddraig.net.entropica.block.biome1;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Summit anchor atop the Frost-Veiled Glacial Runic Spire (Structure 1).
 * Feeding an Aegis Crystal Shard or Aegis Crystal Block awakens the anchor for 15 pulses
 * (every 80 ticks), granting nearby players Resistance I, clearing fire/frost buildup,
 * and suppressing the Spire's ambient hypothermia hazard within 24 blocks.
 */
public class GlacialLifeForceAnchorBlock extends Block {
    public static final MapCodec<GlacialLifeForceAnchorBlock> CODEC = simpleCodec(GlacialLifeForceAnchorBlock::new);

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final IntegerProperty PULSE_CHARGES = IntegerProperty.create("pulse_charges", 0, 15);

    private static final int MAX_PULSE_CHARGES = 15;
    private static final int PULSE_INTERVAL_TICKS = 80;
    private static final double AURA_RADIUS = 24.0D;

    private static final EquipmentSlot[] ARMOR_SLOTS = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.BODY
    };

    public GlacialLifeForceAnchorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ACTIVE, false)
                .setValue(PULSE_CHARGES, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE, PULSE_CHARGES);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean isShard = ModItems.AEGIS_CRYSTAL_SHARD.isBound() && stack.is(ModItems.AEGIS_CRYSTAL_SHARD.get());
        boolean isBlock = ModItems.AEGIS_CRYSTAL_BLOCK_ITEM.isBound() && stack.is(ModItems.AEGIS_CRYSTAL_BLOCK_ITEM.get());

        if (isShard || isBlock) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                if (!state.getValue(ACTIVE) || state.getValue(PULSE_CHARGES) < MAX_PULSE_CHARGES) {
                    stack.consume(1, player);
                }

                BlockState activated = state
                        .setValue(ACTIVE, true)
                        .setValue(PULSE_CHARGES, MAX_PULSE_CHARGES);
                serverLevel.setBlock(pos, activated, 3);
                serverLevel.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
                applyAnchorPulse(serverLevel, pos);
                serverLevel.scheduleTick(pos, this, PULSE_INTERVAL_TICKS);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && state.getValue(ACTIVE) && state.getValue(PULSE_CHARGES) > 0) {
            level.scheduleTick(pos, this, PULSE_INTERVAL_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(ACTIVE)) {
            return;
        }

        applyAnchorPulse(level, pos);

        int remaining = state.getValue(PULSE_CHARGES) - 1;
        if (remaining <= 0) {
            level.setBlock(pos, state.setValue(ACTIVE, false).setValue(PULSE_CHARGES, 0), 3);
            level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            level.setBlock(pos, state.setValue(PULSE_CHARGES, remaining), 3);
            level.scheduleTick(pos, this, PULSE_INTERVAL_TICKS);
        }
    }

    private static void applyAnchorPulse(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        AABB box = new AABB(pos).inflate(AURA_RADIUS);
        List<Player> players = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(center) <= AURA_RADIUS * AURA_RADIUS);

        for (Player p : players) {
            p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 0, true, true, true));
            p.clearFire();
            if (p.getTicksFrozen() > 0) {
                p.setTicksFrozen(0);
            }
        }

        level.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5D, pos.getY() + 1.1D, pos.getZ() + 0.5D,
                16, 0.4D, 0.5D, 0.4D, 0.03D);
        level.playSound(null, pos, SoundEvents.BEACON_AMBIENT, SoundSource.BLOCKS, 0.8F, 1.2F);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (state.getValue(ACTIVE) && random.nextInt(3) == 0) {
            double x = pos.getX() + 0.2D + random.nextDouble() * 0.6D;
            double y = pos.getY() + 0.8D + random.nextDouble() * 0.6D;
            double z = pos.getZ() + 0.2D + random.nextDouble() * 0.6D;
            level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0D, 0.02D, 0.0D);
        }
    }

    /**
     * Checks whether the entity is shielded from Spire hypothermia via any Aegis-named armor piece,
     * an Aegis Shielding Lining, or a Thermal Insulated Blanket in hands/inventory.
     */
    public static boolean hasAegisFrostProtection(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack equipped = entity.getItemBySlot(slot);
            if (!equipped.isEmpty()) {
                ResourceLocation key = BuiltInRegistries.ITEM.getKey(equipped.getItem());
                if (key != null && key.getPath().contains("aegis")) {
                    return true;
                }
            }
        }

        if (isProtectiveLiningOrBlanket(entity.getMainHandItem())
                || isProtectiveLiningOrBlanket(entity.getOffhandItem())) {
            return true;
        }

        if (entity instanceof Player player) {
            Inventory inv = player.getInventory();
            int size = inv.getContainerSize();
            for (int i = 0; i < size; i++) {
                if (isProtectiveLiningOrBlanket(inv.getItem(i))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean isProtectiveLiningOrBlanket(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return (ModItems.AEGIS_SHIELDING_LINING.isBound() && stack.is(ModItems.AEGIS_SHIELDING_LINING.get()))
                || (ModItems.THERMAL_INSULATED_BLANKET.isBound() && stack.is(ModItems.THERMAL_INSULATED_BLANKET.get()));
    }
}
