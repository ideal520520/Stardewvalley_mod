package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import stardewvalley.modid.StardewValley;

import java.util.*;

public class BundleCategoryScreen extends Screen {

    private final String categoryId;
    private final String categoryName;
    private final List<BundleManager.BundleDef> bundles;
    private final Map<String, Boolean> completedBundles;
    private final Set<String> vaultPurchased;
    private final Set<String> rewardClaimedBundleIds;

    public BundleCategoryScreen(String categoryId, String categoryName,
                                 Map<String, Boolean> completedBundles,
                                 Set<String> vaultPurchased,
                                 Set<String> rewardClaimedBundleIds) {
        super(Text.literal(categoryName));
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.bundles = BundleManager.getBundlesByCategory(categoryId);
        this.completedBundles = completedBundles;
        this.vaultPurchased = vaultPurchased;
        this.rewardClaimedBundleIds = rewardClaimedBundleIds;
    }

    @Override
    protected void init() {
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.bundle_category.back"),
            button -> { if (client != null) client.setScreen(new BundleRoomScreen()); }
        ).dimensions(width - 60, 5, 55, 20).build());
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        int iconSize = 64;
        int gapX = 20;
        int gapY = 30;
        int cols = 3;
        int totalW = Math.min(cols, bundles.size()) * iconSize + (Math.min(cols, bundles.size()) - 1) * gapX;
        int startX = (width - totalW) / 2;
        int startY = 50;

        for (int i = 0; i < bundles.size(); i++) {
            BundleManager.BundleDef bundle = bundles.get(i);
            int col = i % cols;
            int row = i / cols;
            int bx = startX + col * (iconSize + gapX);
            int by = startY + row * (iconSize + gapY);

            if (mx >= bx && mx < bx + iconSize && my >= by && my < by + iconSize) {
                if (client != null) {
                    client.setScreen(new BundleDetailScreen(bundle.id(), categoryId,
                        completedBundles, vaultPurchased, rewardClaimedBundleIds));
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        // 标题
        context.drawText(textRenderer, Text.literal(categoryName), width / 2 - textRenderer.getWidth(categoryName) / 2, 10, 0xFFFFAA, false);

        // 绘制bundle图标
        int iconSize = 64;
        int gapX = 20;
        int gapY = 30;
        int cols = 3;
        int totalW = Math.min(cols, bundles.size()) * iconSize + (Math.min(cols, bundles.size()) - 1) * gapX;
        int startX = (width - totalW) / 2;
        int startY = 50;

        String hoveredBundleName = null;
        for (int i = 0; i < bundles.size(); i++) {
            BundleManager.BundleDef bundle = bundles.get(i);
            int col = i % cols;
            int row = i / cols;
            int bx = startX + col * (iconSize + gapX);
            int by = startY + row * (iconSize + gapY);

            // 使用矩阵缩放绘制bundle图标（16x16纹理放大到64x64）
            Identifier bundleIcon = Identifier.of(StardewValley.MOD_ID,
                "textures/bundles/" + getBundleIconName(bundle.id()));
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(bx, by);
            matrices.scale(4.0f, 4.0f);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, bundleIcon, 0, 0, 0, 0, 16, 16, 16, 16);
            matrices.popMatrix();

            // 鼠标悬浮时记录悬停的具体收集包名称
            if (mouseX >= bx && mouseX < bx + iconSize && mouseY >= by && mouseY < by + iconSize) {
                hoveredBundleName = bundle.name();
            }
        }

        // 悬浮显示具体收集包名称
        if (hoveredBundleName != null) {
            context.drawTooltip(textRenderer, List.of(Text.literal(hoveredBundleName)), mouseX, mouseY);
        }

        super.render(context, mouseX, mouseY, delta);
    }

    private String getBundleIconName(String bundleId) {
        return switch (bundleId) {
            case "craft_spring" -> "bundle_spring_foraging_bundle.png";
            case "craft_summer" -> "bundle_summer_foraging_bundle.png";
            case "craft_fall" -> "bundle_fall_foraging_bundle.png";
            case "craft_winter" -> "bundle_winter_foraging_bundle.png";
            case "craft_construction" -> "bundle_construction_bundle.png";
            case "craft_exotic" -> "bundle_exotic_foraging_bundle.png";
            case "pantry_spring" -> "bundle_spring_crops_bundle.png";
            case "pantry_summer" -> "bundle_summer_crops_bundle.png";
            case "pantry_fall" -> "bundle_fall_crops_bundle.png";
            case "pantry_quality" -> "bundle_quality_crops_bundle.png";
            case "pantry_animal" -> "bundle_animal_bundle.png";
            case "pantry_artisan" -> "bundle_artisan_bundle.png";
            case "fish_river" -> "bundle_river_fish_bundle.png";
            case "fish_lake" -> "bundle_lake_fish_bundle.png";
            case "fish_ocean" -> "bundle_ocean_fish_bundle.png";
            case "fish_night" -> "bundle_night_fishing_bundle.png";
            case "fish_crab" -> "bundle_crab_pot_bundle.png";
            case "fish_specialty" -> "bundle_specialty_fish_bundle.png";
            case "boiler_blacksmith" -> "bundle_blacksmith_bundle.png";
            case "boiler_geologist" -> "bundle_geologists_bundle.png";
            case "boiler_adventurer" -> "Bundle_adventurers_bundle.png";
            case "bulletin_chef" -> "bundle_chefs_bundle.png";
            case "bulletin_dye" -> "bundle_dye_bundle.png";
            case "bulletin_field" -> "bundle_field_research_bundle.png";
            case "bulletin_fodder" -> "bundle_fodder_bundle.png";
            case "bulletin_enchanter" -> "bundle_enchanters_bundle.png";
            case "vault_2500" -> "Bundle_Orange.png";
            case "vault_5000" -> "Bundle_Purple.png";
            case "vault_10000" -> "Bundle_Red.png";
            case "vault_25000" -> "Bundle_Yellow.png";
            case "missing_joja" -> "bundle_the_missing_bundle.png";
            default -> "bundle_spring_foraging_bundle.png";
        };
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
