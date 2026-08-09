package stardewvalley.modid.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import stardewvalley.modid.block.ArtisanEquipmentBlock;
import stardewvalley.modid.block.ArtisanEquipmentBlockEntity;
import stardewvalley.modid.block.ArtisanEquipmentState;
import stardewvalley.modid.block.MachineType;
import stardewvalley.modid.block.ModBlocks;
import stardewvalley.modid.block.SolarPanelState;
import stardewvalley.modid.block.TapperBlock;
import stardewvalley.modid.block.TapperBlockEntity;
import stardewvalley.modid.block.TapperState;

public class FairyDustItem extends Item {

    public FairyDustItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (world.isClient()) return ActionResult.SUCCESS;

        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        ServerWorld serverWorld = (ServerWorld) world;

        // ===== 蜂房特殊处理（累加器模式，类似采集器）=====
        if (block == ModBlocks.BEE_HOUSE) {
            ArtisanEquipmentBlock.MachineState machineState = state.get(ArtisanEquipmentBlock.STATE);
            // EMPTY 表示正在累积天数但未产出，DONE 表示已有蜂蜜可收获
            if (machineState == ArtisanEquipmentBlock.MachineState.EMPTY) {
                String honeyType = ArtisanEquipmentState.findNearbyFlowerHoney(serverWorld, pos);
                String honeyId = honeyType != null ? honeyType : "honey";
                String fullId = "stardewvalley:" + honeyId;

                ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(serverWorld);
                stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
                    MachineType.BEE_HOUSE.name(), honeyId, 0, fullId, 1
                ));

                ArtisanEquipmentBlockEntity be = (ArtisanEquipmentBlockEntity) world.getBlockEntity(pos);
                if (be != null) {
                    be.setItems(honeyId, fullId);
                }

                world.setBlockState(pos, state.with(ArtisanEquipmentBlock.STATE, ArtisanEquipmentBlock.MachineState.DONE), 3);
                spawnBlueParticles(serverWorld, pos);
                if (player != null && !player.isCreative()) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
            // DONE 状态由原 onUse 处理收获
            return ActionResult.PASS;
        }

        // ===== 太阳能板特殊处理（累加器模式）=====
        if (block == ModBlocks.SOLAR_PANEL) {
            ArtisanEquipmentBlock.MachineState machineState = state.get(ArtisanEquipmentBlock.STATE);
            if (machineState == ArtisanEquipmentBlock.MachineState.EMPTY) {
                ArtisanEquipmentState stateStore = ArtisanEquipmentState.get(serverWorld);
                stateStore.setData(pos, new ArtisanEquipmentState.MachineData(
                    MachineType.SOLAR_PANEL.name(), "", 0, "stardewvalley:battery_pack", 1
                ));
                world.setBlockState(pos, state.with(ArtisanEquipmentBlock.STATE, ArtisanEquipmentBlock.MachineState.DONE), 3);
                if (world.getBlockEntity(pos) instanceof ArtisanEquipmentBlockEntity be) {
                    be.setItems("", "stardewvalley:battery_pack");
                }
                spawnBlueParticles(serverWorld, pos);
                if (player != null && !player.isCreative()) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
            // DONE 状态由原 onUse 处理收获
            return ActionResult.PASS;
        }

        // ===== 其他工匠设备（9种）：PROCESSING 时加速到 DONE =====
        if (block instanceof ArtisanEquipmentBlock) {
            ArtisanEquipmentBlock.MachineState machineState = state.get(ArtisanEquipmentBlock.STATE);
            if (machineState == ArtisanEquipmentBlock.MachineState.PROCESSING) {
                world.setBlockState(pos, state.with(ArtisanEquipmentBlock.STATE, ArtisanEquipmentBlock.MachineState.DONE), 3);
                spawnBlueParticles(serverWorld, pos);
                if (player != null && !player.isCreative()) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        // ===== 采集器（普通/重型）：!DONE 时加速 =====
        if (block instanceof TapperBlock) {
            if (!state.get(TapperBlock.DONE)) {
                Direction facing = state.get(TapperBlock.FACING);
                BlockPos attachedPos = pos.offset(facing.getOpposite());
                BlockState attachedState = world.getBlockState(attachedPos);
                String woodId = TapperBlock.getBlockId(attachedState);
                String productId = TapperBlock.getProductForWood(woodId);

                TapperState tapperState = TapperState.get(serverWorld);
                tapperState.setData(pos, new TapperState.TapperData(0, productId, 1));

                TapperBlockEntity be = (TapperBlockEntity) world.getBlockEntity(pos);
                if (be != null) {
                    be.setOutputItemId("stardewvalley:" + productId);
                }

                world.setBlockState(pos, state.with(TapperBlock.DONE, true), 3);
                spawnBlueParticles(serverWorld, pos);

                if (player != null && !player.isCreative()) {
                    stack.decrement(1);
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }

        return ActionResult.PASS;
    }

    private static void spawnBlueParticles(ServerWorld world, BlockPos pos) {
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK,
            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
            20, 0.5, 0.5, 0.5, 0.1);
    }
}
