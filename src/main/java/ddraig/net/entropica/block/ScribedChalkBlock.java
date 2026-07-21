package ddraig.net.entropica.block;

import ddraig.net.entropica.block.entity.ScribedChalkBlockEntity;
import ddraig.net.entropica.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.item.ItemStack;
import ddraig.net.entropica.api.EssenceType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.Ingredient;
import ddraig.net.entropica.recipe.MagicCircleRecipeInput;
import ddraig.net.entropica.recipe.MagicCircleRecipe;
import ddraig.net.entropica.registry.ModRecipes;
import java.util.List;
import java.util.Optional;

public class ScribedChalkBlock extends BaseEntityBlock {
    public static final IntegerProperty TIER = IntegerProperty.create("tier", 1, 4);
    public static final EnumProperty<NodeType> NODE_TYPE = EnumProperty.create("node_type", NodeType.class);
    public static final BooleanProperty CIRCUIT = BooleanProperty.create("circuit");

    protected static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);

    public ScribedChalkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(TIER, 1)
                .setValue(NODE_TYPE, NodeType.DEFAULT)
                .setValue(CIRCUIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    public boolean canChalkSurvive(LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        return level.getBlockState(belowPos).isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean isMoving) {
        if (!canChalkSurvive(level, pos)) {
            if (!level.isClientSide()) {
                checkForAndDestroyCircle(level, pos, state);
            }
            level.destroyBlock(pos, true);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TIER, NODE_TYPE, CIRCUIT);
    }

    public static final com.mojang.serialization.MapCodec<ScribedChalkBlock> CODEC = simpleCodec(ScribedChalkBlock::new);

    @Override
    protected com.mojang.serialization.MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public net.minecraft.world.InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, net.minecraft.world.entity.player.Player player, net.minecraft.world.phys.BlockHitResult hitResult) {
        // Redirection logic for magic circles: click anywhere on the circle redirects to the closest node!
        BlockPos centerPos = null;
        int circleTier = 0;

        BlockEntity clickBE = level.getBlockEntity(pos);
        if (clickBE instanceof ScribedChalkBlockEntity chalkBE && chalkBE.isInActiveCircle()) {
            // Find the center OUTPUT block of the active circle
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    BlockPos checkPos = pos.offset(dx, 0, dz);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.is(state.getBlock()) && checkState.getValue(NODE_TYPE) == NodeType.OUTPUT) {
                        BlockEntity be = level.getBlockEntity(checkPos);
                        if (be instanceof ScribedChalkBlockEntity cBE && cBE.isInActiveCircle()) {
                            // Find matching tier
                            for (int tier = 4; tier >= 1; tier--) {
                                if (checkPatternStatic(level, checkPos, checkState.getValue(CIRCUIT), tier) &&
                                    validateNodeCountsStatic(level, checkPos, tier)) {
                                    // Ensure this clicked block is on the boundary or center of this tier
                                    if (Math.max(Math.abs(pos.getX() - checkPos.getX()), Math.abs(pos.getZ() - checkPos.getZ())) <= tier || pos.equals(checkPos)) {
                                        centerPos = checkPos;
                                        circleTier = tier;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (centerPos != null) break;
                }
                if (centerPos != null) break;
            }
        }

        // If we are part of an active magic circle, find the closest node block based on its CURRENT SPINNING ANGLE!
        if (centerPos != null && circleTier > 0) {
            java.util.List<BlockPos> activeNodes = new java.util.ArrayList<>();
            for (int t = 1; t <= circleTier; t++) {
                int[][] offsets = getOffsetsForTier(t);
                for (int[] offset : offsets) {
                    BlockPos p = centerPos.offset(offset[0], 0, offset[1]);
                    BlockState pState = level.getBlockState(p);
                    if (pState.is(state.getBlock()) && pState.getValue(NODE_TYPE) != NodeType.DEFAULT) {
                        activeNodes.add(p);
                    }
                }
            }

            // Sort perimeter nodes by angle around center block
            BlockPos finalCenter = centerPos;
            activeNodes.sort((p1, p2) -> {
                double a1 = Math.atan2(p1.getZ() - finalCenter.getZ(), p1.getX() - finalCenter.getX());
                double a2 = Math.atan2(p2.getZ() - finalCenter.getZ(), p2.getX() - finalCenter.getX());
                if (a1 < 0) a1 += 2 * Math.PI;
                if (a2 < 0) a2 += 2 * Math.PI;
                return Double.compare(a1, a2);
            });

            // Calculate current rotation angle
            float speed = switch (circleTier) {
                case 1 -> 0.8f;
                case 2 -> 0.6f;
                case 3 -> 0.5f;
                default -> 0.4f;
            };
            double rotationAngle = Math.toRadians(-90.0f + level.getGameTime() * speed);

            BlockPos closestNode = centerPos;
            double hitX = hitResult.getLocation().x;
            double hitZ = hitResult.getLocation().z;
            
            // Check distance to static center block (Output node)
            double minDist = (centerPos.getX() + 0.5 - hitX) * (centerPos.getX() + 0.5 - hitX) + 
                              (centerPos.getZ() + 0.5 - hitZ) * (centerPos.getZ() + 0.5 - hitZ);
                              
            // Check distance to each spinning perimeter node's visual position
            int points = activeNodes.size();
            if (points > 0) {
                double angleStep = 2.0 * Math.PI / points;
                double polyRadius = (circleTier == 4) ? circleTier * 0.75f : circleTier * 0.7f;
                
                for (int i = 0; i < points; i++) {
                    double theta = rotationAngle + i * angleStep;
                    double vx = centerPos.getX() + 0.5 + polyRadius * Math.cos(theta);
                    double vz = centerPos.getZ() + 0.5 + polyRadius * Math.sin(theta);
                    
                    double dist = (vx - hitX) * (vx - hitX) + (vz - hitZ) * (vz - hitZ);
                    if (dist < minDist) {
                        minDist = dist;
                        closestNode = activeNodes.get(i);
                    }
                }
            }

            // Redirect interaction if closestNode is different from the currently clicked block pos!
            if (!closestNode.equals(pos)) {
                BlockState targetState = level.getBlockState(closestNode);
                if (targetState.getBlock() instanceof ScribedChalkBlock targetChalk) {
                    net.minecraft.world.phys.BlockHitResult redirectedHit = new net.minecraft.world.phys.BlockHitResult(
                        hitResult.getLocation(), hitResult.getDirection(), closestNode, hitResult.isInside()
                    );
                    return targetChalk.useWithoutItem(targetState, level, closestNode, player, redirectedHit);
                }
            }
        }

        // If they click the OUTPUT node with an empty hand, trigger the processing ritual!
        if (state.getValue(NODE_TYPE) == NodeType.OUTPUT && player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()) {
            int checkTier = 0;
            for (int t = 4; t >= 1; t--) {
                if (checkPatternStatic(level, pos, state.getValue(CIRCUIT), t) &&
                    validateNodeCountsStatic(level, pos, t)) {
                    checkTier = t;
                    break;
                }
            }
            if (checkTier > 0) {
                if (!level.isClientSide()) {
                    int amplifiers = 0;
                    int capacitors = 0;
                    int resonators = 0;

                    List<ItemEntity> inputItemEntities = new java.util.ArrayList<>();
                    List<ItemStack> inputStacks = new java.util.ArrayList<>();
                    List<ScribedChalkBlockEntity> runeBlockEntities = new java.util.ArrayList<>();
                    List<ItemStack> runeStacks = new java.util.ArrayList<>();
                    java.util.Map<EssenceType, Integer> essences = new java.util.HashMap<>();
                    List<ScribedChalkBlockEntity> allChalkBlockEntities = new java.util.ArrayList<>();

                    BlockEntity centerBE = level.getBlockEntity(pos);
                    if (centerBE instanceof ScribedChalkBlockEntity outputBE) {
                        allChalkBlockEntities.add(outputBE);
                    }

                    for (int t = 1; t <= checkTier; t++) {
                        int[][] offsets = getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = pos.offset(offset[0], 0, offset[1]);
                            BlockState neighborState = level.getBlockState(p);
                            if (neighborState.is(state.getBlock())) {
                                boolean isCircuit = neighborState.getValue(CIRCUIT);
                                NodeType type = neighborState.getValue(NODE_TYPE);
                                BlockEntity be = level.getBlockEntity(p);
                                if (be instanceof ScribedChalkBlockEntity chalkBE) {
                                    allChalkBlockEntities.add(chalkBE);
                                    
                                    if (type == NodeType.INPUT) {
                                        AABB scanArea = new AABB(p).inflate(0.2, 0.5, 0.2);
                                        List<ItemEntity> foundItems = level.getEntitiesOfClass(ItemEntity.class, scanArea);
                                        for (ItemEntity ent : foundItems) {
                                            if (!ent.isRemoved() && !ent.getItem().isEmpty()) {
                                                inputItemEntities.add(ent);
                                                inputStacks.add(ent.getItem());
                                            }
                                        }
                                    } else if (type == NodeType.RUNE) {
                                        ItemStack rune = chalkBE.getStoredRune();
                                        if (!rune.isEmpty()) {
                                            runeBlockEntities.add(chalkBE);
                                            runeStacks.add(rune);
                                        }
                                    }
                                    
                                    if (isCircuit) {
                                        if (type == NodeType.AMPLIFIER) amplifiers++;
                                        else if (type == NodeType.CAPACITOR) capacitors++;
                                        else if (type == NodeType.RESONATOR) resonators++;
                                    }
                                }
                            }
                        }
                    }

                    java.util.Set<ScribedChalkBlockEntity> circuitBEs = getConnectedCircuit(level, pos);
                    for (ScribedChalkBlockEntity circuitBE : circuitBEs) {
                        EssenceType affinity = circuitBE.getActiveAffinity();
                        essences.put(affinity, essences.getOrDefault(affinity, 0) + circuitBE.getEssenceLevel());
                    }

                    MagicCircleRecipeInput recipeInput = new MagicCircleRecipeInput(inputStacks, runeStacks, essences, checkTier);
                    Optional<RecipeHolder<MagicCircleRecipe>> recipeOpt = level.getServer().getRecipeManager()
                            .getRecipeFor(ModRecipes.MAGIC_CIRCLE_TYPE.get(), recipeInput, level);

                    if (recipeOpt.isPresent()) {
                        MagicCircleRecipe recipe = recipeOpt.get().value();
                        
                        // 1. Consume input items from entities
                        for (Ingredient ing : recipe.inputs()) {
                            for (int i = 0; i < inputItemEntities.size(); i++) {
                                ItemEntity ent = inputItemEntities.get(i);
                                if (ing.test(ent.getItem())) {
                                    ItemStack stack = ent.getItem();
                                    stack.shrink(1);
                                    if (stack.isEmpty()) {
                                        ent.discard();
                                    } else {
                                        ent.setItem(stack);
                                    }
                                    inputItemEntities.remove(i);
                                    break;
                                }
                            }
                        }
                        
                        // 2. Consume rune items from the chalk block entities
                        for (Ingredient ing : recipe.runes()) {
                            for (int i = 0; i < runeBlockEntities.size(); i++) {
                                ScribedChalkBlockEntity chalkBE = runeBlockEntities.get(i);
                                ItemStack stack = chalkBE.getStoredRune();
                                if (ing.test(stack)) {
                                    stack.shrink(1);
                                    chalkBE.setStoredRune(stack);
                                    chalkBE.setChanged();
                                    level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                                    runeBlockEntities.remove(i);
                                    break;
                                }
                            }
                        }
                        
                        // 2b. Consume essences from the connected circuit
                        for (java.util.Map.Entry<EssenceType, Integer> entry : recipe.essences().entrySet()) {
                            EssenceType type = entry.getKey();
                            int toConsume = entry.getValue();
                            
                            for (ScribedChalkBlockEntity chalkBE : circuitBEs) {
                                if (toConsume <= 0) break;
                                if (chalkBE.getActiveAffinity() == type || (type == EssenceType.REGULAR)) {
                                    int available = chalkBE.getEssenceLevel();
                                    if (available > 0) {
                                        int consumed = Math.min(toConsume, available);
                                        chalkBE.setEssenceLevel(available - consumed);
                                        if (chalkBE.getEssenceLevel() <= 0 && 
                                            chalkBE.getBlockState().getValue(NODE_TYPE) != NodeType.SOURCE && 
                                            chalkBE.getStoredOrbisCell().isEmpty()) {
                                            chalkBE.setActiveAffinity(EssenceType.REGULAR);
                                            chalkBE.setColor(0xFFCCCCCC);
                                        }
                                        chalkBE.setChanged();
                                        level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                                        toConsume -= consumed;
                                    }
                                }
                            }
                        }

                        // 3. Clear/Drain essences on all nodes in the circle (for simple magic circles only)
                        if (!state.getValue(CIRCUIT)) {
                            for (ScribedChalkBlockEntity chalkBE : allChalkBlockEntities) {
                                NodeType nodeType = chalkBE.getBlockState().getValue(NODE_TYPE);
                                if (nodeType == NodeType.SOURCE || !chalkBE.getStoredOrbisCell().isEmpty()) {
                                    continue;
                                }
                                chalkBE.setActiveAffinity(EssenceType.REGULAR);
                                chalkBE.setEssenceLevel(0);
                                chalkBE.setColor(0xFFCCCCCC);
                                chalkBE.setChanged();
                                level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                            }
                        }
                        
                        // 4. Spawn output item at Output node center
                        ItemStack resultStack = recipe.output().copy();
                        ItemEntity resultEntity = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, resultStack);
                        resultEntity.setDeltaMovement(0, 0.25, 0);
                        level.addFreshEntity(resultEntity);
                        
                        // 5. Visual/Audio effects
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Magic Circle has successfully performed the ritual!"), false);
                        level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.2f);
                        
                        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5, 60, 0.5, 0.1, 0.5, 0.2);
                            for (int t = 1; t <= checkTier; t++) {
                                int[][] offsets = getOffsetsForTier(t);
                                for (int[] offset : offsets) {
                                    BlockPos p = pos.offset(offset[0], 0, offset[1]);
                                    serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, p.getX() + 0.5, p.getY() + 0.1, p.getZ() + 0.5, 20, 0.2, 0.1, 0.2, 0.08);
                                }
                            }
                        }
                    } else {
                        StringBuilder message = new StringBuilder("§5[Entropica] Magic Circle formed, but no recipe matches these inputs!");
                        if (!essences.isEmpty()) {
                            message.append("\n§d - Contained Essences:");
                            for (java.util.Map.Entry<EssenceType, Integer> entry : essences.entrySet()) {
                                message.append(" ").append(entry.getValue()).append(" ").append(entry.getKey().name());
                            }
                        } else {
                            message.append("\n§d - Contained Essences: None");
                        }
                        if (!runeStacks.isEmpty()) {
                            message.append("\n§9 - Detected Runes:");
                            for (ItemStack rune : runeStacks) {
                                message.append(" ").append(rune.getHoverName().getString());
                            }
                        }
                        if (amplifiers > 0) {
                            double powerMult = 1.0 + amplifiers * 0.5;
                            message.append("\n§6 - Power Multiplier: ").append(String.format("%.1f", powerMult)).append("x (").append(amplifiers).append(" Amplifiers)");
                        }
                        if (capacitors > 0) {
                            double efficiencyMult = 1.0 - (1.0 - Math.pow(0.75, capacitors));
                            message.append("\n§e - Essence Efficiency: ").append(String.format("%.0f", (1.0 - efficiencyMult) * 100)).append("% cost reduction (").append(capacitors).append(" Capacitors)");
                        }
                        if (resonators > 0) {
                            double speedMult = 1.0 + resonators * 0.5;
                            message.append("\n§b - Processing Speed: ").append(String.format("%.1f", speedMult)).append("x (").append(resonators).append(" Resonators)");
                        }
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal(message.toString()), false);
                        level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.5f, 0.8f);
                    }
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        if (state.getValue(NODE_TYPE) == NodeType.SOURCE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                // Try to insert Orbis Cell
                if (heldItem.getItem() instanceof ddraig.net.entropica.item.OrbisCellItem && chalkBE.getStoredOrbisCell().isEmpty()) {
                    if (!level.isClientSide()) {
                        EssenceType type = ddraig.net.entropica.item.OrbisCellItem.getStoredVisType(heldItem);
                        if (type != null) {
                            ItemStack toStore = heldItem.copy();
                            toStore.setCount(1);
                            chalkBE.setStoredOrbisCell(toStore);
                            chalkBE.setActiveAffinity(type);
                            chalkBE.setColor(type.getColorInt());

                            heldItem.shrink(1);
                            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 0.8f);
                        }
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to insert Essence Item
                if (heldItem.getItem() instanceof ddraig.net.entropica.item.EssenceItem) {
                    if (!level.isClientSide()) {
                        EssenceType type = ddraig.net.entropica.item.EssenceItem.getEssenceType(heldItem);
                        if (type != null) {
                            java.util.Set<ScribedChalkBlockEntity> circuit = getConnectedCircuit(level, pos);
                            int capacity = getCircuitCapacity(circuit);
                            int currentTotal = 0;
                            for (ScribedChalkBlockEntity beInCircuit : circuit) {
                                currentTotal += beInCircuit.getEssenceLevel();
                            }
                            
                            int maxAdd = capacity - currentTotal;
                            if (maxAdd <= 0) {
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Entropica] Circuit essence capacity reached (" + currentTotal + "/" + capacity + ")."), true);
                                return net.minecraft.world.InteractionResult.FAIL;
                            }

                            // Verify affinity compatibility
                            EssenceType circuitAff = EssenceType.REGULAR;
                            for (ScribedChalkBlockEntity beInCircuit : circuit) {
                                if (beInCircuit.getEssenceLevel() > 0 && beInCircuit.getActiveAffinity() != EssenceType.REGULAR) {
                                    circuitAff = beInCircuit.getActiveAffinity();
                                }
                            }
                            if (circuitAff != EssenceType.REGULAR && circuitAff != type) {
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Entropica] Cannot mix different affinities in the same circuit!"), true);
                                return net.minecraft.world.InteractionResult.FAIL;
                            }

                            int tier = ((ddraig.net.entropica.item.EssenceItem) heldItem.getItem()).getTier();
                            int yield = switch (tier) {
                                case 3 -> 16;
                                case 2 -> 4;
                                default -> 1;
                            };
                            
                            int added = addEssenceToCircuit(circuit, type, yield);
                            if (added > 0) {
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Added " + added + " essence to circuit (" + (currentTotal + added) + "/" + capacity + ")."), true);
                                heldItem.shrink(1);
                                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                            } else {
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Entropica] Failed to add essence to circuit."), true);
                            }
                        }
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to extract Orbis Cell
                if (!chalkBE.getStoredOrbisCell().isEmpty() && heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = chalkBE.getStoredOrbisCell();
                        if (!player.addItem(extracted)) {
                            player.drop(extracted, false);
                        }
                        chalkBE.setStoredOrbisCell(ItemStack.EMPTY);
                        chalkBE.setActiveAffinity(EssenceType.REGULAR);
                        chalkBE.setColor(0xFFCCCCCC);

                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.5f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                } else if (chalkBE.getStoredOrbisCell().isEmpty() && heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        java.util.Set<ScribedChalkBlockEntity> circuit = getConnectedCircuit(level, pos);
                        int capacity = getCircuitCapacity(circuit);
                        int currentTotal = 0;
                        for (ScribedChalkBlockEntity beInCircuit : circuit) {
                            currentTotal += beInCircuit.getEssenceLevel();
                        }
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Circuit Essence: " + currentTotal + "/" + capacity + " (" + chalkBE.getActiveAffinity().name() + ")"), false);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        if (state.getValue(NODE_TYPE) == NodeType.ESSENCE_BANK) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                boolean isBaseAmpoule = heldItem.is(ddraig.net.entropica.registry.ModItems.SMALL_AMPOULE_BASE.get()) ||
                                         heldItem.is(ddraig.net.entropica.registry.ModItems.MEDIUM_AMPOULE_BASE.get()) ||
                                         heldItem.is(ddraig.net.entropica.registry.ModItems.LARGE_AMPOULE_BASE.get());

                if (isBaseAmpoule) {
                    if (!level.isClientSide()) {
                        java.util.Set<ScribedChalkBlockEntity> circuit = getConnectedCircuit(level, pos);
                        EssenceType affinity = EssenceType.REGULAR;
                        int totalEssence = 0;
                        for (ScribedChalkBlockEntity beInCircuit : circuit) {
                            totalEssence += beInCircuit.getEssenceLevel();
                            if (beInCircuit.getEssenceLevel() > 0 && beInCircuit.getActiveAffinity() != EssenceType.REGULAR) {
                                affinity = beInCircuit.getActiveAffinity();
                            }
                        }

                        if (affinity == EssenceType.REGULAR || totalEssence <= 0) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Entropica] Circuit has no specialized essence to extract."), true);
                            return net.minecraft.world.InteractionResult.FAIL;
                        }

                        int required = 8;
                        net.minecraft.world.item.Item filledItem = ddraig.net.entropica.registry.ModItems.SMALL_ESSENCE_AMPOULE.get();
                        if (heldItem.is(ddraig.net.entropica.registry.ModItems.MEDIUM_AMPOULE_BASE.get())) {
                            required = 32;
                            filledItem = ddraig.net.entropica.registry.ModItems.MEDIUM_ESSENCE_AMPOULE.get();
                        } else if (heldItem.is(ddraig.net.entropica.registry.ModItems.LARGE_AMPOULE_BASE.get())) {
                            required = 128;
                            filledItem = ddraig.net.entropica.registry.ModItems.LARGE_ESSENCE_AMPOULE.get();
                        }

                        if (totalEssence < required) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§c[Entropica] Not enough essence to fill ampoule (Requires " + required + ", circuit has " + totalEssence + ")."), true);
                            return net.minecraft.world.InteractionResult.FAIL;
                        }

                        consumeEssenceFromCircuit(circuit, affinity, required);
                        ItemStack filled = new ItemStack(filledItem);
                        ddraig.net.entropica.item.EssenceAmpouleItem.setEssenceType(filled, affinity);

                        heldItem.shrink(1);
                        if (!player.addItem(filled)) {
                            player.drop(filled, false);
                        }
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.5f, 1.0f);
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Extracted " + required + " " + affinity.getDisplayName() + " essence into ampoule."), true);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                } else if (heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        java.util.Set<ScribedChalkBlockEntity> circuit = getConnectedCircuit(level, pos);
                        int capacity = getCircuitCapacity(circuit);
                        int currentTotal = 0;
                        for (ScribedChalkBlockEntity beInCircuit : circuit) {
                            currentTotal += beInCircuit.getEssenceLevel();
                        }
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Circuit Essence: " + currentTotal + "/" + capacity + " (" + chalkBE.getActiveAffinity().name() + ") [Bank stored: " + chalkBE.getEssenceLevel() + "/128]"), false);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        // Add Rune node insertion/extraction
        if (state.getValue(NODE_TYPE) == NodeType.RUNE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                // Try to insert Rune
                if (heldItem.getItem() instanceof ddraig.net.entropica.item.RuneItem && chalkBE.getStoredRune().isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack toStore = heldItem.copy();
                        toStore.setCount(1);
                        chalkBE.setStoredRune(toStore);
                        heldItem.shrink(1);
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to extract Rune
                if (!chalkBE.getStoredRune().isEmpty() && heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = chalkBE.getStoredRune();
                        if (!player.addItem(extracted)) {
                            player.drop(extracted, false);
                        }
                        chalkBE.setStoredRune(ItemStack.EMPTY);
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        if (player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND).isEmpty()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                if (!level.isClientSide()) {
                    String name = state.getValue(NODE_TYPE).getSerializedName().toUpperCase();
                    String affName = chalkBE.getActiveAffinity().name();
                    int essLevel = chalkBE.getEssenceLevel();
                    String msg = String.format("§5[Entropica] %s Node | Affinity: %s | Essence Level: %d/32", name, affName, essLevel);
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(msg), true);
                }
                return net.minecraft.world.InteractionResult.SUCCESS;
            }
        }

        return net.minecraft.world.InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide()) {
            checkForAndDestroyCircle(level, pos, state);
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                if (chalkBE.getEssenceLevel() > 0 && chalkBE.getBlockState().getValue(NODE_TYPE) == NodeType.ESSENCE_BANK) {
                    double range = 3.0 + (chalkBE.getEssenceLevel() / 100.0);
                    net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(pos).inflate(range);
                    for (net.minecraft.world.entity.LivingEntity entity : level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class, area)) {
                        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(ddraig.net.entropica.registry.ModEffects.PARALYZED, 2400, 0));
                    }
                }
                if (!chalkBE.getStoredOrbisCell().isEmpty()) {
                    Block.popResource(level, pos, chalkBE.getStoredOrbisCell());
                }
                if (!chalkBE.getStoredRune().isEmpty()) {
                    Block.popResource(level, pos, chalkBE.getStoredRune());
                }
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    private void checkForAndDestroyCircle(Level level, BlockPos brokenPos, BlockState brokenState) {
        boolean circuit = brokenState.getValue(CIRCUIT);
        
        // 1. If the broken block itself is an OUTPUT node, check if it was a center of a circle
        if (brokenState.getValue(NODE_TYPE) == NodeType.OUTPUT) {
            for (int tier = 4; tier >= 1; tier--) {
                if (checkPatternStatic(level, brokenPos, circuit, tier) && validateNodeCountsStatic(level, brokenPos, tier)) {
                    // Destroy all blocks in all concentric layers up to this tier
                    for (int t = 1; t <= tier; t++) {
                        int[][] offsets = getOffsetsForTier(t);
                        for (int[] offset : offsets) {
                            BlockPos p = brokenPos.offset(offset[0], 0, offset[1]);
                            if (level.getBlockState(p).is(brokenState.getBlock())) {
                                level.destroyBlock(p, true);
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        // 2. Check if the broken block was a perimeter block for a nearby OUTPUT node
        for (int tier = 1; tier <= 4; tier++) {
            int[][] offsets = getOffsetsForTier(tier);
            for (int[] offset : offsets) {
                BlockPos centerPos = brokenPos.offset(-offset[0], 0, -offset[1]);
                BlockState centerState = level.getBlockState(centerPos);
                if (centerState.is(brokenState.getBlock()) && centerState.getValue(NODE_TYPE) == NodeType.OUTPUT && centerState.getValue(CIRCUIT) == circuit) {
                    if (checkPatternStaticExcept(level, centerPos, circuit, tier, brokenPos)) {
                        // Destroy center and all other remaining perimeter blocks across all layers
                        level.destroyBlock(centerPos, true);
                        for (int t = 1; t <= tier; t++) {
                            int[][] o = getOffsetsForTier(t);
                            for (int[] offset2 : o) {
                                BlockPos p = centerPos.offset(offset2[0], 0, offset2[1]);
                                if (level.getBlockState(p).is(brokenState.getBlock())) {
                                    level.destroyBlock(p, true);
                                }
                            }
                        }
                        return; // Found and destroyed
                    }
                }
            }
        }
    }

    public static boolean validateNodeCountsStatic(Level level, BlockPos center, int tier) {
        int inputCount = 0;
        int sourceCount = 0;
        int runeCount = 0;
        int specialCount = 0;
        
        for (int t = 1; t <= tier; t++) {
            int[][] offsets = getOffsetsForTier(t);
            for (int[] offset : offsets) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                BlockState state = level.getBlockState(pos);
                if (state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get())) {
                    boolean isCircuit = state.getValue(CIRCUIT);
                    NodeType type = state.getValue(NODE_TYPE);
                    if (type == NodeType.INPUT) inputCount++;
                    else if (type == NodeType.SOURCE) sourceCount++;
                    else if (type == NodeType.RUNE) runeCount++;
                    else if (isCircuit && (type == NodeType.AMPLIFIER || type == NodeType.CAPACITOR || type == NodeType.RESONATOR)) {
                        specialCount++;
                    }
                }
            }
        }
        
        int totalNodes = inputCount + sourceCount + runeCount + specialCount;
        int expectedTotal = 8 * tier;
        
        return inputCount >= 1 && sourceCount >= 1 && runeCount >= 1 && 
               inputCount <= tier && sourceCount <= tier && runeCount <= 6 * tier && 
               totalNodes <= expectedTotal;
    }

    public static boolean checkPatternStatic(Level level, BlockPos center, boolean circuit, int tier) {
        for (int t = 1; t <= tier; t++) {
            int[][] offsets = getOffsetsForTier(t);
            for (int[] offset : offsets) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                BlockState state = level.getBlockState(pos);
                if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) || state.getValue(CIRCUIT) != circuit) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkPatternStaticExcept(Level level, BlockPos center, boolean circuit, int tier, BlockPos exceptPos) {
        for (int t = 1; t <= tier; t++) {
            int[][] offsets = getOffsetsForTier(t);
            for (int[] offset : offsets) {
                BlockPos pos = center.offset(offset[0], 0, offset[1]);
                if (pos.equals(exceptPos)) continue;
                BlockState state = level.getBlockState(pos);
                if (!state.is(ddraig.net.entropica.registry.ModBlocks.SCRIBED_CHALK.get()) || state.getValue(CIRCUIT) != circuit) {
                    return false;
                }
            }
        }
        return true;
    }

    public static int[][] getOffsetsForTier(int tier) {
        return switch (tier) {
            case 1 -> new int[][]{
                {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}
            };
            case 2 -> new int[][]{
                {0, -2}, {1, -2}, {2, -2}, {2, -1}, {2, 0}, {2, 1}, {2, 2}, {1, 2},
                {0, 2}, {-1, 2}, {-2, 2}, {-2, 1}, {-2, 0}, {-2, -1}, {-2, -2}, {-1, -2}
            };
            case 3 -> new int[][]{
                {0, -3}, {1, -3}, {2, -3}, {3, -3}, {3, -2}, {3, -1}, {3, 0}, {3, 1}, {3, 2}, {3, 3},
                {2, 3}, {1, 3}, {0, 3}, {-1, 3}, {-2, 3}, {-3, 3}, {-3, 2}, {-3, 1}, {-3, 0}, {-3, -1},
                {-3, -2}, {-3, -3}, {-2, -3}, {-1, -3}
            };
            default -> new int[][]{
                {0, -4}, {1, -4}, {2, -4}, {3, -4}, {4, -4}, {4, -3}, {4, -2}, {4, -1}, {4, 0}, {4, 1},
                {4, 2}, {4, 3}, {4, 4}, {3, 4}, {2, 4}, {1, 4}, {0, 4}, {-1, 4}, {-2, 4}, {-3, 4},
                {-4, 4}, {-4, 3}, {-4, 2}, {-4, 1}, {-4, 0}, {-4, -1}, {-4, -2}, {-4, -3}, {-4, -4},
                {-3, -4}, {-2, -4}, {-1, -4}
            };
        };
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ScribedChalkBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, ModBlockEntities.SCRIBED_CHALK_BE.get(), ScribedChalkBlockEntity::tick);
    }

    public static int getCircuitCapacity(java.util.Set<ScribedChalkBlockEntity> circuit) {
        int capacity = 0;
        for (ScribedChalkBlockEntity be : circuit) {
            NodeType type = be.getBlockState().getValue(NODE_TYPE);
            if (type == NodeType.ESSENCE_BANK) {
                capacity += 128;
            } else {
                capacity += 8;
            }
        }
        return capacity;
    }

    public static int addEssenceToCircuit(java.util.Set<ScribedChalkBlockEntity> circuit, EssenceType type, int amount) {
        if (amount <= 0) return 0;
        
        EssenceType circuitAffinity = EssenceType.REGULAR;
        int totalEssence = 0;
        for (ScribedChalkBlockEntity be : circuit) {
            totalEssence += be.getEssenceLevel();
            if (be.getEssenceLevel() > 0 && be.getActiveAffinity() != EssenceType.REGULAR) {
                circuitAffinity = be.getActiveAffinity();
            }
        }
        
        if (totalEssence > 0 && circuitAffinity != EssenceType.REGULAR && circuitAffinity != type) {
            return 0;
        }
        
        int capacity = getCircuitCapacity(circuit);
        int toAdd = Math.min(amount, capacity - totalEssence);
        if (toAdd <= 0) return 0;
        
        int remaining = toAdd;
        
        // Phase 1: Prioritize SOURCE and ESSENCE_BANK nodes
        for (ScribedChalkBlockEntity be : circuit) {
            NodeType nt = be.getBlockState().getValue(NODE_TYPE);
            if (nt == NodeType.SOURCE || nt == NodeType.ESSENCE_BANK) {
                int maxNodeCap = (nt == NodeType.ESSENCE_BANK) ? 128 : 8;
                int current = be.getEssenceLevel();
                int space = maxNodeCap - current;
                if (space > 0) {
                    int add = Math.min(remaining, space);
                    be.setEssenceLevel(current + add);
                    be.setActiveAffinity(type);
                    be.setColor(type.getColorInt());
                    be.setChanged();
                    remaining -= add;
                    if (remaining <= 0) break;
                }
            }
        }
        
        // Phase 2: Fill other nodes/paths in the circuit if needed
        if (remaining > 0) {
            for (ScribedChalkBlockEntity be : circuit) {
                NodeType nt = be.getBlockState().getValue(NODE_TYPE);
                if (nt != NodeType.SOURCE && nt != NodeType.ESSENCE_BANK) {
                    int current = be.getEssenceLevel();
                    int space = 8 - current;
                    if (space > 0) {
                        int add = Math.min(remaining, space);
                        be.setEssenceLevel(current + add);
                        be.setActiveAffinity(type);
                        be.setColor(type.getColorInt());
                        be.setChanged();
                        remaining -= add;
                        if (remaining <= 0) break;
                    }
                }
            }
        }
        
        for (ScribedChalkBlockEntity be : circuit) {
            be.setActiveAffinity(type);
            be.setColor(type.getColorInt());
            be.setChanged();
            if (be.getLevel() != null) {
                be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            }
        }
        
        return toAdd;
    }

    public static int consumeEssenceFromCircuit(java.util.Set<ScribedChalkBlockEntity> circuit, EssenceType type, int amount) {
        if (amount <= 0) return 0;
        
        int totalEssence = 0;
        for (ScribedChalkBlockEntity be : circuit) {
            if (be.getActiveAffinity() == type || type == EssenceType.REGULAR) {
                totalEssence += be.getEssenceLevel();
            }
        }
        
        int toConsume = Math.min(amount, totalEssence);
        if (toConsume <= 0) return 0;
        
        int remaining = toConsume;
        
        // Phase 1: Consume from path/standard nodes (non-SOURCE, non-ESSENCE_BANK)
        for (ScribedChalkBlockEntity be : circuit) {
            NodeType nt = be.getBlockState().getValue(NODE_TYPE);
            if (nt != NodeType.SOURCE && nt != NodeType.ESSENCE_BANK) {
                int current = be.getEssenceLevel();
                if (current > 0) {
                    int consume = Math.min(remaining, current);
                    be.setEssenceLevel(current - consume);
                    be.setChanged();
                    remaining -= consume;
                    if (remaining <= 0) break;
                }
            }
        }
        
        // Phase 2: Consume from SOURCE and ESSENCE_BANK nodes
        if (remaining > 0) {
            for (ScribedChalkBlockEntity be : circuit) {
                NodeType nt = be.getBlockState().getValue(NODE_TYPE);
                if (nt == NodeType.SOURCE || nt == NodeType.ESSENCE_BANK) {
                    int current = be.getEssenceLevel();
                    if (current > 0) {
                        int consume = Math.min(remaining, current);
                        be.setEssenceLevel(current - consume);
                        be.setChanged();
                        remaining -= consume;
                        if (remaining <= 0) break;
                    }
                }
            }
        }
        
        int newTotal = 0;
        for (ScribedChalkBlockEntity be : circuit) {
            newTotal += be.getEssenceLevel();
        }
        
        if (newTotal <= 0) {
            for (ScribedChalkBlockEntity be : circuit) {
                be.setEssenceLevel(0);
                be.setActiveAffinity(EssenceType.REGULAR);
                be.setColor(0xFFCCCCCC);
                be.setChanged();
            }
        }
        
        for (ScribedChalkBlockEntity be : circuit) {
            if (be.getLevel() != null) {
                be.getLevel().sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), 3);
            }
        }
        
        return toConsume;
    }

    public static java.util.Set<ScribedChalkBlockEntity> getConnectedCircuit(Level level, BlockPos startPos) {
        java.util.Set<BlockPos> visited = new java.util.HashSet<>();
        java.util.Set<ScribedChalkBlockEntity> circuitBEs = new java.util.HashSet<>();
        java.util.Queue<BlockPos> queue = new java.util.LinkedList<>();
        
        queue.add(startPos);
        visited.add(startPos);
        
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockEntity be = level.getBlockEntity(current);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                circuitBEs.add(chalkBE);
                
                boolean currentCircuit = chalkBE.getBlockState().getValue(CIRCUIT);
                for (ScribedChalkBlockEntity.Direction8 dir8 : ScribedChalkBlockEntity.Direction8.values()) {
                    BlockPos neighborPos = current.offset(dir8.getXOffset(), 0, dir8.getZOffset());
                    if (visited.contains(neighborPos)) continue;
                    
                    BlockState neighborState = level.getBlockState(neighborPos);
                    if (neighborState.getBlock() instanceof ScribedChalkBlock) {
                        if (chalkBE.connectsToNeighbor8(level, current, dir8, currentCircuit)) {
                            queue.add(neighborPos);
                            visited.add(neighborPos);
                        }
                    }
                }
            }
        }
        return circuitBEs;
    }

    public enum NodeType implements StringRepresentable {
        DEFAULT("default"),
        INPUT("input"),
        SOURCE("source"),
        OUTPUT("output"),
        RUNE("rune"),
        COLLECTION("collection"),
        AMPLIFIER("amplifier"),
        CAPACITOR("capacitor"),
        RESONATOR("resonator"),
        DIODE("diode"),
        AND_GATE("and_gate"),
        OR_GATE("or_gate"),
        NOT_GATE("not_gate"),
        EXTRACTION("extraction"),
        DELAY("delay"),
        ESSENCE_BANK("essence_bank");

        private final String name;

        NodeType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
