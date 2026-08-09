package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ArtisanEquipmentState extends PersistentState {
    private static final String NAME = "stardewvalley_artisan_equipment";
    private final Map<BlockPos, MachineData> machineDataMap = new HashMap<>();
    private final Set<BlockPos> machinePositions = new HashSet<>();

    public record MachineData(String machineType, String inputItemId, long finishTime, String outputItemId, int outputCount) {}

    private record Data(List<Entry> entries, List<Long> positions) {}
    private record Entry(long pos, String machineType, String inputItemId, long finishTime, String outputItemId, int outputCount) {}

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
            Codec.STRING.fieldOf("machineType").forGetter(Entry::machineType),
            Codec.STRING.fieldOf("inputItemId").forGetter(Entry::inputItemId),
            Codec.LONG.fieldOf("finishTime").forGetter(Entry::finishTime),
            Codec.STRING.fieldOf("outputItemId").forGetter(Entry::outputItemId),
            Codec.INT.fieldOf("outputCount").forGetter(Entry::outputCount)
        ).apply(instance, Entry::new)
    );

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ENTRY_CODEC.listOf().fieldOf("entries").forGetter(Data::entries),
            Codec.LONG.listOf().fieldOf("positions").forGetter(Data::positions)
        ).apply(instance, Data::new)
    );

    public static final Codec<ArtisanEquipmentState> CODEC = DATA_CODEC.xmap(
        data -> {
            ArtisanEquipmentState state = new ArtisanEquipmentState();
            for (Entry e : data.entries()) {
                state.machineDataMap.put(BlockPos.fromLong(e.pos()), new MachineData(
                    e.machineType(), e.inputItemId(), e.finishTime(), e.outputItemId(), e.outputCount()
                ));
            }
            for (Long l : data.positions()) {
                state.machinePositions.add(BlockPos.fromLong(l));
            }
            return state;
        },
        state -> {
            List<Entry> entries = state.machineDataMap.entrySet().stream()
                .map(e -> new Entry(e.getKey().asLong(), e.getValue().machineType(),
                    e.getValue().inputItemId(), e.getValue().finishTime(), e.getValue().outputItemId(), e.getValue().outputCount()))
                .toList();
            List<Long> positions = state.machinePositions.stream().map(BlockPos::asLong).toList();
            return new Data(entries, positions);
        }
    );

    public static final PersistentStateType<ArtisanEquipmentState> TYPE = new PersistentStateType<>(
        NAME,
        ArtisanEquipmentState::new,
        SafeCodec.wrap(CODEC, ArtisanEquipmentState::new),
        DataFixTypes.LEVEL
    );

    public static ArtisanEquipmentState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public MachineData getData(BlockPos pos) {
        return machineDataMap.get(pos);
    }

    public void setData(BlockPos pos, MachineData data) {
        machineDataMap.put(pos, data);
        machinePositions.add(pos.toImmutable());
        setDirty(true);
    }

    public void removeData(BlockPos pos) {
        machineDataMap.remove(pos);
        machinePositions.remove(pos);
        setDirty(true);
    }

    public Set<BlockPos> getMachinePositions() {
        return machinePositions;
    }

    public boolean hasData(BlockPos pos) {
        return machineDataMap.containsKey(pos);
    }

    /**
     * 将设备位置注册到 machinePositions，供 tickBeeHouses 等遍历使用。
     * 适用于不需要玩家手动放入原料的设备（如蜂房）。
     */
    public void registerMachine(BlockPos pos) {
        machinePositions.add(pos.toImmutable());
        setDirty(true);
    }

    /**
     * 每tick调用：遍历所有设备的 finishTime，纯数值比较。
     * 不需要设备所在区块加载即可减少剩余时间。
     * 当 finishTime 到达时，强制加载区块1tick来更新方块状态。
     * 注意：蜂房使用累加器模式（finishTime存accumulatedDays），不由 tick 处理。
     */
    public void tick(ServerWorld world) {
        long currentTime = world.getTimeOfDay();
        var iterator = machineDataMap.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            BlockPos pos = entry.getKey();
            MachineData data = entry.getValue();

            // 蜂房和太阳能板使用累加器模式，由专门的每日逻辑处理，跳过
            if (MachineType.BEE_HOUSE.name().equals(data.machineType())) continue;
            if (MachineType.SOLAR_PANEL.name().equals(data.machineType())) continue;

            // 时间未到，跳过（纯数值比较，不依赖区块）
            if (currentTime < data.finishTime()) continue;

            // 时间到了！强制加载区块1tick来更新状态
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }

            BlockState blockState = world.getBlockState(pos);
            if (blockState.getBlock() instanceof ArtisanEquipmentBlock) {
                // 只将 PROCESSING 转为 DONE，保护 EMPTY 状态不被覆盖
                if (blockState.get(ArtisanEquipmentBlock.STATE) == ArtisanEquipmentBlock.MachineState.PROCESSING) {
                    world.setBlockState(pos, blockState.with(ArtisanEquipmentBlock.STATE, ArtisanEquipmentBlock.MachineState.DONE), 3);
                }
            } else {
                // 方块已被破坏，清理数据
                iterator.remove();
                machinePositions.remove(pos);
                setDirty(true);
                continue;
            }
            setDirty(true);
        }
    }

    /**
     * 每天调用：蜂房产蜜逻辑（累加器模式，类似采集器）
     * 风天 +3 天，平时 +1 天，累计 >=4 天后设为 DONE 并 -4 保留余数
     */
    public void tickBeeHouses(ServerWorld world, long currentDay) {
        for (var iterator = machinePositions.iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }
            BlockState blockState = world.getBlockState(pos);
            if (!(blockState.getBlock() instanceof ArtisanEquipmentBlock)) {
                iterator.remove();
                setDirty(true);
                continue;
            }
            // 只有空闲状态的蜂房才能累加
            if (blockState.get(ArtisanEquipmentBlock.STATE) != ArtisanEquipmentBlock.MachineState.EMPTY) continue;

            // 冬季不产蜜
            stardewvalley.modid.season.Season season = stardewvalley.modid.season.Season.fromTimeOfDay(world.getTimeOfDay());
            if (season == stardewvalley.modid.season.Season.WINTER) continue;

            // 检测天气
            stardewvalley.modid.weather.WeatherType todayWeather = stardewvalley.modid.weather.WeatherState.get(world).todayWeather;
            boolean isWindDay = todayWeather == stardewvalley.modid.weather.WeatherType.WIND_SPRING ||
                                todayWeather == stardewvalley.modid.weather.WeatherType.WIND_FALL;

            // 读取当前累计天数（MachineData.finishTime 用作 accumulatedDays）
            int accumulatedDays = 0;
            if (machineDataMap.containsKey(pos)) {
                accumulatedDays = (int) machineDataMap.get(pos).finishTime();
            }

            // 累加：风天+3，平时+1
            accumulatedDays += isWindDay ? 3 : 1;

            if (accumulatedDays >= 4) {
                // 达到要求 → 产出蜂蜜，减去4保留余数
                accumulatedDays -= 4;

                // 检测附近花朵
                String honeyType = findNearbyFlowerHoney(world, pos);
                String honeyId = honeyType != null ? honeyType : "honey";

                // 直接产出，不跳过——harvestOutput 会处理物品不存在的情况
                String fullId = "stardewvalley:" + honeyId;
                machineDataMap.put(pos, new MachineData(
                    MachineType.BEE_HOUSE.name(), honeyId, accumulatedDays,
                    fullId, 1
                ));
                // 设置BlockEntity用于客户端渲染
                net.minecraft.block.entity.BlockEntity be = world.getBlockEntity(pos);
                if (be instanceof ArtisanEquipmentBlockEntity ae) {
                    ae.setItems(honeyId, "stardewvalley:" + honeyId);
                }
                world.setBlockState(pos, blockState.with(ArtisanEquipmentBlock.STATE, ArtisanEquipmentBlock.MachineState.DONE), 3);
            } else {
                // 未达到，仅保存累计天数
                String currentHoney = machineDataMap.containsKey(pos) ? machineDataMap.get(pos).inputItemId() : "";
                machineDataMap.put(pos, new MachineData(
                    MachineType.BEE_HOUSE.name(), currentHoney, accumulatedDays, "", 0
                ));
            }
            setDirty(true);
        }
    }

    public static String findNearbyFlowerHoney(ServerWorld world, BlockPos pos) {
        int range = 5;
        for (int dx = -range; dx <= range; dx++) {
            int maxDz = range - Math.abs(dx);
            for (int dz = -maxDz; dz <= maxDz; dz++) {
                BlockPos checkPos = pos.add(dx, 0, dz);
                BlockState flowerState = world.getBlockState(checkPos);
                Block flowerBlock = flowerState.getBlock();

                String flowerName = null;
                if (flowerBlock == ModBlocks.TULIP_CROP && flowerBlock instanceof BaseCropBlock && ((BaseCropBlock) flowerBlock).isMature(flowerState))
                    flowerName = "tulip";
                else if (flowerBlock == ModBlocks.BLUEJAZZ_CROP && flowerBlock instanceof BaseCropBlock && ((BaseCropBlock) flowerBlock).isMature(flowerState))
                    flowerName = "blue_jazz";
                else if (flowerBlock == ModBlocks.SUMMERSPANGLE_CROP && flowerBlock instanceof BaseCropBlock && ((BaseCropBlock) flowerBlock).isMature(flowerState))
                    flowerName = "summer_spangle";
                else if (flowerBlock == ModBlocks.POPPY_CROP && flowerBlock instanceof BaseCropBlock && ((BaseCropBlock) flowerBlock).isMature(flowerState))
                    flowerName = "poppy";
                else if (flowerBlock == ModBlocks.SUNFLOWER_CROP && flowerBlock instanceof BaseCropBlock && ((BaseCropBlock) flowerBlock).isMature(flowerState))
                    flowerName = "sunflower";
                else if (flowerBlock == ModBlocks.FAIRYROSE_CROP && flowerBlock instanceof BaseCropBlock && ((BaseCropBlock) flowerBlock).isMature(flowerState))
                    flowerName = "fairy_rose";

                if (flowerName != null) return flowerName + "_honey";
            }
        }
        return null;
    }

}
