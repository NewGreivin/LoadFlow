package com.loadflow.protocol;

import com.loadflow.model.BotSession;
import com.loadflow.model.BotState;
import com.loadflow.model.TestConfig;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class MinecraftClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private static final Logger log = LoggerFactory.getLogger(MinecraftClientHandler.class);

    public enum ConnectionPhase {
        HANDSHAKE,
        LOGIN,
        CONFIGURATION,
        PLAY
    }

    private final BotSession session;
    private final TestConfig config;
    private ConnectionPhase phase = ConnectionPhase.HANDSHAKE;
    private ScheduledFuture<?> behaviorTask;
    private double currentX = 0;
    private double currentY = 64;
    private double currentZ = 0;
    private float currentYaw = 0;
    private float currentPitch = 0;
    private boolean initialCommandSent = false;

    public MinecraftClientHandler(BotSession session, TestConfig config) {
        this.session = session;
        this.config = config;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        session.setState(BotState.CONNECTING);
        sendHandshake(ctx);
        sendLoginStart(ctx);
    }

    private void sendHandshake(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x00); // Handshake Packet ID
        PacketBuffer.writeVarInt(buf, config.getProtocolVersion());
        PacketBuffer.writeString(buf, config.getHost());
        buf.writeShort(config.getPort());
        PacketBuffer.writeVarInt(buf, 2); // Next state: 2 (Login)
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
        phase = ConnectionPhase.LOGIN;
        session.setState(BotState.LOGGING_IN);
    }

    private void sendLoginStart(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x00); // Login Start Packet ID
        PacketBuffer.writeString(buf, session.getUsername());
        // En 1.19.3+ (Protocolo 761+) el paquete incluye el UUID del jugador
        if (config.getVersion().hasUuidInLoginStart()) {
            PacketBuffer.writeUUID(buf, session.getUuid());
        }
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
    }

    private void sendLoginAcknowledged(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x03); // Login Acknowledged (1.20.2+)
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
        phase = ConnectionPhase.CONFIGURATION;
        session.setState(BotState.CONFIGURATION);
    }

    private void sendFinishConfiguration(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x03); // Finish Configuration Response
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
        phase = ConnectionPhase.PLAY;
        session.setState(BotState.CONNECTED);
        startBehaviorTask(ctx);
    }

    private void sendKnownPacksResponse(ChannelHandlerContext ctx) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x07); // Select Known Packs response
        PacketBuffer.writeVarInt(buf, 0); // 0 known packs
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
    }

    private void sendKeepAlivePlay(ChannelHandlerContext ctx, long id) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, config.getVersion().getServerboundKeepAliveId());
        buf.writeLong(id);
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
    }

    private void sendKeepAliveConfig(ChannelHandlerContext ctx, long id) {
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x04); // Serverbound Keep Alive in Config
        buf.writeLong(id);
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
    }

    private void sendPingPlay(ChannelHandlerContext ctx) {
        if (config.getProtocolVersion() < 764) return; // Solo 1.20.2+ soporta Ping/Pong packet
        ByteBuf buf = ctx.alloc().buffer();
        PacketBuffer.writeVarInt(buf, 0x27); // Serverbound Ping
        buf.writeInt((int) (System.currentTimeMillis() & 0x7FFFFFFF));
        ctx.writeAndFlush(buf);
        session.incrementPacketsSent();
    }

    private void sendInitialCommand(ChannelHandlerContext ctx) {
        if (initialCommandSent) return;
        initialCommandSent = true;

        String cmd = config.getInitialCommand();
        if (cmd == null || cmd.trim().isEmpty()) return;
        cmd = cmd.trim();

        try {
            ByteBuf buf = ctx.alloc().buffer();
            if (config.getProtocolVersion() >= 759) { // 1.19+ Chat Command
                String clean = cmd.startsWith("/") ? cmd.substring(1) : cmd;
                PacketBuffer.writeVarInt(buf, 0x04);
                PacketBuffer.writeString(buf, clean);
            } else { // 1.16 - 1.18 Chat Message
                String full = cmd.startsWith("/") ? cmd : "/" + cmd;
                PacketBuffer.writeVarInt(buf, 0x03);
                PacketBuffer.writeString(buf, full);
            }
            ctx.writeAndFlush(buf);
            session.incrementPacketsSent();
            log.info("Bot {} envió comando inicial: {}", session.getUsername(), cmd);
        } catch (Exception ignored) {}
    }

    private void startBehaviorTask(ChannelHandlerContext ctx) {
        if (behaviorTask != null && !behaviorTask.isCancelled()) return;

        behaviorTask = ctx.executor().scheduleAtFixedRate(() -> {
            if (!ctx.channel().isActive() || phase != ConnectionPhase.PLAY) return;

            try {
                // Si hay un comando inicial (ej: /server lobby para Velocity), enviarlo tras entrar
                if (!initialCommandSent) {
                    sendInitialCommand(ctx);
                }

                // Medir latencia RTT periódicamente si la versión lo soporta
                sendPingPlay(ctx);

                // Rotación o desplazamiento según perfil
                if ("EXPLORER".equalsIgnoreCase(config.getBehaviorProfile())) {
                    currentYaw = (currentYaw + 15.0f) % 360.0f;
                    currentX += (Math.random() - 0.5) * 0.2;
                    currentZ += (Math.random() - 0.5) * 0.2;
                } else {
                    currentYaw = (currentYaw + 5.0f) % 360.0f;
                }

                ByteBuf moveBuf = ctx.alloc().buffer();
                PacketBuffer.writeVarInt(moveBuf, config.getVersion().getServerboundPositionAndRotationId());
                moveBuf.writeDouble(currentX);
                moveBuf.writeDouble(currentY);
                moveBuf.writeDouble(currentZ);
                moveBuf.writeFloat(currentYaw);
                moveBuf.writeFloat(currentPitch);
                moveBuf.writeBoolean(true); // On Ground
                ctx.writeAndFlush(moveBuf);
                session.incrementPacketsSent();
            } catch (Exception e) {
                log.debug("Error enviando tick de bot: {}", e.getMessage());
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
        session.incrementPacketsReceived();
        int packetId = PacketBuffer.readVarInt(msg);

        switch (phase) {
            case LOGIN -> handleLoginPacket(ctx, packetId, msg);
            case CONFIGURATION -> handleConfigPacket(ctx, packetId, msg);
            case PLAY -> handlePlayPacket(ctx, packetId, msg);
            default -> {}
        }
    }

    private void handleLoginPacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        if (packetId == 0x00) { // Disconnect
            String reason = PacketBuffer.readString(msg);
            session.setDisconnectReason(cleanChatText(reason));
            session.setState(BotState.KICKED);
            ctx.close();
        } else if (packetId == 0x02) { // Login Success
            if (config.getVersion().hasConfigurationPhase()) {
                sendLoginAcknowledged(ctx);
            } else {
                // Versiones 1.16 a 1.20.1: Transición directa a PLAY
                phase = ConnectionPhase.PLAY;
                session.setState(BotState.CONNECTED);
                startBehaviorTask(ctx);
            }
        } else if (packetId == 0x03) { // Set Compression
            int threshold = PacketBuffer.readVarInt(msg);
            if (threshold >= 0) {
                ctx.pipeline().addBefore("codec", "compressionDecoder", new MinecraftCompressionCodec.Decoder(threshold));
                ctx.pipeline().addBefore("codec", "compressionEncoder", new MinecraftCompressionCodec.Encoder(threshold));
            }
        }
    }

    private void handleConfigPacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        if (packetId == 0x02) { // Disconnect
            String reason = PacketBuffer.readString(msg);
            session.setDisconnectReason(cleanChatText(reason));
            session.setState(BotState.KICKED);
            ctx.close();
        } else if (packetId == 0x03) { // Finish Configuration
            sendFinishConfiguration(ctx);
        } else if (packetId == 0x04) { // Keep Alive Config
            long id = msg.readLong();
            sendKeepAliveConfig(ctx, id);
        } else if (packetId == 0x0E) { // Select Known Packs
            sendKnownPacksResponse(ctx);
        }
    }

    private void handlePlayPacket(ChannelHandlerContext ctx, int packetId, ByteBuf msg) {
        int discId = config.getVersion().getClientboundDisconnectId();
        int keepAliveId = config.getVersion().getClientboundKeepAliveId();

        if (packetId == discId) { // Disconnect (Play)
            String reason = PacketBuffer.readString(msg);
            session.setDisconnectReason(cleanChatText(reason));
            session.setState(BotState.KICKED);
            ctx.close();
        } else if (packetId == keepAliveId) { // Keep Alive
            long id = msg.readLong();
            long sendTime = System.currentTimeMillis();
            sendKeepAlivePlay(ctx, id);
            if (session.getLatencyMs() < 0 || config.getProtocolVersion() < 764) {
                session.setLatencyMs(Math.max(1, System.currentTimeMillis() - sendTime));
            }
        } else if (packetId == 0x36 && config.getProtocolVersion() >= 764) { // Pong (Play 1.20.2+)
            int pingId = msg.readInt();
            long now = System.currentTimeMillis() & 0x7FFFFFFF;
            long rtt = (now >= pingId) ? (now - pingId) : 1;
            session.setLatencyMs(rtt);
        } else if (packetId == 0x35 && config.getProtocolVersion() >= 764) { // Ping from Server
            int id = msg.readInt();
            ByteBuf pong = ctx.alloc().buffer();
            PacketBuffer.writeVarInt(pong, 0x27);
            pong.writeInt(id);
            ctx.writeAndFlush(pong);
            session.incrementPacketsSent();
        } else if (packetId == 0x40 || packetId == 0x38 || packetId == 0x3E) { // Synchronize Player Position
            try {
                this.currentX = msg.readDouble();
                this.currentY = msg.readDouble();
                this.currentZ = msg.readDouble();
                this.currentYaw = msg.readFloat();
                this.currentPitch = msg.readFloat();
            } catch (Exception ignored) {}
        }
    }

    private String cleanChatText(String jsonOrText) {
        if (jsonOrText == null) return "Desconocido";
        return jsonOrText.replaceAll("[\\{\\}\"\\[\\]]|text:", "").trim();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (behaviorTask != null) {
            behaviorTask.cancel(true);
        }
        if (session.getState() != BotState.KICKED && session.getState() != BotState.ERROR) {
            session.setState(BotState.DISCONNECTED);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        session.setDisconnectReason(cause.getMessage() != null ? cause.getMessage() : cause.getClass().getSimpleName());
        session.setState(BotState.ERROR);
        ctx.close();
    }
}
