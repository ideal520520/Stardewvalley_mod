package stardewvalley.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingBobberEntity.class)
public class FishingBobberMixin {

    @Inject(method = "removeIfInvalid", at = @At("HEAD"), cancellable = true)
    private void onRemoveIfInvalid(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player.isAlive()) {
            ItemStack mainHand = player.getMainHandStack();
            ItemStack offHand = player.getOffHandStack();

            boolean mainIsRod = mainHand.getItem() instanceof FishingRodItem;
            boolean offIsRod = offHand.getItem() instanceof FishingRodItem;

            if ((mainIsRod || offIsRod)
                && ((Entity)(Object)this).squaredDistanceTo(player) <= 1024.0) {
                cir.setReturnValue(false);
            }
        }
    }
}
