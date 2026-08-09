package stardewvalley.modid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ModFishingRodItem extends FishingRodItem {

    public ModFishingRodItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            if (world.isClient()) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new stardewvalley.modid.gui.ModPayloads.RodScreenOpenC2SPayload()
                );
            }
            return ActionResult.SUCCESS;
        }

        if (user.fishHook != null) {
            if (!world.isClient()) {
                user.fishHook.use(itemStack);
            }
            user.swingHand(hand);
            return ActionResult.SUCCESS;
        }

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            net.minecraft.sound.SoundEvents.ENTITY_FISHING_BOBBER_THROW,
            net.minecraft.sound.SoundCategory.NEUTRAL, 0.5f,
            0.4f / (world.getRandom().nextFloat() * 0.4f + 0.8f));

        world.playSound(null, user.getX(), user.getY(), user.getZ(),
            stardewvalley.modid.fishing.FishingSounds.CAST,
            net.minecraft.sound.SoundCategory.MASTER, 1.0F, 1.0F);

        if (!world.isClient()) {
            net.minecraft.entity.projectile.FishingBobberEntity bobber =
                new net.minecraft.entity.projectile.FishingBobberEntity(user, world, 0, 0);
            world.spawnEntity(bobber);
        }

        user.swingHand(hand);
        return ActionResult.SUCCESS;
    }

    public static int getTackleSlotCount(ItemStack rodStack) {
        String id = net.minecraft.registry.Registries.ITEM.getId(rodStack.getItem()).getPath();
        return switch (id) {
            case "advanced_iridium_rod" -> 2;
            case "iridium_rod" -> 1;
            default -> 0;
        };
    }

    public static boolean hasBaitSlot(ItemStack rodStack) {
        String id = net.minecraft.registry.Registries.ITEM.getId(rodStack.getItem()).getPath();
        return switch (id) {
            case "fiberglass_rod", "iridium_rod", "advanced_iridium_rod" -> true;
            default -> false;
        };
    }

    public static ItemStack getBait(ItemStack rodStack) {
        RodComponent comp = rodStack.get(RodComponent.TYPE);
        return comp != null ? comp.getBait() : ItemStack.EMPTY;
    }

    public static void setBait(ItemStack rodStack, ItemStack bait) {
        RodComponent comp = rodStack.getOrDefault(RodComponent.TYPE, new RodComponent());
        comp.setBait(bait);
        rodStack.set(RodComponent.TYPE, comp);
    }

    public static ItemStack getTackle(ItemStack rodStack, int index) {
        RodComponent comp = rodStack.get(RodComponent.TYPE);
        return comp != null ? comp.getTackle(index) : ItemStack.EMPTY;
    }

    public static void setTackle(ItemStack rodStack, int index, ItemStack tackle) {
        RodComponent comp = rodStack.getOrDefault(RodComponent.TYPE, new RodComponent());
        comp.setTackle(index, tackle);
        rodStack.set(RodComponent.TYPE, comp);
    }

    public static void consumeBait(ItemStack rodStack) {
        RodComponent comp = rodStack.get(RodComponent.TYPE);
        if (comp == null) return;
        ItemStack bait = comp.getBait();
        if (bait.isEmpty()) return;
        bait.decrement(1);
        if (bait.isEmpty()) {
            comp.setBait(ItemStack.EMPTY);
        } else {
            comp.setBait(bait);
        }
        rodStack.set(RodComponent.TYPE, comp);
    }

    public static void damageTackles(ItemStack rodStack) {
        RodComponent comp = rodStack.get(RodComponent.TYPE);
        if (comp == null) return;
        boolean changed = false;
        for (int i = 0; i < comp.getTackleCount(); i++) {
            ItemStack tackle = comp.getTackle(i);
            if (tackle.isEmpty()) continue;
            int damage = tackle.getDamage() + 1;
            if (damage >= tackle.getMaxDamage()) {
                comp.setTackle(i, ItemStack.EMPTY);
            } else {
                tackle.setDamage(damage);
                comp.setTackle(i, tackle);
            }
            changed = true;
        }
        if (changed) {
            rodStack.set(RodComponent.TYPE, comp);
        }
    }

    public static void applyFishingCost(PlayerEntity player) {
        ItemStack rod = player.getMainHandStack();
        if (!isSvRod(rod)) rod = player.getOffHandStack();
        if (!isSvRod(rod)) return;
        consumeBait(rod);
        damageTackles(rod);
        player.getInventory().markDirty();
    }

    public static void consumeAndDamage(ItemStack rodStack) {
        if (rodStack.isEmpty() || !isSvRod(rodStack)) return;
        consumeBait(rodStack);
        damageTackles(rodStack);
    }

    private static boolean isSvRod(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        net.minecraft.util.Identifier id = net.minecraft.registry.Registries.ITEM.getId(stack.getItem());
        if (!"stardewvalley".equals(id.getNamespace())) return false;
        String path = id.getPath();
        return path.equals("bamboo_pole") || path.equals("fiberglass_rod") || path.equals("iridium_rod") || path.equals("advanced_iridium_rod");
    }
}
