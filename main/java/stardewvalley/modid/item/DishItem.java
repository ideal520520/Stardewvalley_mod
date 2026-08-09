package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.equipment.RingEffectHandler;

public class DishItem extends Item {

    private final StatusEffectInstance[] effects;
    private final float healAmount;

    public DishItem(Settings settings, float healAmount, StatusEffectInstance... effects) {
        super(settings);
        this.healAmount = healAmount;
        this.effects = effects;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient()) {
            if (healAmount > 0) {
                user.heal(healAmount);
            }
            if (effects != null) {
                for (StatusEffectInstance effect : effects) {
                    if (effect != null) {
                        // 这三类食物效果由 RingEffectHandler 直接管理，跳过 addStatusEffect 避免闪现图标
                        if (effect.getEffectType() == ModStatusEffects.LUCK_BUFF
                            || effect.getEffectType() == ModStatusEffects.DEFENSE_BUFF
                            || effect.getEffectType() == ModStatusEffects.MAGNETISM_BUFF) {
                            RingEffectHandler.registerFoodBuff(user.getUuid(), effect.getEffectType(),
                                effect.getAmplifier() + 1, effect.getDuration());
                        } else {
                            user.addStatusEffect(effect);
                        }
                    }
                }
            }
        }
        return super.finishUsing(stack, world, user);
    }
}
