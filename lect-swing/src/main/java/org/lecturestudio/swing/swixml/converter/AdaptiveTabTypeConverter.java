package org.lecturestudio.swing.swixml.converter;

import org.lecturestudio.swing.model.AdaptiveTabType;
import org.swixml.Converter;
import org.swixml.SwingEngine;
import org.swixml.dom.Attribute;

public class AdaptiveTabTypeConverter implements Converter<AdaptiveTabType> {
	@Override
	public AdaptiveTabType convert(Class<?> aClass, Attribute attribute, SwingEngine<?> swingEngine) throws Exception {
		if (attribute != null && attribute.getValue() != null) {
			final String[] tokens = attribute.getValue().split("\\.");
			return AdaptiveTabType.valueOf(tokens[1]);
		}
		return null;
	}

	@Override
	public Class<?> convertsTo() {
		return AdaptiveTabType.class;
	}
}
