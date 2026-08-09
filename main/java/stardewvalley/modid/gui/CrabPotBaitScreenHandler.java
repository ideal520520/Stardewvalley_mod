package stardewvalley.modid.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.block.CrabPotBlockEntity;

public class CrabPotBaitScreenHandler extends BasePlayerInventoryScreenHandler {

    private final SimpleInventory baitInventory;
    private final BlockPos pos;

    private static final int PLAYER_INV_END = 36;

    public CrabPotBaitScreenHandler(int syncId, PlayerInventory playerInventory, BlockPos pos) {
        super(ModScreenHandlers.CRAB_POT, syncId, playerInventory, 1);
        this.pos = pos;
        this.baitInventory = new SimpleInventory(1);

        // Bait slot (single slot at index 0)
        addSlot(new Slot(baitInventory, 0, 48, 36) {
            @Override
            public boolean canInsert(ItemStack stack) {
                String path = Registries.ITEM.getId(stack.getItem()).getPath();
                return path.startsWith("bait_");
            }

            @Override
            public int getMaxItemCount() {
                return 999;
            }
        });
    }

    /** 服务端：从 BlockEntity 加载鱼饵数据 */
    public void loadFromBlockEntity(CrabPotBlockEntity be) {
        String baitItemId = be.getBaitItemId();
        int baitCount = be.getBaitCount();
        if (!baitItemId.isEmpty() && baitCount > 0) {
            ItemStack baitStack = new ItemStack(Registries.ITEM.get(
                net.minecraft.util.Identifier.of(baitItemId)), baitCount);
            baitInventory.setStack(0, baitStack);
        }
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (!player.getEntityWorld().isClient()) {
            World world = player.getEntityWorld();
            if (world.getBlockEntity(pos) instanceof CrabPotBlockEntity be) {
                ItemStack baitStack = baitInventory.getStack(0);
                if (baitStack.isEmpty()) {
                    be.setBaitItemId("");
                    be.setBaitCount(0);
                } else {
                    be.setBaitItemId(Registries.ITEM.getId(baitStack.getItem()).toString());
                    be.setBaitCount(baitStack.getCount());
                }
            }
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (slotIndex >= PLAYER_INV_END) {
            // Custom slot (bait) → player inventory (0..35)
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

    public BlockPos getPos() {
        return pos;
    }
}
