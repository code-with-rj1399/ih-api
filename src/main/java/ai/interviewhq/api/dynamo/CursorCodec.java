package ai.interviewhq.api.dynamo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

final class CursorCodec {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, Map<String, String>>> TYPE = new TypeReference<>() {
    };

    private CursorCodec() {
    }

    static String encode(Map<String, AttributeValue> lastKey) {
        if (lastKey == null || lastKey.isEmpty()) {
            return null;
        }
        try {
            Map<String, Map<String, String>> wire = new LinkedHashMap<>();
            lastKey.forEach((name, value) -> {
                Map<String, String> encoded = new LinkedHashMap<>();
                if (value.s() != null) {
                    encoded.put("s", value.s());
                } else if (value.n() != null) {
                    encoded.put("n", value.n());
                }
                wire.put(name, encoded);
            });
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(MAPPER.writeValueAsBytes(wire));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to encode DynamoDB cursor", e);
        }
    }

    static Map<String, AttributeValue> decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(cursor.getBytes(StandardCharsets.UTF_8));
            Map<String, Map<String, String>> wire = MAPPER.readValue(json, TYPE);
            Map<String, AttributeValue> key = new LinkedHashMap<>();
            wire.forEach((name, encoded) -> {
                if (encoded.containsKey("s")) {
                    key.put(name, AttributeValue.fromS(encoded.get("s")));
                } else if (encoded.containsKey("n")) {
                    key.put(name, AttributeValue.fromN(encoded.get("n")));
                }
            });
            return key;
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid cursor", e);
        }
    }
}
