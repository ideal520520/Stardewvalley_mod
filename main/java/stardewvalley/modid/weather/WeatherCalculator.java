package stardewvalley.modid.weather;

import net.minecraft.server.world.ServerWorld;
import stardewvalley.modid.season.Season;

public class WeatherCalculator {

    public static WeatherType calculateTomorrow(ServerWorld world) {
        long timeOfDay = world.getTimeOfDay();
        long totalDays = timeOfDay / 24000L;
        long dayInSeason = totalDays % 28L;
        Season season = Season.fromTimeOfDay(timeOfDay);
        long seasonIndex = (totalDays / 28L) % 4L;
        long year = totalDays / 112L;

        // ========== 强制固定天气 ==========

        // 第一年春 1,2,4,5 日
        if (year == 0 && seasonIndex == 0) {
            if (dayInSeason == 0 || dayInSeason == 1 || dayInSeason == 3 || dayInSeason == 4) {
                return WeatherType.SUN;
            }
            if (dayInSeason == 2) {
                return WeatherType.RAIN;
            }
        }

        // 每年夏13日、夏26日
        if (seasonIndex == 1 && (dayInSeason == 12 || dayInSeason == 25)) {
            return WeatherType.STORM;
        }

        // 每年第1天(除第一年春)
        if (dayInSeason == 0 && !(year == 0 && seasonIndex == 0)) {
            return WeatherType.SUN;
        }

        // 绿雨：夏季随机
        if (seasonIndex == 1) {
            int dayOfSeason = (int) dayInSeason + 1;
            int[] greenRainDays = {5, 6, 7, 14, 15, 16, 18, 23};
            for (int d : greenRainDays) {
                if (dayOfSeason == d) {
                    float chance = (dayOfSeason == 23) ? 1.0f : 0.25f;
                    if (world.random.nextFloat() < chance) {
                        return WeatherType.GREEN_RAIN;
                    }
                    break;
                }
            }
        }

        // ========== 概率计算 ==========
        switch (season) {
            case SPRING:
            case FALL:
                return calculateSpringFall(season, world);
            case SUMMER:
                return calculateSummer((int) dayInSeason + 1, world);
            case WINTER:
                return calculateWinter(world);
        }

        return WeatherType.SUN;
    }

    private static WeatherType calculateSpringFall(Season season, ServerWorld world) {
        if (world.random.nextFloat() < 0.183f) {
            if (world.random.nextFloat() < 0.25f) {
                return WeatherType.STORM;
            }
            return WeatherType.RAIN;
        }

        if (season == Season.SPRING) {
            if (world.random.nextFloat() < 0.20f) {
                return WeatherType.WIND_SPRING;
            }
        } else {
            if (world.random.nextFloat() < 0.60f) {
                return WeatherType.WIND_FALL;
            }
        }

        return WeatherType.SUN;
    }

    private static WeatherType calculateSummer(int dayOfSeason, ServerWorld world) {
        float rainChance = 0.12f + (0.003f * dayOfSeason);
        if (world.random.nextFloat() < rainChance) {
            if (world.random.nextFloat() < 0.85f) {
                return WeatherType.STORM;
            }
            return WeatherType.RAIN;
        }
        return WeatherType.SUN;
    }

    private static WeatherType calculateWinter(ServerWorld world) {
        if (world.random.nextFloat() < 0.63f) {
            return WeatherType.SNOW;
        }
        return WeatherType.SUN;
    }
}
