package org.lecturestudio.web.api.data.bind;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.serializer.SerializationContext;
import jakarta.json.stream.JsonGenerator;

public abstract class PolymorphicSerializer {

	private final Jsonb jsonb;


	public PolymorphicSerializer() {
		JsonbConfig config = new JsonbConfig();
		config.withSerializers(new HttpResourceFileSerializer());

		this.jsonb = JsonbBuilder.create(config);
	}

	protected void serializeObject(Object obj, JsonGenerator generator,
			SerializationContext context) {
		generator.writeStartObject();
		generator.write(obj.getClass().getSimpleName(), jsonb.toJson(obj, obj.getClass()));
		generator.writeEnd();
	}
}
