package stardewvalley.modid.fishing;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.gui.BookDataManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FishingTreasureGenerator {

    private record TreasureEntry(
        String itemId,
        int minCount,
        int maxCount,
        int fishingLevelReq,
        double baseChance
    ) {}

    // ===== 普通钓鱼宝箱 =====
    // -1 = 无等级要求, 2+ = 需要钓鱼等级>=2, 依次类推
    private static final List<TreasureEntry> ENTRIES = List.of(
        new TreasureEntry("unmilledrice_seeds", 2, 10, -1, 0.14),
        new TreasureEntry("bait_wild_bait", 2, 10, -1, 0.085),
        new TreasureEntry("qigua", 1, 4, -1, 0.43),
        new TreasureEntry("mystery_box", 1, 1, -1, 0.115),
        new TreasureEntry("golden_mystery_box", 1, 1, -1, 0.115),
        new TreasureEntry("golden_animal_cracker", 1, 1, -1, 0.07),
        new TreasureEntry("coal", 1, 24, -1, 0.295),
        new TreasureEntry("mixedseeds", 1, 3, -1, 0.12),  // 特殊：需要钓鱼等级<2
        new TreasureEntry("dinosaur_egg", 1, 1, 2, 0.0075),
        new TreasureEntry("ancient_seed", 1, 1, 2, 0.0075),
        new TreasureEntry("fire_quartz", 1, 4, 2, 0.037),
        new TreasureEntry("ruby", 1, 4, 2, 0.043),
        new TreasureEntry("emerald", 1, 4, 2, 0.043),
        new TreasureEntry("tear_crystal", 1, 4, 2, 0.037),
        new TreasureEntry("jade", 1, 4, 2, 0.043),
        new TreasureEntry("aquamarine", 1, 4, 2, 0.043),
        new TreasureEntry("earth_crystal", 1, 4, 2, 0.037),
        new TreasureEntry("amethyst", 1, 4, 2, 0.043),
        new TreasureEntry("topaz", 1, 4, 2, 0.043),
        new TreasureEntry("diamond", 1, 2, 2, 0.035),
        new TreasureEntry("neptunes_glaive", 1, 1, 2, 0.006),
        new TreasureEntry("broken_trident", 1, 1, 2, 0.006),
        new TreasureEntry("small_glow_ring", 1, 1, 2, 0.0029),
        new TreasureEntry("glow_ring", 1, 1, 2, 0.0006),
        new TreasureEntry("small_magnet_ring", 1, 1, 2, 0.0029),
        new TreasureEntry("magnet_ring", 1, 1, 2, 0.0006),
        new TreasureEntry("amethyst_ring", 1, 1, 2, 0.0005),
        new TreasureEntry("topaz_ring", 1, 1, 2, 0.0005),
        new TreasureEntry("aquamarine_ring", 1, 1, 2, 0.0005),
        new TreasureEntry("jade_ring", 1, 1, 2, 0.0005),
        new TreasureEntry("emerald_ring", 1, 1, 2, 0.0005),
        new TreasureEntry("ruby_ring", 1, 1, 2, 0.0005),
        new TreasureEntry("iridium_band", 1, 1, 2, 0.0012),
        new TreasureEntry("gold_egg", 1, 1, 2, 0.0012),
        new TreasureEntry("treasure_chest", 1, 1, 2, 0.0024),
        new TreasureEntry("strange_doll_yellow", 1, 1, 2, 0.0012),
        new TreasureEntry("strange_doll_green", 1, 1, 2, 0.0012),
        // 骨骼/化石（2.8-3.1%）
        new TreasureEntry("skeletal_tail", 1, 1, 2, 0.0295),
        new TreasureEntry("nautilus_fossil", 1, 1, 2, 0.0295),
        new TreasureEntry("amphibian_fossil", 1, 1, 2, 0.0295),
        // 文物（0.7-0.8%）
        new TreasureEntry("ancient_doll", 1, 1, 2, 0.0075),
        new TreasureEntry("elvish_jewelry", 1, 1, 2, 0.0075),
        new TreasureEntry("chewing_stick", 1, 1, 2, 0.0075),
        new TreasureEntry("ornamental_fan", 1, 1, 2, 0.0075),
        new TreasureEntry("rare_disc", 1, 1, 2, 0.0075),
        new TreasureEntry("ancient_sword", 1, 1, 2, 0.0075),
        new TreasureEntry("rusty_spoon", 1, 1, 2, 0.0075),
        new TreasureEntry("rusty_spur", 1, 1, 2, 0.0075),
        new TreasureEntry("rusty_cog", 1, 1, 2, 0.0075),
        new TreasureEntry("chicken_statue", 1, 1, 2, 0.0075),
        new TreasureEntry("prehistoric_tool", 1, 1, 2, 0.0075),
        new TreasureEntry("dried_starfish", 1, 1, 2, 0.0075),
        new TreasureEntry("anchor", 1, 1, 2, 0.0075),
        new TreasureEntry("glass_shards", 1, 1, 2, 0.0075),
        new TreasureEntry("bone_flute", 1, 1, 2, 0.0075),
        // 靴子等级2+ 各0.012%（单独池处理）
        new TreasureEntry("iron_ore", 1, 24, 3, 0.075),
        new TreasureEntry("frozen_geode", 1, 6, 3, 0.08),
        new TreasureEntry("fish_sea_jelly", 1, 1, 5, 0.02),
        new TreasureEntry("iridium_ore", 1, 2, 5, 0.011),
        new TreasureEntry("gold_ore", 1, 24, 5, 0.09),
        new TreasureEntry("copper_ore", 1, 24, 5, 0.085),
        new TreasureEntry("wood", 1, 24, 5, 0.055),
        new TreasureEntry("misc_stone", 1, 24, 5, 0.055),
        new TreasureEntry("geode", 1, 6, 5, 0.10),
        new TreasureEntry("magma_geode", 1, 6, 5, 0.05),
        new TreasureEntry("fishtool_dressed_spinner", 1, 1, 6, 0.04),
        new TreasureEntry("fishtool_sonar_bobber", 1, 1, 6, 0.035),
        new TreasureEntry("bait_deluxe_bait", 5, 5, 6, 0.255),
        new TreasureEntry("bait_bait", 1, 15, 6, 0.23),
        new TreasureEntry("prismatic_shard", 1, 1, 6, 0.00012),
        // 海之宝石 - 在宝箱战利品中额外添加（概率3%，由调用方控制）
        new TreasureEntry("jewels_of_the_sea", 1, 1, -1, 0.03)
    );

    // ===== 金色钓鱼宝箱 =====
    private static final List<TreasureEntry> GOLDEN_ENTRIES = List.of(
        new TreasureEntry("unmilledrice_seeds", 2, 10, -1, 0.18),
        new TreasureEntry("bait_wild_bait", 2, 10, -1, 0.055),
        new TreasureEntry("qigua", 1, 4, -1, 0.51),
        new TreasureEntry("mystery_box", 1, 1, -1, 0.145),
        new TreasureEntry("golden_mystery_box", 1, 1, -1, 0.145),
        new TreasureEntry("golden_animal_cracker", 1, 1, -1, 0.09),
        new TreasureEntry("iridium_bar", 1, 5, -1, 0.07),
        new TreasureEntry("carrot_seeds", 2, 8, -1, 0.07),
        new TreasureEntry("summersquash_seeds", 2, 8, -1, 0.07),
        new TreasureEntry("broccoli_seeds", 2, 8, -1, 0.07),
        new TreasureEntry("powdermelon_seeds", 2, 8, -1, 0.07),
        new TreasureEntry("fish_taco", 1, 1, -1, 0.07),
        new TreasureEntry("fairy_dust", 3, 5, -1, 0.07),
        new TreasureEntry("fishtool_dressed_spinner", 1, 1, -1, 0.08),
        new TreasureEntry("bait_challenge_bait", 3, 5, -1, 0.07),
        new TreasureEntry("bait_magnet", 3, 5, -1, 0.07),
        new TreasureEntry("shrimp_cocktail", 1, 1, -1, 0.07),
        new TreasureEntry("fish_stew", 1, 1, -1, 0.07),
        new TreasureEntry("fishtool_sonar_bobber", 1, 1, -1, 0.095),
        new TreasureEntry("iridium_ore", 1, 2, -1, 0.007),
        new TreasureEntry("gold_ore", 1, 24, -1, 0.06),
        new TreasureEntry("iron_ore", 1, 24, -1, 0.045),
        new TreasureEntry("copper_ore", 1, 24, -1, 0.055),
        new TreasureEntry("wood", 1, 24, -1, 0.035),
        new TreasureEntry("misc_stone", 1, 24, -1, 0.035),
        new TreasureEntry("coal", 1, 24, -1, 0.165),
        new TreasureEntry("bait_deluxe_bait", 5, 5, -1, 0.17),
        new TreasureEntry("bait_bait", 1, 15, -1, 0.075),
        new TreasureEntry("mixedseeds", 1, 3, -1, 0.0),  // 特殊：0%不掉落
        new TreasureEntry("skeletal_tail", 1, 1, 2, 0.018),
        new TreasureEntry("nautilus_fossil", 1, 1, 2, 0.018),
        new TreasureEntry("amphibian_fossil", 1, 1, 2, 0.018),
        // 文物（0.5%）
        new TreasureEntry("ancient_doll", 1, 1, 2, 0.005),
        new TreasureEntry("elvish_jewelry", 1, 1, 2, 0.005),
        new TreasureEntry("chewing_stick", 1, 1, 2, 0.005),
        new TreasureEntry("ornamental_fan", 1, 1, 2, 0.005),
        new TreasureEntry("dinosaur_egg", 1, 1, 2, 0.005),
        new TreasureEntry("rare_disc", 1, 1, 2, 0.005),
        new TreasureEntry("ancient_sword", 1, 1, 2, 0.005),
        new TreasureEntry("rusty_spoon", 1, 1, 2, 0.005),
        new TreasureEntry("rusty_spur", 1, 1, 2, 0.005),
        new TreasureEntry("rusty_cog", 1, 1, 2, 0.005),
        new TreasureEntry("chicken_statue", 1, 1, 2, 0.005),
        new TreasureEntry("ancient_seed", 1, 1, 2, 0.005),
        new TreasureEntry("prehistoric_tool", 1, 1, 2, 0.005),
        new TreasureEntry("dried_starfish", 1, 1, 2, 0.005),
        new TreasureEntry("anchor", 1, 1, 2, 0.005),
        new TreasureEntry("glass_shards", 1, 1, 2, 0.005),
        new TreasureEntry("bone_flute", 1, 1, 2, 0.005),
        new TreasureEntry("fire_quartz", 1, 4, 2, 0.023),
        new TreasureEntry("ruby", 1, 4, 2, 0.027),
        new TreasureEntry("emerald", 1, 4, 2, 0.027),
        new TreasureEntry("tear_crystal", 1, 4, 2, 0.023),
        new TreasureEntry("jade", 1, 4, 2, 0.027),
        new TreasureEntry("aquamarine", 1, 4, 2, 0.027),
        new TreasureEntry("earth_crystal", 1, 4, 2, 0.023),
        new TreasureEntry("amethyst", 1, 4, 2, 0.027),
        new TreasureEntry("topaz", 1, 4, 2, 0.027),
        new TreasureEntry("diamond", 1, 2, 2, 0.025),
        new TreasureEntry("neptunes_glaive", 1, 1, 2, 0.004),
        new TreasureEntry("broken_trident", 1, 1, 2, 0.004),
        new TreasureEntry("small_glow_ring", 1, 1, 2, 0.0018),
        new TreasureEntry("glow_ring", 1, 1, 2, 0.0004),
        new TreasureEntry("small_magnet_ring", 1, 1, 2, 0.0018),
        new TreasureEntry("magnet_ring", 1, 1, 2, 0.0004),
        new TreasureEntry("amethyst_ring", 1, 1, 2, 0.0003),
        new TreasureEntry("topaz_ring", 1, 1, 2, 0.0003),
        new TreasureEntry("aquamarine_ring", 1, 1, 2, 0.0003),
        new TreasureEntry("jade_ring", 1, 1, 2, 0.0003),
        new TreasureEntry("emerald_ring", 1, 1, 2, 0.0003),
        new TreasureEntry("ruby_ring", 1, 1, 2, 0.0003),
        new TreasureEntry("treasure_chest", 1, 1, 2, 0.0016),
        new TreasureEntry("prismatic_shard", 1, 1, 2, 0.00008),
        new TreasureEntry("strange_doll_yellow", 1, 1, 2, 0.0008),
        new TreasureEntry("strange_doll_green", 1, 1, 2, 0.0008),
        new TreasureEntry("iridium_band", 1, 1, 2, 0.0008),
        new TreasureEntry("gold_egg", 1, 1, 2, 0.0008),
        new TreasureEntry("geode", 1, 6, 5, 0.065),
        new TreasureEntry("magma_geode", 1, 6, 5, 0.03),
        new TreasureEntry("frozen_geode", 1, 6, 5, 0.05),
        // 海之宝石 - 2%
        new TreasureEntry("jewels_of_the_sea", 1, 1, -1, 0.02)
    );

    // 靴子 - 等级2+，各0.012%（普通宝箱） / 0.008%（金宝箱）
    private static final String[] BOOTS = {
        "sneakers", "rubber_boots", "leather_boots", "work_boots", "combat_boots",
        "tundra_boots", "thermal_boots", "dark_boots", "firewalker_boots", "genie_shoes",
        "space_boots", "cowboy_boots", "emilys_magic_boots", "leprechaun_shoes", "cinderclown_shoes",
        "mermaid_boots", "dragonscale_boots", "crystal_shoes"
    };

    // 技能书 - 各 0.12%（普通宝箱） / 各 1.5%（金宝箱）
    private static final String[] SKILL_BOOKS = {
        "mining_monthly", "combat_quarterly", "stardew_valley_almanac", "woodcutters_weekly",
        "bait_and_bobber"
    };

    // 鱼饵和浮漂列表（普通宝箱整体0.12%，金宝箱整体1.5%）
    private static final String[] BAIT_TACKLE = {
        "bait_magic_bait",
        "fishtool_barbed_hook", "fishtool_cork_bobber", "fishtool_curiosity_lure",
        "fishtool_dressed_spinner", "fishtool_lead_bobber", "fishtool_quality_bobber",
        "fishtool_sonar_bobber", "fishtool_spinner", "fishtool_trap_bobber", "fishtool_treasure_hunter"
    };

    /**
     * 生成普通钓鱼宝箱战利品（带玩家UUID，应用书籍效果与最终运气）
     */
    public static List<ItemStack> generateTreasure(ServerWorld world, Random random, int fishingLevel, ItemStack caughtFish, UUID playerUuid, float luckMultiplier) {
        boolean hasJewelsBook = playerUuid != null && BookDataManager.get(world).hasUsedBook(playerUuid, "jewels_of_the_sea");
        float roeChance = hasJewelsBook ? 0.4f : 0.25f;
        List<ItemStack> result = generateTreasureInternal(world, random, fishingLevel, caughtFish, roeChance, playerUuid, ENTRIES, 0.0012f, 0.0012f, luckMultiplier, 6);

        if (hasJewelsBook) {
            // 海之宝石：鱼籽数量翻倍
            for (ItemStack stack : result) {
                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (id.getPath().endsWith("_roe")) {
                    stack.setCount(stack.getCount() * 2);
                }
            }

            // 海之宝石：(3+x)%概率再获得一本海之宝石
            boolean alreadyHas = BookDataManager.get(world).hasUsedBook(playerUuid, "jewels_of_the_sea");
            int bookChance = alreadyHas ? 3 : (3 + BookDataManager.get(world).getCounter(playerUuid));
            if (random.nextInt(100) < bookChance) {
                Item book = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "jewels_of_the_sea"));
                if (book != null) {
                    result.add(new ItemStack(book, 1));
                }
            }
            BookDataManager.get(world).getAndIncrementCounter(playerUuid);
        }

        return result;
    }

    /**
     * 生成金色钓鱼宝箱战利品（钓鱼精通后宝箱升级，带玩家UUID）
     */
    public static List<ItemStack> generateGoldenTreasure(ServerWorld world, Random random, int fishingLevel, ItemStack caughtFish, UUID playerUuid, float luckMultiplier) {
        boolean hasJewelsBook = playerUuid != null && BookDataManager.get(world).hasUsedBook(playerUuid, "jewels_of_the_sea");
        float roeChance = hasJewelsBook ? 0.4f : 0.25f;
        List<ItemStack> result = generateTreasureInternal(world, random, fishingLevel, caughtFish, roeChance, playerUuid, GOLDEN_ENTRIES, 0.015f, 0.008f, luckMultiplier, 6);

        if (hasJewelsBook) {
            for (ItemStack stack : result) {
                Identifier id = Registries.ITEM.getId(stack.getItem());
                if (id.getPath().endsWith("_roe")) {
                    stack.setCount(stack.getCount() * 2);
                }
            }
            boolean alreadyHas = BookDataManager.get(world).hasUsedBook(playerUuid, "jewels_of_the_sea");
            int bookChance = alreadyHas ? 3 : (3 + BookDataManager.get(world).getCounter(playerUuid));
            if (random.nextInt(100) < bookChance) {
                Item book = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "jewels_of_the_sea"));
                if (book != null) {
                    result.add(new ItemStack(book, 1));
                }
            }
            BookDataManager.get(world).getAndIncrementCounter(playerUuid);
        }

        return result;
    }

    /**
     * 生成钓鱼宝箱战利品（默认25%鱼籽概率，无玩家UUID，无最终运气修正）
     */
    public static List<ItemStack> generateTreasure(ServerWorld world, Random random, int fishingLevel, ItemStack caughtFish) {
        return generateTreasureInternal(world, random, fishingLevel, caughtFish, 0.25f, null, ENTRIES, 0.0012f, 0.0012f, 1.0f, 6);
    }

    /** 内部方法：指定物品池与技能书/靴子概率 */
    private static List<ItemStack> generateTreasureInternal(ServerWorld world, Random random, int fishingLevel, ItemStack caughtFish, float roeChance, UUID playerUuid, List<TreasureEntry> entries, float bookChance, float bootChance, float luckMultiplier, int maxItems) {
        List<ItemStack> result = new ArrayList<>();
        long currentDay = world.getTimeOfDay() / 24000L;
        boolean golden = entries == GOLDEN_ENTRIES;

        for (TreasureEntry entry : entries) {
            if (result.size() >= maxItems) break;
            if (entry.fishingLevelReq >= 0 && fishingLevel < entry.fishingLevelReq) continue;
            // 混合种子：只有钓鱼等级<2时才能获得
            if (entry.itemId.equals("mixedseeds") && fishingLevel >= 2) continue;
            // 金宝箱混合种子：0%不掉落
            if (golden && entry.itemId.equals("mixedseeds")) continue;

            // 迷之盒和金色迷之盒：天数>50才可钓到
            if ((entry.itemId.equals("mystery_box") || entry.itemId.equals("golden_mystery_box")) && currentDay <= 50) {
                continue;
            }
            // 金色迷之盒：需采集精通才能获得
            if (entry.itemId.equals("golden_mystery_box")
                && (playerUuid == null || !stardewvalley.modid.skill.MasteryManager.hasMasteredSkill(world, playerUuid, stardewvalley.modid.skill.SkillRegistry.Category.FORAGING))) {
                continue;
            }

            double chance = entry.baseChance;
            if (golden) {
                // ===== 金宝箱特殊概率区间 =====
                switch (entry.itemId) {
                    case "bait_wild_bait" -> chance = 0.05 + random.nextDouble() * 0.01;     // 5-6%
                    case "mystery_box", "golden_mystery_box" -> chance = 0.09 + random.nextDouble() * 0.11; // 9-20%
                    case "fishtool_dressed_spinner" -> chance = 0.07 + random.nextDouble() * 0.02;  // 7-9%
                    case "fishtool_sonar_bobber" -> chance = 0.09 + random.nextDouble() * 0.01;  // 9-10%
                    case "coal" -> chance = 0.06 + random.nextDouble() * 0.21;  // 6-27%
                    case "bait_deluxe_bait" -> chance = 0.14 + random.nextDouble() * 0.06;  // 14-20%
                    case "bait_bait" -> chance = 0.06 + random.nextDouble() * 0.03;  // 6-9%
                    case "geode" -> chance = 0.02 + random.nextDouble() * 0.09;  // 2-11%
                    case "frozen_geode" -> chance = 0.02 + random.nextDouble() * 0.06;  // 2-8%
                    case "skeletal_tail", "nautilus_fossil", "amphibian_fossil" -> chance = 0.017 + random.nextDouble() * 0.002; // 1.7-1.9%
                    case "diamond" -> chance = 0.02 + random.nextDouble() * 0.01;  // 2-3%
                    case "glow_ring", "magnet_ring" -> chance = 0.0002 + random.nextDouble() * 0.0004; // 0.02-0.06%
                }
            } else {
                // ===== 普通宝箱特殊概率区间 =====
                switch (entry.itemId) {
                    case "coal" -> chance = 0.09 + random.nextDouble() * 0.41;  // 9-50%
                    case "frozen_geode" -> chance = 0.04 + random.nextDouble() * 0.08;  // 4-12%
                    case "iron_ore" -> chance = 0.05 + random.nextDouble() * 0.05;  // 5-10%
                    case "geode" -> chance = 0.04 + random.nextDouble() * 0.12;  // 4-16%
                    case "copper_ore" -> chance = 0.05 + random.nextDouble() * 0.07;  // 5-12%
                    case "wood", "misc_stone" -> chance = 0.05 + random.nextDouble() * 0.01;  // 5-6%
                    case "bait_bait" -> chance = 0.09 + random.nextDouble() * 0.28;  // 9-37%
                    case "bait_deluxe_bait" -> chance = 0.21 + random.nextDouble() * 0.09;  // 21-30%
                    case "fishtool_sonar_bobber" -> chance = 0.03 + random.nextDouble() * 0.01;  // 3-4%
                    case "bait_wild_bait" -> chance = 0.08 + random.nextDouble() * 0.01;  // 8-9%
                    case "diamond" -> chance = 0.03 + random.nextDouble() * 0.01;  // 3-4%
                    case "dinosaur_egg", "ancient_seed" -> chance = 0.007 + random.nextDouble() * 0.001; // 0.7-0.8%
                    case "glow_ring", "magnet_ring" -> chance = 0.0003 + random.nextDouble() * 0.0006; // 0.03-0.09%
                    case "skeletal_tail", "nautilus_fossil", "amphibian_fossil" -> chance = 0.028 + random.nextDouble() * 0.003; // 2.8-3.1%
                    case "mystery_box" -> {
                        chance = 0.07 + random.nextDouble() * 0.09;  // 7-16%
                        if (playerUuid != null && BookDataManager.get(world).hasUsedBook(playerUuid, "book_of_mysteries")) {
                            chance *= 1.33;
                        }
                    }
                    case "golden_mystery_box" -> {
                        chance = 0.07 + random.nextDouble() * 0.09;  // 7-16%
                        if (playerUuid != null && BookDataManager.get(world).hasUsedBook(playerUuid, "book_of_mysteries")) {
                            chance *= 1.33;
                        }
                    }
                }
            }

            // 金色动物饼干：概率×最终运气
            if (entry.itemId.equals("golden_animal_cracker")) {
                chance *= luckMultiplier;
            }

            if (random.nextDouble() < chance) {
                int count = entry.minCount + (entry.maxCount > entry.minCount ? random.nextInt(entry.maxCount - entry.minCount + 1) : 0);
                ItemStack stack = buildStack(entry.itemId, count);
                if (!stack.isEmpty()) {
                    result.add(stack);
                }
            }
        }

        // 技能书（普通各0.12% / 金宝箱各1.5%）
        if (result.size() < maxItems) {
            for (String book : SKILL_BOOKS) {
                if (result.size() >= maxItems) break;
                if (random.nextDouble() < bookChance) {
                    ItemStack stack = buildStack(book, 1);
                    if (!stack.isEmpty()) result.add(stack);
                }
            }
        }

        // 任意鱼饵和浮漂（普通0.12% / 金宝箱1.5%）
        if (result.size() < maxItems && random.nextDouble() < bookChance) {
            String picked = BAIT_TACKLE[random.nextInt(BAIT_TACKLE.length)];
            ItemStack stack = buildStack(picked, 1);
            if (!stack.isEmpty()) result.add(stack);
        }

        // 靴子（普通各0.012% / 金宝箱各0.008%）
        if (fishingLevel >= 2 && result.size() < maxItems) {
            for (String boot : BOOTS) {
                if (result.size() >= maxItems) break;
                if (random.nextDouble() < bootChance) {
                    ItemStack stack = buildStack(boot, 1);
                    if (!stack.isEmpty()) result.add(stack);
                }
            }
        }

        // 鱼籽（25%）- 对应钓上来的鱼的鱼籽
        if (result.size() < maxItems && caughtFish != null && !caughtFish.isEmpty()) {
            if (random.nextDouble() < roeChance) {
                String fishId = Registries.ITEM.getId(caughtFish.getItem()).getPath();
                String baseName = fishId;
                if (baseName.endsWith("_iridium")) baseName = baseName.substring(0, baseName.length() - 8);
                else if (baseName.endsWith("_gold")) baseName = baseName.substring(0, baseName.length() - 5);
                else if (baseName.endsWith("_silver")) baseName = baseName.substring(0, baseName.length() - 7);

                if (baseName.startsWith("fish_")) {
                    String roeName = baseName.substring(5) + "_roe";
                    Identifier roeId = Identifier.of(StardewValley.MOD_ID, roeName);
                    if (Registries.ITEM.containsId(roeId)) {
                        int roeCount = 1 + random.nextInt(6);
                        ItemStack roeStack = new ItemStack(Registries.ITEM.get(roeId), roeCount);
                        result.add(roeStack);
                    }
                }
            }
        }

        // 如果结果为空，至少给一样东西兜底
        if (result.isEmpty()) {
            ItemStack stack = buildStack("coal", 1 + random.nextInt(5));
            if (!stack.isEmpty()) result.add(stack);
        }

        return result;
    }

    private static ItemStack buildStack(String itemId, int count) {
        Identifier id = Identifier.of(StardewValley.MOD_ID, itemId);
        if (!Registries.ITEM.containsId(id)) return ItemStack.EMPTY;
        return new ItemStack(Registries.ITEM.get(id), count);
    }
}
