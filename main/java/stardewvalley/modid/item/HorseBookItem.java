package stardewvalley.modid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class HorseBookItem extends StardewBookItem {
    public HorseBookItem(Settings settings) {
        super(settings, "horse_the_book", false);
    }

    @Override
    protected void applyEffect(ServerWorld world, PlayerEntity player) {
    }
}
