package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.util.SafeCodec;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SolarPanelState extends PersistentState {
    private static final String NAME = "stardewvalley_solar_panels";
    private final Map<BlockPos, Integer> panelData = new HashMap<>();
    private final Set<BlockPos> panelPositions = new HashSet<>();

    private record Entry(long pos, int accumulatedPoints) {}
    private record Data(List<Entry> entries) {}

    private static final Codec<Entry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.LONG.fieldOf("pos").forGetter(Entry::pos),
            Codec.INT.fieldOf("accumulatedPoints").forGetter(Entry::accumulatedPoints)
        ).apply(instance, Entry::new)
    );

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ENTRY_CODEC.listOf().fieldOf("entries").forGetter(Data::entries)
        ).apply(instance, Data::new)
    );

    public static final Codec<SolarPanelState> CODEC = DATA_CODEC.xmap(
        data -> {
            SolarPanelState state = new SolarPanelState();
            for (Entry e : data.entries()) {
                state.panelData.put(BlockPos.fromLong(e.pos()), e.accumulatedPoints());
                state.panelPositions.add(BlockPos.fromLong(e.pos()));
            }
            return state;
        },
        state -> {
            List<Entry> entries = state.panelData.entrySet().stream()
                .map(e -> new Entry(e.getKey().asLong(), e.getValue()))
                .toList();
            return new Data(entries);
        }
    );

    public static final PersistentStateType<SolarPanelState> TYPE = new PersistentStateType<>(
        NAME,
        SolarPanelState::new,
        SafeCodec.wrap(CODEC, SolarPanelState::new),
        DataFixTypes.LEVEL
    );

    public static SolarPanelState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void registerPanel(BlockPos pos) {
        panelPositions.add(pos.toImmutable());
        panelData.put(pos.toImmutable(), 0);
        setDirty(true);
    }

    public void removePanel(BlockPos pos) {
        panelData.remove(pos);
        panelPositions.remove(pos);
        setDirty(true);
    }

    public int getAccumulatedPoints(BlockPos pos) {
        return panelData.getOrDefault(pos, 0);
    }

    /**
     * 每天调用：为每个太阳能板检测天气，晴天累积1点，累积>=5产出电池
     */
    public void tickSolarPanels(ServerWorld world, long currentDay) {
        WeatherType todayWeather = WeatherState.get(world).todayWeather;
        boolean shouldAccumulate = todayWeather != WeatherType.RAIN && todayWeather != WeatherType.STORM
            && todayWeather != WeatherType.SNOW && todayWeather != WeatherType.GREEN_RAIN;

        for (Iterator<BlockPos> iterator = panelPositions.iterator(); iterator.hasNext(); ) {
            BlockPos pos = iterator.next();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }
            BlockState blockState = world.getBlockState(pos);
            if (!(blockState.getBlock() instanceof ArtisanEquipmentBlock)) {
                iterator.remove();
                panelData.remove(pos);
                setDirty(true);
                continue;
            }

            int accumulated = panelData.getOrDefault(pos, 0);

            if (shouldAccumulate) {
                accumulated += 1;
            }

            if (accumulated >= 5) {
                accumulated = 0;
                ArtisanEquipmentState artisanState = ArtisanEquipmentState.get(world);
                artisanState.setData(pos, new ArtisanEquipmentState.MachineData(
                    MachineType.SOLAR_PANEL.name(), "", 0, "stardewvalley:battery_pack", 1
                ));
                // 先更新方块状态，再设置 BlockEntity 物品数据确保客户端同步
                world.setBlockState(pos, blockState.with(ArtisanEquipmentBlock.STATE, ArtisanEquipmentBlock.MachineState.DONE), 3);
                if (world.getBlockEntity(pos) instanceof ArtisanEquipmentBlockEntity be) {
                    be.setItems("", "stardewvalley:battery_pack");
                }
            }

            // 保存累计进度
            panelData.put(pos, accumulated);
            setDirty(true);
        }
    }
}
