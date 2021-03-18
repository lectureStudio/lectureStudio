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
import com.fasterxml.jackson.databind.module.SimpleModule;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.lecturestudio.web.api.message.WebPacket;
import org.lecturestudio.web.api.message.WebPacketJsonDeserializer;

import java.util.List;

/**
 * Decodes a received {@link ByteBuf} into a {@link WebPacket}. Please note that
 * this decoder must be used with a proper {@link ByteToMessageDecoder} such as
 * {@link LengthFieldBasedFrameDecoder} if you are using a stream-based transport
 * such as TCP/IP. A typical setup for TCP/IP would be:
 * 
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
public class JsonDecoder extends MessageToMessageDecoder<ByteBuf> {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	static {
		SimpleModule module = new SimpleModule();
		module.addDeserializer(WebPacket.class, new WebPacketJsonDeserializer(mapper));
		
		mapper.registerModule(module);
	}
	
	
	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
		if (msg == null) {
            return;
        }
		
		final byte[] buffer;
		final int offset;
		final int length = msg.readableBytes();

		if (msg.hasArray()) {
			buffer = msg.array();
			offset = msg.arrayOffset() + msg.readerIndex();
		}
		else {
			buffer = new byte[length];
			msg.getBytes(msg.readerIndex(), buffer);
			offset = 0;
		}
		
		WebPacket packet = mapper.readValue(buffer, offset, length, WebPacket.class);
		
		out.add(packet);
	}

}
