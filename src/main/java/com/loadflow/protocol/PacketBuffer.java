package com.loadflow.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class PacketBuffer {

    public static int readVarInt(ByteBuf in) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = in.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 5) {
                throw new DecoderException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writeVarInt(ByteBuf out, int value) {
        while ((value & -128) != 0) {
            out.writeByte(value & 127 | 128);
            value >>>= 7;
        }
        out.writeByte(value);
    }

    public static int getVarIntSize(int value) {
        for (int i = 1; i < 5; ++i) {
            if ((value & -1 << i * 7) == 0) {
                return i;
            }
        }
        return 5;
    }

    public static long readVarLong(ByteBuf in) {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
            read = in.readByte();
            long value = (read & 0b01111111);
            result |= (value << (7 * numRead));
            numRead++;
            if (numRead > 10) {
                throw new DecoderException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public static void writeVarLong(ByteBuf out, long value) {
        while ((value & -128L) != 0L) {
            out.writeByte((int) (value & 127L | 128L));
            value >>>= 7;
        }
        out.writeByte((int) value);
    }

    public static String readString(ByteBuf in) {
        int len = readVarInt(in);
        byte[] bytes = new byte[len];
        in.readBytes(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static void writeString(ByteBuf out, String str) {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        writeVarInt(out, bytes.length);
        out.writeBytes(bytes);
    }

    public static UUID readUUID(ByteBuf in) {
        long most = in.readLong();
        long least = in.readLong();
        return new UUID(most, least);
    }

    public static void writeUUID(ByteBuf out, UUID uuid) {
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
    }
}
