package stardewvalley.modid.gui;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;
import stardewvalley.modid.StardewValley;
import stardewvalley.modid.season.Season;
import stardewvalley.modid.skill.SkillRegistry;
import stardewvalley.modid.weather.WeatherType;

public class ClockHudRenderer implements HudRenderCallback {

    private static final Identifier CLOCK_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/clock.png");
    private static final int CLOCK_W = 72;
    private static final int CLOCK_H = 60;

    private static final Identifier SPRING_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/seasons/spring.png");
    private static final Identifier SUMMER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/seasons/summer.png");
    private static final Identifier FALL_TEXTURE   = Identifier.of(StardewValley.MOD_ID, "textures/gui/seasons/fall.png");
    private static final Identifier WINTER_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/seasons/winter.png");
    private static final Identifier HAND_TEXTURE = Identifier.of(StardewValley.MOD_ID, "textures/gui/clock_hand.png");

    // 天气图标
    private static final Identifier WEATHER_SUN = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statussun.png");
    private static final Identifier WEATHER_RAIN = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statusrain.png");
    private static final Identifier WEATHER_STORM = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statusstorm.png");
    private static final Identifier WEATHER_SNOW = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statussnow.png");
    private static final Identifier WEATHER_WIND_SPRING = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statuswindspring.png");
    private static final Identifier WEATHER_WIND_FALL = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statuswindfall.png");
    private static final Identifier WEATHER_GREEN_RAIN = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statusgreenrain.png");
    private static final Identifier WEATHER_FESTIVAL = Identifier.of(StardewValley.MOD_ID, "textures/gui/weather/statusfestival.png");

