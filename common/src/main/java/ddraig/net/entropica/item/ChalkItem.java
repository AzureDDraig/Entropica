package ddraig.net.entropica.item;

import ddraig.net.entropica.block.ScribedChalkBlock;
import ddraig.net.entropica.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;

public class ChalkItem extends Item {
    private final int tier;
    private final boolean circuit;

    public ChalkItem(Properties properties, int tier, boolean circuit) {
        super(properties);
        this.tier = tier;
        this.circuit = circuit;
    }

    public int getTier() {
        return this.tier;
    }

    public boolean isCircuit() {
        return this.circuit;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clickedPos = context.getClickedPos();
        BlockState clickedState = level.getBlockState(clickedPos);

        // 1. If right-clicking an existing chalk block of the same circuit type, cycle its NodeType
        if (clickedState.is(ModBlocks.SCRIBED_CHALK.get()) && clickedState.getValue(ScribedChalkBlock.CIRCUIT) == this.circuit) {
            if (!level.isClientSide()) {
                ScribedChalkBlock.NodeType currentType = clickedState.getValue(ScribedChalkBlock.NODE_TYPE);
                ScribedChalkBlock.NodeType nextType;
                if (this.circuit) {
                    nextType = switch (currentType) {
                        case DEFAULT -> ScribedChalkBlock.NodeType.INPUT;
                        case INPUT -> ScribedChalkBlock.NodeType.SOURCE;
                        case SOURCE -> ScribedChalkBlock.NodeType.COLLECTION;
                        case COLLECTION -> ScribedChalkBlock.NodeType.AMPLIFIER;
                        case AMPLIFIER -> ScribedChalkBlock.NodeType.CAPACITOR;
                        case CAPACITOR -> ScribedChalkBlock.NodeType.RESONATOR;
                        case RESONATOR -> ScribedChalkBlock.NodeType.DIODE;
                        case DIODE -> ScribedChalkBlock.NodeType.OUTPUT;
                        case OUTPUT -> ScribedChalkBlock.NodeType.RUNE;
                        case RUNE -> ScribedChalkBlock.NodeType.DEFAULT;
                    };
                } else {
                    nextType = switch (currentType) {
                        case DEFAULT -> ScribedChalkBlock.NodeType.INPUT;
                        case INPUT -> ScribedChalkBlock.NodeType.SOURCE;
                        case SOURCE -> ScribedChalkBlock.NodeType.COLLECTION;
                        case COLLECTION -> ScribedChalkBlock.NodeType.OUTPUT;
                        case OUTPUT -> ScribedChalkBlock.NodeType.RUNE;
                        case RUNE -> ScribedChalkBlock.NodeType.DEFAULT;
                        default -> ScribedChalkBlock.NodeType.DEFAULT;
                    };
                }
                level.setBlock(clickedPos, clickedState.setValue(ScribedChalkBlock.NODE_TYPE, nextType), 3);
                if (nextType == ScribedChalkBlock.NodeType.DIODE) {
                    BlockEntity be = level.getBlockEntity(clickedPos);
                    if (be instanceof ddraig.net.entropica.block.entity.ScribedChalkBlockEntity chalkBE) {
                        chalkBE.setFacing(context.getPlayer() != null ? context.getPlayer().getDirection() : Direction.NORTH);
                    }
                }
                level.playSound(null, clickedPos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5f, 1.2f);

                Player player = context.getPlayer();
                if (player != null) {
                    String nodeName = switch (nextType) {
                        case DEFAULT -> "Default Path";
                        case INPUT -> "Input Node";
                        case SOURCE -> "Source Node";
                        case COLLECTION -> "Collection Node";
                        case AMPLIFIER -> "Amplifier Node";
                        case CAPACITOR -> "Capacitor Node";
                        case RESONATOR -> "Resonator Node";
                        case DIODE -> "Diode Node";
                        case OUTPUT -> "Output Node";
                        case RUNE -> "Rune Node";
                    };
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Node Type changed to: " + nodeName), true);

                    ItemStack stack = context.getItemInHand();
                    net.minecraft.world.entity.EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
            }
            return InteractionResult.SUCCESS;
        }

        Direction face = context.getClickedFace();
        // Chalk can only be scribed on the top face of a solid block
        if (face != Direction.UP || !level.getBlockState(clickedPos).isFaceSturdy(level, clickedPos, Direction.UP)) {
            return InteractionResult.PASS;
        }

        BlockPos placePos = clickedPos.above();
        BlockState currentState = level.getBlockState(placePos);

        if (currentState.isAir() || currentState.canBeReplaced()) {
            if (!level.isClientSide()) {
                Player player = context.getPlayer();
                ScribedChalkBlock.NodeType defaultType = (player != null && player.isCrouching()) 
                        ? ScribedChalkBlock.NodeType.RUNE 
                        : ScribedChalkBlock.NodeType.DEFAULT;

                BlockState chalkState = ModBlocks.SCRIBED_CHALK.get().defaultBlockState()
                        .setValue(ScribedChalkBlock.TIER, this.tier)
                        .setValue(ScribedChalkBlock.NODE_TYPE, defaultType)
                        .setValue(ScribedChalkBlock.CIRCUIT, this.circuit);
                
                level.setBlock(placePos, chalkState, 3);
                
                if (player != null) {
                    ItemStack stack = context.getItemInHand();
                    net.minecraft.world.entity.EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
                
                level.playSound(null, placePos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5f, 1.5f);
            }
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }
}
