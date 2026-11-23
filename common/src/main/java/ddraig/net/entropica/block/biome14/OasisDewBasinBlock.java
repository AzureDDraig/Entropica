package ddraig.net.entropica.block.biome14;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.registry.ModEffects;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

/**
 * Oasis Dew Basin — sculpted Vitae Spring Stone, Amber-Wood, and copper thermal spring font.
 * Warms players within 10 blocks (clearing freeze, Poison, and Hunger and granting Oasis Renewal),
 * keeps nearby Farmland hydrated in polar climates, and infuses Amber Chunks into Bottled Amber Nectar.
 */
public class OasisDewBasinBlock extends Block {
    public static final MapCodec<OasisDewBasinBlock> CODEC = simpleCodec(OasisDewBasinBlock::new);
    public static final BooleanProperty INFUSED = BooleanProperty.create("infused");

    protected static final VoxelShape SHAPE = Block.box(1.5D, 0.0D, 1.5D, 14.5D, 13.5D, 14.5D);

    public OasisDewBasinBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(INFUSED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(INFUSED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            pulseThermalBasin(serverLevel, pos);
            if (!serverLevel.getBlockTicks().hasScheduledTick(pos, this)) {
                serverLevel.scheduleTick(pos, this, 40);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        pulseThermalBasin(level, pos);
        if (!level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, 40);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.is(this)) {
            pulseThermalBasin(level, pos);
            if (level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 18.0D, false) != null
                    && !level.getBlockTicks().hasScheduledTick(pos, this)) {
                level.scheduleTick(pos, this, 40);
            }
        }
    }

    public static void pulseThermalBasin(ServerLevel level, BlockPos pos) {
        Vec3 center = Vec3.atCenterOf(pos);
        double radiusSq = 10.0D * 10.0D;
        AABB box = new AABB(pos).inflate(10.0D);
        List<Player> players = level.getEntitiesOfClass(Player.class, box,
                p -> p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(center) <= radiusSq);

        Holder<MobEffect> renewalHolder = resolveOasisRenewalHolder();
        for (Player player : players) {
            player.setTicksFrozen(0);
            if (player.hasEffect(MobEffects.POISON)) {
                player.removeEffect(MobEffects.POISON);
            }
            if (player.hasEffect(MobEffects.HUNGER)) {
                player.removeEffect(MobEffects.HUNGER);
            }
            if (renewalHolder != null) {
                player.addEffect(new MobEffectInstance(renewalHolder, 100, 0, true, false, true));
            }
        }

        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int dy = -1; dy <= 1; dy++) {
            int y = pos.getY() + dy;
            if (y < level.getMinY() || y >= level.getMaxY()) {
                continue;
            }
            for (int dx = -6; dx <= 6; dx++) {
                for (int dz = -6; dz <= 6; dz++) {
                    cursor.set(pos.getX() + dx, y, pos.getZ() + dz);
                    if (!level.hasChunkAt(cursor)) {
                        continue;
                    }
                    BlockState farmState = level.getBlockState(cursor);
                    if (farmState.is(Blocks.FARMLAND) && farmState.getValue(FarmBlock.MOISTURE) < FarmBlock.MAX_MOISTURE) {
                        level.setBlock(cursor.immutable(), farmState.setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE), 2);
                    }
                }
            }
        }
    }

    private static Holder<MobEffect> resolveOasisRenewalHolder() {
        if (ModEffects.OASIS_RENEWAL.isBound()) {
            return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(ModEffects.OASIS_RENEWAL.get());
        }
        MobEffect fallback = BuiltInRegistries.MOB_EFFECT
                .getOptional(ResourceLocation.fromNamespaceAndPath("entropica", "oasis_renewal"))
                .orElse(null);
        return fallback != null ? BuiltInRegistries.MOB_EFFECT.wrapAsHolder(fallback) : null;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean isAmberChunk = (ModItems.AMBER_CHUNK.isBound() && stack.is(ModItems.AMBER_CHUNK.get()))
                || ResourceLocation.fromNamespaceAndPath("entropica", "amber_chunk")
                        .equals(BuiltInRegistries.ITEM.getKey(stack.getItem()));

        if (isAmberChunk && !state.getValue(INFUSED)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                stack.consume(1, player);
                serverLevel.setBlock(pos, state.setValue(INFUSED, true), 3);
                serverLevel.playSound(null, pos, SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.BLOCKS, 0.85F, 1.2F);
                serverLevel.playSound(null, pos, SoundEvents.HONEY_BLOCK_PLACE, SoundSource.BLOCKS, 0.85F, 1.05F);
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                        12, 0.25D, 0.2D, 0.25D, 0.03D);
            }
            return InteractionResult.SUCCESS;
        }

        if (stack.is(Items.GLASS_BOTTLE)) {
            if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
                if (state.getValue(INFUSED)) {
                    ItemStack nectarStack = ModItems.AMBER_NECTAR_BOTTLE.isBound()
                            ? new ItemStack(ModItems.AMBER_NECTAR_BOTTLE.get())
                            : PotionContents.createItemStack(Items.POTION, Potions.WATER);
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, nectarStack));
                    serverLevel.setBlock(pos, state.setValue(INFUSED, false), 3);
                    serverLevel.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                            pos.getX() + 0.5D, pos.getY() + 0.8D, pos.getZ() + 0.5D,
                            10, 0.24D, 0.18D, 0.24D, 0.02D);
                } else {
                    ItemStack waterStack = PotionContents.createItemStack(Items.POTION, Potions.WATER);
                    player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, waterStack));
                    player.setTicksFrozen(0);
                    serverLevel.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.SUCCESS;
        }

        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            player.setTicksFrozen(0);
            pulseThermalBasin(serverLevel, pos);
            serverLevel.playSound(null, pos, SoundEvents.AMBIENT_UNDERWATER_ENTER, SoundSource.BLOCKS, 0.55F, 1.35F);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    pos.getX() + 0.5D, pos.getY() + 0.75D, pos.getZ() + 0.5D,
                    6, 0.22D, 0.12D, 0.22D, 0.01D);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        boolean infused = state.getValue(INFUSED);
        if (random.nextInt(infused ? 3 : 5) == 0) {
            double x = pos.getX() + 0.28D + random.nextDouble() * 0.44D;
            double y = pos.getY() + 0.72D;
            double z = pos.getZ() + 0.28D + random.nextDouble() * 0.44D;
            if (infused && random.nextBoolean()) {
                level.addParticle(ParticleTypes.END_ROD, x, y + 0.05D, z, 0.0D, 0.015D, 0.0D);
            } else {
                level.addParticle(ParticleTypes.CLOUD, x, y, z, 0.0D, 0.018D, 0.0D);
            }
        }
    }
}
