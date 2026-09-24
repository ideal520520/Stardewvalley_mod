package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import stardewvalley.modid.StardropData;

public class StardropItem extends Item {
    public static final int SATURATION_PER_STARDROP = 4;
    public static final int BASE_SATURATION = 20;

    public StardropItem(Settings settings) {
        super(settings);
    }

    public static int getStardropCount(PlayerEntity player) {
        return StardropData.get(player);
    }

    /** 个人饱和值上限只与吃下的星之果实数量有关 */
    public static void clampPlayerSaturation(ServerPlayerEntity player) {
        int personalMax = BASE_SATURATION + SATURATION_PER_STARDROP * getStardropCount(player);
        float currentSat = player.getHungerManager().getSaturationLevel();
        if (currentSat > personalMax) {
            player.getHungerManager().setSaturationLevel((float) personalMax);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity user) {
        if (!world.isClient() && user instanceof PlayerEntity player) {
            int count = StardropData.add(player, 1);

            int newMaxSaturation = BASE_SATURATION + SATURATION_PER_STARDROP * count;

            player.heal(1000.0f);
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel((float) newMaxSaturation);

            stack.decrement(1);
        }
        return stack;
    }
}
