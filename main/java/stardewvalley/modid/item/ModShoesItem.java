package stardewvalley.modid.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

public class ModShoesItem extends Item {

    private final int defense;
    private final int immunity;

    public ModShoesItem(Settings settings, int defense, int immunity) {
        super(settings);
        this.defense = defense;
        this.immunity = immunity;
    }

    public int getDefense() {
        return defense;
    }

    public int getImmunity() {
        return immunity;
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> tooltipConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltipConsumer, type);
        if (defense > 0) {
            tooltipConsumer.accept(Text.literal("防御: +" + defense).formatted(Formatting.AQUA));
        }
        if (immunity > 0) {
            tooltipConsumer.accept(Text.literal("免疫: +" + immunity).formatted(Formatting.WHITE));
        }
    }
}
