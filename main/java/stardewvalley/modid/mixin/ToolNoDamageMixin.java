package stardewvalley.modid.mixin;

import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ToolNoDamageMixin {

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void preventToolDamage(int amount, ServerWorld world, ServerPlayerEntity player, Consumer<ItemStack> breakCallback, CallbackInfo ci) {
        ItemStack stack = (ItemStack) (Object) this;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (id.getNamespace().equals(StardewValley.MOD_ID)) {
            String path = id.getPath();
            if (path.endsWith("pickaxe") || path.endsWith("axe") || path.contains("_pickaxe") || path.contains("_axe")) {
                ci.cancel();
            }
        }
    }
}
