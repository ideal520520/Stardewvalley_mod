package stardewvalley.modid.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import org.jetbrains.annotations.Nullable;

/**
 * 共享基类：管理玩家包里 36 个槽位的 ScreenHandler
 * 工具/商店子类只需添加自己的特殊槽位，玩家背包交互自动支持原版所有操作
 */
public abstract class BasePlayerInventoryScreenHandler extends ScreenHandler {

    protected static final int SLOT_SIZE = 18;
    /** 本 handler 中玩家背包第一个槽位的索引（自定义槽位之后） */
    protected final int playerInvStartIndex;
    /** 自定义槽位数量 */
    protected final int customSlotCount;

    protected BasePlayerInventoryScreenHandler(@Nullable ScreenHandlerType<?> type, int syncId,
                                                PlayerInventory playerInventory, int customSlotCount) {
        super(type, syncId);
        this.customSlotCount = customSlotCount;
        this.playerInvStartIndex = customSlotCount;

        // 玩家主背包 (27格)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * SLOT_SIZE, getPlayerInvY() + row * SLOT_SIZE));
            }
        }
        // 玩家快捷栏 (9格)
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * SLOT_SIZE, getPlayerInvY() + 3 * SLOT_SIZE + 4));
        }
    }

    /** 子类重写：玩家背包的起始 Y 坐标 */
    protected int getPlayerInvY() {
        return 84;
    }

    public int getCustomSlotCount() {
        return customSlotCount;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (slotIndex < playerInvStartIndex) {
            // 自定义槽位 → 玩家背包
            if (!insertItem(stack, playerInvStartIndex, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // 玩家背包 → 自定义槽位
            if (!insertItem(stack, 0, playerInvStartIndex, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setStack(ItemStack.EMPTY);
        } else {
            slot.markDirty();
        }
        return original;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
