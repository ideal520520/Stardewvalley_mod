package stardewvalley.modid.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.component.ComponentType;

import java.util.ArrayList;
import java.util.List;

import stardewvalley.modid.util.SafeCodec;

public class RodComponent {

    /** 鱼饵槽最大堆叠数量 */
    public static final int MAX_BAIT_COUNT = 999;

    private ItemStack bait;
    private final List<ItemStack> tackles;

    public RodComponent() {
        this.bait = ItemStack.EMPTY;
        this.tackles = new ArrayList<>();
    }

    public RodComponent(ItemStack bait, List<ItemStack> tackles) {
        this.bait = bait == null ? ItemStack.EMPTY : bait;
        this.tackles = tackles != null ? new ArrayList<>(tackles) : new ArrayList<>();
    }

    public ItemStack getBait() { return bait; }
    public void setBait(ItemStack bait) { this.bait = bait == null ? ItemStack.EMPTY : bait; }

    public List<ItemStack> getTackles() { return tackles; }

    public void setTackle(int index, ItemStack tackle) {
        while (tackles.size() <= index) {
            tackles.add(ItemStack.EMPTY);
        }
        tackles.set(index, tackle == null ? ItemStack.EMPTY : tackle);
    }

    public ItemStack getTackle(int index) {
        if (index < 0 || index >= tackles.size()) return ItemStack.EMPTY;
        return tackles.get(index);
    }

    public int getTackleCount() { return tackles.size(); }

    public boolean hasBait() { return !bait.isEmpty(); }
    public boolean hasTackle(int index) { return index >= 0 && index < tackles.size() && !tackles.get(index).isEmpty(); }

    public static final Codec<RodComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("bait", ItemStack.EMPTY).forGetter(c -> c.bait),
            ItemStack.OPTIONAL_CODEC.listOf().optionalFieldOf("tackles", List.of()).forGetter(c -> c.tackles)
        ).apply(instance, RodComponent::new)
    );

    public static final ComponentType<RodComponent> TYPE = ComponentType.<RodComponent>builder()
        .codec(SafeCodec.wrap(CODEC, RodComponent::new))
        .build();

    public RodComponent copy() {
        return new RodComponent(bait.copy(), new ArrayList<>(tackles.stream().map(ItemStack::copy).toList()));
    }
}
