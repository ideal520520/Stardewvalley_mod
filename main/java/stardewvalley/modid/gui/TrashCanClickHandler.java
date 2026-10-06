package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.equipment.TrashCanStateManager;

/**
 * 垃圾桶槽位交互：把垃圾桶当成一个真实槽位处理，但又保留"已有物品时放入新物品即销毁旧物品回收金币"的原有逻辑。
 * 统一处理鼠标左键(PICKUP)、Shift 快速移动(QUICK_MOVE)、数字键交换(SWAP)。
 */
public final class TrashCanClickHandler {
    private TrashCanClickHandler() {}

    /** @return true 表示已由垃圾桶逻辑处理（调用方应取消原版槽位点击） */
    public static boolean handle(ScreenHandler handler, int slotIndex, int button, SlotActionType actionType, PlayerEntity player) {
        if (slotIndex < 0 || slotIndex >= handler.slots.size()) return false;
        Slot slot = handler.slots.get(slotIndex);
        if (!(slot instanceof TrashSlot)) return false;
        if (!slot.isEnabled()) return true;

        switch (actionType) {
            case PICKUP -> pickup(handler, slot, button, player);
            case SWAP -> swap(slot, button, player);
            case QUICK_MOVE -> quickMove(handler, slot, player);
            default -> { return false; }
        }

        slot.markDirty();
        if (!player.getEntityWorld().isClient()) handler.sendContentUpdates();
        return true;
    }

    private static void pickup(ScreenHandler handler, Slot slot, int button, PlayerEntity player) {
        ItemStack trash = slot.getStack();
        ItemStack cursor = handler.getCursorStack();
        if (cursor.isEmpty()) {
            if (trash.isEmpty()) return;
            if (button == 1) {
                int half = (trash.getCount() + 1) / 2;
                handler.setCursorStack(trash.copyWithCount(half));
                int remain = trash.getCount() - half;
                slot.setStack(remain > 0 ? trash.copyWithCount(remain) : ItemStack.EMPTY);
            } else {
                handler.setCursorStack(trash.copy());
                slot.setStack(ItemStack.EMPTY);
            }
        } else {
            handler.setCursorStack(placeIntoTrash(slot, trash, cursor, button == 0, player));
        }
    }

    private static void swap(Slot slot, int button, PlayerEntity player) {
        if (button < 0 || button > 8) return;
        PlayerInventory inv = player.getInventory();
        ItemStack hotbar = inv.getStack(button);
        ItemStack trash = slot.getStack();
        if (hotbar.isEmpty()) {
            if (trash.isEmpty()) return;
            inv.setStack(button, trash.copy());
            slot.setStack(ItemStack.EMPTY);
        } else {
            inv.setStack(button, placeIntoTrash(slot, trash, hotbar, true, player));
        }
        inv.markDirty();
    }

    private static void quickMove(ScreenHandler handler, Slot slot, PlayerEntity player) {
        ItemStack trash = slot.getStack();
        if (trash.isEmpty()) return;
        moveToPlayerInventory(handler, trash);
        if (!trash.isEmpty() && !player.getEntityWorld().isClient()) {
            player.dropItem(trash, false);
        }
        slot.setStack(ItemStack.EMPTY);
    }

    /**
     * 把 incoming 放入垃圾桶；返回留在来源处（光标/热键槽）的剩余物品。
     * 桶内已存在不同物品时，先销毁旧物品并回收金币。
     */
    private static ItemStack placeIntoTrash(Slot slot, ItemStack trash, ItemStack incoming, boolean whole, PlayerEntity player) {
        int want = whole ? incoming.getCount() : 1;
        if (!trash.isEmpty() && !ItemStack.areItemsAndComponentsEqual(trash, incoming)) {
            recycle(trash, player);
            trash = ItemStack.EMPTY;
        }
        if (trash.isEmpty()) {
            int place = Math.min(want, slot.getMaxItemCount(incoming));
            slot.setStack(incoming.copyWithCount(place));
            int remain = incoming.getCount() - place;
            return remain > 0 ? incoming.copyWithCount(remain) : ItemStack.EMPTY;
        }
        // 同种物品 → 合并
        int space = Math.max(0, slot.getMaxItemCount(incoming) - trash.getCount());
        int move = Math.min(space, want);
        if (move > 0) slot.setStack(trash.copyWithCount(trash.getCount() + move));
        int remain = incoming.getCount() - move;
        return remain > 0 ? incoming.copyWithCount(remain) : ItemStack.EMPTY;
    }

    /** 将物品并入玩家背包（主背包 9..35 + 快捷栏 36..44 + 副手 45） */
    private static void moveToPlayerInventory(ScreenHandler handler, ItemStack stack) {
        for (int i = 9; i < 45 && !stack.isEmpty(); i++) {
            Slot s = handler.slots.get(i);
            ItemStack ex = s.getStack();
            if (!ex.isEmpty() && ItemStack.areItemsAndComponentsEqual(ex, stack)) {
                int space = s.getMaxItemCount(stack) - ex.getCount();
                if (space > 0) {
                    int move = Math.min(space, stack.getCount());
                    s.setStack(ex.copyWithCount(ex.getCount() + move));
                    stack.decrement(move);
                    s.markDirty();
                }
            }
        }
        for (int i = 9; i < 45 && !stack.isEmpty(); i++) {
            Slot s = handler.slots.get(i);
            if (s.getStack().isEmpty() && s.canInsert(stack)) {
                ItemStack placed = stack.copy();
                stack.setCount(0);
                s.setStack(placed);
                s.markDirty();
            }
        }
    }

    /** 服务端：销毁物品并按垃圾桶等级回收金币 */
    private static void recycle(ItemStack stack, PlayerEntity player) {
        if (stack.isEmpty() || player.getEntityWorld().isClient()) return;
        if (!(player instanceof ServerPlayerEntity sp)) return;
        ServerWorld world = (ServerWorld) sp.getEntityWorld();
        TrashCanStateManager state = TrashCanStateManager.get(world);
        double rate = TrashCanStateManager.getRecycleRate(state.getLevel());
        if (rate <= 0) return;
        int value = GoldManager.getItemMoneyValue(stack);
        if (value <= 0) return;
        int earned = (int) Math.floor(value * stack.getCount() * rate);
        if (earned <= 0) return;
        GoldManager gm = GoldManager.get(world);
        gm.addGold(earned);
        ServerPlayNetworking.send(sp, new ModPayloads.GoldSyncS2CPayload(gm.getGold()));
    }
}
