package stardewvalley.modid.item;

import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

public class ModToolMaterials {

    private static final TagKey<Item> ITEM_NOTHING = TagKey.of(RegistryKeys.ITEM, Identifier.of(StardewValley.MOD_ID, "nothing"));

    // BASIC → 石制 (mining speed 4.0, cannot mine iron+ correctly)
    public static final ToolMaterial BASIC = new ToolMaterial(
        BlockTags.INCORRECT_FOR_STONE_TOOL,
        Integer.MAX_VALUE,
        4.0F,
        0F,
        15,
        ITEM_NOTHING
    );

    // COPPER → 铜制 (mining speed 5.0, cannot mine iron+ correctly)
    public static final ToolMaterial COPPER = new ToolMaterial(
        BlockTags.INCORRECT_FOR_COPPER_TOOL,
        Integer.MAX_VALUE,
        5.0F,
        0F,
        15,
        ITEM_NOTHING
    );

    // STEEL → 铁制 (mining speed 6.0, cannot mine diamond+ correctly)
    public static final ToolMaterial STEEL = new ToolMaterial(
        BlockTags.INCORRECT_FOR_IRON_TOOL,
        Integer.MAX_VALUE,
        6.0F,
        0F,
        15,
        ITEM_NOTHING
    );

    // GOLD → 钻石制 (mining speed 8.0, cannot mine netherite+ correctly)
    public static final ToolMaterial GOLD = new ToolMaterial(
        BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
        Integer.MAX_VALUE,
        8.0F,
        0F,
        15,
        ITEM_NOTHING
    );

    // IRIDIUM → 下界合金制 (mining speed 9.0, can mine everything correctly)
    public static final ToolMaterial IRIDIUM = new ToolMaterial(
        BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
        Integer.MAX_VALUE,
        10.0F,
        0F,
        15,
        ITEM_NOTHING
    );
}
