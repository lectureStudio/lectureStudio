package org.lecturestudio.web.api.data.bind;

import static java.util.Objects.nonNull;

import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.JsonbException;
import jakarta.json.bind.serializer.DeserializationContext;
import jakarta.json.stream.JsonParser;

public abstract class PolymorphicDeserializer {

	private final Jsonb jsonb;

	private final String packageName;


	public PolymorphicDeserializer() {
		this(null);
	}

	public PolymorphicDeserializer(String packageName) {
		JsonbConfig config = new JsonbConfig();
		config.withDeserializers(new HttpResourceFileDeserializer());

		this.jsonb = JsonbBuilder.create(config);
		this.packageName = packageName;
	}

	public <T> T deserialize(JsonParser parser, DeserializationContext context) {
		JsonObject jsonObj = parser.getObject();

		Iterator<Map.Entry<String, JsonValue>> iter = jsonObj.entrySet().iterator();
		String type = iter.next().getKey();
		String objType = jsonObj.getString(type);
		String className = nonNull(packageName) ?
				packageName + "." + type :
				type;

		try {
			Class<?> cls = Class.forName(className);

			return jsonb.fromJson(objType, (Type) cls);
		}
		catch (ClassNotFoundException e) {
			throw new JsonbException("Unknown type: " + className);
		}
	}
}
