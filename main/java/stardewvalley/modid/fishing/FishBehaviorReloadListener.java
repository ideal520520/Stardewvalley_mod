package stardewvalley.modid.fishing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SinglePreparationResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FishBehaviorReloadListener extends SinglePreparationResourceReloader<Map<String, JsonObject>>
        implements IdentifiableResourceReloadListener {

    private static final Logger LOGGER = LoggerFactory.getLogger("stardew_fishing");
    private static final Gson GSON = new Gson();
    private static final Identifier LOCATION = Identifier.of(FishingSounds.MODID, "data.json");
    private static FishBehaviorReloadListener INSTANCE;

    private final Map<net.minecraft.item.Item, FishBehavior> fishBehaviors = new HashMap<>();
    private FishBehavior defaultBehavior;

    public FishBehaviorReloadListener() {
        INSTANCE = this;
    }

    @Override
    public Identifier getFabricId() {
        return Identifier.of(FishingSounds.MODID, "fish_behavior_reload");
    }

    @Override
    protected Map<String, JsonObject> prepare(ResourceManager manager, Profiler profiler) {
        Map<String, JsonObject> objects = new HashMap<>();
        int packIndex = 0;
        for (Resource resource : manager.getAllResources(LOCATION)) {
            try (InputStream stream = resource.getInputStream();
                 Reader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                objects.put("pack_" + packIndex++, JsonHelper.deserialize(GSON, reader, JsonObject.class));
            } catch (Exception e) {
                LOGGER.error("Error loading {} from pack index {}", LOCATION, packIndex - 1, e);
            }
        }
        return objects;
    }

    @Override
    protected void apply(Map<String, JsonObject> jsonObjects, ResourceManager manager, Profiler profiler) {
        fishBehaviors.clear();
        for (Map.Entry<String, JsonObject> entry : jsonObjects.entrySet()) {
            BehaviorList.CODEC.parse(JsonOps.INSTANCE, entry.getValue())
                .resultOrPartial(err -> LOGGER.warn("Error decoding behavior list: {}", err))
                .ifPresent(list -> {
                    list.behaviors.forEach((loc, behavior) -> {
                        net.minecraft.item.Item item = Registries.ITEM.get(loc);
                        if (list.replace || !fishBehaviors.containsKey(item)) {
                            fishBehaviors.put(item, behavior);
                        }
                    });
                    list.defaultBehavior.ifPresent(b -> defaultBehavior = b);
                });
        }
    }

    public static @Nullable FishBehavior getBehavior(@Nullable ItemStack stack) {
        if (INSTANCE == null) return null;
        if (stack == null) return INSTANCE.defaultBehavior;
        return INSTANCE.fishBehaviors.getOrDefault(stack.getItem(), INSTANCE.defaultBehavior);
    }

    private record BehaviorList(boolean replace, Map<Identifier, FishBehavior> behaviors,
                                Optional<FishBehavior> defaultBehavior) {
        private static final com.mojang.serialization.Codec<BehaviorList> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                com.mojang.serialization.Codec.BOOL.optionalFieldOf("replace", false).forGetter(BehaviorList::replace),
                com.mojang.serialization.Codec.unboundedMap(Identifier.CODEC, FishBehavior.CODEC)
                    .fieldOf("behaviors").forGetter(BehaviorList::behaviors),
                FishBehavior.CODEC.optionalFieldOf("defaultBehavior").forGetter(BehaviorList::defaultBehavior)
        ).apply(inst, BehaviorList::new));
    }
}
