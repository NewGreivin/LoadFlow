package com.loadflow;

import com.loadflow.protocol.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PacketBufferTest {

    @Test
    public void testVarInt() {
        int[] testValues = {0, 1, 127, 128, 255, 25565, 2097151, Integer.MAX_VALUE};
        for (int val : testValues) {
            ByteBuf buf = Unpooled.buffer();
            PacketBuffer.writeVarInt(buf, val);
            int decoded = PacketBuffer.readVarInt(buf);
            assertEquals(val, decoded);
            buf.release();
        }
    }

    @Test
    public void testString() {
        String testStr = "LoadBot_042_TestPlayer";
        ByteBuf buf = Unpooled.buffer();
        PacketBuffer.writeString(buf, testStr);
        String decoded = PacketBuffer.readString(buf);
        assertEquals(testStr, decoded);
        buf.release();
    }

    @Test
    public void testUUID() {
        UUID uuid = UUID.randomUUID();
        ByteBuf buf = Unpooled.buffer();
        PacketBuffer.writeUUID(buf, uuid);
        UUID decoded = PacketBuffer.readUUID(buf);
        assertEquals(uuid, decoded);
        buf.release();
    }
}
