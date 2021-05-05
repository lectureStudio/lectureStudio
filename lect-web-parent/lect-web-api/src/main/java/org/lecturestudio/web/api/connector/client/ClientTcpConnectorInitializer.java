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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.lecturestudio.web.api.model.StreamDescription;

import java.util.ArrayList;
import java.util.List;

public class ClientTcpConnectorInitializer extends ChannelInitializer<SocketChannel> {

	private final List<ChannelHandler> handlers = new ArrayList<>();

	private final StreamDescription streamDescription;


	public ClientTcpConnectorInitializer(StreamDescription streamDescription) {
		this.streamDescription = streamDescription;
	}

	public void addChannelHandler(ChannelHandler handler) {
		this.handlers.add(handler);
	}

	@Override
	protected void initChannel(SocketChannel channel) throws Exception {
		ChannelPipeline pipeline = channel.pipeline();

		SslContext sslCtx = getSslContext();
		if (sslCtx != null) {
			pipeline.addLast(sslCtx.newHandler(channel.alloc(), streamDescription.getAddress(), streamDescription.getPort()));
		}

		pipeline.addLast(new LengthFieldPrepender(4));
		pipeline.addLast(new LengthFieldBasedFrameDecoder(1048576, 0, 4, 0, 4));
		pipeline.addLast(handlers.toArray(new ChannelHandler[0]));
	}

	private SslContext getSslContext() throws Exception {
		SslContext sslCtx = null;
		boolean tls = false;

		if (tls) {
			sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		}

		return sslCtx;
	}

}
