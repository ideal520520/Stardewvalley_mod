package stardewvalley.modid.item;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import stardewvalley.modid.StardewValley;

/**
 * 图腾音效。
 * 需要将 stardewvally_resources/sounds/totem.mp3 转换为 .ogg 格式，
 * 放到 src/main/resources/assets/stardewvalley/sounds/totem.ogg
 */
public class TotemSounds {
    public static final SoundEvent TOTEM_USE = SoundEvent.of(Identifier.of(StardewValley.MOD_ID, "totem"));

    public static void register() {
        // sounds.json 会在资源加载时自动注册
    }
}
