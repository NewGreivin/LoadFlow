package com.loadflow.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class MinecraftCompressionCodec {

    public static class Decoder extends ByteToMessageDecoder {
        private final int threshold;
        private final Inflater inflater = new Inflater();

        public Decoder(int threshold) {
            this.threshold = threshold;
        }

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (!in.isReadable()) return;

            int dataLength = PacketBuffer.readVarInt(in);
            if (dataLength == 0) {
                out.add(in.readRetainedSlice(in.readableBytes()));
            } else {
                if (dataLength < threshold) {
                    throw new DecoderException("Badly compressed packet - size " + dataLength + " is below threshold " + threshold);
                }

                byte[] compressed = new byte[in.readableBytes()];
                in.readBytes(compressed);

                inflater.reset();
                inflater.setInput(compressed);

                byte[] decompressed = new byte[dataLength];
                inflater.inflate(decompressed);

                ByteBuf decompBuf = ctx.alloc().buffer(dataLength);
                decompBuf.writeBytes(decompressed);
                out.add(decompBuf);
            }
        }
    }

    public static class Encoder extends MessageToByteEncoder<ByteBuf> {
        private final int threshold;
        private final Deflater deflater = new Deflater();

        public Encoder(int threshold) {
            this.threshold = threshold;
        }

        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            int uncompressedLength = msg.readableBytes();
            if (uncompressedLength < threshold) {
                PacketBuffer.writeVarInt(out, 0);
                out.writeBytes(msg);
            } else {
                byte[] uncompressed = new byte[uncompressedLength];
                msg.readBytes(uncompressed);

                PacketBuffer.writeVarInt(out, uncompressedLength);

                deflater.reset();
                deflater.setInput(uncompressed);
                deflater.finish();

                byte[] buffer = new byte[1024];
                while (!deflater.finished()) {
                    int count = deflater.deflate(buffer);
                    out.writeBytes(buffer, 0, count);
                }
            }
        }
    }
}
