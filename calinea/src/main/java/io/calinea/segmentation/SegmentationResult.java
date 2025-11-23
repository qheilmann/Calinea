package io.calinea.segmentation;

import java.util.Collections;
import java.util.List;

import net.kyori.adventure.text.Component;

public class SegmentationResult {
    private final List<ComponentLine> lines;

    public SegmentationResult(List<ComponentLine> lines) {
        this.lines = lines;
    }

    public List<ComponentLine> lines() {
        return Collections.unmodifiableList(lines);
    }

    public List<Component> components() {
        return lines.stream()
            .map(ComponentLine::component)
            .toList();
    }
}
