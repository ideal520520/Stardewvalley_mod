package stardewvalley.modid.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.registry.Registries;
import stardewvalley.modid.item.ModSlingshotItem;

public class SlingshotScreenHandler extends BasePlayerInventoryScreenHandler {
    private final SimpleInventory ammoInventory = new SimpleInventory(1);
    /** 服务端持有的弹弓 ItemStack 引用，关闭时写回 */
    private ItemStack slingshotStack;

    public SlingshotScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(ModScreenHandlers.SLINGSHOT, syncId, playerInventory, 1);
        // 弹药槽 (index 0)
        addSlot(new Slot(ammoInventory, 0, 48, 18) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return isAmmoItem(stack);
            }
            @Override
            public int getMaxItemCount(ItemStack stack) {
                return 999;
            }
        });
    }

    /** 服务端：从弹弓 ItemStack 加载弹药数据 */
    public void loadFromSlingshot(ItemStack stack) {
        this.slingshotStack = stack;
        ItemStack ammo = ModSlingshotItem.getAmmo(stack);
        ammoInventory.setStack(0, ammo);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        // 仅在服务端写回
        if (!player.getEntityWorld().isClient() && slingshotStack != null) {
            ModSlingshotItem.setAmmo(slingshotStack, ammoInventory.getStack(0));
        }
    }

    public static boolean isAmmoItem(ItemStack stack) {
        String id = Registries.ITEM.getId(stack.getItem()).getPath();
        return id.equals("coal") || id.equals("copper_ore") || id.equals("explosive_ammo") ||
               id.equals("gold_ore") || id.equals("iridium_ore") || id.equals("iron_ore") ||
               id.equals("misc_stone") || id.equals("wood");
    }

    @Override
    protected int getPlayerInvY() {
        return 56;
    }
}
