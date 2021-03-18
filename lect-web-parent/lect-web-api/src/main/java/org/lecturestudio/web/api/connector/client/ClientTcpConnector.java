/*
 * Copyright (C) 2020 TU Darmstadt, Department of Computer Science,
 * Embedded Systems and Applications Group.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.lecturestudio.web.api.connector.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.model.StreamDescription;

public class ClientTcpConnector extends ClientConnector {

	private static final Logger LOG = LogManager.getLogger(ClientTcpConnector.class);

	private final StreamDescription streamDescription;

	private final ClientTcpConnectorInitializer initializer;

	private Bootstrap bootstrap;
	private EventLoopGroup group;

	private Channel channel;


	public ClientTcpConnector(StreamDescription streamDescription) {
		this.initializer = new ClientTcpConnectorInitializer(streamDescription);
		this.streamDescription = streamDescription;

		addChannelHandler(new TcpConnectorHandler());
	}

	@Override
	public void addChannelHandler(ChannelHandler handler) {
		initializer.addChannelHandler(handler);
	}

	@Override
	public void send(byte[] packet) {
		if (channel.isActive()) {
			final ByteBuf buffer = channel.alloc().buffer(packet.length);
			buffer.writeBytes(packet);

			channel.writeAndFlush(buffer);
		}
	}

	@Override
	protected void initInternal() throws ExecutableException {
		group = new NioEventLoopGroup();

		try {
			bootstrap = new Bootstrap();
			bootstrap.group(group)
					.channel(NioSocketChannel.class)
					.option(ChannelOption.TCP_NODELAY, true)
					.handler(new LoggingHandler(LogLevel.INFO))
					.handler(initializer);
		}
		catch (Exception e) {
			panic("Failed to init " + getClass().getName(), e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		try {
			channel = bootstrap.connect(streamDescription.getAddress(), streamDescription.getPort()).sync().channel();
		}
		catch (Exception e) {
			panic("Failed to start " + getClass().getName(), e);
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			if (channel.isOpen()) {
				group.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
			}
		}
		catch (InterruptedException e) {
			LOG.error("Failed to stop connector", e);
			
			throw new ExecutableException(e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		
	}

	private void panic(String message, Throwable t) throws ExecutableException {
		LOG.error(message, t);

		destroyInternal();

		// Exit on failure.
		throw new ExecutableException(message);
	}

	@Override
	public String toString() {
		return super.toString() + " " + streamDescription;
	}



	@Sharable
	private static class TcpConnectorHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			LOG.error("Exception caught", cause);
			ctx.close();
		}

	}

}
