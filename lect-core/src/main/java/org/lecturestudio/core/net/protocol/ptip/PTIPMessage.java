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

package org.lecturestudio.core.net.protocol.ptip;

import org.lecturestudio.core.net.packet.Packet;

/**
 * The {@code PTIPMessage} interface should be implemented by classes that represent concrete PTIP messages.
 * 
 * @author Alex Andres
 * 
 */
public interface PTIPMessage extends Packet {

	/**
	 * Returns the {@link PTIPMessageCode}. Each message has it's own code. The message code identifies the message.
	 * 
	 * @return The code of the message.
	 */
	PTIPMessageCode getMessageCode();

}
