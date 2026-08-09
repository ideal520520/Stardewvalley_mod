package stardewvalley.modid.item;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.entity.SlingshotProjectileEntity;

import java.util.Map;

public class ModSlingshotItem extends BowItem {

    private final boolean isMaster;
    // 弹药类型 -> (弹药系数, 普通弹弓平均伤害)
    private static final Map<String, float[]> AMMO_DAMAGE = Map.ofEntries(
        Map.entry("coal", new float[]{15f, 19.5f}),
        Map.entry("copper_ore", new float[]{10f, 13f}),
        Map.entry("explosive_ammo", new float[]{20f, 25.5f}),
        Map.entry("gold_ore", new float[]{30f, 38f}),
        Map.entry("iridium_ore", new float[]{50f, 63f}),
        Map.entry("iron_ore", new float[]{20f, 25.5f}),
        Map.entry("misc_stone", new float[]{5f, 7f}),
        Map.entry("wood", new float[]{3f, 3f})
    );

    public ModSlingshotItem(Settings settings, boolean isMaster) {
        super(settings);
        this.isMaster = isMaster;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 2000;
    }

    public static int getPullTicks(float useTicks) {
        return Math.round(useTicks * 20.0f);
    }

    @Override
    public int getRange() {
        return 8;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            if (world.isClient()) {
                net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(
                    new stardewvalley.modid.gui.ModPayloads.SlingshotOpenC2SPayload()
                );
            }
            return ActionResult.SUCCESS;
        }

        SlingshotComponent comp = stack.get(SlingshotComponent.TYPE);
        if (comp == null || !comp.hasAmmo()) {
            if (!world.isClient()) {
                user.sendMessage(net.minecraft.text.Text.literal("弹弓没有弹药！"), true);
            }
            return ActionResult.FAIL;
        }

        user.setCurrentHand(hand);
        return ActionResult.SUCCESS;
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        if (!(user instanceof PlayerEntity player)) return false;

        int maxUseTime = this.getMaxUseTime(stack, user);
        int useTicks = maxUseTime - remainingUseTicks;

        if (useTicks < 8) return false;

        SlingshotComponent comp = stack.get(SlingshotComponent.TYPE);
        if (comp == null || !comp.hasAmmo()) return false;

        ItemStack ammoStack = comp.getAmmo();
        String ammoId = Registries.ITEM.getId(ammoStack.getItem()).getPath();
        float[] damageData = AMMO_DAMAGE.get(ammoId);

        if (damageData == null) return false;

        float baseDamage = damageData[1];
        float avgDamage = isMaster ? baseDamage * 2.0f : baseDamage;
        float finalDamage = applyDamageReduction(avgDamage);

        // 客户端和服务端都消耗弹药
        comp.consumeAmmo();
        stack.set(SlingshotComponent.TYPE, comp);

        if (!world.isClient()) {
            SlingshotProjectileEntity projectile = new SlingshotProjectileEntity(
                world, player.getX(), player.getEyeY() - 0.1, player.getZ()
            );
            projectile.setOwner(player);
            projectile.setDamage(finalDamage);
            projectile.setItem(ammoStack.copyWithCount(1));
            projectile.setVelocity(player, player.getPitch(), player.getYaw(), 0.0F, 3.0F, 1.0F);
            world.spawnEntity(projectile);
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F);

        return true;
    }

    private float applyDamageReduction(float damage) {
        // 伤害越高衰减越大，低伤无衰减、中伤适度减、高伤大幅减
        if (damage <= 10.0f) return damage;
        if (damage <= 30.0f) return 10.0f + (damage - 10.0f) * 0.5f;
        return 19.0f + (damage - 30.0f) * 0.10f;
    }

    public boolean isMasterSlingshot() { return isMaster; }

    public static boolean isSlingshotItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Identifier id = Registries.ITEM.getId(stack.getItem());
        if (!StardewValley.MOD_ID.equals(id.getNamespace())) return false;
        String path = id.getPath();
        return path.equals("slingshot") || path.equals("master_slingshot");
    }

    public static ItemStack getAmmo(ItemStack slingshotStack) {
        SlingshotComponent comp = slingshotStack.get(SlingshotComponent.TYPE);
        return comp != null ? comp.getAmmo() : ItemStack.EMPTY;
    }

    public static void setAmmo(ItemStack slingshotStack, ItemStack ammo) {
        SlingshotComponent comp = slingshotStack.getOrDefault(SlingshotComponent.TYPE, new SlingshotComponent());
        comp.setAmmo(ammo);
        slingshotStack.set(SlingshotComponent.TYPE, comp);
    }

}
