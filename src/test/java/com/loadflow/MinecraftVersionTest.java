package com.loadflow;

import com.loadflow.protocol.MinecraftVersion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MinecraftVersionTest {

    @Test
    public void testVersionParsing() {
        assertEquals(MinecraftVersion.V1_16_5, MinecraftVersion.fromString("1.16.5"));
        assertEquals(MinecraftVersion.V1_20_4, MinecraftVersion.fromString("1.20.4"));
        assertEquals(MinecraftVersion.V1_20_5, MinecraftVersion.fromString("1.20.5"));
        assertEquals(MinecraftVersion.V1_21_4, MinecraftVersion.fromString("1.21.4"));
        assertEquals(MinecraftVersion.V1_20_5, MinecraftVersion.fromString("766"));
    }

    @Test
    public void testProtocolFeatures() {
        assertFalse(MinecraftVersion.V1_16_5.hasConfigurationPhase());
        assertFalse(MinecraftVersion.V1_16_5.hasUuidInLoginStart());

        assertFalse(MinecraftVersion.V1_20_1.hasConfigurationPhase());
        assertTrue(MinecraftVersion.V1_20_1.hasUuidInLoginStart());

        assertTrue(MinecraftVersion.V1_20_2.hasConfigurationPhase());
        assertTrue(MinecraftVersion.V1_20_5.hasConfigurationPhase());
        assertTrue(MinecraftVersion.V1_21_4.hasConfigurationPhase());
    }
}
