package stardewvalley.modid.block;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import stardewvalley.modid.util.SafeCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TapperState extends PersistentState {
    private static final String NAME = "stardewvalley_tapper";
    private final Map<BlockPos, TapperData> tapperDataMap = new HashMap<>();

    public record TapperData(int accumulatedDays, String outputItemId, int outputCount) {}

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

    public static final Codec<TapperState> CODEC = LIST_CODEC.xmap(
        list -> {
            TapperState state = new TapperState();
            for (Entry e : list) {
                state.tapperDataMap.put(BlockPos.fromLong(e.pos()), new TapperData(e.accumulatedDays(), e.outputItemId(), e.outputCount()));
            }
            return state;
        },
        state -> state.tapperDataMap.entrySet().stream()
            .map(e -> new Entry(e.getKey().asLong(), e.getValue().accumulatedDays(), e.getValue().outputItemId(), e.getValue().outputCount()))
            .toList()
    );

    public static final PersistentStateType<TapperState> TYPE = new PersistentStateType<>(
        NAME, TapperState::new, SafeCodec.wrap(CODEC, TapperState::new), DataFixTypes.LEVEL
    );

    public static TapperState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public void registerTapper(BlockPos pos) {
        tapperDataMap.put(pos.toImmutable(), new TapperData(0, "", 0));
        setDirty(true);
    }

    public TapperData getData(BlockPos pos) {
        return tapperDataMap.get(pos);
    }

    public void removeData(BlockPos pos) {
        tapperDataMap.remove(pos);
        setDirty(true);
    }

    public boolean hasData(BlockPos pos) {
        return tapperDataMap.containsKey(pos);
    }

    public void setData(BlockPos pos, TapperData data) {
        tapperDataMap.put(pos.toImmutable(), data);
        setDirty(true);
    }

    public Map<BlockPos, TapperData> getAllData() {
        return tapperDataMap;
    }

    /**
     * 每天 time=10 调用：所有树液采集器累加1天，达到目标天数后产出并重置
     */
    public void tickTappers(ServerWorld world, long currentDay) {
        var iterator = tapperDataMap.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            BlockPos pos = entry.getKey();
            TapperData data = entry.getValue();

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }

            BlockState blockState = world.getBlockState(pos);
            if (!(blockState.getBlock() instanceof TapperBlock tapperBlock)) {
                iterator.remove();
                setDirty(true);
                continue;
            }

            // 已经产出，跳过
            if (blockState.get(TapperBlock.DONE)) continue;

            // 获取木头类型（贴着的那一面和下面一块）
            BlockState facingBlockState = world.getBlockState(pos.offset(blockState.get(TapperBlock.FACING).getOpposite()));
            String woodId = TapperBlock.getBlockId(facingBlockState);
            boolean isHeavy = tapperBlock.isHeavy();
            int requiredDays = TapperBlock.getDaysForWood(woodId, isHeavy);
            String productId = TapperBlock.getProductForWood(woodId);

            // 累加1天
            int newDays = data.accumulatedDays() + 1;

            if (newDays >= requiredDays) {
                // 达到天数 → 产出，重置为0
                TapperBlockEntity be = (TapperBlockEntity) world.getBlockEntity(pos);
                String outputId = "stardewvalley:" + productId;
                if (be != null) {
                    be.setOutputItemId(outputId);
                }
                tapperDataMap.put(pos.toImmutable(), new TapperData(0, productId, 1));
                world.setBlockState(pos, blockState.with(TapperBlock.DONE, true), 3);
            } else {
                // 未达到，累加
                tapperDataMap.put(pos.toImmutable(), new TapperData(newDays, "", 0));
            }
            setDirty(true);
        }
    }
}
