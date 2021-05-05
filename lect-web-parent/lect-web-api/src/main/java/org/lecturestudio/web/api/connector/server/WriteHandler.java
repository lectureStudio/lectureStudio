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

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.GlobalEventExecutor;

@Sharable
public class WriteHandler extends ChannelInboundHandlerAdapter {

	private final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

	private final OutboundHandler outboundHandler;


	public WriteHandler(OutboundHandler outboundHandler) {
		this.outboundHandler = outboundHandler;
	}
	
	public void send(Object data) {
		outboundHandler.write(channels, data);
	}
	
	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		SslHandler sslHandler = ctx.pipeline().get(SslHandler.class);
		if (sslHandler != null) {
			sslHandler.handshakeFuture().addListener(future -> {
				channels.add(ctx.channel());
			});
		}
		else {
			channels.add(ctx.channel());
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		channels.remove(ctx.channel());
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
	
}
