package stardewvalley.modid.mixin;

import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** 暴露 ScreenHandler 的 protected addSlot，供垃圾桶槽位注册使用 */
@Mixin(ScreenHandler.class)
public interface ScreenHandlerInvoker {
    @Invoker("addSlot")
    Slot invokeAddSlot(Slot slot);
}
