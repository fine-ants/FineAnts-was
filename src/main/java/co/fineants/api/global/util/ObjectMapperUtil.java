package co.fineants.api.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import co.fineants.api.global.config.jackson.JacksonConfig;
import co.fineants.api.global.errors.exception.temp.ObjectMapperException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ObjectMapperUtil {
	private static final ObjectMapper objectMapper = new JacksonConfig().objectMapper();
	
	public static <T> String serialize(T obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			log.warn("Serialization failed: obj={}", obj);
			throw new ObjectMapperException("Serialization failed", e);
		}
	}

	public static <T> T deserialize(String json, Class<T> returnType) {
		try {
			return objectMapper.readValue(json, returnType);
		} catch (JsonProcessingException e) {
			log.warn("Deserialization failed: json={}", json);
			throw new ObjectMapperException("Deserialization failed", e);
		}
	}
}
