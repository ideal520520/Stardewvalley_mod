package stardewvalley.modid.item;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.block.ModFarmlandBlock;

public class WateringCanItem extends Item {

    public enum Tier {
        NORMAL(40, 1, 1, 20),
        COPPER(55, 1, 3, 20),
        STEEL(70, 1, 5, 20),
        GOLD(85, 3, 3, 20),
        IRIDIUM(100, 3, 6, 20);

        public final int maxDurability;
        public final int width;
        public final int depth;
        public final int cooldownTicks;

        Tier(int maxDurability, int width, int depth, int cooldownTicks) {
            this.maxDurability = maxDurability;
            this.width = width;
            this.depth = depth;
            this.cooldownTicks = cooldownTicks;
        }
    }

    private final Tier tier;

    public WateringCanItem(Settings settings, Tier tier) {
        super(settings.maxDamage(tier.maxDurability));
        this.tier = tier;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        BlockHitResult hitResult = Item.raycast(world, user, RaycastContext.FluidHandling.SOURCE_ONLY);
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = hitResult.getBlockPos();
            if (world.getBlockState(pos).getBlock() == Blocks.WATER) {
                if (!world.isClient()) {
                    stack.setDamage(0);
                }
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        // Refill on water source
        if (state.getBlock() == Blocks.WATER) {
            if (!world.isClient()) {
                stack.setDamage(0);
            }
            return ActionResult.SUCCESS;
        }
        BlockPos below = pos.down();
        BlockState belowState = world.getBlockState(below);
        if (belowState.getBlock() == Blocks.WATER) {
            if (!world.isClient()) {
                stack.setDamage(0);
            }
            return ActionResult.SUCCESS;
        }

        if (stack.getDamage() >= stack.getMaxDamage()) {
            return ActionResult.PASS;
        }

        if (player != null && player.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.PASS;
        }

        if (state.getBlock() != Blocks.FARMLAND && !(state.getBlock() instanceof FarmlandBlock) && !(state.getBlock() instanceof ModFarmlandBlock)) {
            below = pos.down();
            belowState = world.getBlockState(below);
            if (belowState.getBlock() instanceof FarmlandBlock || belowState.getBlock() instanceof ModFarmlandBlock) {
                pos = below;
                state = belowState;
            } else {
                return ActionResult.PASS;
            }
        }

        if (!world.isClient()) {
            Direction facing = player != null ? player.getHorizontalFacing() : Direction.NORTH;
            int halfW = tier.width / 2;

            for (int w = -halfW; w <= halfW; w++) {
                for (int d = 0; d < tier.depth; d++) {
                    BlockPos target = offsetByFacing(pos, facing, w, d);
                    BlockState targetState = world.getBlockState(target);
                    if (targetState.getBlock() instanceof FarmlandBlock || targetState.getBlock() instanceof ModFarmlandBlock) {
                        BlockState modFarmland = ModBlocks.ensureModFarmland(world, target);
                        world.setBlockState(target, modFarmland
                            .with(FarmlandBlock.MOISTURE, 7)
                            .with(ModFarmlandBlock.RETAIN_DAYS, 1), 3);
                    }
                }
            }

            stack.setDamage(Math.min(stack.getDamage() + 1, stack.getMaxDamage()));

            if (player != null) {
                player.getHungerManager().addExhaustion(1.0f);
                player.getItemCooldownManager().set(stack, tier.cooldownTicks);
            }
        }
        return ActionResult.SUCCESS;
    }

    private static BlockPos offsetByFacing(BlockPos pos, Direction facing, int lateral, int forward) {
        Direction right = facing.rotateYClockwise();
        return pos.offset(facing, forward).offset(right, lateral);
    }

    public Tier getTier() {
        return tier;
    }
}