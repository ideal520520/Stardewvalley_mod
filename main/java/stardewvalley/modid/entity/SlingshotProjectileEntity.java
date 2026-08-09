package stardewvalley.modid.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class SlingshotProjectileEntity extends SnowballEntity {

    private float damageAmount = 0.0f;

    public SlingshotProjectileEntity(EntityType<? extends SlingshotProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    public SlingshotProjectileEntity(World world, double x, double y, double z) {
        super(ModEntities.SLINGSHOT_PROJECTILE, world);
        this.setPosition(x, y, z);
    }

    public void setDamage(float damage) {
        this.damageAmount = damage;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        if (damageAmount > 0 && entity instanceof LivingEntity living && this.getEntityWorld() instanceof ServerWorld serverWorld) {
            DamageSource damageSource = this.getDamageSources().thrown(this, this.getOwner());
            living.damage(serverWorld, damageSource, damageAmount);
        }
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.AIR;
    }

    @Override
    public double getGravity() {
        return 0.025; 
    }
}
