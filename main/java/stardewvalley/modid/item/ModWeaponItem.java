package stardewvalley.modid.item;

import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.equipment.GemRingHelper;
import stardewvalley.modid.skill.SkillEffectHelper;

import java.util.Map;
import java.util.function.Consumer;

public class ModWeaponItem extends Item {

    public static final Identifier ATTACK_DAMAGE_MODIFIER_ID = Identifier.of("stardewvalley", "attack_damage");
    public static final Identifier ATTACK_SPEED_MODIFIER_ID = Identifier.of("stardewvalley", "attack_speed");

    private final WeaponType weaponType;
    private final float attackDamage;
    private final float attackSpeed;     // attacks per second (e.g. 1.667 for sword)
    private final float critChance;      // base crit chance
    private final int critPower;          // additional crit power
    private final float defense;         // damage reduction percentage (0-1)
    private final int weight;            // knockback resistance
    private final boolean lifesteal;
    private final boolean crusader;
    private final int weaponLevel;
    private final int sellPrice;

    public ModWeaponItem(Settings settings, WeaponType weaponType, float attackDamage,
                         float attackSpeed, float critChance, int critPower,
                         float defense, int weight, boolean lifesteal,
                         boolean crusader, int weaponLevel, int sellPrice) {
        super(settings);
        this.weaponType = weaponType;
        this.attackDamage = attackDamage;
        this.attackSpeed = attackSpeed;
        this.critChance = critChance;
        this.critPower = critPower;
        this.defense = defense;
        this.weight = weight;
        this.lifesteal = lifesteal;
        this.crusader = crusader;
        this.weaponLevel = weaponLevel;
        this.sellPrice = sellPrice;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public float getWeaponAttackDamage() {
        return attackDamage;
    }

    public float getAttackSpeed() {
        return attackSpeed;
    }

    public float getCritChance() {
        return critChance;
    }

    public int getCritPower() {
        return critPower;
    }

    public float getDefense() {
        return defense;
    }

    public int getWeight() {
        return weight;
    }

    public boolean hasLifesteal() {
        return lifesteal;
    }

    public boolean hasCrusader() {
        return crusader;
    }

    public int getWeaponLevel() {
        return weaponLevel;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    @Override
    public boolean canMine(net.minecraft.item.ItemStack stack, net.minecraft.block.BlockState state, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.entity.LivingEntity user) {
        // 创造模式：所有武器都不能破坏方块
        // 生存模式：匕首不能破坏，剑/锤可以
        if (user instanceof net.minecraft.entity.player.PlayerEntity player && player.isCreative()) return false;
        return weaponType != WeaponType.DAGGER;
    }

    @Override
    public float getMiningSpeed(net.minecraft.item.ItemStack stack, net.minecraft.block.BlockState state) {
        // 匕首返回0，剑/锤返回1.0（和空手一样慢）
        if (weaponType == WeaponType.DAGGER) return 0.0F;
        return 1.0F;
    }

    /**
     * 有效暴击率 = 暴击基础概率 + 暴击率层数 * 0.02
     * 暴击率层数 = critChance / 0.02（1层=2%）
     * 匕首额外：(暴击基础概率 + 暴击率层数 * 0.02 + 0.005) * 1.12
     */
    public float getEffectiveCritChance() {
        int layers = Math.round(critChance / 0.02f);
        float base = critChance + layers * 0.02f;
        if (weaponType == WeaponType.DAGGER) {
            return (base + 0.005f) * 1.12f;
        }
        return base;
    }

    @Override
    public void postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof PlayerEntity player) {
            ServerWorld serverWorld = player.getEntityWorld() instanceof ServerWorld sw ? sw : null;

            // 宝石戒指加成
            Map<String, Integer> gemRings = serverWorld != null ? GemRingHelper.getGemRings((ServerPlayerEntity) player) : new java.util.HashMap<>();
            float dmgMult = GemRingHelper.getDamageMultiplier(gemRings); // 红宝石/铱环
            float critBonus = GemRingHelper.getCritChanceBonus(gemRings); // 海蓝宝石
            int critPowerBonus = GemRingHelper.getCritPowerBonus(gemRings); // 翡翠
            float speedMult = GemRingHelper.getSpeedMultiplier(gemRings); // 绿宝石
            int weightBonus = GemRingHelper.getWeightBonus(gemRings); // 紫水晶

            // 红宝石：攻击力提升
            float modifiedDamage = attackDamage * dmgMult;

            // 战士/野蛮人：额外伤害加成（乘算）
            if (serverWorld != null) {
                float dmgMultSkill = SkillEffectHelper.getDamageMultiplier(serverWorld, player.getUuid());
                if (dmgMultSkill > 1.0f) {
                    float bonusDmg = modifiedDamage * (dmgMultSkill - 1.0f);
                    target.damage(serverWorld, player.getDamageSources().playerAttack(player), bonusDmg);
                }
            }

            // 绿宝石：减少攻击冷却（直接修改攻击冷却进度）
            if (speedMult > 1.0f && player != null) {
                // 通过设置属性修饰符调整攻击速度
                var attr = player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.ATTACK_SPEED);
                if (attr != null) {
                    var modId = Identifier.of(StardewValley.MOD_ID, "emerald_speed");
                    // 清除旧的加成
                    var existing = attr.getModifier(modId);
                    if (existing != null) attr.removeModifier(existing);
                    // 添加速度加成
                    attr.addTemporaryModifier(
                        new net.minecraft.entity.attribute.EntityAttributeModifier(modId, attr.getBaseValue() * (speedMult - 1.0),
                            net.minecraft.entity.attribute.EntityAttributeModifier.Operation.ADD_VALUE));
                }
            }

            if (lifesteal && !target.isAlive()) {
                player.heal(1.0f);
            }
            // 剑类每次命中触发横扫之刃3效果（75%伤害溅射）
            if (weaponType == WeaponType.SWORD) {
                if (serverWorld != null) {
                    serverWorld.spawnParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getBodyY(0.5), target.getZ(), 1, 0.0, 0.0, 0.0, 0.0);
                    float sweepDmg = modifiedDamage * 0.75f;
                    for (var entity : serverWorld.getEntitiesByClass(
                        LivingEntity.class,
                        target.getBoundingBox().expand(1.0, 0.25, 1.0),
                        e -> e != player && e != target && e.isAlive() && !e.isSpectator()
                    )) {
                        entity.damage(serverWorld, player.getDamageSources().playerAttack(player), sweepDmg);
                    }
                }
            }
            // 匕首类：穿刺攻击
            if (weaponType == WeaponType.DAGGER) {
                if (serverWorld != null) {
                    Vec3d lookVec = player.getRotationVec(1.0f).normalize();
                    double tx = target.getX(), ty = target.getY(), tz = target.getZ();
                    float pierceDmg = modifiedDamage * 0.75f;
                    for (var entity : serverWorld.getEntitiesByClass(
                        LivingEntity.class,
                        target.getBoundingBox().expand(0.5, 0.5, 0.5),
                        e -> e != player && e != target && e.isAlive() && !e.isSpectator()
                    )) {
                        Vec3d toEntity = new Vec3d(entity.getX() - tx, entity.getY() - ty, entity.getZ() - tz);
                        if (toEntity.dotProduct(lookVec) > 0) {
                            entity.damage(serverWorld, player.getDamageSources().playerAttack(player), pierceDmg);
                        }
                    }
                }
            }
            // 紫水晶：击退加成
            if (weightBonus > 0) {
                float baseWeight = weight;
                float totalWeight = baseWeight + weightBonus;
                double knockback = 0.4 * (1.0 + totalWeight * 0.1); // 每级权重+10%击退距离
                target.takeKnockback(knockback, player.getX() - target.getX(), player.getZ() - target.getZ());
            }

