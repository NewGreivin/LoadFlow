package com.loadflow.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.loadflow.model.BotSession;
import com.loadflow.model.TestConfig;
import com.loadflow.model.TestMetrics;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportExporter {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static File exportJson(File directory, TestConfig config, TestMetrics metrics, List<BotSession> sessions) throws IOException {
        if (!directory.exists()) directory.mkdirs();

        String filename = "loadflow_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".json";
        File file = new File(directory, filename);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("timestamp", LocalDateTime.now().toString());
        report.put("config", config);
        report.put("metrics", metrics);
        report.put("sessions", sessions);

        try (FileWriter writer = new FileWriter(file)) {
            GSON.toJson(report, writer);
        }

        return file;
    }

    public static File exportCsv(File directory, List<BotSession> sessions) throws IOException {
        if (!directory.exists()) directory.mkdirs();

        String filename = "loadflow_bots_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        File file = new File(directory, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("ID,Username,UUID,State,LatencyMs,OnlineDurationMs,PacketsSent,PacketsReceived,DisconnectReason");
            for (BotSession s : sessions) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",%d,%d,%d,%d,\"%s\"%n",
                    s.getId(),
                    s.getUsername(),
                    s.getUuid(),
                    s.getState().name(),
                    s.getLatencyMs(),
                    s.getOnlineDurationMs(),
                    s.getPacketsSent(),
                    s.getPacketsReceived(),
                    s.getDisconnectReason().replace("\"", "\"\"")
                );
            }
        }

        return file;
    }
}
