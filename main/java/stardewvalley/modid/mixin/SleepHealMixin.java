package stardewvalley.modid.mixin;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.item.StardropItem;

import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public class SleepHealMixin {

    @Inject(method = "wakeUp", at = @At("HEAD"))
    private void onWakeUp(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        UUID uuid = player.getUuid();
        int stardropCount = StardropItem.getStardropCount(uuid);
        int personalMaxSaturation = StardropItem.BASE_SATURATION + StardropItem.SATURATION_PER_STARDROP * stardropCount;

        // 食物体力值上限增益
        StatusEffectInstance maxEnergyEffect = player.getStatusEffect(ModStatusEffects.MAX_ENERGY_BUFF);
        if (maxEnergyEffect != null) {
            personalMaxSaturation += (maxEnergyEffect.getAmplifier() + 1) * 2;
        }

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
