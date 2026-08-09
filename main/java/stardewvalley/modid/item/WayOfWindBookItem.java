package stardewvalley.modid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class WayOfWindBookItem extends StardewBookItem {
    public WayOfWindBookItem(Settings settings, String bookId) {
        super(settings, bookId, false);
    }

    @Override
    protected void applyEffect(ServerWorld world, PlayerEntity player) {
    }
}
