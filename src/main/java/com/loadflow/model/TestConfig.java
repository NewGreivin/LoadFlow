package com.loadflow.model;

import com.loadflow.protocol.MinecraftVersion;

public class TestConfig {
    private String host = "127.0.0.1";
    private int port = 25565;
    private MinecraftVersion version = MinecraftVersion.V1_20_5;
    private String versionInput = "AUTO"; // "AUTO" o especificado (ej. "1.20.4", "1.16.5", "766")
    private String targetType = "DIRECT"; // "DIRECT", "VELOCITY", "BUNGEECORD"
    private String initialCommand = "";   // ej. "/server lobby" o "/server survival"
    private int targetBots = 50;
    private int rampRatePerSec = 5;
    private int durationSeconds = 300; // 5 minutos por defecto
    private String botPrefix = "LoadBot_";
    private String behaviorProfile = "AFK"; // AFK, EXPLORER
    private boolean autoStopOnError = true;
    private double maxErrorThresholdPct = 30.0;

    public TestConfig() {}

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public MinecraftVersion getVersion() { return version; }
    public void setVersion(MinecraftVersion version) { this.version = version; }

    public int getProtocolVersion() { return version.getProtocol(); }

    public String getVersionInput() { return versionInput; }
    public void setVersionInput(String versionInput) { this.versionInput = versionInput; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public String getInitialCommand() { return initialCommand; }
    public void setInitialCommand(String initialCommand) { this.initialCommand = initialCommand; }

    public int getTargetBots() { return targetBots; }
    public void setTargetBots(int targetBots) { this.targetBots = targetBots; }

    public int getRampRatePerSec() { return rampRatePerSec; }
    public void setRampRatePerSec(int rampRatePerSec) { this.rampRatePerSec = rampRatePerSec; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public String getBotPrefix() { return botPrefix; }
    public void setBotPrefix(String botPrefix) { this.botPrefix = botPrefix; }

    public String getBehaviorProfile() { return behaviorProfile; }
    public void setBehaviorProfile(String behaviorProfile) { this.behaviorProfile = behaviorProfile; }

    public boolean isAutoStopOnError() { return autoStopOnError; }
    public void setAutoStopOnError(boolean autoStopOnError) { this.autoStopOnError = autoStopOnError; }

    public double getMaxErrorThresholdPct() { return maxErrorThresholdPct; }
    public void setMaxErrorThresholdPct(double maxErrorThresholdPct) { this.maxErrorThresholdPct = maxErrorThresholdPct; }
}
