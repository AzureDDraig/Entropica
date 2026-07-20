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

        Player player = context.getPlayer();
        if (clickedState.is(ModBlocks.SCRIBED_CHALK.get()) && this.circuit && player != null && player.isCrouching()) {
            if (!level.isClientSide()) {
                BlockEntity be = level.getBlockEntity(clickedPos);
                if (be instanceof ddraig.net.entropica.block.entity.ScribedChalkBlockEntity chalkBE) {
                    net.minecraft.world.phys.Vec3 clickLoc = context.getClickLocation();
                    double relativeX = clickLoc.x() - (clickedPos.getX() + 0.5);
                    double relativeZ = clickLoc.z() - (clickedPos.getZ() + 0.5);
                    
                    double angle = Math.toDegrees(Math.atan2(relativeZ, relativeX));
                    if (angle < 0) angle += 360.0;
                    
                    int sector = (int) ((angle + 22.5) / 45.0) % 8;
                    ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8 clickDir8 = switch (sector) {
                        case 0 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.EAST;
                        case 1 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.SOUTH_EAST;
                        case 2 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.SOUTH;
                        case 3 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.SOUTH_WEST;
                        case 4 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.WEST;
                        case 5 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.NORTH_WEST;
                        case 6 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.NORTH;
                        case 7 -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.NORTH_EAST;
                        default -> ddraig.net.entropica.block.entity.ScribedChalkBlockEntity.Direction8.EAST;
                    };
                    
                    int currentOverride = chalkBE.getConnectionOverride(clickDir8);
                    int nextOverride = (currentOverride + 1) % 3; // 0 = DEFAULT, 1 = FORCE_CONNECT, 2 = FORCE_DISCONNECT
                    chalkBE.setConnectionOverride(clickDir8, nextOverride);
                    
                    // Keep neighbor in sync
                    BlockPos neighborPos = clickedPos.offset(clickDir8.getXOffset(), 0, clickDir8.getZOffset());
                    BlockEntity neighborBE = level.getBlockEntity(neighborPos);
                    if (neighborBE instanceof ddraig.net.entropica.block.entity.ScribedChalkBlockEntity neighborChalk) {
                        neighborChalk.setConnectionOverride(clickDir8.getOpposite(), nextOverride);
                    }
                    
                    String stateName = switch (nextOverride) {
                        case 0 -> "Default (Auto)";
                        case 1 -> "Forced Connect";
                        case 2 -> "Forced Disconnect";
                        default -> "Default (Auto)";
                    };
                    
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("Connection " + clickDir8.getName().toUpperCase() + " override changed to: " + stateName), true);
                    level.playSound(null, clickedPos, SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.5f, 1.2f);
                    
                    ItemStack stack = context.getItemInHand();
                    net.minecraft.world.entity.EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
                    stack.hurtAndBreak(1, player, slot);
                }
            }
            return InteractionResult.SUCCESS;
        }

        // Automatic alchemical shell drawing around predrawn magic circles
        if (clickedState.is(ModBlocks.SCRIBED_CHALK.get()) && 
            clickedState.getValue(ScribedChalkBlock.NODE_TYPE) == ScribedChalkBlock.NodeType.OUTPUT &&
            !clickedState.getValue(ScribedChalkBlock.CIRCUIT) && 
            this.circuit) {
            
            BlockEntity be = level.getBlockEntity(clickedPos);
            if (be instanceof ddraig.net.entropica.block.entity.ScribedChalkBlockEntity chalkBE) {
                int activeTier = chalkBE.getActiveCircleTier();
                if (activeTier > 0 && activeTier < 4) {
                    if (!level.isClientSide()) {
                        int outerTier = activeTier + 1;
                        int[][] offsets = ScribedChalkBlock.getOffsetsForTier(outerTier);
                        
                        BlockPos closestOffsetPos = null;
                        if (player != null) {
                            double minDist = Double.MAX_VALUE;
                            for (int[] offset : offsets) {
                                BlockPos p = clickedPos.offset(offset[0], 0, offset[1]);
                                double dist = p.distSqr(player.blockPosition());
                                if (dist < minDist) {
                                    minDist = dist;
                                    closestOffsetPos = p;
                                }
                            }
                        }
                        
                        int blocksScribed = 0;
                        for (int[] offset : offsets) {
                            BlockPos p = clickedPos.offset(offset[0], 0, offset[1]);
                            if (closestOffsetPos != null && p.equals(closestOffsetPos)) {
                                continue;
                            }
                            
                            BlockState currentState = level.getBlockState(p);
                            if (currentState.isAir() || currentState.canBeReplaced()) {
                                BlockState chalkState = ModBlocks.SCRIBED_CHALK.get().defaultBlockState()
                                        .setValue(ScribedChalkBlock.TIER, 1)
                                        .setValue(ScribedChalkBlock.NODE_TYPE, ScribedChalkBlock.NodeType.DEFAULT)
                                        .setValue(ScribedChalkBlock.CIRCUIT, true);
                                level.setBlock(p, chalkState, 3);
                                blocksScribed++;
                            }
                        }
                        
                        if (blocksScribed > 0 && player != null) {
                            ItemStack stack = context.getItemInHand();
                            net.minecraft.world.entity.EquipmentSlot slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND ? net.minecraft.world.entity.EquipmentSlot.MAINHAND : net.minecraft.world.entity.EquipmentSlot.OFFHAND;
                            stack.hurtAndBreak(blocksScribed, player, slot);
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aDrawing alchemical circuit shell around magic circle!"), true);
                        }
                        level.playSound(null, clickedPos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.7f, 1.2f);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        }

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
                        case DIODE -> ScribedChalkBlock.NodeType.AND_GATE;
                        case AND_GATE -> ScribedChalkBlock.NodeType.OR_GATE;
                        case OR_GATE -> ScribedChalkBlock.NodeType.NOT_GATE;
                        case NOT_GATE -> ScribedChalkBlock.NodeType.EXTRACTION;
                        case EXTRACTION -> ScribedChalkBlock.NodeType.OUTPUT;
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
                if (nextType == ScribedChalkBlock.NodeType.DIODE ||
                    nextType == ScribedChalkBlock.NodeType.EXTRACTION ||
                    nextType == ScribedChalkBlock.NodeType.AND_GATE ||
                    nextType == ScribedChalkBlock.NodeType.OR_GATE ||
                    nextType == ScribedChalkBlock.NodeType.NOT_GATE) {
                    BlockEntity be = level.getBlockEntity(clickedPos);
                    if (be instanceof ddraig.net.entropica.block.entity.ScribedChalkBlockEntity chalkBE) {
                        chalkBE.setFacing(context.getPlayer() != null ? context.getPlayer().getDirection() : Direction.NORTH);
                    }
                }
                level.playSound(null, clickedPos, SoundEvents.GRAVEL_PLACE, SoundSource.BLOCKS, 0.5f, 1.2f);

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
                        case AND_GATE -> "AND Gate Node";
                        case OR_GATE -> "OR Gate Node";
                        case NOT_GATE -> "NOT Gate Node";
                        case EXTRACTION -> "Extraction Node";
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
