package stardewvalley.modid.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import java.util.function.Supplier;

/**
 * Codec 安全包装工具：当编解码失败时返回默认实例/空数据而非抛出异常，
 * 防止因存档数据格式不兼容导致游戏崩溃。
 */
public class SafeCodec {

    /**
     * 包装一个 Codec，当解码失败时使用 fallback 工厂创建一个默认实例。
     * 编码失败时返回空数据（保留 prefix 不变）。
     */
    public static <T> Codec<T> wrap(Codec<T> codec, Supplier<T> fallback) {
        return new Codec<T>() {
            @Override
            public <T1> DataResult<Pair<T, T1>> decode(DynamicOps<T1> ops, T1 input) {
                DataResult<Pair<T, T1>> result = codec.decode(ops, input);
                return result.result()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.success(Pair.of(fallback.get(), input)));
            }

            @Override
            public <T1> DataResult<T1> encode(T input, DynamicOps<T1> ops, T1 prefix) {
                DataResult<T1> result = codec.encode(input, ops, prefix);
                return result.result()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.success(prefix));
            }

            @Override
            public String toString() {
                return "SafeCodec[" + codec + "]";
            }
        };
    }
}
