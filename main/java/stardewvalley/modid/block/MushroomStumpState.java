package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.season.ForagingLevelManager;
import stardewvalley.modid.util.SafeCodec;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

import java.util.*;
import java.util.stream.Collectors;

public class MushroomStumpState extends PersistentState {
    private static final String NAME = "stardewvalley_mushroom_stump";
    private final Map<BlockPos, StumpData> stumpDataMap = new HashMap<>();

    public record StumpData(int accumulatedDays, String outputItemId, int outputCount) {}

    private record Entry(long pos, int accumulatedDays, String outputItemId, int outputCount) {}

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
            Codec.INT.fieldOf("accumulatedDays").forGetter(Entry::accumulatedDays),
            Codec.STRING.fieldOf("outputItemId").forGetter(Entry::outputItemId),
            Codec.INT.fieldOf("outputCount").forGetter(Entry::outputCount)
        ).apply(instance, Entry::new)
    );

    private static final Codec<List<Entry>> LIST_CODEC = ENTRY_CODEC.listOf();

    public static final Codec<MushroomStumpState> CODEC = LIST_CODEC.xmap(
        list -> {
            MushroomStumpState state = new MushroomStumpState();
            for (Entry e : list) {
                state.stumpDataMap.put(BlockPos.fromLong(e.pos()), new StumpData(e.accumulatedDays(), e.outputItemId(), e.outputCount()));
            }
            return state;
        },
        state -> state.stumpDataMap.entrySet().stream()
            .map(e -> new Entry(e.getKey().asLong(), e.getValue().accumulatedDays(), e.getValue().outputItemId(), e.getValue().outputCount()))
            .toList()
    );

    public static final PersistentStateType<MushroomStumpState> TYPE = new PersistentStateType<>(
        NAME, MushroomStumpState::new, SafeCodec.wrap(CODEC, MushroomStumpState::new), DataFixTypes.LEVEL
    );

    public static MushroomStumpState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void registerStump(BlockPos pos) {
        stumpDataMap.put(pos.toImmutable(), new StumpData(0, "", 0));
        setDirty(true);
    }

    public StumpData getData(BlockPos pos) {
        return stumpDataMap.get(pos);
    }

    public void removeData(BlockPos pos) {
        stumpDataMap.remove(pos);
        setDirty(true);
    }

    public void setData(BlockPos pos, StumpData data) {
        stumpDataMap.put(pos.toImmutable(), data);
        setDirty(true);
    }

    public Map<BlockPos, StumpData> getAllData() {
        return stumpDataMap;
    }

    public void applyFairyDust(ServerWorld world, BlockPos pos) {
        StumpData data = stumpDataMap.get(pos);
        if (data == null) return;
        int newDays = data.accumulatedDays() + 4;
        produce(world, pos, data, newDays);
    }

    public void tickMushroomStumps(ServerWorld world) {
        var iterator = stumpDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BlockPos pos = entry.getKey();
            StumpData data = entry.getValue();

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }

            BlockState blockState = world.getBlockState(pos);
            if (!(blockState.getBlock() instanceof MushroomStumpBlock)) {
                iterator.remove();
                setDirty(true);
                continue;
            }

            // 已有产出则跳过
            if (blockState.get(MushroomStumpBlock.DONE)) continue;

            // 计算当天进度增加
            boolean isRaining = isRainyDay(world);
            int gain = isRaining ? 2 : 1;
            int newDays = data.accumulatedDays() + gain;

            if (newDays >= 4) {
                // 产出
                produce(world, pos, data, newDays);
            } else {
                // 仅累加
                stumpDataMap.put(pos.toImmutable(), new StumpData(newDays, "", 0));
                setDirty(true);
            }
        }
    }

    private void produce(ServerWorld world, BlockPos pos, StumpData data, int newDays) {
        int remainder = newDays - 4;

        // 统计7x7范围内的树
        List<BlockPos> treeLogs = scanTreeLogs(world, pos);
        int treeCount = treeLogs.size();

        // 计算数量
        int quantity = Math.max(1, treeCount / 2);
        if (world.random.nextFloat() < 0.5f) {
            quantity *= 2;
        }
        quantity = Math.min(quantity, 5);

        // 计算品质
        int quality = 0;
        float qualityChance = treeCount / 40.0f;
        while (quality < 3 && world.random.nextFloat() < qualityChance) {
            quality++;
        }

        // 选择蘑菇，附品质后缀
        String baseMushroomId = selectMushroom(world, pos, treeLogs);
        String suffix = switch (quality) {
            case 1 -> "_silver";
            case 2 -> "_gold";
            case 3 -> "_iridium";
            default -> "";
        };
        String outputItemId = baseMushroomId + suffix;

        // 先设置方块状态（这会重新创建BlockEntity）
        world.setBlockState(pos, world.getBlockState(pos).with(MushroomStumpBlock.DONE, true), 3);

        // 再保存产出数据到PersistentState
        stumpDataMap.put(pos.toImmutable(), new StumpData(remainder, outputItemId, quantity));
        setDirty(true);

        // 最后设置渲染用的BlockEntity数据（需要在setBlockState之后，因为setBlockState可能重建BlockEntity）
        if (world.getBlockEntity(pos) instanceof MushroomStumpBlockEntity be) {
            be.setOutputItemId("stardewvalley:" + outputItemId);
        }
    }

    private static boolean isRainyDay(ServerWorld world) {
        WeatherType weather = WeatherState.get(world).todayWeather;
        return weather == WeatherType.RAIN || weather == WeatherType.STORM || weather == WeatherType.GREEN_RAIN;
    }

    private static List<BlockPos> scanTreeLogs(ServerWorld world, BlockPos center) {
        List<BlockPos> logs = new ArrayList<>();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                for (int dy = -3; dy <= 3; dy++) {
                    BlockPos p = center.add(dx, dy, dz);
                    if (isTreeLog(world.getBlockState(p).getBlock())) {
                        logs.add(p);
                    }
                }
            }
        }
        return logs;
    }

    private static boolean isTreeLog(net.minecraft.block.Block block) {
        return block == ModBlocks.PLANTED_OAK || block == ModBlocks.PLANTED_MAPLE
            || block == ModBlocks.PLANTED_PINE || block == ModBlocks.PLANTED_MAHOGANY;
    }

    private static String selectMushroom(ServerWorld world, BlockPos pos, List<BlockPos> treeLogs) {
        List<String> mushroomPool = new ArrayList<>();
        int treeCount = treeLogs.size();

        // 基础分化列表
        int n = Math.max(1, (int) Math.floor(treeCount * 3.0 / 4.0));
        for (int i = 0; i < n; i++) {
            mushroomPool.add(rollBasicMushroom(world.random));
        }

        // 成熟特定列表
        for (BlockPos logPos : treeLogs) {
            String type = getTreeType(world.getBlockState(logPos).getBlock());
            switch (type) {
                case "oak" -> mushroomPool.add("caiji_morel");
                case "maple" -> {
                    if (world.random.nextFloat() < 0.1f) {
                        mushroomPool.add("caiji_purplemushroom");
                    } else {
                        mushroomPool.add("caiji_redmushroom");
                    }
                }
                case "pine" -> mushroomPool.add("caiji_chanterelle");
                default -> mushroomPool.add(rollBasicMushroom(world.random));
            }
        }

        return mushroomPool.get(world.random.nextInt(mushroomPool.size()));
    }

    private static String rollBasicMushroom(net.minecraft.util.math.random.Random random) {
        float roll = random.nextFloat();
        if (roll < 0.05f) {
            return "caiji_purplemushroom";
        } else if (roll < 0.05f + 0.1425f) {
            return "caiji_redmushroom";
        } else {
            return "caiji_commonmushroom";
        }
    }

    private static String getTreeType(net.minecraft.block.Block block) {
        if (block == ModBlocks.PLANTED_OAK) return "oak";
        if (block == ModBlocks.PLANTED_MAPLE) return "maple";
        if (block == ModBlocks.PLANTED_PINE) return "pine";
        return "other";
    }
}
