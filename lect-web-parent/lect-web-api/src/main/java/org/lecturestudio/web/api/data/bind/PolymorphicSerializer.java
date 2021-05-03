package org.lecturestudio.web.api.data.bind;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.serializer.SerializationContext;
import javax.json.stream.JsonGenerator;

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
