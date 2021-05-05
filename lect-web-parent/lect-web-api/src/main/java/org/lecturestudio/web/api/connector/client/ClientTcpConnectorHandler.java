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

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ClientTcpConnectorHandler<T> extends ChannelInboundHandlerAdapter {

	private static final Logger LOG = LogManager.getLogger(ClientTcpConnectorHandler.class);

	private final ConnectorListener<T> listener;


	public ClientTcpConnectorHandler(ConnectorListener<T> listener) {
		this.listener = listener;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		if (listener != null) {
			if (msg instanceof ByteBuf) {
				ByteBuf channelBuffer = (ByteBuf) msg;

				final byte[] packet;
				final int length = channelBuffer.readableBytes();

				if (channelBuffer.hasArray()) {
					packet = channelBuffer.array();
				}
				else {
					packet = new byte[length];
					channelBuffer.getBytes(channelBuffer.readerIndex(), packet);
				}

				channelBuffer.release();

				listener.onConnectorRead((T) packet);
			}
			else {
				listener.onConnectorRead((T) msg);
			}
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		LOG.error(getClass().getSimpleName() + " caught an exception.", cause);

		ctx.close();
	}

}
