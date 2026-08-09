package stardewvalley.modid.equipment;

import net.minecraft.item.ItemStack;

public class ClientTrashCanData {
    private static ItemStack pendingItem = ItemStack.EMPTY;
    private static int trashLevel = 0;

    public static ItemStack getPendingItem() { return pendingItem; }
    public static void setPendingItem(ItemStack stack) { pendingItem = stack == null ? ItemStack.EMPTY : stack; }
    public static int getTrashLevel() { return trashLevel; }
    public static void setTrashLevel(int l) { trashLevel = l; }
    public static void clear() { pendingItem = ItemStack.EMPTY; }
}
