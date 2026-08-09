package stardewvalley.modid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import stardewvalley.modid.gui.DwarvishSafetyManager;

import java.util.function.Consumer;

public class DwarvishSafetyManualItem extends Item {

    public DwarvishSafetyManualItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        DwarvishSafetyManager manager = DwarvishSafetyManager.get(serverWorld);

        if (manager.isActivated(user.getUuid())) {
            user.sendMessage(Text.translatable("message.stardewvalley.already_learned"), true);
            return ActionResult.SUCCESS;
        }

        manager.activate(user.getUuid());

        if (!user.isCreative()) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> tooltipConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltipConsumer, type);
        tooltipConsumer.accept(Text.translatable("item.stardewvalley.dwarvish_safety_manual.desc").formatted(Formatting.GRAY));
    }
}
