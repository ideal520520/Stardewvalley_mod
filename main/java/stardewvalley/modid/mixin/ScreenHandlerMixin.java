package stardewvalley.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.gui.TrashCanClickHandler;

/** 劫持垃圾桶槽位的点击处理，实现"销毁旧物品回收金币"的自定义逻辑 */
@Mixin(ScreenHandler.class)
public abstract class ScreenHandlerMixin {

    @Inject(method = "onSlotClick", at = @At("HEAD"), cancellable = true)
    private void sv$onSlotClick(int slotIndex, int button, SlotActionType actionType, PlayerEntity player, CallbackInfo ci) {
        if (TrashCanClickHandler.handle((ScreenHandler) (Object) this, slotIndex, button, actionType, player)) {
            ci.cancel();
        }
    }
}
