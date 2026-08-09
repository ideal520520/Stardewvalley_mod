package stardewvalley.modid.block;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;

public class TapperRenderState extends BlockEntityRenderState {
    public ItemRenderState itemRenderState = new ItemRenderState();
    public boolean hasItem = false;
    public float displayHeight = 1.0f;
}
