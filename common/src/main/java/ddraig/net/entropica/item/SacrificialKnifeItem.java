package ddraig.net.entropica.item;

import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.block.ScribedChalkBlock;
import ddraig.net.entropica.block.entity.ScribedChalkBlockEntity;
import ddraig.net.entropica.entity.EssenceOrbEntity;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class SacrificialKnifeItem extends Item {

    public SacrificialKnifeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ServerLevel serverLevel = (ServerLevel) level;
        
        // 1. Scan for a nearby output block entity in a 5-block radius
        ScribedChalkBlockEntity outputBE = null;
        BlockPos playerPos = player.blockPosition();
        
        outerLoop:
        for (int x = -5; x <= 5; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    BlockPos p = playerPos.offset(x, y, z);
                    if (level.getBlockEntity(p) instanceof ScribedChalkBlockEntity chalkBE) {
                        if (chalkBE.getBlockState().getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.OUTPUT) {
                            outputBE = chalkBE;
                            break outerLoop;
                        }
                    }
                }
            }
        }

        int baseDamage = 8;
        int extraDamage = 0;
        boolean hasUruz = false;
        boolean hasThurisaz = false;

        if (outputBE != null && outputBE.isProcessing()) {
            // Scan 9x9 footprint around output node for upgrade runes
            BlockPos outputPos = outputBE.getBlockPos();
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos p = outputPos.offset(dx, 0, dz);
                    if (level.getBlockEntity(p) instanceof ScribedChalkBlockEntity chalkBE) {
                        if (chalkBE.isInActiveCircle()) {
                            ItemStack rune = chalkBE.getStoredRune();
                            if (!rune.isEmpty()) {
                                if (rune.getItem() == ModItems.RUNE_URUZ.get()) {
                                    hasUruz = true;
                                }
                                if (rune.getItem() == ModItems.RUNE_THURISAZ.get()) {
                                    hasThurisaz = true;
                                }
                            }
                        }
                    }
                }
            }

            if (hasUruz) {
                extraDamage += 2;
                outputBE.setDoubleYield(true);
            }
            if (hasThurisaz) {
                extraDamage += 2;
                outputBE.setOverclockTicks(outputBE.getOverclockTicks()); // refresh overclock
                // Trigger instant completion by advancing progress to total
                // Note: ScribedChalkBlockEntity tick will finish it on the next tick
                outputBE.startRitual(outputBE.getStoredOrbisCell(), 1); // Trick to complete it
            }

            outputBE.setOverclockTicks(600); // 30 seconds overclock
            outputBE.setChanged();
        }

        int totalDamage = baseDamage + extraDamage;
        
        // Deal true magic damage
        player.hurt(level.damageSources().magic(), totalDamage);

        // Sound and particle effects for the sacrifice
        level.playSound(null, player.getX(), player.getY(), player.getZ(), 
                SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.8F);
        
        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, 
                player.getX(), player.getEyeY() - 0.3, player.getZ(), 
                15, 0.2, 0.2, 0.2, 0.1);

        if (player.isDeadOrDying()) {
            // Spawn Materia-Infused Zombie at death location
            Zombie zombie = EntityType.ZOMBIE.create(level, EntitySpawnReason.TRIGGERED);
            if (zombie != null) {
                zombie.setPos(player.getX(), player.getY(), player.getZ());
                zombie.setYRot(player.getYRot());
                zombie.setXRot(player.getXRot());
                zombie.setCustomName(Component.translatable("msg.entropica.materia_infused_zombie"));
                zombie.setCustomNameVisible(true);
                zombie.setPersistenceRequired();
                zombie.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, -1, 1)); // Resistance II
                zombie.addEffect(new MobEffectInstance(MobEffects.STRENGTH, -1, 0)); // Strength I
                level.addFreshEntity(zombie);
            }
        } else {
            // If the player survived and no ritual was overclocked, spawn essence orb
            if (outputBE == null || !outputBE.isProcessing()) {
                ItemStack orbStack = new ItemStack(ModItems.AVERAGE_ESSENCE.get());
                EssenceType orbType = level.random.nextBoolean() ? EssenceType.VITAE : EssenceType.BLOOD;
                EssenceItem.setEssenceType(orbStack, orbType);

                Vec3 look = player.getLookAngle();
                double spawnX = player.getX() + look.x * 1.2;
                double spawnY = player.getY() + player.getEyeHeight() / 2.0;
                double spawnZ = player.getZ() + look.z * 1.2;

                EssenceOrbEntity orb = new EssenceOrbEntity(level, spawnX, spawnY, spawnZ, orbStack);
                orb.setDeltaMovement(look.x * 0.1, 0.2, look.z * 0.1);
                level.addFreshEntity(orb);
            }
        }

        return InteractionResult.SUCCESS;
    }
}
