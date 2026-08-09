package stardewvalley.modid.fishing;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class FishingSounds {
    public static final String MODID = "stardew_fishing";

    // 这些 sound event 由 assets/stardew_fishing/sounds.json 自动注册
    // 我们只需要创建引用，不需要手动 Registry.register
    public static final SoundEvent CAST = of("cast");
    public static final SoundEvent COMPLETE = of("complete");
    public static final SoundEvent DWOP = of("dwop");
    public static final SoundEvent FISH_ESCAPE = of("fish_escape");
    public static final SoundEvent FISH_BITE = of("fish_bite");
    public static final SoundEvent FISH_HIT = of("fish_hit");
    public static final SoundEvent PULL_ITEM = of("pull_item");
    public static final SoundEvent REEL_CREAK = of("reel_creak");
    public static final SoundEvent REEL_FAST = of("reel_fast");
    public static final SoundEvent REEL_SLOW = of("reel_slow");

    private static SoundEvent of(String name) {
        return SoundEvent.of(Identifier.of(MODID, name));
    }

    public static void register() {
        // sounds.json 会在资源加载时自动注册，什么都不需要做
    }
}
