package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import stardewvalley.modid.crop.CropQuality;
import stardewvalley.modid.block.MachineType.RecipeResult;
import stardewvalley.modid.item.WarpPositionState;

import java.util.Random;
import java.util.UUID;

public class ArtisanEquipmentBlock extends Block implements BlockEntityProvider {
    public static final EnumProperty<MachineState> STATE = EnumProperty.of("state", MachineState.class);

    private final VoxelShape shape;
    private final MachineType machineType;

    public enum MachineState implements StringIdentifiable {
        EMPTY("empty"),
        PROCESSING("processing"),
        DONE("done");

        private final String name;

        MachineState(String name) {
            this.name = name;
        }

        @Override
        public String asString() {
            return name;
        }
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return MapCodec.unit(this);
    }

    public ArtisanEquipmentBlock(Settings settings, VoxelShape shape, MachineType machineType) {
        super(settings);
        this.shape = shape;
        this.machineType = machineType;
        setDefaultState(getDefaultState().with(STATE, MachineState.EMPTY));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(STATE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ArtisanEquipmentBlockEntity(pos, state);
    }

    public MachineType getMachineType() {
        return machineType;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, net.minecraft.block.ShapeContext context) {
        return shape;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean moved) {
        super.onBlockAdded(state, world, pos, oldState, moved);
        // 蜂房需要注册到状态存储，初始累计天数=0
        if (!world.isClient() && machineType == MachineType.BEE_HOUSE) {
            ArtisanEquipmentState stateStore = ArtisanEquipmentState.get((ServerWorld) world);
            stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
                MachineType.BEE_HOUSE.name(), "", 0, "", 0
            ));
        }
        // 太阳能板注册到 SolarPanelState
        if (!world.isClient() && machineType == MachineType.SOLAR_PANEL) {
            SolarPanelState.get((ServerWorld) world).registerPanel(pos);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            ArtisanEquipmentState.get((ServerWorld) world).removeData(pos);
            if (machineType == MachineType.SOLAR_PANEL) {
                SolarPanelState.get((ServerWorld) world).removePanel(pos);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) return ActionResult.SUCCESS;
        if (machineType == null) return ActionResult.PASS;

        ServerWorld serverWorld = (ServerWorld) world;
        MachineState currentState = state.get(STATE);

        if (currentState == MachineState.DONE) {
            return harvestOutput(serverWorld, pos, player, state);
        }

        if (currentState == MachineState.PROCESSING) {
            // 手持仙尘时直接跳过加工时间（不依赖物品交互顺序）
            ItemStack held = player.getMainHandStack();
            if (held.getItem() == net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of(stardewvalley.modid.StardewValley.MOD_ID, "fairy_dust"))) {
                if (!player.isCreative()) held.decrement(1);
                world.setBlockState(pos, state.with(STATE, MachineState.DONE), 3);
                spawnBlueParticles(serverWorld, pos);
                return ActionResult.SUCCESS;
            }
            if (machineType == MachineType.CASK) {
                if (player.isSneaking()) {
                    return cancelCaskAging(serverWorld, pos, player, state);
                }
                player.sendMessage(Text.translatable("message.stardewvalley.cask.processing"), true);
            } else {
                player.sendMessage(Text.translatable("message.stardewvalley.machine.processing"), true);
            }
            return ActionResult.SUCCESS;
        }

        // Empty state - try to insert item
        ItemStack heldStack = player.getMainHandStack();
        if (heldStack.isEmpty()) {
            // 种子生产器支持从副手取物品
            if (machineType == MachineType.SEED_MAKER) {
                heldStack = player.getOffHandStack();
                if (heldStack.isEmpty()) return ActionResult.PASS;
            } else {
                return ActionResult.PASS;
            }
        }

