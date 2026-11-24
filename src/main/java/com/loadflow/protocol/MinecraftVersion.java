package com.loadflow.protocol;

public enum MinecraftVersion {
    V1_16_5("1.16.5", 754, false, false, 0x1F, 0x10, 0x12, 0x1A),
    V1_17_1("1.17.1", 756, false, false, 0x21, 0x0F, 0x11, 0x1A),
    V1_18_2("1.18.2", 758, false, false, 0x21, 0x0F, 0x11, 0x1A),
    V1_19_2("1.19.2", 760, false, false, 0x20, 0x12, 0x14, 0x19),
    V1_19_4("1.19.4", 762, false, true,  0x23, 0x12, 0x14, 0x19),
    V1_20_1("1.20.1", 763, false, true,  0x23, 0x12, 0x14, 0x19),
    V1_20_2("1.20.2", 764, true,  true,  0x24, 0x15, 0x1B, 0x1B),
    V1_20_4("1.20.4", 765, true,  true,  0x24, 0x15, 0x1B, 0x1B),
    V1_20_5("1.20.5/1.20.6", 766, true, true, 0x26, 0x18, 0x1B, 0x1D),
    V1_21_0("1.21/1.21.1", 767, true, true, 0x26, 0x18, 0x1B, 0x1D),
    V1_21_2("1.21.2/1.21.3", 768, true, true, 0x29, 0x1A, 0x1D, 0x20),
    V1_21_4("1.21.4", 769, true, true, 0x29, 0x1A, 0x1D, 0x20);

    private final String name;
    private final int protocol;
    private final boolean configurationPhase;
    private final boolean uuidInLoginStart;
    private final int clientboundKeepAliveId;
    private final int serverboundKeepAliveId;
    private final int serverboundPositionAndRotationId;
    private final int clientboundDisconnectId;

    MinecraftVersion(String name, int protocol, boolean configurationPhase, boolean uuidInLoginStart,
                     int clientboundKeepAliveId, int serverboundKeepAliveId,
                     int serverboundPositionAndRotationId, int clientboundDisconnectId) {
        this.name = name;
        this.protocol = protocol;
        this.configurationPhase = configurationPhase;
        this.uuidInLoginStart = uuidInLoginStart;
        this.clientboundKeepAliveId = clientboundKeepAliveId;
        this.serverboundKeepAliveId = serverboundKeepAliveId;
        this.serverboundPositionAndRotationId = serverboundPositionAndRotationId;
        this.clientboundDisconnectId = clientboundDisconnectId;
    }

    public String getName() { return name; }
    public int getProtocol() { return protocol; }
    public boolean hasConfigurationPhase() { return configurationPhase; }
    public boolean hasUuidInLoginStart() { return uuidInLoginStart; }
    public int getClientboundKeepAliveId() { return clientboundKeepAliveId; }
    public int getServerboundKeepAliveId() { return serverboundKeepAliveId; }
    public int getServerboundPositionAndRotationId() { return serverboundPositionAndRotationId; }
    public int getClientboundDisconnectId() { return clientboundDisconnectId; }

    public static MinecraftVersion fromProtocol(int protocol) {
        for (MinecraftVersion v : values()) {
            if (v.protocol == protocol) return v;
        }
        // Si es mayor que 769, usar la más reciente como fallback compatible
        if (protocol > 769) return V1_21_4;
        // Si está entre versiones intermedias
        if (protocol >= 766) return V1_20_5;
        if (protocol >= 764) return V1_20_4;
        if (protocol >= 763) return V1_20_1;
        if (protocol >= 760) return V1_19_2;
        if (protocol >= 758) return V1_18_2;
        if (protocol >= 756) return V1_17_1;
        return V1_16_5;
    }

    public static MinecraftVersion fromString(String input) {
        if (input == null || input.trim().isEmpty()) return V1_20_5;
        String clean = input.trim().toLowerCase();

        // Si el usuario escribió un número de protocolo directo (ej: 766)
        try {
            int proto = Integer.parseInt(clean);
            return fromProtocol(proto);
        } catch (NumberFormatException ignored) {}

        for (MinecraftVersion v : values()) {
            if (clean.contains(v.name.toLowerCase()) || v.name.toLowerCase().contains(clean)) {
                return v;
            }
        }

        if (clean.contains("1.21.4")) return V1_21_4;
        if (clean.contains("1.21.2") || clean.contains("1.21.3")) return V1_21_2;
        if (clean.contains("1.21")) return V1_21_0;
        if (clean.contains("1.20.5") || clean.contains("1.20.6")) return V1_20_5;
        if (clean.contains("1.20.4") || clean.contains("1.20.3")) return V1_20_4;
        if (clean.contains("1.20.2")) return V1_20_2;
        if (clean.contains("1.20")) return V1_20_1;
        if (clean.contains("1.19.4")) return V1_19_4;
        if (clean.contains("1.19")) return V1_19_2;
        if (clean.contains("1.18")) return V1_18_2;
        if (clean.contains("1.17")) return V1_17_1;
        if (clean.contains("1.16")) return V1_16_5;

        return V1_20_5;
    }

    @Override
    public String toString() {
        return name + " (Protocolo " + protocol + ")";
    }
}
