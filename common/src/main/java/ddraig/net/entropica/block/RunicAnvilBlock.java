package ddraig.net.entropica.block;

import com.mojang.serialization.MapCodec;
import ddraig.net.entropica.block.entity.RunicAnvilBlockEntity;
import ddraig.net.entropica.item.CastingApparatusItem;
import ddraig.net.entropica.item.DynamicVisWeaponItem;
import ddraig.net.entropica.item.SpellgemItem;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModDataComponents;
import ddraig.net.entropica.registry.ModItems;
import ddraig.net.entropica.spell.apparatus.ApparatusType;
import ddraig.net.entropica.spell.apparatus.CastingApparatusState;
import ddraig.net.entropica.spell.apparatus.SocketedSpellgem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Runic Anvil — 2-block-wide arcane forging & alignment workstation (Spellcrafting Guide Sections 8.5.1, 8.6).
 *
 * <p>Accepts two parts onto the anvil ({@link RunicAnvilBlockEntity#SLOT_BASE} and {@link RunicAnvilBlockEntity#SLOT_CATALYST}),
 * conjures a spinning 3D Magic Circle between them, smashes down with an Ethereal Hammer, and leaves the finished
 * aligned/engraved/tempered item floating above the anvil and stored inside {@link RunicAnvilBlockEntity#SLOT_OUTPUT}.</p>
 */
public class RunicAnvilBlock extends BaseEntityBlock {

    public static final MapCodec<RunicAnvilBlock> CODEC = simpleCodec(RunicAnvilBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleWorkstationPart> PART = EnumProperty.create("part", DoubleWorkstationPart.class);

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 15.0D, 15.0D);

    public RunicAnvilBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(PART, DoubleWorkstationPart.LEFT));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    public static Direction getPartnerDirection(Direction facing, DoubleWorkstationPart part) {
        return part == DoubleWorkstationPart.LEFT ? facing.getCounterClockWise() : facing.getClockWise();
    }

    public static BlockPos getPrimaryPos(BlockPos pos, BlockState state) {
        if (state.hasProperty(PART) && state.getValue(PART) == DoubleWorkstationPart.RIGHT) {
            return pos.relative(getPartnerDirection(state.getValue(FACING), DoubleWorkstationPart.RIGHT));
        }
        return pos;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection().getOpposite();
        BlockPos pos = context.getClickedPos();
        BlockPos partnerPos = pos.relative(getPartnerDirection(facing, DoubleWorkstationPart.LEFT));
        Level level = context.getLevel();
        if (level.getWorldBorder().isWithinBounds(partnerPos)
                && level.getBlockState(partnerPos).canBeReplaced(context)) {
            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(PART, DoubleWorkstationPart.LEFT);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide()) {
            BlockPos partnerPos = pos.relative(getPartnerDirection(state.getValue(FACING), DoubleWorkstationPart.LEFT));
            level.setBlock(partnerPos, state.setValue(PART, DoubleWorkstationPart.RIGHT), Block.UPDATE_ALL);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess tickAccess,
                                     BlockPos pos, Direction direction, BlockPos neighborPos,
                                     BlockState neighborState, RandomSource random) {
        DoubleWorkstationPart part = state.getValue(PART);
        if (direction == getPartnerDirection(state.getValue(FACING), part)) {
            if (!neighborState.is(this) || neighborState.getValue(PART) == part) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            BlockPos primaryPos = getPrimaryPos(pos, state);
            if (level.getBlockEntity(primaryPos) instanceof RunicAnvilBlockEntity be) {
                Containers.dropContents(level, primaryPos, be.inventory);
                be.inventory.clearContent();
            }

            DoubleWorkstationPart part = state.getValue(PART);
            BlockPos partnerPos = pos.relative(getPartnerDirection(state.getValue(FACING), part));
            BlockState partnerState = level.getBlockState(partnerPos);
            if (partnerState.is(this) && partnerState.getValue(PART) != part) {
                if (part == DoubleWorkstationPart.RIGHT && !player.isCreative()) {
                    Block.dropResources(partnerState, level, partnerPos, level.getBlockEntity(partnerPos), player, player.getMainHandItem());
                }
                level.setBlock(partnerPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, 2001, partnerPos, Block.getId(partnerState));
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RunicAnvilBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.RUNIC_ANVIL_BE.get(), RunicAnvilBlockEntity::tick);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand == InteractionHand.OFF_HAND && !player.getMainHandItem().isEmpty()) {
            return InteractionResult.CONSUME;
        }
        if (hand == InteractionHand.MAIN_HAND && stack.isEmpty() && !player.getOffhandItem().isEmpty()) {
            return handlePlayerInteraction(level, state, pos, player);
        }
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }
        return handlePlayerInteraction(level, state, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return handlePlayerInteraction(level, state, pos, player);
    }

    public static InteractionResult handlePlayerInteraction(Level level, BlockState state, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos primaryPos = getPrimaryPos(pos, state);
        if (!(level.getBlockEntity(primaryPos) instanceof RunicAnvilBlockEntity be)) {
            return InteractionResult.PASS;
        }

        // 1. Forging in progress prior to hammer impact -> do not interrupt
        if (be.isForging() && be.getForgeTicks() < RunicAnvilBlockEntity.SMASH_IMPACT_TICK) {
            player.displayClientMessage(Component.literal("§d[Runic Anvil] §7Arcane forging in progress..."), true);
            return InteractionResult.SUCCESS;
        }

        // 2. Claim finished floating output item(s) from the anvil inventory
        if (be.hasCompletedOutput()) {
            ItemStack out = be.removeItem(RunicAnvilBlockEntity.SLOT_OUTPUT, 64);
            ItemStack extraGem = be.removeItem(RunicAnvilBlockEntity.SLOT_RETURNED_GEM, 64);
            String claimedName = !out.isEmpty() ? out.getHoverName().getString() : extraGem.getHoverName().getString();
            giveOrDropAtAnvil(level, be, player, out);
            giveOrDropAtAnvil(level, be, player, extraGem);
            level.playSound(null, primaryPos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0F, 1.4F);
            level.playSound(null, primaryPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.55F, 1.25F);
            player.displayClientMessage(Component.literal("§a[Runic Anvil] §fRetrieved §b" + claimedName + "§f!"), true);
            be.markUpdated();
            return InteractionResult.SUCCESS;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack curBase = be.getItem(RunicAnvilBlockEntity.SLOT_BASE);
        ItemStack curCat = be.getItem(RunicAnvilBlockEntity.SLOT_CATALYST);

        // 3. Sneaking or empty-handed while a single un-forged part rests on the anvil -> retrieve it
        if ((player.isShiftKeyDown() || (mainHand.isEmpty() && offHand.isEmpty()))
                && (!curBase.isEmpty() || !curCat.isEmpty())) {
            int slotToPull = !curCat.isEmpty() ? RunicAnvilBlockEntity.SLOT_CATALYST : RunicAnvilBlockEntity.SLOT_BASE;
            ItemStack pulled = be.removeItem(slotToPull, 64);
            giveOrDropAtAnvil(level, be, player, pulled);
            level.playSound(null, primaryPos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.55F, 1.1F);
            be.markUpdated();
            return InteractionResult.SUCCESS;
        }

        // 4. Both anvil input slots empty + holding BOTH parts across Main-Hand & Off-Hand -> load both & forge!
        if (curBase.isEmpty() && curCat.isEmpty()) {
            if (isValidBaseItem(mainHand) && isValidCatalystItem(offHand)) {
                return tryLoadBothAndStart(level, primaryPos, be, player, mainHand, offHand);
            }
            if (isValidBaseItem(offHand) && isValidCatalystItem(mainHand)) {
                return tryLoadBothAndStart(level, primaryPos, be, player, offHand, mainHand);
            }
        }

        // 5. Sneak + Right-Click with socketed Apparatus/Engraved Weapon on empty anvil -> Safe 0s Gem Extraction onto output!
        if (curBase.isEmpty() && curCat.isEmpty() && player.isShiftKeyDown() && isValidBaseItem(mainHand) && offHand.isEmpty()) {
            if (trySafeExtractSocketedGem(level, primaryPos, be, player, mainHand)) {
                return InteractionResult.SUCCESS;
            }
        }

        // 6. Place one part at a time onto the anvil (either Base or Catalyst from Main-Hand or Off-Hand)
        if (tryPlaceSinglePart(level, primaryPos, be, player, mainHand)
                || tryPlaceSinglePart(level, primaryPos, be, player, offHand)) {
            return InteractionResult.SUCCESS;
        }

        player.displayClientMessage(Component.literal(
                "§6[Runic Anvil] §7Place an Apparatus/Weapon + Inscribed Spellgem (or Alloy Ingot) to forge!"
        ), true);
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult tryLoadBothAndStart(Level level, BlockPos pos, RunicAnvilBlockEntity be,
                                                         Player player, ItemStack baseStack, ItemStack catStack) {
        String err = RunicAnvilBlockEntity.validatePair(baseStack, catStack);
        if (err != null) {
            player.displayClientMessage(Component.literal("§e[Runic Anvil] §7" + err), true);
            return InteractionResult.SUCCESS;
        }

        ItemStack placedBase = baseStack.split(1);
        ItemStack placedCat = catStack.copyWithCount(1);
        if (!player.isCreative()) {
            catStack.shrink(1);
        }

        be.inventory.setItem(RunicAnvilBlockEntity.SLOT_BASE, placedBase);
        be.inventory.setItem(RunicAnvilBlockEntity.SLOT_CATALYST, placedCat);
        be.tryBeginForging();
        level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.55F, 1.35F);
        return InteractionResult.SUCCESS;
    }

    private static boolean tryPlaceSinglePart(Level level, BlockPos pos, RunicAnvilBlockEntity be,
                                              Player player, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }
        ItemStack curBase = be.getItem(RunicAnvilBlockEntity.SLOT_BASE);
        ItemStack curCat = be.getItem(RunicAnvilBlockEntity.SLOT_CATALYST);

        if (curBase.isEmpty() && isValidBaseItem(held)) {
            if (!curCat.isEmpty()) {
                String err = RunicAnvilBlockEntity.validatePair(held, curCat);
                if (err != null) {
                    player.displayClientMessage(Component.literal("§e[Runic Anvil] §7" + err), true);
                    return true;
                }
            }
            ItemStack placed = held.split(1);
            be.inventory.setItem(RunicAnvilBlockEntity.SLOT_BASE, placed);
            level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.5F, 1.4F);
            if (!curCat.isEmpty()) {
                be.tryBeginForging();
            } else {
                player.displayClientMessage(Component.literal(
                        "§b[Runic Anvil] §fPlaced §e" + placed.getHoverName().getString()
                                + " §f— add an Inscribed Spellgem or Entropica Alloy Ingot!"
                ), true);
            }
            be.markUpdated();
            return true;
        }

        if (curCat.isEmpty() && isValidCatalystItem(held)) {
            if (!curBase.isEmpty()) {
                String err = RunicAnvilBlockEntity.validatePair(curBase, held);
                if (err != null) {
                    player.displayClientMessage(Component.literal("§e[Runic Anvil] §7" + err), true);
                    return true;
                }
            }
            ItemStack placed = held.copyWithCount(1);
            if (!player.isCreative()) {
                held.shrink(1);
            }
            be.inventory.setItem(RunicAnvilBlockEntity.SLOT_CATALYST, placed);
            level.playSound(null, pos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS, 0.85F, 1.3F);
            if (!curBase.isEmpty()) {
                be.tryBeginForging();
            } else {
                player.displayClientMessage(Component.literal(
                        "§b[Runic Anvil] §fPlaced §d" + placed.getHoverName().getString()
                                + " §f— add a Casting Apparatus, Weapon, or Tool!"
                ), true);
            }
            be.markUpdated();
            return true;
        }

        return false;
    }

    private static boolean trySafeExtractSocketedGem(Level level, BlockPos pos, RunicAnvilBlockEntity be,
                                                     Player player, ItemStack mainHand) {
        CastingApparatusState curState = mainHand.get(ModDataComponents.CASTING_APPARATUS_STATE.get());
        if (curState == null) {
            return false;
        }
        int activeIdx = curState.activeSlotIndex();
        SocketedSpellgem curSocket = curState.getActiveSocket();
        if (curSocket.isEmpty() || curSocket.spell().isEmpty()) {
            return false;
        }

        boolean isDedicated = mainHand.getItem() instanceof CastingApparatusItem;
        ApparatusType targetType = isDedicated
                ? ((CastingApparatusItem) mainHand.getItem()).getApparatusType()
                : ApparatusType.ENGRAVED_TOOL;

        ItemStack extracted = SpellgemItem.createInscribedGem(curSocket.spell().get());
        CastingApparatusState updated = curState.withSocket(activeIdx, SocketedSpellgem.EMPTY);
        if (!isDedicated && updated.countSocketedGems() == 0 && updated.materialEfficiencyBonus() <= 0.0F) {
            mainHand.remove(ModDataComponents.CASTING_APPARATUS_STATE.get());
        } else {
            mainHand.set(ModDataComponents.CASTING_APPARATUS_STATE.get(), updated);
        }

        be.inventory.setItem(RunicAnvilBlockEntity.SLOT_OUTPUT, extracted);
        be.markUpdated();
        level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.9F, 1.35F);
        player.displayClientMessage(Component.literal(
                "§e[Runic Anvil] §fSafely unbound Spellgem from Slot ["
                        + (activeIdx + 1) + "/" + targetType.getGemCapacity() + "] — right-click to collect!"
        ), true);
        return true;
    }

    private static void giveOrDropAtAnvil(Level level, RunicAnvilBlockEntity be, Player player, ItemStack stack) {
        if (stack.isEmpty()) return;
        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            return;
        }
        if (!player.getInventory().add(stack)) {
            Vec3 c = be.getAnvilCenterWorldPos();
            ItemEntity entity = new ItemEntity(level, c.x, c.y + 0.35D, c.z, stack);
            entity.setDefaultPickUpDelay();
            level.addFreshEntity(entity);
        }
    }

    public static boolean isValidBaseItem(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof CastingApparatusItem || isEngravableWeaponOrTool(stack));
    }

    public static boolean isValidCatalystItem(ItemStack stack) {
        return !stack.isEmpty() && (SpellgemItem.isInscribed(stack) || getTemperingAlloyBonus(stack) > 0.0F);
    }

    public static boolean isEngravableWeaponOrTool(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() instanceof CastingApparatusItem || stack.getItem() instanceof BlockItem) {
            return false;
        }
        if (stack.getItem() instanceof DynamicVisWeaponItem) {
            return true;
        }
        if (stack.is(ItemTags.SWORDS)
                || stack.is(ItemTags.AXES)
                || stack.is(ItemTags.PICKAXES)
                || stack.is(ItemTags.SHOVELS)
                || stack.is(ItemTags.HOES)
                || stack.is(Items.TRIDENT)
                || stack.is(Items.MACE)) {
            return true;
        }
        return stack.getMaxStackSize() == 1 && stack.isDamageableItem()
                && !stack.has(DataComponents.EQUIPPABLE);
    }

    public static float getTemperingAlloyBonus(ItemStack offHand) {
        if (offHand.isEmpty()) return 0.0F;
        if (offHand.is(ModItems.ATHANORITE_INGOT.get()) || offHand.is(ModItems.CHARGED_ATHANORITE_INGOT.get())) {
            return 0.30F;
        }
        if (offHand.is(ModItems.SANGUINITE_INGOT.get()) || offHand.is(ModItems.CHARGED_SANGUINITE_INGOT.get())
                || offHand.is(ModItems.MERCURITE_INGOT.get()) || offHand.is(ModItems.CHARGED_MERCURITE_INGOT.get())
                || offHand.is(ModItems.EUCLIDITE_INGOT.get()) || offHand.is(ModItems.CHARGED_EUCLIDITE_INGOT.get())) {
            return 0.25F;
        }
        if (offHand.is(ModItems.EIDOLITE_INGOT.get()) || offHand.is(ModItems.CHARGED_EIDOLITE_INGOT.get())) {
            return 0.20F;
        }
        if (offHand.is(ModItems.RESONITE_INGOT.get()) || offHand.is(ModItems.CHARGED_RESONITE_INGOT.get())) {
            return 0.15F;
        }
        if (offHand.is(ModItems.VISCANITE_INGOT.get()) || offHand.is(ModItems.CHARGED_VISCANITE_INGOT.get())) {
            return 0.10F;
        }
        if (offHand.is(ModItems.ARCANITE_INGOT.get()) || offHand.is(ModItems.CHARGED_ARCANITE_INGOT.get())) {
            return 0.05F;
        }
        return 0.0F;
    }
}