        return startProcessing(serverWorld, pos, player, heldStack, state);
    }

    private ActionResult startProcessing(ServerWorld world, BlockPos pos, PlayerEntity player, ItemStack heldStack, BlockState state) {
        Identifier heldId = Registries.ITEM.getId(heldStack.getItem());
        String idStr = heldId.toString();
        CropQuality quality = MachineType.getQualityFromId(idStr);
        String baseId = idStr;
        int count = heldStack.getCount();

        if (machineType == MachineType.CASK) {
            return startCaskAging(world, pos, player, heldStack, idStr, state);
        }

        RecipeResult recipe = machineType.getRecipe(idStr, quality, count);
        // 特殊机器处理（配方为null的机器）
        if (recipe == null) {
            if (machineType == MachineType.SEED_MAKER) {
                return startSeedMaker(world, pos, player, heldStack, state, idStr);
            } else if (machineType == MachineType.GEODE_CRUSHER) {
                return startGeodeCrusher(world, pos, player, heldStack, state, idStr);
            } else if (machineType == MachineType.RECYCLING_MACHINE) {
                return startRecyclingMachine(world, pos, player, heldStack, state, idStr);
            }
            return ActionResult.PASS;
        }

        String outputId = recipe.outputId();
        int time = recipe.time();
        int outputCount = recipe.outputCount();
        int consumedCount = recipe.consumedCount();

        if (!outputId.contains(":")) {
            outputId = "stardewvalley:" + outputId;
        }

        // Consume input
        heldStack.decrement(consumedCount);

        // 熏鱼机和熔炉额外消耗1个煤炭
        if (machineType == MachineType.FISH_SMOKER || machineType == MachineType.FURNACE) {
            if (!consumeCoal(player)) {
                // 如果没有煤炭，退还物品并取消加工
                heldStack.increment(consumedCount);
                String messageKey = machineType == MachineType.FISH_SMOKER ?
                    "message.stardewvalley.fish_smoker.need_coal" :
                    "message.stardewvalley.furnace.need_coal";
                player.sendMessage(Text.translatable(messageKey), true);
                return ActionResult.PASS;
            }
        }

        // 重型熔炉额外消耗5个煤炭
        if (machineType == MachineType.HEAVY_FURNACE) {
            for (int i = 0; i < 5; i++) {
                if (!consumeCoal(player)) {
                    heldStack.increment(consumedCount);
                    player.sendMessage(Text.translatable("message.stardewvalley.furnace.need_coal"), true);
                    return ActionResult.PASS;
                }
            }
        }

        // Set processing state
        long finishTime = world.getTimeOfDay() + time;
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
            machineType.name(), idStr, finishTime, outputId, outputCount
        ));
        stateStore.setDirty(true);

        // Set BlockEntity data for client rendering
        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems(idStr, outputId);
        }

        world.setBlockState(pos, state.with(STATE, MachineState.PROCESSING), 3);
        world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

        return ActionResult.SUCCESS;
    }

    private ActionResult startCaskAging(ServerWorld world, BlockPos pos, PlayerEntity player, ItemStack heldStack, String idStr, BlockState state) {
        String agedOutput = MachineType.getAgedOutput(idStr);
        if (agedOutput == null) return ActionResult.PASS;

        int agingTime = MachineType.getAgingTime(idStr);

        // Consume input
        heldStack.decrement(1);

        long finishTime = world.getTimeOfDay() + agingTime;
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
            MachineType.CASK.name(), idStr, finishTime, agedOutput, 1
        ));
        stateStore.setDirty(true);

        // Set BlockEntity data for client rendering
        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems(idStr, agedOutput);
        }

        world.setBlockState(pos, state.with(STATE, MachineState.PROCESSING), 3);
        world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

        return ActionResult.SUCCESS;
    }

    private ActionResult cancelCaskAging(ServerWorld world, BlockPos pos, PlayerEntity player, BlockState state) {
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        ArtisanEquipmentState.MachineData data = stateStore.getData(pos);
        if (data == null) return ActionResult.PASS;

        // 取回正在陈酿的输入物品
        String inputId = data.inputItemId();
        if (!inputId.isEmpty()) {
            ItemStack inputStack = createOutputStack(inputId, 1);
            if (!inputStack.isEmpty()) {
                if (!player.getInventory().insertStack(inputStack)) {
                    player.dropItem(inputStack, false);
                }
            }
        }

        // 清空 BlockEntity 渲染数据
        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems("", "");
        }

        stateStore.removeData(pos);
        world.setBlockState(pos, state.with(STATE, MachineState.EMPTY), 3);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 1.0f);

        return ActionResult.SUCCESS;
    }

    // ========= 种子生产器 (SEED_MAKER) =========
    // 作物品名 → 对应种子ID（有特殊映射的在此定义，默认 baseName + "_seeds"）
    private static final java.util.Map<String, String> SEED_MAKER_CROP_MAP = new java.util.HashMap<>();
    private static final java.util.Set<String> SEED_MAKER_ACCEPTED = new java.util.HashSet<>();
    static {
        SEED_MAKER_CROP_MAP.put("grape", "grapestarter");
        SEED_MAKER_CROP_MAP.put("greenbean", "beanstarter");
        SEED_MAKER_CROP_MAP.put("hops", "hopsstarter");
        // 使用 CraftingMaterialSets 已有的 ANY_CROP（含品质变体）
        SEED_MAKER_ACCEPTED.addAll(stardewvalley.modid.crafting.CraftingMaterialSets.ANY_CROP);
        // 排除果树果实（没有对应种子）
        java.util.Set<String> fruitTreeFruits = java.util.Set.of(
            "apple", "apricot", "banana", "cherry", "mango", "orange", "peach", "pomegranate");
        for (String fruit : fruitTreeFruits) {
            SEED_MAKER_ACCEPTED.remove(fruit);
            SEED_MAKER_ACCEPTED.remove(fruit + "_silver");
            SEED_MAKER_ACCEPTED.remove(fruit + "_gold");
            SEED_MAKER_ACCEPTED.remove(fruit + "_iridium");
        }
        // 排除咖啡豆（含品质变体）
        SEED_MAKER_ACCEPTED.remove("coffeebean");
        SEED_MAKER_ACCEPTED.remove("coffeebean_silver");
        SEED_MAKER_ACCEPTED.remove("coffeebean_gold");
        SEED_MAKER_ACCEPTED.remove("coffeebean_iridium");
        // 4种采集物品（映射到季节种子）
        SEED_MAKER_ACCEPTED.add("caiji_wildhorseradish");
        SEED_MAKER_ACCEPTED.add("caiji_spiceberry");
        SEED_MAKER_ACCEPTED.add("caiji_commonmushroom");
        SEED_MAKER_ACCEPTED.add("caiji_winterroot");
    }

    private ActionResult startSeedMaker(ServerWorld world, BlockPos pos, PlayerEntity player, ItemStack heldStack, BlockState state, String idStr) {
        // 检查物品是否在白名单中（ANY_CROP 已含品质变体）
        String pathOnly = idStr;
        if (pathOnly.contains(":")) pathOnly = pathOnly.substring(pathOnly.indexOf(':') + 1);
        if (!SEED_MAKER_ACCEPTED.contains(pathOnly)) {
            return ActionResult.PASS;
        }

        String baseName = MachineType.getBaseCropName(idStr);
        boolean isCaiji = idStr.contains("caiji_");

        // 消耗1个输入物品
        heldStack.decrement(1);

        // 确定输出
        String outputId;
        int outputCount;
        Random random = new Random();

        double roll = random.nextDouble();
        if (roll < 0.005) { // 0.5% 上古种子
            outputId = "stardewvalley:ancientfruit_seeds";
            outputCount = 1;
        } else if (roll < 0.02) { // 1.5% 混合种子
            outputId = "stardewvalley:mixedseeds";
            outputCount = random.nextInt(4) + 1;
        } else { // 97.5% 对应种子
            if (isCaiji) {
                outputId = switch (baseName) {
                    case "wildhorseradish" -> "stardewvalley:spring_seeds";
                    case "spiceberry" -> "stardewvalley:summer_seeds";
                    case "commonmushroom" -> "stardewvalley:fall_seeds";
                    case "winterroot" -> "stardewvalley:winter_seeds";
                    default -> "stardewvalley:" + baseName + "_seeds";
                };
            } else {
                String customSeed = SEED_MAKER_CROP_MAP.get(baseName);
                if (customSeed != null) {
                    outputId = "stardewvalley:" + customSeed;
                } else {
                    outputId = "stardewvalley:" + baseName + "_seeds";
                }
            }
            outputCount = random.nextInt(3) + 1;
        }

        // 设置加工状态
        int finishTime = (int) world.getTimeOfDay() + 400; // 20分钟
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
            MachineType.SEED_MAKER.name(), idStr, finishTime, outputId, outputCount
        ));
        stateStore.setDirty(true);

        // 设置BlockEntity数据
        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems(idStr, outputId);
        }

        world.setBlockState(pos, state.with(STATE, MachineState.PROCESSING), 3);
        world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

        return ActionResult.SUCCESS;
    }

    // ========= 晶球破开器 (GEODE_CRUSHER) =========
    private static final java.util.Set<String> GEODE_TYPES = java.util.Set.of("geode", "frozen_geode", "magma_geode", "omni_geode");

    private ActionResult startGeodeCrusher(ServerWorld world, BlockPos pos, PlayerEntity player, ItemStack heldStack, BlockState state, String idStr) {
        String baseName = MachineType.getBaseCropName(idStr);
        if (!GEODE_TYPES.contains(baseName)) {
            return ActionResult.PASS;
        }

        // 消耗1个输入物品
        heldStack.decrement(1);

        Random random = new Random();
        String outputId;
        int outputCount = 1;

        // 根据晶球类型选择产出表
        switch (baseName) {
            case "geode" -> {
                double roll = random.nextDouble();
                double cum = 0.0;
                // 稀有矿物 (1/32 each)
                if (roll < (cum += 1.0/32)) { outputId = "dwarvish_helm"; }
                else if (roll < (cum += 1.0/16)) { outputId = "earth_crystal"; }
                else if (roll < (cum += 1.0/32)) { outputId = "alamite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "calcite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "celestine"; }
                else if (roll < (cum += 1.0/32)) { outputId = "granite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "jagoite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "jamborite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "limestone"; }
                else if (roll < (cum += 1.0/32)) { outputId = "malachite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "mudstone"; }
                else if (roll < (cum += 1.0/32)) { outputId = "nekoite"; }
                else if (roll < (cum += 1.0/32)) { outputId = "orpiment"; }
                else if (roll < (cum += 1.0/32)) { outputId = "petrified_slime"; }
                else if (roll < (cum += 1.0/32)) { outputId = "sandstone"; }
                else if (roll < (cum += 1.0/32)) { outputId = "slate"; }
                else if (roll < (cum += 1.0/32)) { outputId = "thunder_egg"; }
                else if (roll < (cum += 1.0/8)) { outputId = "misc_stone"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "clay"; outputCount = random.nextDouble() < 0.33 ? getQuantityDistribution(random) : 1; }
                else if (roll < (cum += 1.0/12)) { outputId = "coal"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/12)) { outputId = "copper_ore"; outputCount = getQuantityDistribution(random); }
                else { outputId = "iron_ore"; outputCount = getQuantityDistribution(random); }
            }
            case "frozen_geode" -> {
                double roll = random.nextDouble();
                double cum = 0.0;
                if (roll < (cum += 1.0/30)) { outputId = "ancient_drum"; }
                else if (roll < (cum += 1.0/16)) { outputId = "tear_crystal"; }
                else if (roll < (cum += 1.0/30)) { outputId = "aerinite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "esperite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "fairy_stone"; }
                else if (roll < (cum += 1.0/30)) { outputId = "fluorapatite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "geminite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "ghost_crystal"; }
                else if (roll < (cum += 1.0/30)) { outputId = "hematite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "kyanite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "lunarite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "marble"; }
                else if (roll < (cum += 1.0/30)) { outputId = "ocean_stone"; }
                else if (roll < (cum += 1.0/30)) { outputId = "opal"; }
                else if (roll < (cum += 1.0/30)) { outputId = "pyrite"; }
                else if (roll < (cum += 1.0/30)) { outputId = "soapstone"; }
                else if (roll < (cum += 1.0/8)) { outputId = "misc_stone"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "clay"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "coal"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "copper_ore"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "iron_ore"; outputCount = getQuantityDistribution(random); }
                else { outputId = "gold_ore"; outputCount = getQuantityDistribution(random); }
            }
            case "magma_geode" -> {
                double roll = random.nextDouble();
                double cum = 0.0;
                if (roll < (cum += 1.0/26)) { outputId = "dwarf_gadget"; }
                else if (roll < (cum += 1.0/16)) { outputId = "fire_quartz"; }
                else if (roll < (cum += 1.0/26)) { outputId = "baryte"; }
                else if (roll < (cum += 1.0/26)) { outputId = "basalt"; }
                else if (roll < (cum += 1.0/26)) { outputId = "bixite"; }
                else if (roll < (cum += 1.0/26)) { outputId = "dolomite"; }
                else if (roll < (cum += 1.0/26)) { outputId = "fire_opal"; }
                else if (roll < (cum += 1.0/26)) { outputId = "helvite"; }
                else if (roll < (cum += 1.0/26)) { outputId = "jasper"; }
                else if (roll < (cum += 1.0/26)) { outputId = "lemon_stone"; }
                else if (roll < (cum += 1.0/26)) { outputId = "neptunite"; }
                else if (roll < (cum += 1.0/26)) { outputId = "obsidian"; }
                else if (roll < (cum += 1.0/26)) { outputId = "star_shards"; }
                else if (roll < (cum += 1.0/26)) { outputId = "tigerseye"; }
                else if (roll < (cum += 1.0/8)) { outputId = "misc_stone"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "clay"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "coal"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "copper_ore"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "iron_ore"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "gold_ore"; outputCount = getQuantityDistribution(random); }
                else { outputId = "iridium_ore"; outputCount = getQuantityDistribution(random); }
            }
            case "omni_geode" -> {
                // 万象晶球产出表
                double roll = random.nextDouble();
                double cum = 0.0;
                // 三个神器 (31/2750 each)
                if (roll < (cum += 31.0/2750)) { outputId = "dwarvish_helm"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "dwarf_gadget"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "ancient_drum"; }
                // 三个水晶 (1/48 each)
                else if (roll < (cum += 1.0/48)) { outputId = "earth_crystal"; }
                else if (roll < (cum += 1.0/48)) { outputId = "tear_crystal"; }
                else if (roll < (cum += 1.0/48)) { outputId = "fire_quartz"; }
                // 五彩碎片 (1/250)
                else if (roll < (cum += 1.0/250)) { outputId = "prismatic_shard"; }
                // 41种晶球矿物 (各31/2750)
                else if (roll < (cum += 31.0/2750)) { outputId = "aerinite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "alamite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "bixite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "calcite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "celestine"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "aerinite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "dolomite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "esperite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "fluorapatite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "geminite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "ghost_crystal"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "granite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "jamborite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "hematite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "jagoite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "jasper"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "kyanite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "lemon_stone"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "limestone"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "lunarite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "malachite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "marble"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "mudstone"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "nekoite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "neptunite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "ocean_stone"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "opal"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "orpiment"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "petrified_slime"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "pyrite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "sandstone"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "slate"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "soapstone"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "star_shards"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "thunder_egg"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "tigerseye"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "alamite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "esperite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "obsidian"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "basalt"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "baryte"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "bixite"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "star_shards"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "fire_opal"; }
                else if (roll < (cum += 31.0/2750)) { outputId = "helvite"; }
                // 公共掉落
                else if (roll < (cum += 1.0/8)) { outputId = "misc_stone"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/16)) { outputId = "clay"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "coal"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "copper_ore"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "iron_ore"; outputCount = getQuantityDistribution(random); }
                else if (roll < (cum += 1.0/20)) { outputId = "gold_ore"; outputCount = getQuantityDistribution(random); }
                else { outputId = "iridium_ore"; outputCount = getQuantityDistribution(random); }
            }
            default -> {
                return ActionResult.PASS;
            }
        }

        // 确保所有产出物都有命名空间（星露谷物品默认 stardewvalley:，火把等原版物品自带 minecraft:）
        if (!outputId.contains(":")) {
            outputId = "stardewvalley:" + outputId;
        }

        // 设置加工状态（60分钟 = 1200 ticks）
        int finishTime = (int) world.getTimeOfDay() + 1200;
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
            MachineType.GEODE_CRUSHER.name(), idStr, finishTime, outputId, outputCount
        ));
        stateStore.setDirty(true);

        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems(idStr, outputId);
        }

        world.setBlockState(pos, state.with(STATE, MachineState.PROCESSING), 3);
        world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

        return ActionResult.SUCCESS;
    }

    // ========= 回收机 (RECYCLING_MACHINE) =========
    private ActionResult startRecyclingMachine(ServerWorld world, BlockPos pos, PlayerEntity player, ItemStack heldStack, BlockState state, String idStr) {
        String baseName = MachineType.getBaseCropName(idStr);
        Random random = new Random();
        String outputId;
        int outputCount;

        switch (baseName) {
            case "trash_item" -> {
                double roll = random.nextDouble();
                outputId = roll < 0.49 ? "misc_stone" : roll < 0.79 ? "coal" : "iron_ore";
                outputCount = random.nextInt(3) + 1;
            }
            case "driftwood" -> {
                outputId = random.nextDouble() < 0.75 ? "wood" : "coal";
                outputCount = random.nextInt(3) + 1;
            }
            case "soggy_newspaper" -> {
                if (random.nextDouble() < 0.9) {
                    outputId = "minecraft:torch";
                    outputCount = 3;
                } else {
                    outputId = "cloth";
                    outputCount = 1;
                }
            }
            case "broken_cd" -> {
                outputId = "refined_quartz";
                outputCount = 1;
            }
            case "broken_glasses" -> {
                outputId = "refined_quartz";
                outputCount = 1;
            }
            default -> {
                return ActionResult.PASS;
            }
        }

        // 确保所有产出物都有命名空间
        if (!outputId.contains(":")) {
            outputId = "stardewvalley:" + outputId;
        }

        // 消耗1个输入物品
        heldStack.decrement(1);

        // 设置加工状态（60分钟 = 1200 ticks）
        int finishTime = (int) world.getTimeOfDay() + 1200;
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
            MachineType.RECYCLING_MACHINE.name(), idStr, finishTime, outputId, outputCount
        ));
        stateStore.setDirty(true);

        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems(idStr, outputId);
        }

        world.setBlockState(pos, state.with(STATE, MachineState.PROCESSING), 3);
        world.playSound(null, pos, SoundEvents.BLOCK_WOODEN_TRAPDOOR_OPEN, SoundCategory.BLOCKS, 1.0f, 1.0f);

        return ActionResult.SUCCESS;
    }

    /**
     * 数量分布：30% 1个, 30% 3个, 30% 5个, 9% 10个, 1% 20个
     */
    private static int getQuantityDistribution(Random random) {
        double roll = random.nextDouble();
        if (roll < 0.30) return 1;
        if (roll < 0.60) return 3;
        if (roll < 0.90) return 5;
        if (roll < 0.99) return 10;
        return 20;
    }

    private static ArtisanEquipmentBlockEntity getBlockEntity(ServerWorld world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return be instanceof ArtisanEquipmentBlockEntity ? (ArtisanEquipmentBlockEntity) be : null;
    }

    private ActionResult harvestOutput(ServerWorld world, BlockPos pos, PlayerEntity player, BlockState state) {
        ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(world);
        ArtisanEquipmentState.MachineData data = stateStore.getData(pos);

        if (machineType == MachineType.BEE_HOUSE) {
            // 蜂房：收获时重新检测花朵，构造物品（完全仿照采集器的方式）
            String honeyType = ArtisanEquipmentState.findNearbyFlowerHoney(world, pos);
            String honeyId = honeyType != null ? honeyType : "honey";
            ItemStack honeyStack = new ItemStack(
                Registries.ITEM.get(Identifier.of(stardewvalley.modid.StardewValley.MOD_ID, honeyId)),
                1
            );
            if (!honeyStack.isEmpty()) {
                if (!player.getInventory().insertStack(honeyStack)) {
                    player.dropItem(honeyStack, false);
                }
            }

            // 清空 BlockEntity 渲染
            ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
            if (be != null) {
                be.setItems("", "");
            }

            // 保留累计天数（finishTime=accumulatedDays），状态恢复为空闲
            int accumulatedDays = data != null ? (int) data.finishTime() : 0;
            stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
                MachineType.BEE_HOUSE.name(), "", accumulatedDays, "", 0
            ));
            world.setBlockState(pos, state.with(STATE, MachineState.EMPTY), 3);
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 1.0f);
            return ActionResult.SUCCESS;
        }

        // 非蜂房设备：正常从存储数据读取产出
        if (data == null) {
            world.setBlockState(pos, state.with(STATE, MachineState.EMPTY), 3);
            return ActionResult.SUCCESS;
        }

        String outputId = data.outputItemId();
        int outputCount = data.outputCount();
        ItemStack outputStack = createOutputStack(outputId, outputCount);

        if (outputStack.isEmpty()) {
            world.setBlockState(pos, state.with(STATE, MachineState.EMPTY), 3);
            stateStore.removeData(pos);
            return ActionResult.SUCCESS;
        }

        if (!player.getInventory().insertStack(outputStack)) {
            player.dropItem(outputStack, false);
        }

        // 结晶器：收获后自动用相同宝石开始新一轮复制
        if (machineType == MachineType.CRYSTALARIUM) {
            String inputId = data.inputItemId();
            if (!inputId.isEmpty()) {
                RecipeResult recipe = MachineType.CRYSTALARIUM.getRecipe(inputId, CropQuality.NORMAL, 1);
                if (recipe != null) {
                    long newFinishTime = world.getTimeOfDay() + recipe.time();
                    stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
                        MachineType.CRYSTALARIUM.name(), inputId, newFinishTime, inputId, 1
                    ));
                    ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
                    if (be != null) {
                        be.setItems(inputId, inputId);
                    }
                    world.setBlockState(pos, state.with(STATE, MachineState.PROCESSING), 3);
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 1.0f);
                    return ActionResult.SUCCESS;
                }
            }
        }

        // 清空 BlockEntity 数据，客户端渲染器会停止渲染物品
        ArtisanEquipmentBlockEntity be = getBlockEntity(world, pos);
        if (be != null) {
            be.setItems("", "");
        }

        stateStore.removeData(pos);

        world.setBlockState(pos, state.with(STATE, MachineState.EMPTY), 3);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f, 1.0f);

        return ActionResult.SUCCESS;
    }

    private ItemStack createOutputStack(String outputId, int count) {
        // 尝试解析完整ID（如 "stardewvalley:honey"）
        Identifier outId = Identifier.tryParse(outputId);
        if (outId == null) {
            // 如果没有命名空间，用 mod id 补上
            outId = Identifier.of(stardewvalley.modid.StardewValley.MOD_ID, outputId);
        }
        net.minecraft.item.Item item = Registries.ITEM.get(outId);
        if (item == null || item == net.minecraft.item.Items.AIR) {
            stardewvalley.modid.StardewValley.LOGGER.warn("ArtisanEquipment: cannot find item for outputId={}, parsedId={}", outputId, outId);
            return ItemStack.EMPTY;
        }
        return new ItemStack(item, count);
    }

    private static boolean consumeCoal(PlayerEntity player) {
        net.minecraft.util.Identifier coalId = net.minecraft.util.Identifier.of(stardewvalley.modid.StardewValley.MOD_ID, "coal");
        net.minecraft.item.Item coal = net.minecraft.registry.Registries.ITEM.get(coalId);
        if (coal == null || coal == net.minecraft.item.Items.AIR) return false;
        for (int i = 0; i < player.getInventory().size(); i++) {
            ItemStack stack = player.getInventory().getStack(i);
            if (stack.getItem() == coal && stack.getCount() > 0) {
                stack.decrement(1);
                return true;
            }
        }
        return false;
    }

    private static void spawnBlueParticles(ServerWorld world, BlockPos pos) {
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            20, 0.5, 0.5, 0.5, 0.1);
    }

}

