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

package org.lecturestudio.web.api.connector;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * Encodes the requested {@link WebPacket} into a {@link ByteBuf}. A typical setup
 * for TCP/IP would be:
 * <pre>
 * {@link ChannelPipeline} pipeline = ...;
 *
 * // Decoders
 * pipeline.addLast("frameDecoder", new {@link LengthFieldBasedFrameDecoder}(1048576, 0, 4, 0, 4));
 * pipeline.addLast("packetDecoder", new {@link JsonDecoder}());
 *
 * // Encoder
 * pipeline.addLast("frameEncoder", new {@link LengthFieldPrepender}(4));
 * pipeline.addLast("packetEncoder", new {@link JsonEncoder}());
 * </pre>
 * 
 * and then you can use a {@code WebPacket} instead of a {@link ByteBuf}
 * as a message:
 * 
 * <pre>
 * void channelRead({@link ChannelHandlerContext} ctx, WebPacket packet) {
 *     WebPacket newPacket = new WebPacket();
 *     ctx.channel().writeAndFlush(newPacket);
 * }
 * </pre>
 * 
 * @author Alex Andres
 */
@Sharable
public class JsonEncoder extends MessageToMessageEncoder<Object> {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	
	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
		byte[] buffer = mapper.writeValueAsBytes(msg);

		if (buffer.length == 0) {
			return;
		}

		out.add(Unpooled.wrappedBuffer(buffer));
	}

}
