package stardewvalley.modid.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

public class ModEntities {
    private static final Identifier SLINGSHOT_PROJECTILE_ID = Identifier.of(StardewValley.MOD_ID, "slingshot_projectile");

    public static final EntityType<SlingshotProjectileEntity> SLINGSHOT_PROJECTILE =
        FabricEntityTypeBuilder.<SlingshotProjectileEntity>create(SpawnGroup.MISC, SlingshotProjectileEntity::new)
            .dimensions(EntityDimensions.fixed(0.6F, 0.6F))
            .trackRangeBlocks(64)
            .trackedUpdateRate(10)
            .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, SLINGSHOT_PROJECTILE_ID));

    public static void registerAll() {
        Registry.register(Registries.ENTITY_TYPE, SLINGSHOT_PROJECTILE_ID, SLINGSHOT_PROJECTILE);
    }
}
