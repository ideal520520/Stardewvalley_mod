package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.crafting.CraftingMaterialSets;

import java.util.ArrayList;
import java.util.List;

public class CookingScreen extends Screen {

    private static final Identifier DISHES1_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/dishes1.png");
    private static final Identifier DISHES2_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/dishes2.png");
    private static final int SLOT_SIZE = 18;
    private static final int TEX_W = 200, TEX_H = 152;

    // 点击区域偏移（相对于贴图左上角）
    private static final int P1_OX = 9, P1_OY = 10, P1_W = 177, P1_H = 123;
    private static final int P1_COLS = 10, P1_ROWS = 7;
    private static final int P2_OX = 11, P2_OY = 10, P2_W = 174, P2_H = 34;
    private static final int P2_COLS = 10, P2_ROWS = 2;
    // 翻页按钮偏移（相对于贴图左上角）
    private static final int NEXT_BTN_OX = 180, NEXT_BTN_OY = 135, NEXT_BTN_W = 12, NEXT_BTN_H = 11;
    private static final int PREV_BTN_OX = 8, PREV_BTN_OY = 135, PREV_BTN_W = 12, PREV_BTN_H = 11;

    private int currentPage = 0;
    private int texX;
    private int texY;
    private int invGridX;
    private int invGridY;
    private final List<DishRecipe> allDishes = new ArrayList<>();

    private static class Ingredient {
        final String itemId;
        final int count;
        Ingredient(String itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
    }

    private static class DishRecipe {
        final String resultItemId;
        final String displayName;
        final List<Ingredient> ingredients;

        DishRecipe(String resultItemId, String displayName, List<Ingredient> ingredients) {
            this.resultItemId = resultItemId;
            this.displayName = displayName;
            this.ingredients = ingredients;
        }
    }

    public CookingScreen() {
        super(Text.translatable("gui.stardewvalley.cooking.title"));
        initDishes();
    }

