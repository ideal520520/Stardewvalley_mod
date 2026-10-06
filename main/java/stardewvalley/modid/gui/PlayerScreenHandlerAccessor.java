package stardewvalley.modid.gui;

import net.minecraft.inventory.SimpleInventory;

/** 由 PlayerScreenHandlerMixin 实现，用于外部访问垃圾桶槽位与其容器 */
public interface PlayerScreenHandlerAccessor {
    SimpleInventory sv$getTrashInventory();
    TrashSlot sv$getTrashSlot();
}
