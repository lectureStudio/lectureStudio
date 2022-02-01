/*
 * Copyright (C) 2021 TU Darmstadt, Department of Computer Science,
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

package org.lecturestudio.web.api.data.bind;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

@Provider
public class JsonConfig implements ContextResolver<Jsonb> {

	@Override
	public Jsonb getContext(Class<?> aClass) {
		JsonbConfig config = new JsonbConfig();

		config.withAdapters(
				new MessengerMessageAdapter(),
				new MessengerReplyMessageAdapter()
		);
		config.withSerializers(
				new ClassroomServiceSerializer(),
				new HttpResourceFileSerializer()
		);
		config.withDeserializers(
				new ClassroomServiceDeserializer(),
				new HttpResourceFileDeserializer()
		);

		return JsonbBuilder.create(config);
	}
}
