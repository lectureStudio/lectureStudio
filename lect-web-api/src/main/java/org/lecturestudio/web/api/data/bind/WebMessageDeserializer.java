package org.lecturestudio.web.api.data.bind;

import java.lang.reflect.Type;

import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.bind.serializer.JsonbDeserializer;
import jakarta.json.stream.JsonParser;

import org.lecturestudio.web.api.message.WebMessage;

public class WebMessageDeserializer extends PolymorphicDeserializer
		implements JsonbDeserializer<WebMessage> {

	public WebMessageDeserializer() {
		super(WebMessage.class.getPackageName());
	}

	@Override
	public WebMessage deserialize(JsonParser parser,
			DeserializationContext context, Type type) {
		return deserialize(parser, context);
	}
}
