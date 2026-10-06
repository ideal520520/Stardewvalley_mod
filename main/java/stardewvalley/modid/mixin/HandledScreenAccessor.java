package stardewvalley.modid.mixin;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** 访问 HandledScreen 的 GUI 原点坐标，用于把垃圾桶外框画在真实槽位所在位置 */
@Mixin(HandledScreen.class)
public interface HandledScreenAccessor {
    @Accessor("x")
    int sv$getX();

    @Accessor("y")
    int sv$getY();
}
