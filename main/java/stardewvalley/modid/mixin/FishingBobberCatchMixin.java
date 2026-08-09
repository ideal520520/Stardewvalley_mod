package stardewvalley.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.fishing.FishingModeManager;
import stardewvalley.modid.season.FishingLootManager;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberCatchMixin {

    @Shadow
    private boolean caughtFish;

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    private void onUse(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        FishingBobberEntity self = (FishingBobberEntity) (Object) this;

        Entity ownerEntity = self.getOwner();
        if (!(ownerEntity instanceof PlayerEntity player)) return;
        if (player.getEntityWorld().isClient()) return;

        // 模式1（原始模式）才生效
        if (FishingModeManager.getMode() != 1) return;

        boolean holdingSvRod = FishingLootManager.isSvRod(player.getMainHandStack())
            || FishingLootManager.isSvRod(player.getOffHandStack());

        if (!holdingSvRod) return;
        if (!caughtFish) return;

        ServerWorld serverWorld = (ServerWorld) player.getEntityWorld();
        BlockPos pos = self.getBlockPos();
        FishingLootManager.handleCatch(serverWorld, player, pos);

        serverWorld.playSound(null, pos,
            SoundEvents.ENTITY_FISHING_BOBBER_SPLASH,
            SoundCategory.NEUTRAL, 0.5f, 0.4f);

        self.discard();
        cir.setReturnValue(0);
    }
}
