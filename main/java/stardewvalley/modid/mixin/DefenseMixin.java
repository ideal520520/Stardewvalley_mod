package stardewvalley.modid.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.effect.ModStatusEffects;
import stardewvalley.modid.equipment.EquipmentInventory;
import stardewvalley.modid.equipment.EquipmentStateManager;
import stardewvalley.modid.equipment.RingEffectHandler;
import stardewvalley.modid.gui.BookDataManager;
import stardewvalley.modid.item.ModShoesItem;
import stardewvalley.modid.item.ModWeaponItem;

@Mixin(LivingEntity.class)
public class DefenseMixin {

    @ModifyVariable(method = "damage", at = @At("HEAD"), argsOnly = true)
    private float applyDamageReduction(float amount) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!(entity instanceof ServerPlayerEntity player)) return amount;
        if (amount <= 0) return amount;

        // 由巴祝福：100%免伤
        if (player.hasStatusEffect(ModStatusEffects.YOBA_BLESSING)) {
            return 0.0f;
        }

        // 保护戒指免伤：100%免伤
        if (RingEffectHandler.hasProtectionImmunity(player.getUuid())) {
            return 0.0f;
        }

        float totalDefense = 0;
        var ringDef = player.getStatusEffect(ModStatusEffects.RING_DEFENSE_BUFF);
        if (ringDef != null) {
            totalDefense += ringDef.getAmplifier() + 1;
        }

        float immunity = 0;
        // 只读取免疫值（防御已由 RING_DEFENSE_BUFF 总等级涵盖）
        EquipmentInventory inv = EquipmentStateManager.get((ServerWorld) player.getEntityWorld()).getPlayerData(player.getUuid());
        for (int i = 0; i < 2; i++) {
            ItemStack s = inv.getSlot(i);
            if (s.isEmpty()) continue;
            Identifier id = Registries.ITEM.getId(s.getItem());
            if (!StardewValley.MOD_ID.equals(id.getNamespace())) continue;
            if ("immunity_band".equals(id.getPath())) immunity += 4;
        }

        // 鞋子免疫
        ItemStack shoeStack = inv.getSlot(2);
        if (shoeStack.getItem() instanceof ModShoesItem shoes) {
            immunity += shoes.getImmunity();
        }

        boolean hasCopperWall = BookDataManager.get((ServerWorld) player.getEntityWorld())
            .hasUsedBook(player.getUuid(), "jack_be_nimble_jack_be_thick");
        float reductionPerDefense = hasCopperWall ? 2.0f : 2.5f;
        if (totalDefense > 0) amount = Math.max(1.0f, amount - totalDefense * reductionPerDefense);
        if (immunity > 0) amount *= (1.0f - immunity * 0.05f);
        return amount;
    }
}
