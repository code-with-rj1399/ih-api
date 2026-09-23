package ai.interviewhq.api.dynamo;

import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class Attrs {

    private Attrs() {
    }

    static AttributeValue s(String value) {
        return AttributeValue.fromS(value);
    }

    static AttributeValue n(Number value) {
        return AttributeValue.fromN(value.toString());
    }

    static AttributeValue bool(boolean value) {
        return AttributeValue.fromBool(value);
    }

    static AttributeValue stringList(List<String> values) {
        List<AttributeValue> list = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                if (value != null) {
                    list.add(s(value));
                }
            }
        }
        return AttributeValue.fromL(list);
    }

    static void putS(Map<String, AttributeValue> item, String key, String value) {
        if (value != null && !value.isBlank()) {
            item.put(key, s(value));
        }
    }

    static void putN(Map<String, AttributeValue> item, String key, Number value) {
        if (value != null) {
            item.put(key, n(value));
        }
    }

    static void putBool(Map<String, AttributeValue> item, String key, Boolean value) {
        if (value != null) {
            item.put(key, bool(value));
        }
    }

    static void putInstant(Map<String, AttributeValue> item, String key, Instant value) {
        if (value != null) {
            item.put(key, s(value.toString()));
        }
    }

    static void putStringList(Map<String, AttributeValue> item, String key, List<String> values) {
        if (values != null) {
            item.put(key, stringList(values));
        }
    }

    static String getS(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null ? null : value.s();
    }

    static Integer getInt(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        if (value == null || value.n() == null) {
            return null;
        }
        return Integer.valueOf(value.n());
    }

    static Double getDouble(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        if (value == null || value.n() == null) {
            return null;
        }
        return Double.valueOf(value.n());
    }

    static Boolean getBool(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        return value == null ? null : value.bool();
    }

    static Instant getInstant(Map<String, AttributeValue> item, String key) {
        String raw = getS(item, key);
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Instant.parse(raw);
    }

    static List<String> getStringList(Map<String, AttributeValue> item, String key) {
        AttributeValue value = item.get(key);
        if (value == null) {
            return List.of();
        }
        if (value.hasL()) {
            List<String> result = new ArrayList<>();
            for (AttributeValue entry : value.l()) {
                if (entry.s() != null) {
                    result.add(entry.s());
                }
            }
            return result;
        }
        if (value.hasSs()) {
            return new ArrayList<>(value.ss());
        }
        return List.of();
    }
}
