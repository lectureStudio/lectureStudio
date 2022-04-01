package org.lecturestudio.presenter.api.model.bind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import org.lecturestudio.web.api.filter.IpRangeRule;

/**
 * Exists to provide compatibility with older config versions. Will be removed
 * in near future.
 *
 * @author Alex Andres
 *
 * @deprecated
 */
public class IpRangeRuleDeserializer extends JsonDeserializer<IpRangeRule> {

	@Override
	public IpRangeRule deserialize(JsonParser p, DeserializationContext ctxt) {
		return new IpRangeRule();
	}

	@Override
	public Object deserializeWithType(JsonParser p, DeserializationContext ctxt,
			TypeDeserializer typeDeserializer) {
		return new IpRangeRule();
	}

}
