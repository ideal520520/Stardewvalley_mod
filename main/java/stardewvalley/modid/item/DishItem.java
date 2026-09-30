package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
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
                if (user instanceof ServerPlayerEntity player) {
                    RingEffectHandler.applyFoodEffects(player, effects);
                } else {
                    for (StatusEffectInstance effect : effects) {
                        if (effect != null) {
                            user.addStatusEffect(new StatusEffectInstance(effect));
                        }
                    }
                }
            }
        }
        return super.finishUsing(stack, world, user);
    }
}
