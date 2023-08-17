package org.lecturestudio.web.api.data.bind;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import org.lecturestudio.web.api.message.WebMessage;

public class WebMessageSerializer extends PolymorphicSerializer
		implements JsonbSerializer<WebMessage> {

	@Override
	public void serialize(WebMessage obj, JsonGenerator generator,
			SerializationContext context) {
		serializeObject(obj, generator, context);
	}
}
