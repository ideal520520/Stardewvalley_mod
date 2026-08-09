package stardewvalley.modid.block;

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
import stardewvalley.modid.StardewValley;

public class MushroomStumpBlockEntityRenderer implements BlockEntityRenderer<MushroomStumpBlockEntity, MushroomStumpRenderState> {

    private final ItemModelManager itemModelManager;

    public MushroomStumpBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemModelManager = ctx.itemModelManager();
    }

    @Override
    public MushroomStumpRenderState createRenderState() {
        return new MushroomStumpRenderState();
    }

    @Override
    public void updateRenderState(MushroomStumpBlockEntity entity, MushroomStumpRenderState renderState,
                                   float tickDelta, Vec3d cameraPos,
                                   ModelCommandRenderer.CrumblingOverlayCommand overlayCommand) {
        BlockEntityRenderState.updateBlockEntityRenderState(entity, renderState, overlayCommand);

        String itemId = entity.getOutputItemId();
        if (!itemId.isEmpty()) {
            ItemStack stack = new ItemStack(Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, itemId.contains(":") ? itemId.split(":")[1] : itemId)), 1);
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
    public void render(MushroomStumpRenderState renderState, MatrixStack matrices,
                       OrderedRenderCommandQueue commandQueue, CameraRenderState cameraRenderState) {
        if (!renderState.hasItem) return;

        matrices.push();
        matrices.translate(0.5, 1.1, 0.5);
        matrices.scale(0.6f, 0.6f, 0.6f);
        renderState.itemRenderState.render(matrices, commandQueue, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }
}
