package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import stardewvalley.modid.equipment.RingEffectHandler;

public class JojaColaItem extends Item {

    private final float healAmount;

    public JojaColaItem(Settings settings, float healAmount) {
        super(settings);
        this.healAmount = healAmount;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient()) {
            user.heal(healAmount);
            if (user instanceof ServerPlayerEntity player) {
                RingEffectHandler.registerFoodBuff(player, StatusEffects.SPEED, 1, 420);
            } else {
                user.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 420, 0));
            }
        }
        return super.finishUsing(stack, world, user);
    }
}
