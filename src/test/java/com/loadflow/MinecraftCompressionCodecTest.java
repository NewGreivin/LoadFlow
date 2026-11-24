package com.loadflow;

import com.loadflow.protocol.MinecraftCompressionCodec;
import com.loadflow.protocol.PacketBuffer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class MinecraftCompressionCodecTest {

    @Test
    public void testCompressionThreshold() {
        int threshold = 256;
        EmbeddedChannel channel = new EmbeddedChannel(
            new MinecraftCompressionCodec.Decoder(threshold),
            new MinecraftCompressionCodec.Encoder(threshold)
        );

        // 1. Small packet (below threshold) -> should not be compressed (dataLength = 0)
        ByteBuf smallMsg = Unpooled.buffer();
        PacketBuffer.writeVarInt(smallMsg, 0x01);
        PacketBuffer.writeString(smallMsg, "Hello World");

        channel.writeOutbound(smallMsg.copy());
        ByteBuf encodedSmall = channel.readOutbound();
        assertNotNull(encodedSmall);

        // Decode small packet
        channel.writeInbound(encodedSmall);
        ByteBuf decodedSmall = channel.readInbound();
        assertNotNull(decodedSmall);
        assertEquals(smallMsg, decodedSmall);

        // 2. Large packet (above threshold) -> should be compressed (dataLength > 0)
        ByteBuf largeMsg = Unpooled.buffer();
        PacketBuffer.writeVarInt(largeMsg, 0x02);
        byte[] bigData = new byte[1000];
        for (int i = 0; i < bigData.length; i++) bigData[i] = (byte) (i % 127);
        largeMsg.writeBytes(bigData);

        channel.writeOutbound(largeMsg.copy());
        ByteBuf encodedLarge = channel.readOutbound();
        assertNotNull(encodedLarge);

        // Decode large packet
        channel.writeInbound(encodedLarge);
        ByteBuf decodedLarge = channel.readInbound();
        assertNotNull(decodedLarge);
        assertEquals(largeMsg, decodedLarge);

        smallMsg.release();
        decodedSmall.release();
        largeMsg.release();
        decodedLarge.release();
    }
}
