package stardewvalley.modid.gui;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

/** 垃圾桶槽位：单格、可开关、合并上限 999 */
public class TrashSlot extends Slot {
    /** 槽位物品坐标（相对 GUI 原点） */
    public static final int SLOT_X = 156;
    public static final int SLOT_Y = 63;
    /** 垃圾桶材质左上角（外框绘制在 (-1,-1)） */
    public static final int TEX_X = SLOT_X - 1;
    public static final int TEX_Y = SLOT_Y - 1;

    private boolean enabled = false;

    public TrashSlot(Inventory inventory, int index, int x, int y) {
        super(inventory, index, x, y);
    }

    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    @Override
    public boolean isEnabled() { return enabled; }

    /** 禁止原版逻辑直接向垃圾桶槽位插入物品，全部放入操作交给 TrashCanClickHandler 处理 */
    @Override
    public boolean canInsert(ItemStack stack) { return false; }

    @Override
    public int getMaxItemCount() { return 999; }

    @Override
    public int getMaxItemCount(ItemStack stack) { return Math.min(999, stack.getMaxCount()); }
}
