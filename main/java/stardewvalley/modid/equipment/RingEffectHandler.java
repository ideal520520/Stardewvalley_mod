package stardewvalley.modid.equipment;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.season.LuckManager;
import stardewvalley.modid.gui.GoldManager;
import stardewvalley.modid.item.ModShoesItem;
import stardewvalley.modid.item.ModWeaponItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RingEffectHandler {

    private static final Set<RegistryEntry<StatusEffect>> NEGATIVE_EFFECTS = Set.of(
        StatusEffects.POISON, StatusEffects.WITHER, StatusEffects.WEAKNESS,
        StatusEffects.SLOWNESS, StatusEffects.MINING_FATIGUE, StatusEffects.BLINDNESS,
        StatusEffects.DARKNESS, StatusEffects.LEVITATION, StatusEffects.NAUSEA,
        StatusEffects.HUNGER, StatusEffects.INSTANT_DAMAGE
    );

    private record FoodBuff(int level, int duration) {}

    private static final Map<UUID, Boolean> phoenixUsed = new HashMap<>();
    private static final Map<UUID, Integer> protectionImmunityTicks = new HashMap<>();
    private static final Map<UUID, Boolean> prevGlowVision = new HashMap<>();
    private static final Map<UUID, Integer> prevDefense = new HashMap<>();
    private static final Map<UUID, Integer> prevLuck = new HashMap<>();
    private static final Map<UUID, Integer> prevImmunity = new HashMap<>();
    private static final Map<UUID, Integer> prevMagnetism = new HashMap<>();
    private static final Map<UUID, FoodBuff> luckFood = new HashMap<>();
    private static final Map<UUID, FoodBuff> defFood = new HashMap<>();
    private static final Map<UUID, FoodBuff> magFood = new HashMap<>();

    private static String getRingId(EquipmentInventory inv, int slot) {
        ItemStack stack = inv.getSlot(slot);
        if (stack.isEmpty()) return null;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!StardewValley.MOD_ID.equals(id.getNamespace())) return null;
        return id.getPath();
    }

    private static final Identifier WIND_WAY_SPEED_ID = Identifier.of(StardewValley.MOD_ID, "wind_way_speed");

    /** 由 DishItem 调用，直接注册食物增益，避免 addStatusEffect 导致瞬间图标闪现 */
    public static void registerFoodBuff(UUID playerUuid, RegistryEntry<StatusEffect> type, int level, int duration) {
        FoodBuff fb = new FoodBuff(level, duration);
        if (type == ModStatusEffects.LUCK_BUFF) {
            FoodBuff cur = luckFood.get(playerUuid);
            if (cur == null || level > cur.level() || (level == cur.level() && duration > cur.duration())) {
                luckFood.put(playerUuid, fb);
            }
        } else if (type == ModStatusEffects.DEFENSE_BUFF) {
            FoodBuff cur = defFood.get(playerUuid);
            if (cur == null || level > cur.level() || (level == cur.level() && duration > cur.duration())) {
                defFood.put(playerUuid, fb);
            }
        } else if (type == ModStatusEffects.MAGNETISM_BUFF) {
            FoodBuff cur = magFood.get(playerUuid);
            if (cur == null || level > cur.level() || (level == cur.level() && duration > cur.duration())) {
                magFood.put(playerUuid, fb);
            }
        }
    }

    public static Map<String, Integer> getEquippedRings(ServerPlayerEntity player) {
        Map<String, Integer> counts = new HashMap<>();
        var world = (ServerWorld) player.getEntityWorld();
        EquipmentInventory inv = EquipmentStateManager.get(world).getPlayerData(player.getUuid());
        for (int i = 0; i < 2; i++) {
            String id = getRingId(inv, i);
            if (id != null) counts.put(id, counts.getOrDefault(id, 0) + 1);
        }
        return counts;
    }

    /** 每tick */
    public static void onPlayerTick(ServerPlayerEntity player) {
        var counts = getEquippedRings(player);
        UUID uuid = player.getUuid();
        var world = (ServerWorld) player.getEntityWorld();
        EquipmentInventory inv = EquipmentStateManager.get(world).getPlayerData(uuid);

        // 保护戒指免伤计时递减
        protectionImmunityTicks.computeIfPresent(uuid, (k, v) -> v <= 1 ? null : v - 1);

        // 发光/辉石戒指：夜视（永久/卸下清除/外部干扰可恢复）
        boolean hasGlow = counts.containsKey("small_glow_ring") || counts.containsKey("glow_ring")
            || counts.containsKey("iridium_band") || counts.containsKey("glowstone_ring");
        boolean prevGlow = prevGlowVision.getOrDefault(uuid, false);
        if (hasGlow != prevGlow) {
            if (prevGlow) {
                player.removeStatusEffect(StatusEffects.NIGHT_VISION);
            }
            if (hasGlow) {
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, true, false));
            }
            prevGlowVision.put(uuid, hasGlow);
        }
        // 外部清除检测：应存在但不在玩家身上
        if (hasGlow && !player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, StatusEffectInstance.INFINITE, 0, true, false));
            prevGlowVision.put(uuid, true);
        }

        // 防御 Buff（RING_DEFENSE_BUFF 显示总等级，移除食物 DEFENSE_BUFF）
        int equipDefense = 0;
        ItemStack held = player.getMainHandStack();
        if (held.getItem() instanceof ModWeaponItem weapon) {
            equipDefense += (int) weapon.getDefense();
        }
        equipDefense += counts.getOrDefault("topaz_ring", 0) * 1;
        equipDefense += counts.getOrDefault("crabshell_ring", 0) * 5;
        ItemStack shoeStack = inv.getSlot(2);
        if (shoeStack.getItem() instanceof ModShoesItem shoes) {
            equipDefense += shoes.getDefense();
        }
        if (BookDataManager.get(world).hasUsedBook(player.getUuid(), "jack_be_nimble_jack_be_thick")) {
            equipDefense += 1;
        }
        // 从持久Map读取食物防御（由 registerFoodBuff 写入）
        FoodBuff defTracked = defFood.get(uuid);
        int defFoodLevel = 0;
        int defFoodDur = 0;
        if (defTracked != null) {
            int remain = defTracked.duration() - 1;
            if (remain > 0) {
                defFood.put(uuid, new FoodBuff(defTracked.level(), remain));
                defFoodLevel = defTracked.level();
                defFoodDur = remain;
            } else {
                defFood.remove(uuid);
            }
        }
        int totalDefense = equipDefense + defFoodLevel;
        if (totalDefense != prevDefense.getOrDefault(uuid, -1)) {
            player.removeStatusEffect(ModStatusEffects.RING_DEFENSE_BUFF);
            if (totalDefense > 0) {
                int dur = defFoodLevel > 0 ? defFoodDur : StatusEffectInstance.INFINITE;
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.RING_DEFENSE_BUFF, dur, totalDefense - 1, true, false, true));
            }
            prevDefense.put(uuid, totalDefense);
        }
        // 外部清除检测（牛奶/死亡）：效果应该存在但不在玩家身上
        if (totalDefense > 0 && !player.hasStatusEffect(ModStatusEffects.RING_DEFENSE_BUFF)) {
            if (defFoodLevel > 0) defFood.remove(uuid);
            if (equipDefense > 0) {
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.RING_DEFENSE_BUFF, StatusEffectInstance.INFINITE, equipDefense - 1, true, false, true));
            }
            prevDefense.put(uuid, equipDefense);
        }

        // 幸运 Buff（RING_LUCK_BUFF 显示总等级）
        int luckLevel = counts.getOrDefault("lucky_ring", 0);
        FoodBuff luckTracked = luckFood.get(uuid);
        int foodLuckLevel = 0;
        int foodLuckDur = 0;
        if (luckTracked != null) {
            int remain = luckTracked.duration() - 1;
            if (remain > 0) {
                luckFood.put(uuid, new FoodBuff(luckTracked.level(), remain));
                foodLuckLevel = luckTracked.level();
                foodLuckDur = remain;
            } else {
                luckFood.remove(uuid);
            }
        }
        int totalLuck = luckLevel + foodLuckLevel;
        if (totalLuck != prevLuck.getOrDefault(uuid, -1)) {
            player.removeStatusEffect(ModStatusEffects.RING_LUCK_BUFF);
            if (totalLuck > 0) {
                int dur = foodLuckLevel > 0 ? foodLuckDur : StatusEffectInstance.INFINITE;
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.RING_LUCK_BUFF, dur, totalLuck - 1, true, false, true));
            }
            prevLuck.put(uuid, totalLuck);
        }
        // 外部清除检测（牛奶/死亡）：效果应该存在但不在玩家身上
        if (totalLuck > 0 && !player.hasStatusEffect(ModStatusEffects.RING_LUCK_BUFF)) {
            if (foodLuckLevel > 0) luckFood.remove(uuid);
            if (luckLevel > 0) {
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.RING_LUCK_BUFF, StatusEffectInstance.INFINITE, luckLevel - 1, true, false, true));
            }
            prevLuck.put(uuid, luckLevel);
        }

        // 免疫 Buff
        int immunityLevel = counts.getOrDefault("immunity_band", 0);
        ItemStack shoeStack2 = inv.getSlot(2);
        int shoeImmunity = 0;
        if (shoeStack2.getItem() instanceof ModShoesItem shoes) {
            shoeImmunity = shoes.getImmunity();
        }
        int totalImmunity = immunityLevel * 4 + shoeImmunity;
        if (totalImmunity != prevImmunity.getOrDefault(uuid, -1)) {
            player.removeStatusEffect(ModStatusEffects.IMMUNITY);
            if (totalImmunity > 0) {
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IMMUNITY, StatusEffectInstance.INFINITE, totalImmunity - 1, true, false, true));
            }
            prevImmunity.put(uuid, totalImmunity);
        }
        // 外部清除检测（牛奶/死亡）：效果应该存在但不在玩家身上
        if (totalImmunity > 0 && !player.hasStatusEffect(ModStatusEffects.IMMUNITY)) {
            player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.IMMUNITY, StatusEffectInstance.INFINITE, totalImmunity - 1, true, false, true));
            prevImmunity.put(uuid, totalImmunity);
        }

        // 磁性 Buff（RING_MAGNETISM_BUFF 显示总等级）
        int ringMagnetLevel = counts.getOrDefault("small_magnet_ring", 0) * 1
            + counts.getOrDefault("magnet_ring", 0) * 2
            + counts.getOrDefault("iridium_band", 0) * 2
            + counts.getOrDefault("glowstone_ring", 0) * 1;
        FoodBuff magTracked = magFood.get(uuid);
        int foodMagLevel = 0;
        int foodMagDur = 0;
        if (magTracked != null) {
            int remain = magTracked.duration() - 1;
            if (remain > 0) {
                magFood.put(uuid, new FoodBuff(magTracked.level(), remain));
                foodMagLevel = magTracked.level();
                foodMagDur = remain;
            } else {
                magFood.remove(uuid);
            }
        }
        int totalMagnet = ringMagnetLevel + foodMagLevel;
        if (totalMagnet != prevMagnetism.getOrDefault(uuid, -1)) {
            player.removeStatusEffect(ModStatusEffects.RING_MAGNETISM_BUFF);
            if (totalMagnet > 0) {
                int dur = foodMagLevel > 0 ? foodMagDur : StatusEffectInstance.INFINITE;
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.RING_MAGNETISM_BUFF, dur, totalMagnet - 1, true, false, true));
            }
            prevMagnetism.put(uuid, totalMagnet);
        }
        // 外部清除检测（牛奶/死亡）：效果应该存在但不在玩家身上
        if (totalMagnet > 0 && !player.hasStatusEffect(ModStatusEffects.RING_MAGNETISM_BUFF)) {
            if (foodMagLevel > 0) magFood.remove(uuid);
            if (ringMagnetLevel > 0) {
                player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.RING_MAGNETISM_BUFF, StatusEffectInstance.INFINITE, ringMagnetLevel - 1, true, false, true));
            }
            prevMagnetism.put(uuid, ringMagnetLevel);
        }
        // 磁性拾取（总等级已包含食物）
        if (totalMagnet > 0) {
            double range = 1.5 + totalMagnet;
            double rangeSq = range * range;
            for (net.minecraft.entity.ItemEntity item : player.getEntityWorld().getEntitiesByClass(
                    net.minecraft.entity.ItemEntity.class,
                    player.getBoundingBox().expand(range, range, range),
                    e -> e.isAlive() && !e.cannotPickup() && e.squaredDistanceTo(player) < rangeSq)) {
                item.onPlayerCollision(player);
            }
        }

        // 墨汁意大利饺：免疫所有负面效果
        if (player.hasStatusEffect(ModStatusEffects.SQUID_INK_RAVIOLI_BUFF)) {
            for (var negativeEffect : NEGATIVE_EFFECTS) {
                player.removeStatusEffect(negativeEffect);
            }
        }

        // 结实戒指：每20tick减少负面效果时长，每个戒指减少1s（20tick）
        int sturdyCount = counts.getOrDefault("sturdy_ring", 0);
        if (sturdyCount > 0 && player.age % 20 == 0) {
            for (var effect : new ArrayList<>(player.getStatusEffects())) {
                RegistryEntry<StatusEffect> type = effect.getEffectType();
                if (NEGATIVE_EFFECTS.contains(type)) {
                    int newDuration = effect.getDuration() - 20 * sturdyCount;
                    player.removeStatusEffect(type);
                    if (newDuration > 0) {
                        player.addStatusEffect(new StatusEffectInstance(type, newDuration, effect.getAmplifier(),
                            effect.isAmbient(), effect.shouldShowParticles(), effect.shouldShowIcon()));
                    }
                }
            }
        }

        // 风之道：+12.5%~+25% 移动速度（直接修改属性，无任何视觉效果）
        boolean hasPt1 = BookDataManager.get(world).hasUsedBook(uuid, "way_of_the_wind_pt._1");
        boolean hasPt2 = BookDataManager.get(world).hasUsedBook(uuid, "way_of_the_wind_pt._2");
        EntityAttributeInstance speedAttr = player.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            var existing = speedAttr.getModifier(WIND_WAY_SPEED_ID);
            if (hasPt1 || hasPt2) {
                double targetValue = 0.125 * ((hasPt1 && hasPt2) ? 2 : 1);
                if (existing == null || Math.abs(existing.value() - targetValue) > 0.001) {
                    if (existing != null) speedAttr.removeModifier(existing);
                    speedAttr.addTemporaryModifier(new EntityAttributeModifier(
                        WIND_WAY_SPEED_ID, targetValue, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
                }
            } else if (existing != null) {
                speedAttr.removeModifier(existing);
            }
        }
    }

    /** 击杀时触发 */
    public static void onKill(ServerPlayerEntity player, LivingEntity victim) {
        var counts = getEquippedRings(player);
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // 吸血
        int v = counts.getOrDefault("vampire_ring", 0) + counts.getOrDefault("iridium_band", 0);
        if (v > 0) player.heal(v);

        // 野蛮人
        int sv = counts.getOrDefault("savage_ring", 0);
        if (sv > 0) {
            var e = player.getStatusEffect(StatusEffects.SPEED);
            int dur = (e != null ? e.getDuration() : 0) + 100;
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, dur, 1, true, true));
        }

        // 战士: 概率 0.2 + 最终运气
        float finalLuckWarrior = StardewValley.getFinalLuckMultiplier(world, player) - 1.0f;
        int wc = counts.getOrDefault("warrior_ring", 0);
        if (wc > 0) {
            for (int i = 0; i < wc; i++) {
                if (player.getRandom().nextDouble() < 0.2 + finalLuckWarrior) {
                    var e = player.getStatusEffect(StatusEffects.STRENGTH);
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, (e != null ? e.getDuration() : 0) + 100, 0, true, true));
                }
            }
        }

        // 热咖啡: 概率受最终运气影响 * (1 + 最终运气)，掉落为实体
        float finalLuckCoffee = StardewValley.getFinalLuckMultiplier(world, player) - 1.0f;
        int jc = counts.getOrDefault("hot_java_ring", 0);
        if (jc > 0) {
            double coffeeChance = 0.25 * (1.0 + finalLuckCoffee);
            double espressoChance = 0.10 * (1.0 + finalLuckCoffee);
            for (int i = 0; i < jc; i++) {
                if (player.getRandom().nextDouble() < coffeeChance) {
                    victim.dropStack(world, new ItemStack(Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "coffee"))));
                } else if (player.getRandom().nextDouble() < espressoChance) {
                    victim.dropStack(world, new ItemStack(Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, "triple_shot_espresso"))));
                }
            }
        }

        // 吸魂: 回复15基础体力，经过星露谷能量公式转换后真正回复
        int sc = counts.getOrDefault("soul_sapper_ring", 0);
        if (sc > 0) {
            float skillMult = 1.0f;
            int stamina = (int)(15 * skillMult * sc);
            float raw = stamina * 20.0f / 270.0f;
            int nutrition;
            float saturationModifier;
            float exhaustion = 0.0f;
            if (raw < 1) {
                nutrition = 1;
                saturationModifier = 0.0f;
                exhaustion = (1.0f - raw) * 4.0f;
            } else if (raw <= 16) {
                nutrition = (int) Math.floor(raw);
                saturationModifier = (raw - nutrition) / (nutrition * 2.0f);
            } else {
                nutrition = 16;
                float rawSaturation = (raw - 16) * 2;
                saturationModifier = rawSaturation / (nutrition * 2.0f);
            }
            player.getHungerManager().add(nutrition, saturationModifier);
            if (exhaustion > 0) player.getHungerManager().addExhaustion(exhaustion);
        }
    }

    /** 受伤前调用，返回true跳过原伤害 */
    public static boolean onHurt(ServerPlayerEntity player, DamageSource source, float amount) {
        var counts = getEquippedRings(player);
        ServerWorld world = (ServerWorld) player.getEntityWorld();

        // 史莱姆克星
        if (source.getAttacker() instanceof SlimeEntity || source.getAttacker() instanceof MagmaCubeEntity) {
            if (counts.containsKey("slime_charmer_ring")) return true;
        }

        // 凤凰
        if (player.getHealth() - amount <= 0.0f && amount > 0) {
            if (counts.containsKey("phoenix_ring") && !phoenixUsed.getOrDefault(player.getUuid(), false)) {
                phoenixUsed.put(player.getUuid(), true);
                player.setHealth(player.getMaxHealth() * 0.5f);
                return true;
            }
        }

        // 由巴戒指: 概率触发，获得100%免伤5s（由 DefenseMixin 处理）
        // buff 期间不会再次触发
        int yb = counts.getOrDefault("ring_of_yoba", 0);
        if (yb > 0 && !player.hasStatusEffect(ModStatusEffects.YOBA_BLESSING)) {
            double hpRatio = player.getHealth() / player.getMaxHealth();
            float finalLuckYoba = StardewValley.getFinalLuckMultiplier(world, player) - 1.0f;
            double prob = (0.9 - hpRatio) / (3.0 - finalLuckYoba);
            if (hpRatio < 0.15) prob += 0.2;
            prob = Math.min(prob, 0.9);
            for (int i = 0; i < yb; i++) {
                if (player.getRandom().nextDouble() < prob) {
                    player.addStatusEffect(new StatusEffectInstance(ModStatusEffects.YOBA_BLESSING, 100, 0, true, true));
                }
            }
        }

        // 荆棘: 反伤（原伤害）
        if (counts.containsKey("thorns_ring") && source.getAttacker() instanceof LivingEntity attacker) {
            attacker.damage((ServerWorld) player.getEntityWorld(), source, amount);
        }

        // 燃烧弹: 攻击者燃烧 + 自身抗火
        if (counts.containsKey("napalm_ring") && source.getAttacker() instanceof LivingEntity attacker) {
            attacker.setOnFireFor(3);
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 200, 0, true, true));
        }

        // 保护戒指：受伤后获得 10tick 100% 免伤/个，免伤期间不再触发
        int pc = counts.getOrDefault("protection_ring", 0);
        if (pc > 0 && protectionImmunityTicks.getOrDefault(player.getUuid(), 0) <= 0) {
            protectionImmunityTicks.put(player.getUuid(), pc * 10);
        }

        // 结实: 效果在 onPlayerTick 中处理
        return false;
    }

    public static void resetPhoenix(UUID uuid) { phoenixUsed.remove(uuid); }

    public static boolean hasProtectionImmunity(UUID uuid) {
        return protectionImmunityTicks.getOrDefault(uuid, 0) > 0;
    }
}
