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

package org.lecturestudio.presenter.api.net.webrtc.codec;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;

public class JsonBCodec {

	private final Jsonb jsonb;


	public JsonBCodec() {
		JsonbConfig config = new JsonbConfig()
				.withSerializers(new ChatMessageSerializer())
				.withDeserializers(new ChatMessageDeserializer())
				.withFormatting(true);

		jsonb = JsonbBuilder.create(config);
	}

	public <T> T decode(String data, Class<T> cls) {
		return jsonb.fromJson(data, cls);
	}

	public String encode(Object object) {
		return jsonb.toJson(object);
	}

	public void dispose() throws Exception {
		jsonb.close();
	}
}
