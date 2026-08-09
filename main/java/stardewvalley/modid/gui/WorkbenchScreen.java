package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.crafting.CraftingMaterialSets;
import stardewvalley.modid.gui.ClockHudRenderer;

import java.util.ArrayList;
import java.util.List;

public class WorkbenchScreen extends Screen {

    private static final Identifier WORKBENCH_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/workbench.png");

    private static final int RECIPE_COLS = 6;
    private static final int CELL_W = 40;
    private static final int CELL_H = 40;
    private static final int SLOT_SIZE = 18;
    private static final int GRID_Y = 26;
    private static final int INGREDIENT_COLS = 2;
    private static final int INGREDIENT_SPACING = 18;

    private int invGridX;
    private int invGridY;
    private int gridStartX;
    private int scrollOffset = 0;

    private final List<CraftRecipe> recipes = new ArrayList<>();

    private static class Ingredient {
        final String itemId;
        final int count;
        Ingredient(String itemId, int count) {
            this.itemId = itemId;
            this.count = count;
        }
    }

    private static class CraftRecipe {
        final String resultItemId;
        final String displayName;
        final List<Ingredient> ingredients;
        final boolean hasTodo;

        CraftRecipe(String resultItemId, String displayName, List<Ingredient> ingredients, boolean hasTodo) {
            this.resultItemId = resultItemId;
            this.displayName = displayName;
            this.ingredients = ingredients;
            this.hasTodo = hasTodo;
        }
    }

    public WorkbenchScreen() {
        super(Text.translatable("gui.stardewvalley.workbench.title"));
        initRecipes();
    }

