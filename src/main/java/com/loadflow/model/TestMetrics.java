package com.loadflow.model;

import java.util.List;

public class TestMetrics {
    private int targetBots;
    private int connectedBots;
    private int connectingBots;
    private int disconnectedBots;
    private int failedBots;
    private double successRate;
    private double avgLatencyMs;
    private double p95LatencyMs;
    private long elapsedSeconds;
    private long totalPacketsSent;
    private long totalPacketsReceived;

    public TestMetrics() {}

    public static TestMetrics compute(List<BotSession> sessions, int targetBots, long elapsedSeconds) {
        TestMetrics metrics = new TestMetrics();
        metrics.targetBots = targetBots;
        metrics.elapsedSeconds = elapsedSeconds;

        int connected = 0;
        int connecting = 0;
        int disconnected = 0;
        int failed = 0;
        long latencySum = 0;
        int latencyCount = 0;
        long sent = 0;
        long recv = 0;

        java.util.List<Long> latencies = new java.util.ArrayList<>();

        for (BotSession s : sessions) {
            sent += s.getPacketsSent();
            recv += s.getPacketsReceived();

            switch (s.getState()) {
                case CONNECTED -> {
                    connected++;
                    if (s.getLatencyMs() >= 0) {
                        latencySum += s.getLatencyMs();
                        latencyCount++;
                        latencies.add(s.getLatencyMs());
                    }
                }
                case CONNECTING, LOGGING_IN, CONFIGURATION -> connecting++;
                case DISCONNECTED, DISCONNECTING -> disconnected++;
                case KICKED, ERROR -> failed++;
                default -> {}
            }
        }

        metrics.connectedBots = connected;
        metrics.connectingBots = connecting;
        metrics.disconnectedBots = disconnected;
        metrics.failedBots = failed;
        metrics.totalPacketsSent = sent;
        metrics.totalPacketsReceived = recv;

        int attempts = connected + failed + disconnected;
        metrics.successRate = attempts > 0 ? (connected * 100.0 / attempts) : 100.0;
        metrics.avgLatencyMs = latencyCount > 0 ? ((double) latencySum / latencyCount) : 0;

        if (!latencies.isEmpty()) {
            latencies.sort(Long::compareTo);
            int p95Index = (int) Math.ceil(0.95 * latencies.size()) - 1;
            metrics.p95LatencyMs = latencies.get(Math.max(0, p95Index));
        } else {
            metrics.p95LatencyMs = 0;
        }

        return metrics;
    }

    public int getTargetBots() { return targetBots; }
    public int getConnectedBots() { return connectedBots; }
    public int getConnectingBots() { return connectingBots; }
    public int getDisconnectedBots() { return disconnectedBots; }
    public int getFailedBots() { return failedBots; }
    public double getSuccessRate() { return successRate; }
    public double getAvgLatencyMs() { return avgLatencyMs; }
    public double getP95LatencyMs() { return p95LatencyMs; }
    public long getElapsedSeconds() { return elapsedSeconds; }
    public long getTotalPacketsSent() { return totalPacketsSent; }
    public long getTotalPacketsReceived() { return totalPacketsReceived; }
}
