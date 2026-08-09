package stardewvalley.modid.item;

import net.minecraft.item.Item;

public class FishingTackleItem extends Item {

    public static final int MAX_DURABILITY = 20;

    public FishingTackleItem(Settings settings) {
        super(settings.maxDamage(MAX_DURABILITY));
    }
}
