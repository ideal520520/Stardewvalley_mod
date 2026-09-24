package stardewvalley.modid;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

/** 玩家吃下的星之果实数量，作为数据附件随玩家数据一起持久化，死亡后保留 */
public class StardropData {

    public static final AttachmentType<Integer> STARDROPS = AttachmentRegistry.<Integer>builder()
        .initializer(() -> 0)
        .persistent(Codec.INT)
        .copyOnDeath()
        .buildAndRegister(Identifier.of(StardewValley.MOD_ID, "stardrops"));

    /** 触发静态初始化，必须在玩家数据加载前调用 */
    public static void register() {
    }

    public static int get(PlayerEntity player) {
        return player.getAttachedOrCreate(STARDROPS);
    }

    /** 增加数量并返回增加后的值 */
    public static int add(PlayerEntity player, int amount) {
        int value = Math.max(0, get(player) + amount);
        player.setAttached(STARDROPS, value);
        return value;
    }
}
