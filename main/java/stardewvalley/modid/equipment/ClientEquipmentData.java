package stardewvalley.modid.equipment;

import net.minecraft.item.ItemStack;

public class ClientEquipmentData {
    private static EquipmentInventory clientEquipment = new EquipmentInventory();

    public static EquipmentInventory getEquipment() {
        return clientEquipment;
    }

    public static void updateEquipment(ItemStack[] slots) {
        EquipmentInventory inv = new EquipmentInventory();
        inv.setFromArray(slots);
        clientEquipment = inv;
    }
}
