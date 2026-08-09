package stardewvalley.modid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Uuids;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;
import stardewvalley.modid.util.SafeCodec;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GoldenAnimalCrackerManager extends PersistentState {

    private static final String NAME = "stardewvalley_golden_animal_cracker";

    private final Set<UUID> boostedAnimals = new HashSet<>();

    private record Data(Set<UUID> boostedAnimals) {}

    private static final Codec<Set<UUID>> SET_CODEC = Codec.list(Uuids.CODEC).xmap(
        list -> new HashSet<>(list),
        set -> java.util.List.copyOf(set)
    );

    private static final Codec<Data> DATA_CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            SET_CODEC.optionalFieldOf("boosted_animals", new HashSet<>()).forGetter(Data::boostedAnimals)
        ).apply(instance, Data::new)
    );

    private static final Codec<GoldenAnimalCrackerManager> CODEC = DATA_CODEC.xmap(
        data -> {
            GoldenAnimalCrackerManager mgr = new GoldenAnimalCrackerManager();
            mgr.boostedAnimals.addAll(data.boostedAnimals());
            return mgr;
        },
        mgr -> new Data(new HashSet<>(mgr.boostedAnimals))
    );

    private static final PersistentStateType<GoldenAnimalCrackerManager> TYPE = new PersistentStateType<>(
        NAME,
        GoldenAnimalCrackerManager::new,
        SafeCodec.wrap(CODEC, GoldenAnimalCrackerManager::new),
        DataFixTypes.LEVEL
    );

    public static GoldenAnimalCrackerManager get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(TYPE);
    }

    /** 标记动物为已喂食金色动物饼干 */
    public void boost(UUID animalUuid) {
        boostedAnimals.add(animalUuid);
        setDirty(true);
    }

    /** 检查动物是否已获得饼干加成 */
    public boolean isBoosted(UUID animalUuid) {
        return boostedAnimals.contains(animalUuid);
    }

    /** 取消动物的加成（如动物死亡） */
    public void unboost(UUID animalUuid) {
        if (boostedAnimals.remove(animalUuid)) {
            setDirty(true);
        }
    }
}
