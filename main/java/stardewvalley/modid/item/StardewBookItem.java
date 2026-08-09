package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.gui.ModPayloads;

import java.util.function.Consumer;

public abstract class StardewBookItem extends Item {

    private final String bookId;
    private final boolean reusable;

    public StardewBookItem(Settings settings, String bookId, boolean reusable) {
        super(settings);
        this.bookId = bookId;
        this.reusable = reusable;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        BookDataManager manager = BookDataManager.get(serverWorld);

        if (!reusable && manager.hasUsedBook(user.getUuid(), bookId)) {
            user.sendMessage(Text.translatable("message.stardewvalley.already_learned"), true);
            return ActionResult.SUCCESS;
        }

        applyEffect(serverWorld, user);

        if (!reusable) {
            manager.markBookUsed(user.getUuid(), bookId);
        }

        if (!user.isCreative()) {
            stack.decrement(1);
        }

        // 使用后立即同步客户端状态（不需要重进游戏）
        if (user instanceof ServerPlayerEntity serverPlayer) {
            syncBookStatus(serverPlayer, serverWorld);
        }

        return ActionResult.SUCCESS;
    }

    /** 同步需要客户端感知的书籍状态 */
    private void syncBookStatus(ServerPlayerEntity player, ServerWorld world) {
        BookDataManager mgr = BookDataManager.get(world);
        switch (bookId) {
            case "price_catalogue" -> {
                boolean active = mgr.hasUsedBook(player.getUuid(), bookId);
                ServerPlayNetworking.send(player, new ModPayloads.PriceCatalogueSyncS2CPayload(active));
            }
            case "animal_catalogue" -> {
                boolean active = mgr.hasUsedBook(player.getUuid(), bookId);
                ServerPlayNetworking.send(player, new ModPayloads.AnimalCatalogueDiscountS2CPayload(active));
            }
            case "treasure_appraisal_guide" -> {
                boolean active = mgr.hasUsedBook(player.getUuid(), bookId);
                ServerPlayNetworking.send(player, new ModPayloads.TreasureAppraisalSyncS2CPayload(active));
            }
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, net.minecraft.component.type.TooltipDisplayComponent displayComponent, Consumer<Text> tooltipConsumer, TooltipType type) {
        super.appendTooltip(stack, context, displayComponent, tooltipConsumer, type);
        Text desc = Text.translatable("item.stardewvalley." + bookId + ".desc");
        String raw = desc.getString();
        if (raw != null && !raw.isEmpty() && !raw.equals("item.stardewvalley." + bookId + ".desc")) {
            tooltipConsumer.accept(desc.copy().formatted(Formatting.GRAY));
        }
    }

    protected abstract void applyEffect(ServerWorld world, PlayerEntity player);

    public String getBookId() {
        return bookId;
    }
}
