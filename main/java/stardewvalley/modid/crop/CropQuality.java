package stardewvalley.modid.crop;

public enum CropQuality {
    NORMAL("", 1.0f, 1.0f),
    SILVER("_silver", 1.4f, 1.25f),
    GOLD("_gold", 1.8f, 1.5f),
    IRIDIUM("_iridium", 2.6f, 2.0f);

    private final String suffix;
    private final float healMultiplier;
    private final float moneyMultiplier;

    CropQuality(String suffix, float healMultiplier, float moneyMultiplier) {
        this.suffix = suffix;
        this.healMultiplier = healMultiplier;
        this.moneyMultiplier = moneyMultiplier;
    }

    public String getSuffix() {
        return suffix;
    }

    public float getHealMultiplier() {
        return healMultiplier;
    }

    public float getMoneyMultiplier() {
        return moneyMultiplier;
    }

    public String itemName(String baseName) {
        return baseName + suffix;
    }
}
