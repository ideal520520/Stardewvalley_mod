package stardewvalley.modid.block;

import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

/**
 * 通过 BlockEntityRenderer 直接渲染物品，不再使用单独实体展示。
 * 支持上百台设备无性能问题。
 */
public class ArtisanEquipmentBlockEntityRenderer implements BlockEntityRenderer<ArtisanEquipmentBlockEntity, ArtisanEquipmentRenderState> {

    private final ItemModelManager itemModelManager;

    public ArtisanEquipmentBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemModelManager = ctx.itemModelManager();
    }

    @Override
    public ArtisanEquipmentRenderState createRenderState() {
        return new ArtisanEquipmentRenderState();
    }

    @Override
    public void updateRenderState(ArtisanEquipmentBlockEntity entity, ArtisanEquipmentRenderState renderState,
                                   float tickDelta, Vec3d cameraPos,
                                   ModelCommandRenderer.CrumblingOverlayCommand overlayCommand) {
        BlockEntityRenderState.updateBlockEntityRenderState(entity, renderState, overlayCommand);

        // 方块已被破坏，不渲染
        BlockState blockState = entity.getWorld().getBlockState(entity.getPos());
        if (!(blockState.getBlock() instanceof ArtisanEquipmentBlock artisanBlock)) {
            renderState.hasItem = false;
            renderState.hasSecondItem = false;
            return;
        }

        // 默认重置第二个物品
        renderState.hasSecondItem = false;

        ArtisanEquipmentBlock.MachineState machineState = blockState.get(ArtisanEquipmentBlock.STATE);
        String itemId = null;

        // 宝石复制机完成时：显示输入物品 + 输出物品（两个叠在一起）
        if (machineState == ArtisanEquipmentBlock.MachineState.DONE && artisanBlock.getMachineType() == MachineType.CRYSTALARIUM) {
            String inputId = entity.getInputItemId();
            String outputId = entity.getOutputItemId();

            // 渲染输入物品（第一个，在底部）
            if (inputId != null && !inputId.isEmpty()) {
                Identifier id = Identifier.tryParse(inputId);
                if (id != null) {
                    net.minecraft.item.Item item = Registries.ITEM.get(id);
                    if (item != net.minecraft.item.Items.AIR) {
                        ItemStack stack = new ItemStack(item, 1);
                        this.itemModelManager.clearAndUpdate(
                            renderState.itemRenderState, stack, ItemDisplayContext.GROUND,
                            entity.getWorld(), null, entity.getPos().hashCode()
                        );
                        renderState.hasItem = true;
                    }
                }
            }

            // 渲染输出物品（第二个，在上方 0.3 格）
            if (outputId != null && !outputId.isEmpty()) {
                Identifier id = Identifier.tryParse(outputId);
                if (id != null) {
                    net.minecraft.item.Item item = Registries.ITEM.get(id);
                    if (item != net.minecraft.item.Items.AIR) {
                        ItemStack stack = new ItemStack(item, 1);
                        this.itemModelManager.clearAndUpdate(
                            renderState.secondItemRenderState, stack, ItemDisplayContext.GROUND,
                            entity.getWorld(), null, entity.getPos().hashCode() + 1
                        );
                        renderState.hasSecondItem = true;
                    }
                }
            }

            // 计算高度
            VoxelShape outlineShape = blockState.getOutlineShape(null, entity.getPos());
            if (outlineShape.isEmpty()) {
                renderState.hasItem = false;
                renderState.hasSecondItem = false;
                return;
            }
            renderState.displayHeight = (float) outlineShape.getBoundingBox().maxY + 0.1f;
            return;
        }

        if (machineState == ArtisanEquipmentBlock.MachineState.DONE && artisanBlock.getMachineType() == MachineType.SOLAR_PANEL) {
            // 太阳能板完成时直接渲染电池组
            itemId = "stardewvalley:battery_pack";
        } else if (machineState == ArtisanEquipmentBlock.MachineState.PROCESSING) {
            itemId = entity.getInputItemId();
        } else if (machineState == ArtisanEquipmentBlock.MachineState.DONE) {
            itemId = entity.getOutputItemId();
        }

        if (itemId != null && !itemId.isEmpty()) {
            Identifier id = Identifier.tryParse(itemId);
            if (id != null) {
                net.minecraft.item.Item item = Registries.ITEM.get(id);
                if (item != net.minecraft.item.Items.AIR) {
                    ItemStack stack = new ItemStack(item, 1);
                    this.itemModelManager.clearAndUpdate(
                        renderState.itemRenderState, stack, ItemDisplayContext.GROUND,
                        entity.getWorld(), null, entity.getPos().hashCode()
                    );
                    renderState.hasItem = true;
                }
            }
        } else {
            renderState.hasItem = false;
        }

        // 计算物品展示高度：方块模型最高点 + 0.1 格偏移
        VoxelShape outlineShape = blockState.getOutlineShape(null, entity.getPos());
        if (outlineShape.isEmpty()) {
            renderState.hasItem = false;
            renderState.hasSecondItem = false;
            return;
        }
        renderState.displayHeight = (float) outlineShape.getBoundingBox().maxY + 0.1f;
    }

    @Override
    public void render(ArtisanEquipmentRenderState renderState, MatrixStack matrices,
                       OrderedRenderCommandQueue commandQueue, CameraRenderState cameraRenderState) {
        if (!renderState.hasItem && !renderState.hasSecondItem) return;

        if (renderState.hasItem) {
            matrices.push();
            matrices.translate(0.5, renderState.displayHeight, 0.5);
            matrices.scale(0.6f, 0.6f, 0.6f);
            renderState.itemRenderState.render(matrices, commandQueue, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
        }

        if (renderState.hasSecondItem) {
            matrices.push();
            matrices.translate(0.5, renderState.displayHeight + 0.3, 0.5);
            matrices.scale(0.6f, 0.6f, 0.6f);
            renderState.secondItemRenderState.render(matrices, commandQueue, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
        }
    }
}
