package com.loadflow.engine;

import com.loadflow.model.BotSession;
import com.loadflow.model.LogEntry;
import com.loadflow.model.TestConfig;
import com.loadflow.model.TestMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class TestOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(TestOrchestrator.class);

    private final BotManager botManager;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    private ScheduledFuture<?> rampTask;
    private ScheduledFuture<?> metricsTask;
    private long testStartTime = 0;

    private Consumer<LogEntry> logListener;
    private Consumer<TestMetrics> metricsListener;
    private Consumer<Boolean> stateListener;

    public TestOrchestrator(BotManager botManager) {
        this.botManager = botManager;
    }

    public void setLogListener(Consumer<LogEntry> listener) { this.logListener = listener; }
    public void setMetricsListener(Consumer<TestMetrics> listener) { this.metricsListener = listener; }
    public void setStateListener(Consumer<Boolean> listener) { this.stateListener = listener; }

    public synchronized void startTest(TestConfig config) {
        if (isRunning.get()) {
            emitLog(LogEntry.Level.WARN, "Una prueba ya está en ejecución.");
            return;
        }

        botManager.clear();
        isRunning.set(true);
        testStartTime = System.currentTimeMillis();
        notifyState(true);

        emitLog(LogEntry.Level.INFO, String.format(
            "Iniciando prueba de carga contra %s:%d | Objetivo: %d bots | Rampa: %d bots/seg | Perfil: %s",
            config.getHost(), config.getPort(), config.getTargetBots(), config.getRampRatePerSec(), config.getBehaviorProfile()
        ));

        AtomicInteger currentBotIndex = new AtomicInteger(1);
        int rampIntervalMs = Math.max(200, 1000 / Math.max(1, config.getRampRatePerSec()));
        int botsPerBatch = Math.max(1, config.getRampRatePerSec() / (1000 / rampIntervalMs));

        rampTask = scheduler.scheduleAtFixedRate(() -> {
            if (!isRunning.get()) return;

            int next = currentBotIndex.get();
            if (next > config.getTargetBots()) {
                if (rampTask != null) rampTask.cancel(false);
                emitLog(LogEntry.Level.SUCCESS, "Rampa completada. Todos los " + config.getTargetBots() + " bots han sido despachados.");
                return;
            }

            for (int i = 0; i < botsPerBatch && currentBotIndex.get() <= config.getTargetBots(); i++) {
                int botNum = currentBotIndex.getAndIncrement();
                botManager.spawnBot(botNum, config);
            }
        }, 500, rampIntervalMs, TimeUnit.MILLISECONDS);

        metricsTask = scheduler.scheduleAtFixedRate(() -> {
            if (!isRunning.get()) return;

            long elapsedSec = (System.currentTimeMillis() - testStartTime) / 1000;
            List<BotSession> sessions = botManager.getSessions();
            TestMetrics metrics = TestMetrics.compute(sessions, config.getTargetBots(), elapsedSec);

            if (metricsListener != null) {
                metricsListener.accept(metrics);
            }

            // Circuit Breaker por errores
            if (config.isAutoStopOnError() && metrics.getFailedBots() > 0 && sessions.size() >= 10) {
                double failureRate = (metrics.getFailedBots() * 100.0) / sessions.size();
                if (failureRate >= config.getMaxErrorThresholdPct()) {
                    emitLog(LogEntry.Level.ERROR, String.format("Circuit Breaker activado: La tasa de error (%.1f%%) superó el umbral permitido (%.1f%%). Deteniendo prueba...", failureRate, config.getMaxErrorThresholdPct()));
                    stopTest("Circuit Breaker por alta tasa de errores");
                    return;
                }
            }

            // Límite de duración
            if (config.getDurationSeconds() > 0 && elapsedSec >= config.getDurationSeconds()) {
                emitLog(LogEntry.Level.INFO, "Tiempo límite de la prueba alcanzado (" + config.getDurationSeconds() + "s). Deteniendo prueba...");
                stopTest("Duración completada");
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    public synchronized void stopTest(String reason) {
        if (!isRunning.getAndSet(false)) return;

        if (rampTask != null) {
            rampTask.cancel(true);
            rampTask = null;
        }
        if (metricsTask != null) {
            metricsTask.cancel(false);
            metricsTask = null;
        }

        emitLog(LogEntry.Level.WARN, "STOP ALL ejecutado. Motivo: " + (reason != null ? reason : "Detención manual por el usuario"));
        botManager.disconnectAll();

        long elapsedSec = (System.currentTimeMillis() - testStartTime) / 1000;
        TestMetrics finalMetrics = TestMetrics.compute(botManager.getSessions(), botManager.getSessions().size(), elapsedSec);
        if (metricsListener != null) {
            metricsListener.accept(finalMetrics);
        }

        notifyState(false);
        emitLog(LogEntry.Level.SUCCESS, String.format("Prueba finalizada. Duración: %ds | Bots procesados: %d | Éxito: %.1f%%",
            elapsedSec, botManager.getSessions().size(), finalMetrics.getSuccessRate()));
    }

    private void emitLog(LogEntry.Level level, String msg) {
        log.info("[{}] {}", level, msg);
        if (logListener != null) {
            logListener.accept(new LogEntry(level, msg));
        }
    }

    private void notifyState(boolean running) {
        if (stateListener != null) {
            stateListener.accept(running);
        }
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public void shutdown() {
        stopTest("Cierre de aplicación");
        scheduler.shutdownNow();
        botManager.shutdown();
    }
}
