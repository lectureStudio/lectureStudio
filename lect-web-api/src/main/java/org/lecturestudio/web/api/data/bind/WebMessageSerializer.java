package org.lecturestudio.web.api.data.bind;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import org.lecturestudio.web.api.message.WebMessage;

public class WebMessageSerializer extends PolymorphicSerializer
		implements JsonbSerializer<WebMessage> {

	@Override
	public void serialize(WebMessage obj, JsonGenerator generator,
			SerializationContext context) {
		serializeObject(obj, generator, context);
	}
}
