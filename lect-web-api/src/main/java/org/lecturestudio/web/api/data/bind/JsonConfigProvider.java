package org.lecturestudio.web.api.data.bind;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

@Provider
public class JsonConfigProvider implements ContextResolver<Jsonb> {

	@Override
	public Jsonb getContext(Class<?> aClass) {
		return JsonbBuilder.create(createConfig());
	}

	public static JsonbConfig createConfig() {
		return new JsonbConfig()
				.withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {

					@Override
					public boolean isVisible(Method method) {
						return false;
					}

					@Override
					public boolean isVisible(Field field) {
						return true;
					}
				});
	}
}
