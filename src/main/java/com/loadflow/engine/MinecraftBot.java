package com.loadflow.engine;

import com.loadflow.model.BotSession;
import com.loadflow.model.BotState;
import com.loadflow.model.TestConfig;
import com.loadflow.protocol.MinecraftClientHandler;
import com.loadflow.protocol.VarIntFrameCodec;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class MinecraftBot {
    private final BotSession session;
    private final TestConfig config;
    private final EventLoopGroup workerGroup;
    private Channel channel;

    public MinecraftBot(BotSession session, TestConfig config, EventLoopGroup workerGroup) {
        this.session = session;
        this.config = config;
        this.workerGroup = workerGroup;
    }

    public void connect() {
        session.setState(BotState.CONNECTING);
        Bootstrap b = new Bootstrap();
        b.group(workerGroup)
         .channel(NioSocketChannel.class)
         .option(ChannelOption.TCP_NODELAY, true)
         .option(ChannelOption.SO_KEEPALIVE, true)
         .option(ChannelOption.SO_REUSEADDR, true)
         .option(ChannelOption.ALLOCATOR, io.netty.buffer.PooledByteBufAllocator.DEFAULT)
         .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
         .handler(new ChannelInitializer<SocketChannel>() {
             @Override
             protected void initChannel(SocketChannel ch) {
                 ch.pipeline().addLast("splitter", new VarIntFrameCodec.Decoder());
                 ch.pipeline().addLast("prepender", new VarIntFrameCodec.Encoder());
                 ch.pipeline().addLast("handler", new MinecraftClientHandler(session, config));
             }
         });

        ChannelFuture future = b.connect(config.getHost(), config.getPort());
        this.channel = future.channel();
        future.addListener(f -> {
            if (!f.isSuccess()) {
                session.setDisconnectReason(f.cause() != null ? f.cause().getMessage() : "Fallo de conexión");
                session.setState(BotState.ERROR);
            }
        });
    }

    public void disconnect() {
        session.setState(BotState.DISCONNECTING);
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        session.setState(BotState.DISCONNECTED);
    }

    public BotSession getSession() {
        return session;
    }

    public boolean isConnected() {
        return channel != null && channel.isActive() && session.getState() == BotState.CONNECTED;
    }
}
