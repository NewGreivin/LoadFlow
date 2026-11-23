package com.loadflow.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.List;

public class VarIntFrameCodec {

    public static class Decoder extends ByteToMessageDecoder {
        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
            in.markReaderIndex();
            byte[] buf = new byte[3];
            for (int i = 0; i < buf.length; i++) {
                if (!in.isReadable()) {
                    in.resetReaderIndex();
                    return;
                }

                buf[i] = in.readByte();
                if (buf[i] >= 0) {
                    ByteBuf lengthBuf = ctx.alloc().buffer(buf.length);
                    try {
                        lengthBuf.writeBytes(buf, 0, i + 1);
                        int length = PacketBuffer.readVarInt(lengthBuf);

                        if (in.readableBytes() < length) {
                            in.resetReaderIndex();
                            return;
                        }

                        out.add(in.readRetainedSlice(length));
                        return;
                    } finally {
                        lengthBuf.release();
                    }
                }
            }

            throw new CorruptedFrameException("VarInt frame length is too long");
        }
    }

    public static class Encoder extends MessageToByteEncoder<ByteBuf> {
        @Override
        protected void encode(ChannelHandlerContext ctx, ByteBuf msg, ByteBuf out) {
            int bodyLength = msg.readableBytes();
            int headerLength = PacketBuffer.getVarIntSize(bodyLength);
            out.ensureWritable(headerLength + bodyLength);
            PacketBuffer.writeVarInt(out, bodyLength);
            out.writeBytes(msg);
        }
    }
}
