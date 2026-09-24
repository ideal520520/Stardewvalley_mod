package stardewvalley.modid.mixin;

import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.item.StardropItem;

@Mixin(ServerPlayerEntity.class)
public class SleepHealMixin {

    @Inject(method = "wakeUp", at = @At("HEAD"))
    private void onWakeUp(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        int stardropCount = StardropItem.getStardropCount(player);
        int personalMaxSaturation = StardropItem.BASE_SATURATION + StardropItem.SATURATION_PER_STARDROP * stardropCount;

        // 精疲力尽状态下只恢复一半饱食度并移除debuff
        boolean exhausted = player.hasStatusEffect(ModStatusEffects.EXHAUSTED);
        if (exhausted) {
            personalMaxSaturation = Math.max(1, personalMaxSaturation / 2);
            player.removeStatusEffect(ModStatusEffects.EXHAUSTED);
        }

        player.heal(player.getMaxHealth());
        player.getHungerManager().setFoodLevel(20);
        player.getHungerManager().setSaturationLevel((float) personalMaxSaturation);
    }
}
