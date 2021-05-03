package org.lecturestudio.web.api.data.bind;

import java.lang.reflect.Type;

import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

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