            // 武器自身暴击判定
            float effectiveCrit = getEffectiveCritChance() + critBonus; // 海蓝宝石：加法
            // 侦查员：暴击几率 *1.5
            if (serverWorld != null) {
                effectiveCrit *= SkillEffectHelper.getCritChanceMultiplier(serverWorld, player.getUuid());
                float luckMult = stardewvalley.modid.StardewValley.getFinalLuckMultiplier(serverWorld, (ServerPlayerEntity) player);
                effectiveCrit += (luckMult - 1.0f) * 0.5f;
            }
            if (effectiveCrit > 0 && target.isAlive() && player.getRandom().nextFloat() < effectiveCrit) {
                float critMult = 1.5f + (critPower + critPowerBonus) / 50f; // 翡翠：暴击力量增加
                // 亡命徒：暴击伤害 *2
                if (serverWorld != null) {
                    critMult *= SkillEffectHelper.getCritDamageMultiplier(serverWorld, player.getUuid());
                }
                float extraDamage = modifiedDamage * (critMult - 1.0f);
                if (serverWorld != null) {
                    target.damage(serverWorld, player.getDamageSources().playerAttack(player), extraDamage);
                    serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT,
                        target.getX(), target.getBodyY(0.5), target.getZ(),
                        30, 0.5, 0.3, 0.5, 0.3);
                    serverWorld.spawnParticles(ParticleTypes.CRIT,
                        target.getX(), target.getBodyY(0.5), target.getZ(),
                        25, 0.4, 0.2, 0.4, 0.15);
                    serverWorld.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.0f, 1.0f);
                }
            }
        }
        super.postHit(stack, target, attacker);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return ActionResult.PASS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> tooltipConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltipConsumer, type);
        tooltipConsumer.accept(Text.literal("等级: " + weaponLevel).formatted(Formatting.DARK_GREEN));
        tooltipConsumer.accept(Text.literal("伤害: " + String.format("%.1f", attackDamage)).formatted(Formatting.RED));
        // 加成栏
        int speedStat = getSpeedStatFromCd();
        if (speedStat != 0) {
            String speedStr = String.format("速度: %+d", speedStat);
            tooltipConsumer.accept(Text.literal(speedStr).formatted(Formatting.GREEN));
        }
        if (defense > 0) {
            tooltipConsumer.accept(Text.literal("防御: +" + (int)defense).formatted(Formatting.AQUA));
        }
        float effectiveCrit = getEffectiveCritChance();
        if (effectiveCrit > 0) {
            tooltipConsumer.accept(Text.literal("暴击率: +" + String.format("%.0f", effectiveCrit * 100) + "%").formatted(Formatting.BLUE));
        }
        if (critPower > 0) {
            tooltipConsumer.accept(Text.literal("暴击力量: +" + critPower).formatted(Formatting.BLUE));
        }
        if (weight > 0) {
            tooltipConsumer.accept(Text.literal("重量: +" + weight).formatted(Formatting.DARK_GRAY));
        }
        if (lifesteal) {
            tooltipConsumer.accept(Text.literal("吸血").formatted(Formatting.DARK_RED));
        }
        if (crusader) {
            tooltipConsumer.accept(Text.literal("十字军").formatted(Formatting.YELLOW));
        }
    }

    private int getSpeedStatFromCd() {
        float baseCd = switch (weaponType) {
            case SWORD -> 600f;
            case DAGGER -> 300f;
            case CLUB -> 900f;
        };
        // 攻击速度 = 1000/cd, speedMod = calculated value  
        // reverse-engineer the speed stat
        float actualCd = 1000f / attackSpeed;
        int speedStat = Math.round((baseCd - actualCd) / 40f);
        return speedStat;
    }

    public enum WeaponType {
        SWORD, DAGGER, CLUB
    }
}
