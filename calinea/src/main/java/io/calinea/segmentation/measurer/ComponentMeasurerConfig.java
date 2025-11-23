package io.calinea.segmentation.measurer;

import io.calinea.font.PackInfo;

public class ComponentMeasurerConfig {
    private final PackInfo packInfo;

    public ComponentMeasurerConfig(PackInfo packInfo) {
        this.packInfo = packInfo;
    }

    public PackInfo getPackInfo() {
        return packInfo;
    }
}
