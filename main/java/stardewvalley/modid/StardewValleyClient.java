package stardewvalley.modid;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.util.Identifier;

import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.equipment.ClientBackpackData;
import stardewvalley.modid.equipment.ClientEquipmentData;
import stardewvalley.modid.equipment.ClientTrashCanData;
import stardewvalley.modid.gui.BackpackHandledScreen;
import stardewvalley.modid.gui.BookStoreScreen;
import stardewvalley.modid.gui.BundleDetailScreen;
import stardewvalley.modid.gui.BundleRoomScreen;
import stardewvalley.modid.gui.ClockHudRenderer;
import stardewvalley.modid.gui.CrabPotBaitHandledScreen;
import stardewvalley.modid.gui.GreenRainOverlay;
import stardewvalley.modid.gui.GuideBookScreen;
import stardewvalley.modid.gui.KrobusShopScreen;
import stardewvalley.modid.gui.MainGuiScreen;
import stardewvalley.modid.gui.ModKeybindings;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.gui.ModScreenHandlers;
import stardewvalley.modid.gui.OreShopScreen;
import stardewvalley.modid.gui.RodHandledScreen;
import stardewvalley.modid.gui.SandyShopScreen;
import stardewvalley.modid.gui.ShippingBoxScreen;
import stardewvalley.modid.gui.SlingshotHandledScreen;
import stardewvalley.modid.gui.TravelingCartScreen;

public class StardewValleyClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {

        BlockRenderLayerMap.putBlock(ModBlocks.AMARANTH_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.ANCIENT_FRUIT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.ARTICHOKE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BEET_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BLUEBERRY_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BLUEJAZZ_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BOKCHOY_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BROCCOLI_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CACTUSFRUIT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.PARSNIP_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.PINEAPPLE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POPPY_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POTATO_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.POWDERMELON_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.PUMPKIN_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.QIGUA_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.RADISH_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.REDCABBAGE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.RHUBARB_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.STARFRUIT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.STRAWBERRY_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SUMMERSPANGLE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SUMMERSQUASH_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SUNFLOWER_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.SWEETGEMBERRY_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TAROROOT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TEALEAVES_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TOMATO_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.TULIP_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.UNMILLEDRICE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.WHEAT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.YAM_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CARROT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CAULIFLOWER_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.COFFEEBEAN_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CORN_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CRANBERRIES_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.EGGPLANT_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.FAIRYROSE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.FIBER_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.GARLIC_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.GRAPE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.GREENBEAN_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HOPS_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HOTPEPPER_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.KALE_CROP, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.MELON_CROP, BlockRenderLayer.CUTOUT);

