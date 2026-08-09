package stardewvalley.modid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class CopperWallBookItem extends StardewBookItem {

    public CopperWallBookItem(Settings settings) {
        super(settings, "jack_be_nimble_jack_be_thick", false);
    }

    @Override
    protected void applyEffect(ServerWorld world, PlayerEntity player) {
        // 效果在 DefenseMixin 中通过 BookDataManager 检查实现
    }
}
