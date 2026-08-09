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

public class CrabPotBlockEntityRenderer implements BlockEntityRenderer<CrabPotBlockEntity, CrabPotRenderState> {

    private final ItemModelManager itemModelManager;

    public CrabPotBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {
        this.itemModelManager = ctx.itemModelManager();
    }

    @Override
    public CrabPotRenderState createRenderState() {
        return new CrabPotRenderState();
    }

    @Override
    public void updateRenderState(CrabPotBlockEntity entity, CrabPotRenderState renderState,
                                   float tickDelta, Vec3d cameraPos,
                                   ModelCommandRenderer.CrumblingOverlayCommand overlayCommand) {
        BlockEntityRenderState.updateBlockEntityRenderState(entity, renderState, overlayCommand);

        BlockState blockState = entity.getWorld().getBlockState(entity.getPos());
        if (!(blockState.getBlock() instanceof CrabPotBlock)) {
            renderState.hasItem = false;
            return;
        }

        String itemId = entity.getCaughtItemId();
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

        renderState.displayHeight = 0.375f;
    }

    @Override
    public void render(CrabPotRenderState renderState, MatrixStack matrices,
                       OrderedRenderCommandQueue commandQueue, CameraRenderState cameraRenderState) {
        if (!renderState.hasItem) return;

        matrices.push();
        matrices.translate(0.5, renderState.displayHeight, 0.5);
        matrices.scale(0.6f, 0.6f, 0.6f);
        renderState.itemRenderState.render(matrices, commandQueue, renderState.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
        matrices.pop();
    }
}
