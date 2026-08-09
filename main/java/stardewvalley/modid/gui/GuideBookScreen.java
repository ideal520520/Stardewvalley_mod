package stardewvalley.modid.gui;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class GuideBookScreen extends Screen {

    private static final int LEFT_PANEL_W = 110;
    private static final int TOP_OFFSET = 20;
    private static final int CATEGORY_GAP = 22;
    private static final int SKILL_CATEGORY_INDEX = 15;

    private int selectedCategory = 0;
    private int scrollOffset = 0;
    private int contentScroll = 0;
    private final List<CategoryEntry> categories = new ArrayList<>();

    private record CategoryEntry(String name, String content) {}

    public GuideBookScreen() {
        super(Text.literal("作者的话"));
        initCategories();
    }

    private void initCategories() {
        categories.add(new CategoryEntry("关于本模组",
            "§l星露谷物语 Mod\n\n" +
            "§7这是一个将星露谷物语的核心玩法带入Minecraft的模组，极大还原了星露谷的耕种、采集、钓鱼、挖矿、战斗五大技能体系。\n\n" +
            "§7你可以在Minecraft中体验到：\n" +
            "§7- 30+种星露谷作物，需要浇水、施肥、换季种植\n" +
            "§7- 70+种鱼类，还原星露谷独特的钓鱼小游戏\n" +
            "§7- 70+道菜肴，拥有独特的食物效果\n" +
            "§7- 数百种工匠制品（果酒、果酱、奶酪等）\n" +
            "§7- 完整的技能等级和天赋系统\n" +
            "§7- 四季变换的天气与季节系统\n" +
            "§7- 14个NPC商店，各有特色商品\n" +
            "§7- 收集包和博物馆捐赠系统\n\n" +
            "§7获得初始工具和作物：§b/stardewvalley initialitem\n\n" +
            "§7Mod作者：ideal520\n" +
            "§8感谢kltyton（钓鱼小游戏）、Weatheraintbad（HUD）的开源代码贡献。"
        ));

        categories.add(new CategoryEntry("动物制品",
            "§l动物制品\n\n" +
            "§7本模组为原版动物添加了星露谷风格的产出机制：\n\n" +
            "§b鸡§7：各种鸡蛋均由原版的鸡产出，原版鸡下的蛋被替换为星露谷的各种蛋（普通蛋、大蛋等）。鸡舍大师天赋可使双倍产蛋。\n\n" +
            "§b牛§7：牛奶为使用星露谷桶对准原版的牛右键接奶得到。\n\n" +
            "§b山羊§7：羊奶为使用星露谷桶对准原版的山羊右键接奶得到。\n\n" +
            "§b羊§7：动物毛为使用星露谷剪刀对准原版的羊右键获得。牧羊人天赋可使羊毛翻倍。\n\n" +
            "§b猪§7：松露为给原版的猪添加了像鸡下蛋一样的类似功能，产物为松露。\n\n" +
            "§b兔子§7：兔子的脚为给原版的兔子添加了产出产品的功能。\n\n" +
            "§8动物制品的品质等由运气和动物好感度等决定。\n\n" +
            "§7动物可以在玛妮的牧场购买（总GUI→玛妮图标）。\n\n" +
            "§7使用金色动物饼干可以永久使对应动物的产量翻倍。"
        ));

        categories.add(new CategoryEntry("工匠物品",
            "§l工匠物品 & 设备\n\n" +
            "§7绝大部分物品都遵从星露谷原版机制，可由对应采集品或作物等用设备加工得到。\n\n" +
            "§l主要设备：\n" +
            "§b小桶§7：将水果发酵成果酒，蔬菜酿成果汁\n" +
            "§b罐头瓶§7：将水果制成酱，蔬菜制成腌菜\n" +
            "§b压酪机§7：将牛奶制成奶酪，羊奶制成山羊奶酪\n" +
            "§b蛋黄酱机§7：将鸡蛋制成蛋黄酱（普通/鸭/虚空/恐龙/金）\n" +
            "§b织布机§7：将动物毛制成布\n" +
            "§b蜂房§7：放置在花附近产出对应花蜜\n" +
            "§b产油机§7：将松露制成松露油，种子制成油\n" +
            "§b脱水机§7：将蘑菇、水果制成干制品\n" +
            "§b熏鱼机§7：将鱼熏制成熏鱼\n" +
            "§b熔炉§7：熔炼金属\n\n" +
            "§8所有工匠制品均有银/金/铱品质版本，可通过木桶陈酿提升品质。"
        ));

        categories.add(new CategoryEntry("采集品",
            "§l采集品\n\n" +
            "§7大部分采集品为破坏对应合理方块概率掉落获得，概率受运气影响。\n\n" +
            "§b春季§7：辣根、黄水仙、韭葱、蒲公英、羊肚菌、美洲大树莓\n\n" +
            "§b夏季§7：葡萄、香味浆果、甜豌豆、蕨菜、热带风格水果\n\n" +
            "§b秋季§7：普通蘑菇、野梅、榛子、黑莓、鸡油菌、红蘑菇、紫蘑菇\n\n" +
            "§b冬季§7：番红花、冬根、雪山药、水晶果\n\n" +
            "§b海滩§7：鹦鹉螺壳、珊瑚、海胆、彩虹贝壳、蛤蜊、鸟蛤、贻贝、牡蛎\n\n" +
            "§b其他§7：山洞萝卜、椰子、生姜、岩浆菇、苔藓\n\n" +
            "§7使用野生种子可以在耕地上种植季节对应的采集品。\n\n" +
            "§8收集者天赋33%概率双倍采集，植物学家天赋使采集品获得铱星品质。"
        ));

        categories.add(new CategoryEntry("作物系统",
            "§l作物系统\n\n" +
            "§7星露谷作物只能种植在由星露谷锄头耕出来的耕地上。\n\n" +
            "§7极大还原了星露谷的作物生长收获等特点，而非原版的随机刻生长模式。\n\n" +
            "§7生长需要浇水，不支持水源方块湿润耕地。\n\n" +
            "§7支持肥料、生长激素与保湿土。\n\n" +
            "§7支持原版的骨粉可加速作物生长，且不需要作物所在方块强加载。\n\n" +
            "§8水果仅添加了物品，果树功能尚未实现，敬请期待。\n\n" +
            "§8作物收获的数量与品质受运气和耕种等级影响。\n\n" +
            "§7全模组共有40+种作物，每季有不同的可种植作物。\n" +
            "§7作物有银/金/铱三种品质等级，售价随品质提升（×1.25/×1.5/×2.0）。\n\n" +
            "§l肥料效果：\n" +
            "§7基础/优质/顶级肥料：提升高品质作物概率\n" +
            "§7基础/优质/顶级保湿土壤：保持耕地水分\n" +
            "§7生长激素/顶级/超级：加速作物生长\n\n" +
            "§7花盆上永远可以种植一年四季的作物。"
        ));

        categories.add(new CategoryEntry("菜肴",
            "§l菜肴\n\n" +
            "§7极大的还原了星露谷的食物的特性并与我的世界实现了很强的融合。\n\n" +
            "§7菜肴的实现可见总GUI的dishworkbench。\n\n" +
            "§7本模组包含70+道菜肴，每道菜有独特的食用效果（恢复饥饿值、饱和值、给予状态效果等）。\n\n" +
            "§7需要先获取食谱才能解锁对应的菜肴制作。食谱可通过星露谷菜单栏的书摊老板购买、钓鱼宝藏、怪物掉落等方式获得。\n\n" +
            "§l部分菜品示例：\n" +
            "§7- 秋日丰收：恢复大量饥饿值\n" +
            "§7- 幸运午餐：给予幸运效果\n" +
            "§7- 超级佳肴：全属性提升\n" +
            "§7- 三倍浓缩咖啡：大幅提升速度\n" +
            "§7- 魔法冰糖：特殊甜点效果"
        ));

        categories.add(new CategoryEntry("鱼类",
            "§l鱼类\n\n" +
            "§7极大还原了星露谷极具特色的钓鱼小游戏（需要通过指令 §b/stardewvalley fishmod 2 §7更改）。\n\n" +
            "§7钓鱼小游戏期间：§bESC§7退出页面，§bC§7与§b空格§7与§b鼠标左键§7可使绿色判定条上升。\n\n" +
            "§7拿着鱼杆 §bShift+右键§7 可打开鱼杆GUI装载鱼饵和渔具。\n\n" +
            "§7本模组包含70种鱼类，分布在河流、湖泊、海洋、地下等不同水域。不同季节和天气可钓到的鱼也不同。\n\n" +
            "§l渔具：\n" +
            "§7- 陷阱浮标：减慢鱼逃跑速度\n" +
            "§7- 软木浮标：增加判定条大小\n" +
            "§7- 优质浮标：提升捕获鱼品质\n" +
            "§7- 宝藏猎人：钓鱼宝藏概率提升\n" +
            "§7- 声呐浮标：显示鱼的位置\n\n" +
            "§7不同点：鱼王在本mod可多次钓上（通过装载挑战鱼饵和对应鱼王的针对性鱼饵），但目前没有写钓上二代鱼王的功能。\n\n" +
            "§7在此感谢kltyton的开源代码对该功能的实现的极大贡献。"
        ));

        categories.add(new CategoryEntry("古物 & 杂项",
            "§l古物\n\n" +
            "§7可由各种方式获得并交付在博物馆GUI中获得奖励。\n\n" +
            "§7可捐赠古物包括：矮人卷轴I-IV、古代玩偶、古代鼓、黄金面具、史前工具、各类化石、陶瓷碎片等42种。\n\n" +
            "§7博物馆捐赠达到一定数量可获得特殊奖励：矮人安全手册、矮人翻译指南书、生锈钥匙等。\n\n" +
            "§l杂项\n\n" +
            "§7怪物战利品可由击败原版怪物获得，包括：虫肉、蝙蝠翅膀、史莱姆等。\n\n" +
            "§7可在冒险者公会（马龙商店）购买武器和装备。\n\n" +
            "§7击败怪物可获得战斗经验，提升战斗等级。"
        ));

        categories.add(new CategoryEntry("矿石 & 宝石",
            "§l矿石 & 宝石\n\n" +
            "§7矿石可由原版粗矿石1:1合成本模组矿石，或使用星露谷炸弹炸矿得到。\n\n" +
            "§l矿石（从低级到高级）：\n" +
            "§7煤、铜矿→铜锭、铁矿→铁锭、金矿→金锭、铱矿→铱锭、放射性矿→放射性锭\n\n" +
            "§l宝石：\n" +
            "§7紫水晶、海蓝宝石、钻石、绿宝石、翡翠、红宝石、黄玉、五彩碎片\n" +
            "§7地晶、火水晶、泪晶、石英→精炼石英\n\n" +
            "§7宝石在破坏各种石头的时候概率掉落，或使用星露谷炸弹炸石头获得。\n" +
            "§7宝石也可在晶球复制机中复制。\n\n" +
            "§l晶球系统：\n" +
            "§7普通晶球、冻结晶球、岩浆晶球、万象晶球\n" +
            "§7可在铁匠铺（克林特）破开，产出40+种矿物和古物。双倍晶球天赋可使产出翻倍。\n\n" +
            "§l炸弹：\n" +
            "§7樱桃炸弹（小范围）、普通炸弹（中范围）、超级炸弹（大范围）\n" +
            "§7炸弹引爆时掉落矿石和宝石（与原版TNT不同，不影响地形）。"
        ));

        categories.add(new CategoryEntry("精炼设备",
            "§l精炼设备\n\n" +
            "§7精炼物品的合成见 workbench GUI（总GUI右上角）。\n\n" +
            "§7绝大部分都几乎完全复现了星露谷原版机制和功能。\n\n" +
            "§8注：绝大部分设备都支持仙尘的跳过功能（仙尘可立即完成当前加工）。\n\n" +
            "§l树液采集器（Tapper/Heavy Tapper）：\n" +
            "§7需要放在两格高度原木方块上才能产出。\n" +
            "§7橡树树脂对应：橡木原木/深色橡木原木/苍白橡木原木或对应的橡木方块\n" +
            "§7松焦油对应：金合欢原木/云杉原木或对应的金合欢木/云杉木\n" +
            "§7枫糖浆对应：樱花原木/白桦原木或对应的樱花木/白桦木\n\n" +
            "§l其他设备：\n" +
            "§7避雷针：雷雨天吸收闪电产生电池组，概率受运气影响\n" +
            "§7太阳能板：晴天发电\n" +
            "§7种子生产器：将作物转化为种子\n" +
            "§7晶球破开器：自动破开晶球\n" +
            "§7回收机：将垃圾转化为有用材料\n" +
            "§7重型熔炉：一次熔炼5个锭\n" +
            "§7豪华虫饵盒：每日产出鱼饵\n" +
            "§7蘑菇树桩：产出蘑菇\n" +
            "§7洒水器/优质/铱：自动灌溉周围耕地\n" +
            "§7晶球复制机：复制放入的宝石"
        ));

        categories.add(new CategoryEntry("工具系统",
            "§l工具系统\n\n" +
            "§7星露谷的工具都拥有无限耐久。\n\n" +
            "§l工具等级（普通→铜→钢→金→铱）：\n\n" +
            "§b锄头§7：不同等级的锄头有不同耕地范围。\n" +
            "§7普通1格，铜1×3，钢1×5，金3×3，铱6×3（可调节）。\n\n" +
            "§b水壶§7：不同等级的水壶有不同洒水范围。\n" +
            "§7普通1格，铜1×3，钢1×5，金3×3，铱3×6。\n\n" +
            "§b镰刀§7：不同等级的镰刀有不同的收割范围。\n" +
            "§7铱镰刀右键可收获作物。\n" +
            "§7所有镰刀对准草左键收割为纤维，右键收割为干草。\n" +
            "§7干草可喂食动物增加好感度。\n\n" +
            "§b斧头§7：越高级砍树越快。\n\n" +
            "§b镐§7：越高级挖矿越快。\n\n" +
            "§7工具可在铁匠铺（克林特）升级。\n\n" +
            "§7获得初始工具：§b/stardewvalley initialitem"
        ));

        categories.add(new CategoryEntry("武器 & 装备",
            "§l武器 & 装备\n\n" +
            "§b武器技能系统：\n" +
            "§7拥有技能键，可在 选项→按键控制→按键绑定 中设置快捷键（默认R）。\n" +
            "§7剑：范围4格AOE伤害+击退\n" +
            "§7匕首：前方4格射线，伤害×3\n" +
            "§7棍棒：范围4格AOE，距离越近击退越强\n" +
            "§8技能冷却3秒，特技者天赋减半至1.5秒。\n\n" +
            "§b装备栏：\n" +
            "§7靴子和戒指可佩戴在生存模式的背包菜单栏的扩展装备栏中。\n" +
            "§7功能极大还原了星露谷戒指和靴子的功能。\n\n" +
            "§b戒指（29种）：\n" +
            "§7辉石戒指（发光）、磁铁戒指（吸引物品）、史莱姆克星、窃贼戒指、幸运戒指、荆棘戒指、凤凰戒指（防死）、免疫指环等。\n\n" +
            "§b靴子（18种）：\n" +
            "§7运动鞋、橡胶靴、皮靴、战斗靴、黑暗靴、烈焰行者、精灵鞋、龙鳞靴、水晶鞋等。\n\n" +
            "§b弹弓：\n" +
            "§7弹弓 §bShift+右键§7 可装填弹药到弹弓中。\n" +
            "§7弹药：石头、矿石、煤炭、爆炸弹药等，不同弹药伤害不同。"
        ));

        categories.add(new CategoryEntry("星露谷菜单",
            "§l星露谷菜单栏\n\n" +
            "§7按快捷键打开星露谷总GUI，包含以下功能：\n\n" +
            "§b【技能】§7耕种、采集、钓鱼、采矿、战斗，点击可查看等级和选择天赋。\n\n" +
            "§b【出货箱】§7出售物品获得金币。\n\n" +
            "§b【皮埃尔商店】§7购买种子、肥料等基础物品。\n\n" +
            "§b【铁匠铺（克林特）】§7购买矿石、煤炭，升级工具，破开晶球。\n\n" +
            "§b【鱼店（威利）】§7购买鱼杆、鱼饵、渔具。\n\n" +
            "§b【沙漠商人】§7——§8需完成金库收集包解锁，以物换物。\n\n" +
            "§b【矮人商店】§7——§8需获得矮人翻译指南书（博物馆奖励）。"
        ));

        categories.add(new CategoryEntry("菜单（续）",
            "§l星露谷菜单栏（续）\n\n" +
            "§b【哈维的诊所】§7购买食物、医疗用品。\n\n" +
            "§b【岛屿商人】§7——§8需要第三年才开放。\n\n" +
            "§b【冒险者公会（马龙）】§7购买武器、靴子、戒指，可用五彩碎片兑换银河剑。\n\n" +
            "§b【玛妮的牧场】§7购买动物（鸡、鸭、兔、羊、猪、牛、山羊、恐龙等）。\n\n" +
            "§b【罗宾的木匠铺】§7购买建材、家具。\n\n" +
            "§b【桑迪的绿洲】§7——§8需完成金库收集包解锁。\n\n" +
            "§b【科罗布斯下水道】§7——§8需获得生锈钥匙（博物馆奖励）。\n\n" +
            "§b【法师】§7购买魔法物品。\n\n" +
            "§b【格斯酒吧】§7购买食物、食谱。\n\n" +
            "§b【博物馆（冈瑟）】§7捐赠古物获得奖励。"
        ));

        categories.add(new CategoryEntry("特殊商贩 & 功能",
            "§l特殊商贩\n\n" +
            "§b【旅行货车】§7——§8周末开放（周五/周日），出售随机物品，包括稀有物品。\n\n" +
            "§b【书摊老板】§7——§8每周二开放，出售各种技能书和知识书籍。\n\n" +
            "§l功能面板\n\n" +
            "§b【工作台】§7合成精炼物品、工匠设备、肥料、鱼饵渔具等。\n\n" +
            "§b【烹饪锅】§7制作菜肴，需先获取食谱。\n\n" +
            "§b【收集包房间】§7完成收集包解锁对应奖励。\n" +
            "§7- 工艺室\n" +
            "§7- 茶水间\n" +
            "§7- 鱼缸\n" +
            "§7- 锅炉房\n" +
            "§7- 布告栏\n" +
            "§7- 金库：2500g~25000g购买，完成解锁沙漠和桑迪商店\n" +
            "§7- 失踪收集包：需要特殊物品，奖励星之果实"
        ));

        categories.add(new CategoryEntry("技能系统",
            "§l五大技能系统\n\n" +
            "§7等级通过对应操作获得经验值自动提升（Lv1~Lv10）。\n" +
            "§7Lv5和Lv10可选择天赋。\n\n" +
            "§b耕种§7：种植收获作物→升级\n" +
            "§7Lv5: 畜牧人(动物售价+20%) / 农耕人(作物售价+10%)\n" +
            "§7Lv10: 农业学家(生长+10%) / 工匠(工匠+40%) / 鸡舍大师(双蛋) / 牧羊人(羊毛翻倍)\n" +
            "§b精通§7：获得铱镰刀，可找到金色动物饼干\n\n" +
            "§b采集§7：砍树、采集物品→升级\n" +
            "§7Lv5: 护林人(额外原木) / 收集者(双倍采集)\n" +
            "§7Lv10: 伐木工人(硬木) / 植物学家(铱星) / 萃取者(树脂+30%) / 追踪者(采集翻倍)\n" +
            "§b精通§7：可找到金色迷之盒\n\n" +
            "§b钓鱼§7：钓鱼成功→升级\n" +
            "§7Lv5: 渔夫(鱼+25%) / 捕猎者(蟹笼减材料)\n" +
            "§7Lv10: 垂钓者(鱼+50%) / 海盗(宝藏) / 水手(无垃圾) / 诱饵大师(免饵)\n" +
            "§b精通§7：获得高级铱金鱼竿、100挑战鱼饵，可找到金色钓鱼宝箱\n\n" +
            "§b挖矿§7：挖矿、炸矿→升级\n" +
            "§7Lv5: 矿工(额外矿石) / 地质学家(额外宝石)\n" +
            "§7Lv10: 铁匠(矿物+40%) / 挖掘者(双倍晶球) / 勘探者(额外煤) / 宝石专家\n" +
            "§b精通§7：宝石掉落数量翻倍\n\n" +
            "§b战斗§7：击杀怪物→升级\n" +
            "§7Lv5: 战士(伤害+10%) / 侦查员(暴击×1.5)\n" +
            "§7Lv10: 野蛮人(伤害+15%) / 防御者(HP+5) / 特技者(冷却减半) / 亡命徒(暴击×2)\n" +
            "§b精通§7：生命上限+4\n\n" +
            "§8精通系统：五个技能均达到满级10级后解锁，超出10级的经验累加为精通点数，总点数达到一定值即可在总GUI点击技能图标精通对应技能。"
        ));

        categories.add(new CategoryEntry("联系方式",
            "§l更多内容\n\n" +
            "§7更多内容请在游戏中探索！\n\n" +
            "§7输入 §b/stardewvalley info off §7可关闭进入游戏时的欢迎消息。\n\n" +
            "§l如果发现bug或你有宝贵的建议：\n\n" +
            "§bGitHub：§7https://github.com/ideal520520\n\n" +
            "§b邮箱：§7ideal520520@gmail.com\n\n" +
            "§8游戏界面HUD感谢Weatheraintbad的开源代码对该功能的极大贡献。\n" +
            "§8钓鱼小游戏感谢kltyton的开源代码对该功能的极大贡献。"
        ));
    }

    @Override
    protected void init() {
        super.init();
        // 关闭按钮
        addDrawableChild(net.minecraft.client.gui.widget.ButtonWidget.builder(
            Text.literal("关闭"),
            button -> { if (client != null) client.setScreen(null); }
        ).dimensions(this.width - 50, 0, 50, 20).build());
    }

    private int getMaxScroll() {
        return Math.max(0, categories.size() * CATEGORY_GAP - (this.height - TOP_OFFSET - 30));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (mouseX < LEFT_PANEL_W) {
            scrollOffset = (int) Math.clamp(scrollOffset - verticalAmount * CATEGORY_GAP, 0, getMaxScroll());
            return true;
        }
        int contentMax = getContentMaxScroll();
        if (contentMax > 0) {
            contentScroll = (int) Math.clamp(contentScroll - verticalAmount * 12, 0, contentMax);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        if (super.mouseClicked(click, bl)) return true;
        double mouseX = click.x();
        double mouseY = click.y();

        if (mouseX < LEFT_PANEL_W) {
            int idx = (int) ((mouseY - TOP_OFFSET + scrollOffset) / CATEGORY_GAP);
            if (idx >= 0 && idx < categories.size()) {
                int yStart = TOP_OFFSET - scrollOffset + idx * CATEGORY_GAP;
                if (mouseY >= yStart && mouseY < yStart + 20) {
                    selectedCategory = idx;
                    contentScroll = 0;
                    return true;
                }
            }
        }
        return false;
    }

    private int getContentMaxScroll() {
        if (selectedCategory != SKILL_CATEGORY_INDEX) return 0;
        if (selectedCategory < 0 || selectedCategory >= categories.size()) return 0;
        int contentX = LEFT_PANEL_W + 10;
        int contentW = this.width - contentX - 10;
        int contentH = this.height - TOP_OFFSET - 10;
        int lines = wrappedLines(categories.get(selectedCategory).content(), contentW - 4).size();
        return Math.max(0, lines * 10 - (contentH - 10));
    }

    private List<String> wrappedLines(String text, int maxWidth) {
        List<String> result = new ArrayList<>();
        for (String raw : text.split("\n")) {
            if (raw.isEmpty()) {
                result.add("");
                continue;
            }
            // 提取段首颜色码，保证换行后颜色延续
            String colorPrefix = "";
            if (raw.startsWith("§") && raw.length() >= 2) {
                colorPrefix = raw.substring(0, 2);
            }
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < raw.length(); i++) {
                String ch = raw.substring(i, i + 1);
                // 颜色码原样保留，不计入宽度
                if (ch.equals("§")) {
                    line.append(ch);
                    if (i + 1 < raw.length()) {
                        line.append(raw.charAt(i + 1));
                        i++;
                    }
                    continue;
                }
                if (textRenderer.getWidth(line.toString() + ch) > maxWidth) {
                    result.add(line.toString());
                    line.setLength(0);
                    if (!colorPrefix.isEmpty()) {
                        line.append(colorPrefix);
                    }
                }
                line.append(ch);
            }
            result.add(line.toString());
        }
        return result;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // 全屏深色背景
        context.fill(0, 0, this.width, this.height, 0xCC1A1A1A);

        // 右侧内容区背景（先画，避免盖住右上角的关闭按钮）
        int contentX = LEFT_PANEL_W + 10;
        int contentY = TOP_OFFSET;
        int contentW = this.width - contentX - 10;
        int contentH = this.height - contentY - 10;
        context.fill(contentX - 2, contentY - 2, contentX + contentW, contentY + contentH, 0xFF242424);

        super.render(context, mouseX, mouseY, delta);

        // 左侧分类面板底色
        context.fill(2, 2, LEFT_PANEL_W, this.height - 2, 0xFF2B2B2B);

        // 标题（书本小图标 + 文字）
        context.drawItem(net.minecraft.item.Items.WRITTEN_BOOK.getDefaultStack(), 10, 4);
        context.drawText(textRenderer, Text.literal("§l作者的话"), 30, 7, 0xFFFFAAFF, true);

        // 绘制分类列表
        for (int i = 0; i < categories.size(); i++) {
            int y = TOP_OFFSET - scrollOffset + i * CATEGORY_GAP;
            if (y + 20 < TOP_OFFSET || y > this.height - 5) continue;

            boolean hovered = mouseX < LEFT_PANEL_W && mouseY >= y && mouseY < y + 20;
            boolean selected = i == selectedCategory;

            if (selected) {
                context.fill(4, y, LEFT_PANEL_W - 4, y + 20, 0xFF4A6B4A);
            } else if (hovered) {
                context.fill(4, y, LEFT_PANEL_W - 4, y + 20, 0x404A6B4A);
            }

            int color = selected ? 0xFFFFAAFF : (hovered ? 0xFFFFFFFF : 0xFFCCCCCC);
            context.drawText(textRenderer, Text.literal(categories.get(i).name()), 8, y + 3, color, true);
        }

        // 绘制选中分类的内容（自动换行 + 滚动）
        if (selectedCategory >= 0 && selectedCategory < categories.size()) {
            List<String> lines = wrappedLines(categories.get(selectedCategory).content(), contentW - 4);
            int lineY = contentY - contentScroll;
            for (String line : lines) {
                if (lineY > contentY + contentH) break;
                if (lineY >= contentY - 10) {
                    if (line.isEmpty()) {
                        if (selectedCategory == SKILL_CATEGORY_INDEX) {
                            lineY += 4;
                        }
                        continue;
                    }
                    context.drawText(textRenderer, Text.literal(line), contentX, lineY, 0xFFFFFFFF, true);
                }
                lineY += 10;
            }

            // 底部提示可滚动
            if (getContentMaxScroll() > 0) {
                Text hint = Text.literal("§7▲▼ 滚动查看");
                context.drawText(textRenderer, hint, contentX + contentW - textRenderer.getWidth(hint) - 2, contentY + contentH - 12, 0xFFFFFFFF, true);
            }
        }
    }
}
