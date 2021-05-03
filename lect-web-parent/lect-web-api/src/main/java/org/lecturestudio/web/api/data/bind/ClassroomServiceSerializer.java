package org.lecturestudio.web.api.data.bind;

import javax.json.bind.serializer.JsonbSerializer;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

import org.lecturestudio.web.api.model.ClassroomService;

public class ClassroomServiceSerializer extends PolymorphicSerializer
		implements JsonbSerializer<ClassroomService> {

	@Override
	public void serialize(ClassroomService obj, JsonGenerator generator,
			SerializationContext context) {
		serializeObject(obj, generator, context);
	}
}
