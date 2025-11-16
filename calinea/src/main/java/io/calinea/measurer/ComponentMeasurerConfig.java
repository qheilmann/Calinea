package io.calinea.measurer;

import io.calinea.models.PackInfo;

public class ComponentMeasurerConfig {
    private final PackInfo packInfo;

    public ComponentMeasurerConfig(PackInfo packInfo) {
        this.packInfo = packInfo;
    }

    public PackInfo getPackInfo() {
        return packInfo;
    }
}
