package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import stardewvalley.modid.crop.CropQuality;

public class CropItem extends Item {

    private final float healAmount;
    private final int baseMoneyValue;
    private final int moneyValue;
    private final CropQuality quality;
    private final float exhaustion;

    public CropItem(Settings settings, float baseHealAmount, int baseMoneyValue, CropQuality quality, float exhaustion) {
        super(settings);
        this.healAmount = baseHealAmount * quality.getHealMultiplier();
        this.baseMoneyValue = baseMoneyValue;
        this.moneyValue = Math.round(baseMoneyValue * quality.getMoneyMultiplier());
        this.quality = quality;
        this.exhaustion = exhaustion;
    }

    public CropItem(Settings settings, float baseHealAmount, int baseMoneyValue) {
        this(settings, baseHealAmount, baseMoneyValue, CropQuality.NORMAL, 0.0f);
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient()) {
            user.heal(healAmount);
            if (exhaustion > 0 && user instanceof PlayerEntity player) {
                player.getHungerManager().addExhaustion(exhaustion);
            }
        }
        return super.finishUsing(stack, world, user);
    }

    public float getHealAmount() {
        return healAmount;
    }

    public int getBaseMoneyValue() {
        return baseMoneyValue;
    }

    public int getMoneyValue() {
        return moneyValue;
    }

    public CropQuality getQuality() {
        return quality;
    }

    public float getExhaustion() {
        return exhaustion;
    }
}
