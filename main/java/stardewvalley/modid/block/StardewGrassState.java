package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.season.LuckManager;
import stardewvalley.modid.util.SafeCodec;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StardewGrassState extends PersistentState {
    private static final String NAME = "stardewvalley_grass";
    private static final int MAX_GRASS_COUNT = 1000;

    private final Set<BlockPos> grassPositions = new HashSet<>();
    private int totalGrassCount = 0;

    private record Data(List<Long> positions, int totalCount) {}

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.listOf().fieldOf("positions").forGetter(Data::positions),
            Codec.INT.optionalFieldOf("totalCount", 0).forGetter(Data::totalCount)
        ).apply(instance, Data::new)
    );

    public static final Codec<StardewGrassState> CODEC = DATA_CODEC.xmap(
        data -> {
            StardewGrassState state = new StardewGrassState();
            for (Long l : data.positions()) {
                state.grassPositions.add(BlockPos.fromLong(l));
            }
            state.totalGrassCount = data.totalCount();
            return state;
        },
        state -> new Data(state.grassPositions.stream().map(BlockPos::asLong).toList(), state.totalGrassCount)
    );

    public static final PersistentStateType<StardewGrassState> TYPE = new PersistentStateType<>(
        NAME, StardewGrassState::new, SafeCodec.wrap(CODEC, StardewGrassState::new), DataFixTypes.LEVEL
    );

    public static StardewGrassState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void addPos(BlockPos pos) {
        if (grassPositions.add(pos.toImmutable())) {
            setDirty(true);
        }
    }

    public void removePos(BlockPos pos) {
        if (grassPositions.remove(pos)) {
            setDirty(true);
        }
    }

    public boolean hasPos(BlockPos pos) {
        return grassPositions.contains(pos);
    }

    public Set<BlockPos> getPositions() {
        return grassPositions;
    }

    public int getTotalCount() {
        return totalGrassCount;
    }

    public void incrementTotalCount() {
        totalGrassCount++;
        setDirty(true);
    }

    public void incrementTotalCount(int amount) {
        totalGrassCount += amount;
        setDirty(true);
    }

    public void decrementTotalCount() {
        if (totalGrassCount > 0) {
            totalGrassCount--;
            setDirty(true);
        }
    }

    public void setTotalCount(int count) {
        totalGrassCount = count;
        setDirty(true);
    }

    /**
     * 每天 time=12 调用：遍历所有特殊草，扩散到周围12个位置
     */
    public void tickGrassSpread(ServerWorld world, long currentDay) {
        List<BlockPos> positionsCopy = List.copyOf(grassPositions);

        // 统计今天自然死亡和自然新增的草数量
        int removedToday = 0;
        int addedToday = 0;

        // Step 1: 遍历所有已记录的草位置，移除已被破坏的草
        for (BlockPos pos : positionsCopy) {
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }

            if (!(world.getBlockState(pos).getBlock() instanceof StardewGrassBlock)) {
                grassPositions.remove(pos);
                setDirty(true);
                removedToday++;
            }
        }

        // 更新总计数：减去今天消失的草
        if (removedToday > 0) {
            totalGrassCount -= removedToday;
            if (totalGrassCount < 0) totalGrassCount = 0;
            setDirty(true);
        }

        // 若总数已达上限，跳过扩散
        if (totalGrassCount >= MAX_GRASS_COUNT) {
            return;
        }

        // Step 2: 获取当天天气和每日运气，计算扩散概率
        WeatherType todayWeather = WeatherState.get(world).todayWeather;
        float dailyLuck = LuckManager.get(world).getDailyLuck(currentDay);
        float luckMult = 1.0f + dailyLuck;
        float baseChance;

        switch (todayWeather) {
            case RAIN -> baseChance = 0.25f;
            case WIND_SPRING, WIND_FALL -> baseChance = 0.20f;
            case STORM -> baseChance = 0.25f;
            case GREEN_RAIN -> baseChance = 0.33f;
            default -> baseChance = 0.18f; // SUN, SNOW, FESTIVAL
        }

        float spreadChance = baseChance * luckMult;

        // Step 3: 扩散
        for (BlockPos pos : positionsCopy) {
            // 跳过已被移除的草
            if (!grassPositions.contains(pos)) continue;

            // 12个扩散候选位置
            BlockPos[] candidates = {
                pos.add(1, 0, 0), pos.add(-1, 0, 0), pos.add(0, 0, 1), pos.add(0, 0, -1),
                pos.add(1, 1, 0), pos.add(-1, 1, 0), pos.add(0, 1, 1), pos.add(0, 1, -1),
                pos.add(1, -1, 0), pos.add(-1, -1, 0), pos.add(0, -1, 1), pos.add(0, -1, -1)
            };

            for (BlockPos candidate : candidates) {
                // 使用当天天气×运气对应的扩散概率
                if (world.random.nextFloat() >= spreadChance) continue;

                // 加载候选位置所在区块
                int cChunkX = candidate.getX() >> 4;
                int cChunkZ = candidate.getZ() >> 4;
                if (!world.isChunkLoaded(cChunkX, cChunkZ)) {
                    world.getChunk(cChunkX, cChunkZ);
                }

                // 检查目标位置能否长草：必须是空气，下方方块必须是草方块或泥土
                if (!world.isAir(candidate)) continue;
                BlockState belowState = world.getBlockState(candidate.down());
                if (belowState.getBlock() != net.minecraft.block.Blocks.GRASS_BLOCK &&
                    belowState.getBlock() != net.minecraft.block.Blocks.DIRT) continue;

                // 检查是否已有特殊草
                if (grassPositions.contains(candidate)) continue;

                // 生长出特殊草
                world.setBlockState(candidate, ModBlocks.STARDEW_GRASS.getDefaultState(), 3);
                grassPositions.add(candidate.toImmutable());
                setDirty(true);
                addedToday++;
            }
        }

        // 更新总计数：加上今天新增的草
        if (addedToday > 0) {
            totalGrassCount += addedToday;
            setDirty(true);
        }
    }
}
