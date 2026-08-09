package stardewvalley.modid.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.component.ComponentType;
import stardewvalley.modid.util.SafeCodec;

public class SlingshotComponent {
    public static final int MAX_AMMO_COUNT = 999;

    private ItemStack ammo;

    public SlingshotComponent() {
        this.ammo = ItemStack.EMPTY;
    }

    public SlingshotComponent(ItemStack ammo) {
        this.ammo = ammo == null ? ItemStack.EMPTY : ammo;
    }

    public ItemStack getAmmo() { return ammo; }
    public void setAmmo(ItemStack ammo) { this.ammo = ammo == null ? ItemStack.EMPTY : ammo; }
    public boolean hasAmmo() { return !ammo.isEmpty(); }

    public static final Codec<SlingshotComponent> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            ItemStack.OPTIONAL_CODEC.optionalFieldOf("ammo", ItemStack.EMPTY).forGetter(c -> c.ammo)
        ).apply(instance, SlingshotComponent::new)
    );

    public static final ComponentType<SlingshotComponent> TYPE = ComponentType.<SlingshotComponent>builder()
        .codec(SafeCodec.wrap(CODEC, SlingshotComponent::new))
        .build();

    public SlingshotComponent copy() {
        return new SlingshotComponent(ammo.copy());
    }

    public void consumeAmmo() {
        if (!ammo.isEmpty()) {
            ammo.decrement(1);
            if (ammo.isEmpty()) {
                ammo = ItemStack.EMPTY;
            }
        }
    }
}
