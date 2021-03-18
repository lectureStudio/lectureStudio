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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;

import java.nio.ByteBuffer;

import org.lecturestudio.core.net.rtp.RtpPacket;
import org.lecturestudio.core.recording.action.ActionType;

@Sharable
public class EventWriteHandler extends WriteHandler {

	/** The last selected document. */
	private RtpPacket lastDocumentPacket;

	/** The last selected page. */
	private RtpPacket lastPagePacket;


	public EventWriteHandler(OutboundHandler outboundHandler) {
		super(outboundHandler);
	}

	@Override
	public void send(Object packet) {
		// Intercept document and page events.
		if (packet instanceof ByteBuf) {
			ByteBuf channelBuffer = (ByteBuf) packet;

			final int dataLength = channelBuffer.readableBytes();
			final byte[] data = new byte[dataLength];

			channelBuffer.getBytes(channelBuffer.readerIndex(), data);

			RtpPacket rtpPacket = new RtpPacket(data, dataLength);
			cacheRtpPacket(rtpPacket);
		}

		super.send(packet);
	}

	@Override
	public void channelActive(final ChannelHandlerContext ctx) {
		super.channelActive(ctx);

		// Send immediately the current document and selected page.
		if (lastDocumentPacket != null) {
			byte[] data = lastDocumentPacket.toByteArray();
			ByteBuf buffer = Unpooled.buffer(data.length);
			buffer.writeBytes(data);

			super.send(buffer);
		}
		if (lastPagePacket != null) {
			byte[] data = lastPagePacket.toByteArray();
			ByteBuf buffer = Unpooled.buffer(data.length);
			buffer.writeBytes(data);

			super.send(buffer);
		}
	}

	/**
	 * Remember the last selected document and page.
	 *
	 * @param packet
	 */
	private void cacheRtpPacket(RtpPacket packet) {
		ByteBuffer buffer = ByteBuffer.wrap(packet.getPayload());
		// Extract header.
		buffer.getInt();	// Length
		int type = buffer.get();

		ActionType actionType = ActionType.values()[type];

		switch (actionType) {
			case PAGE:
				lastPagePacket = packet;
				break;

			case DOCUMENT_CREATED:
			case DOCUMENT_SELECTED:
				lastDocumentPacket = packet;
				break;

			default:
				break;
		}
	}

}
