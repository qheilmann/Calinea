package io.calinea.reader;

import java.util.List;
import java.util.SequencedCollection;

public class JsonFontFormat {

    public static final String FORMAT = "calinea-font-widths";
    public static final SequencedCollection<Integer> SUPPORTED_VERSIONS = List.of(1);
    public static final int CURRENT_VERSION = SUPPORTED_VERSIONS.getLast();
}
