package org.lecturestudio.web.api.data.bind;

import java.lang.reflect.Type;

import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import org.lecturestudio.web.api.model.ClassroomService;

public class ClassroomServiceDeserializer extends PolymorphicDeserializer
		implements JsonbDeserializer<ClassroomService> {

	public ClassroomServiceDeserializer() {
		super(ClassroomService.class.getPackageName());
	}

	@Override
	public ClassroomService deserialize(JsonParser parser,
			DeserializationContext context, Type type) {
		return deserialize(parser, context);
	}
}
