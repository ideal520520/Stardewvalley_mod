package stardewvalley.modid.gui;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.util.SafeCodec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class OreToolUpgradeManager extends PersistentState {

    private static final String NAME = "stardewvalley_tool_upgrades";

    public static class UpgradeEntry {
        public String playerId;
        public String toolType;
        public String targetTier;
        public long completeTime;

        public UpgradeEntry(String playerId, String toolType, String targetTier, long completeTime) {
            this.playerId = playerId;
            this.toolType = toolType;
            this.targetTier = targetTier;
            this.completeTime = completeTime;
        }
    }

    private static final Codec<UpgradeEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.STRING.fieldOf("playerId").forGetter(e -> e.playerId),
            Codec.STRING.fieldOf("toolType").forGetter(e -> e.toolType),
            Codec.STRING.fieldOf("targetTier").forGetter(e -> e.targetTier),
            Codec.LONG.fieldOf("completeTime").forGetter(e -> e.completeTime)
        ).apply(instance, UpgradeEntry::new)
    );

    private record SimpleData(List<UpgradeEntry> entries) {}

    private static final Codec<SimpleData> SIMPLE_DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codec.list(ENTRY_CODEC).fieldOf("entries").forGetter(d -> d.entries)
        ).apply(instance, SimpleData::new)
    );

    public static final Codec<OreToolUpgradeManager> CODEC = SIMPLE_DATA_CODEC.xmap(
        data -> {
            OreToolUpgradeManager m = new OreToolUpgradeManager();
            for (UpgradeEntry e : data.entries) {
                m.upgrades.put(UUID.fromString(e.playerId), new InternalEntry(e.toolType, e.targetTier, e.completeTime));
            }
            return m;
        },
        m -> {
            List<UpgradeEntry> list = new ArrayList<>();
            for (Map.Entry<UUID, InternalEntry> e : m.upgrades.entrySet()) {
                list.add(new UpgradeEntry(e.getKey().toString(), e.getValue().toolType, e.getValue().targetTier, e.getValue().completeTime));
            }
            return new SimpleData(list);
        }
    );

    public static final PersistentStateType<OreToolUpgradeManager> TYPE = new PersistentStateType<>(
        NAME,
        OreToolUpgradeManager::new,
        SafeCodec.wrap(CODEC, OreToolUpgradeManager::new),
        DataFixTypes.LEVEL
    );

    public static class InternalEntry {
        public String toolType;
        public String targetTier;
        public long completeTime;

        public InternalEntry(String toolType, String targetTier, long completeTime) {
            this.toolType = toolType;
            this.targetTier = targetTier;
            this.completeTime = completeTime;
        }
    }

    private final Map<UUID, InternalEntry> upgrades = new HashMap<>();
    private final transient Set<UUID> completionNotified = new HashSet<>();

    public static OreToolUpgradeManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    public boolean hasUpgradeInProgress(UUID playerUuid) {
        return upgrades.containsKey(playerUuid);
    }

    public String getUpgradeToolType(UUID playerUuid) {
        InternalEntry e = upgrades.get(playerUuid);
        return e != null ? e.toolType : "";
    }

    public String getUpgradeTargetTier(UUID playerUuid) {
        InternalEntry e = upgrades.get(playerUuid);
        return e != null ? e.targetTier : "";
    }

    public long getUpgradeCompleteTime(UUID playerUuid) {
        InternalEntry e = upgrades.get(playerUuid);
        return e != null ? e.completeTime : 0;
    }

    public boolean startUpgrade(UUID playerUuid, String toolType, String targetTier, long currentTime) {
        if (upgrades.containsKey(playerUuid)) return false;
        long currentDay = currentTime / 24000L;
        long completeTime = (currentDay + 2) * 24000L + 5L;
        upgrades.put(playerUuid, new InternalEntry(toolType, targetTier, completeTime));
        setDirty(true);
        return true;
    }

    public InternalEntry completeUpgrade(UUID playerUuid) {
        InternalEntry entry = upgrades.remove(playerUuid);
        if (entry != null) setDirty(true);
        return entry;
    }

    public void tickUpgrades(ServerWorld world) {
        // 不再在 time=0 自动发放，改为通过铁匠铺领取
    }

    public InternalEntry getUpgrade(UUID playerUuid) {
        return upgrades.get(playerUuid);
    }

    public void notifyCompletedUpgrades(ServerWorld world) {
        long currentTime = world.getTimeOfDay();
        for (Map.Entry<UUID, InternalEntry> entry : upgrades.entrySet()) {
            InternalEntry ue = entry.getValue();
            if (currentTime >= ue.completeTime && !completionNotified.contains(entry.getKey())) {
                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(entry.getKey());
                if (player != null) {
                    if ("trash_can".equals(ue.toolType)) {
                        String tierLabel = switch (ue.targetTier) {
                            case "1" -> "铜"; case "2" -> "铁"; case "3" -> "金"; case "4" -> "铱"; default -> ue.targetTier;
                        };
                        player.sendMessage(Text.literal("§a[铁匠铺] 你的" + tierLabel + "垃圾桶已升级完成！到铁匠铺领取。"), false);
                    } else {
                        String tierLabel = switch (ue.targetTier) {
                            case "copper" -> "铜"; case "steel" -> "钢"; case "gold" -> "金"; case "iridium" -> "铱"; default -> ue.targetTier;
                        };
                        String toolLabel = switch (ue.toolType) {
                            case "hoe" -> "锄头"; case "pickaxe" -> "镐"; case "axe" -> "斧头"; case "watering_can" -> "水壶"; default -> ue.toolType;
                        };
                        player.sendMessage(Text.literal("§a[铁匠铺] 你的" + tierLabel + toolLabel + "已升级完成！到铁匠铺领取。"), false);
                    }
                }
                completionNotified.add(entry.getKey());
            }
        }
    }

    public static String getRequiredBar(String targetTier) {
        return switch (targetTier) {
            case "copper" -> "copper_bar";
            case "steel" -> "iron_bar";
            case "gold" -> "gold_bar";
            case "iridium" -> "iridium_bar";
            default -> null;
        };
    }

    public static int getRequiredGold(String targetTier) {
        return switch (targetTier) {
            case "copper" -> 2000;
            case "steel" -> 5000;
            case "gold" -> 10000;
            case "iridium" -> 25000;
            default -> 0;
        };
    }

    public static String getNextTier(String currentTier) {
        return switch (currentTier) {
            case "normal" -> "copper";
            case "copper" -> "steel";
            case "steel" -> "gold";
            case "gold" -> "iridium";
            default -> null;
        };
    }
}
