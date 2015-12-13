/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 *                       Version 3, 29 June 2007
 *
 * Copyright (C) 2007 Free Software Foundation, Inc. <http://fsf.org/>
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 *
 * You can view LICENCE file for details. 
 *
 * @author The Dragonet Team
 */
package org.dragonet.proxy.network;

import com.flowpowered.networking.Message;
import com.flowpowered.networking.NetworkClient;
import com.flowpowered.networking.protocol.AbstractProtocol;
import com.flowpowered.networking.session.BasicSession;
import com.flowpowered.networking.session.Session;
import io.netty.channel.Channel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import java.net.SocketAddress;
import lombok.Getter;
import net.glowstone.net.message.handshake.HandshakeMessage;
import net.glowstone.net.message.login.LoginStartMessage;
import net.glowstone.net.pipeline.CodecsHandler;
import net.glowstone.net.pipeline.FramingHandler;
import net.glowstone.net.pipeline.MessageHandler;
import net.glowstone.net.pipeline.NoopHandler;
import net.glowstone.net.protocol.GlowProtocol;
import net.glowstone.net.protocol.LoginProtocol;
import net.glowstone.net.protocol.ProtocolType;
import org.dragonet.proxy.DragonProxy;
import org.dragonet.proxy.configuration.Lang;
import org.dragonet.proxy.utilities.Versioning;

/**
 * Maintaince the connection between the proxy and remote Minecraft server.
 */
public class DownstreamSession extends NetworkClient {

    /**
     * The time in seconds which are elapsed before a client is disconnected due
     * to a read timeout.
     */
    private static final int READ_TIMEOUT = 20;

    /**
     * The time in seconds which are elapsed before a client is deemed idle due
     * to a write timeout.
     */
    private static final int WRITE_IDLE_TIMEOUT = 15;

    @Getter
    private final DragonProxy proxy;

    @Getter
    private final UpstreamSession upstream;

    private DynamicSession session;

    public DownstreamSession(DragonProxy proxy, UpstreamSession upstream) {
        this.proxy = proxy;
        this.upstream = upstream;
    }

    @Override
    public Session newSession(Channel c) {
        session = new DynamicSession(c, new LoginProtocol());
        MessageHandler handler = new MessageHandler(this);
        CodecsHandler codecs = new CodecsHandler(ProtocolType.HANDSHAKE.getProtocol());
        FramingHandler framing = new FramingHandler();

        c.pipeline().remove("handler");
        c.pipeline()
                .addLast("encryption", NoopHandler.INSTANCE)
                .addLast("framing", framing)
                .addLast("compression", NoopHandler.INSTANCE)
                .addLast("codecs", codecs)
                .addLast("readtimeout", new ReadTimeoutHandler(READ_TIMEOUT))
                .addLast("writeidletimeout", new IdleStateHandler(0, WRITE_IDLE_TIMEOUT, 0))
                .addLast("handler", handler);

        c.write(new HandshakeMessage(Versioning.MINECRAFT_PC_PROTOCOL, proxy.getRemoteServerAddress().getHostName(), proxy.getRemoteServerAddress().getPort(), 3));
        session.setProtocol(new LoginProtocol());

        c.writeAndFlush(new LoginStartMessage(upstream.getUsername()));

        return session;
    }

    public void onError(Throwable err) {
        err.printStackTrace();
        proxy.getLogger().info(String.format("%s[%s]: ", upstream.getUsername(), upstream.getRemoteAddress()) + proxy.getLang().get(Lang.MESSAGE_REMOTE_ERROR));
        upstream.disconnect(proxy.getLang().get(Lang.MESSAGE_REMOTE_ERROR));
    }

    @Override
    public void sessionInactivated(Session session) {
        proxy.getLogger().info(String.format("%s[%s]: ", upstream.getUsername(), upstream.getRemoteAddress()) + proxy.getLang().get(Lang.MESSAGE_REMOTE_DISCONNECTED));
        upstream.disconnect(proxy.getLang().get(Lang.MESSAGE_REMOTE_DISCONNECTED));
    }

    @Override
    public void onConnectFailure(SocketAddress address, Throwable t) {
        proxy.getLogger().info(String.format("%s[%s]: ", upstream.getUsername(), upstream.getRemoteAddress()) + proxy.getLang().get(Lang.MESSAGE_REMOTE_CONNECT_FAILURE));
        upstream.disconnect(proxy.getLang().get(Lang.MESSAGE_REMOTE_CONNECT_FAILURE));
    }

    @Override
    public void onConnectSuccess(SocketAddress address) {
        proxy.getLogger().info(proxy.getLang().get(Lang.MESSAGE_REMOTE_CONNECTED, upstream.getUsername(), upstream.getRemoteAddress()));
    }

    public void messageReceived(Message message) {
        if (message == null) {
            return;
        }
        System.out.println("Received PC message: " + message.getClass().getSimpleName());
        //TODO
    }

    public class DynamicSession extends BasicSession {

        public DynamicSession(Channel channel, AbstractProtocol bootstrapProtocol) {
            super(channel, bootstrapProtocol);
        }

        @Override
        public void setProtocol(AbstractProtocol protocol) {
            if (!GlowProtocol.class.isAssignableFrom(protocol.getClass())) {
                return;
            }
            getChannel().flush();
            getChannel().pipeline().replace("codecs", "codecs", new CodecsHandler((GlowProtocol) protocol));
            super.setProtocol(protocol);
        }

        @Override
        public void onHandlerThrowable(Message message, com.flowpowered.networking.MessageHandler<?, ?> handle, Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onOutboundThrowable(Throwable throwable) {
            throwable.printStackTrace();
        }

        @Override
        public void onInboundThrowable(Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
