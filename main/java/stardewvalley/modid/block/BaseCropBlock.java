package stardewvalley.modid.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import stardewvalley.modid.block.ModFarmlandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.crop.CropConfig;
import stardewvalley.modid.crop.CropQuality;
import stardewvalley.modid.crop.QualityCalculator;
import stardewvalley.modid.season.FarmingLevelManager;
import stardewvalley.modid.season.Season;

import stardewvalley.modid.item.CropItem;
import stardewvalley.modid.skill.SkillEffectHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BaseCropBlock extends Block implements Fertilizable, GrowableCrop {

    public static final IntProperty AGE = IntProperty.of("age", 0, 35);
    public static final IntProperty GROWTH_ACCUM = IntProperty.of("growth_accum", 0, 99);

    public static final Map<RegistryKey<World>, Set<BlockPos>> CROP_POSITIONS = new ConcurrentHashMap<>();

    private final CropConfig config;
    private boolean creativeBreaking;
    private UUID lastPlayerUuid;
    private final Set<Season> seasonSet;

    public BaseCropBlock(Settings settings, CropConfig config) {
        super(settings);
        this.config = config;
        this.seasonSet = new HashSet<>(Arrays.asList(config.seasons()));
        setDefaultState(getStateManager().getDefaultState()
            .with(AGE, 0)
            .with(GROWTH_ACCUM, 0));
    }

    public CropConfig getConfig() {
        return config;
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE, GROWTH_ACCUM);
    }

    public boolean isMature(BlockState state) {
        return config.regrowCycle() > 0
            ? state.get(AGE) == config.growthAge()
            : state.get(AGE) >= config.maxAge();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return config.groundTest().test(world.getBlockState(pos.down()));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction == Direction.DOWN && !config.groundTest().test(neighborState)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!world.isClient() && !state.isOf(oldState.getBlock())) {
            BlockPos immutable = pos.toImmutable();
            CROP_POSITIONS.computeIfAbsent(world.getRegistryKey(), k -> ConcurrentHashMap.newKeySet())
                .add(immutable);
            if (world instanceof ServerWorld serverWorld) {
                CropPositionState.get(serverWorld).addCropPosition(immutable);
            }
        }
    }

    public void onNewDay(ServerWorld world, BlockPos pos, BlockState state) {
        BlockState below = world.getBlockState(pos.down());
        boolean isAllSeason = below.getBlock() instanceof ModFarmlandBlock farmland && farmland.allowsAllSeasons();
        if (!isAllSeason && !seasonSet.contains(Season.getCachedSeason())) {
            world.breakBlock(pos, true);
            return;
        }
        tryGrow(world, pos, state);
    }

    private int getFertilizerTier(World world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        if (below.getBlock() instanceof ModFarmlandBlock) {
            return below.get(ModFarmlandBlock.FERTILIZER_TIER);
        }
        return 0;
    }

    private float getGrowthBoost(World world, BlockPos pos) {
        BlockState below = world.getBlockState(pos.down());
        if (below.getBlock() instanceof ModFarmlandBlock) {
            int boost = below.get(ModFarmlandBlock.GROWTH_BOOST);
            return switch (boost) {
                case 1 -> 0.10f;
                case 2 -> 0.25f;
                case 3 -> 0.40f;
                default -> 0;
            };
        }
        return 0;
    }

    private ItemStack makeQualityStack(ServerWorld world, UUID playerUuid, BlockPos pos) {
        int farmingLevel = playerUuid != null
            ? FarmingLevelManager.get(world).getLevel(playerUuid)
            : 0;
        ServerPlayerEntity player = playerUuid != null && world.getServer() != null
            ? world.getServer().getPlayerManager().getPlayer(playerUuid)
            : null;
        if (player != null) {
            farmingLevel = stardewvalley.modid.effect.SkillBuffHelper.getEffectiveFarmingLevel(world, playerUuid, player);
        }
        int fertilizerTier = getFertilizerTier(world, pos);
        float luckMult = player != null
            ? StardewValley.getFinalLuckMultiplier(world, player)
            : StardewValley.getLuckMultiplier(world);
        CropQuality quality = QualityCalculator.rollQuality(world.random, farmingLevel, fertilizerTier, luckMult);
        String qualityItemName = config.cropId() + quality.getSuffix();
        Identifier id = Identifier.of(StardewValley.MOD_ID, qualityItemName);
        Item item = Registries.ITEM.get(id);
        return new ItemStack(item != null ? item : Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, config.cropId())));
    }

    private void awardHarvestXp(ServerWorld world, UUID playerUuid, ItemStack stack) {
        if (playerUuid == null) return;
        if (stack.getItem() instanceof CropItem cropItem) {
            int price = cropItem.getMoneyValue();
            int xp = (int) Math.round(16.0 * Math.log(0.018 * price + 1.0));
            if (xp > 0) {
                FarmingLevelManager.get(world).addXp(playerUuid, xp);
            }
        }
    }

    @Override
    public void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean movedByPiston) {
        boolean wasCreative = creativeBreaking;
        UUID uuid = lastPlayerUuid;
        creativeBreaking = false;
        lastPlayerUuid = null;

        if (!world.getBlockState(pos).isOf(this)) {
            Set<BlockPos> positions = CROP_POSITIONS.get(world.getRegistryKey());
            if (positions != null) {
                positions.remove(pos);
            }
            CropPositionState.get(world).removeCropPosition(pos);
        }

        if (!movedByPiston && isMature(state) && !world.getBlockState(pos).isOf(this) && !wasCreative) {
            for (int i = 0; i < config.harvestDropCount(); i++) {
                ItemStack mainDrop = makeQualityStack(world, uuid, pos);
                awardHarvestXp(world, uuid, mainDrop);
                dropStack(world, pos, mainDrop);

                float luckMult = uuid != null && world.getServer() != null && world.getServer().getPlayerManager().getPlayer(uuid) instanceof ServerPlayerEntity sp
                    ? StardewValley.getFinalLuckMultiplier(world, sp)
                    : StardewValley.getLuckMultiplier(world);
                if (config.extraDropChance() > 0 && world.random.nextFloat() < config.extraDropChance() * luckMult) {
                    dropStack(world, pos, new ItemStack(Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, config.cropId()))));
                }
            }
            for (int i = 0; i < config.harvestSeedDrop(); i++) {
                String seedName = config.cropId() + "_seeds";
                Item seedItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, seedName));
                if (seedItem != null) {
                    dropStack(world, pos, new ItemStack(seedItem));
                }
            }
        }
        super.onStateReplaced(state, world, pos, movedByPiston);
    }

    @Override
    public void afterBreak(World world, PlayerEntity player, BlockPos pos, BlockState state, net.minecraft.block.entity.BlockEntity blockEntity, ItemStack stack) {
        creativeBreaking = player.isCreative();
        lastPlayerUuid = player.getUuid();
        super.afterBreak(world, player, pos, state, blockEntity, stack);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (config.regrowCycle() > 0 && isMature(state)) {
            if (!world.isClient()) {
                ServerWorld serverWorld = (ServerWorld) world;
                for (int i = 0; i < config.harvestDropCount(); i++) {
                    ItemStack mainDrop = makeQualityStack(serverWorld, player.getUuid(), pos);
                    awardHarvestXp(serverWorld, player.getUuid(), mainDrop);
                    dropStack(world, pos, mainDrop);

                    if (config.extraDropChance() > 0 && world.random.nextFloat() < config.extraDropChance() * StardewValley.getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player)) {
                        dropStack(world, pos, new ItemStack(Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, config.cropId()))));
                    }
                }

                world.setBlockState(pos, state.with(AGE, config.maxAge()), 2);
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    public void tryGrow(ServerWorld world, BlockPos pos, BlockState state) {
        BlockState below = world.getBlockState(pos.down());
        if (!config.groundTest().test(below)) return;

        if (config.requiresWater()) {
            boolean hasWater;
            if (below.getBlock() instanceof ModFarmlandBlock) {
                int retainDays = below.get(ModFarmlandBlock.RETAIN_DAYS);
                int moisture = below.get(FarmlandBlock.MOISTURE);
                hasWater = retainDays > 0 || moisture > 0;
            } else {
                hasWater = below.getBlock() instanceof FarmlandBlock
                    && below.get(FarmlandBlock.MOISTURE) > 0;
            }
            if (!hasWater) return;
        }

        int age = state.get(AGE);
        int accum = state.get(GROWTH_ACCUM);
        float rate = 1.0f + getGrowthBoost(world, pos);

        // 农业学家：仅对该作物种植者生效，生长速度 +10%（乘法，与化肥叠加）
        UUID planterUuid = CropPositionState.get(world).getPlanter(pos);
        if (planterUuid != null && SkillEffectHelper.hasAgriculturist(world, planterUuid)) {
            rate *= 1.10f;
        }

        if (config.regrowCycle() > 0) {
            if (age < config.growthAge()) {
                accum += (int)(100 * rate);
                while (accum >= 100 && age < config.growthAge()) {
                    age++;
                    accum -= 100;
                }
                world.setBlockState(pos, state.with(AGE, age).with(GROWTH_ACCUM, Math.min(accum, 99)), 2);
            } else if (age > config.growthAge()) {
                accum -= (int)(100 * rate);
                while (accum < 0 && age > config.growthAge()) {
                    age--;
                    accum += 100;
                }
                world.setBlockState(pos, state.with(AGE, age).with(GROWTH_ACCUM, Math.max(accum, 0)), 2);
            }
        } else {
            if (age < config.maxAge()) {
                accum += (int)(100 * rate);
                while (accum >= 100 && age < config.maxAge()) {
                    age++;
                    accum -= 100;
                }
                world.setBlockState(pos, state.with(AGE, age).with(GROWTH_ACCUM, Math.min(accum, 99)), 2);
            }
        }
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return config.regrowCycle() > 0
            ? state.get(AGE) != config.growthAge()
            : state.get(AGE) < config.maxAge();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return isFertilizable(world, pos, state);
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        int age = state.get(AGE);
        if (config.regrowCycle() > 0) {
            if (age < config.growthAge()) {
                world.setBlockState(pos, state.with(AGE, age + 1).with(GROWTH_ACCUM, 0), 2);
            } else if (age > config.growthAge()) {
                world.setBlockState(pos, state.with(AGE, age - 1).with(GROWTH_ACCUM, 0), 2);
            }
        } else {
            if (age < config.maxAge()) {
                world.setBlockState(pos, state.with(AGE, age + 1).with(GROWTH_ACCUM, 0), 2);
            }
        }
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return config.shapes()[state.get(AGE)];
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos) {
        return 1.0f;
    }
}