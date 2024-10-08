package org.jabref.logic.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DevelopmentStageTest {

    @Test
    void checkStabilityOrder() {
        assertTrue(Version.DevelopmentStage.ALPHA.isMoreStableThan(Version.DevelopmentStage.UNKNOWN));
        assertTrue(Version.DevelopmentStage.BETA.isMoreStableThan(Version.DevelopmentStage.ALPHA));
        assertTrue(Version.DevelopmentStage.STABLE.isMoreStableThan(Version.DevelopmentStage.BETA));

        assertEquals(4, Version.DevelopmentStage.values().length, "It seems that the development stages have been changed, please adjust the test");
    }

    @Test
    void parseStages() {
        assertEquals(Version.DevelopmentStage.ALPHA, Version.DevelopmentStage.parse("-alpha"));
        assertEquals(Version.DevelopmentStage.BETA, Version.DevelopmentStage.parse("-beta"));
        assertEquals(Version.DevelopmentStage.STABLE, Version.DevelopmentStage.parse(""));
    }

    @Test
    void parseNull() {
        assertEquals(Version.DevelopmentStage.UNKNOWN, Version.DevelopmentStage.parse(null));
    }

    @Test
    void parseUnknownString() {
        assertEquals(Version.DevelopmentStage.UNKNOWN, Version.DevelopmentStage.parse("asdf"));
    }
}
