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

package org.lecturestudio.core.net.bus;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

public class NetworkBus {

	private static NetworkBus INSTANCE = null;

    private final EventBus bus = new EventBus("Network Bus");


	private NetworkBus() {
        bus.register(this);
	}
	
	private static NetworkBus getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new NetworkBus();
		}
		return INSTANCE;
	}
	
	public static void register(final Object subscriber) {
		getInstance().bus.register(subscriber);
	}
	
	public static void unregister(final Object subscriber) {
		getInstance().bus.unregister(subscriber);
	}
	
	public static void post(Object event) {
		getInstance().bus.post(event);
	}

    @Subscribe
    public void listen(DeadEvent event) {
        //System.out.println("Dead Event: " + event.getEvent().getClass());
    }

}
