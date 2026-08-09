package stardewvalley.modid.item;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.block.TotemPoleBlockEntity;
import stardewvalley.modid.gui.ModPayloads;

public class WarpTotemItem extends Item {

    private static final int TELEPORT_COOLDOWN = 1200;
    private static final int SET_COOLDOWN = 200;
    private static final int ANIMATION_TICKS = 40;

    private final String totemType;

    public WarpTotemItem(Settings settings, String totemType) {
        super(settings);
        this.totemType = totemType;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.getItemCooldownManager().isCoolingDown(stack)) {
            return ActionResult.FAIL;
        }

        if (world.isClient()) return ActionResult.SUCCESS;

        ServerWorld serverWorld = (ServerWorld) world;
        WarpPositionState warpState = WarpPositionState.get(serverWorld);

        if (user.isSneaking()) {
            // Shift+右键：保存坐标（不消耗物品）
            warpState.setPosition(user.getUuid(), totemType, user.getX(), user.getY(), user.getZ());

            BlockPos newPos = BlockPos.ofFloored(user.getX(), user.getY(), user.getZ());
            Long oldPosLong = warpState.getAndSetDisplayPos(user.getUuid(), totemType, newPos.asLong());
            if (oldPosLong != null && oldPosLong.longValue() != newPos.asLong()) {
                BlockPos oldBlockPos = BlockPos.fromLong(oldPosLong);
                if (serverWorld.getBlockState(oldBlockPos).getBlock() == ModBlocks.TOTEM_POLE) {
                    serverWorld.removeBlock(oldBlockPos, false);
                }
            }

            serverWorld.setBlockState(newPos, ModBlocks.TOTEM_POLE.getDefaultState());
            BlockEntity be = serverWorld.getBlockEntity(newPos);
            if (be instanceof TotemPoleBlockEntity pole) {
                pole.setDisplayItemId("stardewvalley:warp_totem_" + totemType);
            }

            sendFloatingItem(serverWorld, user);
            user.getItemCooldownManager().set(stack, SET_COOLDOWN);
        } else {
            // 右键：传送（消耗1个物品）
            WarpPositionState.WarpData data = warpState.getPosition(user.getUuid(), totemType);
            if (data == null) {
                return ActionResult.SUCCESS;
            }

            if (!user.isCreative()) stack.decrement(1);

            playSound(serverWorld, user);
            sendFloatingItem(serverWorld, user);
            user.getItemCooldownManager().set(stack, TELEPORT_COOLDOWN);

            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) user;
            double tx = data.x(), ty = data.y(), tz = data.z();
            float yaw = user.getYaw(), pitch = user.getPitch();
            new Thread(() -> {
                try {
                    Thread.sleep(ANIMATION_TICKS * 50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
                serverWorld.getServer().execute(() ->
                    serverPlayer.networkHandler.requestTeleport(tx, ty, tz, yaw, pitch));
            }).start();
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        return ActionResult.PASS;
    }

    private void playSound(ServerWorld world, PlayerEntity player) {
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
            TotemSounds.TOTEM_USE, SoundCategory.PLAYERS, 10.0f, 1.0f);
    }

    private void sendFloatingItem(ServerWorld world, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            String itemId = StardewValley.MOD_ID + ":warp_totem_" + totemType;
            ServerPlayNetworking.send(serverPlayer, new ModPayloads.FloatingItemS2CPayload(itemId));
        }
    }
}
