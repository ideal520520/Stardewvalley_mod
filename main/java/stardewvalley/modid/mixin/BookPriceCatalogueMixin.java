package stardewvalley.modid.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.gui.ClockHudRenderer;
import stardewvalley.modid.gui.GoldManager;

import java.util.List;

@Mixin(ItemStack.class)
public class BookPriceCatalogueMixin {

    @Inject(method = "getTooltip", at = @At("RETURN"))
    private void addPriceTooltip(Item.TooltipContext context, PlayerEntity player, TooltipType tooltipType, CallbackInfoReturnable<List<Text>> cir) {
        if (!ClockHudRenderer.isPriceCatalogueActive()) return;

        List<Text> tooltip = cir.getReturnValue();
        if (tooltip == null) return;

        ItemStack self = (ItemStack) (Object) this;
        if (self.isEmpty()) return;

        Identifier id = Registries.ITEM.getId(self.getItem());
        if (!StardewValley.MOD_ID.equals(id.getNamespace())) return;

        int price = GoldManager.getPublicItemPrice(id);
        if (price > 0) {
            // 古代珍宝鉴定指南：古物售价4倍（如果客户端有该buff的话）
            if (GoldManager.isArtifactItem(id) && ClockHudRenderer.hasArtifactMultiplier()) {
                price *= 4;
            }
            tooltip.add(Text.literal("售价: " + price + "金").formatted(Formatting.GOLD));
        } else {
            tooltip.add(Text.literal("不可售卖").formatted(Formatting.DARK_GRAY));
        }
    }
}