    private static final String[] DAY_NAMES = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};

    private static int clientGold = 2000;
    private static boolean animalCatalogueDiscount = false;
    private static boolean priceCatalogueActive = false;
    private static boolean artifactMultiplier = false;

    public static void setArtifactMultiplier(boolean active) {
        artifactMultiplier = active;
    }
    public static boolean hasArtifactMultiplier() { return artifactMultiplier; }

    public static void setAnimalCatalogueDiscount(boolean hasDiscount) {
        animalCatalogueDiscount = hasDiscount;
    }
    public static boolean hasAnimalCatalogueDiscount() { return animalCatalogueDiscount; }

    public static void setPriceCatalogueActive(boolean active) {
        priceCatalogueActive = active;
    }
    public static boolean isPriceCatalogueActive() { return priceCatalogueActive; }

    private static int clientFarmingLevel = 0;
    private static int clientFarmingXp = 0;
    private static int clientFarmingNextXp = 100;
    private static int clientForagingLevel = 0;
    private static int clientForagingXp = 0;
    private static int clientForagingNextXp = 100;
    private static int clientFishingLevel = 0;
    private static int clientFishingXp = 0;
    private static int clientFishingNextXp = 100;
    private static int clientMiningLevel = 0;
    private static int clientMiningXp = 0;
    private static int clientMiningNextXp = 100;
    private static int clientCombatLevel = 0;
    private static int clientCombatXp = 0;
    private static int clientCombatNextXp = 100;
    private static float clientLuck = 0f;
    private static WeatherType clientWeather = WeatherType.SUN;

    public static void register() {
        HudRenderCallback.EVENT.register(new ClockHudRenderer());
    }

    public static void setClientGold(int gold) {
        clientGold = gold;
    }
    public static int getClientGold() { return clientGold; }

    public static void setClientFarming(int level, int xp, int nextXp) {
        clientFarmingLevel = level;
        clientFarmingXp = xp;
        clientFarmingNextXp = nextXp;
    }
    public static int getClientFarmingLevel() { return clientFarmingLevel; }
    public static int getClientFarmingXp() { return clientFarmingXp; }
    public static int getClientFarmingNextXp() { return clientFarmingNextXp; }

    public static void setClientForaging(int level, int xp, int nextXp) {
        clientForagingLevel = level;
        clientForagingXp = xp;
        clientForagingNextXp = nextXp;
    }
    public static int getClientForagingLevel() { return clientForagingLevel; }
    public static int getClientForagingXp() { return clientForagingXp; }
    public static int getClientForagingNextXp() { return clientForagingNextXp; }

    public static void setClientFishing(int level, int xp, int nextXp) {
        clientFishingLevel = level;
        clientFishingXp = xp;
        clientFishingNextXp = nextXp;
    }
    public static int getClientFishingLevel() { return clientFishingLevel; }
    public static int getClientFishingXp() { return clientFishingXp; }
    public static int getClientFishingNextXp() { return clientFishingNextXp; }

    public static void setClientMining(int level, int xp, int nextXp) {
        clientMiningLevel = level;
        clientMiningXp = xp;
        clientMiningNextXp = nextXp;
    }
    public static int getClientMiningLevel() { return clientMiningLevel; }
    public static int getClientMiningXp() { return clientMiningXp; }
    public static int getClientMiningNextXp() { return clientMiningNextXp; }

    public static void setClientCombat(int level, int xp, int nextXp) {
        clientCombatLevel = level;
        clientCombatXp = xp;
        clientCombatNextXp = nextXp;
    }
    public static int getClientCombatLevel() { return clientCombatLevel; }
    public static int getClientCombatXp() { return clientCombatXp; }
    public static int getClientCombatNextXp() { return clientCombatNextXp; }

    // ====== 客户端技能数据 ======
    private static String clientFarmingTier1;
    private static String clientFarmingTier2;
    private static String clientForagingTier1;
    private static String clientForagingTier2;
    private static String clientFishingTier1;
    private static String clientFishingTier2;
    private static String clientMiningTier1;
    private static String clientMiningTier2;
    private static String clientCombatTier1;
    private static String clientCombatTier2;

    public static void setClientSkills(ModPayloads.SkillSyncS2CPayload payload) {
        clientFarmingTier1 = payload.farmingLevel5();
        clientFarmingTier2 = payload.farmingLevel10();
        clientForagingTier1 = payload.foragingLevel5();
        clientForagingTier2 = payload.foragingLevel10();
        clientFishingTier1 = payload.fishingLevel5();
        clientFishingTier2 = payload.fishingLevel10();
        clientMiningTier1 = payload.miningLevel5();
        clientMiningTier2 = payload.miningLevel10();
        clientCombatTier1 = payload.combatLevel5();
        clientCombatTier2 = payload.combatLevel10();
    }

    public static String getClientSkill(SkillRegistry.Category category, int tier) {
        return switch (category) {
            case FARMING -> tier == 1 ? clientFarmingTier1 : clientFarmingTier2;
            case FORAGING -> tier == 1 ? clientForagingTier1 : clientForagingTier2;
            case FISHING -> tier == 1 ? clientFishingTier1 : clientFishingTier2;
            case MINING -> tier == 1 ? clientMiningTier1 : clientMiningTier2;
            case COMBAT -> tier == 1 ? clientCombatTier1 : clientCombatTier2;
        };
    }

    // ====== 客户端精通数据 ======
    private static int clientMasteryPoints = 0;
    private static int clientMasteryLevel = 0;
    private static boolean clientFarmingMastered = false;
    private static boolean clientForagingMastered = false;
    private static boolean clientFishingMastered = false;
    private static boolean clientMiningMastered = false;
    private static boolean clientCombatMastered = false;

    public static void setClientMastery(ModPayloads.MasterySyncS2CPayload payload) {
        clientMasteryPoints = payload.masteryPoints();
        clientMasteryLevel = payload.masteryLevel();
        clientFarmingMastered = payload.farming();
        clientForagingMastered = payload.foraging();
        clientFishingMastered = payload.fishing();
        clientMiningMastered = payload.mining();
        clientCombatMastered = payload.combat();
    }

    public static int getClientMasteryPoints() { return clientMasteryPoints; }
    public static int getClientMasteryLevel() { return clientMasteryLevel; }

    public static boolean isClientMastered(SkillRegistry.Category category) {
        return switch (category) {
            case FARMING -> clientFarmingMastered;
            case FORAGING -> clientForagingMastered;
            case FISHING -> clientFishingMastered;
            case MINING -> clientMiningMastered;
            case COMBAT -> clientCombatMastered;
        };
    }

    public static int getClientMasteredCount() {
        int count = 0;
        for (SkillRegistry.Category cat : SkillRegistry.Category.values()) {
            if (isClientMastered(cat)) count++;
        }
        return count;
    }

    public static void setClientLuck(float luck) { clientLuck = luck; }
    public static float getClientLuck() { return clientLuck; }

    public static void setClientWeather(WeatherType weather) { clientWeather = weather; }
    public static WeatherType getClientWeather() { return clientWeather; }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) return;

        ClientWorld world = client.world;
        long timeOfDay = world.getTimeOfDay();
        long timeInDay = timeOfDay % 24000L;
        long totalDays = timeOfDay / 24000L;

        int screenWidth = context.getScaledWindowWidth();
        int x = screenWidth - CLOCK_W - 4;
        int y = 4;

        // 绘制时钟背景
        context.drawTexture(RenderPipelines.GUI_TEXTURED, CLOCK_TEXTURE,
            x, y, 0.0f, 0.0f, CLOCK_W, CLOCK_H, CLOCK_W, CLOCK_H, CLOCK_W, CLOCK_H);

        // 在时钟纹理的(30,16)~(41,23)区域绘制当前季节图标
        Season currentSeason = Season.fromTimeOfDay(timeOfDay);
        Identifier seasonTexture = switch (currentSeason) {
            case SPRING -> SPRING_TEXTURE;
            case SUMMER -> SUMMER_TEXTURE;
            case FALL -> FALL_TEXTURE;
            case WINTER -> WINTER_TEXTURE;
        };
        context.drawTexture(RenderPipelines.GUI_TEXTURED, seasonTexture,
            x + 30, y + 16, 0.0f, 0.0f, 12, 8, 12, 8);

        // 绘制天气图标 (56,15)~(69,24) 区域
        Identifier weatherTexture = getWeatherTexture(clientWeather);
        if (weatherTexture != null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, weatherTexture,
                x + 56, y + 15, 0.0f, 0.0f, 14, 10, 14, 10);
        }

        // 绘制指针：以时钟(20,20)为旋转中心，指针(17,3)固定于此
        float t = (float) timeInDay / 24000.0f;
        float handAngle;
        if (t <= 0.5f) {
            handAngle = -t * (float) Math.PI;
        } else {
            handAngle = (float) Math.PI / 2.0f - (t - 0.5f) * (float) Math.PI;
        }
        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.translate(x + 20.0f, y + 20.0f);
        matrices.rotate(handAngle);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, HAND_TEXTURE,
            -17, -3, 0.0f, 0.0f, 18, 7, 18, 7, 18, 7);
        matrices.popMatrix();

        float letterScale = 0.13f;
        float numScale = 0.255f;

        int dayInSeason = (int) (totalDays % 28) + 1;
        int dayOfWeek = (int) (totalDays % 7);

        int dayTextX = x + 30;
        LetterRenderer.drawText(context, DAY_NAMES[dayOfWeek], dayTextX, y + 7, letterScale);
        dayTextX += LetterRenderer.textWidth(DAY_NAMES[dayOfWeek], letterScale) + 2;
        LetterRenderer.drawText(context, "DAY", dayTextX, y + 7, letterScale);
        dayTextX += LetterRenderer.textWidth("DAY", letterScale);
        NumberRenderer.drawNumber(context, dayInSeason, dayTextX, y + 6, numScale);

        int hours = (int) ((timeInDay / 1000 + 6) % 24);
        int minutes = (int) ((timeInDay % 1000) * 60 / 1000);

        int timeX = x + 29;
        NumberRenderer.drawNumber(context, hours / 10, timeX, y + 29, numScale);
        timeX += NumberRenderer.charWidth(numScale);
        NumberRenderer.drawNumber(context, hours % 10, timeX, y + 29, numScale);
        timeX += NumberRenderer.charWidth(numScale);
        LetterRenderer.drawLetter(context, 'H', timeX + 2, y + 30, letterScale);
        timeX += LetterRenderer.charWidth(letterScale) + 1;
        NumberRenderer.drawNumber(context, minutes / 10, timeX + 2, y + 29, numScale);
        timeX += NumberRenderer.charWidth(numScale) + 2;
        NumberRenderer.drawNumber(context, minutes % 10, timeX, y + 29, numScale);
        timeX += NumberRenderer.charWidth(numScale);
        LetterRenderer.drawLetter(context, 'm', timeX + 3, y + 30, letterScale);

        String goldStr = String.valueOf(clientGold);
        int goldX = x + 64 - goldStr.length() * NumberRenderer.charWidth(numScale) - 2;
        NumberRenderer.drawNumber(context, clientGold, goldX, y + 49, numScale);
    }

    private static Identifier getWeatherTexture(WeatherType weather) {
        return switch (weather) {
            case SUN -> WEATHER_SUN;
            case RAIN -> WEATHER_RAIN;
            case STORM -> WEATHER_STORM;
            case SNOW -> WEATHER_SNOW;
            case WIND_SPRING -> WEATHER_WIND_SPRING;
            case WIND_FALL -> WEATHER_WIND_FALL;
            case GREEN_RAIN -> WEATHER_GREEN_RAIN;
            case FESTIVAL -> WEATHER_FESTIVAL;
        };
    }
}
