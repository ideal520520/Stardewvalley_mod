package stardewvalley.modid.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.BombBlock;

public class BombItem extends Item {

    private final Block bombBlock;

    public BombItem(Settings settings, Block bombBlock) {
        super(settings);
        this.bombBlock = bombBlock;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos clickedPos = context.getBlockPos();
        Direction side = context.getSide();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();

        if (world.isClient()) return ActionResult.SUCCESS;

        BlockPos placePos = clickedPos.offset(side);

        if (!world.getBlockState(placePos).isReplaceable()) {
            return ActionResult.FAIL;
        }

        BlockState bombState = bombBlock.getDefaultState()
            .with(net.minecraft.block.FacingBlock.FACING, side);

        world.setBlockState(placePos, bombState, 3);

        // Start fuse timer
        if (world instanceof ServerWorld serverWorld) {
            BombBlock.startFuse(serverWorld, placePos);
        }

        // Play placement sound
        world.playSound(null, placePos, SoundEvents.BLOCK_STONE_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);

        // Consume item
        if (player != null && !player.isCreative()) {
            stack.decrement(1);
        }

        return ActionResult.SUCCESS;
    }
}
