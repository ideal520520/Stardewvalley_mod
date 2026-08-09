package stardewvalley.modid.equipment;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.HashSet;
import java.util.Set;

/**
 * 管理玩家的3个额外装备栏数据。
 * 前两个装备栏只能放置戒指，第三个装备栏可放置其他物品。
 */
public class EquipmentInventory {
    public static final int SLOT_COUNT = 3;

    private final ItemStack[] slots = new ItemStack[SLOT_COUNT];

    /** 所有戒指物品的ID路径集合 */
    private static final Set<String> RING_IDS = new HashSet<>();

    static {
        String[] ringNames = {
            "amethyst_ring", "aquamarine_ring", "burglars_ring", "crabshell_ring", "emerald_ring",
            "glow_ring", "glowstone_ring", "hot_java_ring", "immunity_band", "iridium_band",
            "jade_ring", "jukebox_ring", "lucky_ring", "magnet_ring", "napalm_ring",
            "phoenix_ring", "protection_ring", "ring_of_yoba", "ruby_ring", "savage_ring",
            "slime_charmer_ring", "small_glow_ring", "small_magnet_ring", "soul_sapper_ring",
            "sturdy_ring", "thorns_ring", "topaz_ring", "vampire_ring", "warrior_ring"
        };
        for (String name : ringNames) {
            RING_IDS.add(name);
        }
    }

    public EquipmentInventory() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i] = ItemStack.EMPTY;
        }
    }

    public ItemStack getSlot(int index) {
        if (index < 0 || index >= SLOT_COUNT) return ItemStack.EMPTY;
        return slots[index];
    }

    public void setSlot(int index, ItemStack stack) {
        if (index < 0 || index >= SLOT_COUNT) return;
        slots[index] = stack == null ? ItemStack.EMPTY : stack;
    }

    public ItemStack[] copySlots() {
        ItemStack[] copy = new ItemStack[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            copy[i] = slots[i].copy();
        }
        return copy;
    }

    public void setFromArray(ItemStack[] arr) {
        for (int i = 0; i < SLOT_COUNT && i < arr.length; i++) {
            slots[i] = arr[i] == null ? ItemStack.EMPTY : arr[i];
        }
    }

    /** 判断物品是否为戒指 */
    public static boolean isRing(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!StardewValley.MOD_ID.equals(id.getNamespace())) return false;
        return RING_IDS.contains(id.getPath());
    }

    /** 检查指定槽位是否能放入该物品 */
    public static boolean canPlaceInSlot(int slotIndex, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (slotIndex == 0 || slotIndex == 1) {
            // 前两个装备栏只能放戒指
            return isRing(stack);
        }
        // 第三个装备栏只能放鞋子
        if (slotIndex == 2) {
            return stack.getItem() instanceof stardewvalley.modid.item.ModShoesItem;
        }
        return false;
    }
}
