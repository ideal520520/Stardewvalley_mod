package stardewvalley.modid.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.gui.DwarvishSafetyManager;

@Mixin(LivingEntity.class)
public abstract class SafetyManualDamageMixin {

    @Unique
    private static final ThreadLocal<Boolean> SAFETY_MANUAL_APPLIED = ThreadLocal.withInitial(() -> false);

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void modifyExplosionDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (SAFETY_MANUAL_APPLIED.get()) return;

        if (!source.isIn(DamageTypeTags.IS_EXPLOSION)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;

        DwarvishSafetyManager manager = DwarvishSafetyManager.get(world);
        if (!manager.isActivated(player.getUuid())) return;

        SAFETY_MANUAL_APPLIED.set(true);
        cir.setReturnValue(self.damage(world, source, amount * 0.5F));
        SAFETY_MANUAL_APPLIED.set(false);
    }
}
