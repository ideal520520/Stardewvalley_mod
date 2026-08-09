package stardewvalley.modid.equipment;

/**
 * 客户端缓存的背包等级（用于 ShopScreen 显示购买状态）
 * 实际槽位数据由 ScreenHandler 自动同步
 */
public class ClientBackpackData {
    private static int backpackLevel = 0; // 0=初始(2行), 1=backpack(3行), 2=36_backpack(4行)

    public static int getBackpackLevel() { return backpackLevel; }
    public static void setBackpackLevel(int level) { backpackLevel = level; }

    public static int getRowCount() {
        return switch (backpackLevel) {
            case 0 -> 2;
            case 1 -> 3;
            case 2 -> 4;
            default -> 2;
        };
    }

    public static int getSlotCount() {
        return getRowCount() * 9;
    }
}
