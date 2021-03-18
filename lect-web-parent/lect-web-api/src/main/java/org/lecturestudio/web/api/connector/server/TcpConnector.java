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

package org.lecturestudio.web.api.connector.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.net.BindException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.connector.ConnectorFactory;
import org.lecturestudio.web.api.model.StreamDescription;

public class TcpConnector extends Connector {

	private static final Logger LOG = LogManager.getLogger(TcpConnector.class);

	private final List<ChannelHandler> handlers;
	
	private TcpConnectorInitializer initializer;
	
	private ServerBootstrap bootstrap;
	private EventLoopGroup bossGroup;
	private EventLoopGroup workerGroup;
	
	private WriteHandler writeHandler;
	
	
	public TcpConnector(StreamDescription streamDescription) {
		super(streamDescription);
		
		handlers = new ArrayList<>();
		
		addChannelHandler(new TcpConnectorHandler());
	}
	
	@Override
	public void send(Object packet) {
		if (writeHandler != null) {
			writeHandler.send(packet);
		}
	}
	
	@Override
	public void addChannelHandler(ChannelHandler handler) {
		handlers.add(handler);
	}

	@Override
	protected void initInternal() throws ExecutableException {
		boolean tlsEnabled = getStreamDescription().getTransport().isTlsEnabled();

		bossGroup = new NioEventLoopGroup(1);
		workerGroup = new NioEventLoopGroup();
		initializer = new TcpConnectorInitializer(handlers, tlsEnabled);

		try {
			bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.handler(new LoggingHandler(LogLevel.INFO))
					.childHandler(initializer);
		}
		catch (Exception e) {
			panic("Failed to init " + getClass().getName(), e);
		}
	}

	@Override
	protected void startInternal() throws ExecutableException {
		while (true) {
			try {
				bootstrap.bind(getPort()).sync();
				
				if (LOG.isDebugEnabled()) {
					LOG.debug("Bound {}", this);
				}
				
				// Stop bind attempts.
				break;
			}
			catch (Exception e) {
				// Check if the address is already in use.
				if (e instanceof BindException) {
					// Try next port number.
					int newPort = ConnectorFactory.getNextPort();
					
					if (LOG.isDebugEnabled()) {
						LOG.debug("Changing port number for {} to [{}]", this, newPort);
					}
					
					getStreamDescription().setPort(newPort);
				}
				else {
					String message = "Failed to start " + getClass().getName();
					panic(message, e);
				}
			}
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		try {
			bossGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
			workerGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS).sync();
		}
		catch (InterruptedException e) {
			panic("Failed to stop connector.", e);
		}
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		
	}
	
	@Override
	public String toString() {
		return super.toString() + " " + getPort();
	}

	private void panic(String message, Throwable t) throws ExecutableException {
		destroyInternal();

		LOG.error(message, t);

		// Exit on failure.
		throw new ExecutableException(message);
	}
	
	
	
	@Sharable
	private class TcpConnectorHandler extends ChannelInboundHandlerAdapter {

		@Override
		public void channelActive(final ChannelHandlerContext ctx) {
			writeHandler = ctx.pipeline().get(WriteHandler.class);

			ctx.fireChannelActive();
		}

		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			ctx.close();
		}

	}

}
