package stardewvalley.modid.block;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

public class ModBlockEntities {
    public static final BlockEntityType<ArtisanEquipmentBlockEntity> ARTISAN_EQUIPMENT = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "artisan_equipment"),
        FabricBlockEntityTypeBuilder.create(
            ArtisanEquipmentBlockEntity::new,
            ModBlocks.BEE_HOUSE, ModBlocks.CASK, ModBlocks.CHEESE_PRESS,
            ModBlocks.DEHYDRATOR, ModBlocks.FISH_SMOKER, ModBlocks.KEG,
            ModBlocks.LOOM, ModBlocks.MAYONNAISE_MACHINE, ModBlocks.OIL_MAKER,
            ModBlocks.PRESERVES_JAR, ModBlocks.FURNACE, ModBlocks.CHARCOAL_KILN, ModBlocks.CRYSTALARIUM,
            ModBlocks.SOLAR_PANEL, ModBlocks.SEED_MAKER, ModBlocks.GEODE_CRUSHER,
            ModBlocks.HEAVY_FURNACE, ModBlocks.RECYCLING_MACHINE, ModBlocks.BAIT_MAKER
        ).build()
    );

    public static final BlockEntityType<TapperBlockEntity> TAPPER = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "tapper"),
        FabricBlockEntityTypeBuilder.create(
            TapperBlockEntity::new,
            ModBlocks.TAPPER, ModBlocks.HEAVY_TAPPER
        ).build()
    );

    public static final BlockEntityType<LightningRodBlockEntity> LIGHTNING_ROD = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "lightning_rod"),
        FabricBlockEntityTypeBuilder.create(
            LightningRodBlockEntity::new,
            ModBlocks.LIGHTNING_ROD
        ).build()
    );

    public static final BlockEntityType<TotemPoleBlockEntity> TOTEM_POLE = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "totem_pole"),
        FabricBlockEntityTypeBuilder.create(
            TotemPoleBlockEntity::new,
            ModBlocks.TOTEM_POLE
        ).build()
    );

    public static final BlockEntityType<CrabPotBlockEntity> CRAB_POT = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "crab_pot"),
        FabricBlockEntityTypeBuilder.create(
            CrabPotBlockEntity::new,
            ModBlocks.CRAB_POT
        ).build()
    );

    public static final BlockEntityType<DeluxeWormBinBlockEntity> DELUXE_WORM_BIN = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "deluxe_worm_bin"),
        FabricBlockEntityTypeBuilder.create(
            DeluxeWormBinBlockEntity::new,
            ModBlocks.DELUXE_WORM_BIN
        ).build()
    );

    public static final BlockEntityType<MushroomStumpBlockEntity> MUSHROOM_STUMP = Registry.register(
        Registries.BLOCK_ENTITY_TYPE,
        Identifier.of(StardewValley.MOD_ID, "mushroom_stump"),
        FabricBlockEntityTypeBuilder.create(
            MushroomStumpBlockEntity::new,
            ModBlocks.MUSHROOM_LOG
        ).build()
    );

    public static void registerAll() {}
}
