package com.loadflow.model;

public class TestConfig {
    private String host = "127.0.0.1";
    private int port = 25565;
    private int protocolVersion = 766; // Minecraft 1.20.5 & 1.20.6
    private int targetBots = 50;
    private int rampRatePerSec = 5;
    private int durationSeconds = 300; // 5 minutos por defecto
    private String botPrefix = "LoadBot_";
    private String behaviorProfile = "AFK"; // AFK, EXPLORER, RANDOM_LOOK
    private boolean autoStopOnError = true;
    private double maxErrorThresholdPct = 30.0;

    public TestConfig() {}

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getProtocolVersion() { return protocolVersion; }
    public void setProtocolVersion(int protocolVersion) { this.protocolVersion = protocolVersion; }

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
