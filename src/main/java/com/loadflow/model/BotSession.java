package com.loadflow.model;

import java.util.UUID;
import java.util.concurrent.atomic.LongAdder;

public class BotSession {
    private final int id;
    private final String username;
    private final UUID uuid;
    private volatile BotState state;
    private volatile long connectedAt = 0;
    private volatile long disconnectedAt = 0;
    private volatile long latencyMs = -1;
    private volatile String disconnectReason = "";
    private final LongAdder packetsReceived = new LongAdder();
    private final LongAdder packetsSent = new LongAdder();

    public BotSession(int id, String username, UUID uuid) {
        this.id = id;
        this.username = username;
        this.uuid = uuid;
        this.state = BotState.IDLE;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUuid() {
        return uuid;
    }

    public BotState getState() {
        return state;
    }

    public void setState(BotState state) {
        this.state = state;
        if (state == BotState.CONNECTED && connectedAt == 0) {
            this.connectedAt = System.currentTimeMillis();
        } else if ((state == BotState.DISCONNECTED || state == BotState.KICKED || state == BotState.ERROR) && disconnectedAt == 0) {
            this.disconnectedAt = System.currentTimeMillis();
        }
    }

    public long getConnectedAt() {
        return connectedAt;
    }

    public long getDisconnectedAt() {
        return disconnectedAt;
    }

    public long getOnlineDurationMs() {
        if (connectedAt == 0) return 0;
        if (disconnectedAt > 0) return disconnectedAt - connectedAt;
        return System.currentTimeMillis() - connectedAt;
    }

    public long getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(long latencyMs) {
        this.latencyMs = latencyMs;
    }

    public String getDisconnectReason() {
        return disconnectReason;
    }

    public void setDisconnectReason(String disconnectReason) {
        this.disconnectReason = disconnectReason;
    }

    public long getPacketsReceived() {
        return packetsReceived.sum();
    }

    public void incrementPacketsReceived() {
        this.packetsReceived.increment();
    }

    public long getPacketsSent() {
        return packetsSent.sum();
    }

    public void incrementPacketsSent() {
        this.packetsSent.increment();
    }
}
