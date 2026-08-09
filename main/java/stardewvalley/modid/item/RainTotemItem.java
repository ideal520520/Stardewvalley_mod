package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.gui.ModPayloads;
import stardewvalley.modid.weather.WeatherState;
import stardewvalley.modid.weather.WeatherType;

public class RainTotemItem extends Item {

    private static final int COOLDOWN = 600;

    public RainTotemItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }

        if (world.isClient()) return ActionResult.SUCCESS;

        ServerWorld serverWorld = (ServerWorld) world;
        WeatherState weatherState = WeatherState.get(serverWorld);

        weatherState.tomorrowWeather = WeatherType.RAIN;
        weatherState.rainTotemOverride = true;
        weatherState.rainTotemDay = serverWorld.getTimeOfDay() / 24000L;
        weatherState.setDirty(true);

        if (!user.isCreative()) stack.decrement(1);

        // 打雷声
        serverWorld.playSound(null, user.getX(), user.getY(), user.getZ(),
            SoundEvents.ENTITY_LIGHTNING_BOLT_THUNDER, SoundCategory.WEATHER, 1.0f, 1.0f);

        if (user instanceof ServerPlayerEntity serverPlayer) {
            String itemId = StardewValley.MOD_ID + ":rain_totem";
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.FloatingItemS2CPayload(itemId));
        }

        user.getItemCooldownManager().set(stack, COOLDOWN);

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }
}
