package stardewvalley.modid.season;

public enum Season {
    SPRING,
    SUMMER,
    FALL,
    WINTER;

    private static final int DAYS_PER_SEASON = 28;
    private static Season cachedSeason = SPRING;

    public static void updateCachedSeason(long timeOfDay) {
        cachedSeason = fromTimeOfDay(timeOfDay);
    }

    public static Season getCachedSeason() {
        return cachedSeason;
    }

    public static Season fromTimeOfDay(long timeOfDay) {
        long day = timeOfDay / 24000L;
        int index = (int) ((day / DAYS_PER_SEASON) % 4);
        return values()[index];
    }

    public static int getDayOfSeason(long timeOfDay) {
        long day = timeOfDay / 24000L;
        return (int) (day % DAYS_PER_SEASON) + 1;
    }

    public static int getTotalDays(long timeOfDay) {
        return (int) (timeOfDay / 24000L);
    }
}
