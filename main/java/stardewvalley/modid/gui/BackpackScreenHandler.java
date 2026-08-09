package stardewvalley.modid.gui;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import stardewvalley.modid.equipment.BackpackStateManager;

import java.util.UUID;

public class BackpackScreenHandler extends ScreenHandler {

    private final SimpleInventory backpackInventory;
    private final int backpackLevel;
    private final int backpackSlotCount;

    private BackpackStateManager manager;
    private UUID playerUuid;

    // 布局常量
    private static final int SLOT_SIZE = 18;
    private static final int BACKPACK_START_Y = 22;
    private static final int BACKPACK_TO_INVENTORY_GAP = 24;

    public BackpackScreenHandler(int syncId, PlayerInventory playerInventory, int backpackLevel) {
        super(ModScreenHandlers.BACKPACK, syncId);
        this.backpackLevel = backpackLevel;
        this.backpackSlotCount = getSlotCount(backpackLevel);
        this.backpackInventory = new SimpleInventory(backpackSlotCount);

        int bpRows = (int) Math.ceil((double) backpackSlotCount / 9);

        // 扩展背包槽位
        for (int i = 0; i < backpackSlotCount; i++) {
            int row = i / 9;
            int col = i % 9;
            addSlot(new Slot(backpackInventory, i, 8 + col * SLOT_SIZE, BACKPACK_START_Y + row * SLOT_SIZE));
        }

        // 玩家背包标题行的 Y
        int inventoryTitleY = BACKPACK_START_Y + bpRows * SLOT_SIZE + BACKPACK_TO_INVENTORY_GAP;
        // 玩家主背包 (27格)：标题下方 12px
        int invStartY = inventoryTitleY + 12;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * SLOT_SIZE, invStartY + row * SLOT_SIZE));
            }
        }

        // 玩家快捷栏 (9格)：主背包下方 4px 间隙
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * SLOT_SIZE, invStartY + 3 * SLOT_SIZE + 4));
        }
    }

    public void loadFromManager(BackpackStateManager mgr, ServerPlayerEntity player) {
        this.manager = mgr;
        this.playerUuid = player.getUuid();
        for (int i = 0; i < backpackSlotCount; i++) {
            backpackInventory.setStack(i, mgr.getPlayerSlot(playerUuid, i));
        }
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        Slot slot = slots.get(slotIndex);
        if (!slot.hasStack()) return ItemStack.EMPTY;

        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();

        if (slotIndex < backpackSlotCount) {
            if (!insertItem(stack, backpackSlotCount, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (!insertItem(stack, 0, backpackSlotCount, false)) {
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
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        if (manager != null && playerUuid != null) {
            for (int i = 0; i < backpackSlotCount; i++) {
                manager.setPlayerSlot(playerUuid, i, backpackInventory.getStack(i));
            }
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    public int getBackpackLevel() { return backpackLevel; }
    public int getBackpackSlotCount() { return backpackSlotCount; }

    public static int getSlotCount(int level) {
        return switch (level) {
            case 0 -> 18;
            case 1 -> 27;
            case 2 -> 36;
            default -> 18;
        };
    }

    /**
     * 计算 GUI 总高度
     */
    public static int getGuiHeight(int level) {
        int bpRows = (int) Math.ceil((double) getSlotCount(level) / 9);
        int backpackArea = BACKPACK_START_Y + bpRows * SLOT_SIZE;
        int playerInvArea = 12 + 3 * SLOT_SIZE + 4 + SLOT_SIZE; // 标题+3行+间隙+1行快捷栏
        return backpackArea + BACKPACK_TO_INVENTORY_GAP + playerInvArea + 8; // 底部边距
    }

    /**
     * 玩家背包标题的 Y 坐标
     */
    public static int getInventoryTitleY(int level) {
        int bpRows = (int) Math.ceil((double) getSlotCount(level) / 9);
        return BACKPACK_START_Y + bpRows * SLOT_SIZE + BACKPACK_TO_INVENTORY_GAP;
    }

    /**
     * 返回按钮的 Y 坐标（背包格子下方 2px，与"背包"标题同排）
     */
    public static int getBackButtonY(int level) {
        int bpRows = (int) Math.ceil((double) getSlotCount(level) / 9);
        return BACKPACK_START_Y + bpRows * SLOT_SIZE + 2;
    }
}
