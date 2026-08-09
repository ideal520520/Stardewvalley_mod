package stardewvalley.modid.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ModDaggerItem extends ModWeaponItem {

    private final float weaponDamage;

    public ModDaggerItem(Settings settings, float attackDamage,
                         float attackSpeed, float critChance, int critPower,
                         float defense, int weight, boolean lifesteal,
                         boolean crusader, int weaponLevel, int sellPrice) {
        super(settings, WeaponType.DAGGER, attackDamage, attackSpeed, critChance, critPower, defense, weight, lifesteal, crusader, weaponLevel, sellPrice);
        this.weaponDamage = attackDamage;
    }

    public float getWeaponDamage() {
        return weaponDamage;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        return ActionResult.PASS;
    }
}
