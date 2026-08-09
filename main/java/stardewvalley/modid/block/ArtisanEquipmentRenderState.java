package stardewvalley.modid.block;

import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.texture.Sprite;

public class ArtisanEquipmentRenderState extends BlockEntityRenderState {
    public ItemRenderState itemRenderState = new ItemRenderState();
    public boolean hasItem = false;
    public float displayHeight = 1.0f;
    public Sprite itemSprite = null;

    // Second item for crystalarium dual display
    public ItemRenderState secondItemRenderState = new ItemRenderState();
    public boolean hasSecondItem = false;
}
