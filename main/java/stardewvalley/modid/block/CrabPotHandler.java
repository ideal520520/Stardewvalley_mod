package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.skill.SkillEffectHelper;

import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class CrabPotHandler {

    private static final String[] GARBAGE_ITEMS = {
        "stardewvalley:trash_item",
        "stardewvalley:driftwood",
        "stardewvalley:soggy_newspaper",
        "stardewvalley:broken_cd",
        "stardewvalley:broken_glasses"
    };

    private static final String[] ALL_CREATURES = {
        "stardewvalley:fish_lobster",
        "stardewvalley:caiji_clam",
        "stardewvalley:fish_crab",
        "stardewvalley:caiji_cockle",
        "stardewvalley:caiji_mussel",
        "stardewvalley:fish_shrimp",
        "stardewvalley:caiji_oyster",
        "stardewvalley:fish_crayfish",
        "stardewvalley:fish_snail",
        "stardewvalley:fish_periwinkle"
    };

    /** 蟹笼生物名 → 在 ALL_CREATURES 中的完整 ID */
    private static final java.util.Map<String, String> CREATURE_BAIT_MAP = java.util.Map.ofEntries(
        java.util.Map.entry("lobster", "stardewvalley:fish_lobster"),
        java.util.Map.entry("clam", "stardewvalley:caiji_clam"),
        java.util.Map.entry("crab", "stardewvalley:fish_crab"),
        java.util.Map.entry("cockle", "stardewvalley:caiji_cockle"),
        java.util.Map.entry("mussel", "stardewvalley:caiji_mussel"),
        java.util.Map.entry("shrimp", "stardewvalley:fish_shrimp"),
        java.util.Map.entry("oyster", "stardewvalley:caiji_oyster"),
        java.util.Map.entry("crayfish", "stardewvalley:fish_crayfish"),
        java.util.Map.entry("snail", "stardewvalley:fish_snail"),
        java.util.Map.entry("periwinkle", "stardewvalley:fish_periwinkle")
    );

    public static void registerCrabPot(ServerWorld world, BlockPos pos) {
        CrabPotState.get(world).addPot(pos);
    }

    /**
     * 处理单个蟹笼的产出（供仙尘使用）
     */
    public static boolean processSinglePot(ServerWorld world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        if (!(blockState.getBlock() instanceof CrabPotBlock)) return false;

        if (!blockState.contains(CrabPotBlock.WATERLOGGED) || !blockState.get(CrabPotBlock.WATERLOGGED)) return false;

        CrabPotBlockEntity be = (CrabPotBlockEntity) world.getBlockEntity(pos);
        if (be == null) return false;

        long currentDay = world.getTimeOfDay() / 24000L;
        if (!be.getCaughtItemId().isEmpty()) return false;
        if (be.getLastHarvestDay() == currentDay) return false;

        UUID placerUuid = be.getPlacerUuid();
        boolean hasLuremaster = placerUuid != null && SkillEffectHelper.hasSkill(world, placerUuid, "luremaster");
        String baitId = be.getBaitItemId();
        boolean hasBait = baitId != null && !baitId.isEmpty();

        if (!hasBait && !hasLuremaster) return false;

        boolean hasMariner = placerUuid != null && SkillEffectHelper.hasSkill(world, placerUuid, "mariner");

        // 检查是否为针对性鱼饵
        String targetCreature = getTargetedCreature(baitId);

        float garbageChance = 0.38f;
        boolean isWildBait = baitId != null && baitId.contains("bait_wild_bait");
        boolean isDeluxeBait = baitId != null && baitId.contains("bait_deluxe_bait");
        boolean isTargetedBait = targetCreature != null;
        if (isWildBait || isDeluxeBait) garbageChance = 0.19f;
        if (isTargetedBait) garbageChance = 0.19f; // 针对性鱼饵同样降低一半垃圾概率
        if (hasMariner) garbageChance = 0f;

        float luckMult = StardewValley.getLuckMultiplier(world);
        garbageChance *= (2.0f - luckMult);

        String caughtItemId;
        int caughtQuality = 0;

        if (world.random.nextFloat() < garbageChance) {
            caughtItemId = GARBAGE_ITEMS[world.random.nextInt(GARBAGE_ITEMS.length)];
        } else {
            // 如果有针对性鱼饵且目标生物在蟹笼列表中，85%概率捕获目标
            if (targetCreature != null && world.random.nextFloat() < 0.85f) {
                caughtItemId = targetCreature;
            } else {
                caughtItemId = ALL_CREATURES[world.random.nextInt(ALL_CREATURES.length)];
            }
        }

        if ((isDeluxeBait || isTargetedBait) && !caughtItemId.startsWith("stardewvalley:trash_item")
            && !caughtItemId.startsWith("stardewvalley:driftwood")
            && !caughtItemId.startsWith("stardewvalley:soggy_newspaper")
            && !caughtItemId.startsWith("stardewvalley:broken_cd")
            && !caughtItemId.startsWith("stardewvalley:broken_glasses")) {
            caughtQuality = 1;
        }

        be.setCaughtItemId(caughtItemId);
        be.setCaughtQuality(caughtQuality);
        be.setCaughtCount(1);
        be.setLastHarvestDay(currentDay);

        if (!hasLuremaster && hasBait) {
            if (be.getBaitCount() > 1) {
                be.setBaitCount(be.getBaitCount() - 1);
            } else {
                be.setBaitItemId("");
                be.setBaitCount(0);
            }
        }
        return true;
    }

    /**
     * 从鱼饵ID中提取目标蟹笼生物名。
     * 只返回蟹笼能捕获的生物（lobster/clam/crab/cockle/mussel/shrimp/oyster/crayfish/snail/periwinkle），
     * 其他鱼的针对性鱼饵返回 null（对蟹笼无效）。
     */
    private static String getTargetedCreature(String baitId) {
        if (baitId == null || baitId.isEmpty()) return null;
        // 提取 bait_{fishname}_bait 中的 fishname
        String path = baitId;
        if (path.contains(":")) path = path.substring(path.indexOf(':') + 1);
        if (!path.startsWith("bait_") || !path.endsWith("_bait")) return null;
        String fishName = path.substring(5, path.length() - 5);
        if (fishName.isEmpty()) return null;
        // 从标准鱼饵ID中排除
        if (fishName.equals("bait") || fishName.equals("challenge") || fishName.equals("deluxe")
            || fishName.equals("magic") || fishName.equals("magnet") || fishName.equals("wild")) return null;
        // 检查是否在蟹笼生物映射中
        return CREATURE_BAIT_MAP.get(fishName);
    }

    public static void removeCrabPot(ServerWorld world, BlockPos pos) {
        CrabPotState.get(world).removePot(pos);
    }

    /**
     * Called at timeInDay == 14 (14 ticks). Processes all crab pots for loot.
     */
    public static void tickCrabPots(ServerWorld world) {
        Set<BlockPos> positions = CrabPotState.get(world).getPotPositions();
        if (positions == null || positions.isEmpty()) return;

        Iterator<BlockPos> it = positions.iterator();

        while (it.hasNext()) {
            BlockPos pos = it.next();

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;
            if (!world.isChunkLoaded(chunkX, chunkZ)) {
                world.getChunk(chunkX, chunkZ);
            }

            if (!processSinglePot(world, pos)) {
                it.remove();
            }
        }
    }
}
