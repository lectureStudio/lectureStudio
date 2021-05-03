package org.lecturestudio.web.api.data.bind;

import java.lang.reflect.Type;
import java.util.Base64;

import javax.json.JsonObject;
import javax.json.bind.serializer.DeserializationContext;
import javax.json.bind.serializer.JsonbDeserializer;
import javax.json.stream.JsonParser;

import org.lecturestudio.web.api.model.HttpResourceFile;

public class HttpResourceFileDeserializer implements JsonbDeserializer<HttpResourceFile> {

	@Override
	public HttpResourceFile deserialize(JsonParser parser,
			DeserializationContext context, Type type) {
		JsonObject jsonObj = parser.getObject();
		String name = jsonObj.getString("name");
		long modified = jsonObj.getJsonNumber("modified").longValue();
		byte[] content = Base64.getDecoder().decode(jsonObj.getString("content"));

		HttpResourceFile resourceFile = new HttpResourceFile(name, content);
		resourceFile.setModified(modified);

		return resourceFile;
	}

}
