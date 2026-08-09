package stardewvalley.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.item.ModWeaponItem;

@Mixin(PlayerEntity.class)
public class PlayerAttackMixin {

    @Inject(method = "isCriticalHit", at = @At("HEAD"), cancellable = true)
    private void disableJumpCritForModWeapons(Entity target, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        ItemStack mainHand = player.getMainHandStack();
        if (mainHand.getItem() instanceof ModWeaponItem) {
            cir.setReturnValue(false);
        }
    }
}
