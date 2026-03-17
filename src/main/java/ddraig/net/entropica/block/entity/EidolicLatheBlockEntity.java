package ddraig.net.entropica.block.entity;

import com.mojang.serialization.Codec;
import ddraig.net.entropica.api.EssenceType;
import ddraig.net.entropica.api.fumes.IFumeHandler;
import ddraig.net.entropica.api.fumes.IFumeMultiblockController;
import ddraig.net.entropica.api.fumes.VisFumeStack;
import ddraig.net.entropica.api.ichor.IIchorMultiblock;
import ddraig.net.entropica.api.ichor.VisIchorStack;
import ddraig.net.entropica.block.AttunementPedestalBlock;
import ddraig.net.entropica.block.EidolicFocalPedestalBlock;
import ddraig.net.entropica.block.VisFumeInputPortBlock;
import ddraig.net.entropica.block.VisIchorInputPortBlock;
import ddraig.net.entropica.registry.ModBlockEntities;
import ddraig.net.entropica.registry.ModBlocks;
import ddraig.net.entropica.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class EidolicLatheBlockEntity extends BlockEntity implements IFumeHandler, IFumeMultiblockController, IIchorMultiblock {

    public final SimpleContainer inventory = new SimpleContainer(5) {
        @Override
        public void setChanged() {
            super.setChanged();
            EidolicLatheBlockEntity.this.setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private boolean isFormed = false;
    private int recheckDelay = -1;
    private int tickCount = 0;

    public final List<BlockPos> connectedPedestals = new ArrayList<>();
    public final List<BlockPos> fuelPedestals = new ArrayList<>();

    public BlockPos activePortPos = null;

    // Fuel Buffers
    private VisFumeStack storedFume = VisFumeStack.EMPTY;
    private VisIchorStack storedIchor = VisIchorStack.EMPTY;
    private static final int MAX_VIS_CAPACITY = 100000;

    // Crafting State Machine
    public boolean isCrafting = false;
    public boolean waitForClick = false;
    public int craftingProgress = 0;
    public int maxCraftingProgress = 500; // 25 Seconds
    public EssenceType craftingEssenceType = null;
    private boolean isIchorCraft = false;
    private int ampouleFumeTotal = 0;

    public EidolicLatheBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EIDOLIC_FOCAL_PEDESTAL_BE.get(), pos, state);
    }

    public AABB getRenderBoundingBox() {
        return new AABB(this.worldPosition).inflate(15.0);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;

        tickCount++;

        if (this.isFormed && tickCount % 10 == 0) {
            this.attemptFormMultiblock();
        }

        if (this.recheckDelay > 0) {
            this.recheckDelay--;
            if (this.recheckDelay == 0) {
                boolean wasFormed = this.isFormed;
                boolean success = this.attemptFormMultiblock();
                if (wasFormed != success) {
                    level.setBlock(pos, state.setValue(EidolicFocalPedestalBlock.FORMED, success), 3);
                }
            }
        }

        if (this.isFormed && this.isCrafting) {

            if (this.waitForClick) {
                return; // PAUSED! Waiting for the player to click to advance the ritual.
            }

            // Pause thresholds based on the 500 tick limit (30%, 60%, 85%)
            if (this.craftingProgress == 150 || this.craftingProgress == 300 || this.craftingProgress == 425) {
                this.waitForClick = true;
                this.setChanged();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                return;
            }

            this.craftingProgress++;

            if (this.craftingProgress % 5 == 0) {
                this.setChanged();
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }

            if (this.craftingProgress >= this.maxCraftingProgress) {
                finishCrafting();
            }
        }
    }

    // ==========================================
    // AMPOULE HELPER METHODS
    // ==========================================

    private int getAmpouleCapacity(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (rl == null) return 0;
        String path = rl.getPath();

        if (path.contains("ampoule") && !path.contains("base")) {
            if (path.contains("large_")) return 64;
            if (path.contains("medium_")) return 16;
            if (path.contains("small_")) return 4;
        }
        return 0;
    }

    private Item getAmpouleBase(ItemStack stack) {
        if (stack.isEmpty()) return net.minecraft.world.item.Items.GLASS_BOTTLE;
        ResourceLocation rl = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (rl == null) return net.minecraft.world.item.Items.GLASS_BOTTLE;
        String path = rl.getPath();

        if (path.contains("large_")) return ModItems.LARGE_AMPOULE_BASE.get();
        if (path.contains("medium_")) return ModItems.MEDIUM_AMPOULE_BASE.get();
        if (path.contains("small_")) return ModItems.SMALL_AMPOULE_BASE.get();
        return net.minecraft.world.item.Items.GLASS_BOTTLE;
    }

    private EssenceType getAmpouleType(ItemStack stack) {
        if (stack.isEmpty()) return null;

        net.minecraft.world.item.component.CustomData data = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
        if (data.copyTag().contains("EssenceType")) {
            try {
                return EssenceType.valueOf(data.copyTag().getString("EssenceType").orElse(""));
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    // ==========================================
    // DYNAMIC LATHE CRAFTING LOGIC
    // ==========================================

    public void attemptCraft(Player player) {
        if (!this.isFormed || this.level == null || this.level.isClientSide() || this.isCrafting) return;

        if (!this.inventory.getItem(4).isEmpty()) {
            if (player != null) player.displayClientMessage(Component.literal("§cClear the completed weapon from the focal pedestal first!"), true);
            return;
        }

        // 1. SCAN CENTRAL PEDESTAL FOR WEAPON SKELETON
        ItemStack conceptStack = ItemStack.EMPTY;
        ItemStack coreStack = ItemStack.EMPTY;
        ItemStack materialStack = ItemStack.EMPTY;

        for (int i = 0; i < 4; i++) {
            ItemStack s = this.inventory.getItem(i);
            if (s.isEmpty()) continue;

            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(s.getItem());
            if (rl != null) {
                String path = rl.getPath();
                if (path.contains("concept")) conceptStack = s;
                else if (path.contains("core")) coreStack = s;
                else if (path.contains("arcanite") || path.contains("viscanite") || path.contains("resonite") || path.contains("eidolite")) {
                    materialStack = s;
                }
            }
        }

        if (conceptStack.isEmpty() || coreStack.isEmpty() || materialStack.isEmpty() || materialStack.getCount() < 4) {
            if (player != null) player.displayClientMessage(Component.literal("§cMissing Weapon Skeleton. Requires: 1x Concept, 1x Core, 4x Base Material."), true);
            return;
        }

        // 2. SCAN ATTUNEMENT PEDESTALS FOR AMPOULES & MODIFIERS
        int tempAmpouleFuelTotal = 0;
        EssenceType detectedAmpouleType = null;
        boolean hasMismatch = false;

        this.fuelPedestals.clear();

        for (BlockPos pPos : this.connectedPedestals) {
            if (this.level.getBlockEntity(pPos) instanceof AttunementPedestalBlockEntity ped) {
                boolean providedFuel = false;
                for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                    ItemStack stack = ped.inventory.getItem(i);
                    if (!stack.isEmpty()) {
                        int cap = getAmpouleCapacity(stack);
                        EssenceType t = getAmpouleType(stack);

                        if (cap > 0 && t != null) {
                            if (detectedAmpouleType == null) {
                                detectedAmpouleType = t;
                            } else if (detectedAmpouleType != t) {
                                hasMismatch = true;
                            }

                            if (!hasMismatch) {
                                tempAmpouleFuelTotal += cap * stack.getCount();
                                providedFuel = true;
                            }
                        }
                    }
                }
                if (providedFuel) {
                    this.fuelPedestals.add(pPos);
                }
            }
        }

        if (hasMismatch) {
            if (player != null) {
                player.displayClientMessage(Component.literal("§cVolatile Resonance! Mismatched Vis types detected!"), true);

                for (BlockPos pPos : this.connectedPedestals) {
                    if (this.level.getBlockEntity(pPos) instanceof AttunementPedestalBlockEntity ped) {
                        boolean pedChanged = false;
                        for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                            ItemStack stack = ped.inventory.getItem(i);
                            if (getAmpouleCapacity(stack) > 0 && getAmpouleType(stack) != null) {
                                Item base = getAmpouleBase(stack);
                                int count = stack.getCount();
                                ped.inventory.setItem(i, new ItemStack(base, count));
                                pedChanged = true;
                            }
                        }
                        if (pedChanged) {
                            ped.setChanged();
                            this.level.sendBlockUpdated(pPos, ped.getBlockState(), ped.getBlockState(), 3);
                        }
                    }
                }

                player.hurt(this.level.damageSources().magic(), 6.0f);
                Vec3 dir = player.position().subtract(this.worldPosition.getCenter()).normalize();
                player.push(dir.x * 1.5, 0.5, dir.z * 1.5);
                player.hurtMarked = true;

                if (this.level instanceof ServerLevel sl) {
                    sl.playSound(null, this.worldPosition, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            }
            return;
        }

        // 3. Validate Fuel Availability (Network + Pedestal Ampoules)
        boolean hasFume = this.storedFume != null && !this.storedFume.isEmpty() && this.storedFume.getAmount() > 0;
        boolean hasIchor = this.storedIchor != null && !this.storedIchor.isEmpty() && this.storedIchor.getAmount() > 0;
        boolean hasAmpoule = tempAmpouleFuelTotal > 0;

        if (!hasFume && !hasIchor && !hasAmpoule) {
            if (player != null) player.displayClientMessage(Component.literal("§cInsufficient Vis or Ichor in the network and no ampoules provided."), true);
            return;
        }

        EssenceType networkType = hasIchor ? this.storedIchor.getType() : (hasFume ? this.storedFume.getType() : null);

        if (networkType != null && hasAmpoule && networkType != detectedAmpouleType) {
            if (player != null) player.displayClientMessage(Component.literal("§cMismatched Vis types between network and ampoules."), true);
            return;
        }

        // Lock in the crafting state
        this.isCrafting = true;
        this.waitForClick = false;
        this.craftingProgress = 0;
        this.maxCraftingProgress = 500; // 25 second ritual

        this.craftingEssenceType = networkType != null ? networkType : detectedAmpouleType;
        this.isIchorCraft = false; // Default until an Ichor pipe connects
        this.ampouleFumeTotal = tempAmpouleFuelTotal;

        this.storedFume = VisFumeStack.EMPTY;
        this.storedIchor = VisIchorStack.EMPTY;

        if (player != null) player.displayClientMessage(Component.literal("§aLathe activated. Commencing Phase 1: Essence Injection..."), true);

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

        if (this.level instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 0.8f);
        }
    }

    private void finishCrafting() {
        this.isCrafting = false;
        this.waitForClick = false;

        // 1. RE-VALIDATE SKELETON
        ItemStack conceptStack = ItemStack.EMPTY;
        ItemStack coreStack = ItemStack.EMPTY;
        ItemStack materialStack = ItemStack.EMPTY;

        for (int i = 0; i < 4; i++) {
            ItemStack s = this.inventory.getItem(i);
            if (s.isEmpty()) continue;
            ResourceLocation rl = BuiltInRegistries.ITEM.getKey(s.getItem());
            if (rl != null) {
                String path = rl.getPath();
                if (path.contains("concept")) conceptStack = s;
                else if (path.contains("core")) coreStack = s;
                else {
                    materialStack = s;
                }
            }
        }

        if (conceptStack.isEmpty() || coreStack.isEmpty() || materialStack.isEmpty() || materialStack.getCount() < 4) {
            this.storedFume = VisFumeStack.EMPTY;
            this.storedIchor = VisIchorStack.EMPTY;
            this.setChanged();
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            return;
        }

        // 2. DETERMINE OUTPUT ITEM AND BASE STATS
        String conceptPath = BuiltInRegistries.ITEM.getKey(conceptStack.getItem()).getPath();
        Item outputBaseItem = ModItems.DYNAMIC_SWORD.get(); // Fallback
        if (conceptPath.contains("axe")) outputBaseItem = ModItems.DYNAMIC_AXE.get();
        else if (conceptPath.contains("pickaxe")) outputBaseItem = ModItems.DYNAMIC_PICKAXE.get();

        ItemStack result = new ItemStack(outputBaseItem);

        // Determine Autonomous Status & Core Slots
        String corePath = BuiltInRegistries.ITEM.getKey(coreStack.getItem()).getPath();
        boolean isAutonomous = corePath.contains("eidolite");
        int coreSlots = 3;
        if (corePath.contains("ancient")) coreSlots = 9;
        else if (corePath.contains("charged")) coreSlots = 6;

        // Determine Material Tier dynamically based on the ingot path
        String materialPath = BuiltInRegistries.ITEM.getKey(materialStack.getItem()).getPath();
        String tierName = "Arcanite";
        float basePhysicalDamage = 5.0f;
        float baseAttackSpeed = -2.4f; // Vanilla Sword Speed

        if (materialPath.contains("viscanite")) {
            tierName = "Viscanite"; basePhysicalDamage = 7.0f;
        } else if (materialPath.contains("resonite")) {
            tierName = "Resonite"; basePhysicalDamage = 9.0f;
        } else if (materialPath.contains("eidolite")) {
            tierName = "Eidolite"; basePhysicalDamage = 11.0f;
        }

        // 3. PARSE MODIFIERS FROM PEDESTALS
        float modifierDamage = 0f;
        float modifierSpeed = 0f;
        String embeddedSpell = "none";

        List<AttunementPedestalBlockEntity> pedestalsToConsume = new ArrayList<>();

        for (BlockPos pPos : this.connectedPedestals) {
            if (this.level.getBlockEntity(pPos) instanceof AttunementPedestalBlockEntity ped) {
                pedestalsToConsume.add(ped);
                for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                    ItemStack stack = ped.inventory.getItem(i);
                    if (!stack.isEmpty()) {
                        int cap = getAmpouleCapacity(stack);
                        if (cap == 0 || getAmpouleType(stack) != this.craftingEssenceType) {
                            String modPath = BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath();

                            if (modPath.contains("quartz")) modifierDamage += 1.0f;
                            else if (modPath.contains("feather")) modifierSpeed += 0.1f;
                            else if (modPath.contains("spell_gem")) {
                                // Dynamically extract the spell ID stored on the gem! Use Optional safely.
                                net.minecraft.world.item.component.CustomData spellData = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.EMPTY);
                                if (spellData.copyTag().contains("SpellID")) {
                                    embeddedSpell = spellData.copyTag().getString("SpellID").orElse(modPath.replace("spell_gem_", ""));
                                } else {
                                    // Fallback for statically named gems
                                    embeddedSpell = modPath.replace("spell_gem_", "");
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. CALCULATE VIS DAMAGE SCALING
        float multiplier = this.isIchorCraft ? 20.0f : 1.0f;
        int networkFumesCollected = this.isIchorCraft ? this.storedIchor.getAmount() : this.storedFume.getAmount();
        float equivalentFumes = (networkFumesCollected * multiplier) + (this.ampouleFumeTotal * 1.0f);

        // 5. APPLY ATTRIBUTES TO WEAPON
        net.minecraft.world.item.component.ItemAttributeModifiers.Builder modifierBuilder = net.minecraft.world.item.component.ItemAttributeModifiers.builder();

        if (this.craftingEssenceType == EssenceType.REGULAR) {
            float extraPhysical = (Math.min((float)MAX_VIS_CAPACITY, equivalentFumes) / (float)MAX_VIS_CAPACITY) * 10.0f;
            float totalDamage = basePhysicalDamage + modifierDamage + extraPhysical;

            modifierBuilder.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "base_attack_damage"), totalDamage, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
            modifierBuilder.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "base_attack_speed"), baseAttackSpeed + modifierSpeed, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);

            result.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, modifierBuilder.build());

            if (ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE != null) {
                // Constructs using exactly: Builder(baseType, baseDamage, innateSlots)
                ddraig.net.entropica.component.VisWeaponState state = new ddraig.net.entropica.component.VisWeaponState.Builder(this.craftingEssenceType, 0f, coreSlots)
                        .material(tierName)
                        .speed(modifierSpeed)
                        .spell(embeddedSpell)
                        .autonomous(isAutonomous)
                        .build();
                result.set(ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE.get(), state);
            }
        } else {
            float extraVisDamage = (Math.min((float)MAX_VIS_CAPACITY, equivalentFumes) / (float)MAX_VIS_CAPACITY) * 20.0f;

            modifierBuilder.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "base_attack_damage"), basePhysicalDamage + modifierDamage, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);
            modifierBuilder.add(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED, new net.minecraft.world.entity.ai.attributes.AttributeModifier(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("entropica", "base_attack_speed"), baseAttackSpeed + modifierSpeed, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE), net.minecraft.world.entity.EquipmentSlotGroup.MAINHAND);

            result.set(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS, modifierBuilder.build());

            if (ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE != null && this.craftingEssenceType != null) {
                // Constructs using exactly: Builder(baseType, baseDamage, innateSlots)
                ddraig.net.entropica.component.VisWeaponState state = new ddraig.net.entropica.component.VisWeaponState.Builder(this.craftingEssenceType, extraVisDamage, coreSlots)
                        .material(tierName)
                        .speed(modifierSpeed)
                        .spell(embeddedSpell)
                        .autonomous(isAutonomous)
                        .build();
                result.set(ddraig.net.entropica.registry.ModDataComponents.VIS_WEAPON_STATE.get(), state);
            }
        }

        // 6. CONSUME PHYSICAL ITEMS
        conceptStack.shrink(1);
        coreStack.shrink(1);
        materialStack.shrink(4);

        // Drain Ampoules
        int remainingToDrain = this.ampouleFumeTotal;
        if (remainingToDrain > 0) {
            for (AttunementPedestalBlockEntity ped : pedestalsToConsume) {
                boolean pedChanged = false;
                for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                    ItemStack stack = ped.inventory.getItem(i);
                    int cap = getAmpouleCapacity(stack);

                    if (cap > 0 && getAmpouleType(stack) == this.craftingEssenceType) {
                        int count = stack.getCount();
                        int drainedCount = 0;
                        while (remainingToDrain > 0 && drainedCount < count) {
                            remainingToDrain -= cap;
                            drainedCount++;
                        }
                        if (drainedCount > 0) {
                            Item base = getAmpouleBase(stack);
                            stack.shrink(drainedCount);
                            ItemStack emptyStack = new ItemStack(base, drainedCount);
                            if (stack.isEmpty()) ped.inventory.setItem(i, emptyStack);
                            else Containers.dropItemStack(this.level, ped.getBlockPos().getX() + 0.5, ped.getBlockPos().getY() + 1.2, ped.getBlockPos().getZ() + 0.5, emptyStack);
                            pedChanged = true;
                        }
                    }
                }
                if (pedChanged) {
                    ped.setChanged();
                    this.level.sendBlockUpdated(ped.getBlockPos(), ped.getBlockState(), ped.getBlockState(), 3);
                }
            }
        }

        // Consume standard modifiers
        for (AttunementPedestalBlockEntity ped : pedestalsToConsume) {
            boolean pedChanged = false;
            for (int i = 0; i < ped.inventory.getContainerSize(); i++) {
                ItemStack stack = ped.inventory.getItem(i);
                if (!stack.isEmpty()) {
                    int cap = getAmpouleCapacity(stack);
                    if (cap == 0 || getAmpouleType(stack) != this.craftingEssenceType) {
                        stack.shrink(1);
                        pedChanged = true;
                    }
                }
            }
            if (pedChanged) {
                ped.setChanged();
                this.level.sendBlockUpdated(ped.getBlockPos(), ped.getBlockState(), ped.getBlockState(), 3);
            }
        }

        // Insert Final Result into the dedicated hovering output slot (Slot 4)
        this.inventory.setItem(4, result);

        // Empty the network tanks to prevent leftover Vis!
        this.storedFume = VisFumeStack.EMPTY;
        this.storedIchor = VisIchorStack.EMPTY;

        this.setChanged();
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);

        if (this.level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5, 50, 0.2, 0.2, 0.2, 0.2);
            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.WITCH, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.2, this.worldPosition.getZ() + 0.5, 50, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, this.worldPosition, SoundEvents.TOTEM_USE, SoundSource.BLOCKS, 1.0f, 1.5f);
        }
    }

    public boolean attemptFormMultiblock() {
        if (this.level == null || this.level.isClientSide()) return false;

        int baseX = this.worldPosition.getX();
        int baseY = this.worldPosition.getY() - 1;
        int baseZ = this.worldPosition.getZ();

        int attunementCount = 0;
        int portCount = 0;
        List<BlockPos> foundPedestals = new ArrayList<>();
        BlockPos foundPort = null;

        // Scan the 7x7x5 main structure
        for (int y = 0; y < 5; y++) {
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    BlockPos scanPos = new BlockPos(baseX + x, baseY + y, baseZ + z);
                    BlockState scanState = this.level.getBlockState(scanPos);
                    Block block = scanState.getBlock();

                    boolean isPort = (block instanceof VisFumeInputPortBlock || block instanceof VisIchorInputPortBlock);
                    boolean isAirOrPort = scanState.isAir() || isPort;

                    if (isPort) {
                        portCount++;
                        if (foundPort == null) foundPort = scanPos;
                    }

                    if (y == 0) {
                        if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                    } else if (y == 1) {
                        if (x == 0 && z == 0) {
                            if (!scanPos.equals(this.worldPosition)) return failFormation();
                        } else if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (block instanceof AttunementPedestalBlock) {
                                attunementCount++;
                                foundPedestals.add(scanPos);
                            } else if (!isAirOrPort) {
                                return failFormation();
                            }
                        }
                    } else if (y == 2 || y == 3) {
                        if (Math.abs(x) == 3 && Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (!isAirOrPort) return failFormation();
                        }
                    } else if (y == 4) {
                        if (Math.abs(x) == 3 || Math.abs(z) == 3) {
                            if (block != ModBlocks.ARCANE_FORGE_BASE.get()) return failFormation();
                        } else {
                            if (!isAirOrPort) return failFormation();
                        }
                    }
                }
            }
        }

        // Layer 6 (Air Check / Floating Ports Check exactly above the structure)
        for (int x = -3; x <= 3; x++) {
            for (int z = -3; z <= 3; z++) {
                BlockPos scanPos = new BlockPos(baseX + x, baseY + 5, baseZ + z);
                Block block = this.level.getBlockState(scanPos).getBlock();
                if (block instanceof VisFumeInputPortBlock || block instanceof VisIchorInputPortBlock) {
                    portCount++;
                    if (foundPort == null) foundPort = scanPos;
                }
            }
        }

        if (portCount < 1) return failFormation();
        if (attunementCount < 4 || attunementCount > 8) return failFormation();

        boolean pedestalsChanged = !this.connectedPedestals.equals(foundPedestals);
        boolean portChanged = (this.activePortPos == null && foundPort != null) || (this.activePortPos != null && !this.activePortPos.equals(foundPort));

        if (!this.isFormed || pedestalsChanged || portChanged) {
            this.connectedPedestals.clear();
            this.connectedPedestals.addAll(foundPedestals);
            this.activePortPos = foundPort;

            this.isFormed = true;
            updateBlockState(true);
            this.setChanged();

            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return true;
    }

    private boolean failFormation() {
        if (this.isFormed) {
            this.isFormed = false;
            this.isCrafting = false;
            this.waitForClick = false;
            updateBlockState(false);
            this.setChanged();

            if (this.level != null) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }

            this.recheckDelay = 5;
        }
        return false;
    }

    private void updateBlockState(boolean formed) {
        if (this.level != null) {
            BlockState state = this.level.getBlockState(this.worldPosition);
            if (state.hasProperty(EidolicFocalPedestalBlock.FORMED) && state.getValue(EidolicFocalPedestalBlock.FORMED) != formed) {
                this.level.setBlock(this.worldPosition, state.setValue(EidolicFocalPedestalBlock.FORMED, formed), 3);
            }
        }
    }

    public boolean isFormed() {
        return isFormed;
    }

    public boolean interactWithPlayer(Player player, InteractionHand hand) {
        if (this.isCrafting) {
            if (this.waitForClick) {
                this.waitForClick = false;
                if (player != null) {
                    if (this.craftingProgress == 150) player.displayClientMessage(Component.literal("§dPhase 2: Distributing Essences..."), true);
                    else if (this.craftingProgress == 300) player.displayClientMessage(Component.literal("§dPhase 3: Channeling to Apex..."), true);
                    else if (this.craftingProgress == 425) player.displayClientMessage(Component.literal("§dPhase 4: Finalizing Forge..."), true);
                }

                this.craftingProgress++;

                this.setChanged();
                if (this.level != null && !this.level.isClientSide()) {
                    this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
                }
                return true;
            }
            player.displayClientMessage(Component.literal("§cThe Lathe is actively forging!"), true);
            return false;
        }

        ItemStack heldItem = player.getItemInHand(hand);

        if (heldItem.isEmpty() && !inventory.getItem(4).isEmpty()) {
            player.setItemInHand(hand, inventory.getItem(4).copy());
            inventory.setItem(4, ItemStack.EMPTY);
            return true;
        }

        if (heldItem.isEmpty()) {
            for (int i = 3; i >= 0; i--) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (!stackInSlot.isEmpty()) {
                    player.setItemInHand(hand, stackInSlot.copy());
                    inventory.setItem(i, ItemStack.EMPTY);
                    return true;
                }
            }
        }
        else {
            for (int i = 0; i < 4; i++) {
                ItemStack stackInSlot = inventory.getItem(i);
                if (stackInSlot.isEmpty()) {
                    inventory.setItem(i, heldItem.copyWithCount(1));
                    heldItem.shrink(1);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.inventory);
        }
    }

    // ==========================================
    // MULTIBLOCK FUME HANDLER IMPLEMENTATION
    // ==========================================

    @Override
    public void invalidateMultiblock() {
        this.failFormation();
    }

    @Override
    public int getSafeCapacity() {
        return this.isFormed ? MAX_VIS_CAPACITY : 0;
    }

    @Override
    public int getAbsoluteCapacity() {
        return this.isFormed ? MAX_VIS_CAPACITY : 0;
    }

    @Override
    public VisFumeStack getStoredFume() {
        return this.storedFume;
    }

    @Override
    public VisFumeStack getFumeInTank() {
        return this.storedFume != null ? this.storedFume : VisFumeStack.EMPTY;
    }

    @Override
    public int fill(VisFumeStack resource, boolean simulate) {
        if (!this.isFormed || resource.isEmpty()) return 0;

        if (!this.isCrafting) return 0;

        if (this.craftingEssenceType == null) {
            if (!simulate) {
                this.craftingEssenceType = resource.getType();
                this.isIchorCraft = false;
                this.setChanged();
            }
        } else if (this.craftingEssenceType != resource.getType()) {
            return 0; // Reject mismatched Fumes during crafting
        }

        int space = getSafeCapacity() - this.storedFume.getAmount();
        if (space <= 0) return 0;

        int amountToFill = Math.min(resource.getAmount(), space);

        if (!simulate) {
            if (this.storedFume.isEmpty()) {
                this.storedFume = new VisFumeStack(resource.getType(), amountToFill);
            } else {
                this.storedFume.grow(amountToFill);
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return amountToFill;
    }

    @Override
    public VisFumeStack drain(int maxDrain, boolean simulate) {
        if (!this.isFormed || this.storedFume.isEmpty() || maxDrain <= 0) return VisFumeStack.EMPTY;

        int amountToDrain = Math.min(this.storedFume.getAmount(), maxDrain);
        VisFumeStack drained = new VisFumeStack(this.storedFume.getType(), amountToDrain);

        if (!simulate) {
            this.storedFume.shrink(amountToDrain);
            if (this.storedFume.getAmount() <= 0) {
                this.storedFume = VisFumeStack.EMPTY;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return drained;
    }

    // ==========================================
    // MULTIBLOCK ICHOR HANDLER IMPLEMENTATION
    // ==========================================

    @Override
    public BlockPos getMasterPos() {
        return this.worldPosition;
    }

    @Override
    public boolean isMaster() {
        return true;
    }

    @Override
    public boolean canConnectIchor(Direction side) {
        return true; // The Lathe accepts connections globally via its floating ports
    }

    @Override
    public boolean isIchorValid(EssenceType type) {
        return true; // The Lathe accepts any Ichor type for crafting
    }

    @Override
    public int getCapacity() {
        return this.isFormed ? MAX_VIS_CAPACITY : 0;
    }

    @Override
    public VisIchorStack getIchorInTank() {
        return this.storedIchor != null ? this.storedIchor : VisIchorStack.EMPTY;
    }

    @Override
    public int fill(VisIchorStack resource, boolean simulate) {
        if (!this.isFormed || resource.isEmpty()) return 0;

        if (!this.isCrafting) return 0;

        if (this.craftingEssenceType == null) {
            if (!simulate) {
                this.craftingEssenceType = resource.getType();
                this.isIchorCraft = true;
                this.setChanged();
            }
        } else if (this.craftingEssenceType != resource.getType()) {
            return 0; // Reject mismatched Ichor
        }

        int space = getCapacity() - this.storedIchor.getAmount();
        if (space <= 0) return 0;

        int amountToFill = Math.min(resource.getAmount(), space);

        if (!simulate) {
            if (this.storedIchor.isEmpty()) {
                this.storedIchor = new VisIchorStack(resource.getType(), amountToFill);
            } else {
                this.storedIchor.grow(amountToFill);
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return amountToFill;
    }

    @Override
    public VisIchorStack drainIchor(int maxDrain, boolean simulate) {
        if (!this.isFormed || this.storedIchor.isEmpty() || maxDrain <= 0) return VisIchorStack.EMPTY;

        int amountToDrain = Math.min(this.storedIchor.getAmount(), maxDrain);
        VisIchorStack drained = new VisIchorStack(this.storedIchor.getType(), amountToDrain);

        if (!simulate) {
            this.storedIchor.shrink(amountToDrain);
            if (this.storedIchor.getAmount() <= 0) {
                this.storedIchor = VisIchorStack.EMPTY;
            }
            this.setChanged();
            if (this.level != null && !this.level.isClientSide()) {
                this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
            }
        }
        return drained;
    }

    // ==========================================
    // SAVE / LOAD LOGIC
    // ==========================================

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("IsFormed", Codec.BOOL, this.isFormed);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            output.store("LatheStack_" + i, ItemStack.OPTIONAL_CODEC, inventory.getItem(i));
        }
        output.store("ConnectedPedestals", BlockPos.CODEC.listOf(), this.connectedPedestals);

        output.store("FuelPedestalsCount", Codec.INT, this.fuelPedestals.size());
        for (int i = 0; i < this.fuelPedestals.size(); i++) {
            output.store("FuelPedestal_" + i, Codec.LONG, this.fuelPedestals.get(i).asLong());
        }

        if (this.activePortPos != null) {
            output.store("ActivePortPos", Codec.LONG, this.activePortPos.asLong());
        }

        output.store("IsCrafting", Codec.BOOL, this.isCrafting);
        output.store("WaitForClick", Codec.BOOL, this.waitForClick);
        output.store("CraftProgress", Codec.INT, this.craftingProgress);
        output.store("MaxCraftProgress", Codec.INT, this.maxCraftingProgress);
        output.store("IsIchor", Codec.BOOL, this.isIchorCraft);

        output.store("AmpouleFumeTotal", Codec.INT, this.ampouleFumeTotal);

        if (this.craftingEssenceType != null) {
            output.store("CraftType", Codec.STRING, this.craftingEssenceType.name());
        }

        if (!this.storedFume.isEmpty()) {
            output.store("FumeType", Codec.STRING, this.storedFume.getType().name());
            output.store("FumeAmount", Codec.INT, this.storedFume.getAmount());
        }
        if (!this.storedIchor.isEmpty()) {
            output.store("IchorType", Codec.STRING, this.storedIchor.getType().name());
            output.store("IchorAmount", Codec.INT, this.storedIchor.getAmount());
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.isFormed = input.read("IsFormed", Codec.BOOL).orElse(false);
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            int finalI = i;
            input.read("LatheStack_" + i, ItemStack.OPTIONAL_CODEC).ifPresent(stack -> inventory.setItem(finalI, stack));
        }

        this.connectedPedestals.clear();
        input.read("ConnectedPedestals", BlockPos.CODEC.listOf()).ifPresent(this.connectedPedestals::addAll);

        this.fuelPedestals.clear();
        int fpCount = input.read("FuelPedestalsCount", Codec.INT).orElse(0);
        for (int i = 0; i < fpCount; i++) {
            long fp = input.read("FuelPedestal_" + i, Codec.LONG).orElse(-1L);
            if (fp != -1L) this.fuelPedestals.add(BlockPos.of(fp));
        }

        long pPos = input.read("ActivePortPos", Codec.LONG).orElse(-1L);
        if (pPos != -1L) this.activePortPos = BlockPos.of(pPos);

        this.isCrafting = input.read("IsCrafting", Codec.BOOL).orElse(false);
        this.waitForClick = input.read("WaitForClick", Codec.BOOL).orElse(false);
        this.craftingProgress = input.read("CraftProgress", Codec.INT).orElse(0);
        this.maxCraftingProgress = input.read("MaxCraftProgress", Codec.INT).orElse(1);
        this.isIchorCraft = input.read("IsIchor", Codec.BOOL).orElse(false);

        this.ampouleFumeTotal = input.read("AmpouleFumeTotal", Codec.INT).orElse(0);

        String cTypeStr = input.read("CraftType", Codec.STRING).orElse("");
        if (!cTypeStr.isEmpty()) {
            try { this.craftingEssenceType = EssenceType.valueOf(cTypeStr); }
            catch (IllegalArgumentException e) { this.craftingEssenceType = null; }
        }

        String fumeTypeStr = input.read("FumeType", Codec.STRING).orElse("");
        int fumeAmt = input.read("FumeAmount", Codec.INT).orElse(0);
        if (!fumeTypeStr.isEmpty() && fumeAmt > 0) {
            try { this.storedFume = new VisFumeStack(EssenceType.valueOf(fumeTypeStr), fumeAmt); }
            catch (IllegalArgumentException e) { this.storedFume = VisFumeStack.EMPTY; }
        } else { this.storedFume = VisFumeStack.EMPTY; }

        String ichorTypeStr = input.read("IchorType", Codec.STRING).orElse("");
        int ichorAmt = input.read("IchorAmount", Codec.INT).orElse(0);
        if (!ichorTypeStr.isEmpty() && ichorAmt > 0) {
            try { this.storedIchor = new VisIchorStack(EssenceType.valueOf(ichorTypeStr), ichorAmt); }
            catch (IllegalArgumentException e) { this.storedIchor = VisIchorStack.EMPTY; }
        } else { this.storedIchor = VisIchorStack.EMPTY; }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p) { return this.saveWithoutMetadata(p); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}