package stardewvalley.modid.equipment;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class BurglarRingLootHandler {

    private static final AtomicBoolean processing = new AtomicBoolean(false);

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register((RegistryEntry<LootTable> entry, LootContext context, List<ItemStack> drops) -> {
            if (processing.get()) return;

            // 只处理实体战利品表
            if (!context.hasParameter(LootContextParameters.THIS_ENTITY)) return;
            Object entity = context.get(LootContextParameters.THIS_ENTITY);
            if (!(entity instanceof LivingEntity)) return;

            // 获取击杀玩家（LAST_DAMAGE_PLAYER 即最后攻击的玩家）
            if (!context.hasParameter(LootContextParameters.LAST_DAMAGE_PLAYER)) return;
            Object damager = context.get(LootContextParameters.LAST_DAMAGE_PLAYER);
            if (!(damager instanceof ServerPlayerEntity player)) return;

            ServerWorld world = context.getWorld();

            // 统计窃贼戒指数量
            EquipmentInventory inv = EquipmentStateManager.get(world).getPlayerData(player.getUuid());
            int burglarsCount = 0;
            for (int i = 0; i < 2; i++) {
                ItemStack s = inv.getSlot(i);
                if (s.isEmpty()) continue;
                Identifier id = Registries.ITEM.getId(s.getItem());
                if (StardewValley.MOD_ID.equals(id.getNamespace()) && "burglars_ring".equals(id.getPath())) {
                    burglarsCount++;
                }
            }
            if (burglarsCount <= 0) return;

            processing.set(true);
            try {
                for (int i = 0; i < burglarsCount; i++) {
                    entry.value().generateLoot(context, drops::add);
                }
            } finally {
                processing.set(false);
            }
        });
    }
}
