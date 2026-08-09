package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;
import stardewvalley.modid.season.Season;

import java.util.*;

public class BookStoreManager extends PersistentState {
    private static final String NAME = "stardewvalley_bookstore";

    // 存储每个季节中书店开业的日期
    // key: seasonIndex (0=spring,1=summer,2=fall,3=winter), value: set of dayOfSeason
    private Map<Integer, Set<Integer>> openDays = new HashMap<>();
    private int lastGeneratedSeason = -1;
    private boolean hasBroadcasted = false;

    // 旅行货车出现的天数
    private static final Set<Integer> TRAVELING_CART_DAYS = Set.of(5, 7, 12, 14, 19, 21, 26, 28);

    public Map<Integer, Set<Integer>> getOpenDays() {
        return openDays;
    }

    public int getLastGeneratedSeason() {
        return lastGeneratedSeason;
    }

    public boolean isHasBroadcasted() {
        return hasBroadcasted;
    }

    public void setHasBroadcasted(boolean hasBroadcasted) {
        this.hasBroadcasted = hasBroadcasted;
        setDirty(true);
    }

    public void markBroadcasted() {
        this.hasBroadcasted = true;
        setDirty(true);
    }

    /**
     * 为当前季节生成2个书店营业日（排除旅行货车天）
     */
    public Map<Integer, Set<Integer>> getOrGenerateOpenDays(ServerWorld world) {
        long timeOfDay = world.getTimeOfDay();
        Season currentSeason = Season.fromTimeOfDay(timeOfDay);
        int seasonIndex = currentSeason.ordinal();

        if (lastGeneratedSeason == seasonIndex && openDays.containsKey(seasonIndex)) {
            return openDays;
        }

        // 基于季节的随机种子
        Random random = new Random(timeOfDay / 24000L / 28);
        Set<Integer> availableDays = new HashSet<>();
        for (int day = 1; day <= 28; day++) {
            if (!TRAVELING_CART_DAYS.contains(day)) {
                availableDays.add(day);
            }
        }

        List<Integer> dayList = new ArrayList<>(availableDays);
        Collections.shuffle(dayList, random);
        Set<Integer> selectedDays = new HashSet<>();
        for (int i = 0; i < 2 && i < dayList.size(); i++) {
            selectedDays.add(dayList.get(i));
        }

        openDays.put(seasonIndex, selectedDays);
        lastGeneratedSeason = seasonIndex;
        setDirty(true);

        return openDays;
    }

    /**
     * 判断今天是否营业
     */
    public boolean isOpenToday(ServerWorld world) {
        long timeOfDay = world.getTimeOfDay();
        Season currentSeason = Season.fromTimeOfDay(timeOfDay);
        int seasonIndex = currentSeason.ordinal();
        int dayOfSeason = Season.getDayOfSeason(timeOfDay);

        getOrGenerateOpenDays(world);

        Set<Integer> days = openDays.get(seasonIndex);
        return days != null && days.contains(dayOfSeason);
    }

    // ====== 序列化 ======

    private record OpenDaysData(Map<Integer, List<Integer>> openDays, int lastGeneratedSeason, boolean hasBroadcasted) {}

    private static final Codec<Map<Integer, List<Integer>>> OPEN_DAYS_CODEC = Codec.unboundedMap(Codec.INT, Codec.list(Codec.INT));

    private static final Codec<OpenDaysData> SAVE_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            OPEN_DAYS_CODEC.fieldOf("openDays").forGetter(OpenDaysData::openDays),
            Codec.INT.fieldOf("lastGeneratedSeason").forGetter(OpenDaysData::lastGeneratedSeason),
            Codec.BOOL.fieldOf("hasBroadcasted").forGetter(OpenDaysData::hasBroadcasted)
        ).apply(instance, OpenDaysData::new)
    );

    public static final Codec<BookStoreManager> CODEC = SAVE_DATA_CODEC.xmap(
        data -> {
            BookStoreManager mgr = new BookStoreManager();
            mgr.lastGeneratedSeason = data.lastGeneratedSeason;
            mgr.hasBroadcasted = data.hasBroadcasted;
            for (var entry : data.openDays.entrySet()) {
                mgr.openDays.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            return mgr;
        },
        mgr -> {
            Map<Integer, List<Integer>> listMap = new HashMap<>();
            for (var entry : mgr.openDays.entrySet()) {
                listMap.put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
            return new OpenDaysData(listMap, mgr.lastGeneratedSeason, mgr.hasBroadcasted);
        }
    );

    private static final PersistentStateType<BookStoreManager> TYPE = new PersistentStateType<>(
        NAME,
        BookStoreManager::new,
        SafeCodec.wrap(CODEC, BookStoreManager::new),
        DataFixTypes.LEVEL
    );

    public static BookStoreManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }
}
