package cn.nukkit.level.vibration;

import lombok.Getter;

@Getter
public class VibrationType {
    private final int frequency;
    private final String identifier;

    public VibrationType(int frequency, String identifier) {
        if (frequency < 1 || frequency > 15) {
            throw new IllegalArgumentException("frequency must between 1 and 15");
        }
        this.identifier = identifier;
        this.frequency = frequency;
    }
}