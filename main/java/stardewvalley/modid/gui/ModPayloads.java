package stardewvalley.modid.gui;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import stardewvalley.modid.StardewValley;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ModPayloads {

    public static final Identifier GOLD_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "gold_sync");
    public static final Identifier GOLD_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "gold_request");
    public static final Identifier SHIPPING_DATA_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "shipping_data_sync");
    public static final Identifier SHIPPING_DATA_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "shipping_data_request");
    public static final Identifier ADD_TO_SHIPPING_ID = Identifier.of(StardewValley.MOD_ID, "add_to_shipping");
    public static final Identifier FARMING_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "farming_sync");
    public static final Identifier FARMING_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "farming_request");
    public static final Identifier FORAGING_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "foraging_sync");
    public static final Identifier FORAGING_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "foraging_request");
    public static final Identifier FISHING_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "fishing_sync");
    public static final Identifier FISHING_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "fishing_request");
    public static final Identifier MINING_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "mining_sync");
    public static final Identifier MINING_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "mining_request");
    public static final Identifier COMBAT_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "combat_sync");
    public static final Identifier COMBAT_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "combat_request");
    public static final Identifier FLOATING_ITEM_ID = Identifier.of(StardewValley.MOD_ID, "floating_item");
    public static final Identifier ANIMAL_CATALOGUE_DISCOUNT_ID = Identifier.of(StardewValley.MOD_ID, "animal_catalogue_discount");
    public static final Identifier PRICE_CATALOGUE_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "price_catalogue_sync");
    public static final Identifier TREASURE_APPRAISAL_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "treasure_appraisal_sync");

    public record TreasureAppraisalSyncS2CPayload(boolean active) implements CustomPayload {
        public static final CustomPayload.Id<TreasureAppraisalSyncS2CPayload> ID = new CustomPayload.Id<>(TREASURE_APPRAISAL_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, TreasureAppraisalSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.active),
            buf -> new TreasureAppraisalSyncS2CPayload(buf.readBoolean())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record PriceCatalogueSyncS2CPayload(boolean active) implements CustomPayload {
        public static final CustomPayload.Id<PriceCatalogueSyncS2CPayload> ID = new CustomPayload.Id<>(PRICE_CATALOGUE_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, PriceCatalogueSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.active),
            buf -> new PriceCatalogueSyncS2CPayload(buf.readBoolean())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record AnimalCatalogueDiscountS2CPayload(boolean hasDiscount) implements CustomPayload {
        public static final CustomPayload.Id<AnimalCatalogueDiscountS2CPayload> ID = new CustomPayload.Id<>(ANIMAL_CATALOGUE_DISCOUNT_ID);
        public static final PacketCodec<PacketByteBuf, AnimalCatalogueDiscountS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.hasDiscount),
            buf -> new AnimalCatalogueDiscountS2CPayload(buf.readBoolean())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record FloatingItemS2CPayload(String itemId) implements CustomPayload {
        public static final CustomPayload.Id<FloatingItemS2CPayload> ID = new CustomPayload.Id<>(FLOATING_ITEM_ID);
        public static final PacketCodec<PacketByteBuf, FloatingItemS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeString(value.itemId),
            buf -> new FloatingItemS2CPayload(buf.readString())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record GoldSyncS2CPayload(int gold) implements CustomPayload {
        public static final CustomPayload.Id<GoldSyncS2CPayload> ID = new CustomPayload.Id<>(GOLD_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, GoldSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeInt(value.gold),
            buf -> new GoldSyncS2CPayload(buf.readInt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record GoldRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<GoldRequestC2SPayload> ID = new CustomPayload.Id<>(GOLD_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, GoldRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new GoldRequestC2SPayload()
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ShippingDataRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<ShippingDataRequestC2SPayload> ID = new CustomPayload.Id<>(SHIPPING_DATA_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, ShippingDataRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new ShippingDataRequestC2SPayload()
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ShippingDataSyncS2CPayload(List<GoldManager.ShippingEntry> entries) implements CustomPayload {
        public static final CustomPayload.Id<ShippingDataSyncS2CPayload> ID = new CustomPayload.Id<>(SHIPPING_DATA_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, ShippingDataSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.entries.size());
                for (GoldManager.ShippingEntry entry : value.entries) {
                    buf.writeIdentifier(entry.itemId);
                    buf.writeInt(entry.count);
                    buf.writeBoolean(entry.playerUuid != null);
                    if (entry.playerUuid != null) buf.writeUuid(entry.playerUuid);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<GoldManager.ShippingEntry> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    Identifier id = buf.readIdentifier();
                    int count = buf.readInt();
                    UUID uuid = buf.readBoolean() ? buf.readUuid() : null;
                    list.add(new GoldManager.ShippingEntry(id, count, uuid));
                }
                return new ShippingDataSyncS2CPayload(list);
            }
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record FarmingSyncS2CPayload(int level, int xp, int nextLevelXp) implements CustomPayload {
        public static final CustomPayload.Id<FarmingSyncS2CPayload> ID = new CustomPayload.Id<>(FARMING_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, FarmingSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.level);
                buf.writeInt(value.xp);
                buf.writeInt(value.nextLevelXp);
            },
            buf -> new FarmingSyncS2CPayload(buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record FarmingRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<FarmingRequestC2SPayload> ID = new CustomPayload.Id<>(FARMING_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, FarmingRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new FarmingRequestC2SPayload()
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ForagingSyncS2CPayload(int level, int xp, int nextLevelXp) implements CustomPayload {
        public static final CustomPayload.Id<ForagingSyncS2CPayload> ID = new CustomPayload.Id<>(FORAGING_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, ForagingSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.level()); buf.writeInt(value.xp()); buf.writeInt(value.nextLevelXp()); },
            buf -> new ForagingSyncS2CPayload(buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ForagingRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<ForagingRequestC2SPayload> ID = new CustomPayload.Id<>(FORAGING_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, ForagingRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new ForagingRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record FishingSyncS2CPayload(int level, int xp, int nextLevelXp) implements CustomPayload {
        public static final CustomPayload.Id<FishingSyncS2CPayload> ID = new CustomPayload.Id<>(FISHING_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, FishingSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.level()); buf.writeInt(value.xp()); buf.writeInt(value.nextLevelXp()); },
            buf -> new FishingSyncS2CPayload(buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record FishingRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<FishingRequestC2SPayload> ID = new CustomPayload.Id<>(FISHING_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, FishingRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new FishingRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record MiningSyncS2CPayload(int level, int xp, int nextLevelXp) implements CustomPayload {
        public static final CustomPayload.Id<MiningSyncS2CPayload> ID = new CustomPayload.Id<>(MINING_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, MiningSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.level()); buf.writeInt(value.xp()); buf.writeInt(value.nextLevelXp()); },
            buf -> new MiningSyncS2CPayload(buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record MiningRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<MiningRequestC2SPayload> ID = new CustomPayload.Id<>(MINING_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, MiningRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new MiningRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record ShopBuyC2SPayload(String itemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<ShopBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "shop_buy"));
        public static final PacketCodec<PacketByteBuf, ShopBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.itemId);
                buf.writeInt(value.count);
            },
            buf -> new ShopBuyC2SPayload(buf.readString(), buf.readInt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record AddToShippingC2SPayload(int slotIndex, int count) implements CustomPayload {
        public static final CustomPayload.Id<AddToShippingC2SPayload> ID = new CustomPayload.Id<>(ADD_TO_SHIPPING_ID);
        public static final PacketCodec<PacketByteBuf, AddToShippingC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.slotIndex);
                buf.writeInt(value.count);
            },
            buf -> new AddToShippingC2SPayload(buf.readInt(), buf.readInt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Ore shop buy
    public record OreShopBuyC2SPayload(String itemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<OreShopBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "ore_shop_buy"));
        public static final PacketCodec<PacketByteBuf, OreShopBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.itemId); buf.writeInt(value.count); },
            buf -> new OreShopBuyC2SPayload(buf.readString(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Tool upgrade request
    public record ToolUpgradeRequestC2SPayload(String toolType) implements CustomPayload {
        public static final CustomPayload.Id<ToolUpgradeRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "tool_upgrade_request"));
        public static final PacketCodec<PacketByteBuf, ToolUpgradeRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeString(value.toolType),
            buf -> new ToolUpgradeRequestC2SPayload(buf.readString())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Tool upgrade status sync
    public record ToolUpgradeStatusSyncS2CPayload(boolean upgrading, String toolType, String targetTier, long completeTime) implements CustomPayload {
        public static final CustomPayload.Id<ToolUpgradeStatusSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "tool_upgrade_status_sync"));
        public static final PacketCodec<PacketByteBuf, ToolUpgradeStatusSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeBoolean(value.upgrading);
                buf.writeString(value.toolType);
                buf.writeString(value.targetTier);
                buf.writeLong(value.completeTime);
            },
            buf -> new ToolUpgradeStatusSyncS2CPayload(buf.readBoolean(), buf.readString(), buf.readString(), buf.readLong())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Tool upgrade status request
    public record ToolUpgradeStatusRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<ToolUpgradeStatusRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "tool_upgrade_status_request"));
        public static final PacketCodec<PacketByteBuf, ToolUpgradeStatusRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new ToolUpgradeStatusRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Fish shop buy
    public record FishShopBuyC2SPayload(String itemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<FishShopBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "fish_shop_buy"));
        public static final PacketCodec<PacketByteBuf, FishShopBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.itemId); buf.writeInt(value.count); },
            buf -> new FishShopBuyC2SPayload(buf.readString(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Sell to ore shop
    public record OreShopSellC2SPayload(int slotIndex, int count) implements CustomPayload {
        public static final CustomPayload.Id<OreShopSellC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "ore_shop_sell"));
        public static final PacketCodec<PacketByteBuf, OreShopSellC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.slotIndex); buf.writeInt(value.count); },
            buf -> new OreShopSellC2SPayload(buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Sell to fish shop
    public record FishShopSellC2SPayload(int slotIndex, int count) implements CustomPayload {
        public static final CustomPayload.Id<FishShopSellC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "fish_shop_sell"));
        public static final PacketCodec<PacketByteBuf, FishShopSellC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.slotIndex); buf.writeInt(value.count); },
            buf -> new FishShopSellC2SPayload(buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Combat sync payload
    public record CombatSyncS2CPayload(int level, int xp, int nextLevelXp) implements CustomPayload {
        public static final CustomPayload.Id<CombatSyncS2CPayload> ID = new CustomPayload.Id<>(COMBAT_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, CombatSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.level()); buf.writeInt(value.xp()); buf.writeInt(value.nextLevelXp()); },
            buf -> new CombatSyncS2CPayload(buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // Combat request payload
    public record CombatRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<CombatRequestC2SPayload> ID = new CustomPayload.Id<>(COMBAT_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, CombatRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new CombatRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 钓竿ScreenHandler打开请求 ======
    public record RodScreenOpenC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<RodScreenOpenC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "rod_screen_open")
        );
        public static final PacketCodec<PacketByteBuf, RodScreenOpenC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new RodScreenOpenC2SPayload()
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 运气同步 ======
    public record LuckSyncS2CPayload(float luck) implements CustomPayload {
        public static final CustomPayload.Id<LuckSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "luck_sync"));
        public static final PacketCodec<PacketByteBuf, LuckSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeFloat(value.luck),
            buf -> new LuckSyncS2CPayload(buf.readFloat())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record LuckRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<LuckRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "luck_request"));
        public static final PacketCodec<PacketByteBuf, LuckRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new LuckRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 天气同步 ======
    public static final Identifier WEATHER_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "weather_sync");
    public static final Identifier WEATHER_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "weather_request");

    public record WeatherSyncS2CPayload(int weatherOrdinal) implements CustomPayload {
        public static final CustomPayload.Id<WeatherSyncS2CPayload> ID = new CustomPayload.Id<>(WEATHER_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, WeatherSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeInt(value.weatherOrdinal),
            buf -> new WeatherSyncS2CPayload(buf.readInt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record WeatherRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<WeatherRequestC2SPayload> ID = new CustomPayload.Id<>(WEATHER_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, WeatherRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new WeatherRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 武器技能 ======
    public record WeaponSkillC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<WeaponSkillC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "weapon_skill"));
        public static final PacketCodec<PacketByteBuf, WeaponSkillC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new WeaponSkillC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 工作台合成 ======
    public record WorkbenchCraftC2SPayload(String resultItemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<WorkbenchCraftC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "workbench_craft"));
        public static final PacketCodec<PacketByteBuf, WorkbenchCraftC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.resultItemId);
                buf.writeInt(value.count);
            },
            buf -> new WorkbenchCraftC2SPayload(buf.readString(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 技能系统 ======
    public static final Identifier SKILL_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "skill_sync");
    public static final Identifier SKILL_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "skill_request");
    public static final Identifier SKILL_SELECT_ID = Identifier.of(StardewValley.MOD_ID, "skill_select");

    // 技能数据同步（服务端→客户端）
    // 编码: category ordinal (int) + level5 skill id (string) + level10 skill id (string)，重复5次
    // category顺序: FARMING=0, FORAGING=1, FISHING=2, MINING=3, COMBAT=4
    public record SkillSyncS2CPayload(
        String farmingLevel5, String farmingLevel10,
        String foragingLevel5, String foragingLevel10,
        String fishingLevel5, String fishingLevel10,
        String miningLevel5, String miningLevel10,
        String combatLevel5, String combatLevel10
    ) implements CustomPayload {
        public static final CustomPayload.Id<SkillSyncS2CPayload> ID = new CustomPayload.Id<>(SKILL_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, SkillSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                writeNullableString(buf, value.farmingLevel5);
                writeNullableString(buf, value.farmingLevel10);
                writeNullableString(buf, value.foragingLevel5);
                writeNullableString(buf, value.foragingLevel10);
                writeNullableString(buf, value.fishingLevel5);
                writeNullableString(buf, value.fishingLevel10);
                writeNullableString(buf, value.miningLevel5);
                writeNullableString(buf, value.miningLevel10);
                writeNullableString(buf, value.combatLevel5);
                writeNullableString(buf, value.combatLevel10);
            },
            buf -> new SkillSyncS2CPayload(
                readNullableString(buf), readNullableString(buf),
                readNullableString(buf), readNullableString(buf),
                readNullableString(buf), readNullableString(buf),
                readNullableString(buf), readNullableString(buf),
                readNullableString(buf), readNullableString(buf)
            )
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端请求技能数据
    public record SkillRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<SkillRequestC2SPayload> ID = new CustomPayload.Id<>(SKILL_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, SkillRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new SkillRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端选择技能
    // categoryOrdinal: 0=耕种,1=采集,2=钓鱼,3=挖矿,4=战斗
    // tier: 1=5级技能, 2=10级技能
    // skillId: 技能ID，如果为null表示取消选择(仅限tier2)
    public record SkillSelectC2SPayload(int categoryOrdinal, int tier, String skillId) implements CustomPayload {
        public static final CustomPayload.Id<SkillSelectC2SPayload> ID = new CustomPayload.Id<>(SKILL_SELECT_ID);
        public static final PacketCodec<PacketByteBuf, SkillSelectC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.categoryOrdinal);
                buf.writeInt(value.tier);
                writeNullableString(buf, value.skillId);
            },
            buf -> new SkillSelectC2SPayload(buf.readInt(), buf.readInt(), readNullableString(buf))
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 精通系统 ======
    public static final Identifier MASTERY_SYNC_ID = Identifier.of(StardewValley.MOD_ID, "mastery_sync");
    public static final Identifier MASTERY_REQUEST_ID = Identifier.of(StardewValley.MOD_ID, "mastery_request");
    public static final Identifier MASTERY_SELECT_ID = Identifier.of(StardewValley.MOD_ID, "mastery_select");

    // 精通数据同步（服务端→客户端）
    // 编码: 精通点数(int) + 精通等级(int) + 五个技能是否精通(boolean×5)，顺序 FARMING,FORAGING,FISHING,MINING,COMBAT
    public record MasterySyncS2CPayload(
        int masteryPoints, int masteryLevel,
        boolean farming, boolean foraging, boolean fishing, boolean mining, boolean combat
    ) implements CustomPayload {
        public static final CustomPayload.Id<MasterySyncS2CPayload> ID = new CustomPayload.Id<>(MASTERY_SYNC_ID);
        public static final PacketCodec<PacketByteBuf, MasterySyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.masteryPoints);
                buf.writeInt(value.masteryLevel);
                buf.writeBoolean(value.farming);
                buf.writeBoolean(value.foraging);
                buf.writeBoolean(value.fishing);
                buf.writeBoolean(value.mining);
                buf.writeBoolean(value.combat);
            },
            buf -> new MasterySyncS2CPayload(
                buf.readInt(), buf.readInt(),
                buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean(), buf.readBoolean()
            )
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端请求精通数据
    public record MasteryRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<MasteryRequestC2SPayload> ID = new CustomPayload.Id<>(MASTERY_REQUEST_ID);
        public static final PacketCodec<PacketByteBuf, MasteryRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new MasteryRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端选择精通技能（categoryOrdinal: 0=耕种,1=采集,2=钓鱼,3=挖矿,4=战斗）
    public record MasterySelectC2SPayload(int categoryOrdinal) implements CustomPayload {
        public static final CustomPayload.Id<MasterySelectC2SPayload> ID = new CustomPayload.Id<>(MASTERY_SELECT_ID);
        public static final PacketCodec<PacketByteBuf, MasterySelectC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeInt(value.categoryOrdinal),
            buf -> new MasterySelectC2SPayload(buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    private static void writeNullableString(PacketByteBuf buf, String s) {
        if (s == null) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeString(s);
        }
    }

    private static String readNullableString(PacketByteBuf buf) {
        return buf.readBoolean() ? buf.readString() : null;
    }

    // ====== 旅行货车 ======

    // 旅行货车商品数据类（非CustomPayload，纯数据容器）
    public static class TravelingCartItem {
        public String itemId;
        public int price;
        public int maxBuy;
        public int available; // 剩余可购买数量
        public boolean isSpecial;

        public TravelingCartItem(String itemId, int price, int maxBuy, int available, boolean isSpecial) {
            this.itemId = itemId;
            this.price = price;
            this.maxBuy = maxBuy;
            this.available = available;
            this.isSpecial = isSpecial;
        }
    }

    // 客户端请求旅行货车商品列表
    public record TravelingCartStockRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<TravelingCartStockRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "traveling_cart_stock_request"));
        public static final PacketCodec<PacketByteBuf, TravelingCartStockRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new TravelingCartStockRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 服务端同步旅行货车商品列表给客户端
    public record TravelingCartStockSyncS2CPayload(List<TravelingCartItem> items) implements CustomPayload {
        public static final CustomPayload.Id<TravelingCartStockSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "traveling_cart_stock_sync"));
        public static final PacketCodec<PacketByteBuf, TravelingCartStockSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.items.size());
                for (TravelingCartItem item : value.items) {
                    buf.writeString(item.itemId);
                    buf.writeInt(item.price);
                    buf.writeInt(item.maxBuy);
                    buf.writeInt(item.available);
                    buf.writeBoolean(item.isSpecial);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<TravelingCartItem> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(new TravelingCartItem(buf.readString(), buf.readInt(), buf.readInt(), buf.readInt(), buf.readBoolean()));
                }
                return new TravelingCartStockSyncS2CPayload(list);
            }
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端向服务端发送购买旅行货车商品的请求
    public record TravelingCartBuyC2SPayload(String itemId, int count, int price) implements CustomPayload {
        public static final CustomPayload.Id<TravelingCartBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "traveling_cart_buy"));
        public static final PacketCodec<PacketByteBuf, TravelingCartBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.itemId); buf.writeInt(value.count); buf.writeInt(value.price); },
            buf -> new TravelingCartBuyC2SPayload(buf.readString(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 收集包系统 ======

    public record BundleDataRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<BundleDataRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "bundle_data_request"));
        public static final PacketCodec<PacketByteBuf, BundleDataRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new BundleDataRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BundleSubmitItemC2SPayload(String bundleId, int slotIndex) implements CustomPayload {
        public static final CustomPayload.Id<BundleSubmitItemC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "bundle_submit_item"));
        public static final PacketCodec<PacketByteBuf, BundleSubmitItemC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.bundleId); buf.writeInt(value.slotIndex); },
            buf -> new BundleSubmitItemC2SPayload(buf.readString(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BundleClaimRewardC2SPayload(String bundleId) implements CustomPayload {
        public static final CustomPayload.Id<BundleClaimRewardC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "bundle_claim_reward"));
        public static final PacketCodec<PacketByteBuf, BundleClaimRewardC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.bundleId); },
            buf -> new BundleClaimRewardC2SPayload(buf.readString())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BundleVaultBuyC2SPayload(String bundleId) implements CustomPayload {
        public static final CustomPayload.Id<BundleVaultBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "bundle_vault_buy"));
        public static final PacketCodec<PacketByteBuf, BundleVaultBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.bundleId); },
            buf -> new BundleVaultBuyC2SPayload(buf.readString())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BundleSlotSyncEntry(String bundleId, java.util.Map<String, Integer> submittedItems, boolean completed) {
        public void write(PacketByteBuf buf) {
            buf.writeString(bundleId);
            buf.writeInt(submittedItems.size());
            for (var e : submittedItems.entrySet()) {
                buf.writeString(e.getKey());
                buf.writeInt(e.getValue());
            }
            buf.writeBoolean(completed);
        }
        public static BundleSlotSyncEntry read(PacketByteBuf buf) {
            String id = buf.readString();
            int size = buf.readInt();
            java.util.Map<String, Integer> items = new java.util.LinkedHashMap<>();
            for (int i = 0; i < size; i++) items.put(buf.readString(), buf.readInt());
            boolean done = buf.readBoolean();
            return new BundleSlotSyncEntry(id, items, done);
        }
    }

    public record BundleDataSyncS2CPayload(java.util.List<BundleSlotSyncEntry> entries,
                                            java.util.Set<String> completedBundles,
                                            java.util.Set<String> vaultPurchased,
                                            java.util.Set<String> rewardClaimedBundleIds) implements CustomPayload {
        public static final CustomPayload.Id<BundleDataSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "bundle_data_sync"));
        public static final PacketCodec<PacketByteBuf, BundleDataSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.entries.size());
                for (BundleSlotSyncEntry e : value.entries) e.write(buf);
                buf.writeInt(value.completedBundles.size());
                for (String s : value.completedBundles) buf.writeString(s);
                buf.writeInt(value.vaultPurchased.size());
                for (String s : value.vaultPurchased) buf.writeString(s);
                buf.writeInt(value.rewardClaimedBundleIds.size());
                for (String s : value.rewardClaimedBundleIds) buf.writeString(s);
            },
            buf -> {
                int entryCount = buf.readInt();
                java.util.List<BundleSlotSyncEntry> entries = new java.util.ArrayList<>();
                for (int i = 0; i < entryCount; i++) entries.add(BundleSlotSyncEntry.read(buf));
                int compCount = buf.readInt();
                java.util.Set<String> completed = new java.util.HashSet<>();
                for (int i = 0; i < compCount; i++) completed.add(buf.readString());
                int vaultCount = buf.readInt();
                java.util.Set<String> vault = new java.util.HashSet<>();
                for (int i = 0; i < vaultCount; i++) vault.add(buf.readString());
                int claimCount = buf.readInt();
                java.util.Set<String> claimed = new java.util.HashSet<>();
                for (int i = 0; i < claimCount; i++) claimed.add(buf.readString());
                return new BundleDataSyncS2CPayload(entries, completed, vault, claimed);
            }
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 沙漠商人交易 ======
    public record DesertTraderBuyC2SPayload(String resultItemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<DesertTraderBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "desert_trader_buy"));
        public static final PacketCodec<PacketByteBuf, DesertTraderBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.resultItemId);
                buf.writeInt(value.count);
            },
            buf -> new DesertTraderBuyC2SPayload(buf.readString(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 科罗布斯商店 ======
    public static class KrobusShopItem {
        public String itemId;
        public int price;
        public int maxBuy;
        public int available;
        public KrobusShopItem(String itemId, int price, int maxBuy, int available) {
            this.itemId = itemId; this.price = price; this.maxBuy = maxBuy; this.available = available;
        }
    }

    public record KrobusDataRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<KrobusDataRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "krobus_data_request"));
        public static final PacketCodec<PacketByteBuf, KrobusDataRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new KrobusDataRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record KrobusDataSyncS2CPayload(List<KrobusShopItem> items) implements CustomPayload {
        public static final CustomPayload.Id<KrobusDataSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "krobus_data_sync"));
        public static final PacketCodec<PacketByteBuf, KrobusDataSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.items.size());
                for (KrobusShopItem item : value.items) {
                    buf.writeString(item.itemId);
                    buf.writeInt(item.price);
                    buf.writeInt(item.maxBuy);
                    buf.writeInt(item.available);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<KrobusShopItem> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(new KrobusShopItem(buf.readString(), buf.readInt(), buf.readInt(), buf.readInt()));
                }
                return new KrobusDataSyncS2CPayload(list);
            }
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record KrobusBuyC2SPayload(String itemId, int count, int price) implements CustomPayload {
        public static final CustomPayload.Id<KrobusBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "krobus_buy"));
        public static final PacketCodec<PacketByteBuf, KrobusBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.itemId); buf.writeInt(value.count); buf.writeInt(value.price); },
            buf -> new KrobusBuyC2SPayload(buf.readString(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 马龙武器商店以物换物 ======
    public record MarlonBarterC2SPayload(String resultItemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<MarlonBarterC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "marlon_barter"));
        public static final PacketCodec<PacketByteBuf, MarlonBarterC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.resultItemId);
                buf.writeInt(value.count);
            },
            buf -> new MarlonBarterC2SPayload(buf.readString(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 马龙商店兑换状态同步 ======
    public record MarlonTradeStateRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<MarlonTradeStateRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "marlon_trade_state_request"));
        public static final PacketCodec<PacketByteBuf, MarlonTradeStateRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new MarlonTradeStateRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record MarlonTradeStateSyncS2CPayload(boolean hasTraded) implements CustomPayload {
        public static final CustomPayload.Id<MarlonTradeStateSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "marlon_trade_state_sync"));
        public static final PacketCodec<PacketByteBuf, MarlonTradeStateSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.hasTraded),
            buf -> new MarlonTradeStateSyncS2CPayload(buf.readBoolean())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 额外装备栏系统 ======

    /**
     * 客户端→服务端：装备栏操作
     * action: 0=PUT, 1=TAKE, 2=SWAP
     * slotIndex: 0-2
     * itemId: 操作的物品ID (PUT/SWAP时使用)
     * count: 物品数量 (PUT/SWAP时使用)
     */
    public record EquipmentActionC2SPayload(int action, int slotIndex, String itemId, int count) implements CustomPayload {
        public static final CustomPayload.Id<EquipmentActionC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "equipment_action")
        );
        public static final PacketCodec<PacketByteBuf, EquipmentActionC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.action);
                buf.writeInt(value.slotIndex);
                buf.writeString(value.itemId);
                buf.writeInt(value.count);
            },
            buf -> new EquipmentActionC2SPayload(buf.readInt(), buf.readInt(), buf.readString(), buf.readInt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /**
     * 服务端→客户端：装备栏数据同步
     * 将3个槽位的物品以NBT列表形式发送
     */
    public record EquipmentSyncS2CPayload(NbtCompound slotData) implements CustomPayload {
        public static final CustomPayload.Id<EquipmentSyncS2CPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "equipment_sync")
        );
        public static final PacketCodec<PacketByteBuf, EquipmentSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeNbt(value.slotData),
            buf -> new EquipmentSyncS2CPayload(buf.readNbt())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 垃圾桶系统 ======

    /** 客户端→服务端：垃圾桶操作 (0=放入/交换, 1=取出) */
    public record TrashCanActionC2SPayload(int action, String itemId, int count, int oldItemValue, int oldItemCount) implements CustomPayload {
        public static final CustomPayload.Id<TrashCanActionC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "trashcan_action")
        );
        public static final PacketCodec<PacketByteBuf, TrashCanActionC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeInt(value.action); buf.writeString(value.itemId); buf.writeInt(value.count); buf.writeInt(value.oldItemValue); buf.writeInt(value.oldItemCount); },
            buf -> new TrashCanActionC2SPayload(buf.readInt(), buf.readString(), buf.readInt(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 服务端→客户端：垃圾桶回收结果同步 */
    public record TrashCanSyncS2CPayload(int earnedGold) implements CustomPayload {
        public static final CustomPayload.Id<TrashCanSyncS2CPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "trashcan_sync")
        );
        public static final PacketCodec<PacketByteBuf, TrashCanSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeInt(value.earnedGold),
            buf -> new TrashCanSyncS2CPayload(buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 服务端→客户端：垃圾桶当前物品数据同步 */
    public record TrashCanDataSyncS2CPayload(String itemId, int count, int earnedGold) implements CustomPayload {
        public static final CustomPayload.Id<TrashCanDataSyncS2CPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "trashcan_data_sync")
        );
        public static final PacketCodec<PacketByteBuf, TrashCanDataSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> { buf.writeString(value.itemId()); buf.writeInt(value.count()); buf.writeInt(value.earnedGold()); },
            buf -> new TrashCanDataSyncS2CPayload(buf.readString(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 客户端→服务端：请求垃圾桶当前物品数据 */
    public record TrashCanDataRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<TrashCanDataRequestC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "trashcan_data_req")
        );
        public static final PacketCodec<PacketByteBuf, TrashCanDataRequestC2SPayload> CODEC = PacketCodec.of(
            (v, b) -> {}, b -> new TrashCanDataRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 垃圾桶升级 ======

    public record TrashCanLevelRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<TrashCanLevelRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "trashcan_level_req"));
        public static final PacketCodec<PacketByteBuf, TrashCanLevelRequestC2SPayload> CODEC = PacketCodec.of((v, b) -> {}, b -> new TrashCanLevelRequestC2SPayload());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record TrashCanLevelSyncS2CPayload(int level) implements CustomPayload {
        public static final CustomPayload.Id<TrashCanLevelSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "trashcan_level_sync"));
        public static final PacketCodec<PacketByteBuf, TrashCanLevelSyncS2CPayload> CODEC = PacketCodec.of(
            (v, b) -> b.writeInt(v.level), b -> new TrashCanLevelSyncS2CPayload(b.readInt()));
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record TrashCanUpgradeC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<TrashCanUpgradeC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "trashcan_upgrade"));
        public static final PacketCodec<PacketByteBuf, TrashCanUpgradeC2SPayload> CODEC = PacketCodec.of((v, b) -> {}, b -> new TrashCanUpgradeC2SPayload());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 桑迪商店 ======

    public static class SandyShopStockItem {
        public String itemId;
        public int price;
        public int maxBuy;
        public int available;

        public SandyShopStockItem(String itemId, int price, int maxBuy, int available) {
            this.itemId = itemId;
            this.price = price;
            this.maxBuy = maxBuy;
            this.available = available;
        }
    }

    public record SandyShopStockRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<SandyShopStockRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "sandy_shop_stock_request"));
        public static final PacketCodec<PacketByteBuf, SandyShopStockRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new SandyShopStockRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record SandyShopStockSyncS2CPayload(List<SandyShopStockItem> items) implements CustomPayload {
        public static final CustomPayload.Id<SandyShopStockSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "sandy_shop_stock_sync"));
        public static final PacketCodec<PacketByteBuf, SandyShopStockSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.items.size());
                for (SandyShopStockItem item : value.items) {
                    buf.writeString(item.itemId);
                    buf.writeInt(item.price);
                    buf.writeInt(item.maxBuy);
                    buf.writeInt(item.available);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<SandyShopStockItem> list = new java.util.ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(new SandyShopStockItem(buf.readString(), buf.readInt(), buf.readInt(), buf.readInt()));
                }
                return new SandyShopStockSyncS2CPayload(list);
            }
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record SandyShopBuyC2SPayload(String itemId, int count, int price) implements CustomPayload {
        public static final CustomPayload.Id<SandyShopBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "sandy_shop_buy"));
        public static final PacketCodec<PacketByteBuf, SandyShopBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.itemId);
                buf.writeInt(value.count);
                buf.writeInt(value.price);
            },
            buf -> new SandyShopBuyC2SPayload(buf.readString(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ===== Crab Pot Bait =====
    public record CrabPotOpenC2SPayload(BlockPos pos) implements CustomPayload {
        public static final CustomPayload.Id<CrabPotOpenC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "crab_pot_open"));
        public static final PacketCodec<PacketByteBuf, CrabPotOpenC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBlockPos(value.pos()),
            buf -> new CrabPotOpenC2SPayload(buf.readBlockPos())
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 弹弓打开（ScreenHandler 模式） ======
    public record SlingshotOpenC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<SlingshotOpenC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "slingshot_open")
        );
        public static final PacketCodec<PacketByteBuf, SlingshotOpenC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new SlingshotOpenC2SPayload()
        );
        @Override
        public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 书店系统 ======

    public static class BookStoreItem {
        public String itemId;
        public int price;
        public boolean isBarter;
        public String requiredItemId;
        public int requiredCount;
        public String requiredItemId2;
        public int requiredCount2;
        public int resultCount;

        public BookStoreItem(String itemId, int price, boolean isBarter, String requiredItemId, int requiredCount, String requiredItemId2, int requiredCount2) {
            this(itemId, price, isBarter, requiredItemId, requiredCount, requiredItemId2, requiredCount2, 1);
        }

        public BookStoreItem(String itemId, int price, boolean isBarter, String requiredItemId, int requiredCount, String requiredItemId2, int requiredCount2, int resultCount) {
            this.itemId = itemId;
            this.price = price;
            this.isBarter = isBarter;
            this.requiredItemId = requiredItemId;
            this.requiredCount = requiredCount;
            this.requiredItemId2 = requiredItemId2;
            this.requiredCount2 = requiredCount2;
            this.resultCount = resultCount;
        }
    }

    public record BookStoreStockRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<BookStoreStockRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "book_store_stock_request"));
        public static final PacketCodec<PacketByteBuf, BookStoreStockRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new BookStoreStockRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BookStoreStockSyncS2CPayload(List<BookStoreItem> items) implements CustomPayload {
        public static final CustomPayload.Id<BookStoreStockSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "book_store_stock_sync"));
        public static final PacketCodec<PacketByteBuf, BookStoreStockSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.items.size());
                for (BookStoreItem item : value.items) {
                    buf.writeString(item.itemId);
                    buf.writeInt(item.price);
                    buf.writeBoolean(item.isBarter);
                    writeNullableString(buf, item.requiredItemId);
                    buf.writeInt(item.requiredCount);
                    writeNullableString(buf, item.requiredItemId2);
                    buf.writeInt(item.requiredCount2);
                    buf.writeInt(item.resultCount);
                }
            },
            buf -> {
                int size = buf.readInt();
                List<BookStoreItem> list = new ArrayList<>();
                for (int i = 0; i < size; i++) {
                    list.add(new BookStoreItem(
                        buf.readString(), buf.readInt(), buf.readBoolean(),
                        readNullableString(buf), buf.readInt(), readNullableString(buf), buf.readInt(),
                        buf.readInt()
                    ));
                }
                return new BookStoreStockSyncS2CPayload(list);
            }
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BookStoreBuyC2SPayload(String itemId, int count, int price) implements CustomPayload {
        public static final CustomPayload.Id<BookStoreBuyC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "book_store_buy"));
        public static final PacketCodec<PacketByteBuf, BookStoreBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.itemId);
                buf.writeInt(value.count);
                buf.writeInt(value.price);
            },
            buf -> new BookStoreBuyC2SPayload(buf.readString(), buf.readInt(), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    public record BookStoreBarterC2SPayload(String resultItemId, int count, String reqItemId, int reqCount, String reqItemId2, int reqCount2) implements CustomPayload {
        public static final CustomPayload.Id<BookStoreBarterC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "book_store_barter"));
        public static final PacketCodec<PacketByteBuf, BookStoreBarterC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeString(value.resultItemId);
                buf.writeInt(value.count);
                writeNullableString(buf, value.reqItemId);
                buf.writeInt(value.reqCount);
                writeNullableString(buf, value.reqItemId2);
                buf.writeInt(value.reqCount2);
            },
            buf -> new BookStoreBarterC2SPayload(buf.readString(), buf.readInt(), readNullableString(buf), buf.readInt(), readNullableString(buf), buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端→服务端：请求书店营业状态
    public record BookStoreOpenRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<BookStoreOpenRequestC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "book_store_open_request"));
        public static final PacketCodec<PacketByteBuf, BookStoreOpenRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new BookStoreOpenRequestC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 服务端→客户端：书店营业状态同步
    public record BookStoreOpenSyncS2CPayload(boolean open) implements CustomPayload {
        public static final CustomPayload.Id<BookStoreOpenSyncS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(StardewValley.MOD_ID, "book_store_open_sync"));
        public static final PacketCodec<PacketByteBuf, BookStoreOpenSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeBoolean(value.open),
            buf -> new BookStoreOpenSyncS2CPayload(buf.readBoolean())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 扩展背包系统 ======

    /** 客户端→服务端：请求打开扩展背包（服务端通过 ScreenHandler 打开） */
    public record BackpackOpenC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<BackpackOpenC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "backpack_open")
        );
        public static final PacketCodec<PacketByteBuf, BackpackOpenC2SPayload> CODEC = PacketCodec.of(
            (v, b) -> {}, b -> new BackpackOpenC2SPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 服务端→客户端：背包等级同步（用于 ShopScreen） */
    public record BackpackDataSyncS2CPayload(net.minecraft.nbt.NbtCompound data) implements CustomPayload {
        public static final CustomPayload.Id<BackpackDataSyncS2CPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "backpack_data_sync")
        );
        public static final PacketCodec<PacketByteBuf, BackpackDataSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeNbt(value.data),
            buf -> new BackpackDataSyncS2CPayload(buf.readNbt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    /** 客户端→服务端：从商店购买背包升级 */
    public record BackpackBuyC2SPayload(String upgradeType) implements CustomPayload {
        public static final CustomPayload.Id<BackpackBuyC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "backpack_buy")
        );
        public static final PacketCodec<PacketByteBuf, BackpackBuyC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeString(value.upgradeType),
            buf -> new BackpackBuyC2SPayload(buf.readString())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 博物馆捐赠系统 ======

    // 客户端请求博物馆捐赠数据
    public record MuseumDataRequestC2SPayload() implements CustomPayload {
        public static final CustomPayload.Id<MuseumDataRequestC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "museum_data_request"));
        public static final PacketCodec<PacketByteBuf, MuseumDataRequestC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new MuseumDataRequestC2SPayload());
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 服务端同步博物馆捐赠数据给客户端
    public record MuseumDataSyncS2CPayload(
        java.util.List<String> donatedItems,
        java.util.Set<Integer> claimedRewardIndices,
        java.util.Set<String> abilities
    ) implements CustomPayload {
        public static final CustomPayload.Id<MuseumDataSyncS2CPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "museum_data_sync"));
        public static final PacketCodec<PacketByteBuf, MuseumDataSyncS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {
                buf.writeInt(value.donatedItems.size());
                for (String s : value.donatedItems) buf.writeString(s);
                buf.writeInt(value.claimedRewardIndices.size());
                for (int i : value.claimedRewardIndices) buf.writeInt(i);
                buf.writeInt(value.abilities.size());
                for (String s : value.abilities) buf.writeString(s);
            },
            buf -> {
                int itemCount = buf.readInt();
                java.util.List<String> items = new java.util.ArrayList<>();
                for (int i = 0; i < itemCount; i++) items.add(buf.readString());
                int claimCount = buf.readInt();
                java.util.Set<Integer> claimed = new java.util.HashSet<>();
                for (int i = 0; i < claimCount; i++) claimed.add(buf.readInt());
                int abilCount = buf.readInt();
                java.util.Set<String> abilities = new java.util.HashSet<>();
                for (int i = 0; i < abilCount; i++) abilities.add(buf.readString());
                return new MuseumDataSyncS2CPayload(items, claimed, abilities);
            }
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端→服务端：捐赠物品
    public record MuseumDonateC2SPayload(int slotIndex) implements CustomPayload {
        public static final CustomPayload.Id<MuseumDonateC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "museum_donate"));
        public static final PacketCodec<PacketByteBuf, MuseumDonateC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeInt(value.slotIndex),
            buf -> new MuseumDonateC2SPayload(buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // 客户端→服务端：领取奖励
    public record MuseumClaimRewardC2SPayload(int rewardIndex) implements CustomPayload {
        public static final CustomPayload.Id<MuseumClaimRewardC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "museum_claim_reward"));
        public static final PacketCodec<PacketByteBuf, MuseumClaimRewardC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeInt(value.rewardIndex),
            buf -> new MuseumClaimRewardC2SPayload(buf.readInt())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 矿物商店加工服务 ======
    // inputType: geode/frozen_geode/magma_geode/omni_geode/ancient_treasure_decor/golden_coconut/mystery_box/golden_mystery_box
    public record GeodeProcessC2SPayload(String inputType) implements CustomPayload {
        public static final CustomPayload.Id<GeodeProcessC2SPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "geode_process"));
        public static final PacketCodec<PacketByteBuf, GeodeProcessC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> buf.writeString(value.inputType),
            buf -> new GeodeProcessC2SPayload(buf.readString())
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }

    // ====== 任务书（服务端→客户端：打开成书界面）======
    public record GuideBookOpenS2CPayload() implements CustomPayload {
        public static final CustomPayload.Id<GuideBookOpenS2CPayload> ID = new CustomPayload.Id<>(
            Identifier.of(StardewValley.MOD_ID, "guide_book_open"));
        public static final PacketCodec<PacketByteBuf, GuideBookOpenS2CPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            buf -> new GuideBookOpenS2CPayload()
        );
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
}