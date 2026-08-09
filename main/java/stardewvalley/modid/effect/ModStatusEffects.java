package stardewvalley.modid.effect;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

class SimpleStatusEffect extends StatusEffect {
    protected SimpleStatusEffect(StatusEffectCategory category, int color) {
        super(category, color);
    }
}

public class ModStatusEffects {

    public static final RegistryEntry<StatusEffect> YOBA_BLESSING;
    public static final RegistryEntry<StatusEffect> DEFENSE_BUFF;
    public static final RegistryEntry<StatusEffect> LUCK_BUFF;
    public static final RegistryEntry<StatusEffect> IMMUNITY;
    public static final RegistryEntry<StatusEffect> OIL_OF_GARLIC;
    public static final RegistryEntry<StatusEffect> MONSTER_MUSK;
    public static final RegistryEntry<StatusEffect> MAGNETISM_BUFF;
    public static final RegistryEntry<StatusEffect> FARMING_BUFF;
    public static final RegistryEntry<StatusEffect> FISHING_BUFF;
    public static final RegistryEntry<StatusEffect> FORAGING_BUFF;
    public static final RegistryEntry<StatusEffect> MINING_BUFF;
    public static final RegistryEntry<StatusEffect> SQUID_INK_RAVIOLI_BUFF;
    public static final RegistryEntry<StatusEffect> MAX_ENERGY_BUFF;
    // 精疲力尽（负面效果）
    public static final RegistryEntry<StatusEffect> EXHAUSTED;
    // 装备专用效果（与食物效果分离，避免叠加冲突）
    public static final RegistryEntry<StatusEffect> RING_LUCK_BUFF;
    public static final RegistryEntry<StatusEffect> RING_DEFENSE_BUFF;
    public static final RegistryEntry<StatusEffect> RING_MAGNETISM_BUFF;

    static {
        YOBA_BLESSING = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "yoba_blessing"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFD700));
        DEFENSE_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "defense_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8B4513));
        LUCK_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "luck_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x00FF00));
        IMMUNITY = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "immunity"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xE0E0E0));
        OIL_OF_GARLIC = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "oil_of_garlic"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFFFFF));
        MONSTER_MUSK = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "monster_musk"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xCC88FF));
        MAGNETISM_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "magnetism_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFFAA00));
        FARMING_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "farming_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x4CAF50));
        FISHING_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "fishing_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x2196F3));
        FORAGING_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "foraging_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF9800));
        MINING_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "mining_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x9C27B0));
        SQUID_INK_RAVIOLI_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "squid_ink_ravioli_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x607D8B));
        MAX_ENERGY_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "max_energy_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xFF5722));

        EXHAUSTED = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "exhausted"),
            new SimpleStatusEffect(StatusEffectCategory.HARMFUL, 0x808080));

        RING_LUCK_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "ring_luck_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x00CC00));
        RING_DEFENSE_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "ring_defense_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0x8B6914));
        RING_MAGNETISM_BUFF = Registry.registerReference(Registries.STATUS_EFFECT,
            Identifier.of(StardewValley.MOD_ID, "ring_magnetism_buff"),
            new SimpleStatusEffect(StatusEffectCategory.BENEFICIAL, 0xCC8800));
    }

    public static void register() {}
}