    private void initRecipes() {
        // ====== 金属锭转换配方 ======
        recipes.add(new CraftRecipe("iron_bar", "铁锭", List.of(
            new Ingredient("copper_bar", 3)
        ), false));
        recipes.add(new CraftRecipe("gold_bar", "金锭", List.of(
            new Ingredient("iron_bar", 2)
        ), false));

        recipes.add(new CraftRecipe("keg", "小桶", List.of(
            new Ingredient("wood", 30),
            new Ingredient("copper_bar", 1),
            new Ingredient("iron_bar", 1),
            new Ingredient("oak_resin", 1)
        ), false));

        recipes.add(new CraftRecipe("cheese_press", "压酪机", List.of(
            new Ingredient("wood", 45),
            new Ingredient("misc_stone", 45),
            new Ingredient("hardwood", 10),
            new Ingredient("copper_bar", 1)
        ), false));

        recipes.add(new CraftRecipe("mayonnaise_machine", "蛋黄酱机", List.of(
            new Ingredient("wood", 15),
            new Ingredient("misc_stone", 15),
            new Ingredient("earth_crystal", 1),
            new Ingredient("copper_bar", 1)
        ), false));

        recipes.add(new CraftRecipe("preserves_jar", "罐头瓶", List.of(
            new Ingredient("wood", 50),
            new Ingredient("misc_stone", 40),
            new Ingredient("coal", 8)
        ), false));

        recipes.add(new CraftRecipe("bee_house", "蜂房", List.of(
            new Ingredient("wood", 40),
            new Ingredient("coal", 8),
            new Ingredient("iron_bar", 1),
            new Ingredient("maple_syrup", 1)
        ), false));

        recipes.add(new CraftRecipe("oil_maker", "产油机", List.of(
            new Ingredient("slime", 50),
            new Ingredient("hardwood", 20),
            new Ingredient("gold_bar", 1)
        ), false));

        recipes.add(new CraftRecipe("cask", "木桶", List.of(
            new Ingredient("wood", 20),
            new Ingredient("hardwood", 1)
        ), false));

        recipes.add(new CraftRecipe("dehydrator", "烘干机", List.of(
            new Ingredient("wood", 30),
            new Ingredient("clay", 2),
            new Ingredient("fire_quartz", 1)
        ), false));

        recipes.add(new CraftRecipe("loom", "织布机", List.of(
            new Ingredient("wood", 60),
            new Ingredient("fiber", 30),
            new Ingredient("pine_tar", 1)
        ), false));

        recipes.add(new CraftRecipe("fish_smoker", "熏鱼机", List.of(
            new Ingredient("fish_sea_jelly", 1),
            new Ingredient("fish_river_jelly", 1),
            new Ingredient("fish_cave_jelly", 1),
            new Ingredient("hardwood", 10)
        ), false));

        recipes.add(new CraftRecipe("furnace", "熔炉", List.of(
            new Ingredient("misc_stone", 25),
            new Ingredient("copper_ore", 25)
        ), false));

        // ====== 精炼设备配方 ======
        recipes.add(new CraftRecipe("charcoal_kiln", "煤炭窑", List.of(
            new Ingredient("wood", 20),
            new Ingredient("copper_bar", 2)
        ), false));
        // 捕猎者天赋减少蟹笼材料：40木材2铁锭→20木材2铜锭
        String fishing5 = ClockHudRenderer.getClientSkill(stardewvalley.modid.skill.SkillRegistry.Category.FISHING, 1);
        boolean hasTapper = "tapper".equals(fishing5);
        recipes.add(new CraftRecipe("crab_pot", "蟹笼", hasTapper ? List.of(
            new Ingredient("wood", 20),
            new Ingredient("copper_bar", 2)
        ) : List.of(
            new Ingredient("wood", 40),
            new Ingredient("iron_bar", 2)
        ), false));
        recipes.add(new CraftRecipe("crystalarium", "宝石复制机", List.of(
            new Ingredient("misc_stone", 99),
            new Ingredient("gold_bar", 5),
            new Ingredient("iridium_bar", 2),
            new Ingredient("battery_pack", 1)
        ), false));
        recipes.add(new CraftRecipe("garden_pot", "花盆", List.of(
            new Ingredient("clay", 2),
            new Ingredient("misc_stone", 10),
            new Ingredient("refined_quartz", 1)
        ), false));
        recipes.add(new CraftRecipe("mushroom_log", "蘑菇树桩", List.of(
            new Ingredient("hardwood", 10),
            new Ingredient("moss", 10)
        ), false));
        recipes.add(new CraftRecipe("deluxe_worm_bin", "豪华虫饵盒", List.of(
            new Ingredient("hardwood", 10),
            new Ingredient("gold_bar", 1),
            new Ingredient("iron_bar", 1),
            new Ingredient("fiber", 50),
            new Ingredient("moss", 30)
        ), false));

        // ====== 树液采集器与避雷针配方 ======
        recipes.add(new CraftRecipe("tapper", "树液采集器", List.of(
            new Ingredient("wood", 40),
            new Ingredient("copper_bar", 2)
        ), false));
        recipes.add(new CraftRecipe("heavy_tapper", "重型树液采集器", List.of(
            new Ingredient("hardwood", 30),
            new Ingredient("radioactive_bar", 1)
        ), false));
        recipes.add(new CraftRecipe("lightning_rod", "避雷针", List.of(
            new Ingredient("iron_bar", 1),
            new Ingredient("refined_quartz", 1),
            new Ingredient("bat_wing", 5)
        ), false));

        // 肥料配方
        recipes.add(new CraftRecipe("basic_fertilizer", "初级肥料", List.of(
            new Ingredient("caiji_sap", 2)
        ), false));
        recipes.add(new CraftRecipe("quality_fertilizer", "高级肥料×2", List.of(
            new Ingredient("caiji_sap", 4),
            new Ingredient("any_fish", 1)
        ), false));
        recipes.add(new CraftRecipe("deluxe_fertilizer", "顶级肥料×5", List.of(
            new Ingredient("iridium_bar", 1),
            new Ingredient("caiji_sap", 5)
        ), false));
        recipes.add(new CraftRecipe("basic_retaining_soil", "初级保湿土壤", List.of(
            new Ingredient("misc_stone", 2)
        ), false));
        recipes.add(new CraftRecipe("quality_retaining_soil", "高级保湿土壤×2", List.of(
            new Ingredient("misc_stone", 3),
            new Ingredient("clay", 1)
        ), false));
        recipes.add(new CraftRecipe("deluxe_retaining_soil", "顶级保湿土壤×2", List.of(
            new Ingredient("misc_stone", 5),
            new Ingredient("fiber", 3),
            new Ingredient("clay", 1)
        ), false));
        recipes.add(new CraftRecipe("speed-gro", "生长激素×5", List.of(
            new Ingredient("pine_tar", 1),
            new Ingredient("moss", 5)
        ), false));
        recipes.add(new CraftRecipe("deluxe_speed-gro", "高级生长激素×5", List.of(
            new Ingredient("oak_resin", 1),
            new Ingredient("bone_fragment", 5)
        ), false));
        recipes.add(new CraftRecipe("hyper_speed-gro", "顶级生长激素×5", List.of(
            new Ingredient("radioactive_ore", 1),
            new Ingredient("bone_fragment", 3),
            new Ingredient("solar_essence", 1)
        ), false));

        // ====== 渔具配方 ======
        recipes.add(new CraftRecipe("fishtool_spinner", "旋式鱼饵", List.of(
            new Ingredient("iron_bar", 2)
        ), false));
        recipes.add(new CraftRecipe("fishtool_dressed_spinner", "精装旋式鱼饵", List.of(
            new Ingredient("iron_bar", 2),
            new Ingredient("cloth", 1)
        ), false));
        recipes.add(new CraftRecipe("fishtool_trap_bobber", "陷阱浮标", List.of(
            new Ingredient("copper_bar", 1),
            new Ingredient("caiji_sap", 10)
        ), false));
        recipes.add(new CraftRecipe("fishtool_cork_bobber", "软木塞浮标", List.of(
            new Ingredient("wood", 10),
            new Ingredient("hardwood", 5),
            new Ingredient("slime", 10)
        ), false));
        recipes.add(new CraftRecipe("fishtool_treasure_hunter", "寻宝者", List.of(
            new Ingredient("gold_bar", 2)
        ), false));
        recipes.add(new CraftRecipe("fishtool_barbed_hook", "倒刺钩", List.of(
            new Ingredient("copper_bar", 1),
            new Ingredient("iron_bar", 1),
            new Ingredient("gold_bar", 1)
        ), false));
        recipes.add(new CraftRecipe("fishtool_quality_bobber", "优质浮标", List.of(
            new Ingredient("copper_bar", 1),
            new Ingredient("caiji_sap", 20)
        ), false));
        recipes.add(new CraftRecipe("fishtool_sonar_bobber", "声纳浮漂", List.of(
            new Ingredient("iron_bar", 1),
            new Ingredient("refined_quartz", 2)
        ), false));

        // ====== 鱼饵配方 ======
        recipes.add(new CraftRecipe("bait_bait", "鱼饵×5", List.of(
            new Ingredient("bug_meat", 1)
        ), false));
        recipes.add(new CraftRecipe("bait_magnet", "磁铁×3", List.of(
            new Ingredient("iron_bar", 1)
        ), false));
        recipes.add(new CraftRecipe("bait_wild_bait", "万能鱼饵×5", List.of(
            new Ingredient("fiber", 10),
            new Ingredient("slime", 5),
            new Ingredient("bug_meat", 5)
        ), false));
        recipes.add(new CraftRecipe("bait_magic_bait", "魔法鱼饵×5", List.of(
            new Ingredient("radioactive_ore", 1),
            new Ingredient("bug_meat", 3)
        ), false));
        recipes.add(new CraftRecipe("bait_deluxe_bait", "高级鱼饵×5", List.of(
            new Ingredient("bait_bait", 5),
            new Ingredient("moss", 2)
        ), false));
        recipes.add(new CraftRecipe("bait_challenge_bait", "挑战鱼饵×5", List.of(
            new Ingredient("bone_fragment", 5),
            new Ingredient("moss", 2)
        ), false));

        // ====== 精炼物品配方 ======
        recipes.add(new CraftRecipe("field_snack", "工作小食", List.of(
            new Ingredient("acorn", 1),
            new Ingredient("maple_seed", 1),
            new Ingredient("pine_cone", 1)
        ), false));
        recipes.add(new CraftRecipe("bug_steak", "虫肉块", List.of(
            new Ingredient("bug_meat", 10)
        ), false));
        recipes.add(new CraftRecipe("life_elixir", "生命药水", List.of(
            new Ingredient("caiji_redmushroom", 1),
            new Ingredient("caiji_purplemushroom", 1),
            new Ingredient("caiji_morel", 1),
            new Ingredient("caiji_chanterelle", 1)
        ), false));
        recipes.add(new CraftRecipe("cherry_bomb", "樱桃炸弹", List.of(
            new Ingredient("copper_ore", 4),
            new Ingredient("coal", 1)
        ), false));
        recipes.add(new CraftRecipe("bomb", "炸弹", List.of(
            new Ingredient("iron_ore", 4),
            new Ingredient("coal", 1)
        ), false));
        recipes.add(new CraftRecipe("mega_bomb", "超级炸弹", List.of(
            new Ingredient("gold_ore", 4),
            new Ingredient("solar_essence", 1),
            new Ingredient("void_essence", 1)
        ), false));
        recipes.add(new CraftRecipe("explosive_ammo", "爆炸弹丸×5", List.of(
            new Ingredient("iron_bar", 1),
            new Ingredient("coal", 2)
        ), false));
        recipes.add(new CraftRecipe("fairy_dust", "仙尘", List.of(
            new Ingredient("diamond", 1),
            new Ingredient("fairyrose", 1)
        ), false));
        recipes.add(new CraftRecipe("monster_musk", "怪兽香水", List.of(
            new Ingredient("bat_wing", 30),
            new Ingredient("slime", 30)
        ), false));
        recipes.add(new CraftRecipe("rain_totem", "雨水图腾", List.of(
            new Ingredient("hardwood", 1),
            new Ingredient("truffle_oil", 1),
            new Ingredient("pine_tar", 5)
        ), false));
        recipes.add(new CraftRecipe("staircase", "楼梯", List.of(
            new Ingredient("misc_stone", 99)
        ), false));
        recipes.add(new CraftRecipe("sprinkler", "洒水器", List.of(
            new Ingredient("copper_bar", 1),
            new Ingredient("iron_bar", 1)
        ), false));
        recipes.add(new CraftRecipe("quality_sprinkler", "优质洒水器", List.of(
            new Ingredient("iron_bar", 1),
            new Ingredient("gold_bar", 1),
            new Ingredient("refined_quartz", 1)
        ), false));
        recipes.add(new CraftRecipe("iridium_sprinkler", "铱洒水器", List.of(
            new Ingredient("gold_bar", 1),
            new Ingredient("iridium_bar", 1),
            new Ingredient("battery_pack", 1)
        ), false));
        recipes.add(new CraftRecipe("warp_totem_beach", "传送图腾：海滩", List.of(
            new Ingredient("hardwood", 1),
            new Ingredient("caiji_coral", 2),
            new Ingredient("fiber", 10)
        ), false));
        recipes.add(new CraftRecipe("warp_totem_mountains", "传送图腾：山岭", List.of(
            new Ingredient("hardwood", 1),
            new Ingredient("iron_bar", 1),
            new Ingredient("misc_stone", 25)
        ), false));
        recipes.add(new CraftRecipe("warp_totem_farm", "传送图腾：农场", List.of(
            new Ingredient("hardwood", 1),
            new Ingredient("honey", 1),
            new Ingredient("fiber", 20)
        ), false));
        recipes.add(new CraftRecipe("warp_totem_desert", "传送图腾：沙漠", List.of(
            new Ingredient("hardwood", 2),
            new Ingredient("caiji_coconut", 1),
            new Ingredient("iridium_ore", 4)
        ), false));
        recipes.add(new CraftRecipe("warp_totem_island", "传送图腾：姜岛", List.of(
            new Ingredient("hardwood", 5),
            new Ingredient("dragon_tooth", 1),
            new Ingredient("caiji_ginger", 1)
        ), false));

        // ====== 戒指配方 ======
        recipes.add(new CraftRecipe("warrior_ring", "战士戒指", List.of(
            new Ingredient("iron_bar", 10),
            new Ingredient("coal", 25),
            new Ingredient("tear_crystal", 10)
        ), false));
        recipes.add(new CraftRecipe("ring_of_yoba", "由巴的戒指", List.of(
            new Ingredient("gold_bar", 5),
            new Ingredient("iron_bar", 5),
            new Ingredient("diamond", 1)
        ), false));
        recipes.add(new CraftRecipe("sturdy_ring", "结实戒指", List.of(
            new Ingredient("copper_bar", 2),
            new Ingredient("bug_meat", 25),
            new Ingredient("slime", 25)
        ), false));
        recipes.add(new CraftRecipe("iridium_band", "铱环", List.of(
            new Ingredient("iridium_bar", 5),
            new Ingredient("solar_essence", 50),
            new Ingredient("void_essence", 50)
        ), false));
        recipes.add(new CraftRecipe("thorns_ring", "荆棘戒指", List.of(
            new Ingredient("bone_fragment", 50),
            new Ingredient("misc_stone", 50),
            new Ingredient("gold_bar", 1)
        ), false));
        recipes.add(new CraftRecipe("glowstone_ring", "辉石戒指", List.of(
            new Ingredient("solar_essence", 5),
            new Ingredient("iron_bar", 5)
        ), false));

        // ====== 野生种子配方 ======
        recipes.add(new CraftRecipe("spring_seeds", "野生种子（春季）", List.of(
            new Ingredient("caiji_wildhorseradish", 1),
            new Ingredient("caiji_daffodil", 1),
            new Ingredient("caiji_leek", 1),
            new Ingredient("caiji_dandelion", 1)
        ), false));
        recipes.add(new CraftRecipe("summer_seeds", "野生种子（夏季）", List.of(
            new Ingredient("caiji_spiceberry", 1),
            new Ingredient("grape", 1),
            new Ingredient("caiji_sweetpea", 1)
        ), false));
        recipes.add(new CraftRecipe("fall_seeds", "野生种子（秋季）", List.of(
            new Ingredient("caiji_commonmushroom", 1),
            new Ingredient("caiji_wildplum", 1),
            new Ingredient("caiji_hazelnut", 1),
            new Ingredient("caiji_blackberry", 1)
        ), false));
        recipes.add(new CraftRecipe("winter_seeds", "野生种子（冬季）", List.of(
            new Ingredient("caiji_winterroot", 1),
            new Ingredient("caiji_crystalfruit", 1),
            new Ingredient("caiji_snowyam", 1),
            new Ingredient("caiji_crocus", 1)
        ), false));
        recipes.add(new CraftRecipe("ancientfruit_seeds", "上古种子", List.of(
            new Ingredient("ancient_seed", 1)
        ), false));
        recipes.add(new CraftRecipe("grass_starter", "草籽", List.of(
            new Ingredient("fiber", 10)
        ), false));
        recipes.add(new CraftRecipe("tealeaves_seeds", "茶苗", List.of(
            new Ingredient("any_wild_seed", 2),
            new Ingredient("fiber", 5),
            new Ingredient("wood", 5)
        ), false));

        recipes.add(new CraftRecipe("fiber_seeds", "纤维种子×4", List.of(
            new Ingredient("mixedseeds", 1),
            new Ingredient("caiji_sap", 5),
            new Ingredient("clay", 1)
        ), false));

        // ====== 宝石合成配方 ======
        recipes.add(new CraftRecipe("prismatic_shard", "五彩碎片", List.of(
            new Ingredient("aquamarine", 99),
            new Ingredient("emerald", 99),
            new Ingredient("topaz", 99),
            new Ingredient("ruby", 99),
            new Ingredient("amethyst", 99)
        ), false));
    }

