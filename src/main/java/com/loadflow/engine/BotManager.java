package com.loadflow.engine;

import com.loadflow.model.BotSession;
import com.loadflow.model.TestConfig;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

public class BotManager {
    private static final Logger log = LoggerFactory.getLogger(BotManager.class);

    private final EventLoopGroup eventLoopGroup;
    private final List<MinecraftBot> bots = new CopyOnWriteArrayList<>();
    private final List<BotSession> sessions = new CopyOnWriteArrayList<>();

    public BotManager() {
        // Hilos optimizados para IO asíncrono Netty
        int threads = Math.max(2, Runtime.getRuntime().availableProcessors());
        this.eventLoopGroup = new NioEventLoopGroup(threads);
    }

    public synchronized MinecraftBot spawnBot(int index, TestConfig config) {
        String username = config.getBotPrefix() + String.format("%03d", index);
        // Generación estándar de UUID offline en Minecraft Java Edition
        UUID offlineUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));

        BotSession session = new BotSession(index, username, offlineUuid);
        MinecraftBot bot = new MinecraftBot(session, config, eventLoopGroup);

        sessions.add(session);
        bots.add(bot);

        bot.connect();
        return bot;
    }

    public synchronized void disconnectAll() {
        log.info("Desconectando todos los bots ({})", bots.size());
        for (MinecraftBot bot : bots) {
            try {
                bot.disconnect();
            } catch (Exception e) {
                log.warn("Error desconectando bot {}: {}", bot.getSession().getUsername(), e.getMessage());
            }
        }
    }

    public synchronized void clear() {
        disconnectAll();
        bots.clear();
        sessions.clear();
    }

    public List<BotSession> getSessions() {
        return Collections.unmodifiableList(new ArrayList<>(sessions));
    }

    public List<MinecraftBot> getBots() {
        return Collections.unmodifiableList(new ArrayList<>(bots));
    }

    public void shutdown() {
        disconnectAll();
        eventLoopGroup.shutdownGracefully();
    }
}
