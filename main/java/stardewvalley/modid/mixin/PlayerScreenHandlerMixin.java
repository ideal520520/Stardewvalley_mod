package stardewvalley.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.PlayerScreenHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.gui.PlayerScreenHandlerAccessor;
import stardewvalley.modid.gui.TrashSlot;

/** 向原版玩家背包界面注册一个真实的垃圾桶槽位，交互/同步交给原版 ScreenHandler 处理 */
@Mixin(PlayerScreenHandler.class)
public abstract class PlayerScreenHandlerMixin implements PlayerScreenHandlerAccessor {

    @Unique private SimpleInventory sv$trashInventory;
    @Unique private TrashSlot sv$trashSlot;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void sv$addTrashSlot(PlayerInventory inventory, boolean onServer, PlayerEntity owner, CallbackInfo ci) {
        this.sv$trashInventory = new SimpleInventory(1);
        this.sv$trashSlot = new TrashSlot(this.sv$trashInventory, 0, TrashSlot.SLOT_X, TrashSlot.SLOT_Y);
        ((ScreenHandlerInvoker) (Object) this).invokeAddSlot(this.sv$trashSlot);
    }

    @Override
    public SimpleInventory sv$getTrashInventory() { return this.sv$trashInventory; }

    @Override
    public TrashSlot sv$getTrashSlot() { return this.sv$trashSlot; }
}
