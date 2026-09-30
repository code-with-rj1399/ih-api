package ai.interviewhq.api.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OpaqueCursorCodec {

    private final ObjectMapper objectMapper;
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    private final Base64.Decoder decoder = Base64.getUrlDecoder();

    public OpaqueCursorCodec(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String encode(Map<String, String> lastEvaluatedKey) {
        if (lastEvaluatedKey == null || lastEvaluatedKey.isEmpty()) {
            return null;
        }

        try {
            String json = objectMapper.writeValueAsString(new LinkedHashMap<>(lastEvaluatedKey));
            return encoder.encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Unable to encode pagination cursor", exception);
        }
    }

    public Map<String, String> decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }

        try {
            byte[] decoded = decoder.decode(cursor);
            Map<String, String> key = objectMapper.readValue(
                    decoded,
                    new TypeReference<Map<String, String>>() {}
            );

            if (key.isEmpty()) {
                throw new PaginationCursorException(
                        "Cursor must contain a pagination key"
                );
            }

            return Map.copyOf(key);
        } catch (IllegalArgumentException | IOException exception) {
            throw new PaginationCursorException(
                    "Invalid pagination cursor",
                    exception
            );
        }
    }
}
