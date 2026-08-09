package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import net.minecraft.entity.effect.StatusEffectInstance;
import stardewvalley.modid.effect.ModStatusEffects;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StardropItem extends Item {
    private static final Map<UUID, Integer> STARDROP_COUNTS = new HashMap<>();
    public static final int SATURATION_PER_STARDROP = 4;
    public static final int BASE_SATURATION = 20;

    public StardropItem(Settings settings) {
        super(settings);
    }

    public static int getStardropCount(UUID uuid) {
        return STARDROP_COUNTS.getOrDefault(uuid, 0);
    }

    private static Object saturationCapKey;
    private static boolean saturationPlusLoaded = true;

    private static void setSaturationCap(ServerWorld world, int value) {
        if (!saturationPlusLoaded) return;
        try {
            if (saturationCapKey == null) {
                Class<?> spClass = Class.forName("com.kerbe.SaturationPlus");
                saturationCapKey = spClass.getField("SATURATION_CAP").get(null);
            }
            Object gameRules = world.getGameRules();
            Method getMethod = gameRules.getClass().getMethod("get", saturationCapKey.getClass());
            Object rule = getMethod.invoke(gameRules, saturationCapKey);
            Method setMethod = rule.getClass().getMethod("set", int.class, net.minecraft.server.MinecraftServer.class);
            setMethod.invoke(rule, value, (net.minecraft.server.MinecraftServer) null);
        } catch (Exception ignored) {
        }
    }

    public static void initDefaultSaturationCap(ServerWorld world) {
        setSaturationCap(world, Integer.MAX_VALUE);
    }

    public static void clampPlayerSaturation(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        int count = STARDROP_COUNTS.getOrDefault(uuid, 0);
        int personalMax = BASE_SATURATION + SATURATION_PER_STARDROP * count;
        // 食物体力值上限增益：每级+2饱和值上限
        StatusEffectInstance maxEnergyEffect = player.getStatusEffect(ModStatusEffects.MAX_ENERGY_BUFF);
        if (maxEnergyEffect != null) {
            personalMax += (maxEnergyEffect.getAmplifier() + 1) * 2;
        }
        float currentSat = player.getHungerManager().getSaturationLevel();
        if (currentSat > personalMax) {
            player.getHungerManager().setSaturationLevel((float) personalMax);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            UUID uuid = player.getUuid();
            int count = STARDROP_COUNTS.getOrDefault(uuid, 0) + 1;
            STARDROP_COUNTS.put(uuid, count);

            int newMaxSaturation = BASE_SATURATION + SATURATION_PER_STARDROP * count;

            player.heal(1000.0f);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel((float) newMaxSaturation);

            if (world instanceof ServerWorld serverWorld) {
                setSaturationCap(serverWorld, Integer.MAX_VALUE);
            }

            stack.decrement(1);
        }
        return stack;
    }
}