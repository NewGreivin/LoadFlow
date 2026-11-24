package com.loadflow.protocol;

public class ServerPingResult {
    private final boolean success;
    private final String serverBrand;
    private final String versionName;
    private final int protocolVersion;
    private final int onlinePlayers;
    private final int maxPlayers;
    private final String motd;
    private final long pingMs;
    private final String errorMessage;
    private final MinecraftVersion matchedVersion;

    public ServerPingResult(boolean success, String serverBrand, String versionName, int protocolVersion,
                            int onlinePlayers, int maxPlayers, String motd, long pingMs, String errorMessage) {
        this.success = success;
        this.serverBrand = serverBrand;
        this.versionName = versionName;
        this.protocolVersion = protocolVersion;
        this.onlinePlayers = onlinePlayers;
        this.maxPlayers = maxPlayers;
        this.motd = motd;
        this.pingMs = pingMs;
        this.errorMessage = errorMessage;
        this.matchedVersion = MinecraftVersion.fromProtocol(protocolVersion);
    }

    public static ServerPingResult failure(String errorMessage) {
        return new ServerPingResult(false, "Desconocido", "Desconocido", 0, 0, 0, "", -1, errorMessage);
    }

    public boolean isSuccess() { return success; }
    public String getServerBrand() { return serverBrand; }
    public String getVersionName() { return versionName; }
    public int getProtocolVersion() { return protocolVersion; }
    public int getOnlinePlayers() { return onlinePlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public String getMotd() { return motd; }
    public long getPingMs() { return pingMs; }
    public String getErrorMessage() { return errorMessage; }
    public MinecraftVersion getMatchedVersion() { return matchedVersion; }

    public boolean isVelocity() {
        return serverBrand.toLowerCase().contains("velocity") || versionName.toLowerCase().contains("velocity");
    }

    public boolean isBungeeCord() {
        return serverBrand.toLowerCase().contains("bungee") || versionName.toLowerCase().contains("bungee") ||
               serverBrand.toLowerCase().contains("waterfall") || versionName.toLowerCase().contains("waterfall");
    }

    public String getSummary() {
        if (!success) return "❌ Error: " + errorMessage;
        String proxyTag = isVelocity() ? " [Proxy Velocity]" : (isBungeeCord() ? " [Proxy BungeeCord]" : "");
        return String.format("✔ %s%s | Protocolo %d (%s) | Jugadores: %d/%d | Ping: %d ms",
            versionName, proxyTag, protocolVersion, matchedVersion.getName(), onlinePlayers, maxPlayers, pingMs);
    }
}