        // 注册工匠设备物品展示渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.ARTISAN_EQUIPMENT,
            stardewvalley.modid.block.ArtisanEquipmentBlockEntityRenderer::new
        );
        // 注册树液采集器物品展示渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.TAPPER,
            stardewvalley.modid.block.TapperBlockEntityRenderer::new
        );
        // 注册避雷针物品展示渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.LIGHTNING_ROD,
            stardewvalley.modid.block.LightningRodBlockEntityRenderer::new
        );
        // 注册图腾展示渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.TOTEM_POLE,
            stardewvalley.modid.block.TotemPoleBlockEntityRenderer::new
        );
        // 注册蟹笼物品展示渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.CRAB_POT,
            stardewvalley.modid.block.CrabPotBlockEntityRenderer::new
        );
        // 注册高级虫饵盒渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.DELUXE_WORM_BIN,
            stardewvalley.modid.block.DeluxeWormBinBlockEntityRenderer::new
        );
        // 注册蘑菇树桩渲染器
        BlockEntityRendererFactories.register(
            stardewvalley.modid.block.ModBlockEntities.MUSHROOM_STUMP,
            stardewvalley.modid.block.MushroomStumpBlockEntityRenderer::new
        );
        // 方块渲染层
        BlockRenderLayerMap.putBlock(ModBlocks.TAPPER, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.HEAVY_TAPPER, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.STARDEW_GRASS, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CHARCOAL_KILN, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.BAIT_MAKER, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CRAB_POT, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.CRYSTALARIUM, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.DELUXE_WORM_BIN, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.GARDEN_POT, BlockRenderLayer.CUTOUT);
        BlockRenderLayerMap.putBlock(ModBlocks.MUSHROOM_LOG, BlockRenderLayer.CUTOUT);

        // 注册草地方块颜色提供器（使星露谷草显示正确的草地颜色）
        ColorProviderRegistry.BLOCK.register(
            (state, world, pos, tintIndex) -> {
                if (world != null && pos != null) {
                    return BiomeColors.getGrassColor(world, pos);
                }
                return 0x7C9C5C;
            },
            ModBlocks.STARDEW_GRASS
        );

        net.minecraft.client.gui.screen.ingame.HandledScreens.register(ModScreenHandlers.BACKPACK, BackpackHandledScreen::new);
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(ModScreenHandlers.SLINGSHOT, SlingshotHandledScreen::new);
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(ModScreenHandlers.ROD, RodHandledScreen::new);
        net.minecraft.client.gui.screen.ingame.HandledScreens.register(ModScreenHandlers.CRAB_POT, CrabPotBaitHandledScreen::new);

        ModKeybindings.register();
        ClockHudRenderer.register();
        GreenRainOverlay.register();

        // 始终注册钓鱼小游戏客户端（由命令切换模式）
        new stardewvalley.modid.fishing.FishingClient().onInitializeClient();

        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                ClientPlayNetworking.send(new ModPayloads.GoldRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.FarmingRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.ForagingRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.FishingRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.MiningRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.CombatRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.LuckRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.WeatherRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.SkillRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.MasteryRequestC2SPayload());
                ClientPlayNetworking.send(new ModPayloads.MuseumDataRequestC2SPayload());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.GoldSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientGold(payload.gold());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.AnimalCatalogueDiscountS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setAnimalCatalogueDiscount(payload.hasDiscount());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.PriceCatalogueSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setPriceCatalogueActive(payload.active());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.TreasureAppraisalSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setArtifactMultiplier(payload.active());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ForagingSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientForaging(payload.level(), payload.xp(), payload.nextLevelXp());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.FarmingSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientFarming(payload.level(), payload.xp(), payload.nextLevelXp());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.FishingSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientFishing(payload.level(), payload.xp(), payload.nextLevelXp());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.MiningSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientMining(payload.level(), payload.xp(), payload.nextLevelXp());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.CombatSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientCombat(payload.level(), payload.xp(), payload.nextLevelXp());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ShippingDataSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof ShippingBoxScreen screen) {
                    screen.setShippingData(payload.entries());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.ToolUpgradeStatusSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof OreShopScreen screen) {
                    screen.setToolUpgradeStatus(payload.upgrading(), payload.toolType(), payload.targetTier(), payload.completeTime());
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.LuckSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientLuck(payload.luck());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.WeatherSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                stardewvalley.modid.weather.WeatherType weather = stardewvalley.modid.weather.WeatherType.values()[payload.weatherOrdinal()];
                ClockHudRenderer.setClientWeather(weather);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.SkillSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientSkills(payload);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.MasterySyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClockHudRenderer.setClientMastery(payload);
            });
        });

        // Traveling Cart stock sync
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.TravelingCartStockSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                TravelingCartScreen.CURRENT_STOCK.clear();
                for (ModPayloads.TravelingCartItem item : payload.items()) {
                    TravelingCartScreen.CURRENT_STOCK.add(new TravelingCartScreen.TravelingCartEntry(item.itemId, item.price, item.maxBuy, item.available, item.isSpecial));
                }
            });
        });

        // 桑迪商店库存同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.SandyShopStockSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (context.client().currentScreen instanceof SandyShopScreen screen) {
                    screen.onStockSync(payload.items());
                }
            });
        });

        // 书店库存同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.BookStoreStockSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                BookStoreScreen.CURRENT_STOCK.clear();
                BookStoreScreen.CURRENT_STOCK.addAll(payload.items().stream().map(item ->
                    new BookStoreScreen.BookStoreEntry(item.itemId, item.price)
                ).toList());
                for (ModPayloads.BookStoreItem item : payload.items()) {
                    if (item.isBarter) {
                        BookStoreScreen.CURRENT_STOCK.add(new BookStoreScreen.BookStoreEntry(
                            item.itemId, item.requiredItemId, item.requiredCount, item.resultCount
                        ));
                    }
                }
                if (context.client().currentScreen instanceof BookStoreScreen screen) {
                    screen.refreshDisplay();
                }
            });
        });

        // 书店营业状态同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.BookStoreOpenSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                MainGuiScreen.bookStoreOpenToday = payload.open();
            });
        });

        // 科罗布斯商店库存同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.KrobusDataSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                KrobusShopScreen.CURRENT_STOCK.clear();
                KrobusShopScreen.CURRENT_STOCK.addAll(payload.items());
                if (context.client().currentScreen instanceof KrobusShopScreen screen) {
                    screen.refreshStock();
                }
            });
        });

        // 接收图腾浮动物品动画
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.FloatingItemS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(
                    net.minecraft.util.Identifier.of(payload.itemId()));
                if (item != net.minecraft.item.Items.AIR) {
                    context.client().gameRenderer.showFloatingItem(new net.minecraft.item.ItemStack(item));
                }
            });
        });

        // 接收收集包同步数据
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.BundleDataSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                java.util.Map<String, Boolean> completedMap = new java.util.HashMap<>();
                for (String s : payload.completedBundles()) {
                    completedMap.put(s, true);
                }

                if (context.client().currentScreen instanceof BundleRoomScreen screen) {
                    screen.setBundleData(completedMap, payload.vaultPurchased(), payload.rewardClaimedBundleIds());
                } else if (context.client().currentScreen instanceof BundleDetailScreen screen) {
                    String currentId = screen.getCurrentBundleId();
                    for (ModPayloads.BundleSlotSyncEntry entry : payload.entries()) {
                        if (entry.bundleId().equals(currentId)) {
                            screen.updateSyncData(entry.submittedItems(), completedMap,
                                payload.vaultPurchased(), payload.rewardClaimedBundleIds());
                            break;
                        }
                        // 确保所有完成的bundle都被标记
                        if (entry.completed()) {
                            completedMap.put(entry.bundleId(), true);
                        }
                    }
                }

                // 缓存金库完成状态（主界面用于沙漠/桑迪商店的开放判断）
                boolean allVaultDone = payload.vaultPurchased().containsAll(java.util.Set.of("vault_2500", "vault_5000", "vault_10000", "vault_25000"));
                stardewvalley.modid.gui.MainGuiScreen.vaultCompleted = allVaultDone;
            });
        });

        // 接收装备栏数据同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.EquipmentSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                net.minecraft.nbt.NbtCompound data = payload.slotData();
                if (data == null) return;
                data.getList("slots").ifPresent(list -> {
                    net.minecraft.item.ItemStack[] slots = new net.minecraft.item.ItemStack[3];
                    for (int i = 0; i < Math.min(list.size(), 3); i++) {
                        int idx = i;
                        list.getCompound(i).ifPresent(slotData -> {
                            slotData.getString("id").ifPresent(idStr -> {
                                net.minecraft.util.Identifier itemId = net.minecraft.util.Identifier.of(idStr);
                                net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(itemId);
                                int count = slotData.getInt("count").orElse(1);
                                slots[idx] = new net.minecraft.item.ItemStack(item, count);
                            });
                        });
                        if (slots[i] == null) {
                            slots[i] = net.minecraft.item.ItemStack.EMPTY;
                        }
                    }
                    ClientEquipmentData.updateEquipment(slots);
                });
            });
        });

        // 接收垃圾桶数据同步（服务端操作后回复）
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.TrashCanDataSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                String itemId = payload.itemId();
                int count = payload.count();
                if (itemId.isEmpty() || count <= 0) {
                    ClientTrashCanData.clear();
                } else {
                    Identifier id = Identifier.of(itemId);
                    net.minecraft.item.Item item = net.minecraft.registry.Registries.ITEM.get(id);
                    if (item != null && item != net.minecraft.item.Items.AIR) {
                        ClientTrashCanData.setPendingItem(new net.minecraft.item.ItemStack(item, count));
                    }
                }
                // 操作后请求刷新库存界面
            });
        });

        // 接收垃圾桶等级同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.TrashCanLevelSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientTrashCanData.setTrashLevel(payload.level());
            });
        });

        // 接收马龙商店兑换状态同步
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.MarlonTradeStateSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                stardewvalley.modid.gui.MarlonShopScreen.hasBartered = payload.hasTraded();
            });
        });

        // 接收背包等级同步（用于 ShopScreen）
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.BackpackDataSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                net.minecraft.nbt.NbtCompound data = payload.data();
                if (data == null) return;
                int level = data.getInt("level").orElse(0);
                ClientBackpackData.setBackpackLevel(level);
            });
        });

        // 博物馆捐赠数据同步（同时缓存能力数据供主界面判断）
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.MuseumDataSyncS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                stardewvalley.modid.gui.MuseumDonationScreen.clientAbilities = new java.util.HashSet<>(payload.abilities());
                if (context.client().currentScreen instanceof stardewvalley.modid.gui.MuseumDonationScreen screen) {
                    screen.onDataSync(payload.donatedItems(), payload.claimedRewardIndices(), payload.abilities());
                }
            });
        });

        // 注册弹丸实体渲染器
        EntityRendererRegistry.register(
            stardewvalley.modid.entity.ModEntities.SLINGSHOT_PROJECTILE,
            ctx -> new net.minecraft.client.render.entity.FlyingItemEntityRenderer<>(ctx, 0.75F, true)
        );

        // 客户端加入时请求收集包数据
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            client.execute(() -> {
                if (client.player != null) {
                    // 延迟一tick发送，确保连接就绪
                    new java.util.Timer().schedule(new java.util.TimerTask() {
                        @Override
                        public void run() {
                            client.execute(() -> {
                                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                                    new ModPayloads.BundleDataRequestC2SPayload());
                            });
                        }
                    }, 1000);
                }
            });
        });

        // 作者的话：服务端通知打开自定义指南界面
        ClientPlayNetworking.registerGlobalReceiver(ModPayloads.GuideBookOpenS2CPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new GuideBookScreen());
            });
        });
    }
}