    @Override
    protected void init() {
        invGridX = this.width - 9 * SLOT_SIZE;
        invGridY = (this.height - 4 * SLOT_SIZE) / 2;
        gridStartX = 8;

        addDrawableChild(ButtonWidget.builder(
            Text.literal("< 返回"),
            button -> { if (client != null) client.setScreen(new MainGuiScreen()); }
        ).dimensions(this.width - 55, 5, 50, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0x88000000);

        context.drawText(textRenderer, Text.literal("合成台"), 28, 12, 0xFFFFFF, false);

        renderRecipeGrid(context, mouseX, mouseY);
        renderInventory(context);
        renderTooltip(context, mouseX, mouseY);

        super.render(context, mouseX, mouseY, delta);
    }

    private void renderRecipeGrid(DrawContext context, int mouseX, int mouseY) {
        int clipTop = GRID_Y;
        int clipBottom = this.height;
        context.enableScissor(0, clipTop, width, clipBottom - clipTop);

        for (int i = 0; i < recipes.size(); i++) {
            CraftRecipe recipe = recipes.get(i);
            int row = i / RECIPE_COLS;
            int col = i % RECIPE_COLS;
            int x = gridStartX + col * CELL_W;
            int y = GRID_Y + row * CELL_H - scrollOffset;

            if (y + CELL_H < clipTop || y > clipBottom) continue;

            boolean canCraft = !recipe.hasTodo && hasEnoughMaterials(recipe);

            int bgColor = recipe.hasTodo ? 0xFF442222 : (canCraft ? 0xFF333333 : 0xFF222222);
            int borderColor = recipe.hasTodo ? 0xFF884444 : (canCraft ? 0xFF448844 : 0xFF333333);
            context.fill(x, y, x + CELL_W, y + CELL_H, bgColor);
            context.fill(x + 1, y + 1, x + CELL_W - 1, y + CELL_H - 1, borderColor);

            // 结果物品图标
            Item resultItem = Registries.ITEM.get(Identifier.of(StardewValley.MOD_ID, recipe.resultItemId));
            if (resultItem != null) {
                context.drawItem(new ItemStack(resultItem), x + 2, y + 1);
            }

            // 中文名称
            Text nameText = Text.literal(recipe.displayName);
            context.drawText(textRenderer, nameText, x + 18, y + 1, canCraft ? 0xFFFFFF : 0x888888, false);

            if (recipe.hasTodo) {
                context.drawText(textRenderer, Text.literal("[TODO]"), x + 18, y + 10, 0xFFAA44, false);
            }

            // 材料（分两列排布）
            int ingredientStartY = y + 17;
            for (int j = 0; j < recipe.ingredients.size(); j++) {
                Ingredient ing = recipe.ingredients.get(j);
                int ingCol = j % INGREDIENT_COLS;
                int ingRow = j / INGREDIENT_COLS;
                int ix = x + 2 + ingCol * INGREDIENT_SPACING;
                int iy = ingredientStartY + ingRow * 9;

                int playerCount = CraftingMaterialSets.countItemInInventory(ing.itemId, client);
                Item ingItem = CraftingMaterialSets.getItemForDisplay(ing.itemId);

                if (ingItem != null) {
                    var matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(ix + 4, iy + 4);
                    matrices.scale(0.6f, 0.6f);
                    context.drawItem(new ItemStack(ingItem), -4, -4);
                    matrices.popMatrix();
                }

                String countStr = playerCount + "/" + ing.count;
                int color = playerCount >= ing.count ? 0x88FF88 : 0xFF8888;
                context.drawText(textRenderer, Text.literal(countStr), ix + 10, iy + 1, color, false);
            }
        }
        context.disableScissor();
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
        for (int i = 0; i < recipes.size(); i++) {
            CraftRecipe recipe = recipes.get(i);
            int row = i / RECIPE_COLS;
            int col = i % RECIPE_COLS;
            int x = gridStartX + col * CELL_W;
            int y = GRID_Y + row * CELL_H - scrollOffset;

            if (mouseX >= x && mouseX < x + CELL_W && mouseY >= y && mouseY < y + CELL_H) {
                List<Text> lines = new ArrayList<>();
                lines.add(Text.literal(recipe.displayName));
                lines.add(Text.literal(""));
                for (Ingredient ing : recipe.ingredients) {
                    int pc = CraftingMaterialSets.countItemInInventory(ing.itemId, client);
                    String ingName;
                    if (ing.itemId.contains(":")) {
                        Item ingItem = Registries.ITEM.get(Identifier.of(ing.itemId));
                        ingName = ingItem != null ? ingItem.getName().getString() : ing.itemId;
                    } else if (ing.itemId.matches("[a-z0-9/._-]+")) {
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
                    } else {
                        ingName = ing.itemId;
                    }
                    lines.add(Text.literal("  " + ingName + " x" + ing.count + " (你有: " + pc + ")"));
                }
                if (recipe.hasTodo) {
                    lines.add(Text.literal("  [TODO] 缺少必要物品"));
                }
                context.drawTooltip(textRenderer, lines, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;

        double mx = click.x();
        double my = click.y();

        // 返回按钮
        if (mx >= width - 60 && mx < width - 5 && my >= 5 && my < 25) {
            if (client != null) client.setScreen(new MainGuiScreen());
            return true;
        }

        for (int i = 0; i < recipes.size(); i++) {
            CraftRecipe recipe = recipes.get(i);
            if (recipe.hasTodo) continue;
            if (!hasEnoughMaterials(recipe)) continue;

            int row = i / RECIPE_COLS;
            int col = i % RECIPE_COLS;
            int x = gridStartX + col * CELL_W;
            int y = GRID_Y + row * CELL_H - scrollOffset;

            if (mx >= x && mx < x + CELL_W && my >= y && my < y + CELL_H) {
                int craftCount = getCraftCount(recipe);
                if (craftCount > 0) {
                    ClientPlayNetworking.send(new ModPayloads.WorkbenchCraftC2SPayload(recipe.resultItemId, craftCount));
                }
                return true;
            }
        }
        return false;
    }

    private int getCraftCount(CraftRecipe recipe) {
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

    private boolean hasEnoughMaterials(CraftRecipe recipe) {
        for (Ingredient ing : recipe.ingredients) {
            if (CraftingMaterialSets.countItemInInventory(ing.itemId, client) < ing.count) return false;
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int maxScroll = Math.max(0, (int) Math.ceil((double) recipes.size() / RECIPE_COLS) * CELL_H - (this.height - GRID_Y - 30));
        scrollOffset = (int) Math.max(0, Math.min(scrollOffset - verticalAmount * 8, maxScroll));
        return true;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
