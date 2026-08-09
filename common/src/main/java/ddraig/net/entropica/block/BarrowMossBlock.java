package ddraig.net.entropica.block;

import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BarrowMossBlock extends Block {
    private static final Map<UUID, Long> REGEN_COOLDOWNS = new ConcurrentHashMap<>();

    public BarrowMossBlock(Properties properties) {
        super(properties);
    }

    public static boolean tryApplyRegeneration(LivingEntity livingEntity, Level level) {
        long gameTime = level.getGameTime();
        Long lastTime = REGEN_COOLDOWNS.get(livingEntity.getUUID());
        if (lastTime == null || (gameTime - lastTime) >= 300L) { // 15-second cooldown (300 ticks)
            REGEN_COOLDOWNS.put(livingEntity.getUUID(), gameTime);
            livingEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 80, 0, false, false, true));
            return true;
        }
        return false;
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        super.stepOn(level, pos, state, entity);
        
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide) {
            if (livingEntity.isInvertedHealAndHarm()) {
                // Undead Mob Buff: Strength I & Speed I for 6 seconds
                livingEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 120, 0, false, true, true));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 120, 0, false, true, true));
            } else {
                // Living Non-Undead Entity: 1 Damage Trade-Off + Debuff Cleansing + 15s Regen Cooldown
                boolean hasDebuff = livingEntity.hasEffect(MobEffects.WITHER) || 
                                    livingEntity.hasEffect(MobEffects.POISON) || 
                                    livingEntity.hasEffect(MobEffects.WEAKNESS);

                if (hasDebuff) {
                    // 1. Deal 1 damage first as trade-off for cleansing
                    livingEntity.hurt(level.damageSources().magic(), 1.0F);

                    // 2. Remove debuffs
                    livingEntity.removeEffect(MobEffects.WITHER);
                    livingEntity.removeEffect(MobEffects.POISON);
                    livingEntity.removeEffect(MobEffects.WEAKNESS);
                }

                // 3. Apply Regeneration I if 15-second cooldown has passed
                tryApplyRegeneration(livingEntity, level);
            }
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        ItemStack held = player.getItemInHand(player.getUsedItemHand());
        if (held.is(Items.BONE_MEAL)) {
            if (!level.isClientSide) {
                if (!player.getAbilities().instabuild) {
                    held.shrink(1);
                }

                RandomSource random = level.getRandom();
                for (int i = 0; i < 6; i++) {
                    int dx = random.nextInt(3) - 1;
                    int dy = random.nextInt(3) - 1;
                    int dz = random.nextInt(3) - 1;
                    BlockPos targetPos = pos.offset(dx, dy, dz);
                    BlockState targetState = level.getBlockState(targetPos);

                    if (targetState.is(Blocks.STONE) || targetState.is(Blocks.DEEPSLATE) || 
                        targetState.is(Blocks.COBBLESTONE) || targetState.is(Blocks.DIRT) || 
                        targetState.is(Blocks.GRASS_BLOCK)) {
                        level.setBlockAndUpdate(targetPos, ModBlocks.BARROW_MOSS.get().defaultBlockState());
                    }
                }

                level.playSound(null, pos, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 1.0f, 1.0f);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (random.nextInt(3) == 0) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 1.05;
            double z = pos.getZ() + random.nextDouble();

            float r = 0.06f;
            float g = 0.72f;
            float b = 0.50f;

            level.addParticle(ModParticles.SPECTRUM_SPARKLE.get(), x, y, z, r, g, b);
        }
    }
}
