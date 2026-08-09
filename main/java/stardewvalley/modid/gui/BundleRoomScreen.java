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

public class BundleRoomScreen extends Screen {

    private static final Identifier JUNIMO_ICON = Identifier.of(StardewValley.MOD_ID, "textures/gui/Junimo_Icon.png");
    private static final Identifier GOLDEN_SCROLL = Identifier.of(StardewValley.MOD_ID, "textures/gui/Golden_Scroll.png");

    private static final String[] CATEGORIES = {"craft", "pantry", "fish", "boiler", "bulletin", "vault", "missing"};
    private static final String[] CATEGORY_NAMES = {"工艺室", "茶水间", "鱼缸", "锅炉房", "布告栏", "金库", "废弃Joja超市"};

    private Map<String, Boolean> completedBundles = new HashMap<>();
    private Set<String> vaultPurchased = new HashSet<>();
    private Set<String> rewardClaimedBundleIds = new HashSet<>();
    private boolean dataLoaded = false;

    private final boolean[] categoryCompleted = new boolean[7];
    private final int[] categoryProgress = new int[7];
    private final int[] categoryTotal = new int[7];

    public BundleRoomScreen() {
        super(Text.translatable("gui.stardewvalley.bundle_room.title"));
    }

    public void setBundleData(Map<String, Boolean> completed, Set<String> vaultPurchased,
                              Set<String> rewardClaimedBundleIds) {
        this.completedBundles = completed;
        this.vaultPurchased = vaultPurchased;
        this.rewardClaimedBundleIds = rewardClaimedBundleIds;
        this.dataLoaded = true;
        updateCategoryStats();
    }

    private void updateCategoryStats() {
        for (int i = 0; i < CATEGORIES.length; i++) {
            String cat = CATEGORIES[i];
            var bundles = BundleManager.getBundlesByCategory(cat);
            int done = 0;
            for (var b : bundles) {
                boolean c = completedBundles.getOrDefault(b.id(), false);
                if (c) done++;
            }
            categoryCompleted[i] = done == bundles.size();
            categoryProgress[i] = done;
            categoryTotal[i] = bundles.size();
        }
    }

    @Override
    protected void init() {
        ClientPlayNetworking.send(new ModPayloads.BundleDataRequestC2SPayload());
        addDrawableChild(ButtonWidget.builder(
            Text.translatable("gui.stardewvalley.bundle_room.back"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(width - 60, 5, 55, 20).build());
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        int scrollSize = 64;
        int gapX = 20;
        int cols = 3;
        int totalW = cols * scrollSize + (cols - 1) * gapX;
        int startX = (width - totalW) / 2;
        int rows = 2;
        int gapY = 40;
        int totalH = rows * scrollSize + (rows - 1) * gapY;
        int startY = (height - totalH) / 2;

        int totalCategories = CATEGORIES.length;
        for (int i = 0; i < totalCategories; i++) {
            int col = i % cols;
            int row = i / cols;
            int sx = startX + col * (scrollSize + gapX);
            int sy = startY + row * (scrollSize + gapY);
            // 第7个（索引6）放到第二行第四列（row 1, col 3）
            if (i == 6) {
                sx = startX + 3 * (scrollSize + gapX);
                sy = startY + 1 * (scrollSize + gapY);
            }
            if (mx >= sx && mx < sx + scrollSize && my >= sy && my < sy + scrollSize) {
                // 废弃Joja超市需要所有其他收集包完成才能进入
                if (i == 6 && !isMissingBundleUnlocked()) return true;
                if (client != null) {
                    client.setScreen(new BundleCategoryScreen(CATEGORIES[i], CATEGORY_NAMES[i],
                        completedBundles, vaultPurchased, rewardClaimedBundleIds));
                }
                return true;
            }
        }
        return false;
    }

    private boolean isMissingBundleUnlocked() {
        for (int i = 0; i < CATEGORIES.length - 1; i++) {
            var bundles = BundleManager.getBundlesByCategory(CATEGORIES[i]);
            boolean allDone = bundles.stream().allMatch(b -> completedBundles.getOrDefault(b.id(), false));
            if (!allDone) return false;
        }
        return true;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        // 标题
        context.drawTexture(RenderPipelines.GUI_TEXTURED, JUNIMO_ICON, width / 2 - 80, 8, 0, 0, 16, 16, 16, 16);
        context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_room.collector_bundle"), width / 2 - 60, 10, 0xFFFFAA, false);

        if (!dataLoaded) {
            context.drawText(textRenderer, Text.translatable("gui.stardewvalley.bundle_room.loading"), width / 2 - 20, height / 2, 0x808080, false);
            super.render(context, mouseX, mouseY, delta);
            return;
        }

        int scrollSize = 64;
        int gapX = 20;
        int cols = 3;
        int totalW = cols * scrollSize + (cols - 1) * gapX;
        int startX = (width - totalW) / 2;
        int rows = 2;
        int gapY = 40;
        int totalH = rows * scrollSize + (rows - 1) * gapY;
        int startY = (height - totalH) / 2;

        String hoveredCategoryName = null;
        for (int i = 0; i < CATEGORIES.length; i++) {
            int col = i % cols;
            int row = i / cols;
            int sx = startX + col * (scrollSize + gapX);
            int sy = startY + row * (scrollSize + gapY);
            // 第7个（索引6）放到第二行第四列（row 1, col 3）
            if (i == 6) {
                sx = startX + 3 * (scrollSize + gapX);
                sy = startY + 1 * (scrollSize + gapY);
            }

            // 使用矩阵缩放绘制Golden Scroll（16x16纹理放大到64x64）
            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(sx, sy);
            matrices.scale(4.0f, 4.0f);
            context.drawTexture(RenderPipelines.GUI_TEXTURED, GOLDEN_SCROLL, 0, 0, 0, 0, 16, 16, 16, 16);
            matrices.popMatrix();

            // 鼠标悬浮时记录悬停的分类名称
            if (mouseX >= sx && mouseX < sx + scrollSize && mouseY >= sy && mouseY < sy + scrollSize) {
                hoveredCategoryName = CATEGORY_NAMES[i];
            }
        }

        // 悬浮显示大类收集包名称
        if (hoveredCategoryName != null) {
            context.drawTooltip(textRenderer, List.of(Text.literal(hoveredCategoryName)), mouseX, mouseY);
        }
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
