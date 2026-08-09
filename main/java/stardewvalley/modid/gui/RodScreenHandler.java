package stardewvalley.modid.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import stardewvalley.modid.item.ModFishingRodItem;
import stardewvalley.modid.item.RodComponent;

public class RodScreenHandler extends BasePlayerInventoryScreenHandler {
    private final SimpleInventory rodInventory;
    private ItemStack rodStack;
    private final int tackleSlotCount;
    /** Player inventory occupies slots 0-35, custom slots start here */
    private static final int PLAYER_INV_END = 36;

    public RodScreenHandler(int syncId, PlayerInventory playerInventory, ModScreenHandlers.RodOpenData data) {
        super(ModScreenHandlers.ROD, syncId, playerInventory, 1 + data.tackleSlotCount());
        this.tackleSlotCount = data.tackleSlotCount();
        this.rodInventory = new SimpleInventory(1 + tackleSlotCount);

        // Bait slot (index 0 in rodInventory)
        addSlot(new Slot(rodInventory, 0, 48, 36) {
            @Override
            public boolean canInsert(ItemStack stack) {
                String path = Registries.ITEM.getId(stack.getItem()).getPath();
                return path.startsWith("bait_");
            }

            @Override
            public int getMaxItemCount() {
                return RodComponent.MAX_BAIT_COUNT;
            }
        });

        // Tackle slots (indices 1..tackleSlotCount in rodInventory)
        for (int i = 0; i < tackleSlotCount; i++) {
            final int index = i;
            addSlot(new Slot(rodInventory, 1 + i, 48 + i * 20, 60) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    String path = Registries.ITEM.getId(stack.getItem()).getPath();
                    return path.startsWith("fishtool_");
                }

                @Override
                public int getMaxItemCount() {
                    return 1;
                }
            });
        }
    }

    /** 服务端：从钓竿 ItemStack 加载组件数据 */
    public void loadFromRod(ItemStack stack) {
        this.rodStack = stack;
        RodComponent comp = stack.get(RodComponent.TYPE);
        if (comp != null) {
            rodInventory.setStack(0, comp.getBait().copy());
            for (int i = 0; i < tackleSlotCount; i++) {
                rodInventory.setStack(1 + i, comp.getTackle(i).copy());
            }
        }
    }

    public int getTackleSlotCount() {
        return tackleSlotCount;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        // 仅在服务端写回
        if (!player.getEntityWorld().isClient() && rodStack != null) {
            RodComponent comp = rodStack.getOrDefault(RodComponent.TYPE, new RodComponent());
            comp.setBait(rodInventory.getStack(0).copy());
            for (int i = 0; i < tackleSlotCount; i++) {
                comp.setTackle(i, rodInventory.getStack(1 + i).copy());
            }
            rodStack.set(RodComponent.TYPE, comp);
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (slotIndex >= PLAYER_INV_END) {
            // Custom slot (bait/tackle) → player inventory (0..35)
            if (!insertItem(stack, 0, PLAYER_INV_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // Player inventory → custom slots
            if (!insertItem(stack, PLAYER_INV_END, slots.size(), false)) {
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
    protected int getPlayerInvY() {
        return 92;
    }
}