    private void initDishes() {
        // ====== Page 1 (70 dishes, 10x7) ======
        allDishes.add(new DishRecipe("fried_egg", "煎鸡蛋", List.of(new Ingredient("any_egg", 1))));
        allDishes.add(new DishRecipe("omelet", "煎蛋卷", List.of(new Ingredient("any_egg", 1), new Ingredient("any_milk", 1))));
        allDishes.add(new DishRecipe("salad", "沙拉", List.of(new Ingredient("caiji_leek", 1), new Ingredient("caiji_dandelion", 1), new Ingredient("vinegar", 1))));
        allDishes.add(new DishRecipe("cheese_cauliflower", "乳酪花椰菜", List.of(new Ingredient("cauliflower", 1), new Ingredient("cheese", 1))));
        allDishes.add(new DishRecipe("baked_fish", "烤鱼", List.of(new Ingredient("fish_sunfish", 1), new Ingredient("fish_bream", 1), new Ingredient("wheat_flour", 1))));
        allDishes.add(new DishRecipe("parsnip_soup", "防风草汤", List.of(new Ingredient("parsnip", 1), new Ingredient("any_milk", 1), new Ingredient("vinegar", 1))));
        allDishes.add(new DishRecipe("vegetable_medley", "蔬菜杂烩", List.of(new Ingredient("tomato", 1), new Ingredient("beet", 1))));
        allDishes.add(new DishRecipe("complete_breakfast", "完美早餐", List.of(new Ingredient("fried_egg", 1), new Ingredient("any_milk", 1), new Ingredient("hashbrowns", 1), new Ingredient("pancakes", 1))));
        allDishes.add(new DishRecipe("fried_calamari", "炸鱿鱼", List.of(new Ingredient("fish_squid", 1), new Ingredient("wheat_flour", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("strange_bun", "奇怪的小面包", List.of(new Ingredient("wheat_flour", 1), new Ingredient("fish_periwinkle", 1), new Ingredient("void_mayonnaise", 1))));

        allDishes.add(new DishRecipe("lucky_lunch", "幸运午餐", List.of(new Ingredient("fish_sea_cucumber", 1), new Ingredient("tortilla", 1), new Ingredient("bluejazz", 1))));
        allDishes.add(new DishRecipe("fried_mushroom", "炒蘑菇", List.of(new Ingredient("caiji_commonmushroom", 1), new Ingredient("caiji_morel", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("pizza", "披萨", List.of(new Ingredient("wheat_flour", 1), new Ingredient("tomato", 1), new Ingredient("cheese", 1))));
        allDishes.add(new DishRecipe("bean_hotpot", "豆类火锅", List.of(new Ingredient("greenbean", 2))));
        allDishes.add(new DishRecipe("glazed_yams", "琉璃山药", List.of(new Ingredient("yam", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("carp_surprise", "惊喜鲤鱼", List.of(new Ingredient("fish_carp", 4))));
        allDishes.add(new DishRecipe("hashbrowns", "薯饼", List.of(new Ingredient("potato", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("pancakes", "薄煎饼", List.of(new Ingredient("wheat_flour", 1), new Ingredient("any_egg", 1))));
        allDishes.add(new DishRecipe("salmon_dinner", "鲑鱼晚餐", List.of(new Ingredient("fish_salmon", 1), new Ingredient("amaranth", 1), new Ingredient("kale", 1))));
        allDishes.add(new DishRecipe("fish_taco", "鱼肉卷", List.of(new Ingredient("fish_tuna", 1), new Ingredient("tortilla", 1), new Ingredient("redcabbage", 1), new Ingredient("mayonnaise", 1))));

        allDishes.add(new DishRecipe("crispy_bass", "香酥鲈鱼", List.of(new Ingredient("fish_largemouth_bass", 1), new Ingredient("wheat_flour", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("pepper_poppers", "爆炒青椒", List.of(new Ingredient("hotpepper", 1), new Ingredient("cheese", 1))));
        allDishes.add(new DishRecipe("bread", "面包", List.of(new Ingredient("wheat_flour", 1))));
        allDishes.add(new DishRecipe("tom_kha_soup", "椰汁汤", List.of(new Ingredient("caiji_coconut", 1), new Ingredient("fish_shrimp", 1), new Ingredient("caiji_commonmushroom", 1))));
        allDishes.add(new DishRecipe("trout_soup", "鳟鱼汤", List.of(new Ingredient("fish_rainbow_trout", 1), new Ingredient("fish_green_algae", 1))));
        allDishes.add(new DishRecipe("chocolate_cake", "巧克力蛋糕", List.of(new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1), new Ingredient("any_egg", 1))));
        allDishes.add(new DishRecipe("pink_cake", "粉红蛋糕", List.of(new Ingredient("melon", 1), new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1), new Ingredient("any_egg", 1))));
        allDishes.add(new DishRecipe("rhubarb_pie", "大黄派", List.of(new Ingredient("rhubarb", 1), new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("cookie", "饼干", List.of(new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1), new Ingredient("any_egg", 1))));
        allDishes.add(new DishRecipe("spaghetti", "意大利面", List.of(new Ingredient("wheat_flour", 1), new Ingredient("tomato", 1))));

        allDishes.add(new DishRecipe("fried_eel", "炒鳗鱼", List.of(new Ingredient("fish_eel", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("spicy_eel", "香辣鳗鱼", List.of(new Ingredient("fish_eel", 1), new Ingredient("hotpepper", 1))));
        allDishes.add(new DishRecipe("sashimi", "生鱼片", List.of(new Ingredient("any_fish", 1))));
        allDishes.add(new DishRecipe("maki_roll", "生鱼寿司", List.of(new Ingredient("any_fish", 1), new Ingredient("caiji_seaweed", 1), new Ingredient("rice", 1))));
        allDishes.add(new DishRecipe("tortilla", "墨西哥薄饼", List.of(new Ingredient("corn", 1))));
        allDishes.add(new DishRecipe("red_plate", "红之盛宴", List.of(new Ingredient("redcabbage", 1), new Ingredient("radish", 1))));
        allDishes.add(new DishRecipe("eggplant_parmesan", "帕尔玛奶酪茄子", List.of(new Ingredient("eggplant", 1), new Ingredient("tomato", 1))));
        allDishes.add(new DishRecipe("rice_pudding", "大米布丁", List.of(new Ingredient("any_milk", 1), new Ingredient("sugar", 1), new Ingredient("rice", 1))));
        allDishes.add(new DishRecipe("ice_cream", "冰淇淋", List.of(new Ingredient("any_milk", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("blueberry_tart", "蓝莓千层酥", List.of(new Ingredient("blueberry", 1), new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1), new Ingredient("any_egg", 1))));

        allDishes.add(new DishRecipe("autums_bounty", "秋日恩赐", List.of(new Ingredient("yam", 1), new Ingredient("pumpkin", 1))));
        allDishes.add(new DishRecipe("pumpkin_soup", "南瓜汤", List.of(new Ingredient("pumpkin", 1), new Ingredient("any_milk", 1))));
        allDishes.add(new DishRecipe("super_meal", "巨无霸餐", List.of(new Ingredient("bokchoy", 1), new Ingredient("cranberries", 1), new Ingredient("artichoke", 1))));
        allDishes.add(new DishRecipe("cranberry_sauce", "红莓酱", List.of(new Ingredient("cranberries", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("stuffing", "塞料面包", List.of(new Ingredient("bread", 1), new Ingredient("cranberries", 1), new Ingredient("caiji_hazelnut", 1))));
        allDishes.add(new DishRecipe("farmers_lunch", "农夫午餐", List.of(new Ingredient("omelet", 1), new Ingredient("parsnip", 1))));
        allDishes.add(new DishRecipe("survival_burger", "救生汉堡", List.of(new Ingredient("bread", 1), new Ingredient("caiji_cavecarrot", 1), new Ingredient("eggplant", 1))));
        allDishes.add(new DishRecipe("dish_of_the_sea", "海之菜肴", List.of(new Ingredient("fish_sardine", 2), new Ingredient("hashbrowns", 1))));
        allDishes.add(new DishRecipe("miners_treat", "矿工特供", List.of(new Ingredient("caiji_cavecarrot", 2), new Ingredient("sugar", 1), new Ingredient("any_milk", 1))));
        allDishes.add(new DishRecipe("roots_platter", "块茎拼盘", List.of(new Ingredient("caiji_cavecarrot", 1), new Ingredient("caiji_winterroot", 1))));

        allDishes.add(new DishRecipe("triple_shot_espresso", "三倍浓缩咖啡", List.of(new Ingredient("coffee", 3))));
        allDishes.add(new DishRecipe("seafoam_pudding", "海泡布丁", List.of(new Ingredient("fish_flounder", 1), new Ingredient("fish_midnight_carp", 1), new Ingredient("squid_ink", 1))));
        allDishes.add(new DishRecipe("algae_soup", "海藻汤", List.of(new Ingredient("fish_green_algae", 4))));
        allDishes.add(new DishRecipe("pale_broth", "清汤", List.of(new Ingredient("fish_white_algae", 2))));
        allDishes.add(new DishRecipe("plum_pudding", "葡萄干布丁", List.of(new Ingredient("caiji_wildplum", 2), new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("artichoke_dip", "水煮洋蓟", List.of(new Ingredient("artichoke", 1), new Ingredient("any_milk", 1))));
        allDishes.add(new DishRecipe("stir_fry", "蔬菜什锦盖饭", List.of(new Ingredient("caiji_cavecarrot", 1), new Ingredient("caiji_commonmushroom", 1), new Ingredient("kale", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("roasted_hazelnuts", "烤榛子", List.of(new Ingredient("caiji_hazelnut", 3))));
        allDishes.add(new DishRecipe("pumpkin_pie", "南瓜派", List.of(new Ingredient("pumpkin", 1), new Ingredient("wheat_flour", 1), new Ingredient("any_milk", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("radish_salad", "萝卜沙拉", List.of(new Ingredient("any_oil", 1), new Ingredient("vinegar", 1), new Ingredient("radish", 1))));

        allDishes.add(new DishRecipe("fruit_salad", "水果沙拉", List.of(new Ingredient("blueberry", 1), new Ingredient("melon", 1), new Ingredient("apricot", 1))));
        allDishes.add(new DishRecipe("blackberry_cobbler", "黑莓脆皮饼", List.of(new Ingredient("caiji_blackberry", 2), new Ingredient("sugar", 1), new Ingredient("wheat_flour", 1))));
        allDishes.add(new DishRecipe("cranberry_candy", "蔓越莓糖果", List.of(new Ingredient("cranberries", 1), new Ingredient("apple", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("bruschetta", "意式烤面包", List.of(new Ingredient("bread", 1), new Ingredient("any_oil", 1), new Ingredient("tomato", 1))));
        allDishes.add(new DishRecipe("coleslaw", "卷心菜沙拉", List.of(new Ingredient("redcabbage", 1), new Ingredient("vinegar", 1), new Ingredient("mayonnaise", 1))));
        allDishes.add(new DishRecipe("fiddlehead_risotto", "意式蕨菜炖饭", List.of(new Ingredient("any_oil", 1), new Ingredient("caiji_fiddleheadfern", 1), new Ingredient("garlic", 1))));
        allDishes.add(new DishRecipe("poppyseed_muffin", "虞美人籽松糕", List.of(new Ingredient("poppy", 1), new Ingredient("wheat_flour", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("chowder", "海鲜杂烩汤", List.of(new Ingredient("caiji_clam", 1), new Ingredient("any_milk", 1))));
        allDishes.add(new DishRecipe("lobster_bisque", "龙虾浓汤", List.of(new Ingredient("fish_lobster", 1), new Ingredient("any_milk", 1))));
        allDishes.add(new DishRecipe("escargot", "法式田螺", List.of(new Ingredient("fish_snail", 1), new Ingredient("garlic", 1))));

        // ====== Page 2 (11 dishes) ======
        allDishes.add(new DishRecipe("fish_stew", "烩鱼汤", List.of(new Ingredient("fish_crayfish", 1), new Ingredient("caiji_mussel", 1), new Ingredient("fish_periwinkle", 1), new Ingredient("tomato", 1))));
        allDishes.add(new DishRecipe("maple_bar", "枫糖棒", List.of(new Ingredient("maple_syrup", 1), new Ingredient("sugar", 1), new Ingredient("wheat_flour", 1))));
        allDishes.add(new DishRecipe("crab_cakes", "蟹黄糕", List.of(new Ingredient("fish_crab", 1), new Ingredient("wheat_flour", 1), new Ingredient("any_egg", 1), new Ingredient("any_oil", 1))));
        allDishes.add(new DishRecipe("shrimp_cocktail", "虾鸡尾酒", List.of(new Ingredient("tomato", 1), new Ingredient("fish_shrimp", 1), new Ingredient("caiji_wildhorseradish", 1))));
        allDishes.add(new DishRecipe("ginger_ale", "姜汁汽水", List.of(new Ingredient("caiji_ginger", 3), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("banana_pudding", "香蕉布丁", List.of(new Ingredient("banana", 1), new Ingredient("any_milk", 1), new Ingredient("sugar", 1))));
        allDishes.add(new DishRecipe("mango_sticky_rice", "芒果糯米饭", List.of(new Ingredient("mango", 1), new Ingredient("caiji_coconut", 1), new Ingredient("rice", 1))));
        allDishes.add(new DishRecipe("poi", "夏威夷芋泥", List.of(new Ingredient("taroroot", 4))));
        allDishes.add(new DishRecipe("tropical_curry", "热带咖喱", List.of(new Ingredient("caiji_coconut", 1), new Ingredient("pineapple", 1), new Ingredient("hotpepper", 1))));
        allDishes.add(new DishRecipe("squid_ink_ravioli", "墨汁意大利饺", List.of(new Ingredient("squid_ink", 1), new Ingredient("wheat_flour", 1), new Ingredient("tomato", 1))));

        allDishes.add(new DishRecipe("moss_soup", "苔藓汤", List.of(new Ingredient("moss", 20))));
    }

    @Override
    protected void init() {
        texX = (this.width - TEX_W) / 2;
        texY = 30;
        invGridX = (this.width - 9 * SLOT_SIZE) / 2;
        invGridY = texY + TEX_H + 5;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        context.drawText(textRenderer, Text.literal("烹饪"), (this.width - textRenderer.getWidth("烹饪")) / 2, 10, 0xFFFFFF, false);

        if (currentPage == 0) {
            renderPage(context, DISHES1_TEXTURE, P1_OX, P1_OY, P1_W, P1_H, P1_COLS, P1_ROWS,
                0, P1_COLS * P1_ROWS);
        } else {
            renderPage(context, DISHES2_TEXTURE, P2_OX, P2_OY, P2_W, P2_H, P2_COLS, P2_ROWS,
                P1_COLS * P1_ROWS, allDishes.size());
        }

        // 翻页按钮
        if (currentPage == 0) {
            int bx = texX + NEXT_BTN_OX;
            int by = texY + NEXT_BTN_OY;
            context.drawText(textRenderer, Text.literal(">"), bx + 2, by + 1, 0xFFFFFF, false);
        } else {
            int bx = texX + PREV_BTN_OX;
            int by = texY + PREV_BTN_OY;
            context.drawText(textRenderer, Text.literal("<"), bx + 2, by + 1, 0xFFFFFF, false);
        }

        renderInventory(context);
        renderTooltip(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderPage(DrawContext context, Identifier texture, int areaOx, int areaOy, int areaW, int areaH,
                            int cols, int rows, int startIndex, int endIndex) {
        context.drawTexture(RenderPipelines.GUI_TEXTURED, texture, texX, texY, 0, 0, TEX_W, TEX_H, TEX_W, TEX_H);
    }

    private void renderInventory(DrawContext context) {
        context.drawText(textRenderer, Text.literal("背包"), invGridX, invGridY - 12, 0xFFFFFF, false);

        for (int slot = 0; slot < 36; slot++) {
            int col = slot % 9;
            int row = slot / 9;
            int sx = invGridX + col * SLOT_SIZE;
            int sy = invGridY + row * SLOT_SIZE;

            context.fill(sx, sy, sx + SLOT_SIZE, sy + SLOT_SIZE, 0xFF8B8B8B);
            context.fill(sx + 1, sy + 1, sx + SLOT_SIZE - 1, sy + SLOT_SIZE - 1, 0xFFC6C6C6);

            int invIndex = slot < 9 ? slot + 27 : slot - 9;
            ItemStack stack = client != null && client.player != null
                ? client.player.getInventory().getStack(invIndex)
                : ItemStack.EMPTY;
            if (!stack.isEmpty()) {
                context.drawItem(stack, sx + 1, sy + 1);
                context.drawStackOverlay(textRenderer, stack, sx + 1, sy + 1);
            }
        }
    }

    private void renderTooltip(DrawContext context, int mouseX, int mouseY) {
        DishRecipe dish = getDishAt(mouseX, mouseY);
        if (dish == null) return;

        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal(dish.displayName));
        lines.add(Text.literal(""));

        for (Ingredient ing : dish.ingredients) {
            int pc = CraftingMaterialSets.countItemInInventory(ing.itemId, client);
            String ingName;
            Item ingItem = CraftingMaterialSets.getItemForDisplay(ing.itemId);
            if (ingItem != null) {
                if (CraftingMaterialSets.getTagSet(ing.itemId) != null) {
                    String tagName = CraftingMaterialSets.getTagDisplayName(ing.itemId);
                    ingName = tagName != null ? tagName : "任意" + ingItem.getName().getString();
                } else {
                    ingName = ingItem.getName().getString();
                }
            } else {
                ingName = ing.itemId;
            }
            lines.add(Text.literal("  " + ingName + " x" + ing.count + " (你有: " + pc + ")"));
        }

        context.drawTooltip(textRenderer, lines, mouseX, mouseY);
    }

    private DishRecipe getDishAt(int mx, int my) {
        if (currentPage == 0) {
            return getDishInArea(mx, my, texX + P1_OX, texY + P1_OY, P1_W, P1_H, P1_COLS, P1_ROWS, 0, 70);
        } else {
            return getDishInArea(mx, my, texX + P2_OX, texY + P2_OY, P2_W, P2_H, P2_COLS, P2_ROWS, 70, 81);
        }
    }

    private DishRecipe getDishInArea(int mx, int my, int areaX, int areaY, int areaW, int areaH,
                                     int cols, int rows, int startIndex, int endIndex) {
        if (mx < areaX || mx >= areaX + areaW || my < areaY || my >= areaY + areaH) return null;
        int gap = 2;
        double cellW = (double) (areaW - gap * (cols - 1)) / cols;
        double cellH = (double) (areaH - gap * (rows - 1)) / rows;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                double cx = areaX + col * (cellW + gap);
                double cy = areaY + row * (cellH + gap);
                if (mx >= cx && mx < cx + cellW && my >= cy && my < cy + cellH) {
                    // 第二页：第二行只有第1列有菜品
                    if (currentPage == 1 && row == 1 && col > 0) return null;
                    int index = startIndex + row * cols + col;
                    if (index >= startIndex && index < endIndex && index < allDishes.size()) {
                        return allDishes.get(index);
                    }
                    return null;
                }
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mx = click.x();
        double my = click.y();

        // 返回按钮（右上角）
        if (mx >= this.width - 55 && mx < this.width - 5 && my >= 5 && my < 25) {
            if (client != null) client.setScreen(new MainGuiScreen());
            return true;
        }

        // 翻页按钮
        if (currentPage == 0) {
            int bx = texX + NEXT_BTN_OX;
            int by = texY + NEXT_BTN_OY;
            if (mx >= bx && mx < bx + NEXT_BTN_W && my >= by && my < by + NEXT_BTN_H) {
                currentPage = 1;
                return true;
            }
        }
        if (currentPage == 1) {
            int bx = texX + PREV_BTN_OX;
            int by = texY + PREV_BTN_OY;
            if (mx >= bx && mx < bx + PREV_BTN_W && my >= by && my < by + PREV_BTN_H) {
                currentPage = 0;
                return true;
            }
        }

        // 点击菜品
        DishRecipe dish = getDishAt((int) mx, (int) my);
        if (dish != null && hasEnoughMaterials(dish)) {
            int craftCount = getCraftCount();
            if (craftCount > 0) {
                ClientPlayNetworking.send(new ModPayloads.WorkbenchCraftC2SPayload(dish.resultItemId, craftCount));
            }
            return true;
        }

        return false;
    }

    private int getCraftCount() {
        boolean shift = isShiftDown();
        boolean ctrl = isCtrlDown();
        if (shift && ctrl) return 999;
        if (ctrl) return 25;
        if (shift) return 5;
        return 1;
    }

    private boolean isShiftDown() {
        return GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private boolean isCtrlDown() {
        return GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
            || GLFW.glfwGetKey(GLFW.glfwGetCurrentContext(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private boolean hasEnoughMaterials(DishRecipe dish) {
        for (Ingredient ing : dish.ingredients) {
            if (CraftingMaterialSets.countItemInInventory(ing.itemId, client) < ing.count) return false;
        }
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
