package stardewvalley.modid.item;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ArtisanItem extends Item {

    public enum ArtisanType {
        WINE, JELLY, DRIED_FRUIT, JUICE, PICKLE, ROE, AGED_ROE, CAVIAR, SMOKED_FISH
    }

    private final ArtisanType type;
    private final float healAmount;
    private final int sellPrice;
    private final boolean edible;
    private final boolean isDrink;
    private final StatusEffectInstance[] effects;

    // Formula-based constructor (normal artisan items)
    public ArtisanItem(Settings settings, ArtisanType type, int baseStamina, int baseHeal, int basePrice, boolean hasFood, int qualityTier) {
        this(settings, type, baseStamina, baseHeal, basePrice, hasFood, qualityTier, (StatusEffectInstance[]) null);
    }

    public ArtisanItem(Settings settings, ArtisanType type, int baseStamina, int baseHeal, int basePrice, boolean hasFood, int qualityTier, StatusEffectInstance... effects) {
        super(settings);

        this.type = type;
        this.effects = effects;
        float qualityHealMul = switch (qualityTier) { case 0 -> 1.0f; case 1 -> 1.4f; case 2 -> 1.8f; case 3 -> 2.6f; default -> 1.0f; };
        float qualityMoneyMul = switch (qualityTier) { case 0 -> 1.0f; case 1 -> 1.25f; case 2 -> 1.5f; case 3 -> 2.0f; default -> 1.0f; };

        boolean hasPositiveFood = hasFood && baseStamina > 0;
        boolean hasNegativeFood = hasFood && baseStamina < 0;

        int finalStamina;
        float finalHealAmount;
        int finalSellPrice;

        switch (type) {
            case WINE -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * 1.75f);
                    finalHealAmount = (baseHeal * 1.75f) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = baseStamina;
                    finalHealAmount = baseHeal / 5.0f;
                } else {
                    finalStamina = Math.round(basePrice * 0.1f);
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round(basePrice * 3.0f * qualityMoneyMul);
            }
            case JELLY -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * 2.0f);
                    finalHealAmount = (baseHeal * 2.0f) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = baseStamina;
                    finalHealAmount = baseHeal / 5.0f;
                } else {
                    finalStamina = Math.round(basePrice * 0.2f);
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round((basePrice * 2.0f + 50.0f) * qualityMoneyMul);
            }
            case DRIED_FRUIT -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * 3.0f);
                    finalHealAmount = (baseHeal * 3.0f) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = baseStamina;
                    finalHealAmount = baseHeal / 5.0f;
                } else {
                    finalStamina = Math.round(basePrice * 0.5f);
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round((basePrice * 7.5f + 25.0f) * qualityMoneyMul);
            }
            case JUICE -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * 2.0f);
                    finalHealAmount = (baseHeal * 2.0f) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = baseStamina;
                    finalHealAmount = baseHeal / 5.0f;
                } else {
                    finalStamina = Math.round(basePrice * 0.4f);
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round(basePrice * 2.25f * qualityMoneyMul);
            }
            case PICKLE -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * 1.75f);
                    finalHealAmount = (baseHeal * 1.75f) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = baseStamina;
                    finalHealAmount = baseHeal / 5.0f;
                } else {
                    finalStamina = Math.round(basePrice * 0.25f);
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round((basePrice * 2.0f + 50.0f) * qualityMoneyMul);
            }
            case ROE -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * qualityHealMul);
                    finalHealAmount = (baseHeal * qualityHealMul) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = baseStamina;
                    finalHealAmount = baseHeal / 5.0f;
                } else {
                    finalStamina = 0;
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round(basePrice * qualityMoneyMul);
            }
            case AGED_ROE -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * qualityHealMul);
                    finalHealAmount = (baseHeal * qualityHealMul) / 5.0f;
                } else {
                    finalStamina = 0;
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round(basePrice * 2.0f * qualityMoneyMul);
            }
            case CAVIAR -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * qualityHealMul);
                    finalHealAmount = (baseHeal * qualityHealMul) / 5.0f;
                } else {
                    finalStamina = 0;
                    finalHealAmount = 0;
                }
                finalSellPrice = Math.round(basePrice * qualityMoneyMul);
            }
            case SMOKED_FISH -> {
                if (hasPositiveFood) {
                    finalStamina = Math.round(baseStamina * 1.5f);
                    finalHealAmount = (baseHeal * 1.5f) / 5.0f;
                } else if (hasNegativeFood) {
                    finalStamina = Math.round(baseStamina * 1.5f);
                    finalHealAmount = (baseHeal * 1.5f) / 5.0f;
                } else {
                    // 不可食用的鱼熏制后：体力=价格×2/3，生命=价格×1/3
                    finalStamina = Math.round(basePrice * 2.0f / 3.0f);
                    finalHealAmount = (basePrice / 3.0f) / 5.0f;
                }
                finalSellPrice = Math.round(basePrice * 2.0f * qualityMoneyMul);
            }
            default -> {
                finalStamina = 0;
                finalHealAmount = 0;
                finalSellPrice = 0;
            }
        }

        this.healAmount = finalHealAmount * qualityHealMul;
        this.sellPrice = finalSellPrice;
        this.edible = (finalStamina > 0 || baseStamina < 0);
        this.isDrink = (type == ArtisanType.WINE || type == ArtisanType.JUICE);
    }

    // Fixed-value constructor (for special drinks with custom stats)
    public ArtisanItem(Settings settings, int stamina, float healAmount, int sellPrice, boolean edible, boolean isDrink, StatusEffectInstance... effects) {
        super(settings);
        this.type = ArtisanType.WINE;
        this.effects = effects;
        this.healAmount = healAmount;
        this.sellPrice = sellPrice;
        this.edible = edible;
        this.isDrink = isDrink;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && edible) {
            if (healAmount > 0) {
                user.heal(healAmount);
            }
            if (effects != null) {
                for (StatusEffectInstance effect : effects) {
                    if (effect != null) {
                        user.addStatusEffect(effect);
                    }
                }
            } else if (type == ArtisanType.WINE) {
                // Default wine effect: slowness 1 for 30s
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 600, 0));
            }
        }
        return super.finishUsing(stack, world, user);
    }

    public float getHealAmount() {
        return healAmount;
    }

    public int getSellPrice() {
        return sellPrice;
    }

    public boolean isEdible() {
        return edible;
    }
}
