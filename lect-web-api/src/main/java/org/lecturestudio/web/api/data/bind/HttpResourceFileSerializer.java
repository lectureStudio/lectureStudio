package org.lecturestudio.web.api.data.bind;

import java.util.Base64;

import jakarta.json.bind.serializer.JsonbSerializer;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

import org.lecturestudio.web.api.model.HttpResourceFile;

public class HttpResourceFileSerializer implements JsonbSerializer<HttpResourceFile> {

	@Override
	public void serialize(HttpResourceFile obj, JsonGenerator generator,
			SerializationContext context) {
		generator.writeStartObject();
		generator.write("name", obj.getName());
		generator.write("modified", obj.getModified());
		generator.write("content", Base64.getEncoder().encodeToString(obj.getContent()));
		generator.writeEnd();
	}
}
