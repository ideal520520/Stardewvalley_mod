package stardewvalley.modid.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.passive.HorseEntity;
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

@Mixin(Entity.class)
public class BookHorseSpeedMixin {

    @Unique
    private static final Identifier HORSE_BOOK_SPEED_ID = Identifier.of(StardewValley.MOD_ID, "horse_book_speed");

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Entity self = (Entity) (Object) this;
        if (!(self instanceof HorseEntity horse)) return;
        if (!(horse.getControllingPassenger() instanceof ServerPlayerEntity rider)) return;

        ServerWorld world = (ServerWorld) horse.getEntityWorld();
        boolean hasBook = BookDataManager.get(world).hasUsedBook(rider.getUuid(), "horse_the_book");

        EntityAttributeInstance speedAttr = horse.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        var existing = speedAttr.getModifier(HORSE_BOOK_SPEED_ID);
        if (hasBook) {
            if (existing == null) {
                speedAttr.addTemporaryModifier(new EntityAttributeModifier(
                    HORSE_BOOK_SPEED_ID, 0.125, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        } else if (existing != null) {
            speedAttr.removeModifier(existing);
        }
    }
}
