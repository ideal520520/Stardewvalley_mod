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

public class DeluxeWormBinBlockEntityRenderer implements BlockEntityRenderer<DeluxeWormBinBlockEntity, DeluxeWormBinRenderState> {

    private final ItemModelManager itemModelManager;

    public DeluxeWormBinBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemModelManager = ctx.itemModelManager();
    }

    @Override
    public DeluxeWormBinRenderState createRenderState() {
        return new DeluxeWormBinRenderState();
    }

    @Override
    public void updateRenderState(DeluxeWormBinBlockEntity entity, DeluxeWormBinRenderState renderState,
                                   float tickDelta, Vec3d cameraPos,
                                   ModelCommandRenderer.CrumblingOverlayCommand overlayCommand) {
        BlockEntityRenderState.updateBlockEntityRenderState(entity, renderState, overlayCommand);

        if (entity.hasOutput()) {
            ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of("stardewvalley", "bait_deluxe_bait")), 1);
            this.itemModelManager.clearAndUpdate(
                renderState.itemRenderState, stack, ItemDisplayContext.GROUND,
                entity.getWorld(), null, entity.getPos().hashCode()
            );
            renderState.hasItem = true;
        } else {
            renderState.hasItem = false;
        }
    }

    @Override
    public void render(DeluxeWormBinRenderState renderState, MatrixStack matrices,
                       OrderedRenderCommandQueue commandQueue, CameraRenderState cameraRenderState) {
        if (!renderState.hasItem) return;

        matrices.push();
        matrices.translate(0.5, 1.1, 0.5);
        matrices.scale(0.6f, 0.6f, 0.6f);
        renderState.itemRenderState.render(matrices, commandQueue, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }
}
