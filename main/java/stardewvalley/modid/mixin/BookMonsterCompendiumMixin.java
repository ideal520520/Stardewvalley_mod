package stardewvalley.modid.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.item.ModItems;
import stardewvalley.modid.skill.SkillEffectHelper;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Mixin(LivingEntity.class)
public class BookMonsterCompendiumMixin {

    @Unique
    private static final AtomicBoolean compendiumProcessing = new AtomicBoolean(false);

    @Unique
    private static Set<EntityType<?>> compendiumGetTargetMobs() {
        return Set.of(
            EntityType.BLAZE,
            EntityType.BOGGED,
            EntityType.BREEZE,
            EntityType.CREEPER,
            EntityType.ELDER_GUARDIAN,
            EntityType.ENDERMITE,
            EntityType.EVOKER,
            EntityType.GHAST,
            EntityType.GUARDIAN,
            EntityType.HOGLIN,
            EntityType.HUSK,
            EntityType.MAGMA_CUBE,
            EntityType.PARCHED,
            EntityType.PHANTOM,
            EntityType.PIGLIN_BRUTE,
            EntityType.PILLAGER,
            EntityType.RAVAGER,
            EntityType.SHULKER,
            EntityType.SILVERFISH,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.VEX,
            EntityType.VINDICATOR,
            EntityType.WARDEN,
            EntityType.WITCH,
            EntityType.WITHER_SKELETON,
            EntityType.ZOGLIN,
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.CAVE_SPIDER,
            EntityType.DROWNED,
            EntityType.ENDERMAN,
            EntityType.PIGLIN,
            EntityType.SPIDER,
            EntityType.ZOMBIFIED_PIGLIN
        );
    }

    @Inject(method = "dropLoot", at = @At("TAIL"))
    private void onDropLoot(ServerWorld world, DamageSource source, boolean causedByPlayer, CallbackInfo ci) {
        if (compendiumProcessing.get()) return;
        if (!causedByPlayer) return;
        if (!(source.getAttacker() instanceof ServerPlayerEntity player)) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (!compendiumGetTargetMobs().contains(self.getType())) return;

        if (!BookDataManager.get(world).hasUsedBook(player.getUuid(), "monster_compendium")) return;

        float luckMultiplier = StardewValley.getFinalLuckMultiplier(world, player);
        if (self.getRandom().nextFloat() >= 0.1f * luckMultiplier) return;

        compendiumProcessing.set(true);
        try {
            var method = LivingEntity.class.getDeclaredMethod("dropLoot", ServerWorld.class, DamageSource.class, boolean.class);
            method.setAccessible(true);
            method.invoke(self, world, source, causedByPlayer);
        } catch (Exception e) {
            // fallback
        }
        compendiumProcessing.set(false);
    }
}
