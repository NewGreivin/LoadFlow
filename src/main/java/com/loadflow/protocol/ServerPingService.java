package com.loadflow.protocol;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class ServerPingService {

    public static CompletableFuture<ServerPingResult> pingServerAsync(String host, int port) {
        return CompletableFuture.supplyAsync(() -> pingServer(host, port));
    }

    public static ServerPingResult pingServer(String host, int port) {
        long startTime = System.currentTimeMillis();
        try (Socket socket = new Socket()) {
            socket.setSoTimeout(4000);
            socket.connect(new InetSocketAddress(host, port), 4000);

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            // 1. Handshake Packet (State = 1 Status)
            ByteBuf handshakeBuf = Unpooled.buffer();
            PacketBuffer.writeVarInt(handshakeBuf, 0x00); // Handshake ID
            PacketBuffer.writeVarInt(handshakeBuf, 766);  // Probing Protocol (1.20.5)
            PacketBuffer.writeString(handshakeBuf, host);
            handshakeBuf.writeShort(port);
            PacketBuffer.writeVarInt(handshakeBuf, 1);    // Next State: 1 (Status)

            ByteBuf frame = Unpooled.buffer();
            PacketBuffer.writeVarInt(frame, handshakeBuf.readableBytes());
            frame.writeBytes(handshakeBuf);
            byte[] frameBytes = new byte[frame.readableBytes()];
            frame.readBytes(frameBytes);
            out.write(frameBytes);
            out.flush();

            // 2. Status Request Packet
            out.writeByte(0x01); // Frame Length = 1
            out.writeByte(0x00); // Packet ID = 0 (Status Request)
            out.flush();

            // 3. Read Status Response
            int packetLength = readVarIntFromStream(in);
            int packetId = readVarIntFromStream(in);

            if (packetId != 0x00) {
                return ServerPingResult.failure("ID de paquete inesperado en ping: " + packetId);
            }

            int jsonLength = readVarIntFromStream(in);
            byte[] jsonBytes = new byte[jsonLength];
            in.readFully(jsonBytes);
            String jsonStr = new String(jsonBytes, StandardCharsets.UTF_8);

            // 4. Ping / Pong para medir latencia exacta
            long pingSent = System.currentTimeMillis();
            out.writeByte(0x09); // Frame Length = 9
            out.writeByte(0x01); // Packet ID = 1 (Ping)
            out.writeLong(pingSent);
            out.flush();

            readVarIntFromStream(in); // Longitud del pong
            readVarIntFromStream(in); // ID del pong (0x01)
            in.readLong();            // Timestamp
            long pingRtt = System.currentTimeMillis() - pingSent;

            // Parsear JSON
            JsonObject root = JsonParser.parseString(jsonStr).getAsJsonObject();

            String versionName = "Desconocido";
            int protocol = 766;
            if (root.has("version") && root.get("version").isJsonObject()) {
                JsonObject ver = root.getAsJsonObject("version");
                if (ver.has("name")) versionName = ver.get("name").getAsString();
                if (ver.has("protocol")) protocol = ver.get("protocol").getAsInt();
            }

            int online = 0;
            int max = 0;
            if (root.has("players") && root.get("players").isJsonObject()) {
                JsonObject players = root.getAsJsonObject("players");
                if (players.has("online")) online = players.get("online").getAsInt();
                if (players.has("max")) max = players.get("max").getAsInt();
            }

            String motd = "";
            if (root.has("description")) {
                if (root.get("description").isJsonObject() && root.getAsJsonObject("description").has("text")) {
                    motd = root.getAsJsonObject("description").get("text").getAsString();
                } else if (root.get("description").isJsonPrimitive()) {
                    motd = root.get("description").getAsString();
                }
            }

            String serverBrand = versionName;
            return new ServerPingResult(true, serverBrand, versionName, protocol, online, max, motd, Math.max(1, pingRtt), null);

        } catch (Exception e) {
            return ServerPingResult.failure("No se pudo conectar al servidor: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()));
        }
    }

    private static int readVarIntFromStream(DataInputStream in) throws Exception {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = in.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) throw new RuntimeException("VarInt is too big");
        } while ((read & 0b10000000) != 0);
        return result;
    }
}
