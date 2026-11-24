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
        private final byte[] buffer = new byte[8192];

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

                int compressedLen = in.readableBytes();
                byte[] compressed;
                int offset = 0;
                if (in.hasArray()) {
                    compressed = in.array();
                    offset = in.arrayOffset() + in.readerIndex();
                    in.skipBytes(compressedLen);
                } else {
                    compressed = new byte[compressedLen];
                    in.readBytes(compressed);
                }

                inflater.reset();
                inflater.setInput(compressed, offset, compressedLen);

                ByteBuf decompBuf = ctx.alloc().buffer(dataLength, dataLength);
                try {
                    while (!inflater.finished() && decompBuf.isWritable()) {
                        int count = inflater.inflate(buffer);
                        if (count > 0) {
                            decompBuf.writeBytes(buffer, 0, count);
                        } else if (inflater.needsInput()) {
                            break;
                        }
                    }
                    out.add(decompBuf);
                } catch (Exception e) {
                    decompBuf.release();
                    throw e;
                }
            }
        }
    }

    public static class Encoder extends MessageToByteEncoder<ByteBuf> {
        private final int threshold;
        private final Deflater deflater = new Deflater();
        private final byte[] buffer = new byte[8192];

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
                byte[] uncompressed;
                int offset = 0;
                if (msg.hasArray()) {
                    uncompressed = msg.array();
                    offset = msg.arrayOffset() + msg.readerIndex();
                    msg.skipBytes(uncompressedLength);
                } else {
                    uncompressed = new byte[uncompressedLength];
                    msg.readBytes(uncompressed);
                }

                PacketBuffer.writeVarInt(out, uncompressedLength);

                deflater.reset();
                deflater.setInput(uncompressed, offset, uncompressedLength);
                deflater.finish();

                while (!deflater.finished()) {
                    int count = deflater.deflate(buffer);
                    out.writeBytes(buffer, 0, count);
                }
            }
        }
    }
}
