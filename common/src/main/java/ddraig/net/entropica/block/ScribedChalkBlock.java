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

        // If they click the OUTPUT node, trigger processing or ward logic
        if (state.getValue(NODE_TYPE) == NodeType.OUTPUT) {
            ItemStack heldItem = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
            BlockEntity centerBE = level.getBlockEntity(pos);
            if (centerBE instanceof ScribedChalkBlockEntity outputBE) {
                if (outputBE.isWard() && outputBE.getWardTicks() > 0) {
                    if (heldItem.getItem() instanceof ddraig.net.entropica.item.EssenceItem essenceItem) {
                        if (!level.isClientSide()) {
                            int tier = essenceItem.getTier();
                            int addTicks = switch (tier) {
                                case 0 -> 1200; // Materia Fragment
                                case 1 -> 2400; // Weak Essence
                                case 2 -> 6000; // Average Essence
                                default -> 24000; // Strong / Grand Essence
                            };
                            outputBE.setWardTicks(outputBE.getWardTicks() + addTicks);
                            if (!player.getAbilities().instabuild) {
                                heldItem.shrink(1);
                            }
                            level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0f, 1.2f);
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Fed repulsion ward. Remaining duration: " + (outputBE.getWardTicks() / 20) + " seconds."), true);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    } else if (heldItem.isEmpty()) {
                        if (!level.isClientSide()) {
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Active Repulsion Ward. Remaining duration: " + (outputBE.getWardTicks() / 20) + " seconds."), false);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                }
            }

            if (!heldItem.isEmpty()) {
                return net.minecraft.world.InteractionResult.PASS;
            }

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
                    List<ScribedChalkBlockEntity> inputBlockEntities = new java.util.ArrayList<>();
                    List<ItemStack> inputStacks = new java.util.ArrayList<>();
                    List<ScribedChalkBlockEntity> runeBlockEntities = new java.util.ArrayList<>();
                    List<ItemStack> runeStacks = new java.util.ArrayList<>();
                    java.util.Map<EssenceType, Integer> essences = new java.util.HashMap<>();
                    List<ScribedChalkBlockEntity> allChalkBlockEntities = new java.util.ArrayList<>();

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
                                        ItemStack stored = chalkBE.getStoredItem();
                                        if (!stored.isEmpty()) {
                                            inputBlockEntities.add(chalkBE);
                                            inputStacks.add(stored);
                                        } else {
                                            AABB scanArea = new AABB(p).inflate(0.2, 0.5, 0.2);
                                            List<ItemEntity> foundItems = level.getEntitiesOfClass(ItemEntity.class, scanArea);
                                            for (ItemEntity ent : foundItems) {
                                                if (!ent.isRemoved() && !ent.getItem().isEmpty()) {
                                                    inputItemEntities.add(ent);
                                                    inputStacks.add(ent.getItem());
                                                }
                                            }
                                        }
                                    } else if (type == NodeType.RUNE) {
                                        ItemStack rune = chalkBE.getStoredRune();
                                        if (!rune.isEmpty()) {
                                            runeBlockEntities.add(chalkBE);
                                            runeStacks.add(rune);
                                        }
                                    }
                                    
                                    EssenceType affinity = chalkBE.getActiveAffinity();
                                    if (affinity != EssenceType.REGULAR) {
                                        essences.put(affinity, essences.getOrDefault(affinity, 0) + chalkBE.getEssenceLevel());
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

                    MagicCircleRecipeInput recipeInput = new MagicCircleRecipeInput(inputStacks, runeStacks, essences, checkTier);
                    List<RecipeHolder<MagicCircleRecipe>> allRecipes = new java.util.ArrayList<>();
                    if (level.getServer() != null && level.getServer().getRecipeManager() != null) {
                        for (RecipeHolder<?> holder : level.getServer().getRecipeManager().getRecipes()) {
                            if (holder.value() instanceof MagicCircleRecipe) {
                                allRecipes.add((RecipeHolder<MagicCircleRecipe>) holder);
                            }
                        }
                    }
                    for (MagicCircleRecipe r : ddraig.net.entropica.recipe.HardcodedRecipes.getMagicCircleRecipes()) {
                        allRecipes.add(new RecipeHolder<>(
                            net.minecraft.resources.ResourceKey.create(
                                net.minecraft.core.registries.Registries.RECIPE,
                                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "hardcoded_magic_circle_" + r.hashCode())
                            ),
                            r
                        ));
                    }
                    
                    List<RecipeHolder<MagicCircleRecipe>> matchingRecipes = new java.util.ArrayList<>();
                    for (RecipeHolder<MagicCircleRecipe> holder : allRecipes) {
                        if (holder.value().matches(recipeInput, level)) {
                            matchingRecipes.add(holder);
                        }
                    }

                    if (!matchingRecipes.isEmpty()) {
                        // Sort matching recipes by cost descending: highest sum of inputs + runes + essences
                        matchingRecipes.sort((r1, r2) -> {
                            MagicCircleRecipe val1 = r1.value();
                            MagicCircleRecipe val2 = r2.value();
                            
                            int essenceCost1 = val1.essences().values().stream().mapToInt(Integer::intValue).sum();
                            int essenceCost2 = val2.essences().values().stream().mapToInt(Integer::intValue).sum();
                            
                            int cost1 = val1.inputs().size() + val1.runes().size() + essenceCost1;
                            int cost2 = val2.inputs().size() + val2.runes().size() + essenceCost2;
                            
                            if (cost1 != cost2) {
                                return Integer.compare(cost2, cost1); // descending
                            }
                            return Integer.compare(val2.tier(), val1.tier()); // descending
                        });

                        MagicCircleRecipe recipe = matchingRecipes.get(0).value();
                        
                        // 1. Consume input items from entities
                        for (Ingredient ing : recipe.inputs()) {
                            boolean consumed = false;
                            for (int i = 0; i < inputBlockEntities.size(); i++) {
                                ScribedChalkBlockEntity chalkBE = inputBlockEntities.get(i);
                                ItemStack stack = chalkBE.getStoredItem();
                                if (ing.test(stack)) {
                                    stack.shrink(1);
                                    chalkBE.setStoredItem(stack);
                                    chalkBE.setChanged();
                                    level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                                    inputBlockEntities.remove(i);
                                    consumed = true;
                                    break;
                                }
                            }
                            if (!consumed) {
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
                        
                        // 3. Clear/Drain essences on all nodes in the circle
                        for (ScribedChalkBlockEntity chalkBE : allChalkBlockEntities) {
                            NodeType nodeType = chalkBE.getBlockState().getValue(NODE_TYPE);
                            if (nodeType == NodeType.CAPACITOR) {
                                int retained = (int) (chalkBE.getEssenceLevel() * 0.2); // Suggestion C: Retains 20% essence
                                if (retained > 0) {
                                    chalkBE.setEssenceLevel(retained);
                                } else {
                                    chalkBE.setActiveAffinity(EssenceType.REGULAR);
                                    chalkBE.setEssenceLevel(0);
                                    chalkBE.setColor(0xFFCCCCCC);
                                }
                            } else {
                                chalkBE.setActiveAffinity(EssenceType.REGULAR);
                                chalkBE.setEssenceLevel(0);
                                chalkBE.setColor(0xFFCCCCCC);
                            }
                            chalkBE.setChanged();
                            level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                        }
                        
                        // 4. Start active ritual processing on center Output node (100 ticks = 5 seconds)
                        if (centerBE instanceof ScribedChalkBlockEntity outputBE) {
                            outputBE.setAmplifierCount(amplifiers);
                            outputBE.setCapacitorCount(capacitors);
                            outputBE.startRitual(recipe.output(), 100);
                        }
                        
                        // 5. Visual/Audio effects
                        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] Magic Circle has successfully initiated the ritual!"), false);
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
                        boolean hasAlgiz = false;
                        for (ItemStack rune : runeStacks) {
                            if (rune.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_ALGIZ.get()) {
                                hasAlgiz = true;
                                break;
                            }
                        }
                        int totalEssence = essences.values().stream().mapToInt(Integer::intValue).sum();
                        
                        if (hasAlgiz && totalEssence >= 32) {
                            // Consume RUNE_ALGIZ
                            for (int i = 0; i < runeBlockEntities.size(); i++) {
                                ScribedChalkBlockEntity chalkBE = runeBlockEntities.get(i);
                                ItemStack stack = chalkBE.getStoredRune();
                                if (stack.getItem() == ddraig.net.entropica.registry.ModItems.RUNE_ALGIZ.get()) {
                                    stack.shrink(1);
                                    chalkBE.setStoredRune(stack);
                                    chalkBE.setChanged();
                                    level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                                    break;
                                }
                            }
                            // Consume essences
                            for (ScribedChalkBlockEntity chalkBE : allChalkBlockEntities) {
                                chalkBE.setActiveAffinity(EssenceType.REGULAR);
                                chalkBE.setEssenceLevel(0);
                                chalkBE.setColor(0xFFCCCCCC);
                                chalkBE.setChanged();
                                level.sendBlockUpdated(chalkBE.getBlockPos(), chalkBE.getBlockState(), chalkBE.getBlockState(), 3);
                            }
                            // Set ward status
                            if (centerBE instanceof ScribedChalkBlockEntity outputBE) {
                                outputBE.setWard(true);
                                outputBE.setWardTicks(12000);
                            }
                            
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§5[Entropica] The essence repulsion ward has been activated!"), false);
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
            }
            return net.minecraft.world.InteractionResult.SUCCESS;
        }
        }

        if (state.getValue(NODE_TYPE) == NodeType.SOURCE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                ItemStack heldItemCheck = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (heldItemCheck.getItem() instanceof ddraig.net.entropica.item.ChalkItem) {
                    return net.minecraft.world.InteractionResult.PASS;
                }
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

                // Try to insert Essence Item or Ampoule
                boolean isEssence = heldItem.getItem() instanceof ddraig.net.entropica.item.EssenceItem;
                boolean isAmpoule = heldItem.getItem() instanceof ddraig.net.entropica.item.EssenceAmpouleItem;
                if (isEssence || isAmpoule) {
                    if (!level.isClientSide()) {
                        EssenceType type = isEssence 
                            ? ddraig.net.entropica.item.EssenceItem.getEssenceType(heldItem)
                            : ddraig.net.entropica.item.EssenceAmpouleItem.getEssenceType(heldItem);
                        if (type != null) {
                            chalkBE.setActiveAffinity(type);
                            chalkBE.setColor(type.getColorInt());

                            int tier = isEssence 
                                ? ((ddraig.net.entropica.item.EssenceItem) heldItem.getItem()).getTier()
                                : ((ddraig.net.entropica.item.EssenceAmpouleItem) heldItem.getItem()).getTier();
                            
                            int yield = switch (tier) {
                                case 3 -> 16;
                                case 2 -> 4;
                                default -> 1;
                            };
                            chalkBE.setEssenceLevel(chalkBE.getEssenceLevel() + yield);

                            heldItem.shrink(1);
                            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
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
                }
            }
        }

        if (state.getValue(NODE_TYPE) == NodeType.INPUT) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                ItemStack heldItemCheck = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (heldItemCheck.getItem() instanceof ddraig.net.entropica.item.ChalkItem) {
                    return net.minecraft.world.InteractionResult.PASS;
                }
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                // Try to insert general item
                if (chalkBE.getStoredItem().isEmpty() && !heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack toStore = heldItem.copy();
                        toStore.setCount(1);
                        chalkBE.setStoredItem(toStore);

                        heldItem.shrink(1);
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }

                // Try to extract general item
                if (!chalkBE.getStoredItem().isEmpty() && heldItem.isEmpty()) {
                    if (!level.isClientSide()) {
                        ItemStack extracted = chalkBE.getStoredItem();
                        if (!player.addItem(extracted)) {
                            player.drop(extracted, false);
                        }
                        chalkBE.setStoredItem(ItemStack.EMPTY);

                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.2f);
                    }
                    return net.minecraft.world.InteractionResult.SUCCESS;
                }
            }
        }

        // Add Rune node insertion/extraction
        if (state.getValue(NODE_TYPE) == NodeType.RUNE) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ScribedChalkBlockEntity chalkBE) {
                ItemStack heldItemCheck = player.getItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND);
                if (heldItemCheck.getItem() instanceof ddraig.net.entropica.item.ChalkItem) {
                    return net.minecraft.world.InteractionResult.PASS;
                }
                net.minecraft.world.InteractionHand hand = net.minecraft.world.InteractionHand.MAIN_HAND;
                ItemStack heldItem = player.getItemInHand(hand);

                // Fehu Filter Node Specialization
                if (!chalkBE.getStoredRune().isEmpty() && chalkBE.getStoredRune().getItem() == ddraig.net.entropica.registry.ModItems.RUNE_FEHU.get()) {
                    ddraig.net.entropica.api.EssenceType type = ddraig.net.entropica.item.EssenceItem.getEssenceType(heldItem);
                    if (type != null) {
                        if (!level.isClientSide()) {
                            chalkBE.setFilterType(type);
                            player.displayClientMessage(net.minecraft.network.chat.Component.literal("§aFilter specialized to: " + type.getDisplayName()), true);
                            level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.7f, 1.2f);
                        }
                        return net.minecraft.world.InteractionResult.SUCCESS;
                    }
                    if (heldItem.isEmpty()) {
                        if (chalkBE.getFilterType() != null) {
                            if (!level.isClientSide()) {
                                chalkBE.setFilterType(null);
                                player.displayClientMessage(net.minecraft.network.chat.Component.literal("§eFilter reset to default (All colored essence)"), true);
                                level.playSound(null, pos, SoundEvents.DYE_USE, SoundSource.BLOCKS, 0.7f, 0.8f);
                            }
                            return net.minecraft.world.InteractionResult.SUCCESS;
                        }
                    }
                }

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
                        chalkBE.setFilterType(null);
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

    private void dropStoredContents(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof ScribedChalkBlockEntity chalkBE) {
            if (!chalkBE.getStoredOrbisCell().isEmpty()) {
                Block.popResource(level, pos, chalkBE.getStoredOrbisCell());
                chalkBE.setStoredOrbisCell(ItemStack.EMPTY);
            }
            if (!chalkBE.getStoredRune().isEmpty()) {
                Block.popResource(level, pos, chalkBE.getStoredRune());
                chalkBE.setStoredRune(ItemStack.EMPTY);
            }
            if (!chalkBE.getStoredItem().isEmpty()) {
                Block.popResource(level, pos, chalkBE.getStoredItem());
                chalkBE.setStoredItem(ItemStack.EMPTY);
            }
            if (!chalkBE.getActiveRecipeOutput().isEmpty()) {
                Block.popResource(level, pos, chalkBE.getActiveRecipeOutput());
                chalkBE.setActiveRecipeOutput(ItemStack.EMPTY);
            }
        }
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.player.Player player) {
        if (!level.isClientSide()) {
            dropStoredContents(level, pos);
            checkForAndDestroyCircle(level, pos, state);
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
                                dropStoredContents(level, p);
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
                        dropStoredContents(level, centerPos);
                        level.destroyBlock(centerPos, true);
                        for (int t = 1; t <= tier; t++) {
                            int[][] o = getOffsetsForTier(t);
                            for (int[] offset2 : o) {
                                BlockPos p = centerPos.offset(offset2[0], 0, offset2[1]);
                                if (level.getBlockState(p).is(brokenState.getBlock())) {
                                    dropStoredContents(level, p);
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
        int expectedTotal = 2 * tier + 1;
        
        return inputCount >= 1 && sourceCount >= 1 && runeCount >= 1 && totalNodes <= expectedTotal;
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
            case 1 -> new int[][]{{0, -1}, {1, 0}, {0, 1}, {-1, 0}};
            case 2 -> new int[][]{{0, -2}, {1, -1}, {2, 0}, {1, 1}, {0, 2}, {-1, 1}, {-2, 0}, {-1, -1}};
            case 3 -> new int[][]{
                {0, -3}, {1, -3}, {2, -2}, {3, -1}, {3, 1}, {2, 2}, {1, 3}, {0, 3}, {-1, 3}, {-2, 2}, {-3, 1}, {-3, -1}, {-2, -2}, {-1, -3}
            };
            default -> new int[][]{
                {0, -4}, {1, -4}, {2, -3}, {3, -2}, {4, -1}, {4, 0}, {4, 1}, {3, 2}, {2, 3}, {1, 4}, {0, 4}, {-1, 4}, {-2, 3}, {-3, 2}, {-4, 1}, {-4, 0}, {-4, -1}, {-3, -2}, {-2, -3}, {-1, -4}
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
        EXTRACTION("extraction");

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
