package com.loadflow;

import com.loadflow.model.BotSession;
import com.loadflow.model.BotState;
import com.loadflow.model.TestMetrics;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMetricsTest {

    @Test
    public void testComputeMetrics() {
        List<BotSession> sessions = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            BotSession s = new BotSession(i, "Bot_" + i, UUID.randomUUID());
            if (i <= 8) {
                s.setState(BotState.CONNECTED);
                s.setLatencyMs(10 + i * 2); // 12, 14, 16, 18, 20, 22, 24, 26
            } else {
                s.setState(BotState.ERROR);
                s.setDisconnectReason("Timeout");
            }
            sessions.add(s);
        }

        TestMetrics metrics = TestMetrics.compute(sessions, 10, 60);

        assertEquals(10, metrics.getTargetBots());
        assertEquals(8, metrics.getConnectedBots());
        assertEquals(2, metrics.getFailedBots());
        assertEquals(80.0, metrics.getSuccessRate(), 0.01);
        assertTrue(metrics.getAvgLatencyMs() > 0);
        assertTrue(metrics.getP95LatencyMs() >= metrics.getAvgLatencyMs());
    }
}
