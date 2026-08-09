package stardewvalley.modid.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import stardewvalley.modid.block.BombBlock;

@Mixin(LivingEntity.class)
public abstract class BombDamageMixin {

    @Unique
    private static final ThreadLocal<Boolean> REDUCING = ThreadLocal.withInitial(() -> false);

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!REDUCING.get() && source.isIn(DamageTypeTags.IS_EXPLOSION) && source.getSource() == null && BombBlock.hasActiveExplosion()) {
            REDUCING.set(true);
            cir.setReturnValue(((LivingEntity) (Object) this).damage(world, source, amount * 0.2F));
            REDUCING.set(false);
        }
    }
}
