package stardewvalley.modid;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stardewvalley.modid.block.ArtisanEquipmentState;
import stardewvalley.modid.block.SolarPanelState;
import stardewvalley.modid.block.LightningRodBlock;
import stardewvalley.modid.block.LightningRodState;
import stardewvalley.modid.block.TapperState;
import stardewvalley.modid.block.BaseCropBlock;
import stardewvalley.modid.block.BombBlock;
import stardewvalley.modid.block.CrabPotHandler;
import stardewvalley.modid.block.DeluxeWormBinHandler;
import stardewvalley.modid.block.MushroomStumpState;
import stardewvalley.modid.block.ModBlockEntities;

import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import stardewvalley.modid.block.CropPositionState;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.season.ForagingLevelManager;
import stardewvalley.modid.block.ModFarmlandBlock;
import stardewvalley.modid.block.SprinklerBlock;
import stardewvalley.modid.block.SprinklerState;
import stardewvalley.modid.block.StardewGrassBlock;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.effect.SkillBuffHelper;
import stardewvalley.modid.equipment.EquipmentActionType;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.equipment.EquipmentStateManager;
import stardewvalley.modid.equipment.TrashCanStateManager;
import stardewvalley.modid.equipment.BackpackStateManager;
import stardewvalley.modid.equipment.BurglarRingLootHandler;
import stardewvalley.modid.equipment.RingEffectHandler;
import stardewvalley.modid.gui.BundleManager;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.gui.GoldManager;
import stardewvalley.modid.gui.BackpackScreenHandler;
import stardewvalley.modid.gui.CrabPotBaitScreenHandler;
import stardewvalley.modid.gui.SlingshotScreenHandler;
import stardewvalley.modid.gui.RodScreenHandler;
import stardewvalley.modid.gui.ModScreenHandlers;
import stardewvalley.modid.gui.MarlonTradeManager;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.gui.OreToolUpgradeManager;
import stardewvalley.modid.gui.TravelingCartManager;
import stardewvalley.modid.gui.SandyShopManager;
import stardewvalley.modid.gui.KrobusShopManager;
import stardewvalley.modid.gui.BookStoreManager;
import stardewvalley.modid.gui.MuseumManager;
import stardewvalley.modid.item.ModFishingRodItem;
import stardewvalley.modid.AnimalFriendshipManager;
import stardewvalley.modid.item.AnimalInteractionHandler;
import stardewvalley.modid.item.GoldenAnimalCrackerItem;
import stardewvalley.modid.item.ModItems;
import stardewvalley.modid.item.ModItemGroups;
import stardewvalley.modid.item.RodComponent;
import stardewvalley.modid.item.StardropItem;
import stardewvalley.modid.season.CombatLevelManager;
import stardewvalley.modid.season.LuckManager;
import stardewvalley.modid.season.MiningLevelManager;
import stardewvalley.modid.season.Season;
import stardewvalley.modid.skill.SkillEffectHelper;
import stardewvalley.modid.weather.WeatherCalculator;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;
import stardewvalley.modid.entity.ModEntities;
import stardewvalley.modid.crafting.CraftingMaterialSets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StardewValley implements ModInitializer {
	public static final String MOD_ID = "stardewvalley";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final Map<RegistryKey<World>, Long> LAST_DAY = new HashMap<>();
	private static final Set<UUID> INITIAL_ITEMS_GIVEN = new HashSet<>();
	private static final Set<BlockPos> BREAKING_GRASS = new HashSet<>();

	@Override
	public void onInitialize() {
		ModItems.registerAll();
		ModBlocks.registerAll();
		ModItemGroups.registerAll();
		ModBlockEntities.registerAll();
		ModEntities.registerAll();
		AnimalInteractionHandler.register();
		// 注册金色动物饼干事件
		GoldenAnimalCrackerItem.registerEvents();

		// 注册钓竿数据组件
		net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "rod_data"),
			RodComponent.TYPE
		);

		// 注册弹弓数据组件
		net.minecraft.registry.Registry.register(
			net.minecraft.registry.Registries.DATA_COMPONENT_TYPE,
			Identifier.of(MOD_ID, "slingshot_data"),
			stardewvalley.modid.item.SlingshotComponent.TYPE
		);

		registerPayloads();
		registerServerHandlers();
		registerSkillHandlers();
		registerMasteryHandlers();
		registerDailyLogic();
		registerTapperBreakHandler();
		registerPositionRestore();
		registerArtisanEquipmentTick();
		registerStardropSaturationLogic();
		registerCommands();
		registerBlockEvents();
		registerGrassEvents();
		registerIridiumScytheCropHarvest();
		registerCombatEvents();
		registerChickenFeeding();
		registerEquipmentHandler();
		registerTrashCanHandler();
		ModScreenHandlers.registerAll();
		registerBackpackHandler();
		registerMuseumHandler();
		registerRingEffects();
		BurglarRingLootHandler.register();
		ModStatusEffects.register();

		stardewvalley.modid.season.ForagingLootModifier.register();
		registerWelcomeMessage();
		LOGGER.info("Stardew Valley crops mod initialized!");
	}

	private void registerPayloads() {
		PayloadTypeRegistry.playC2S().register(ModPayloads.GoldRequestC2SPayload.ID, ModPayloads.GoldRequestC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.ShippingDataRequestC2SPayload.ID, ModPayloads.ShippingDataRequestC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.AddToShippingC2SPayload.ID, ModPayloads.AddToShippingC2SPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(ModPayloads.ShopBuyC2SPayload.ID, ModPayloads.ShopBuyC2SPayload.CODEC);

		PayloadTypeRegistry.playS2C().register(ModPayloads.GoldSyncS2CPayload.ID, ModPayloads.GoldSyncS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.AnimalCatalogueDiscountS2CPayload.ID, ModPayloads.AnimalCatalogueDiscountS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.PriceCatalogueSyncS2CPayload.ID, ModPayloads.PriceCatalogueSyncS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.TreasureAppraisalSyncS2CPayload.ID, ModPayloads.TreasureAppraisalSyncS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.ShippingDataSyncS2CPayload.ID, ModPayloads.ShippingDataSyncS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(ModPayloads.FarmingSyncS2CPayload.ID, ModPayloads.FarmingSyncS2CPayload.CODEC);

		PayloadTypeRegistry.playC2S().register(ModPayloads.FarmingRequestC2SPayload.ID, ModPayloads.FarmingRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.ForagingSyncS2CPayload.ID, ModPayloads.ForagingSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.ForagingRequestC2SPayload.ID, ModPayloads.ForagingRequestC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ModPayloads.FishingSyncS2CPayload.ID, ModPayloads.FishingSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.FishingRequestC2SPayload.ID, ModPayloads.FishingRequestC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ModPayloads.MiningSyncS2CPayload.ID, ModPayloads.MiningSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.MiningRequestC2SPayload.ID, ModPayloads.MiningRequestC2SPayload.CODEC);

        PayloadTypeRegistry.playC2S().register(ModPayloads.OreShopBuyC2SPayload.ID, ModPayloads.OreShopBuyC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.OreShopSellC2SPayload.ID, ModPayloads.OreShopSellC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.FishShopSellC2SPayload.ID, ModPayloads.FishShopSellC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.FishShopBuyC2SPayload.ID, ModPayloads.FishShopBuyC2SPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(ModPayloads.CombatSyncS2CPayload.ID, ModPayloads.CombatSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.CombatRequestC2SPayload.ID, ModPayloads.CombatRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.ToolUpgradeRequestC2SPayload.ID, ModPayloads.ToolUpgradeRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.ToolUpgradeStatusRequestC2SPayload.ID, ModPayloads.ToolUpgradeStatusRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.ToolUpgradeStatusSyncS2CPayload.ID, ModPayloads.ToolUpgradeStatusSyncS2CPayload.CODEC);

        // 始终注册钓鱼小游戏网络包（由命令切换模式）
        stardewvalley.modid.fishing.FishingNetworking.register();

        // 钓竿ScreenHandler打开
        PayloadTypeRegistry.playC2S().register(ModPayloads.RodScreenOpenC2SPayload.ID, ModPayloads.RodScreenOpenC2SPayload.CODEC);

        // 弹弓打开（ScreenHandler 模式）
        PayloadTypeRegistry.playC2S().register(ModPayloads.SlingshotOpenC2SPayload.ID, ModPayloads.SlingshotOpenC2SPayload.CODEC);

        // 运气同步
        PayloadTypeRegistry.playS2C().register(ModPayloads.LuckSyncS2CPayload.ID, ModPayloads.LuckSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.LuckRequestC2SPayload.ID, ModPayloads.LuckRequestC2SPayload.CODEC);

        // 天气同步
        PayloadTypeRegistry.playS2C().register(ModPayloads.WeatherSyncS2CPayload.ID, ModPayloads.WeatherSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.WeatherRequestC2SPayload.ID, ModPayloads.WeatherRequestC2SPayload.CODEC);

        // 图腾动画
        PayloadTypeRegistry.playS2C().register(ModPayloads.FloatingItemS2CPayload.ID, ModPayloads.FloatingItemS2CPayload.CODEC);

        // 武器技能
        PayloadTypeRegistry.playC2S().register(ModPayloads.WeaponSkillC2SPayload.ID, ModPayloads.WeaponSkillC2SPayload.CODEC);

        // 工作台合成
        PayloadTypeRegistry.playC2S().register(ModPayloads.WorkbenchCraftC2SPayload.ID, ModPayloads.WorkbenchCraftC2SPayload.CODEC);

        // 沙漠商人交易
        PayloadTypeRegistry.playC2S().register(ModPayloads.DesertTraderBuyC2SPayload.ID, ModPayloads.DesertTraderBuyC2SPayload.CODEC);

        // 马龙武器商店以物换物
        PayloadTypeRegistry.playC2S().register(ModPayloads.MarlonBarterC2SPayload.ID, ModPayloads.MarlonBarterC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.MarlonTradeStateRequestC2SPayload.ID, ModPayloads.MarlonTradeStateRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.MarlonTradeStateSyncS2CPayload.ID, ModPayloads.MarlonTradeStateSyncS2CPayload.CODEC);

        // 技能系统
        PayloadTypeRegistry.playS2C().register(ModPayloads.SkillSyncS2CPayload.ID, ModPayloads.SkillSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.SkillRequestC2SPayload.ID, ModPayloads.SkillRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.SkillSelectC2SPayload.ID, ModPayloads.SkillSelectC2SPayload.CODEC);

        // 精通系统
        PayloadTypeRegistry.playS2C().register(ModPayloads.MasterySyncS2CPayload.ID, ModPayloads.MasterySyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.MasteryRequestC2SPayload.ID, ModPayloads.MasteryRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.MasterySelectC2SPayload.ID, ModPayloads.MasterySelectC2SPayload.CODEC);

        // 旅行货车
        PayloadTypeRegistry.playC2S().register(ModPayloads.TravelingCartStockRequestC2SPayload.ID, ModPayloads.TravelingCartStockRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.TravelingCartStockSyncS2CPayload.ID, ModPayloads.TravelingCartStockSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.TravelingCartBuyC2SPayload.ID, ModPayloads.TravelingCartBuyC2SPayload.CODEC);

        // 桑迪商店
        PayloadTypeRegistry.playC2S().register(ModPayloads.SandyShopStockRequestC2SPayload.ID, ModPayloads.SandyShopStockRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.SandyShopStockSyncS2CPayload.ID, ModPayloads.SandyShopStockSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.SandyShopBuyC2SPayload.ID, ModPayloads.SandyShopBuyC2SPayload.CODEC);

        // 科罗布斯商店
        PayloadTypeRegistry.playC2S().register(ModPayloads.KrobusDataRequestC2SPayload.ID, ModPayloads.KrobusDataRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.KrobusDataSyncS2CPayload.ID, ModPayloads.KrobusDataSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.KrobusBuyC2SPayload.ID, ModPayloads.KrobusBuyC2SPayload.CODEC);

        // 收集包系统
        PayloadTypeRegistry.playC2S().register(ModPayloads.BundleDataRequestC2SPayload.ID, ModPayloads.BundleDataRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.BundleDataSyncS2CPayload.ID, ModPayloads.BundleDataSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BundleSubmitItemC2SPayload.ID, ModPayloads.BundleSubmitItemC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BundleClaimRewardC2SPayload.ID, ModPayloads.BundleClaimRewardC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BundleVaultBuyC2SPayload.ID, ModPayloads.BundleVaultBuyC2SPayload.CODEC);

        // 额外装备栏
        PayloadTypeRegistry.playC2S().register(ModPayloads.EquipmentActionC2SPayload.ID, ModPayloads.EquipmentActionC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.EquipmentSyncS2CPayload.ID, ModPayloads.EquipmentSyncS2CPayload.CODEC);

        // 垃圾桶
        PayloadTypeRegistry.playC2S().register(ModPayloads.TrashCanActionC2SPayload.ID, ModPayloads.TrashCanActionC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.TrashCanSyncS2CPayload.ID, ModPayloads.TrashCanSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.TrashCanDataRequestC2SPayload.ID, ModPayloads.TrashCanDataRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.TrashCanDataSyncS2CPayload.ID, ModPayloads.TrashCanDataSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.TrashCanLevelRequestC2SPayload.ID, ModPayloads.TrashCanLevelRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.TrashCanLevelSyncS2CPayload.ID, ModPayloads.TrashCanLevelSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.TrashCanUpgradeC2SPayload.ID, ModPayloads.TrashCanUpgradeC2SPayload.CODEC);

        // 蟹笼操作/数据同步
        PayloadTypeRegistry.playC2S().register(ModPayloads.CrabPotOpenC2SPayload.ID, ModPayloads.CrabPotOpenC2SPayload.CODEC);

        // 书店
        PayloadTypeRegistry.playC2S().register(ModPayloads.BookStoreStockRequestC2SPayload.ID, ModPayloads.BookStoreStockRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.BookStoreStockSyncS2CPayload.ID, ModPayloads.BookStoreStockSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BookStoreBuyC2SPayload.ID, ModPayloads.BookStoreBuyC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BookStoreBarterC2SPayload.ID, ModPayloads.BookStoreBarterC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BookStoreOpenRequestC2SPayload.ID, ModPayloads.BookStoreOpenRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.BookStoreOpenSyncS2CPayload.ID, ModPayloads.BookStoreOpenSyncS2CPayload.CODEC);

        // 扩展背包（ScreenHandler 自动处理槽位同步）
        PayloadTypeRegistry.playC2S().register(ModPayloads.BackpackOpenC2SPayload.ID, ModPayloads.BackpackOpenC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.BackpackDataSyncS2CPayload.ID, ModPayloads.BackpackDataSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.BackpackBuyC2SPayload.ID, ModPayloads.BackpackBuyC2SPayload.CODEC);

        // 博物馆捐赠系统
        PayloadTypeRegistry.playC2S().register(ModPayloads.MuseumDataRequestC2SPayload.ID, ModPayloads.MuseumDataRequestC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.MuseumDataSyncS2CPayload.ID, ModPayloads.MuseumDataSyncS2CPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.MuseumDonateC2SPayload.ID, ModPayloads.MuseumDonateC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.MuseumClaimRewardC2SPayload.ID, ModPayloads.MuseumClaimRewardC2SPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ModPayloads.GeodeProcessC2SPayload.ID, ModPayloads.GeodeProcessC2SPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ModPayloads.GuideBookOpenS2CPayload.ID, ModPayloads.GuideBookOpenS2CPayload.CODEC);

	}

	private void registerServerHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.GoldRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				GoldManager manager = GoldManager.get(world);
				ServerPlayNetworking.send(player, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
				boolean hasDiscount = BookDataManager.get(world).hasUsedBook(player.getUuid(), "animal_catalogue");
				ServerPlayNetworking.send(player, new ModPayloads.AnimalCatalogueDiscountS2CPayload(hasDiscount));
				boolean priceCatalogueActive = BookDataManager.get(world).hasUsedBook(player.getUuid(), "price_catalogue");
				ServerPlayNetworking.send(player, new ModPayloads.PriceCatalogueSyncS2CPayload(priceCatalogueActive));
				boolean treasureAppraisalActive = BookDataManager.get(world).hasUsedBook(player.getUuid(), "treasure_appraisal_guide");
				ServerPlayNetworking.send(player, new ModPayloads.TreasureAppraisalSyncS2CPayload(treasureAppraisalActive));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.LuckRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld sw = (ServerWorld) context.player().getEntityWorld();
				LuckManager lm = LuckManager.get(sw);
				long currentDay = sw.getTimeOfDay() / 24000L;
				float luck = lm.getDailyLuck(currentDay);
				ServerPlayNetworking.send(context.player(), new ModPayloads.LuckSyncS2CPayload(luck));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.ShippingDataRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				GoldManager manager = GoldManager.get((ServerWorld) context.player().getEntityWorld());
				ServerPlayNetworking.send(context.player(), new ModPayloads.ShippingDataSyncS2CPayload(
					java.util.List.copyOf(manager.getShippingItems())
				));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.FarmingRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				stardewvalley.modid.season.FarmingLevelManager flm = stardewvalley.modid.season.FarmingLevelManager.get(sw);
				int baseLevel = flm.getLevel(player.getUuid());
				int effectiveLevel = SkillBuffHelper.getEffectiveFarmingLevel(sw, player.getUuid(), player);
				int xp = flm.getXp(player.getUuid());
				int[] thresholds = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
				int nextXp = baseLevel < 10 ? thresholds[baseLevel] : 15000;
				ServerPlayNetworking.send(player, new ModPayloads.FarmingSyncS2CPayload(effectiveLevel, xp, nextXp));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.ForagingRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				stardewvalley.modid.season.ForagingLevelManager flm = stardewvalley.modid.season.ForagingLevelManager.get(sw);
				int baseLevel = flm.getLevel(player.getUuid());
				int effectiveLevel = SkillBuffHelper.getEffectiveForagingLevel(sw, player.getUuid(), player);
				int xp = flm.getXp(player.getUuid());
				int[] thresholds = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
				int nextXp = baseLevel < 10 ? thresholds[baseLevel] : 15000;
				ServerPlayNetworking.send(player, new ModPayloads.ForagingSyncS2CPayload(effectiveLevel, xp, nextXp));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.FishingRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				stardewvalley.modid.season.FishingLevelManager flm = stardewvalley.modid.season.FishingLevelManager.get(sw);
				int baseLevel = flm.getLevel(player.getUuid());
				int effectiveLevel = SkillBuffHelper.getEffectiveFishingLevel(sw, player.getUuid(), player);
				int xp = flm.getXp(player.getUuid());
				int[] thresholds = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
				int nextXp = baseLevel < 10 ? thresholds[baseLevel] : 15000;
				ServerPlayNetworking.send(player, new ModPayloads.FishingSyncS2CPayload(effectiveLevel, xp, nextXp));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MiningRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				stardewvalley.modid.season.MiningLevelManager mlm = stardewvalley.modid.season.MiningLevelManager.get(sw);
				int baseLevel = mlm.getLevel(player.getUuid());
				int effectiveLevel = SkillBuffHelper.getEffectiveMiningLevel(sw, player.getUuid(), player);
				int xp = mlm.getXp(player.getUuid());
				int[] thresholds = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
				int nextXp = baseLevel < 10 ? thresholds[baseLevel] : 15000;
				ServerPlayNetworking.send(player, new ModPayloads.MiningSyncS2CPayload(effectiveLevel, xp, nextXp));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.CombatRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld sw = (ServerWorld) context.player().getEntityWorld();
				CombatLevelManager clm = CombatLevelManager.get(sw);
				int level = clm.getLevel(context.player().getUuid());
				int xp = clm.getXp(context.player().getUuid());
				int[] thresholds = {100, 380, 770, 1300, 2150, 3300, 4800, 6900, 10000, 15000};
				int nextXp = level < 10 ? thresholds[level] : 15000;
				ServerPlayNetworking.send(context.player(), new ModPayloads.CombatSyncS2CPayload(level, xp, nextXp));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.ShopBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld shopWorld = (ServerWorld) player.getEntityWorld();
				GoldManager manager = GoldManager.get(shopWorld);
				int price = getShopPrice(payload.itemId(), shopWorld.getTimeOfDay());
				// 动物目录：玛尼的牲畜售价降低20%
				boolean isLivestock = payload.itemId().contains("spawn_egg");
				if (isLivestock && BookDataManager.get(shopWorld).hasUsedBook(player.getUuid(), "animal_catalogue")) {
					price = Math.round(price * 0.8f);
				}
				int maxBuy = getShopMaxBuy(payload.itemId());
				int count = maxBuy > 0 ? Math.min(payload.count(), maxBuy) : payload.count();
				int totalCost = price * count;
				if (manager.getGold() >= totalCost) {
					manager.addGold(-totalCost);
					Identifier itemIdentifier;
					if (payload.itemId().contains(":")) {
						itemIdentifier = Identifier.of(payload.itemId());
					} else {
						itemIdentifier = Identifier.of(MOD_ID, payload.itemId());
					}
					net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(itemIdentifier);
					if (item != null) {
						player.getInventory().offerOrDrop(new net.minecraft.item.ItemStack(item, count));
					}
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.OreShopBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				GoldManager manager = GoldManager.get((ServerWorld) context.player().getEntityWorld());
				long totalDays = context.player().getEntityWorld().getTimeOfDay() / 24000L;
				boolean secondYear = totalDays > 72;
				int price = switch (payload.itemId()) {
					case "copper_ore" -> secondYear ? 150 : 75;
					case "iron_ore" -> secondYear ? 250 : 150;
					case "coal" -> secondYear ? 250 : 150;
					case "gold_ore" -> secondYear ? 750 : 400;
					default -> 0;
				};
				int totalCost = price * payload.count();
				if (manager.getGold() >= totalCost) {
					manager.addGold(-totalCost);
					net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, payload.itemId()));
					if (item != null) {
						context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(item, payload.count()));
					}
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.OreShopSellC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld oreSellWorld = (ServerWorld) player.getEntityWorld();
				int slotIndex = payload.slotIndex();
				int invIndex = slotIndex < 9 ? slotIndex + 27 : slotIndex - 9;
				ItemStack stack = player.getInventory().getStack(invIndex);
				if (stack.isEmpty()) return;
				Identifier itemId = Registries.ITEM.getId(stack.getItem());
				if (!itemId.getNamespace().equals(MOD_ID)) return;

				GoldManager manager = GoldManager.get(oreSellWorld);
				int price = getOreSellPrice(itemId.getPath());
				// 古代珍宝鉴定指南：古物售价4倍
				if (GoldManager.isArtifactItem(itemId)) {
					price = Math.round(price * GoldManager.getArtifactPriceMultiplier(player.getUuid(), oreSellWorld));
				}
				if (price <= 0) return;

				int count = Math.min(payload.count(), stack.getCount());
				manager.addGold(price * count);
				stack.decrement(count);
				if (stack.isEmpty()) {
					player.getInventory().setStack(invIndex, ItemStack.EMPTY);
				}
				player.getInventory().markDirty();
				for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.FishShopSellC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				int slotIndex = payload.slotIndex();
				int invIndex = slotIndex < 9 ? slotIndex + 27 : slotIndex - 9;
				ItemStack stack = player.getInventory().getStack(invIndex);
				if (stack.isEmpty()) return;
				Identifier itemId = Registries.ITEM.getId(stack.getItem());
				if (!itemId.getNamespace().equals(MOD_ID)) return;
				if (!stardewvalley.modid.skill.SkillEffectHelper.isFishItem(itemId)) return;

				GoldManager manager = GoldManager.get((ServerWorld) player.getEntityWorld());
				int price = GoldManager.getItemMoneyValue(itemId);
				if (price <= 0) return;

				int count = Math.min(payload.count(), stack.getCount());
				manager.addGold(price * count);
				stack.decrement(count);
				if (stack.isEmpty()) {
					player.getInventory().setStack(invIndex, ItemStack.EMPTY);
				}
				player.getInventory().markDirty();
				for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.FishShopBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				GoldManager manager = GoldManager.get((ServerWorld) context.player().getEntityWorld());
				int price = switch (payload.itemId()) {
					case "crab_pot" -> 1500;
					case "fish_smoker" -> 5000;
					case "trout_soup" -> 250;
					case "bait_bait" -> 5;
					case "bait_deluxe_bait" -> 100;
					case "fishtool_sonar_bobber" -> 500;
					case "fishtool_spinner" -> 500;
					case "fishtool_trap_bobber" -> 500;
					case "fishtool_lead_bobber" -> 200;
					case "fishtool_treasure_hunter" -> 750;
					case "fishtool_cork_bobber" -> 750;
					case "fishtool_barbed_hook" -> 1000;
					case "fishtool_dressed_spinner" -> 1000;
					case "bait_magnet" -> 1000;
					case "bamboo_pole" -> 500;
					case "fiberglass_rod" -> 1800;
					case "iridium_rod" -> 7500;
					case "advanced_iridium_rod" -> 25000;
					default -> {
						// 针对性鱼饵：价格 = bait售价 × 2
						if (payload.itemId().startsWith("bait_") && payload.itemId().endsWith("_bait")) {
							int basePrice = GoldManager.getItemMoneyValue(net.minecraft.util.Identifier.of(MOD_ID, payload.itemId()));
							yield basePrice > 0 ? basePrice * 2 : 0;
						}
						yield 0;
					}
				};
				int totalCost = price * payload.count();
				if (totalCost > 0 && manager.getGold() >= totalCost) {
					manager.addGold(-totalCost);
					net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, payload.itemId()));
					if (item != null) {
						context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(item, payload.count()));
					}
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.ToolUpgradeRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				OreToolUpgradeManager mgr = OreToolUpgradeManager.get(world);

				if ("pickup".equals(payload.toolType())) {
					OreToolUpgradeManager.InternalEntry entry = mgr.completeUpgrade(context.player().getUuid());
					if (entry != null) {
						String itemName = entry.targetTier + "_" + entry.toolType;
						net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, itemName));
						if (item != null) {
							context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(item));
						}
					}
					return;
				}

				if (mgr.hasUpgradeInProgress(context.player().getUuid())) return;

				String toolType = payload.toolType();
				String currentTier = getPlayerToolTier(context.player(), toolType);
				// Verify the tool is actually in inventory
				String verifyName = currentTier.equals("normal") ? toolType : currentTier + "_" + toolType;
				net.minecraft.item.Item verifyItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, verifyName));
				boolean toolFound = false;
				if (verifyItem != null) {
					for (int i = 0; i < context.player().getInventory().size(); i++) {
						if (context.player().getInventory().getStack(i).getItem() == verifyItem) {
							toolFound = true;
							break;
						}
					}
				}
				if (!toolFound) return;

				String nextTier = OreToolUpgradeManager.getNextTier(currentTier);
				if (nextTier == null) return;

				String barId = OreToolUpgradeManager.getRequiredBar(nextTier);
				int requiredGold = OreToolUpgradeManager.getRequiredGold(nextTier);

				GoldManager goldMgr = GoldManager.get(world);
				if (goldMgr.getGold() < requiredGold) return;

				net.minecraft.item.Item barItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, barId));
				if (barItem == null) return;

				int barCount = 0;
				for (int i = 0; i < context.player().getInventory().size(); i++) {
					net.minecraft.item.ItemStack stack = context.player().getInventory().getStack(i);
					if (!stack.isEmpty() && stack.getItem() == barItem) {
						barCount += stack.getCount();
					}
				}
				if (barCount < 5) return;

				// Deduct bars
				int toRemove = 5;
				for (int i = 0; i < context.player().getInventory().size() && toRemove > 0; i++) {
					net.minecraft.item.ItemStack stack = context.player().getInventory().getStack(i);
					if (!stack.isEmpty() && stack.getItem() == barItem) {
						int remove = Math.min(toRemove, stack.getCount());
						stack.decrement(remove);
						toRemove -= remove;
						if (stack.isEmpty()) {
							context.player().getInventory().setStack(i, net.minecraft.item.ItemStack.EMPTY);
						}
					}
				}

				goldMgr.addGold(-requiredGold);

				// Remove old tool
				String oldItemName = currentTier.equals("normal") ? toolType : currentTier + "_" + toolType;
				net.minecraft.item.Item oldTool = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, oldItemName));
				if (oldTool != null) {
					for (int i = 0; i < context.player().getInventory().size(); i++) {
						net.minecraft.item.ItemStack stack = context.player().getInventory().getStack(i);
						if (!stack.isEmpty() && stack.getItem() == oldTool) {
							stack.decrement(1);
							if (stack.isEmpty()) {
								context.player().getInventory().setStack(i, net.minecraft.item.ItemStack.EMPTY);
							}
							break;
						}
					}
				}

				mgr.startUpgrade(context.player().getUuid(), toolType, nextTier, world.getTimeOfDay());

				for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(goldMgr.getGold()));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.ToolUpgradeStatusRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				OreToolUpgradeManager mgr = OreToolUpgradeManager.get(world);
				boolean upgrading = mgr.hasUpgradeInProgress(context.player().getUuid());
				if (upgrading) {
					long completeTime = mgr.getUpgradeCompleteTime(context.player().getUuid());
					if (world.getTimeOfDay() >= completeTime) {
						// 升级已完成，进入铁匠铺时自动发放
						OreToolUpgradeManager.InternalEntry entry = mgr.completeUpgrade(context.player().getUuid());
						if (entry != null) {
							if ("trash_can".equals(entry.toolType)) {
								TrashCanStateManager tsm = TrashCanStateManager.get(world);
								int nextLevel = tsm.getLevel() + 1;
								tsm.setLevel(nextLevel);
								for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
									ServerPlayNetworking.send(p, new ModPayloads.TrashCanLevelSyncS2CPayload(tsm.getLevel()));
								}
							} else {
								String itemName = entry.targetTier + "_" + entry.toolType;
								net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, itemName));
								if (item != null) {
									context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(item));
								}
							}
						}
						ServerPlayNetworking.send(context.player(), new ModPayloads.ToolUpgradeStatusSyncS2CPayload(false, "", "", 0));
					} else {
						ServerPlayNetworking.send(context.player(), new ModPayloads.ToolUpgradeStatusSyncS2CPayload(
							true,
							mgr.getUpgradeToolType(context.player().getUuid()),
							mgr.getUpgradeTargetTier(context.player().getUuid()),
							completeTime
						));
					}
				} else {
					ServerPlayNetworking.send(context.player(), new ModPayloads.ToolUpgradeStatusSyncS2CPayload(false, "", "", 0));
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.AddToShippingC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				int slotIndex = payload.slotIndex();
				int invIndex = slotIndex < 9 ? slotIndex + 27 : slotIndex - 9;
				ItemStack stack = player.getInventory().getStack(invIndex);
				if (stack.isEmpty()) return;

				Identifier itemId = Registries.ITEM.getId(stack.getItem());
				if (!itemId.getNamespace().equals(MOD_ID)) return;
				if (GoldManager.getItemMoneyValue(itemId) <= 0) return;

				int count = Math.min(payload.count(), stack.getCount());
				GoldManager manager = GoldManager.get((ServerWorld) player.getEntityWorld());
				manager.addToShipping(itemId, count, player.getUuid());

				stack.decrement(count);
				if (stack.isEmpty()) {
					player.getInventory().setStack(invIndex, ItemStack.EMPTY);
				}
				player.getInventory().markDirty();
			});
		});

		// ===== Crab Pot Bait =====
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.CrabPotOpenC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				BlockPos pos = payload.pos();
				if (!world.isChunkLoaded(pos.getX() >> 4, pos.getZ() >> 4)) return;
				BlockEntity be = world.getBlockEntity(pos);
				if (!(be instanceof stardewvalley.modid.block.CrabPotBlockEntity crabBe)) return;
				openCrabPotScreen(player, pos, crabBe);
			});
		});



		// 武器技能处理器
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.WeaponSkillC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				ItemStack held = player.getMainHandStack();
				if (held.isEmpty()) return;
				if (!(held.getItem() instanceof stardewvalley.modid.item.ModWeaponItem weapon)) return;

				// 技能动画
				player.swingHand(net.minecraft.util.Hand.MAIN_HAND);

				// 3秒冷却（特技者减半）
				if (player.getItemCooldownManager().isCoolingDown(held)) {
					return;
				}
				int cooldown = 60;
				if (SkillEffectHelper.hasAcrobat((ServerWorld) player.getEntityWorld(), player.getUuid())) {
					cooldown = 30;
				}
				player.getItemCooldownManager().set(held, cooldown);

				switch (weapon.getWeaponType()) {
					case SWORD -> {
						float dmg = weapon.getWeaponAttackDamage();
						float weightMult = 1.0f + weapon.getWeight() * 0.3f;
						for (var entity : world.getEntitiesByClass(
							net.minecraft.entity.LivingEntity.class,
							player.getBoundingBox().expand(4.0, 1.0, 4.0),
							e -> e != player && e.isAlive() && !e.isSpectator()
						)) {
							if (player.squaredDistanceTo(entity) < 16.0) {
								entity.damage(world, player.getDamageSources().playerAttack(player), dmg);
								// 水平击退（受重量加成：每级+30%），无垂直击飞
								double dx = entity.getX() - player.getX();
								double dz = entity.getZ() - player.getZ();
								double dist = Math.sqrt(dx * dx + dz * dz);
								if (dist > 0.01) {
									entity.addVelocity(dx / dist * 0.8 * weightMult, 0, dz / dist * 0.8 * weightMult);
								}
							}
						}
					}
					case DAGGER -> {
						// 匕首技能：前方4格内第一个实体，伤害*3
						float dmg = weapon.getWeaponAttackDamage() * 3.0f;
						net.minecraft.util.math.Vec3d eyePos = player.getEyePos();
						net.minecraft.util.math.Vec3d look = player.getRotationVec(1.0f);
						net.minecraft.util.math.Vec3d endPos = eyePos.add(look.x * 4.0, look.y * 4.0, look.z * 4.0);
						double bestDist = 16.0;
						net.minecraft.entity.LivingEntity target = null;
						for (var entity : world.getEntitiesByClass(
							net.minecraft.entity.LivingEntity.class,
							new Box(eyePos, endPos).expand(1.0),
							e -> e != player && e.isAlive() && !e.isSpectator()
						)) {
							net.minecraft.util.math.Box box = entity.getBoundingBox();
							java.util.Optional<net.minecraft.util.math.Vec3d> hit = box.raycast(eyePos, endPos);
							if (hit.isPresent()) {
								double dist = eyePos.squaredDistanceTo(hit.get());
								if (dist < bestDist) {
									bestDist = dist;
									target = entity;
								}
							}
						}
						if (target != null) {
							target.damage(world, player.getDamageSources().playerAttack(player), dmg);
						}
					}
					case CLUB -> {
						// 棍棒技能：范围4.0，击中/空放中心点切换，重锤重击特效，距离负相关击退
						net.minecraft.util.hit.HitResult hit = player.raycast(5.0, 0.0F, false);
						float dmg = weapon.getWeaponAttackDamage();
						double cx, cz;
						if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof net.minecraft.entity.LivingEntity target) {
							cx = target.getX(); cz = target.getZ();
							spawnMaceSmashParticle(world, target.getX(), target.getY(), target.getZ());
							spawnMaceHeavySmashEffect(world, target.getX(), target.getY(), target.getZ());
						} else {
							cx = player.getX(); cz = player.getZ();
							spawnMaceSmashParticle(world, player.getX(), player.getY(), player.getZ());
							spawnMaceHeavySmashEffect(world, player.getX(), player.getY(), player.getZ());
						}
						double radius = 4.0;
						float weightMult = 1.0f + weapon.getWeight() * 0.3f;
						for (var entity : world.getEntitiesByClass(
							net.minecraft.entity.LivingEntity.class,
							new Box(cx - radius, 0, cz - radius, cx + radius, 256, cz + radius),
							e -> e != player && e.isAlive() && !e.isSpectator()
						)) {
							double distSq = entity.squaredDistanceTo(cx, entity.getY(), cz);
							if (distSq < radius * radius) {
								entity.damage(world, player.getDamageSources().playerAttack(player), dmg);
								// 水平击退，速度与到中心距离负相关（越近击退越强）
								double dx = entity.getX() - cx;
								double dz = entity.getZ() - cz;
								double dist = Math.sqrt(dx * dx + dz * dz);
								if (dist > 0.01) {
									double knockSpeed = 0.7 * (1.0 - dist / radius) * weightMult;
									knockSpeed = Math.max(0.15, knockSpeed);
									entity.addVelocity(dx / dist * knockSpeed, 0, dz / dist * knockSpeed);
								}
							}
						}
					}
				}
			});
		});

		// 弹弓打开（ScreenHandler 模式）
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SlingshotOpenC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player() instanceof ServerPlayerEntity player) {
					openSlingshotScreen(player);
				}
			});
		});

		// 钓竿打开（ScreenHandler 模式）
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.RodScreenOpenC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player() instanceof ServerPlayerEntity player) {
					openRodScreen(player);
				}
			});
		});

		// 天气请求：回复当前天气
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.WeatherRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				WeatherState state = WeatherState.get(world);
				ServerPlayNetworking.send(context.player(), new ModPayloads.WeatherSyncS2CPayload(state.todayWeather.ordinal()));
			});
		});

		// 工作台合成
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.WorkbenchCraftC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				String resultItemId = payload.resultItemId();
				int requestedCount = payload.count();
				if (requestedCount <= 0 || requestedCount > 999) return;

				// 获取配方
				java.util.List<WorkbenchIngredient> recipe = getWorkbenchRecipe(resultItemId);
				if (recipe == null) return;

				// 计算最大可合成次数
				int maxCrafts = Integer.MAX_VALUE;
				for (WorkbenchIngredient ing : recipe) {
					int total = CraftingMaterialSets.countItemServer(ing.itemId, player);
					maxCrafts = Math.min(maxCrafts, total / ing.count);
				}

				int craftCount = Math.min(requestedCount, maxCrafts);
				if (craftCount <= 0) return;

				// 扣材料
				for (WorkbenchIngredient ing : recipe) {
					CraftingMaterialSets.removeFromInventory(ing.itemId, ing.count * craftCount, player);
				}

				// 给成品
				Item resultItem = Registries.ITEM.get(Identifier.of(MOD_ID, resultItemId));
				if (resultItem != null) {
					int outputCount = CRAFT_OUTPUT_COUNT.getOrDefault(resultItemId, 1) * craftCount;
					player.getInventory().offerOrDrop(new ItemStack(resultItem, outputCount));
				}
				player.getInventory().markDirty();
			});
		});

		// Traveling Cart handlers
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.TravelingCartStockRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				TravelingCartManager cartManager = TravelingCartManager.get(world);
				LuckManager luckManager = LuckManager.get(world);
				long currentDay = world.getTimeOfDay() / 24000L;
				float luck = luckManager.getDailyLuck(currentDay);
				List<ModPayloads.TravelingCartItem> stock = cartManager.getStockForPlayer(world, luck, context.player().getUuid());

				ServerPlayNetworking.send(context.player(), new ModPayloads.TravelingCartStockSyncS2CPayload(stock));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.TravelingCartBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				TravelingCartManager cartManager = TravelingCartManager.get(world);
				UUID playerUuid = context.player().getUuid();

				// 验证库存并检查剩余可购买数量
				boolean found = false;
				for (ModPayloads.TravelingCartItem item : cartManager.getCurrentStock()) {
					if (item.itemId.equals(payload.itemId()) && item.price == payload.price()) {
						found = true;
						break;
					}
				}
				if (!found) return;

				// 检查剩余数量
				int available = cartManager.getAvailableCount(playerUuid, payload.itemId());
				if (available < payload.count()) return;

				GoldManager manager = GoldManager.get(world);
				int totalCost = payload.price() * payload.count();
				if (manager.getGold() >= totalCost) {
					manager.addGold(-totalCost);
					net.minecraft.item.Item mcItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(StardewValley.MOD_ID, payload.itemId()));
					if (mcItem != null) {
						context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(mcItem, payload.count()));
					}
					cartManager.tryPurchase(playerUuid, payload.itemId(), payload.count());
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
					}
					// 同步更新后的库存给该玩家
					LuckManager luckManager = LuckManager.get(world);
					long currentDay = world.getTimeOfDay() / 24000L;
					float luck = luckManager.getDailyLuck(currentDay);
					List<ModPayloads.TravelingCartItem> updatedStock = cartManager.getStockForPlayer(world, luck, playerUuid);
					ServerPlayNetworking.send(context.player(), new ModPayloads.TravelingCartStockSyncS2CPayload(updatedStock));
				}
			});
		});

		// 桑迪商店 - 库存请求
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SandyShopStockRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				SandyShopManager manager = SandyShopManager.get(world);
				ModPayloads.SandyShopStockItem stock = manager.getStockForPlayer(context.player().getUuid());
				List<ModPayloads.SandyShopStockItem> list = new java.util.ArrayList<>();
				if (stock != null) list.add(stock);
				ServerPlayNetworking.send(context.player(), new ModPayloads.SandyShopStockSyncS2CPayload(list));
			});
		});

		// 桑迪商店 - 购买
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SandyShopBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				SandyShopManager manager = SandyShopManager.get(world);
				UUID playerUuid = context.player().getUuid();

				ModPayloads.SandyShopStockItem stock = manager.getStockForPlayer(playerUuid);
				if (stock == null || !stock.itemId.equals(payload.itemId()) || stock.price != payload.price()) return;

				int count = payload.count();
				if (count <= 0) return;
				if (stock.maxBuy > 0) {
					int available = manager.getAvailableCount(playerUuid, payload.itemId());
					if (available < count) return;
				}

				GoldManager goldManager = GoldManager.get(world);
				int totalCost = payload.price() * count;
				if (goldManager.getGold() >= totalCost) {
					goldManager.addGold(-totalCost);
					net.minecraft.item.Item mcItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(StardewValley.MOD_ID, payload.itemId()));
					if (mcItem != null) {
						context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(mcItem, count));
					}
					manager.tryPurchase(playerUuid, payload.itemId(), count);
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(goldManager.getGold()));
					}
					// 同步更新后的库存
					ModPayloads.SandyShopStockItem updatedStock = manager.getStockForPlayer(playerUuid);
					List<ModPayloads.SandyShopStockItem> list = new java.util.ArrayList<>();
					if (updatedStock != null) list.add(updatedStock);
					ServerPlayNetworking.send(context.player(), new ModPayloads.SandyShopStockSyncS2CPayload(list));
				}
			});
		});

		// 科罗布斯商店处理器
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.KrobusDataRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				KrobusShopManager manager = KrobusShopManager.get(world);
				long currentDay = world.getTimeOfDay() / 24000L;
				manager.onNewDay(currentDay);
				List<ModPayloads.KrobusShopItem> stock = manager.getStockData(context.player().getUuid());
				ServerPlayNetworking.send(context.player(), new ModPayloads.KrobusDataSyncS2CPayload(stock));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.KrobusBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				KrobusShopManager manager = KrobusShopManager.get(world);
				GoldManager goldManager = GoldManager.get(world);
				UUID playerUuid = context.player().getUuid();
				int totalCost = payload.price() * payload.count();
				if (goldManager.getGold() >= totalCost && manager.tryPurchase(playerUuid, payload.itemId(), payload.count())) {
					goldManager.addGold(-totalCost);
					// 给玩家物品
					Identifier itemId = Identifier.of(StardewValley.MOD_ID, payload.itemId());
					Item item = Registries.ITEM.get(itemId);
					if (item != null) {
						context.player().getInventory().offerOrDrop(new ItemStack(item, payload.count()));
					}
					// 同步更新
					ServerPlayNetworking.send(context.player(), new ModPayloads.GoldSyncS2CPayload(goldManager.getGold()));
					List<ModPayloads.KrobusShopItem> stock = manager.getStockData(playerUuid);
					ServerPlayNetworking.send(context.player(), new ModPayloads.KrobusDataSyncS2CPayload(stock));
				}
			});
		});

		// ====== 收集包系统 ======
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BundleDataRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				BundleManager bm = BundleManager.get(world);
				java.util.UUID playerUuid = context.player().getUuid();

				// 构建同步数据
				java.util.List<ModPayloads.BundleSlotSyncEntry> entries = new java.util.ArrayList<>();
				java.util.Set<String> completedSet = new java.util.HashSet<>(bm.getCompletedBundles());
				java.util.Set<String> vaultSet = new java.util.HashSet<>(bm.getVaultPurchased());
				java.util.Set<String> claimedBundleIds = new java.util.HashSet<>(bm.getClaimedBundleIds(playerUuid));

				java.util.List<BundleManager.BundleSlotData> allData = bm.getAllSlotData();
				for (BundleManager.BundleSlotData d : allData) {
					entries.add(new ModPayloads.BundleSlotSyncEntry(d.bundleId, d.submittedItems, d.completed));
				}

				ServerPlayNetworking.send(context.player(),
					new ModPayloads.BundleDataSyncS2CPayload(entries, completedSet, vaultSet, claimedBundleIds));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BundleSubmitItemC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				BundleManager bm = BundleManager.get(world);

				String bundleId = payload.bundleId();
				int slotIndex = payload.slotIndex();
				int invIndex = slotIndex < 9 ? slotIndex + 27 : slotIndex - 9;
				ItemStack stack = player.getInventory().getStack(invIndex);
				if (stack.isEmpty()) return;

				String path = Registries.ITEM.getId(stack.getItem()).getPath();
				if (!Registries.ITEM.getId(stack.getItem()).getNamespace().equals(MOD_ID)) return;

				BundleManager.BundleDef def = BundleManager.getBundleDef(bundleId);
				if (def == null) return;

				// 查找匹配的所需物品
				for (BundleManager.BundleItemDef req : def.requiredItems()) {
					// 品质匹配（接受更高品质）
					if (itemMatchesRequiredSV(path, req.itemId())) {
						int submitted = bm.getSubmittedCount(bundleId, req.itemId());
						if (submitted >= req.count()) continue;

						int toTake = Math.min(stack.getCount(), req.count() - submitted);
						stack.decrement(toTake);
						if (stack.isEmpty()) {
							player.getInventory().setStack(invIndex, ItemStack.EMPTY);
						}
						player.getInventory().markDirty();

						// 用req的itemId作为提交key（统一用普通品质版本）
						bm.submitItem(bundleId, req.itemId(), toTake);
						bm.checkAndCompleteBundle(bundleId);

						// 全服同步
						syncBundleDataToAll(context.server(), bm, world);
						break;
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BundleClaimRewardC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				BundleManager bm = BundleManager.get(world);

				String bundleId = payload.bundleId();
				if (!bm.isBundleCompleted(bundleId)) return;
				if (bm.hasClaimedBundleReward(player.getUuid(), bundleId)) return;

				BundleManager.BundleDef def = BundleManager.getBundleDef(bundleId);
				if (def == null) return;

				// 发放奖励
				Identifier rewardId = parseItemId(def.rewardItemId());
				net.minecraft.item.Item rewardItem = Registries.ITEM.get(rewardId);
				if (rewardItem != null) {
					player.getInventory().offerOrDrop(new ItemStack(rewardItem, def.rewardCount()));
				}

				bm.markBundleRewardClaimed(player.getUuid(), bundleId);
				syncBundleDataToAll(context.server(), bm, world);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BundleVaultBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				BundleManager bm = BundleManager.get(world);

				String bundleId = payload.bundleId();
				if (bm.isVaultPurchased(bundleId)) return;

				BundleManager.BundleDef def = BundleManager.getBundleDef(bundleId);
				if (def == null) return;

				int cost = switch (bundleId) {
					case "vault_2500" -> 2500;
					case "vault_5000" -> 5000;
					case "vault_10000" -> 10000;
					case "vault_25000" -> 25000;
					default -> 0;
				};

				GoldManager gm = GoldManager.get(world);
				if (gm.getGold() < cost) return;

				gm.addGold(-cost);
				bm.markVaultPurchased(bundleId);

				// 发放奖励
				Identifier rewardId = parseItemId(def.rewardItemId());
				net.minecraft.item.Item rewardItem = Registries.ITEM.get(rewardId);
				if (rewardItem != null) {
					player.getInventory().offerOrDrop(new ItemStack(rewardItem, def.rewardCount()));
				}

				bm.markBundleRewardClaimed(player.getUuid(), bundleId);

				// 同步金币和收集包数据
				for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(gm.getGold()));
				}
				syncBundleDataToAll(context.server(), bm, world);
			});
		});

		// 马龙武器商店以物换物：1个五彩碎片换1把银河剑（每个玩家限一次）
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MarlonBarterC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				// 检查是否已兑换过
				MarlonTradeManager manager = MarlonTradeManager.get(world);
				if (manager.hasTraded(player.getUuid())) return;
				int count = payload.count();
				// 检查是否有足够五彩碎片
				int prismaticCount = 0;
				for (int slot = 0; slot < player.getInventory().size(); slot++) {
					ItemStack stack = player.getInventory().getStack(slot);
					Identifier id = Registries.ITEM.getId(stack.getItem());
					if (id.getNamespace().equals(MOD_ID) && id.getPath().equals("prismatic_shard")) {
						prismaticCount += stack.getCount();
					} else if (id.getNamespace().equals("minecraft") && id.getPath().equals("prismatic_shard")) {
						prismaticCount += stack.getCount();
					}
				}
				if (prismaticCount < count) return;
				// 移除五彩碎片
				int remaining = count;
				for (int slot = 0; slot < player.getInventory().size() && remaining > 0; slot++) {
					ItemStack stack = player.getInventory().getStack(slot);
					Identifier id = Registries.ITEM.getId(stack.getItem());
					boolean isPrismatic = (id.getNamespace().equals(MOD_ID) || id.getNamespace().equals("minecraft"))
						&& id.getPath().equals("prismatic_shard");
					if (!isPrismatic) continue;
					int toRemove = Math.min(stack.getCount(), remaining);
					stack.decrement(toRemove);
					remaining -= toRemove;
				}
				// 标记已兑换
				manager.markTraded(player.getUuid());
				// 给予银河剑
				net.minecraft.item.Item galaxySword = Registries.ITEM.get(Identifier.of(MOD_ID, "galaxy_sword"));
				if (galaxySword != null) {
					player.getInventory().offerOrDrop(new ItemStack(galaxySword, count));
				}
				// 同步已兑换状态给客户端
				ServerPlayNetworking.send(player, new ModPayloads.MarlonTradeStateSyncS2CPayload(true));
			});
		});

		// 马龙兑换状态请求
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MarlonTradeStateRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				boolean hasTraded = MarlonTradeManager.get(world).hasTraded(player.getUuid());
				ServerPlayNetworking.send(player, new ModPayloads.MarlonTradeStateSyncS2CPayload(hasTraded));
			});
		});

		// BookStore handlers
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BookStoreStockRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				List<ModPayloads.BookStoreItem> stock = getBookStoreStock(context.player(), world);
				ServerPlayNetworking.send(context.player(), new ModPayloads.BookStoreStockSyncS2CPayload(stock));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BookStoreOpenRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				boolean open = BookStoreManager.get(world).isOpenToday(world);
				ServerPlayNetworking.send(context.player(), new ModPayloads.BookStoreOpenSyncS2CPayload(open));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BookStoreBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld world = (ServerWorld) context.player().getEntityWorld();
				UUID playerUuid = context.player().getUuid();
				GoldManager manager = GoldManager.get(world);
				int totalCost = payload.price() * payload.count();
				if (manager.getGold() >= totalCost) {
					manager.addGold(-totalCost);
					net.minecraft.item.Item mcItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(StardewValley.MOD_ID, payload.itemId()));
					if (mcItem != null) {
						context.player().getInventory().offerOrDrop(new net.minecraft.item.ItemStack(mcItem, payload.count()));
					}
					for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
						ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
					}
				}
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BookStoreBarterC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (payload.reqItemId() != null && payload.reqCount() > 0) {
					int count = 0;
					for (int slot = 0; slot < context.player().getInventory().size(); slot++) {
						ItemStack stack = context.player().getInventory().getStack(slot);
						if (!stack.isEmpty()) {
							Identifier id = Registries.ITEM.getId(stack.getItem());
							if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(payload.reqItemId())) {
								count += stack.getCount();
							}
						}
					}
					if (count < payload.reqCount()) return;
					int toRemove = payload.reqCount();
					for (int slot = 0; slot < context.player().getInventory().size() && toRemove > 0; slot++) {
						ItemStack stack = context.player().getInventory().getStack(slot);
						if (!stack.isEmpty()) {
							Identifier id = Registries.ITEM.getId(stack.getItem());
							if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(payload.reqItemId())) {
								int removed = Math.min(toRemove, stack.getCount());
								stack.decrement(removed);
								toRemove -= removed;
							}
						}
					}
				}
				if (payload.reqItemId2() != null && payload.reqCount2() > 0) {
					int count = 0;
					for (int slot = 0; slot < context.player().getInventory().size(); slot++) {
						ItemStack stack = context.player().getInventory().getStack(slot);
						if (!stack.isEmpty()) {
							Identifier id = Registries.ITEM.getId(stack.getItem());
							if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(payload.reqItemId2())) {
								count += stack.getCount();
							}
						}
					}
					if (count < payload.reqCount2()) return;
					int toRemove = payload.reqCount2();
					for (int slot = 0; slot < context.player().getInventory().size() && toRemove > 0; slot++) {
						ItemStack stack = context.player().getInventory().getStack(slot);
						if (!stack.isEmpty()) {
							Identifier id = Registries.ITEM.getId(stack.getItem());
							if (id.getNamespace().equals(StardewValley.MOD_ID) && id.getPath().equals(payload.reqItemId2())) {
								int removed = Math.min(toRemove, stack.getCount());
								stack.decrement(removed);
								toRemove -= removed;
							}
						}
					}
				}
				net.minecraft.item.Item resultItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, payload.resultItemId()));
				if (resultItem != null) {
					context.player().getInventory().offerOrDrop(new ItemStack(resultItem, payload.count()));
				}
			});
		});

		// 沙漠商人/姜岛商人以物换物（无数量限制）
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.DesertTraderBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				String resultItemId = payload.resultItemId();
				int count = payload.count();
				if (count <= 0 || count > 999) return;

				// 获取配方
				java.util.List<WorkbenchIngredient> recipe = getBarterTradeRecipe(resultItemId);
				if (recipe == null) return;

				// 检查材料是否足够（支持多次合成）
				int maxCrafts = Integer.MAX_VALUE;
				for (WorkbenchIngredient ing : recipe) {
					int total = CraftingMaterialSets.countItemServer(ing.itemId, player);
					maxCrafts = Math.min(maxCrafts, total / ing.count);
				}
				int craftCount = Math.min(count, maxCrafts);
				if (craftCount <= 0) return;

				// 扣材料
				for (WorkbenchIngredient ing : recipe) {
					CraftingMaterialSets.removeFromInventory(ing.itemId, ing.count * craftCount, player);
				}

				// 给予物品
				Item resultItem = Registries.ITEM.get(Identifier.of(MOD_ID, resultItemId));
				if (resultItem != null) {
					int outputMultiplier = BARTER_OUTPUT_COUNT.getOrDefault(resultItemId, 1);
					player.getInventory().offerOrDrop(new ItemStack(resultItem, craftCount * outputMultiplier));
				}
			});
		});
	}

	// 所有银星品质果酒ID集合（用于匹配 _any_wine_silver）
	private static final java.util.Set<String> SERVER_SILVER_WINE_ITEMS = createServerSilverWineSet();

	private static java.util.Set<String> createServerSilverWineSet() {
		java.util.Set<String> set = new java.util.HashSet<>();
		for (String itemId : stardewvalley.modid.crafting.CraftingMaterialSets.ANY_WINE) {
			if (itemId.endsWith("_wine_silver")) {
				set.add(itemId);
			}
		}
		return java.util.Collections.unmodifiableSet(set);
	}

	private static boolean itemMatchesRequiredSV(String inventoryItemId, String requiredItemId) {
		if (inventoryItemId.equals(requiredItemId)) return true;
		// 特殊处理: _any_wine_silver 匹配任何银星品质果酒
		if (requiredItemId.equals("_any_wine_silver") && SERVER_SILVER_WINE_ITEMS.contains(inventoryItemId)) {
			return true;
		}
		// 品质变体匹配
		for (String suffix : new String[]{"_iridium", "_gold", "_silver"}) {
			if (inventoryItemId.endsWith(suffix)) {
				String base = inventoryItemId.substring(0, inventoryItemId.length() - suffix.length());
				if (base.equals(requiredItemId)) return true;
			}
		}
		return false;
	}

	private static Identifier parseItemId(String itemId) {
		if (itemId.contains(":")) {
			return Identifier.of(itemId);
		}
		return Identifier.of(MOD_ID, itemId);
	}

	private static void syncBundleDataToAll(net.minecraft.server.MinecraftServer server, BundleManager bm, ServerWorld world) {
		java.util.List<ModPayloads.BundleSlotSyncEntry> entries = new java.util.ArrayList<>();
		java.util.Set<String> completedSet = new java.util.HashSet<>(bm.getCompletedBundles());
		java.util.Set<String> vaultSet = new java.util.HashSet<>(bm.getVaultPurchased());

		java.util.List<BundleManager.BundleSlotData> allData = bm.getAllSlotData();
		for (BundleManager.BundleSlotData d : allData) {
			entries.add(new ModPayloads.BundleSlotSyncEntry(d.bundleId, d.submittedItems, d.completed));
		}

		for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
			java.util.Set<String> claimedBundleIds = new java.util.HashSet<>(bm.getClaimedBundleIds(p.getUuid()));
			ServerPlayNetworking.send(p, new ModPayloads.BundleDataSyncS2CPayload(entries, completedSet, vaultSet, claimedBundleIds));
		}
	}

	private static boolean isSvRod(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		Identifier id = Registries.ITEM.getId(stack.getItem());
		if (!MOD_ID.equals(id.getNamespace())) return false;
		String path = id.getPath();
		return path.equals("bamboo_pole") || path.equals("fiberglass_rod") || path.equals("iridium_rod") || path.equals("advanced_iridium_rod");
	}

	private static boolean isSvSlingshot(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		Identifier id = Registries.ITEM.getId(stack.getItem());
		if (!MOD_ID.equals(id.getNamespace())) return false;
		String path = id.getPath();
		return path.equals("slingshot") || path.equals("master_slingshot");
	}

	private static Item getIngredientItem(String itemId) {
		if (itemId.contains(":")) {
			return Registries.ITEM.get(Identifier.of(itemId));
		}
		return Registries.ITEM.get(Identifier.of(MOD_ID, itemId));
	}

	private static ItemStack removeFromSlot(ServerPlayerEntity player, int slotIndex, int count) {
		if (slotIndex < 0 || slotIndex >= player.getInventory().size()) return ItemStack.EMPTY;
		ItemStack stack = player.getInventory().getStack(slotIndex);
		if (!stack.isEmpty() && stack.getCount() >= count) {
			ItemStack result = stack.copy();
			result.setCount(count);
			stack.decrement(count);
			if (stack.isEmpty()) {
				player.getInventory().setStack(slotIndex, ItemStack.EMPTY);
			}
			player.getInventory().markDirty();
			return result;
		}
		return ItemStack.EMPTY;
	}

	private static ItemStack removeFromInventory(ServerPlayerEntity player, Item item, int count) {
		for (int i = 0; i < player.getInventory().size(); i++) {
			ItemStack stack = player.getInventory().getStack(i);
			if (!stack.isEmpty() && stack.getItem() == item && stack.getCount() >= count) {
				ItemStack result = stack.copy();
				result.setCount(count);
				stack.decrement(count);
				if (stack.isEmpty()) {
					player.getInventory().setStack(i, ItemStack.EMPTY);
				}
				return result;
			}
		}
		return ItemStack.EMPTY;
	}

	// ====== 额外装备栏服务器处理器 ======

	private void registerEquipmentHandler() {
		// 玩家加入时加载并同步装备栏数据
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> {
				ServerPlayerEntity player = handler.getPlayer();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				EquipmentStateManager stateManager = EquipmentStateManager.get(world);
				EquipmentInventory inv = stateManager.getPlayerData(player.getUuid());
				syncEquipment(player, inv);
				TrashCanStateManager tsm = TrashCanStateManager.get(world);
				ServerPlayNetworking.send(player, new ModPayloads.TrashCanLevelSyncS2CPayload(tsm.getLevel()));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.EquipmentActionC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				EquipmentStateManager stateManager = EquipmentStateManager.get(world);
				EquipmentInventory inv = stateManager.getPlayerData(player.getUuid());
				int slotIndex = payload.slotIndex();
				if (slotIndex < 0 || slotIndex >= EquipmentInventory.SLOT_COUNT) return;

				switch (EquipmentActionType.values()[payload.action()]) {
					case PUT: {
						ItemStack cursor = player.currentScreenHandler.getCursorStack();
						if (cursor.isEmpty()) return;
						if (!EquipmentInventory.canPlaceInSlot(slotIndex, cursor)) return;
						inv.setSlot(slotIndex, cursor.copy());
						player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);
						stateManager.setPlayerData(player.getUuid(), inv);
						syncEquipment(player, inv);
						break;
					}
					case TAKE: {
						ItemStack slotStack = inv.getSlot(slotIndex);
						if (slotStack.isEmpty()) return;
						player.currentScreenHandler.setCursorStack(slotStack.copy());
						inv.setSlot(slotIndex, ItemStack.EMPTY);
						stateManager.setPlayerData(player.getUuid(), inv);
						syncEquipment(player, inv);
						break;
					}
					case SWAP: {
						ItemStack cursor = player.currentScreenHandler.getCursorStack();
						ItemStack slotStack = inv.getSlot(slotIndex);
						if (cursor.isEmpty() || slotStack.isEmpty()) return;
						if (!EquipmentInventory.canPlaceInSlot(slotIndex, cursor)) return;
						inv.setSlot(slotIndex, cursor.copy());
						player.currentScreenHandler.setCursorStack(slotStack.copy());
						stateManager.setPlayerData(player.getUuid(), inv);
						syncEquipment(player, inv);
						break;
					}
				}
			});
		});
	}

	/** 向客户端同步装备栏数据 */
	private static void syncEquipment(ServerPlayerEntity player, EquipmentInventory inv) {
		net.minecraft.nbt.NbtCompound data = new net.minecraft.nbt.NbtCompound();
		net.minecraft.nbt.NbtList list = new net.minecraft.nbt.NbtList();
		for (int i = 0; i < EquipmentInventory.SLOT_COUNT; i++) {
			net.minecraft.nbt.NbtCompound slotData = new net.minecraft.nbt.NbtCompound();
			ItemStack stack = inv.getSlot(i);
			if (!stack.isEmpty()) {
				slotData.putString("id", net.minecraft.registry.Registries.ITEM.getId(stack.getItem()).toString());
				slotData.putInt("count", stack.getCount());
			}
			list.add(slotData);
		}
		data.put("slots", list);
		ServerPlayNetworking.send(player, new ModPayloads.EquipmentSyncS2CPayload(data));
	}

	// ====== 垃圾桶服务器处理器 ======

	private void registerTrashCanHandler() {
		// 数据请求
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.TrashCanDataRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				TrashCanStateManager state = TrashCanStateManager.get(world);
				if (state.getLevel() <= 0) return;
				TrashCanStateManager.PendingItem pi = state.getPendingItem(player.getUuid());
				ServerPlayNetworking.send(player, new ModPayloads.TrashCanDataSyncS2CPayload(pi.itemId, pi.count, 0));
			});
		});
		// 操作处理器
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.TrashCanActionC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				TrashCanStateManager state = TrashCanStateManager.get(world);
				int level = state.getLevel();
				if (level <= 0) return;
				double rate = TrashCanStateManager.getRecycleRate(level);
				UUID uuid = player.getUuid();

				if (payload.action() == 0) {
					// PUT / MERGE / REPLACE — 物品放入
					String incomingItemId = payload.itemId();
					int incomingCount = payload.count();
					if (incomingItemId.isEmpty() || incomingCount <= 0) return;
					Identifier incomingId = Identifier.of(incomingItemId);
					net.minecraft.item.Item item = Registries.ITEM.get(incomingId);
					if (item == null || item == Items.AIR) return;

					// 清除服务端光标，防止与服务端不同步导致点击背包格子时装回物品
					player.currentScreenHandler.setCursorStack(ItemStack.EMPTY);

					TrashCanStateManager.PendingItem pi = state.getPendingItem(uuid);
					int earned = 0;

					if (pi.itemId.isEmpty()) {
						// 空桶 → 直接存入，不给gold
						state.setPendingItem(uuid, incomingItemId, incomingCount);
					} else if (pi.itemId.equals(incomingItemId)) {
						// 同种 → 合并，不超过999
						int total = pi.count + incomingCount;
						if (total > 999) {
							int overflow = total - 999;
							pi.count = 999;
							// 超出部分回到玩家背包
							ItemStack overflowStack = new ItemStack(item, overflow);
							player.getInventory().offerOrDrop(overflowStack);
						} else {
							pi.count = total;
						}
						state.setPendingItem(uuid, incomingItemId, pi.count);
					} else {
						// 不同种 → 旧物品回收给gold后删除，新物品存入（不给gold）
						Identifier oldId = Identifier.of(pi.itemId);
						int oldValue = GoldManager.getItemMoneyValue(oldId);
						if (oldValue > 0 && pi.count > 0) {
							earned = (int) (oldValue * pi.count * rate);
						}
						state.setPendingItem(uuid, incomingItemId, incomingCount);
					}

					if (earned > 0) {
						GoldManager gm = GoldManager.get(world);
						gm.addGold(earned);
						// 同步gold到客户端
						ServerPlayNetworking.send(player, new ModPayloads.GoldSyncS2CPayload(gm.getGold()));
					}

					TrashCanStateManager.PendingItem newPi = state.getPendingItem(uuid);
					ServerPlayNetworking.send(player, new ModPayloads.TrashCanDataSyncS2CPayload(newPi.itemId, newPi.count, earned));
				} else if (payload.action() == 1) {
					// TAKE — 取出物品
					TrashCanStateManager.PendingItem pi = state.getPendingItem(uuid);
					if (!pi.itemId.isEmpty() && pi.count > 0) {
						Identifier takeId = Identifier.of(pi.itemId);
						net.minecraft.item.Item takeItem = Registries.ITEM.get(takeId);
						if (takeItem != null && takeItem != Items.AIR) {
							player.getInventory().offerOrDrop(new ItemStack(takeItem, pi.count));
						}
					}
					state.clearPendingItem(uuid);
					ServerPlayNetworking.send(player, new ModPayloads.TrashCanDataSyncS2CPayload("", 0, 0));
				}
			});
		});
		// 等级请求
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.TrashCanLevelRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				TrashCanStateManager state = TrashCanStateManager.get((ServerWorld) player.getEntityWorld());
				ServerPlayNetworking.send(player, new ModPayloads.TrashCanLevelSyncS2CPayload(state.getLevel()));
			});
		});
		// 升级请求（改为和工具一样需要等待时间）
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.TrashCanUpgradeC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				OreToolUpgradeManager mgr = OreToolUpgradeManager.get(world);

				// 检查是否有正在进行的垃圾桶升级已完成（收集已完成的升级）
				if (mgr.hasUpgradeInProgress(player.getUuid())) {
					OreToolUpgradeManager.InternalEntry entry = mgr.getUpgrade(player.getUuid());
					if (entry != null && "trash_can".equals(entry.toolType) && world.getTimeOfDay() >= entry.completeTime) {
						mgr.completeUpgrade(player.getUuid());
						TrashCanStateManager state = TrashCanStateManager.get(world);
						int nextLevel = state.getLevel() + 1;
						state.setLevel(nextLevel);
						for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
							ServerPlayNetworking.send(p, new ModPayloads.TrashCanLevelSyncS2CPayload(state.getLevel()));
						}
					}
					return;
				}

				TrashCanStateManager state = TrashCanStateManager.get(world);
				int level = state.getLevel();
				if (level >= 4) return;

				// 检查是否有任何升级进行中
				if (mgr.hasUpgradeInProgress(player.getUuid())) return;

				int nextLevel = level + 1;
				String barItem = switch (nextLevel) { case 1 -> "copper_bar"; case 2 -> "iron_bar"; case 3 -> "gold_bar"; default -> "iridium_bar"; };
				int costGold = switch (nextLevel) { case 1 -> 1000; case 2 -> 2500; case 3 -> 5000; default -> 12500; };
				GoldManager gm = GoldManager.get(world);
				if (gm.getGold() < costGold) return;
				Item bar = Registries.ITEM.get(Identifier.of(MOD_ID, barItem));
				if (bar == null || bar == Items.AIR) return;
				int count = 0;
				for (int i = 0; i < player.getInventory().size(); i++) {
					ItemStack s = player.getInventory().getStack(i);
					if (!s.isEmpty() && s.getItem() == bar) count += s.getCount();
				}
				if (count < 5) return;
				gm.addGold(-costGold);
				removeFromInventory(player, bar, 5);

				mgr.startUpgrade(player.getUuid(), "trash_can", String.valueOf(nextLevel), world.getTimeOfDay());

				for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(gm.getGold()));
				}
			});
		});
	}

	// ====== 扩展背包系统 ======

	/**
	 * 服务端打开扩展背包（由 InventoryScreenClickMixin / ModKeybindings 调用）
	 */
	/**
	 * 服务端打开弹弓界面（由 SlingshotOpenC2SPayload 调用）
	 */
	public static void openSlingshotScreen(ServerPlayerEntity player) {
		ItemStack sling;
		if (isSvSlingshot(player.getMainHandStack())) {
			sling = player.getMainHandStack();
		} else if (isSvSlingshot(player.getOffHandStack())) {
			sling = player.getOffHandStack();
		} else {
			return;
		}

		ItemStack finalSling = sling;
		player.openHandledScreen(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<ModScreenHandlers.SlingshotOpenData>() {
			@Override
			public ModScreenHandlers.SlingshotOpenData getScreenOpeningData(ServerPlayerEntity p) {
				return new ModScreenHandlers.SlingshotOpenData();
			}

			@Override
			public net.minecraft.text.Text getDisplayName() {
				return net.minecraft.text.Text.literal("Slingshot");
			}

			@Override
			public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inv, net.minecraft.entity.player.PlayerEntity p) {
				SlingshotScreenHandler handler = new SlingshotScreenHandler(syncId, inv);
				handler.loadFromSlingshot(finalSling);
				return handler;
			}
		});
	}

	/**
	 * 服务端打开钓竿界面（由 RodScreenOpenC2SPayload 调用）
	 */
	public static void openRodScreen(ServerPlayerEntity player) {
		ItemStack rod;
		if (isSvRod(player.getMainHandStack())) {
			rod = player.getMainHandStack();
		} else if (isSvRod(player.getOffHandStack())) {
			rod = player.getOffHandStack();
		} else {
			return;
		}

		// 竹鱼竿没有槽位，不需要打开 UI
		if (!stardewvalley.modid.item.ModFishingRodItem.hasBaitSlot(rod)
			&& stardewvalley.modid.item.ModFishingRodItem.getTackleSlotCount(rod) == 0) {
			return;
		}

		int tackleCount = ModFishingRodItem.getTackleSlotCount(rod);
		ItemStack finalRod = rod;
		player.openHandledScreen(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<ModScreenHandlers.RodOpenData>() {
			@Override
			public ModScreenHandlers.RodOpenData getScreenOpeningData(ServerPlayerEntity p) {
				return new ModScreenHandlers.RodOpenData(tackleCount);
			}

			@Override
			public net.minecraft.text.Text getDisplayName() {
				return net.minecraft.text.Text.literal("Fishing Rod");
			}

			@Override
			public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inv, net.minecraft.entity.player.PlayerEntity p) {
				RodScreenHandler handler = new RodScreenHandler(syncId, inv, new ModScreenHandlers.RodOpenData(tackleCount));
				handler.loadFromRod(finalRod);
				return handler;
			}
		});
	}

	/**
	 * 服务端打开蟹笼诱饵界面（由 CrabPotOpenC2SPayload 调用）
	 */
	public static void openCrabPotScreen(ServerPlayerEntity player, BlockPos pos, stardewvalley.modid.block.CrabPotBlockEntity crabBe) {
		player.openHandledScreen(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<ModScreenHandlers.CrabPotOpenData>() {
			@Override
			public ModScreenHandlers.CrabPotOpenData getScreenOpeningData(ServerPlayerEntity p) {
				return new ModScreenHandlers.CrabPotOpenData(pos);
			}

			@Override
			public net.minecraft.text.Text getDisplayName() {
				return net.minecraft.text.Text.literal("Crab Pot");
			}

			@Override
			public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inv, net.minecraft.entity.player.PlayerEntity p) {
				CrabPotBaitScreenHandler handler = new CrabPotBaitScreenHandler(syncId, inv, pos);
				handler.loadFromBlockEntity(crabBe);
				return handler;
			}
		});
	}

	public static void openBackpackScreen(ServerPlayerEntity player) {
		ServerWorld world = (ServerWorld) player.getEntityWorld();
		BackpackStateManager mgr = BackpackStateManager.get(world);
		int level = mgr.getPlayerLevel(player.getUuid());

		player.openHandledScreen(new net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory<ModScreenHandlers.BackpackOpenData>() {
			@Override
			public ModScreenHandlers.BackpackOpenData getScreenOpeningData(ServerPlayerEntity p) {
				return new ModScreenHandlers.BackpackOpenData(level);
			}

			@Override
			public net.minecraft.text.Text getDisplayName() {
				return net.minecraft.text.Text.literal("Extended Backpack");
			}

			@Override
			public net.minecraft.screen.ScreenHandler createMenu(int syncId, net.minecraft.entity.player.PlayerInventory inv, net.minecraft.entity.player.PlayerEntity p) {
				BackpackScreenHandler handler = new BackpackScreenHandler(syncId, inv, level);
				handler.loadFromManager(mgr, player);
				return handler;
			}
		});
	}

	private void registerBackpackHandler() {
		// 客户端请求打开扩展背包
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BackpackOpenC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				if (context.player() instanceof ServerPlayerEntity player) {
					openBackpackScreen(player);
				}
			});
		});

		// 从商店购买背包升级
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.BackpackBuyC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				BackpackStateManager mgr = BackpackStateManager.get(world);
				GoldManager gm = GoldManager.get(world);
				int currentLevel = mgr.getPlayerLevel(player.getUuid());

				if ("backpack".equals(payload.upgradeType()) && currentLevel < 1) {
					if (gm.getGold() >= 2000) {
						gm.addGold(-2000);
						mgr.setPlayerLevel(player.getUuid(), 1);
						for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
							ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(gm.getGold()));
						}
					}
				} else if ("36_backpack".equals(payload.upgradeType()) && currentLevel < 2) {
					if (gm.getGold() >= 10000) {
						gm.addGold(-10000);
						mgr.setPlayerLevel(player.getUuid(), 2);
						for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
							ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(gm.getGold()));
						}
					}
				}
				// 购买后同步等级给客户端（ShopScreen 需要）
				ServerPlayNetworking.send(player, new ModPayloads.BackpackDataSyncS2CPayload(levelToNbt(mgr.getPlayerLevel(player.getUuid()))));
			});
		});
	}

	private net.minecraft.nbt.NbtCompound levelToNbt(int level) {
		net.minecraft.nbt.NbtCompound data = new net.minecraft.nbt.NbtCompound();
		data.putInt("level", level);
		return data;
	}
	
	private void registerMuseumHandler() {
		// ====== 博物馆捐赠系统 ======
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MuseumDataRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				MuseumManager mm = MuseumManager.get(world);
				MuseumManager.MuseumSyncData data = mm.createSyncData(player.getUuid());
				ServerPlayNetworking.send(player, new ModPayloads.MuseumDataSyncS2CPayload(
					data.donatedItems(), data.claimedRewardIndices(), data.abilities()));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MuseumDonateC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				int slotIndex = payload.slotIndex();
				int invIndex = slotIndex < 9 ? slotIndex + 27 : slotIndex - 9;
				net.minecraft.item.ItemStack stack = player.getInventory().getStack(invIndex);
				if (stack.isEmpty()) return;
				net.minecraft.util.Identifier itemId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
				if (!itemId.getNamespace().equals(StardewValley.MOD_ID)) return;
				String path = itemId.getPath();
				if (!MuseumManager.isDonatable(path)) return;

				MuseumManager mm = MuseumManager.get(world);
				boolean firstDonation = mm.donateItem(path);
				if (firstDonation) {
					stack.decrement(1);
					if (stack.isEmpty()) {
						player.getInventory().setStack(invIndex, net.minecraft.item.ItemStack.EMPTY);
					}
					player.getInventory().markDirty();

					for (var sDef : MuseumManager.SPECIAL_REWARDS) {
						if (sDef.triggerItemId().equals(path)) {
							if (sDef.isAbility()) {
								mm.addAbility(player.getUuid(), sDef.abilityId());
							} else if (sDef.rewardItemId() != null) {
								Identifier rewardId = Identifier.of(StardewValley.MOD_ID, sDef.rewardItemId());
								net.minecraft.item.Item rewardItem = net.minecraft.registry.Registries.ITEM.get(rewardId);
								if (rewardItem != null && rewardItem != net.minecraft.item.Items.AIR) {
									player.getInventory().offerOrDrop(new net.minecraft.item.ItemStack(rewardItem, sDef.rewardCount()));
								}
							}
						}
					}
				}

				MuseumManager.MuseumSyncData data = mm.createSyncData(player.getUuid());
				ServerPlayNetworking.send(player, new ModPayloads.MuseumDataSyncS2CPayload(
					data.donatedItems(), data.claimedRewardIndices(), data.abilities()));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MuseumClaimRewardC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				MuseumManager mm = MuseumManager.get(world);
				int rewardIndex = payload.rewardIndex();

				if (rewardIndex == -1) {
					if (mm.areAllDwarfScrollsDonatedGlobally() && !mm.hasAbility(player.getUuid(), "dwarf_translation")) {
						mm.addAbility(player.getUuid(), "dwarf_translation");
					}
					MuseumManager.MuseumSyncData sd = mm.createSyncData(player.getUuid());
					ServerPlayNetworking.send(player, new ModPayloads.MuseumDataSyncS2CPayload(
						sd.donatedItems(), sd.claimedRewardIndices(), sd.abilities()));
					return;
				}

				if (rewardIndex < 0 || rewardIndex >= MuseumManager.REWARDS.size()) return;

				int totalDonated = mm.getTotalDonationCount();
				MuseumManager.RewardDef def = MuseumManager.REWARDS.get(rewardIndex);
				if (totalDonated >= def.requiredCount()) {
					if (def.isAbility()) {
						mm.addAbility(player.getUuid(), def.abilityId());
					} else if (def.rewardItemId() != null) {
						Identifier rewardId = Identifier.of(StardewValley.MOD_ID, def.rewardItemId());
						net.minecraft.item.Item rewardItem = net.minecraft.registry.Registries.ITEM.get(rewardId);
						if (rewardItem != null && rewardItem != net.minecraft.item.Items.AIR) {
							player.getInventory().offerOrDrop(new net.minecraft.item.ItemStack(rewardItem, def.rewardCount()));
						}
					}
					mm.claimReward(player.getUuid(), rewardIndex);
				}

				MuseumManager.MuseumSyncData data = mm.createSyncData(player.getUuid());
				ServerPlayNetworking.send(player, new ModPayloads.MuseumDataSyncS2CPayload(
					data.donatedItems(), data.claimedRewardIndices(), data.abilities()));
			});
		});

		// ====== 矿物商店加工服务 ======
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.GeodeProcessC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld world = (ServerWorld) player.getEntityWorld();
				String inputType = payload.inputType();
				GoldManager manager = GoldManager.get(world);

				if (manager.getGold() < 25) {
					player.sendMessage(Text.translatable("message.stardewvalley.shop.not_enough_gold"), true);
					return;
				}

				net.minecraft.util.Identifier inputId = net.minecraft.util.Identifier.of(MOD_ID, inputType);
				net.minecraft.item.Item inputItem = net.minecraft.registry.Registries.ITEM.get(inputId);
				if (inputItem == null || inputItem == net.minecraft.item.Items.AIR) return;

				boolean hasItem = false;
				int foundSlot = -1;
				for (int i = 0; i < player.getInventory().size(); i++) {
					ItemStack stack = player.getInventory().getStack(i);
					if (stack.getItem() == inputItem && stack.getCount() > 0) {
						hasItem = true;
						foundSlot = i;
						break;
					}
				}
				if (!hasItem) return;

				manager.addGold(-25);
				player.getInventory().getStack(foundSlot).decrement(1);

				Random random = new Random();
				String resultId;
				int resultCount = 1;

				switch (inputType) {
					case "geode", "frozen_geode", "magma_geode", "omni_geode" -> {
						java.util.Map.Entry<String, Integer> geodeResult = rollGeodeOutput(inputType, random);
						resultId = geodeResult.getKey();
						resultCount = geodeResult.getValue();
					}
					case "ancient_treasure_decor" -> { resultId = rollArtifactTrove(random); }
					case "golden_coconut" -> { resultId = rollGoldenCoconut(random); resultCount = 5; }
					case "mystery_box" -> {
						java.util.Map.Entry<String, Integer> result = rollMysteryBox(random, player);
						resultId = result.getKey(); resultCount = result.getValue();
					}
					case "golden_mystery_box" -> {
						java.util.Map.Entry<String, Integer> result = rollGoldenMysteryBox(random, player);
						resultId = result.getKey(); resultCount = result.getValue();
					}
					default -> { return; }
				}

				if (!resultId.contains(":")) resultId = MOD_ID + ":" + resultId;
				Identifier outId = Identifier.tryParse(resultId);
				if (outId == null) return;
				net.minecraft.item.Item resultItem = net.minecraft.registry.Registries.ITEM.get(outId);
				if (resultItem == null || resultItem == net.minecraft.item.Items.AIR) return;

				player.getInventory().offerOrDrop(new ItemStack(resultItem, resultCount));

				for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
					ServerPlayNetworking.send(p, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
				}
			});
		});
	}

	// ====== 戒指效果系统 ======

	private void registerRingEffects() {
		// PlayerTickMixin 注入 PlayerEntity.tick() 处理戒指每tick效果

		// 击杀怪物事件
		net.fabricmc.fabric.api.entity.event.v1.ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, victim, damageSource) -> {
			if (world.isClient()) return;
			if (!(attacker instanceof ServerPlayerEntity player)) return;
			if (!(victim instanceof LivingEntity livingVictim)) return;
			RingEffectHandler.onKill(player, livingVictim);
		});
		// 受伤事件钩住
		net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DAMAGE.register((entity, source, amount) -> {
			if (!(entity instanceof ServerPlayerEntity player)) return true;
			return !RingEffectHandler.onHurt(player, source, amount);
		});
	}

	// ====== 技能系统服务器处理器 ======
	static {
		// 确保 SkillRegistry 类初始化（加载所有技能定义）
	}

	private void registerSkillHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SkillRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerWorld sw = (ServerWorld) context.player().getEntityWorld();
				stardewvalley.modid.skill.SkillDataManager sdm = stardewvalley.modid.skill.SkillDataManager.get(sw);
				java.util.UUID uuid = context.player().getUuid();
				ServerPlayNetworking.send(context.player(), new ModPayloads.SkillSyncS2CPayload(
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FARMING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FARMING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FORAGING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FORAGING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FISHING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FISHING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.MINING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.MINING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.COMBAT),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.COMBAT)
				));
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.SkillSelectC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				stardewvalley.modid.skill.SkillDataManager sdm = stardewvalley.modid.skill.SkillDataManager.get(sw);
				java.util.UUID uuid = player.getUuid();

				stardewvalley.modid.skill.SkillRegistry.Category category = stardewvalley.modid.skill.SkillRegistry.Category.values()[payload.categoryOrdinal()];
				int tier = payload.tier();
				String skillId = payload.skillId();

				if (tier == 1) {
					// 检查等级是否>=5
					int level = getCategoryLevel(player, category);
					if (level < 5) return;

					// 检查技能是否存在且为一级
					stardewvalley.modid.skill.SkillRegistry.SkillEntry entry = stardewvalley.modid.skill.SkillRegistry.get(skillId);
					if (entry == null || entry.tier != 1 || entry.category != category) return;
					if (!entry.enabled) return;

					// 清除旧的二级技能（因为一级技能换了）
					sdm.clearLevel10(uuid, category);
					sdm.setLevel5Skill(uuid, category, skillId);
				} else if (tier == 2) {
					// 检查等级是否>=10
					int level = getCategoryLevel(player, category);
					if (level < 10) return;

					// 检查前置一级技能是否已选
					String tier1Choice = sdm.getLevel5Skill(uuid, category);
					if (tier1Choice == null) return;

					// 检查技能是否存在、为二级、属于正确类别且有正确前置
					stardewvalley.modid.skill.SkillRegistry.SkillEntry entry = stardewvalley.modid.skill.SkillRegistry.get(skillId);
					if (entry == null || entry.tier != 2 || entry.category != category) return;
					if (!entry.enabled) return;
					if (!tier1Choice.equals(entry.prerequisite)) return;

					sdm.setLevel10Skill(uuid, category, skillId);
				}

				// 同步给所有玩家（本客户端也需要更新）
				ModPayloads.SkillSyncS2CPayload syncPayload = new ModPayloads.SkillSyncS2CPayload(
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FARMING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FARMING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FORAGING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FORAGING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FISHING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FISHING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.MINING),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.MINING),
					sdm.getLevel5Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.COMBAT),
					sdm.getLevel10Skill(uuid, stardewvalley.modid.skill.SkillRegistry.Category.COMBAT)
				);
				ServerPlayNetworking.send(player, syncPayload);
			});
		});
	}

	private void registerMasteryHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MasteryRequestC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				java.util.UUID uuid = player.getUuid();
				sendMasterySync(player, sw, uuid);
			});
		});

		ServerPlayNetworking.registerGlobalReceiver(ModPayloads.MasterySelectC2SPayload.ID, (payload, context) -> {
			context.server().execute(() -> {
				ServerPlayerEntity player = context.player();
				ServerWorld sw = (ServerWorld) player.getEntityWorld();
				java.util.UUID uuid = player.getUuid();
				stardewvalley.modid.skill.MasteryManager mgr = stardewvalley.modid.skill.MasteryManager.get(sw);

				stardewvalley.modid.skill.SkillRegistry.Category category = stardewvalley.modid.skill.SkillRegistry.Category.values()[payload.categoryOrdinal()];
				if (mgr.tryMaster(sw, player, category.name())) {
					grantMasteryRewards(player, sw, category);
					player.sendMessage(net.minecraft.text.Text.literal("§a已精通技能: " + categoryName(category) + masteryRewardText(category)), false);
				}
				sendMasterySync(player, sw, uuid);
			});
		});
	}

	private void sendMasterySync(ServerPlayerEntity player, ServerWorld sw, java.util.UUID uuid) {
		stardewvalley.modid.skill.MasteryManager mgr = stardewvalley.modid.skill.MasteryManager.get(sw);
		ServerPlayNetworking.send(player, new ModPayloads.MasterySyncS2CPayload(
			stardewvalley.modid.skill.MasteryManager.getMasteryPoints(sw, uuid),
			stardewvalley.modid.skill.MasteryManager.getMasteryLevel(sw, uuid),
			mgr.isMastered(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FARMING.name()),
			mgr.isMastered(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FORAGING.name()),
			mgr.isMastered(uuid, stardewvalley.modid.skill.SkillRegistry.Category.FISHING.name()),
			mgr.isMastered(uuid, stardewvalley.modid.skill.SkillRegistry.Category.MINING.name()),
			mgr.isMastered(uuid, stardewvalley.modid.skill.SkillRegistry.Category.COMBAT.name())
		));
	}

	private static String categoryName(stardewvalley.modid.skill.SkillRegistry.Category category) {
		return switch (category) {
			case FARMING -> "耕种";
			case FORAGING -> "采集";
			case FISHING -> "钓鱼";
			case MINING -> "挖矿";
			case COMBAT -> "战斗";
		};
	}

	/** 各技能精通奖励提示文本 */
	private static String masteryRewardText(stardewvalley.modid.skill.SkillRegistry.Category category) {
		return switch (category) {
			case FARMING -> "  §e获得：铱镰刀×1";
			case FORAGING -> "  §e解锁：可找到金色迷之盒";
			case FISHING -> "  §e获得：高级铱金鱼竿×1、挑战鱼饵×100";
			case MINING -> "  §e解锁：宝石掉落数量翻倍";
			case COMBAT -> "  §e获得：生命上限+4";
		};
	}

	/** 精通奖励发放：耕种→铱镰刀；钓鱼→高级铱金鱼竿+100挑战鱼饵；战斗→立即应用生命上限加成 */
	private void grantMasteryRewards(ServerPlayerEntity player, ServerWorld sw, stardewvalley.modid.skill.SkillRegistry.Category category) {
		switch (category) {
			case FARMING -> {
				giveItem(player, "iridium_scythe", 1);
			}
			case FISHING -> {
				giveItem(player, "advanced_iridium_rod", 1);
				giveItem(player, "bait_challenge_bait", 100);
			}
			case COMBAT -> {
				stardewvalley.modid.season.CombatLevelManager.get(sw).applyHealthBonus(player);
			}
			default -> {}
		}
	}

	private void giveItem(ServerPlayerEntity player, String itemId, int count) {
		net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, itemId));
		if (item == null || item == net.minecraft.item.Items.AIR) return;
		player.getInventory().offerOrDrop(new net.minecraft.item.ItemStack(item, count));
	}

	private int getCategoryLevel(ServerPlayerEntity player, stardewvalley.modid.skill.SkillRegistry.Category category) {
		ServerWorld sw = (ServerWorld) player.getEntityWorld();
		return switch (category) {
			case FARMING -> stardewvalley.modid.season.FarmingLevelManager.get(sw).getLevel(player.getUuid());
			case FORAGING -> stardewvalley.modid.season.ForagingLevelManager.get(sw).getLevel(player.getUuid());
			case FISHING -> stardewvalley.modid.season.FishingLevelManager.get(sw).getLevel(player.getUuid());
			case MINING -> stardewvalley.modid.season.MiningLevelManager.get(sw).getLevel(player.getUuid());
			case COMBAT -> stardewvalley.modid.season.CombatLevelManager.get(sw).getLevel(player.getUuid());
		};
	}

	private void registerDailyLogic() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				// 雷雨天：每隔1000tick，每个未充电避雷针20%概率充电
				if (world.getTime() % 1000 == 0) {
					WeatherType todayWeather = WeatherState.get(world).todayWeather;
					if (todayWeather == WeatherType.STORM) {
						chargeLightningRodsDuringStorm(world);
					}
				}
				RegistryKey<World> worldKey = world.getRegistryKey();
				long currentDay = world.getTimeOfDay() / 24000L;
				Long lastDay = LAST_DAY.get(worldKey);

				if (lastDay != null && currentDay == lastDay) {
					continue;
				}

				long timeInDay = world.getTimeOfDay() % 24000L;

				if (timeInDay == 1) {
					// 广播当天的共享幸运值
					long day = world.getTimeOfDay() / 24000L;
					float luck = LuckManager.get(world).getDailyLuck(day);
					String luckMsg;
					if (luck > 0.07f) {
						luckMsg = "精灵们今天非常开心！他们会尽可能给所有人带来好运的。";
					} else if (luck > 0.02f) {
						luckMsg = "精灵们今天的心情很不错。你今天要走运了。";
					} else if (luck >= -0.02f && luck <= 0.02f) {
						if (luck >= -0.001f && luck <= 0.001f) {
							luckMsg = "这可罕见了，精灵们今天很冷漠。";
						} else {
							luckMsg = "精灵们今天没有任何感情倾向。因此今天的运势全掌握在你手中。";
						}
					} else if (luck >= -0.07f) {
						luckMsg = "今天精灵们有些烦躁。你要走霉运了。";
					} else {
						luckMsg = "精灵今天十分不满。它们会竭尽全力给你捣蛋的。";
					}
					for (var player : world.getPlayers()) {
						player.sendMessage(Text.literal("§b[每日运势] " + luckMsg), false);
					}
					// 第50天解锁迷之盒提示
					if (currentDay == 50) {
						for (var player : world.getPlayers()) {
							player.sendMessage(Text.literal("§6[提示] 现在可以找到谜之盒了！"), false);
						}
					}
					// 书店播报
					BookStoreManager bookStore = BookStoreManager.get(world);
					if (bookStore.isOpenToday(world) && !bookStore.isHasBroadcasted()) {
						for (var player : world.getPlayers()) {
							player.sendMessage(Text.literal("§b[书摊老板] 书摊老板今天在镇上！"), false);
						}
						bookStore.markBroadcasted();
					}
				} else if (timeInDay == 2) {
					Season.updateCachedSeason(world.getTimeOfDay());
					applyWeather(world);
				} else if (timeInDay == 3) {
					processCrops(world, worldKey);
				} else if (timeInDay == 4) {
					processFarmland(world, worldKey);
				} else if (timeInDay == 5) {
					// 下雨/绿雨/雷雨天灌溉耕地
					WeatherType todayWeather = WeatherState.get(world).todayWeather;
					if (todayWeather == WeatherType.RAIN || todayWeather == WeatherType.GREEN_RAIN || todayWeather == WeatherType.STORM) {
						applyRainWatering(world, worldKey);
					}
					// 洒水器灌溉（在下雨灌溉之后执行）
					SprinklerBlock.waterCrops(world);
				} else if (timeInDay == 6) {
					processGoldSettlement(server, world);
				} else if (timeInDay == 7) {
					processFarmlandDegrade(world, worldKey);
				} else if (timeInDay == 8) {
					OreToolUpgradeManager.get(world).notifyCompletedUpgrades(world);
				} else if (timeInDay == 9) {
					// 蜂房产蜜
					ArtisanEquipmentState.get(world).tickBeeHouses(world, currentDay);
					// 太阳能板每日逻辑
					SolarPanelState.get(world).tickSolarPanels(world, currentDay);
				} else if (timeInDay == 10) {
					AnimalInteractionHandler.resetDaily(currentDay);
				} else if (timeInDay == 11) {
					// 树液采集器产出检查
					TapperState.get(world).tickTappers(world, currentDay);
				} else if (timeInDay == 12) {
					// 星露谷特殊草扩散
					stardewvalley.modid.block.StardewGrassState.get(world).tickGrassSpread(world, currentDay);
				} else if (timeInDay == 13) {
					// 前一天未喂食的动物好感度减10
					AnimalFriendshipManager.get(world).onNewDay(currentDay);
				} else if (timeInDay == 14) {
					// 蟹笼产出检查
					CrabPotHandler.tickCrabPots(world);
				} else if (timeInDay == 15) {
					// 豪华虫饵盒产出（15000 ticks = 15:00）
					DeluxeWormBinHandler.tickDeluxeWormBins(world, worldKey);
					// 蘑菇树桩产出
					MushroomStumpState.get(world).tickMushroomStumps(world);
				} else if (timeInDay == 1001) {
					calculateTomorrowWeather(world);
					LAST_DAY.put(worldKey, currentDay);
				}
			}
		});
	}

	private void applyWeather(ServerWorld world) {
		WeatherState state = WeatherState.get(world);
		WeatherType weather = state.tomorrowWeather;
		state.todayWeather = weather;
		state.setDirty(true);

		switch (weather) {
			case SUN, WIND_SPRING, WIND_FALL, FESTIVAL:
				world.setWeather(26000, 0, false, false);
				break;
			case RAIN, GREEN_RAIN, SNOW:
				world.setWeather(0, 26000, true, false);
				break;
			case STORM:
				world.setWeather(0, 26000, true, true);
				break;
		}

		for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
			ServerPlayNetworking.send(player, new ModPayloads.WeatherSyncS2CPayload(weather.ordinal()));
		}
	}

	private void calculateTomorrowWeather(ServerWorld world) {
		WeatherState state = WeatherState.get(world);
		long currentDay = world.getTimeOfDay() / 24000L;

		// 如果玩家使用了雨水图腾强制覆盖，并且是当天使用的，则保持为RAIN
		if (state.rainTotemOverride && state.rainTotemDay == currentDay) {
			state.tomorrowWeather = WeatherType.RAIN;
			state.rainTotemOverride = false; // 使用后清除标志
			state.lastCalculationDay = currentDay;
			state.setDirty(true);

			String forecast = "雨天";
			for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
				player.sendMessage(net.minecraft.text.Text.literal("§b[天气预报] 明天将是" + forecast), false);
			}
			return;
		}

		WeatherType tomorrow = WeatherCalculator.calculateTomorrow(world);
		state.tomorrowWeather = tomorrow;
		state.lastCalculationDay = currentDay;
		state.rainTotemOverride = false;
		state.setDirty(true);

		String forecast = switch (tomorrow) {
			case SUN -> "晴天";
			case RAIN -> "雨天";
			case STORM -> "雷雨";
			case SNOW -> "雪天";
			case WIND_SPRING -> "花粉微风天";
			case WIND_FALL -> "落叶狂风天";
			case GREEN_RAIN -> "绿雨";
			case FESTIVAL -> "节日";
		};
		for (net.minecraft.server.network.ServerPlayerEntity player : world.getPlayers()) {
			player.sendMessage(net.minecraft.text.Text.literal("§b[天气预报] 明天将是" + forecast), false);
		}
	}

	private void registerPositionRestore() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				CropPositionState state = CropPositionState.get(world);
				RegistryKey<World> key = world.getRegistryKey();
				if (!state.getCropPositions().isEmpty()) {
					BaseCropBlock.CROP_POSITIONS.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
						.addAll(state.getCropPositions());
				}
				// 耕地位置由 FarmlandState 持久化，无需额外恢复
				// 洒水器位置由 SprinklerState 持久化，无需额外恢复
				SprinklerState.get(world);
			}
		});
	}

	private void processCrops(ServerWorld world, RegistryKey<World> worldKey) {
		Set<BlockPos> cropPositions = BaseCropBlock.CROP_POSITIONS.get(worldKey);
		if (cropPositions != null) {
			CropPositionState posState = CropPositionState.get(world);
			Iterator<BlockPos> cropIt = cropPositions.iterator();
			while (cropIt.hasNext()) {
				BlockPos pos = cropIt.next();
				int chunkX = pos.getX() >> 4;
				int chunkZ = pos.getZ() >> 4;
				if (!world.isChunkLoaded(chunkX, chunkZ)) {
					world.getChunk(chunkX, chunkZ);
				}
				BlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof BaseCropBlock cropBlock) {
					cropBlock.onNewDay(world, pos, state);
				} else {
					cropIt.remove();
					posState.removeCropPosition(pos);
				}
			}
		}
	}

	private void processFarmland(ServerWorld world, RegistryKey<World> worldKey) {
		Set<BlockPos> farmlandPositions = stardewvalley.modid.block.FarmlandState.get(world).getPositions();
		if (farmlandPositions != null && !farmlandPositions.isEmpty()) {
			CropPositionState posState = CropPositionState.get(world);
			Iterator<BlockPos> farmIt = farmlandPositions.iterator();
			while (farmIt.hasNext()) {
				BlockPos pos = farmIt.next();
				int chunkX = pos.getX() >> 4;
				int chunkZ = pos.getZ() >> 4;
				if (!world.isChunkLoaded(chunkX, chunkZ)) {
					world.getChunk(chunkX, chunkZ);
				}
				BlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof ModFarmlandBlock farmlandBlock) {
					farmlandBlock.onNewDay(world, pos, state);
				} else {
					farmIt.remove();
					posState.removeFarmlandPosition(pos);
				}
			}
		}
	}

	private void registerTapperBreakHandler() {
		net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (world instanceof ServerWorld sw) {
				// 检查被破坏方块的所有水平方向是否有树液采集器
				for (net.minecraft.util.math.Direction dir : net.minecraft.util.math.Direction.values()) {
					if (dir.getAxis() == net.minecraft.util.math.Direction.Axis.Y) continue;
					BlockPos checkPos = pos.offset(dir);
					net.minecraft.block.BlockState checkState = world.getBlockState(checkPos);
					if (checkState.getBlock() instanceof stardewvalley.modid.block.TapperBlock) {
						// 手动掉落采集器物品（没有战利品表，直接掉落方块物品）
						net.minecraft.item.Item item = checkState.getBlock().asItem();
						if (item != null && item != net.minecraft.item.Items.AIR) {
							net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
								sw, checkPos.getX() + 0.5, checkPos.getY() + 0.5, checkPos.getZ() + 0.5,
								new net.minecraft.item.ItemStack(item)
							);
							sw.spawnEntity(itemEntity);
						}
						world.removeBlock(checkPos, false);
					}
				}
			}
		});
	}

	private void registerArtisanEquipmentTick() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				// 每tick：检测所有设备完成状态，不要求区块加载
				// 只要 world.getTimeOfDay() >= finishTime 就会触发
				ArtisanEquipmentState.get(world).tick(world);
			}
		});
	}

	private void registerStardropSaturationLogic() {
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			for (ServerWorld world : server.getWorlds()) {
				StardropItem.initDefaultSaturationCap(world);
			}
		});

		ServerTickEvents.END_SERVER_TICK.register(server -> {
			for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
				StardropItem.clampPlayerSaturation(player);
			}
		});
	}

	private void applyRainWatering(ServerWorld world, RegistryKey<World> worldKey) {
		Set<BlockPos> farmlandPositions = stardewvalley.modid.block.FarmlandState.get(world).getPositions();
		if (farmlandPositions != null && !farmlandPositions.isEmpty()) {
			for (BlockPos pos : farmlandPositions) {
				int chunkX = pos.getX() >> 4;
				int chunkZ = pos.getZ() >> 4;
				if (!world.isChunkLoaded(chunkX, chunkZ)) {
					world.getChunk(chunkX, chunkZ);
				}
				BlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof ModFarmlandBlock) {
					int currentRetain = state.get(ModFarmlandBlock.RETAIN_DAYS);
					world.setBlockState(pos, state
						.with(FarmlandBlock.MOISTURE, 7)
						.with(ModFarmlandBlock.RETAIN_DAYS, currentRetain > 0 ? currentRetain : 1), 3);
				}
			}
		}
	}

	private void processFarmlandDegrade(ServerWorld world, RegistryKey<World> worldKey) {
		Set<BlockPos> originalFarmland = stardewvalley.modid.block.FarmlandState.get(world).getPositions();
		if (originalFarmland != null && !originalFarmland.isEmpty()) {
			CropPositionState posState = CropPositionState.get(world);
			Set<BlockPos> snapshot = new HashSet<>(originalFarmland);
			for (BlockPos pos : snapshot) {
				int chunkX = pos.getX() >> 4;
				int chunkZ = pos.getZ() >> 4;
				if (!world.isChunkLoaded(chunkX, chunkZ)) {
					world.getChunk(chunkX, chunkZ);
				}
				BlockState state = world.getBlockState(pos);
				if (state.getBlock() instanceof ModFarmlandBlock farmlandBlock) {
					farmlandBlock.onTime5(world, pos, state);
				} else {
					originalFarmland.remove(pos);
					posState.removeFarmlandPosition(pos);
				}
			}
		}
	}

	private void processGoldSettlement(net.minecraft.server.MinecraftServer server, ServerWorld world) {
		GoldManager manager = GoldManager.get(world);
		int earned = manager.settleShipping(world);
		if (earned > 0) {
			server.getPlayerManager().broadcast(
				Text.literal("Shipping box settled: +" + earned + "g"),
				false
			);
		}
		server.getPlayerManager().getPlayerList().forEach(player -> {
			if (player.getEntityWorld() == world) {
				ServerPlayNetworking.send(player, new ModPayloads.GoldSyncS2CPayload(manager.getGold()));
			}
		});
	}

	private void chargeLightningRodsDuringStorm(ServerWorld world) {
		// 遍历LightningRodState中存储的所有避雷针位置
		LightningRodState state = LightningRodState.get(world);
		for (BlockPos pos : state.getRodPositions()) {
			// 强制加载区块
			int chunkX = pos.getX() >> 4;
			int chunkZ = pos.getZ() >> 4;
			if (!world.isChunkLoaded(chunkX, chunkZ)) {
				world.getChunk(chunkX, chunkZ);
			}
			BlockState bs = world.getBlockState(pos);
			if (bs.getBlock() instanceof LightningRodBlock && !bs.get(LightningRodBlock.CHARGED)) {
				// 20%概率充电（受每日运气影响）
				if (world.random.nextFloat() < 0.2f * getLuckMultiplier(world)) {
					// 生成闪电实体击中避雷针
					net.minecraft.entity.LightningEntity lightning = new net.minecraft.entity.LightningEntity(
						net.minecraft.entity.EntityType.LIGHTNING_BOLT, world
					);
					lightning.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
					world.spawnEntity(lightning);
					LightningRodBlock.charge(world, pos);
				}
			}
		}
	}

	private static String getPlayerToolTier(ServerPlayerEntity player, String toolType) {
		String[] tiers = {"iridium", "gold", "steel", "copper", "normal"};
		for (String tier : tiers) {
			String itemName = tier.equals("normal") ? toolType : tier + "_" + toolType;
			net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, itemName));
			if (item == null) continue;
			// Scan inventory slots to confirm the tool is actually present
			for (int i = 0; i < player.getInventory().size(); i++) {
				if (player.getInventory().getStack(i).getItem() == item) {
					return tier;
				}
			}
		}
		return "normal";
	}

	private static int getOreSellPrice(String itemId) {
		return switch (itemId) {
			case "coal" -> 15;
			case "copper_ore" -> 5;
			case "iron_ore" -> 10;
			case "gold_ore" -> 25;
			case "iridium_ore" -> 100;
			case "copper_bar" -> 60;
			case "iron_bar" -> 120;
			case "gold_bar" -> 150;
			case "iridium_bar" -> 1000;
			case "amethyst" -> 130;
			case "aquamarine" -> 180;
			case "diamond" -> 750;
			case "emerald" -> 250;
			case "jade" -> 200;
			case "ruby" -> 250;
			case "topaz" -> 80;
			case "earth_crystal" -> 50;
			case "fire_quartz" -> 100;
			case "prismatic_shard" -> 3000;
			case "geode" -> 50;
			case "frozen_geode" -> 100;
			case "magma_geode" -> 150;
			case "omni_geode" -> 150;
			case "quartz" -> 25;
			case "tear_crystal" -> 75;
			default -> 0;
		};
	}

	// ====== 工作台合成配方 ======
	private record WorkbenchIngredient(String itemId, int count) {}

	private static java.util.List<WorkbenchIngredient> getWorkbenchRecipe(String resultItemId) {
		return switch (resultItemId) {
			// ====== 金属锭转换配方 ======
			case "iron_bar" -> java.util.List.of(new WorkbenchIngredient("copper_bar", 3));
			case "gold_bar" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 2));
			case "keg" -> java.util.List.of(
				new WorkbenchIngredient("wood", 30),
				new WorkbenchIngredient("copper_bar", 1),
				new WorkbenchIngredient("iron_bar", 1),
				new WorkbenchIngredient("oak_resin", 1)
			);
			case "cheese_press" -> java.util.List.of(
				new WorkbenchIngredient("wood", 45),
				new WorkbenchIngredient("misc_stone", 45),
				new WorkbenchIngredient("hardwood", 10),
				new WorkbenchIngredient("copper_bar", 1)
			);
			case "mayonnaise_machine" -> java.util.List.of(
				new WorkbenchIngredient("wood", 15),
				new WorkbenchIngredient("misc_stone", 15),
				new WorkbenchIngredient("earth_crystal", 1),
				new WorkbenchIngredient("copper_bar", 1)
			);
			case "preserves_jar" -> java.util.List.of(
				new WorkbenchIngredient("wood", 50),
				new WorkbenchIngredient("misc_stone", 40),
				new WorkbenchIngredient("coal", 8)
			);
			case "bee_house" -> java.util.List.of(
				new WorkbenchIngredient("wood", 40),
				new WorkbenchIngredient("coal", 8),
				new WorkbenchIngredient("iron_bar", 1),
				new WorkbenchIngredient("maple_syrup", 1)
			);
			case "oil_maker" -> java.util.List.of(
				new WorkbenchIngredient("slime", 50),
				new WorkbenchIngredient("hardwood", 20),
				new WorkbenchIngredient("gold_bar", 1)
			);
			case "dehydrator" -> java.util.List.of(
				new WorkbenchIngredient("wood", 30),
				new WorkbenchIngredient("clay", 2),
				new WorkbenchIngredient("fire_quartz", 1)
			);
			case "loom" -> java.util.List.of(
				new WorkbenchIngredient("wood", 60),
				new WorkbenchIngredient("fiber", 30),
				new WorkbenchIngredient("pine_tar", 1)
			);
			case "cask" -> java.util.List.of(
				new WorkbenchIngredient("wood", 20),
				new WorkbenchIngredient("hardwood", 1)
			);
			case "fish_smoker" -> java.util.List.of(
				new WorkbenchIngredient("fish_sea_jelly", 1),
				new WorkbenchIngredient("fish_river_jelly", 1),
				new WorkbenchIngredient("fish_cave_jelly", 1),
				new WorkbenchIngredient("hardwood", 10)
			);
			case "furnace" -> java.util.List.of(
				new WorkbenchIngredient("misc_stone", 25),
				new WorkbenchIngredient("copper_ore", 25)
			);
			// ====== 树液采集器与避雷针配方 ======
			case "tapper" -> java.util.List.of(
				new WorkbenchIngredient("wood", 40),
				new WorkbenchIngredient("copper_bar", 2)
			);
			case "heavy_tapper" -> java.util.List.of(
				new WorkbenchIngredient("hardwood", 30),
				new WorkbenchIngredient("radioactive_bar", 1)
			);
			case "lightning_rod" -> java.util.List.of(
				new WorkbenchIngredient("iron_bar", 1),
				new WorkbenchIngredient("refined_quartz", 1),
				new WorkbenchIngredient("bat_wing", 5)
			);
			// ====== 烹饪菜品配方 ======
			case "fried_egg" -> java.util.List.of(new WorkbenchIngredient("any_egg", 1));
			case "omelet" -> java.util.List.of(new WorkbenchIngredient("any_egg", 1), new WorkbenchIngredient("any_milk", 1));
			case "salad" -> java.util.List.of(new WorkbenchIngredient("caiji_leek", 1), new WorkbenchIngredient("caiji_dandelion", 1), new WorkbenchIngredient("vinegar", 1));
			case "cheese_cauliflower" -> java.util.List.of(new WorkbenchIngredient("cauliflower", 1), new WorkbenchIngredient("cheese", 1));
			case "baked_fish" -> java.util.List.of(new WorkbenchIngredient("fish_sunfish", 1), new WorkbenchIngredient("fish_bream", 1), new WorkbenchIngredient("wheat_flour", 1));
			case "parsnip_soup" -> java.util.List.of(new WorkbenchIngredient("parsnip", 1), new WorkbenchIngredient("any_milk", 1), new WorkbenchIngredient("vinegar", 1));
			case "vegetable_medley" -> java.util.List.of(new WorkbenchIngredient("tomato", 1), new WorkbenchIngredient("beet", 1));
			case "complete_breakfast" -> java.util.List.of(new WorkbenchIngredient("fried_egg", 1), new WorkbenchIngredient("any_milk", 1), new WorkbenchIngredient("hashbrowns", 1), new WorkbenchIngredient("pancakes", 1));
			case "fried_calamari" -> java.util.List.of(new WorkbenchIngredient("fish_squid", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("any_oil", 1));
			case "strange_bun" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("fish_periwinkle", 1), new WorkbenchIngredient("void_mayonnaise", 1));
			case "lucky_lunch" -> java.util.List.of(new WorkbenchIngredient("fish_sea_cucumber", 1), new WorkbenchIngredient("tortilla", 1), new WorkbenchIngredient("bluejazz", 1));
			case "fried_mushroom" -> java.util.List.of(new WorkbenchIngredient("caiji_commonmushroom", 1), new WorkbenchIngredient("caiji_morel", 1), new WorkbenchIngredient("any_oil", 1));
			case "pizza" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("tomato", 1), new WorkbenchIngredient("cheese", 1));
			case "bean_hotpot" -> java.util.List.of(new WorkbenchIngredient("greenbean", 2));
			case "glazed_yams" -> java.util.List.of(new WorkbenchIngredient("yam", 1), new WorkbenchIngredient("sugar", 1));
			case "carp_surprise" -> java.util.List.of(new WorkbenchIngredient("fish_carp", 4));
			case "hashbrowns" -> java.util.List.of(new WorkbenchIngredient("potato", 1), new WorkbenchIngredient("any_oil", 1));
			case "pancakes" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("any_egg", 1));
			case "salmon_dinner" -> java.util.List.of(new WorkbenchIngredient("fish_salmon", 1), new WorkbenchIngredient("amaranth", 1), new WorkbenchIngredient("kale", 1));
			case "fish_taco" -> java.util.List.of(new WorkbenchIngredient("fish_tuna", 1), new WorkbenchIngredient("tortilla", 1), new WorkbenchIngredient("redcabbage", 1), new WorkbenchIngredient("mayonnaise", 1));
			case "crispy_bass" -> java.util.List.of(new WorkbenchIngredient("fish_largemouth_bass", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("any_oil", 1));
			case "pepper_poppers" -> java.util.List.of(new WorkbenchIngredient("hotpepper", 1), new WorkbenchIngredient("cheese", 1));
			case "bread" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1));
			case "tom_kha_soup" -> java.util.List.of(new WorkbenchIngredient("caiji_coconut", 1), new WorkbenchIngredient("fish_shrimp", 1), new WorkbenchIngredient("caiji_commonmushroom", 1));
			case "trout_soup" -> java.util.List.of(new WorkbenchIngredient("fish_rainbow_trout", 1), new WorkbenchIngredient("fish_green_algae", 1));
			case "chocolate_cake" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("any_egg", 1));
			case "pink_cake" -> java.util.List.of(new WorkbenchIngredient("melon", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("any_egg", 1));
			case "rhubarb_pie" -> java.util.List.of(new WorkbenchIngredient("rhubarb", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1));
			case "cookie" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("any_egg", 1));
			case "spaghetti" -> java.util.List.of(new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("tomato", 1));
			case "fried_eel" -> java.util.List.of(new WorkbenchIngredient("fish_eel", 1), new WorkbenchIngredient("any_oil", 1));
			case "spicy_eel" -> java.util.List.of(new WorkbenchIngredient("fish_eel", 1), new WorkbenchIngredient("hotpepper", 1));
			case "sashimi" -> java.util.List.of(new WorkbenchIngredient("any_fish", 1));
			case "maki_roll" -> java.util.List.of(new WorkbenchIngredient("any_fish", 1), new WorkbenchIngredient("caiji_seaweed", 1), new WorkbenchIngredient("rice", 1));
			case "tortilla" -> java.util.List.of(new WorkbenchIngredient("corn", 1));
			case "red_plate" -> java.util.List.of(new WorkbenchIngredient("redcabbage", 1), new WorkbenchIngredient("radish", 1));
			case "eggplant_parmesan" -> java.util.List.of(new WorkbenchIngredient("eggplant", 1), new WorkbenchIngredient("tomato", 1));
			case "rice_pudding" -> java.util.List.of(new WorkbenchIngredient("any_milk", 1), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("rice", 1));
			case "ice_cream" -> java.util.List.of(new WorkbenchIngredient("any_milk", 1), new WorkbenchIngredient("sugar", 1));
			case "blueberry_tart" -> java.util.List.of(new WorkbenchIngredient("blueberry", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("any_egg", 1));
			case "autums_bounty" -> java.util.List.of(new WorkbenchIngredient("yam", 1), new WorkbenchIngredient("pumpkin", 1));
			case "pumpkin_soup" -> java.util.List.of(new WorkbenchIngredient("pumpkin", 1), new WorkbenchIngredient("any_milk", 1));
			case "super_meal" -> java.util.List.of(new WorkbenchIngredient("bokchoy", 1), new WorkbenchIngredient("cranberries", 1), new WorkbenchIngredient("artichoke", 1));
			case "cranberry_sauce" -> java.util.List.of(new WorkbenchIngredient("cranberries", 1), new WorkbenchIngredient("sugar", 1));
			case "stuffing" -> java.util.List.of(new WorkbenchIngredient("bread", 1), new WorkbenchIngredient("cranberries", 1), new WorkbenchIngredient("caiji_hazelnut", 1));
			case "farmers_lunch" -> java.util.List.of(new WorkbenchIngredient("omelet", 1), new WorkbenchIngredient("parsnip", 1));
			case "survival_burger" -> java.util.List.of(new WorkbenchIngredient("bread", 1), new WorkbenchIngredient("caiji_cavecarrot", 1), new WorkbenchIngredient("eggplant", 1));
			case "dish_of_the_sea" -> java.util.List.of(new WorkbenchIngredient("fish_sardine", 2), new WorkbenchIngredient("hashbrowns", 1));
			case "miners_treat" -> java.util.List.of(new WorkbenchIngredient("caiji_cavecarrot", 2), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("any_milk", 1));
			case "roots_platter" -> java.util.List.of(new WorkbenchIngredient("caiji_cavecarrot", 1), new WorkbenchIngredient("caiji_winterroot", 1));
			case "triple_shot_espresso" -> java.util.List.of(new WorkbenchIngredient("coffee", 3));
			case "seafoam_pudding" -> java.util.List.of(new WorkbenchIngredient("fish_flounder", 1), new WorkbenchIngredient("fish_midnight_carp", 1), new WorkbenchIngredient("squid_ink", 1));
			case "algae_soup" -> java.util.List.of(new WorkbenchIngredient("fish_green_algae", 4));
			case "pale_broth" -> java.util.List.of(new WorkbenchIngredient("fish_white_algae", 2));
			case "plum_pudding" -> java.util.List.of(new WorkbenchIngredient("caiji_wildplum", 2), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1));
			case "artichoke_dip" -> java.util.List.of(new WorkbenchIngredient("artichoke", 1), new WorkbenchIngredient("any_milk", 1));
			case "stir_fry" -> java.util.List.of(new WorkbenchIngredient("caiji_cavecarrot", 1), new WorkbenchIngredient("caiji_commonmushroom", 1), new WorkbenchIngredient("kale", 1), new WorkbenchIngredient("any_oil", 1));
			case "roasted_hazelnuts" -> java.util.List.of(new WorkbenchIngredient("caiji_hazelnut", 3));
			case "pumpkin_pie" -> java.util.List.of(new WorkbenchIngredient("pumpkin", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("any_milk", 1), new WorkbenchIngredient("sugar", 1));
			case "radish_salad" -> java.util.List.of(new WorkbenchIngredient("any_oil", 1), new WorkbenchIngredient("vinegar", 1), new WorkbenchIngredient("radish", 1));
			case "fruit_salad" -> java.util.List.of(new WorkbenchIngredient("blueberry", 1), new WorkbenchIngredient("melon", 1), new WorkbenchIngredient("apricot", 1));
			case "blackberry_cobbler" -> java.util.List.of(new WorkbenchIngredient("caiji_blackberry", 2), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("wheat_flour", 1));
			case "cranberry_candy" -> java.util.List.of(new WorkbenchIngredient("cranberries", 1), new WorkbenchIngredient("apple", 1), new WorkbenchIngredient("sugar", 1));
			case "bruschetta" -> java.util.List.of(new WorkbenchIngredient("bread", 1), new WorkbenchIngredient("any_oil", 1), new WorkbenchIngredient("tomato", 1));
			case "coleslaw" -> java.util.List.of(new WorkbenchIngredient("redcabbage", 1), new WorkbenchIngredient("vinegar", 1), new WorkbenchIngredient("mayonnaise", 1));
			case "fiddlehead_risotto" -> java.util.List.of(new WorkbenchIngredient("any_oil", 1), new WorkbenchIngredient("caiji_fiddleheadfern", 1), new WorkbenchIngredient("garlic", 1));
			case "poppyseed_muffin" -> java.util.List.of(new WorkbenchIngredient("poppy", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("sugar", 1));
			case "chowder" -> java.util.List.of(new WorkbenchIngredient("caiji_clam", 1), new WorkbenchIngredient("any_milk", 1));
			case "lobster_bisque" -> java.util.List.of(new WorkbenchIngredient("fish_lobster", 1), new WorkbenchIngredient("any_milk", 1));
			case "escargot" -> java.util.List.of(new WorkbenchIngredient("fish_snail", 1), new WorkbenchIngredient("garlic", 1));
			case "fish_stew" -> java.util.List.of(new WorkbenchIngredient("fish_crayfish", 1), new WorkbenchIngredient("caiji_mussel", 1), new WorkbenchIngredient("fish_periwinkle", 1), new WorkbenchIngredient("tomato", 1));
			case "maple_bar" -> java.util.List.of(new WorkbenchIngredient("maple_syrup", 1), new WorkbenchIngredient("sugar", 1), new WorkbenchIngredient("wheat_flour", 1));
			case "crab_cakes" -> java.util.List.of(new WorkbenchIngredient("fish_crab", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("any_egg", 1), new WorkbenchIngredient("any_oil", 1));
			case "shrimp_cocktail" -> java.util.List.of(new WorkbenchIngredient("tomato", 1), new WorkbenchIngredient("fish_shrimp", 1), new WorkbenchIngredient("caiji_wildhorseradish", 1));
			case "ginger_ale" -> java.util.List.of(new WorkbenchIngredient("caiji_ginger", 3), new WorkbenchIngredient("sugar", 1));
			case "banana_pudding" -> java.util.List.of(new WorkbenchIngredient("banana", 1), new WorkbenchIngredient("any_milk", 1), new WorkbenchIngredient("sugar", 1));
			case "mango_sticky_rice" -> java.util.List.of(new WorkbenchIngredient("mango", 1), new WorkbenchIngredient("caiji_coconut", 1), new WorkbenchIngredient("rice", 1));
			case "poi" -> java.util.List.of(new WorkbenchIngredient("taroroot", 4));
			case "tropical_curry" -> java.util.List.of(new WorkbenchIngredient("caiji_coconut", 1), new WorkbenchIngredient("pineapple", 1), new WorkbenchIngredient("hotpepper", 1));
			case "squid_ink_ravioli" -> java.util.List.of(new WorkbenchIngredient("squid_ink", 1), new WorkbenchIngredient("wheat_flour", 1), new WorkbenchIngredient("tomato", 1));
			case "moss_soup" -> java.util.List.of(new WorkbenchIngredient("moss", 20));
			// ====== 石英/精炼石英配方 ======
			case "quartz" -> java.util.List.of(new WorkbenchIngredient("minecraft:quartz", 1));
			// ====== 渔具配方 ======
			case "fishtool_dressed_spinner" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 2), new WorkbenchIngredient("cloth", 1));
			case "fishtool_spinner" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 2));
			case "fishtool_trap_bobber" -> java.util.List.of(new WorkbenchIngredient("copper_bar", 1), new WorkbenchIngredient("caiji_sap", 10));
			case "fishtool_cork_bobber" -> java.util.List.of(new WorkbenchIngredient("wood", 10), new WorkbenchIngredient("hardwood", 5), new WorkbenchIngredient("slime", 10));
			case "fishtool_treasure_hunter" -> java.util.List.of(new WorkbenchIngredient("gold_bar", 2));
			case "fishtool_barbed_hook" -> java.util.List.of(new WorkbenchIngredient("copper_bar", 1), new WorkbenchIngredient("iron_bar", 1), new WorkbenchIngredient("gold_bar", 1));
			case "fishtool_quality_bobber" -> java.util.List.of(new WorkbenchIngredient("copper_bar", 1), new WorkbenchIngredient("caiji_sap", 20));
			case "fishtool_sonar_bobber" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 1), new WorkbenchIngredient("refined_quartz", 2));
			// ====== 鱼饵配方 ======
			case "bait_bait" -> java.util.List.of(new WorkbenchIngredient("bug_meat", 1));
			case "bait_magnet" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 1));
			case "bait_wild_bait" -> java.util.List.of(new WorkbenchIngredient("fiber", 10), new WorkbenchIngredient("slime", 5), new WorkbenchIngredient("bug_meat", 5));
			case "bait_magic_bait" -> java.util.List.of(new WorkbenchIngredient("radioactive_ore", 1), new WorkbenchIngredient("bug_meat", 3));
			case "bait_deluxe_bait" -> java.util.List.of(new WorkbenchIngredient("bait_bait", 5), new WorkbenchIngredient("moss", 2));
			case "bait_challenge_bait" -> java.util.List.of(new WorkbenchIngredient("bone_fragment", 5), new WorkbenchIngredient("moss", 2));
			// ====== 虫肉配方 ======
			case "bug_meat" -> java.util.List.of(new WorkbenchIngredient("minecraft:rotten_flesh", 1));
			// ====== 肥料配方 ======
			case "basic_fertilizer" -> java.util.List.of(new WorkbenchIngredient("caiji_sap", 2));
			case "quality_fertilizer" -> java.util.List.of(new WorkbenchIngredient("caiji_sap", 4), new WorkbenchIngredient("any_fish", 1));
			case "deluxe_fertilizer" -> java.util.List.of(new WorkbenchIngredient("iridium_bar", 1), new WorkbenchIngredient("caiji_sap", 5));
			case "basic_retaining_soil" -> java.util.List.of(new WorkbenchIngredient("misc_stone", 2));
			case "quality_retaining_soil" -> java.util.List.of(new WorkbenchIngredient("misc_stone", 3), new WorkbenchIngredient("clay", 1));
			case "deluxe_retaining_soil" -> java.util.List.of(new WorkbenchIngredient("misc_stone", 5), new WorkbenchIngredient("fiber", 3), new WorkbenchIngredient("clay", 1));
			case "speed-gro" -> java.util.List.of(new WorkbenchIngredient("pine_tar", 1), new WorkbenchIngredient("moss", 5));
			case "deluxe_speed-gro" -> java.util.List.of(new WorkbenchIngredient("oak_resin", 1), new WorkbenchIngredient("bone_fragment", 5));
			// ====== 顶级生长激素 ======
			case "hyper_speed-gro" -> java.util.List.of(new WorkbenchIngredient("radioactive_ore", 1), new WorkbenchIngredient("bone_fragment", 3), new WorkbenchIngredient("solar_essence", 1));
			// ====== 宝石合成配方 ======
			case "prismatic_shard" -> java.util.List.of(
				new WorkbenchIngredient("aquamarine", 99),
				new WorkbenchIngredient("emerald", 99),
				new WorkbenchIngredient("topaz", 99),
				new WorkbenchIngredient("ruby", 99),
				new WorkbenchIngredient("amethyst", 99)
			);
			// ====== 精炼物品配方 ======
			case "field_snack" -> java.util.List.of(new WorkbenchIngredient("acorn", 1), new WorkbenchIngredient("maple_seed", 1), new WorkbenchIngredient("pine_cone", 1));
			case "bug_steak" -> java.util.List.of(new WorkbenchIngredient("bug_meat", 10));
			case "life_elixir" -> java.util.List.of(new WorkbenchIngredient("caiji_redmushroom", 1), new WorkbenchIngredient("caiji_purplemushroom", 1), new WorkbenchIngredient("caiji_morel", 1), new WorkbenchIngredient("caiji_chanterelle", 1));
			case "cherry_bomb" -> java.util.List.of(new WorkbenchIngredient("copper_ore", 4), new WorkbenchIngredient("coal", 1));
			case "bomb" -> java.util.List.of(new WorkbenchIngredient("iron_ore", 4), new WorkbenchIngredient("coal", 1));
			case "mega_bomb" -> java.util.List.of(new WorkbenchIngredient("gold_ore", 4), new WorkbenchIngredient("solar_essence", 1), new WorkbenchIngredient("void_essence", 1));
			case "explosive_ammo" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 1), new WorkbenchIngredient("coal", 2));
			case "fairy_dust" -> java.util.List.of(new WorkbenchIngredient("diamond", 1), new WorkbenchIngredient("fairyrose", 1));
			case "monster_musk" -> java.util.List.of(new WorkbenchIngredient("bat_wing", 30), new WorkbenchIngredient("slime", 30));
			case "rain_totem" -> java.util.List.of(new WorkbenchIngredient("hardwood", 1), new WorkbenchIngredient("truffle_oil", 1), new WorkbenchIngredient("pine_tar", 5));
			case "staircase" -> java.util.List.of(new WorkbenchIngredient("misc_stone", 99));
			case "sprinkler" -> java.util.List.of(new WorkbenchIngredient("copper_bar", 1), new WorkbenchIngredient("iron_bar", 1));
			case "quality_sprinkler" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 1), new WorkbenchIngredient("gold_bar", 1), new WorkbenchIngredient("refined_quartz", 1));
			case "iridium_sprinkler" -> java.util.List.of(new WorkbenchIngredient("gold_bar", 1), new WorkbenchIngredient("iridium_bar", 1), new WorkbenchIngredient("battery_pack", 1));
			case "warp_totem_beach" -> java.util.List.of(new WorkbenchIngredient("hardwood", 1), new WorkbenchIngredient("caiji_coral", 2), new WorkbenchIngredient("fiber", 10));
			case "warp_totem_mountains" -> java.util.List.of(new WorkbenchIngredient("hardwood", 1), new WorkbenchIngredient("iron_bar", 1), new WorkbenchIngredient("misc_stone", 25));
			case "warp_totem_farm" -> java.util.List.of(new WorkbenchIngredient("hardwood", 1), new WorkbenchIngredient("honey", 1), new WorkbenchIngredient("fiber", 20));
			case "warp_totem_desert" -> java.util.List.of(new WorkbenchIngredient("hardwood", 2), new WorkbenchIngredient("caiji_coconut", 1), new WorkbenchIngredient("iridium_ore", 4));
			case "warp_totem_island" -> java.util.List.of(new WorkbenchIngredient("hardwood", 5), new WorkbenchIngredient("dragon_tooth", 1), new WorkbenchIngredient("caiji_ginger", 1));
			// ====== 戒指配方 ======
			case "warrior_ring" -> java.util.List.of(new WorkbenchIngredient("iron_bar", 10), new WorkbenchIngredient("coal", 25), new WorkbenchIngredient("tear_crystal", 10));
			case "ring_of_yoba" -> java.util.List.of(new WorkbenchIngredient("gold_bar", 5), new WorkbenchIngredient("iron_bar", 5), new WorkbenchIngredient("diamond", 1));
			case "sturdy_ring" -> java.util.List.of(new WorkbenchIngredient("copper_bar", 2), new WorkbenchIngredient("bug_meat", 25), new WorkbenchIngredient("slime", 25));
			case "iridium_band" -> java.util.List.of(new WorkbenchIngredient("iridium_bar", 5), new WorkbenchIngredient("solar_essence", 50), new WorkbenchIngredient("void_essence", 50));
			case "thorns_ring" -> java.util.List.of(new WorkbenchIngredient("bone_fragment", 50), new WorkbenchIngredient("misc_stone", 50), new WorkbenchIngredient("gold_bar", 1));
			case "glowstone_ring" -> java.util.List.of(new WorkbenchIngredient("solar_essence", 5), new WorkbenchIngredient("iron_bar", 5));
			// ====== 野生种子配方 ======
			case "spring_seeds" -> java.util.List.of(new WorkbenchIngredient("caiji_wildhorseradish", 1), new WorkbenchIngredient("caiji_daffodil", 1), new WorkbenchIngredient("caiji_leek", 1), new WorkbenchIngredient("caiji_dandelion", 1));
			case "summer_seeds" -> java.util.List.of(new WorkbenchIngredient("caiji_spiceberry", 1), new WorkbenchIngredient("grape", 1), new WorkbenchIngredient("caiji_sweetpea", 1));
			case "fall_seeds" -> java.util.List.of(new WorkbenchIngredient("caiji_commonmushroom", 1), new WorkbenchIngredient("caiji_wildplum", 1), new WorkbenchIngredient("caiji_hazelnut", 1), new WorkbenchIngredient("caiji_blackberry", 1));
			case "winter_seeds" -> java.util.List.of(new WorkbenchIngredient("caiji_winterroot", 1), new WorkbenchIngredient("caiji_crystalfruit", 1), new WorkbenchIngredient("caiji_snowyam", 1), new WorkbenchIngredient("caiji_crocus", 1));
			case "ancientfruit_seeds" -> java.util.List.of(new WorkbenchIngredient("ancient_seed", 1));
			case "grass_starter" -> java.util.List.of(new WorkbenchIngredient("fiber", 10));
			case "tealeaves_seeds" -> java.util.List.of(new WorkbenchIngredient("any_wild_seed", 2), new WorkbenchIngredient("fiber", 5), new WorkbenchIngredient("wood", 5));
			case "fiber_seeds" -> java.util.List.of(new WorkbenchIngredient("mixedseeds", 1), new WorkbenchIngredient("caiji_sap", 5), new WorkbenchIngredient("clay", 1));
			default -> null;
		};
	}

	// ====== 以物换物商店配方（沙漠商人/姜岛商人） ======
	private static java.util.List<WorkbenchIngredient> getBarterTradeRecipe(String resultItemId) {
		return switch (resultItemId) {
			// ====== 沙漠商人固定商品 ======
			case "ancient_treasure_decor" -> java.util.List.of(new WorkbenchIngredient("omni_geode", 5));
			case "warp_totem_desert" -> java.util.List.of(new WorkbenchIngredient("omni_geode", 3));
			case "triple_shot_espresso" -> java.util.List.of(new WorkbenchIngredient("diamond", 1));
			case "spicy_eel" -> java.util.List.of(new WorkbenchIngredient("ruby", 1));
			case "mega_bomb" -> java.util.List.of(new WorkbenchIngredient("iridium_ore", 5));
			case "bomb" -> java.util.List.of(new WorkbenchIngredient("quartz", 5));
			// ====== 沙漠商人滚动商品 ======
			case "hay" -> java.util.List.of(new WorkbenchIngredient("omni_geode", 1));
			case "fiber" -> java.util.List.of(new WorkbenchIngredient("misc_stone", 5));
			case "cloth" -> java.util.List.of(new WorkbenchIngredient("aquamarine", 3));
			case "magic_rock_candy" -> java.util.List.of(new WorkbenchIngredient("prismatic_shard", 3));
			case "cheese" -> java.util.List.of(new WorkbenchIngredient("emerald", 1));
			case "spring_seeds" -> java.util.List.of(new WorkbenchIngredient("summer_seeds", 2));
			case "summer_seeds" -> java.util.List.of(new WorkbenchIngredient("fall_seeds", 2));
			case "fall_seeds" -> java.util.List.of(new WorkbenchIngredient("winter_seeds", 2));
			case "winter_seeds" -> java.util.List.of(new WorkbenchIngredient("spring_seeds", 2));
			case "staircase" -> java.util.List.of(new WorkbenchIngredient("jade", 1));
			// ====== 姜岛商人固定商品 ======
			case "warp_totem_farm" -> java.util.List.of(new WorkbenchIngredient("taroroot", 5));
			case "taroroot_seeds" -> java.util.List.of(new WorkbenchIngredient("bone_fragment", 2));
			case "pineapple_seeds" -> java.util.List.of(new WorkbenchIngredient("caiji_magmacap", 1));
			case "golden_coconut" -> java.util.List.of(new WorkbenchIngredient("caiji_coconut", 10));
			case "mahogany_seed" -> java.util.List.of(new WorkbenchIngredient("fish_stingray", 1));
			// ====== 姜岛商人特定商品 ======
			case "galaxy_soul" -> java.util.List.of(new WorkbenchIngredient("radioactive_ore", 10));
			default -> null;
		};
	}

	private static final java.util.Map<String, Integer> BARTER_OUTPUT_COUNT = java.util.Map.ofEntries(
		java.util.Map.entry("hay", 3)
	);

	private static final java.util.Map<String, Integer> CRAFT_OUTPUT_COUNT = java.util.Map.ofEntries(
		java.util.Map.entry("bug_meat", 2),
		java.util.Map.entry("bait_bait", 5),
		java.util.Map.entry("bait_magnet", 3),
		java.util.Map.entry("bait_challenge_bait", 5),
		java.util.Map.entry("bait_deluxe_bait", 5),
		java.util.Map.entry("explosive_ammo", 5),
		java.util.Map.entry("quality_fertilizer", 2),
		java.util.Map.entry("quality_retaining_soil", 2),
		java.util.Map.entry("deluxe_retaining_soil", 2),
		java.util.Map.entry("deluxe_fertilizer", 5),
		java.util.Map.entry("speed-gro", 5),
		java.util.Map.entry("deluxe_speed-gro", 5),
		java.util.Map.entry("hyper_speed-gro", 5),
		java.util.Map.entry("fiber_seeds", 4),
		java.util.Map.entry("spring_seeds", 10),
		java.util.Map.entry("summer_seeds", 10),
		java.util.Map.entry("fall_seeds", 10),
		java.util.Map.entry("winter_seeds", 10)
	);

	private void registerCommands() {
		// 钓鱼模式切换命令
		stardewvalley.modid.fishing.FishingModeManager.registerCommand();

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			dispatcher.register(CommandManager.literal("stardewvalley")
				.then(CommandManager.literal("initialitem")
					.executes(context -> {
						ServerPlayerEntity player = context.getSource().getPlayer();
						if (player == null) return 0;

						UUID uuid = player.getUuid();

						// 工具与指南书：每次使用命令都可再次获得
						String[] toolIds = {"pickaxe", "axe", "hoe", "watering_can"};
						for (String toolId : toolIds) {
							Identifier id = Identifier.of(MOD_ID, toolId);
							Item item = Registries.ITEM.get(id);
							if (item != null) {
								player.getInventory().offerOrDrop(new ItemStack(item));
							}
						}
						Item guideBook = Registries.ITEM.get(Identifier.of(MOD_ID, "guide_book"));
						if (guideBook != null) {
							player.getInventory().offerOrDrop(new ItemStack(guideBook));
						}

						// 作物（种子与作物）只能领取一次
						boolean firstTime = !INITIAL_ITEMS_GIVEN.contains(uuid);
						if (firstTime) {
							INITIAL_ITEMS_GIVEN.add(uuid);
							Item seedItem = Registries.ITEM.get(Identifier.of(MOD_ID, "parsnip_seeds"));
							if (seedItem != null) {
								player.getInventory().offerOrDrop(new ItemStack(seedItem, 15));
							}
							Item cropItem = Registries.ITEM.get(Identifier.of(MOD_ID, "parsnip"));
							if (cropItem != null) {
								player.getInventory().offerOrDrop(new ItemStack(cropItem, 5));
							}
						}

						context.getSource().sendFeedback(() ->
							Text.literal(firstTime ? "已获得初始工具和作物！" : "已获得初始工具！（作物只能领取一次）"), false);
						return 1;
					})
				)
			);

			dispatcher.register(CommandManager.literal("stardewvalley")
				.then(CommandManager.literal("info")
					.then(CommandManager.literal("off")
						.executes(context -> {
							ServerPlayerEntity player = context.getSource().getPlayer();
							if (player == null) return 0;
							ServerWorld world = (ServerWorld) player.getEntityWorld();
							WelcomeMessageState msgState = WelcomeMessageState.get(world);
							msgState.setDisabled(player.getUuid(), true);
							return 1;
						})
					)
				)
			);
		});
	}

	private void registerBlockEvents() {
		PlayerBlockBreakEvents.AFTER.register((world, player, pos, state, entity) -> {
			if (world.isClient()) return;
			ServerWorld serverWorld = (ServerWorld) world;

			// 混合种子/混合花卉种子 3% 掉落（草方块/泥土/沙子/草/星露谷草）；雨天（雨/雷雨）翻倍；受每日运气影响
			float mixedChance = (serverWorld.isRaining() ? 0.06f : 0.03f) * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player);
			// 浣熊日记：额外增加4%概率（加法，已包含在基础概率中）
			if (BookDataManager.get(serverWorld).hasUsedBook(player.getUuid(), "ways_of_the_wild")) {
				mixedChance += 0.04f;
			}
			if (state.getBlock() == Blocks.GRASS_BLOCK || state.getBlock() == Blocks.DIRT || state.getBlock() == Blocks.SAND
				|| state.getBlock() == Blocks.SHORT_GRASS || state.getBlock() == Blocks.TALL_GRASS || state.getBlock() == Blocks.FERN
				|| state.getBlock() == Blocks.LEAF_LITTER || state.getBlock() == stardewvalley.modid.block.ModBlocks.STARDEW_GRASS) {
				if (serverWorld.random.nextFloat() < mixedChance) {
					Item item = Registries.ITEM.get(Identifier.of(MOD_ID, "mixedseeds"));
					if (item != null) {
						dropItem(serverWorld, pos, new ItemStack(item));
					}
				}
				if (stardewvalley.modid.season.Season.fromTimeOfDay(world.getTimeOfDay()) == stardewvalley.modid.season.Season.SUMMER) {
					if (serverWorld.random.nextFloat() < mixedChance) {
						Item item = Registries.ITEM.get(Identifier.of(MOD_ID, "mixedflowerseeds"));
						if (item != null) {
							dropItem(serverWorld, pos, new ItemStack(item));
						}
					}
				}
			}

			// 原木被破坏掉落树液；雨天掉落2个苔藓；绿雨天必定掉落1个苔藓；+3采集经验
			boolean isGreenRain = stardewvalley.modid.weather.WeatherState.get(serverWorld).todayWeather == stardewvalley.modid.weather.WeatherType.GREEN_RAIN;
			if (state.isIn(BlockTags.LOGS)) {
				Item sap = Registries.ITEM.get(Identifier.of(MOD_ID, "caiji_sap"));
				if (sap != null) {
					dropItem(serverWorld, pos, new ItemStack(sap));
				}
				if (isGreenRain) {
					Item moss = Registries.ITEM.get(Identifier.of(MOD_ID, "moss"));
					if (moss != null) {
						dropItem(serverWorld, pos, new ItemStack(moss));
					}
				} else if (serverWorld.isRaining()) {
					Item moss = Registries.ITEM.get(Identifier.of(MOD_ID, "moss"));
					if (moss != null) {
						dropItem(serverWorld, pos, new ItemStack(moss, 2));
					}
				}
				ForagingLevelManager.get(serverWorld).addXp(player.getUuid(), 3);

				// 护林人：25% 概率额外掉落一个原版对应树木的原木（受每日运气影响）
				if (SkillEffectHelper.hasForester(serverWorld, player.getUuid())) {
					if (serverWorld.random.nextFloat() < 0.25f * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player)) {
						Item logItem = state.getBlock().asItem();
						if (logItem != Items.AIR) {
							dropItem(serverWorld, pos, new ItemStack(logItem));
						}
					}
				}

				// 伐木工人：33% 概率掉落一个硬木（受每日运气影响）
				if (SkillEffectHelper.hasLumberjack(serverWorld, player.getUuid())) {
					if (serverWorld.random.nextFloat() < 0.33f * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player)) {
						Item hardwoodItem = Registries.ITEM.get(Identifier.of(MOD_ID, "hardwood"));
						if (hardwoodItem != null) {
							dropItem(serverWorld, pos, new ItemStack(hardwoodItem));
						}
					}
				}
			}

			// 树叶被破坏有10%概率掉落树液；雨天25%概率掉落苔藓；绿雨天12.5%掉落苔藓（均受每日运气影响）
			if (state.isIn(BlockTags.LEAVES)) {
				if (serverWorld.random.nextFloat() < 0.10f * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player)) {
					Item sap = Registries.ITEM.get(Identifier.of(MOD_ID, "caiji_sap"));
					if (sap != null) {
						dropItem(serverWorld, pos, new ItemStack(sap));
					}
				}
				boolean mossDrop = isGreenRain
				? serverWorld.random.nextFloat() < 0.125f * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player)
				: serverWorld.isRaining() && serverWorld.random.nextFloat() < 0.25f * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player);
				if (mossDrop) {
					Item moss = Registries.ITEM.get(Identifier.of(MOD_ID, "moss"));
					if (moss != null) {
						dropItem(serverWorld, pos, new ItemStack(moss));
					}
				}
			}

			// 草被镰刀破坏掉落纤维（原版草 + 星露谷草）
			boolean isVanillaGrass = state.getBlock() == Blocks.SHORT_GRASS || state.getBlock() == Blocks.TALL_GRASS || state.getBlock() == Blocks.FERN;
			boolean isStardewGrass = state.getBlock() instanceof StardewGrassBlock;
			if (isVanillaGrass || isStardewGrass) {
				ItemStack held = player.getMainHandStack();
				Identifier heldId = Registries.ITEM.getId(held.getItem());
				if (heldId.getNamespace().equals(MOD_ID) && heldId.getPath().contains("scythe")) {
					// 原版草：直接掉落纤维（星露谷草由 onBreak → dropStardewItems 处理，不重复掉落）
					if (isVanillaGrass) {
						Item fiber = Registries.ITEM.get(Identifier.of(MOD_ID, "fiber"));
						if (fiber != null) {
							dropItem(serverWorld, pos, new ItemStack(fiber));
						}
					}

					// 金镰刀/铱镰刀：额外处理前方范围内的其他草
					String scythePath = heldId.getPath();
					if ((scythePath.equals("golden_scythe") || scythePath.equals("iridium_scythe")) && !BREAKING_GRASS.contains(pos)) {
						java.util.List<BlockPos> areaPositions = getScytheAreaPositions(pos, player.getHorizontalFacing(), scythePath);
						for (BlockPos areaPos : areaPositions) {
							if (areaPos.equals(pos)) continue;
							if (!BREAKING_GRASS.add(areaPos)) continue;
							try {
								BlockState areaState = serverWorld.getBlockState(areaPos);
								Block areaBlock = areaState.getBlock();
								if (areaBlock == Blocks.SHORT_GRASS || areaBlock == Blocks.TALL_GRASS || areaBlock == Blocks.FERN) {
									Item fiberItem = Registries.ITEM.get(Identifier.of(MOD_ID, "fiber"));
									if (fiberItem != null) {
										dropItem(serverWorld, areaPos, new ItemStack(fiberItem));
									}
									serverWorld.breakBlock(areaPos, false);
								} else if (areaBlock instanceof StardewGrassBlock) {
									// 先手动掉落纤维（breakBlock 不会触发 onBreak → dropStardewItems）
									Item fiberItem = Registries.ITEM.get(Identifier.of(MOD_ID, "fiber"));
									if (fiberItem != null) {
										dropItem(serverWorld, areaPos, new ItemStack(fiberItem));
									}
									serverWorld.breakBlock(areaPos, true, player);
								}
							} finally {
								BREAKING_GRASS.remove(areaPos);
							}
						}
					}
				}
			}

			// ===== Mining XP & Gem Drops from vanilla blocks =====
			Block broken = state.getBlock();
			ItemStack heldStack = player.getMainHandStack();
			if (heldStack.isEmpty()) return;
			Identifier heldId = Registries.ITEM.getId(heldStack.getItem());
			if (!heldId.getPath().contains("pickaxe")) return;

			if (broken == Blocks.STONE || broken == Blocks.COBBLESTONE || broken == Blocks.ANDESITE ||
				broken == Blocks.DIORITE || broken == Blocks.GRANITE || broken == Blocks.BASALT ||
				broken == Blocks.BLACKSTONE || broken == Blocks.DEEPSLATE ||
				broken == Blocks.COBBLED_DEEPSLATE || broken == Blocks.TUFF) {
				MiningLevelManager.get(serverWorld).addXp(player.getUuid(), 1);
				tryMiningGemDrop(serverWorld, player, pos);
			} else if (broken == Blocks.COAL_ORE || broken == Blocks.DEEPSLATE_COAL_ORE) {
				MiningLevelManager.get(serverWorld).addXp(player.getUuid(), 10);
				// 矿工：额外掉落+1煤炭
				if (SkillEffectHelper.hasMiner(serverWorld, player.getUuid())) {
					dropItem(serverWorld, pos, new ItemStack(Items.COAL));
				}
				// 勘探者：额外掉落原版煤炭
				if (SkillEffectHelper.hasProspector(serverWorld, player.getUuid())) {
					int count = 1 + serverWorld.random.nextInt(6);
					dropItem(serverWorld, pos, new ItemStack(Items.COAL, count));
				}
			} else if (broken == Blocks.COPPER_ORE || broken == Blocks.DEEPSLATE_COPPER_ORE) {
				MiningLevelManager.get(serverWorld).addXp(player.getUuid(), 5);
				if (SkillEffectHelper.hasMiner(serverWorld, player.getUuid())) {
					dropItem(serverWorld, pos, new ItemStack(Items.RAW_COPPER));
				}
			} else if (broken == Blocks.IRON_ORE || broken == Blocks.DEEPSLATE_IRON_ORE) {
				MiningLevelManager.get(serverWorld).addXp(player.getUuid(), 12);
				if (SkillEffectHelper.hasMiner(serverWorld, player.getUuid())) {
					dropItem(serverWorld, pos, new ItemStack(Items.RAW_IRON));
				}
			} else if (broken == Blocks.GOLD_ORE || broken == Blocks.DEEPSLATE_GOLD_ORE) {
				MiningLevelManager.get(serverWorld).addXp(player.getUuid(), 18);
				if (SkillEffectHelper.hasMiner(serverWorld, player.getUuid())) {
					dropItem(serverWorld, pos, new ItemStack(Items.RAW_GOLD));
				}
			} else if (broken == Blocks.DIAMOND_ORE || broken == Blocks.DEEPSLATE_DIAMOND_ORE) {
				MiningLevelManager.get(serverWorld).addXp(player.getUuid(), 50);
				if (SkillEffectHelper.hasMiner(serverWorld, player.getUuid())) {
					dropItem(serverWorld, pos, new ItemStack(Items.DIAMOND));
				}
			}

			// ===== 星露谷稿子额外15%几率掉落星露谷宝石 =====
			if (heldId.getNamespace().equals(MOD_ID)) {
				if (serverWorld.random.nextFloat() < 0.15f * getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player)) {
					if (broken == Blocks.NETHER_QUARTZ_ORE) {
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, "quartz"));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					} else if (broken == Blocks.REDSTONE_ORE || broken == Blocks.DEEPSLATE_REDSTONE_ORE) {
						String gemId = serverWorld.random.nextBoolean() ? "ruby" : "fire_quartz";
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, gemId));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					} else if (broken == Blocks.DIAMOND_ORE || broken == Blocks.DEEPSLATE_DIAMOND_ORE) {
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, "diamond"));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					} else if (broken == Blocks.EMERALD_ORE || broken == Blocks.DEEPSLATE_EMERALD_ORE) {
						String gemId = serverWorld.random.nextBoolean() ? "emerald" : "jade";
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, gemId));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					} else if (broken == Blocks.AMETHYST_CLUSTER || broken == Blocks.AMETHYST_BLOCK || broken == Blocks.BUDDING_AMETHYST || broken == Blocks.SMALL_AMETHYST_BUD || broken == Blocks.MEDIUM_AMETHYST_BUD || broken == Blocks.LARGE_AMETHYST_BUD) {
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, "amethyst"));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					} else if (broken == Blocks.GOLD_ORE || broken == Blocks.DEEPSLATE_GOLD_ORE) {
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, "topaz"));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					} else if (broken == Blocks.LAPIS_ORE || broken == Blocks.DEEPSLATE_LAPIS_ORE) {
						Item gem = Registries.ITEM.get(Identifier.of(MOD_ID, "aquamarine"));
						if (gem != null) dropItem(serverWorld, pos, new ItemStack(gem));
					}
				}
			}
		});
	}

	private void registerGrassEvents() {
		// 镰刀右键原版草或星露谷草 → 获得干草
		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			net.minecraft.item.ItemStack held = player.getMainHandStack();
			if (held.isEmpty()) return net.minecraft.util.ActionResult.PASS;
			net.minecraft.util.Identifier heldId = net.minecraft.registry.Registries.ITEM.getId(held.getItem());
			boolean isScythe = heldId.getNamespace().equals(MOD_ID) &&
				(heldId.getPath().equals("scythe") || heldId.getPath().equals("golden_scythe") || heldId.getPath().equals("iridium_scythe"));
			if (!isScythe) return net.minecraft.util.ActionResult.PASS;

			net.minecraft.util.math.BlockPos pos = hitResult.getBlockPos();
			net.minecraft.block.BlockState blockState = world.getBlockState(pos);
			net.minecraft.block.Block block = blockState.getBlock();
			boolean isVanillaGrass = block == net.minecraft.block.Blocks.SHORT_GRASS || block == net.minecraft.block.Blocks.TALL_GRASS || block == net.minecraft.block.Blocks.FERN;
			boolean isStardewGrass = block instanceof StardewGrassBlock;
			if (!isVanillaGrass && !isStardewGrass) return net.minecraft.util.ActionResult.PASS;

			// 客户端显式播放挥手动效
			if (world.isClient()) {
				player.swingHand(hand);
				return net.minecraft.util.ActionResult.SUCCESS;
			}

			ServerWorld sw = (ServerWorld) world;
			String scythePath = heldId.getPath();

			// 处理当前方块
			handleGrassRightClick(sw, player, pos, scythePath);

			// 金镰刀/铱镰刀：额外处理前方范围内的其他草
			if (scythePath.equals("golden_scythe") || scythePath.equals("iridium_scythe")) {
				java.util.List<net.minecraft.util.math.BlockPos> areaPositions = getScytheAreaPositions(pos, player.getHorizontalFacing(), scythePath);
				java.util.Set<net.minecraft.util.math.BlockPos> processed = new java.util.HashSet<>();
				processed.add(pos);
				for (net.minecraft.util.math.BlockPos areaPos : areaPositions) {
					if (!processed.add(areaPos)) continue;
					net.minecraft.block.BlockState areaState = world.getBlockState(areaPos);
					net.minecraft.block.Block areaBlock = areaState.getBlock();
					if (areaBlock == net.minecraft.block.Blocks.SHORT_GRASS || areaBlock == net.minecraft.block.Blocks.TALL_GRASS || areaBlock == net.minecraft.block.Blocks.FERN || areaBlock instanceof StardewGrassBlock) {
						handleGrassRightClick(sw, player, areaPos, scythePath);
					}
				}
			}

			return net.minecraft.util.ActionResult.SUCCESS;
		});
	}

	private void handleGrassRightClick(ServerWorld sw, PlayerEntity player, BlockPos pos, String scythePath) {
		// 基础掉落1个干草，金镰刀50%概率额外1个，铱镰刀33%概率额外1个（均受每日运气影响）
		int hayCount = 1;
		if (scythePath.equals("golden_scythe") && sw.random.nextFloat() < 0.5f * getFinalLuckMultiplier(sw, (ServerPlayerEntity) player)) hayCount = 2;
		else if (scythePath.equals("iridium_scythe") && sw.random.nextFloat() < 0.33f * getFinalLuckMultiplier(sw, (ServerPlayerEntity) player)) hayCount = 2;

		net.minecraft.item.Item hayItem = net.minecraft.registry.Registries.ITEM.get(net.minecraft.util.Identifier.of(MOD_ID, "hay"));
		if (hayItem != null) {
			net.minecraft.entity.ItemEntity itemEntity = new net.minecraft.entity.ItemEntity(
				sw, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
				new net.minecraft.item.ItemStack(hayItem, hayCount)
			);
			sw.spawnEntity(itemEntity);
		}

		sw.playSound(null, pos, net.minecraft.sound.SoundEvents.BLOCK_GRASS_BREAK, net.minecraft.sound.SoundCategory.BLOCKS, 1.0f, 1.0f);
		sw.breakBlock(pos, false, player);
	}

	/**
	 * 根据镰刀类型计算前方范围位置列表
	 * 金镰刀：6格（自身+左右+前+前左+前右）
	 * 铱镰刀：15格（5宽×3深，点击方块为第一行中心）
	 */
	private static java.util.List<BlockPos> getScytheAreaPositions(BlockPos center, net.minecraft.util.math.Direction facing, String scythePath) {
		java.util.List<BlockPos> positions = new java.util.ArrayList<>();
		net.minecraft.util.math.Direction left = facing.rotateYCounterclockwise();
		net.minecraft.util.math.Direction right = facing.rotateYClockwise();

		if (scythePath.equals("golden_scythe")) {
			positions.add(center);
			positions.add(center.offset(left));
			positions.add(center.offset(right));
			positions.add(center.offset(facing));
			positions.add(center.offset(facing).offset(left));
			positions.add(center.offset(facing).offset(right));
		} else if (scythePath.equals("iridium_scythe")) {
			for (int d = 0; d < 3; d++) {
				for (int w = -2; w <= 2; w++) {
					BlockPos p = center;
					if (d > 0) p = p.offset(facing, d);
					if (w < 0) p = p.offset(right, -w);
					else if (w > 0) p = p.offset(left, w);
					positions.add(p);
				}
			}
		}
		return positions;
	}

	private void registerIridiumScytheCropHarvest() {
		// 铱镰刀右键作物 → 范围收获范围内所有成熟作物
		net.fabricmc.fabric.api.event.player.UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
			net.minecraft.item.ItemStack held = player.getMainHandStack();
			if (held.isEmpty()) return net.minecraft.util.ActionResult.PASS;
			net.minecraft.util.Identifier heldId = net.minecraft.registry.Registries.ITEM.getId(held.getItem());
			if (!heldId.getNamespace().equals(MOD_ID) || !heldId.getPath().equals("iridium_scythe")) return net.minecraft.util.ActionResult.PASS;

			net.minecraft.util.math.BlockPos pos = hitResult.getBlockPos();
			net.minecraft.block.BlockState state = world.getBlockState(pos);
			net.minecraft.block.Block block = state.getBlock();

			if (!(block instanceof BaseCropBlock)) return net.minecraft.util.ActionResult.PASS;

			// 客户端显式播放挥手动效
			if (world.isClient()) {
				player.swingHand(hand);
				return net.minecraft.util.ActionResult.SUCCESS;
			}

			ServerWorld sw = (ServerWorld) world;
			java.util.List<BlockPos> positions = getScytheAreaPositions(pos, player.getHorizontalFacing(), "iridium_scythe");

			for (BlockPos cropPos : positions) {
				net.minecraft.block.BlockState cropState = sw.getBlockState(cropPos);
				net.minecraft.block.Block cropBlock = cropState.getBlock();
				if (!(cropBlock instanceof BaseCropBlock baseCrop)) continue;
				if (!baseCrop.isMature(cropState)) continue;

				stardewvalley.modid.crop.CropConfig config = baseCrop.getConfig();
				if (config.regrowCycle() > 0) {
					// 持续性作物：执行右键收获逻辑（与 BaseCropBlock.onUse 一致）
					baseCrop.onUse(cropState, sw, cropPos, player,
						new net.minecraft.util.hit.BlockHitResult(
							new net.minecraft.util.math.Vec3d(cropPos.getX() + 0.5, cropPos.getY() + 0.5, cropPos.getZ() + 0.5),
							net.minecraft.util.math.Direction.UP, cropPos, false
						)
					);
				} else {
					// 一次性作物：执行左键收获（破坏方块触发 onStateReplaced 掉落）
					sw.breakBlock(cropPos, true, player);
				}
			}

			return net.minecraft.util.ActionResult.SUCCESS;
		});
	}

	// Gem data: itemId, sellPrice(x in formula), xpOnDrop
	private static final String[] GEM_IDS = {
		"amethyst", "aquamarine", "diamond", "emerald", "jade", "ruby", "topaz",
		"earth_crystal", "fire_quartz",
		"mysterious_ore", "geode", "frozen_geode", "magma_geode", "omni_geode", "coal",
		"quartz", "tear_crystal"
	};
	private static final double[] GEM_PRICES = {
		130, 180, 750, 250, 200, 250, 80,
		50, 100,
		3000, 50, 100, 150, 150, 50,
		25, 75
	};
	private static final int[] GEM_XP = {
		16, 40, 150, 80, 40, 80, 16,
		15, 20,
		50, 8, 16, 32, 64, 4,
		8, 20
	};
	// Indexes for biome-restricted items
	private static final int IDX_FROZEN_GEODE = 11;
	private static final int IDX_MAGMA_GEODE = 12;

	private static boolean isColdBiome(ServerWorld world, BlockPos pos) {
		String path = world.getBiome(pos).getKey()
			.map(key -> key.getValue().getPath())
			.orElse("plains");
		return path.equals("cold_ocean") || path.equals("deep_cold_ocean") ||
			path.equals("frozen_ocean") || path.equals("deep_frozen_ocean") ||
			path.equals("jagged_peaks") || path.equals("frozen_peaks") ||
			path.equals("frozen_river") || path.equals("grove") ||
			path.equals("snowy_slopes") || path.equals("snowy_taiga") ||
			path.equals("old_growth_spruce_taiga") || path.equals("snowy_beach") ||
			path.equals("snowy_plains") || path.equals("ice_spikes");
	}

	private static void tryMiningGemDrop(ServerWorld world, PlayerEntity player, BlockPos pos) {
		boolean cold = isColdBiome(world, pos);
		java.util.List<Integer> candidates = new ArrayList<>();
		double totalProb = 0;
		for (int i = 0; i < GEM_IDS.length; i++) {
			if (i == IDX_FROZEN_GEODE && !cold) continue;
			if (i == IDX_MAGMA_GEODE && cold) continue;
			candidates.add(i);
			totalProb += 11.0 / (8.0 * GEM_PRICES[i]);
		}
		float luckMult = player instanceof ServerPlayerEntity sp ? getFinalLuckMultiplier(world, sp) : getLuckMultiplier(world);
		if (world.random.nextDouble() >= totalProb * luckMult) return;
		double roll = world.random.nextDouble() * totalProb;
		double acc = 0;
		for (int idx : candidates) {
			acc += 11.0 / (8.0 * GEM_PRICES[idx]);
			if (roll < acc) {
				handleGemDrop(world, player, pos, idx);
				return;
			}
		}
	}

	private static void handleGemDrop(ServerWorld world, PlayerEntity player, BlockPos pos, int idx) {
		MiningLevelManager.get(world).addXp(player.getUuid(), GEM_XP[idx]);
		String id = GEM_IDS[idx];
		if ("mysterious_ore".equals(id)) {
			int iridiumCount = 1 + world.random.nextInt(3); // 1~3
			int goldCount = 1 + world.random.nextInt(4);    // 1~4
			Item iridium = Registries.ITEM.get(Identifier.of(MOD_ID, "iridium_ore"));
			Item gold = Registries.ITEM.get(Identifier.of(MOD_ID, "gold_ore"));
			if (iridium != null) dropItem(world, pos, new ItemStack(iridium, iridiumCount));
			if (gold != null) dropItem(world, pos, new ItemStack(gold, goldCount));
			float luckMult = player instanceof ServerPlayerEntity sp ? getFinalLuckMultiplier(world, sp) : getLuckMultiplier(world);
			if (world.random.nextFloat() < 0.25f * luckMult) {
				Item prismatic = Registries.ITEM.get(Identifier.of(MOD_ID, "prismatic_shard"));
				if (prismatic != null) dropItem(world, pos, new ItemStack(prismatic));
			}
		} else {
			// 地质学家：50% 概率掉落两个宝石（受每日运气+幸运戒指影响）
			boolean isGeode = id.equals("geode") || id.equals("frozen_geode") || id.equals("magma_geode") || id.equals("omni_geode");
			boolean doubleDrop = false;
			if (isGeode) {
				// 挖掘者：晶球掉落数量双倍
				if (SkillEffectHelper.hasExcavator(world, player.getUuid())) {
					doubleDrop = true;
				}
			} else {
				// 地质学家：50% 概率额外多掉落一个（受每日运气+幸运戒指影响）
				float luckMult = player instanceof ServerPlayerEntity sp ? getFinalLuckMultiplier(world, sp) : getLuckMultiplier(world);
				if (SkillEffectHelper.hasGeologist(world, player.getUuid()) && world.random.nextFloat() < 0.50f * luckMult) {
					doubleDrop = true;
				}
			}
			int count = doubleDrop ? 2 : 1;
			// 采矿精通：掉落宝石数量翻倍
			if (stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(world, player.getUuid(), stardewvalley.modid.skill.SkillRegistry.Category.MINING)) {
				count *= 2;
			}
			Item item = Registries.ITEM.get(Identifier.of(MOD_ID, id));
			if (item != null) dropItem(world, pos, new ItemStack(item, count));
		}
	}

	private void registerChickenFeeding() {
		net.fabricmc.fabric.api.event.player.UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
			net.minecraft.item.ItemStack stack = player.getStackInHand(hand);
			if (stack.isEmpty()) return net.minecraft.util.ActionResult.PASS;

			// 检查是否为干草
			net.minecraft.util.Identifier heldId = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
			boolean isHay = heldId.getNamespace().equals(MOD_ID) && heldId.getPath().equals("hay");

			// 鸡：原版breedingItem（种子）绑定关系
			if (!isHay && entity instanceof net.minecraft.entity.passive.ChickenEntity chicken) {
				if (world.isClient()) return net.minecraft.util.ActionResult.PASS;
				if (stack.get(net.minecraft.component.DataComponentTypes.FOOD) != null && chicken.isBreedingItem(stack)) {
					ChickenBindingManager.get((net.minecraft.server.world.ServerWorld) world).bind((net.minecraft.server.world.ServerWorld) world, chicken.getUuid(), player.getUuid());
				}
				return net.minecraft.util.ActionResult.PASS;
			}

			if (!isHay) return net.minecraft.util.ActionResult.PASS;

			// 检查是否为可喂食动物
			boolean isFeedable = entity instanceof net.minecraft.entity.passive.ChickenEntity ||
				entity instanceof net.minecraft.entity.passive.SheepEntity ||
				entity instanceof net.minecraft.entity.passive.GoatEntity ||
				entity instanceof net.minecraft.entity.passive.CowEntity ||
				entity instanceof net.minecraft.entity.passive.PigEntity;
			if (!isFeedable) return net.minecraft.util.ActionResult.PASS;

			// 客户端：播放挥手动效
			if (world.isClient()) {
				player.swingHand(hand);
				return net.minecraft.util.ActionResult.SUCCESS;
			}

			// 服务端：消耗干草
			if (!player.isCreative()) stack.decrement(1);

			net.minecraft.server.world.ServerWorld sw = (net.minecraft.server.world.ServerWorld) world;

			// 心动粒子 + 声音
			sw.spawnParticles(
				net.minecraft.particle.ParticleTypes.HEART,
				entity.getX(), entity.getY() + 1, entity.getZ(),
				3, 0.3, 0.3, 0.3, 0.1
			);
			sw.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
				net.minecraft.sound.SoundEvents.ENTITY_GENERIC_EAT, net.minecraft.sound.SoundCategory.NEUTRAL, 1.0f, 1.0f);

			// 触发心动模式（繁殖）
			if (entity instanceof net.minecraft.entity.passive.AnimalEntity animal) {
				animal.setLoveTicks(600);
			}

			// 鸡：记录绑定关系（用于鸡舍大师判定）
			if (entity instanceof net.minecraft.entity.passive.ChickenEntity chicken) {
				ChickenBindingManager.get(sw).bind(sw, chicken.getUuid(), player.getUuid());
			}

			return net.minecraft.util.ActionResult.SUCCESS;
		});
	}

	private void registerCombatEvents() {
		// 杀死原版怪物获得其最大生命值一半的经验
		ServerEntityCombatEvents.AFTER_KILLED_OTHER_ENTITY.register((world, attacker, victim, damageSource) -> {
			if (world.isClient()) return;
			if (!(attacker instanceof net.minecraft.server.network.ServerPlayerEntity player)) return;
			if (!(world instanceof ServerWorld serverWorld)) return;

			// 只对原版怪物生效
			net.minecraft.util.Identifier victimId = net.minecraft.registry.Registries.ENTITY_TYPE.getId(victim.getType());
			if (!victimId.getNamespace().equals("minecraft")) return;

			float maxHealth = victim.getMaxHealth();
			int xpAmount = Math.max(1, (int) (maxHealth / 2.0f));
			CombatLevelManager.get(serverWorld).addXp(player, xpAmount);
		});

		// 玩家登录时应用生命加成
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ServerPlayerEntity player = handler.getPlayer();
			ServerWorld world = (ServerWorld) player.getEntityWorld();
			CombatLevelManager.get(world).applyHealthBonus(player);
		});
	}

	private void registerWelcomeMessage() {
		net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			server.execute(() -> {
				ServerPlayerEntity player = handler.getPlayer();
				ServerWorld world = (ServerWorld) player.getEntityWorld();

				// 首次进入世界赠送任务书
				WelcomeMessageState msgState = WelcomeMessageState.get(world);
				if (!msgState.hasReceivedGuideBook(player.getUuid())) {
					Item guideBook = Registries.ITEM.get(Identifier.of(MOD_ID, "guide_book"));
					if (guideBook != null) {
						player.getInventory().offerOrDrop(new ItemStack(guideBook));
						msgState.markGuideBookReceived(player.getUuid());
					}
				}

				if (!msgState.isDisabled(player.getUuid())) {
					player.sendMessage(Text.literal("§aThanks for playing §bStardewValley §aMod by §eideal520!"));
					player.sendMessage(Text.literal(" §7Found a bug or have valuable suggestions? Report it on my GitHub: https://github.com/ideal520520 | Email: ideal520520@gmail.com"));
					player.sendMessage(Text.literal(" §8Type §7/stardewvalley info off §8to disable this message"));
					player.sendMessage(Text.literal(""));
				}
			});
		});
	}

	private static void dropItem(ServerWorld world, BlockPos pos, ItemStack stack) {
		net.minecraft.entity.ItemEntity entity = new net.minecraft.entity.ItemEntity(world,
			pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack);
		world.spawnEntity(entity);
	}

	private static void spawnMaceSmashParticle(ServerWorld world, double x, double y, double z) {
		// 重锤猛击粒子效果
		world.spawnParticles(ParticleTypes.EXPLOSION, x, y + 0.5, z, 5, 1.5, 0.2, 1.5, 0.1);
		world.spawnParticles(ParticleTypes.SWEEP_ATTACK, x, y + 0.5, z, 8, 1.5, 0.1, 1.5, 0.0);
	}

	private static void spawnMaceHeavySmashEffect(ServerWorld world, double x, double y, double z) {
		BlockPos groundPos = BlockPos.ofFloored(x, y - 0.1, z);
		BlockState groundBlock = world.getBlockState(groundPos);
		if (!groundBlock.isAir()) {
			world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.DUST_PILLAR, groundBlock),
				x, y, z, 200, 1.2, 0.4, 1.2, 0.05);
		}
		world.playSound(null, x, y, z,
			SoundEvents.ITEM_MACE_SMASH_GROUND_HEAVY, SoundCategory.PLAYERS, 0.5f, 1.0f);
	}

	public static void tryExplosionGemDrop(ServerWorld world, BlockPos pos) {
		boolean cold = isColdBiome(world, pos);
		java.util.List<Integer> candidates = new java.util.ArrayList<>();
		double totalProb = 0;
		for (int i = 0; i < GEM_IDS.length; i++) {
			if (i == IDX_FROZEN_GEODE && !cold) continue;
			if (i == IDX_MAGMA_GEODE && cold) continue;
			candidates.add(i);
			totalProb += 7.04 / (8.0 * GEM_PRICES[i]);
		}
		float luckMult;
		UUID placerUuid = BombBlock.getPlacer(pos);
		if (placerUuid != null && world.getServer() != null && world.getServer().getPlayerManager().getPlayer(placerUuid) instanceof ServerPlayerEntity sp) {
			luckMult = getFinalLuckMultiplier(world, sp);
		} else {
			luckMult = getLuckMultiplier(world);
		}
		if (world.random.nextDouble() >= totalProb * luckMult) return;
		double roll = world.random.nextDouble() * totalProb;
		double acc = 0;
		for (int idx : candidates) {
			acc += 7.04 / (8.0 * GEM_PRICES[idx]);
			if (roll < acc) {
				handleExplosionGemDrop(world, pos, idx, luckMult);
				return;
			}
		}
	}

	private static void handleExplosionGemDrop(ServerWorld world, BlockPos pos, int idx, float luckMult) {
		String id = GEM_IDS[idx];
		if ("mysterious_ore".equals(id)) {
			int iridiumCount = 1 + world.random.nextInt(3);
			int goldCount = 1 + world.random.nextInt(4);
			Item iridium = Registries.ITEM.get(Identifier.of(MOD_ID, "iridium_ore"));
			Item gold = Registries.ITEM.get(Identifier.of(MOD_ID, "gold_ore"));
			if (iridium != null) dropItem(world, pos, new ItemStack(iridium, iridiumCount));
			if (gold != null) dropItem(world, pos, new ItemStack(gold, goldCount));
			if (world.random.nextFloat() < 0.25f * luckMult) {
				Item prismatic = Registries.ITEM.get(Identifier.of(MOD_ID, "prismatic_shard"));
				if (prismatic != null) dropItem(world, pos, new ItemStack(prismatic));
			}
		} else {
			boolean isGeode = id.equals("geode") || id.equals("frozen_geode") || id.equals("magma_geode") || id.equals("omni_geode");
			int count = (isGeode && world.random.nextFloat() < 0.5f * luckMult) ? 2 : 1;
			// 采矿精通：掉落宝石数量翻倍
			UUID placerUuid = BombBlock.getPlacer(pos);
			if (placerUuid != null && stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(world, placerUuid, stardewvalley.modid.skill.SkillRegistry.Category.MINING)) {
				count *= 2;
			}
			Item item = Registries.ITEM.get(Identifier.of(MOD_ID, id));
			if (item != null) dropItem(world, pos, new ItemStack(item, count));
		}
	}

	/** 获取每日运气系数：1.0 + dailyLuck（范围 0.9 ~ 1.1） */
	public static float getLuckMultiplier(ServerWorld world) {
		long currentDay = world.getTimeOfDay() / 24000L;
		float dailyLuck = LuckManager.get(world).getDailyLuck(currentDay);
		return 1.0f + dailyLuck;
	}

	/** 获取最终运气系数：1.0 + dailyLuck + 幸运总等级（来源于 RING_LUCK_BUFF） */
	public static float getFinalLuckMultiplier(ServerWorld world, ServerPlayerEntity player) {
		long currentDay = world.getTimeOfDay() / 24000L;
		float dailyLuck = LuckManager.get(world).getDailyLuck(currentDay);
		int luckBuffLevel = 0;
		StatusEffectInstance ringEffect = player.getStatusEffect(ModStatusEffects.RING_LUCK_BUFF);
		if (ringEffect != null) {
			luckBuffLevel += ringEffect.getAmplifier() + 1;
		}
		return 1.0f + dailyLuck + luckBuffLevel * 0.05f;
	}

	private static int getShopPrice(String itemId, long timeOfDay) {
		int price = stardewvalley.modid.gui.ShopScreen.getItemPrice(itemId);
		if (price > 0) return price;
		long totalDays = timeOfDay / 24000L;
		int year = (int)(totalDays / 112) + 1;
		return switch (itemId) {
			// Harvey
			case "energy_tonic" -> 1000;
			case "muscle_remedy" -> 1000;
			// Marnie goods
			case "hay" -> 50;
			case "milk_pail" -> 1000;
			case "shears" -> 1000;
			case "auto_grafter" -> 25000;
			case "gold_egg" -> 100000;
			// Marnie livestock (minecraft namespace)
			case "minecraft:chicken_spawn_egg" -> 800;
			case "minecraft:cow_spawn_egg" -> 1500;
			case "minecraft:goat_spawn_egg" -> 4000;
			case "minecraft:sheep_spawn_egg" -> 8000;
			case "minecraft:rabbit_spawn_egg" -> 8000;
			case "minecraft:pig_spawn_egg" -> 16000;
			// Dwarf Shop
			case "dwarvish_safety_manual" -> 4000;
			case "life_elixir" -> 2000;
			case "oil_of_garlic" -> 3000;
			case "cherry_bomb" -> 400;
			case "bomb" -> 900;
			case "mega_bomb" -> 1500;
			case "miners_treat" -> 1000;
			// Marlon weapons
			case "rusty_sword" -> 250;
			case "wooden_blade" -> 250;
			case "bone_sword" -> 350;
			case "iron_dirk" -> 500;
			case "silver_saber" -> 750;
			case "elf_blade" -> 750;
			case "steel_smallsword" -> 750;
			case "cutlass" -> 750;
			case "pirates_sword" -> 850;
			case "wood_mallet" -> 2000;
			case "claymore" -> 2000;
			case "templars_blade" -> 4000;
			case "crystal_dagger" -> 4500;
			case "steel_falchion" -> 9000;
			case "obsidian_edge" -> 9000;
			case "lava_katana" -> 25000;
			case "galaxy_sword" -> 50000;
			case "galaxy_dagger" -> 35000;
			case "galaxy_hammer" -> 75000;
			// Marlon rings
			case "amethyst_ring" -> 1000;
			case "topaz_ring" -> 1000;
			case "aquamarine_ring" -> 2500;
			case "jade_ring" -> 2500;
			case "emerald_ring" -> 5000;
			case "ruby_ring" -> 5000;
			// Marlon boots/shoes
			case "sneakers" -> 500;
			case "rubber_boots" -> 500;
			case "leather_boots" -> 500;
			case "work_boots" -> 700;
			case "combat_boots" -> 1250;
			case "tundra_boots" -> 750;
			case "thermal_boots" -> 1000;
			case "dark_boots" -> 2500;
			case "firewalker_boots" -> 2000;
			case "genie_shoes" -> 3500;
			case "space_boots" -> 5000;
			case "cowboy_boots" -> 1500;
			case "emilys_magic_boots" -> 6000;
			case "leprechaun_shoes" -> 3000;
			case "cinderclown_shoes" -> 8000;
			case "mermaid_boots" -> 10000;
			case "dragonscale_boots" -> 12000;
			case "crystal_shoes" -> 7000;
			// Marlon other
			case "explosive_ammo" -> 300;
			case "slingshot" -> 500;
			case "master_slingshot" -> 1000;
			case "slime_charmer_ring" -> 25000;
			case "savage_ring" -> 25000;
			case "burglars_ring" -> 20000;
			case "vampire_ring" -> 15000;
			case "crabshell_ring" -> 15000;
			case "napalm_ring" -> 30000;
			case "insect_head" -> 10000;
			// Sandy 固定商品
			case "cactusfruit_seeds" -> 150;
			case "rhubarb_seeds" -> 100;
			case "starfruit_seeds" -> 400;
			case "beet_seeds" -> 20;
			// Sandy 滚动商品
			case "caiji_coconut" -> 200;
			case "cactusfruit" -> 150;
			case "omni_geode" -> 500;
			case "deluxe_speed-gro" -> 80;
			case "honey" -> 200;
			case "deluxe_retaining_soil" -> 200;
			case "ice_cream" -> 240;
			// Gus
			case "beer" -> 400;
			case "salad" -> 220;
			case "bread" -> 120;
			case "spaghetti" -> 240;
			case "pizza" -> 600;
			case "coffee" -> 300;
			case "tropical_curry" -> 600;
			case "pale_ale" -> 1000;
			case "mead" -> 800;
			case "cranberry_candy" -> 400;
			case "mango_wine_gold" -> 5000;
			// Robin（按年份动态价格）
			case "wood" -> year <= 1 ? 10 : 50;
			case "misc_stone" -> year <= 1 ? 20 : 100;
			case "wood_chipper" -> 1000;
			default -> 0;
		};
	}

	private static int getShopMaxBuy(String itemId) {
		return switch (itemId) {
			case "caiji_coconut" -> 10;
			case "omni_geode" -> 3;
			default -> 0;
		};
	}

	private static List<ModPayloads.BookStoreItem> getBookStoreStock(ServerPlayerEntity player, ServerWorld world) {
		List<ModPayloads.BookStoreItem> stock = new ArrayList<>();
		long totalDays = world.getTimeOfDay() / 24000L;
		Random random = new Random(totalDays);
		UUID playerUuid = player.getUuid();

		// ===== 常驻商品 =====
		stock.add(new ModPayloads.BookStoreItem("price_catalogue", 3000, false, null, 0, null, 0));
		stock.add(new ModPayloads.BookStoreItem("horse_the_book", 25000, false, null, 0, null, 0));
		stock.add(new ModPayloads.BookStoreItem("ol_slitherlegs", 25000, false, null, 0, null, 0));
		stock.add(new ModPayloads.BookStoreItem("queen_of_sauce_cookbook", 50000, false, null, 0, null, 0));

		// ===== 风之道：检测玩家是否使用过pt.1 =====
		BookDataManager bookManager = BookDataManager.get(world);
		if (bookManager.hasUsedBook(playerUuid, "way_of_the_wind_pt._1")) {
			stock.add(new ModPayloads.BookStoreItem("way_of_the_wind_pt._2", 35000, false, null, 0, null, 0));
		} else {
			stock.add(new ModPayloads.BookStoreItem("way_of_the_wind_pt._1", 15000, false, null, 0, null, 0));
		}

		// ===== book_of_stars 25%概率出现 =====
		if (random.nextFloat() < 0.25f) {
			stock.add(new ModPayloads.BookStoreItem("book_of_stars", 15000, false, null, 0, null, 0));
		}

		// ===== 轮流商品：从5本中随机选1个 =====
		String[] rotatingBooks = {
			"stardew_valley_almanac", "bait_and_bobber",
			"mining_monthly", "combat_quarterly", "woodcutters_weekly"
		};
		int rotatingIndex = random.nextInt(rotatingBooks.length);
		int[] randomPrices = {5000, 8000, 10000};
		int rp = randomPrices[random.nextInt(randomPrices.length)];
		stock.add(new ModPayloads.BookStoreItem(rotatingBooks[rotatingIndex], rp, false, null, 0, null, 0));

		// ===== 第二年之后：从11本中随机选2个 =====
		if (totalDays >= 112) {
			String[] year2Books = {
				"the_alleyway_buffet", "the_art_o_crabbing", "dwarvish_safety_manual",
				"jewels_of_the_sea", "ways_of_the_wild", "woodys_secret",
				"jack_be_nimble_jack_be_thick", "friendship_101", "monster_compendium",
				"mapping_cave_systems", "treasure_appraisal_guide"
			};
			// Fisher-Yates 部分洗牌取前2个
			int[] indices = new int[year2Books.length];
			for (int i = 0; i < year2Books.length; i++) indices[i] = i;
			for (int i = year2Books.length - 1; i > 0; i--) {
				int swap = random.nextInt(i + 1);
				int temp = indices[i];
				indices[i] = indices[swap];
				indices[swap] = temp;
			}
			stock.add(new ModPayloads.BookStoreItem(year2Books[indices[0]], 20000, false, null, 0, null, 0));
			stock.add(new ModPayloads.BookStoreItem(year2Books[indices[1]], 20000, false, null, 0, null, 0));
		}

		// ===== 交换物品 - 保持不变 =====
		stock.add(new ModPayloads.BookStoreItem("fish_cave_jelly", 0, true, "jewels_of_the_sea", 1, null, 0, 3));
		stock.add(new ModPayloads.BookStoreItem("fish_river_jelly", 0, true, "jewels_of_the_sea", 1, null, 0, 3));
		stock.add(new ModPayloads.BookStoreItem("fish_sea_jelly", 0, true, "jewels_of_the_sea", 1, null, 0, 3));
		stock.add(new ModPayloads.BookStoreItem("hardwood", 0, true, "woodys_secret", 1, null, 0, 20));
		stock.add(new ModPayloads.BookStoreItem("stuffing", 0, true, "jack_be_nimble_jack_be_thick", 1, null, 0, 3));
		stock.add(new ModPayloads.BookStoreItem("slime", 0, true, "monster_compendium", 2, null, 0, 100));
		stock.add(new ModPayloads.BookStoreItem("mystery_box", 0, true, "book_of_mysteries", 1, null, 0, 7));
		stock.add(new ModPayloads.BookStoreItem("spicy_eel", 0, true, "treasure_appraisal_guide", 1, null, 0, 3));
		stock.add(new ModPayloads.BookStoreItem("ancient_treasure_decor", 0, true, "treasure_appraisal_guide", 1, null, 0, 3));
		stock.add(new ModPayloads.BookStoreItem("fairy_dust", 0, true, "book_of_stars", 1, null, 0, 8));
		stock.add(new ModPayloads.BookStoreItem("pepper_poppers", 0, true, "stardew_valley_almanac", 1, null, 0, 2));
		stock.add(new ModPayloads.BookStoreItem("bait_deluxe_bait", 0, true, "bait_and_bobber", 1, null, 0, 30));
		stock.add(new ModPayloads.BookStoreItem("wood", 0, true, "woodcutters_weekly", 1, null, 0, 100));
		stock.add(new ModPayloads.BookStoreItem("coal", 0, true, "mining_monthly", 1, null, 0, 20));
		stock.add(new ModPayloads.BookStoreItem("monster_musk", 0, true, "combat_quarterly", 1, null, 0, 1));

		return stock;
	}

	// ====== 矿物商店加工 - 随机产出表 ======

	/** 晶球产出（复用晶球破开机的逻辑，含数量分布） */
	private static java.util.Map.Entry<String, Integer> rollGeodeOutput(String geodeType, Random random) {
		double roll = random.nextDouble();
		double cum = 0.0;
		String id;
		int count = 1;
		// 数量分布: 30%→1, 30%→3, 30%→5, 9%→10, 1%→20
		java.util.function.Supplier<Integer> qty = () -> {
			double r = random.nextDouble();
			if (r < 0.30) return 1;
			if (r < 0.60) return 3;
			if (r < 0.90) return 5;
			if (r < 0.99) return 10;
			return 20;
		};
		switch (geodeType) {
			case "geode" -> {
				if (roll < (cum += 1.0/32)) { id = "dwarvish_helm"; }
				else if (roll < (cum += 1.0/16)) { id = "earth_crystal"; }
				else if (roll < (cum += 1.0/32)) { id = "alamite"; }
				else if (roll < (cum += 1.0/32)) { id = "calcite"; }
				else if (roll < (cum += 1.0/32)) { id = "celestine"; }
				else if (roll < (cum += 1.0/32)) { id = "granite"; }
				else if (roll < (cum += 1.0/32)) { id = "jagoite"; }
				else if (roll < (cum += 1.0/32)) { id = "jamborite"; }
				else if (roll < (cum += 1.0/32)) { id = "limestone"; }
				else if (roll < (cum += 1.0/32)) { id = "malachite"; }
				else if (roll < (cum += 1.0/32)) { id = "mudstone"; }
				else if (roll < (cum += 1.0/32)) { id = "nekoite"; }
				else if (roll < (cum += 1.0/32)) { id = "orpiment"; }
				else if (roll < (cum += 1.0/32)) { id = "petrified_slime"; }
				else if (roll < (cum += 1.0/32)) { id = "sandstone"; }
				else if (roll < (cum += 1.0/32)) { id = "slate"; }
				else if (roll < (cum += 1.0/32)) { id = "thunder_egg"; }
				else if (roll < (cum += 1.0/8)) { id = "misc_stone"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "clay"; count = random.nextDouble() < 0.33 ? qty.get() : 1; }
				else if (roll < (cum += 1.0/12)) { id = "coal"; count = qty.get(); }
				else if (roll < (cum += 1.0/12)) { id = "copper_ore"; count = qty.get(); }
				else { id = "iron_ore"; count = qty.get(); }
			}
			case "frozen_geode" -> {
				if (roll < (cum += 1.0/30)) { id = "ancient_drum"; }
				else if (roll < (cum += 1.0/16)) { id = "tear_crystal"; }
				else if (roll < (cum += 1.0/30)) { id = "aerinite"; }
				else if (roll < (cum += 1.0/30)) { id = "esperite"; }
				else if (roll < (cum += 1.0/30)) { id = "fairy_stone"; }
				else if (roll < (cum += 1.0/30)) { id = "fluorapatite"; }
				else if (roll < (cum += 1.0/30)) { id = "geminite"; }
				else if (roll < (cum += 1.0/30)) { id = "ghost_crystal"; }
				else if (roll < (cum += 1.0/30)) { id = "hematite"; }
				else if (roll < (cum += 1.0/30)) { id = "kyanite"; }
				else if (roll < (cum += 1.0/30)) { id = "lunarite"; }
				else if (roll < (cum += 1.0/30)) { id = "marble"; }
				else if (roll < (cum += 1.0/30)) { id = "ocean_stone"; }
				else if (roll < (cum += 1.0/30)) { id = "opal"; }
				else if (roll < (cum += 1.0/30)) { id = "pyrite"; }
				else if (roll < (cum += 1.0/30)) { id = "soapstone"; }
				else if (roll < (cum += 1.0/8)) { id = "misc_stone"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "clay"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "coal"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "copper_ore"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "iron_ore"; count = qty.get(); }
				else { id = "gold_ore"; count = qty.get(); }
			}
			case "magma_geode" -> {
				if (roll < (cum += 1.0/26)) { id = "dwarf_gadget"; }
				else if (roll < (cum += 1.0/16)) { id = "fire_quartz"; }
				else if (roll < (cum += 1.0/26)) { id = "baryte"; }
				else if (roll < (cum += 1.0/26)) { id = "basalt"; }
				else if (roll < (cum += 1.0/26)) { id = "bixite"; }
				else if (roll < (cum += 1.0/26)) { id = "dolomite"; }
				else if (roll < (cum += 1.0/26)) { id = "fire_opal"; }
				else if (roll < (cum += 1.0/26)) { id = "helvite"; }
				else if (roll < (cum += 1.0/26)) { id = "jasper"; }
				else if (roll < (cum += 1.0/26)) { id = "lemon_stone"; }
				else if (roll < (cum += 1.0/26)) { id = "neptunite"; }
				else if (roll < (cum += 1.0/26)) { id = "obsidian"; }
				else if (roll < (cum += 1.0/26)) { id = "star_shards"; }
				else if (roll < (cum += 1.0/26)) { id = "tigerseye"; }
				else if (roll < (cum += 1.0/8)) { id = "misc_stone"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "clay"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "coal"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "copper_ore"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "iron_ore"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "gold_ore"; count = qty.get(); }
				else { id = "iridium_ore"; count = qty.get(); }
			}
			case "omni_geode" -> {
				if (roll < (cum += 31.0/2750)) { id = "dwarvish_helm"; }
				else if (roll < (cum += 31.0/2750)) { id = "dwarf_gadget"; }
				else if (roll < (cum += 31.0/2750)) { id = "ancient_drum"; }
				else if (roll < (cum += 1.0/48)) { id = "earth_crystal"; }
				else if (roll < (cum += 1.0/48)) { id = "tear_crystal"; }
				else if (roll < (cum += 1.0/48)) { id = "fire_quartz"; }
				else if (roll < (cum += 1.0/250)) { id = "prismatic_shard"; }
				else if (roll < (cum += 31.0/2750)) { id = "aerinite"; }
				else if (roll < (cum += 31.0/2750)) { id = "alamite"; }
				else if (roll < (cum += 31.0/2750)) { id = "bixite"; }
				else if (roll < (cum += 31.0/2750)) { id = "calcite"; }
				else if (roll < (cum += 31.0/2750)) { id = "celestine"; }
				else if (roll < (cum += 31.0/2750)) { id = "dolomite"; }
				else if (roll < (cum += 31.0/2750)) { id = "esperite"; }
				else if (roll < (cum += 31.0/2750)) { id = "fluorapatite"; }
				else if (roll < (cum += 31.0/2750)) { id = "geminite"; }
				else if (roll < (cum += 31.0/2750)) { id = "ghost_crystal"; }
				else if (roll < (cum += 31.0/2750)) { id = "granite"; }
				else if (roll < (cum += 31.0/2750)) { id = "jamborite"; }
				else if (roll < (cum += 31.0/2750)) { id = "hematite"; }
				else if (roll < (cum += 31.0/2750)) { id = "jagoite"; }
				else if (roll < (cum += 31.0/2750)) { id = "jasper"; }
				else if (roll < (cum += 31.0/2750)) { id = "kyanite"; }
				else if (roll < (cum += 31.0/2750)) { id = "lemon_stone"; }
				else if (roll < (cum += 31.0/2750)) { id = "limestone"; }
				else if (roll < (cum += 31.0/2750)) { id = "lunarite"; }
				else if (roll < (cum += 31.0/2750)) { id = "malachite"; }
				else if (roll < (cum += 31.0/2750)) { id = "marble"; }
				else if (roll < (cum += 31.0/2750)) { id = "mudstone"; }
				else if (roll < (cum += 31.0/2750)) { id = "nekoite"; }
				else if (roll < (cum += 31.0/2750)) { id = "neptunite"; }
				else if (roll < (cum += 31.0/2750)) { id = "ocean_stone"; }
				else if (roll < (cum += 31.0/2750)) { id = "opal"; }
				else if (roll < (cum += 31.0/2750)) { id = "orpiment"; }
				else if (roll < (cum += 31.0/2750)) { id = "petrified_slime"; }
				else if (roll < (cum += 31.0/2750)) { id = "pyrite"; }
				else if (roll < (cum += 31.0/2750)) { id = "sandstone"; }
				else if (roll < (cum += 31.0/2750)) { id = "slate"; }
				else if (roll < (cum += 31.0/2750)) { id = "soapstone"; }
				else if (roll < (cum += 31.0/2750)) { id = "star_shards"; }
				else if (roll < (cum += 31.0/2750)) { id = "thunder_egg"; }
				else if (roll < (cum += 31.0/2750)) { id = "tigerseye"; }
				else if (roll < (cum += 31.0/2750)) { id = "obsidian"; }
				else if (roll < (cum += 31.0/2750)) { id = "basalt"; }
				else if (roll < (cum += 31.0/2750)) { id = "baryte"; }
				else if (roll < (cum += 31.0/2750)) { id = "star_shards"; }
				else if (roll < (cum += 31.0/2750)) { id = "fire_opal"; }
				else if (roll < (cum += 31.0/2750)) { id = "helvite"; }
				else if (roll < (cum += 1.0/8)) { id = "misc_stone"; count = qty.get(); }
				else if (roll < (cum += 1.0/16)) { id = "clay"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "coal"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "copper_ore"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "iron_ore"; count = qty.get(); }
				else if (roll < (cum += 1.0/20)) { id = "gold_ore"; count = qty.get(); }
				else { id = "iridium_ore"; count = qty.get(); }
			}
			default -> { id = "misc_stone"; }
		}
		return new java.util.AbstractMap.SimpleEntry<>(id, count);
	}

	/** 古物宝藏产出：27种古物各 1/27 */
	private static String rollArtifactTrove(Random random) {
		String[] artifacts = {
			"anchor", "ancient_doll", "ancient_drum", "ancient_seed", "ancient_sword",
			"arrowhead", "bone_flute", "chewing_stick", "chicken_statue", "chipped_amphora",
			"dried_starfish", "dwarf_gadget", "dwarvish_helm", "elvish_jewelry", "glass_shards",
			"golden_mask", "golden_pumpkin", "golden_relic", "ornamental_fan",
			"prehistoric_handaxe", "prehistoric_tool", "rare_disc", "rusty_cog", "rusty_spoon",
			"rusty_spur", "treasure_chest", "treasure_appraisal_guide"
		};
		return artifacts[random.nextInt(artifacts.length)];
	}

	/** 金色椰子产出 */
	private static String rollGoldenCoconut(Random random) {
		String[] rewards = {"pineapple_seeds", "taroroot_seeds", "mahogany_seed", "iridium_ore"};
		return rewards[random.nextInt(rewards.length)];
	}

	/** 迷之盒产出 */
	private static java.util.Map.Entry<String, Integer> rollMysteryBox(Random random, ServerPlayerEntity player) {
		double roll = random.nextDouble();
		int season = (int)((player.getEntityWorld().getTimeOfDay() / 24000L) % 4);
		int fishingLevel = stardewvalley.modid.season.FishingLevelManager.get((net.minecraft.server.world.ServerWorld) player.getEntityWorld()).getLevel(player.getUuid());

		// 累积概率
		double cum = 0.0;

		// 魔法糖冰棍 0.2%
		if (roll < (cum += 0.002)) return entry("magic_rock_candy", 1);
		// 紫水晶戒指 0.6%
		if (roll < (cum += 0.006)) return entry("amethyst_ring", 1);
		// 苹果树苗 0.1%
		if (roll < (cum += 0.001)) return entry("apple_sapling", 1);
		// 杏子树苗 0.1%
		if (roll < (cum += 0.001)) return entry("apricot_sapling", 1);
		// 海蓝宝石戒指 0.3%
		if (roll < (cum += 0.003)) return entry("aquamarine_ring", 1);
		// 洋蓟种子
		if (season == 2) { // 秋季
			if (roll < (cum += 0.0151)) return entry("artichoke_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("artichoke_seeds", 20);
		} else if (season == 3) { // 冬季
			if (roll < (cum += 0.005)) return entry("artichoke_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("artichoke_seeds", 20);
		} else {
			cum += 0.0216; // 跳过不在季节的
		}
		// 秋日恩赐 0.13%
		if (roll < (cum += 0.0013)) return entry("autums_bounty", 1);
		// 鱼饵和浮漂 0.13%
		if (roll < (cum += 0.0013)) return entry("bait_and_bobber", 1);
		// 烤鱼 0.13%
		if (roll < (cum += 0.0013)) return entry("baked_fish", 1);
		// 豆类火锅 0.13%
		if (roll < (cum += 0.0013)) return entry("bean_hotpot", 1);
		// 青豆种子
		if (season == 1) { // 春
			if (roll < (cum += 0.0151)) return entry("beanstarter", 8);
			if (roll < (cum += 0.0016)) return entry("beanstarter", 20);
		} else if (season == 3) { // 冬
			if (roll < (cum += 0.005)) return entry("beanstarter", 8);
			if (roll < (cum += 0.0005)) return entry("beanstarter", 20);
		} else { cum += 0.0216; }
		// 蓝莓千层酥 0.13%
		if (roll < (cum += 0.0013)) return entry("blueberry_tart", 1);
		// 炸弹 6.03%
		if (roll < (cum += 0.0603)) return entry("bomb", 5);
		// 谜之书 0.49%
		if (roll < (cum += 0.0049)) return entry("book_of_mysteries", 1);
		// 星之书 0.49%
		if (roll < (cum += 0.0049)) return entry("book_of_stars", 1);
		// 面包 0.13%
		if (roll < (cum += 0.0013)) return entry("bread", 1);
		// 西兰花种子 (夏季21日-秋季20日)
		if (season == 1 || season == 2) {
			if (roll < (cum += 0.0063)) return entry("broccoli_seeds", 8);
		} else { cum += 0.0063; }
		// 惊喜鲤鱼 0.13%
		if (roll < (cum += 0.0013)) return entry("surprise_carp", 1);
		// 胡萝卜种子 (冬季21日-春季23日)
		if (season == 3 || season == 0) {
			if (roll < (cum += 0.0063)) return entry("carrot_seeds", 8);
		} else { cum += 0.0063; }
		// 花椰菜种子
		if (season == 0) { // 春
			if (roll < (cum += 0.0151)) return entry("cauliflower_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("cauliflower_seeds", 20);
		} else if (season == 3) { // 冬
			if (roll < (cum += 0.005)) return entry("cauliflower_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("cauliflower_seeds", 20);
		} else { cum += 0.0216; }
		// 乳酪花椰菜 0.13%
		if (roll < (cum += 0.0013)) return entry("cheese_cauliflower", 1);
		// 樱桃树苗 0.1%
		if (roll < (cum += 0.001)) return entry("cherry_sapling", 1);
		// 巧克力蛋糕 0.13%
		if (roll < (cum += 0.0013)) return entry("chocolate_cake", 1);
		// 海鲜杂烩汤 0.86%
		if (roll < (cum += 0.0086)) return entry("chowder", 1);
		// 咖啡 6.03%
		if (roll < (cum += 0.0603)) return entry("coffee", 3);
		// 战斗季刊 0.13%
		if (roll < (cum += 0.0013)) return entry("combat_quarterly", 1);
		// 完美早餐 0.13%
		if (roll < (cum += 0.0013)) return entry("complete_breakfast", 1);
		// 饼干 0.13%
		if (roll < (cum += 0.0013)) return entry("cookie", 1);
		// 软木塞浮标
		if (fishingLevel >= 6) {
			if (roll < (cum += 0.0016)) return entry("fishtool_cork_bobber", 1);
		} else { cum += 0.0016; }
		// 玉米种子
		if (season == 1 || season == 2) { // 夏/秋
			if (roll < (cum += 0.0151)) return entry("corn_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("corn_seeds", 20);
		} else if (season == 3) { // 冬
			if (roll < (cum += 0.01)) return entry("corn_seeds", 8);
			if (roll < (cum += 0.001)) return entry("corn_seeds", 20);
		} else { cum += 0.0277; }
		// 蟹黄糕 0.86%
		if (roll < (cum += 0.0086)) return entry("crab_cakes", 1);
		// 红莓酱 0.13%
		if (roll < (cum += 0.0013)) return entry("cranberry_sauce", 1);
		// 香酥鲈鱼 0.13%
		if (roll < (cum += 0.0013)) return entry("crispy_bass", 1);
		// 高级生长激素 6.03%
		if (roll < (cum += 0.0603)) return entry("deluxe_growth_hormone", 10);
		if (roll < (cum += 0.0063)) return entry("deluxe_growth_hormone", 20);
		// 海之菜肴
		if (fishingLevel < 6) {
			if (roll < (cum += 0.0063)) return entry("dish_of_the_sea", 2);
		} else {
			// 钓鱼≥6时，部分概率分配给精装旋式鱼饵和软木塞浮标
			cum += 0.0016; // 精装旋式鱼饵的占位
			if (roll < (cum += 0.0031)) return entry("dish_of_the_sea", 2);
		}
		// 精装旋式鱼饵
		if (fishingLevel >= 6) {
			if (roll < (cum += 0.0016)) return entry("fishtool_dressed_spinner", 1);
		} else { cum += 0.0016; }
		// 帕尔玛奶酪茄子 0.13%
		if (roll < (cum += 0.0013)) return entry("eggplant_parmesan", 1);
		// 茄子种子
		if (season == 2) { // 秋
			if (roll < (cum += 0.0151)) return entry("eggplant_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("eggplant_seeds", 20);
		} else if (season == 3) { // 冬
			if (roll < (cum += 0.005)) return entry("eggplant_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("eggplant_seeds", 20);
		} else { cum += 0.0216; }
		// 绿宝石戒指 0.16%
		if (roll < (cum += 0.0016)) return entry("emerald_ring", 1);
		// 法式田螺 0.86%
		if (roll < (cum += 0.0086)) return entry("escargot", 1);
		// 后续大量条目... 剩余概率合计约 44%
		// 剩余 60+ 项，简化处理为常见物品
		if (roll < (cum += 0.0086)) return entry("fish_stew", 1);
		if (roll < (cum += 0.0013)) return entry("fish_taco", 1);
		if (roll < (cum += 0.0013)) return entry("fried_calamari", 1);
		if (roll < (cum += 0.0013)) return entry("fried_eel", 1);
		if (roll < (cum += 0.0013)) return entry("fried_egg", 1);
		if (roll < (cum += 0.0013)) return entry("fried_mushroom", 1);
		if (roll < (cum += 0.0013)) return entry("glazed_yams", 1);
		if (roll < (cum += 0.006)) return entry("glowstone_ring", 1);
		if (roll < (cum += 0.0013)) return entry("golden_pumpkin", 1);
		if (roll < (cum += 0.0603)) return entry("hardwood", 10);
		if (roll < (cum += 0.0013)) return entry("hashbrowns", 1);
		if (roll < (cum += 0.0013)) return entry("ice_cream", 1);
		if (roll < (cum += 0.003)) return entry("jade_ring", 1);
		if (roll < (cum += 0.0063)) return entry("life_elixir", 2);
		if (roll < (cum += 0.0086)) return entry("lobster_bisque", 1);
		if (roll < (cum += 0.0013)) return entry("lucky_lunch", 1);
		if (roll < (cum += 0.0063)) return entry("lucky_lunch", 2);
		if (roll < (cum += 0.0013)) return entry("maki_roll", 1);
		if (roll < (cum += 0.0086)) return entry("maple_bar", 1);
		if (roll < (cum += 0.0063)) return entry("mega_bomb", 5);
		if (roll < (cum += 0.0013)) return entry("mining_monthly", 1);
		if (roll < (cum += 0.0603)) return entry("mixed_flower_seeds", 10);
		if (roll < (cum += 0.0603)) return entry("mixed_seeds", 10);
		if (roll < (cum += 0.0362)) return entry("mystery_box", 2);
		if (roll < (cum += 0.0063)) return entry("mystery_box", 3 + random.nextInt(2));
		if (roll < (cum += 0.0013)) return entry("omelet", 1);
		if (roll < (cum += 0.001)) return entry("orange_sapling", 1);
		if (roll < (cum += 0.0031)) return entry("ossified_blade", 1);
		if (roll < (cum += 0.0013)) return entry("pancakes", 1);
		// 防风草种子
		if (season == 0) {
			if (roll < (cum += 0.0151)) return entry("parsnip_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("parsnip_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.005)) return entry("parsnip_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("parsnip_seeds", 20);
		} else { cum += 0.0216; }
		if (roll < (cum += 0.0013)) return entry("parsnip_soup", 1);
		if (roll < (cum += 0.001)) return entry("peach_sapling", 1);
		if (roll < (cum += 0.0013)) return entry("pepper_poppers", 1);
		if (roll < (cum += 0.001)) return entry("pomegranate_sapling", 1);
		// 辣椒种子
		if (season == 1) {
			if (roll < (cum += 0.0151)) return entry("hotpepper_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("hotpepper_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.005)) return entry("hotpepper_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("hotpepper_seeds", 20);
		} else { cum += 0.0216; }
		if (roll < (cum += 0.0013)) return entry("pink_cake", 1);
		if (roll < (cum += 0.0013)) return entry("pizza", 1);
		// 土豆种子
		if (season == 0) {
			if (roll < (cum += 0.0151)) return entry("potato_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("potato_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.005)) return entry("potato_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("potato_seeds", 20);
		} else { cum += 0.0216; }
		// 霜瓜种子 (秋季21日-冬季20日)
		if (season == 2 || season == 3) {
			if (roll < (cum += 0.0063)) return entry("powdermelon_seeds", 8);
		} else { cum += 0.0063; }
		if (roll < (cum += 0.004)) return entry("prismatic_shard", 1);
		// 南瓜种子
		if (season == 2) {
			if (roll < (cum += 0.0151)) return entry("pumpkin_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("pumpkin_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.005)) return entry("pumpkin_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("pumpkin_seeds", 20);
		} else { cum += 0.0216; }
		if (roll < (cum += 0.0013)) return entry("pumpkin_soup", 1);
		if (roll < (cum += 0.0603)) return entry("quality_fertilizer", 10);
		if (roll < (cum += 0.0063)) return entry("quality_fertilizer", 20);
		if (roll < (cum += 0.0063)) return entry("quality_sprinkler", 1);
		// 萝卜种子
		if (season == 1) {
			if (roll < (cum += 0.0151)) return entry("radish_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("radish_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.005)) return entry("radish_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("radish_seeds", 20);
		} else { cum += 0.0216; }
		if (roll < (cum += 0.0013)) return entry("red_plate", 1);
		if (roll < (cum += 0.0013)) return entry("rhubarb_pie", 1);
		if (roll < (cum += 0.0013)) return entry("rice_pudding", 1);
		if (roll < (cum += 0.0016)) return entry("ruby_ring", 1);
		if (roll < (cum += 0.0013)) return entry("salad", 1);
		if (roll < (cum += 0.0013)) return entry("salmon_dinner", 1);
		if (roll < (cum += 0.0013)) return entry("sashimi", 1);
		if (roll < (cum += 0.0086)) return entry("shrimp_cocktail", 1);
		if (roll < (cum += 0.0013)) return entry("spaghetti", 1);
		if (roll < (cum += 0.0013)) return entry("spicy_eel", 1);
		if (roll < (cum += 0.0013)) return entry("stardew_valley_almanac", 1);
		if (roll < (cum += 0.0013)) return entry("strange_bun", 1);
		if (roll < (cum += 0.0013)) return entry("stuffing", 1);
		if (roll < (cum += 0.006)) return entry("sturdy_ring", 1);
		// 金皮西葫芦种子 (春季24日-夏季20日)
		if (season == 0 || season == 1) {
			if (roll < (cum += 0.0063)) return entry("zucchini_seeds", 8);
		} else { cum += 0.0063; }
		if (roll < (cum += 0.0013)) return entry("survival_burger", 1);
		if (roll < (cum += 0.0013)) return entry("tropical_curry", 1);
		if (roll < (cum += 0.0013)) return entry("tortilla", 1);
		if (roll < (cum += 0.008)) return entry("treasure_chest", 1);
		if (roll < (cum += 0.0063)) return entry("triple_shot_espresso", 3);
		if (roll < (cum += 0.0013)) return entry("trout_soup", 1);
		if (roll < (cum += 0.0013)) return entry("vegetable_medley", 1);
		if (roll < (cum += 0.0603)) return entry("warp_totem_beach", 1);
		if (roll < (cum += 0.0603)) return entry("warp_totem_farm", 1);
		if (roll < (cum += 0.0063)) return entry("warp_totem_farm", 3);
		if (roll < (cum += 0.0603)) return entry("warp_totem_mountains", 1);
		// 小麦种子
		if (season == 1) {
			if (roll < (cum += 0.0151)) return entry("wheat_seeds", 8);
			if (roll < (cum += 0.0016)) return entry("wheat_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.005)) return entry("wheat_seeds", 8);
			if (roll < (cum += 0.0005)) return entry("wheat_seeds", 20);
		} else { cum += 0.0216; }
		if (roll < (cum += 0.0013)) return entry("woodcutters_weekly", 1);

		// 保底
		return entry("misc_stone", 1);
	}

	/** 金色迷之盒产出 */
	private static java.util.Map.Entry<String, Integer> rollGoldenMysteryBox(Random random, ServerPlayerEntity player) {
		double roll = random.nextDouble();
		int season = (int)((player.getEntityWorld().getTimeOfDay() / 24000L) % 4);
		int fishingLevel = stardewvalley.modid.season.FishingLevelManager.get((net.minecraft.server.world.ServerWorld) player.getEntityWorld()).getLevel(player.getUuid());

		double cum = 0.0;

		// 金色动物饼干 0.5%（战利品表项）
		if (roll < (cum += 0.005)) return entry("golden_animal_cracker", 1);
		// 魔法糖冰棍 0.4%
		if (roll < (cum += 0.004)) return entry("magic_rock_candy", 1);
		// 五彩碎片 0.79%
		if (roll < (cum += 0.0079)) return entry("prismatic_shard", 1);
		// 财宝箱 1.57%
		if (roll < (cum += 0.0157)) return entry("treasure_chest", 1);
		// 星之书 0.96%
		if (roll < (cum += 0.0096)) return entry("book_of_stars", 1);
		// 谜之书 0.96%
		if (roll < (cum += 0.0096)) return entry("book_of_mysteries", 1);
		// 黄金南瓜 0.94%
		if (roll < (cum += 0.0094)) return entry("golden_pumpkin", 1);
		// 超级炸弹 5.8%
		if (roll < (cum += 0.058)) return entry("mega_bomb", 5);
		// 三倍浓缩咖啡 5.8%
		if (roll < (cum += 0.058)) return entry("triple_shot_espresso", 3);
		// 精装旋式鱼饵 (钓鱼等级≥6)
		if (fishingLevel >= 6) {
			if (roll < (cum += 0.0145)) return entry("fishtool_dressed_spinner", 1);
		} else { cum += 0.0145; }
		// 软木塞浮标 (钓鱼等级≥6)
		if (fishingLevel >= 6) {
			if (roll < (cum += 0.0145)) return entry("fishtool_cork_bobber", 1);
		} else { cum += 0.0145; }
		// 海之菜肴
		if (fishingLevel < 6) {
			if (roll < (cum += 0.058)) return entry("dish_of_the_sea", 2);
		} else {
			if (roll < (cum += 0.029)) return entry("dish_of_the_sea", 2);
		}
		// 幸运午餐 5.8%
		if (roll < (cum += 0.058)) return entry("lucky_lunch", 2);
		// 高级肥料 5.8%
		if (roll < (cum += 0.058)) return entry("quality_fertilizer", 20);
		// 高级生长激素 5.8%
		if (roll < (cum += 0.058)) return entry("deluxe_growth_hormone", 20);
		// 生命药水 5.8%
		if (roll < (cum += 0.058)) return entry("life_elixir", 2);
		// 传送图腾：农场 5.8%
		if (roll < (cum += 0.058)) return entry("warp_totem_farm", 3);
		// 果树苗各0.97%
		if (roll < (cum += 0.0097)) return entry("cherry_sapling", 1);
		if (roll < (cum += 0.0097)) return entry("apricot_sapling", 1);
		if (roll < (cum += 0.0097)) return entry("orange_sapling", 1);
		if (roll < (cum += 0.0097)) return entry("peach_sapling", 1);
		if (roll < (cum += 0.0097)) return entry("pomegranate_sapling", 1);
		if (roll < (cum += 0.0097)) return entry("apple_sapling", 1);
		// 种子 - 春季
		if (season == 0) {
			if (roll < (cum += 0.0145)) return entry("parsnip_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("parsnip_seeds", 20);
		} else { cum += 0.0145; }
		if (season == 0) {
			if (roll < (cum += 0.0145)) return entry("beanstarter", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("beanstarter", 20);
		} else { cum += 0.0145; }
		if (season == 0) {
			if (roll < (cum += 0.0145)) return entry("cauliflower_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("cauliflower_seeds", 20);
		} else { cum += 0.0145; }
		if (season == 0) {
			if (roll < (cum += 0.0145)) return entry("potato_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("potato_seeds", 20);
		} else { cum += 0.0145; }
		// 玉米种子
		if (season == 1 || season == 2) {
			if (roll < (cum += 0.0145)) return entry("corn_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0097)) return entry("corn_seeds", 20);
		} else { cum += 0.0145; }
		// 小麦种子
		if (season == 1) {
			if (roll < (cum += 0.0145)) return entry("wheat_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("wheat_seeds", 20);
		} else { cum += 0.0145; }
		// 辣椒种子
		if (season == 1) {
			if (roll < (cum += 0.0145)) return entry("hotpepper_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("hotpepper_seeds", 20);
		} else { cum += 0.0145; }
		// 萝卜种子
		if (season == 1) {
			if (roll < (cum += 0.0145)) return entry("radish_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("radish_seeds", 20);
		} else { cum += 0.0145; }
		// 茄子种子
		if (season == 2) {
			if (roll < (cum += 0.0145)) return entry("eggplant_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("eggplant_seeds", 20);
		} else { cum += 0.0145; }
		// 洋蓟种子
		if (season == 2) {
			if (roll < (cum += 0.0145)) return entry("artichoke_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("artichoke_seeds", 20);
		} else { cum += 0.0145; }
		// 南瓜种子
		if (season == 2) {
			if (roll < (cum += 0.0145)) return entry("pumpkin_seeds", 20);
		} else if (season == 3) {
			if (roll < (cum += 0.0048)) return entry("pumpkin_seeds", 20);
		} else { cum += 0.0145; }
		// 骨化剑 2.9%
		if (roll < (cum += 0.029)) return entry("ossified_blade", 1);
		// 绿宝石戒指 1.45%
		if (roll < (cum += 0.0145)) return entry("emerald_ring", 1);
		// 红宝石戒指 1.45%
		if (roll < (cum += 0.0145)) return entry("ruby_ring", 1);
		// 优质洒水器 5.8%
		if (roll < (cum += 0.058)) return entry("quality_sprinkler", 1);
		// 迷之盒 5.8%
		if (roll < (cum += 0.058)) return entry("mystery_box", 3 + random.nextInt(2));
		// 书
		if (roll < (cum += 0.0116)) return entry("stardew_valley_almanac", 1);
		if (roll < (cum += 0.0116)) return entry("bait_and_bobber", 1);
		if (roll < (cum += 0.0116)) return entry("woodcutters_weekly", 1);
		if (roll < (cum += 0.0116)) return entry("mining_monthly", 1);
		if (roll < (cum += 0.0116)) return entry("combat_quarterly", 1);
		// 特殊季节种子
		if (season == 3) { // 冬季21日-春季23日
			if (roll < (cum += 0.058)) return entry("carrot_seeds", 8);
		} else { cum += 0.058; }
		if (season == 0) { // 春季24日-夏季20日
			if (roll < (cum += 0.058)) return entry("zucchini_seeds", 8);
		} else if (season == 1) {
			if (roll < (cum += 0.058)) return entry("zucchini_seeds", 8);
		} else { cum += 0.058; }
		if (season == 1) { // 夏季21日-秋季20日
			if (roll < (cum += 0.058)) return entry("broccoli_seeds", 8);
		} else if (season == 2) {
			if (roll < (cum += 0.058)) return entry("broccoli_seeds", 8);
		} else { cum += 0.058; }
		if (season == 2) { // 秋季21日-冬季20日
			if (roll < (cum += 0.058)) return entry("powdermelon_seeds", 8);
		} else if (season == 3) {
			if (roll < (cum += 0.058)) return entry("powdermelon_seeds", 8);
		} else { cum += 0.058; }

		return entry("misc_stone", 1);
	}

	private static java.util.Map.Entry<String, Integer> entry(String id, int count) {
		return new java.util.AbstractMap.SimpleEntry<>(id, count);
	}
}