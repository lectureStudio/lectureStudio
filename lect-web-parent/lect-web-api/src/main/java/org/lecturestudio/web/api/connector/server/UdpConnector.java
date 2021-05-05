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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;

import org.lecturestudio.core.ExecutableException;
import org.lecturestudio.web.api.model.StreamDescription;

public class UdpConnector extends Connector {

	private EventLoopGroup group;


	public UdpConnector(StreamDescription streamDescription) {
		super(streamDescription);
	}

	@Override
	public void send(Object packet) {
		
	}
	
	@Override
	public void addChannelHandler(ChannelHandler handler) {

	}

	@Override
	protected void initInternal() throws ExecutableException {

	}
	
	@Override
	protected void startInternal() throws ExecutableException {
		group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group).channel(NioDatagramChannel.class).handler(new UdpConnectorHandler());
			Channel channel = b.bind(getPort()).sync().channel();

			System.out.println(channel.localAddress());
		}
		catch (Exception e) {
			e.printStackTrace();
			destroyInternal();
		}
	}

	@Override
	protected void stopInternal() throws ExecutableException {
		destroyInternal();
	}

	@Override
	protected void destroyInternal() throws ExecutableException {
		try {
			group.shutdownGracefully().await();
			group = null;
		}
		catch (InterruptedException e) {
			throw new ExecutableException(e);
		}
	}

}
