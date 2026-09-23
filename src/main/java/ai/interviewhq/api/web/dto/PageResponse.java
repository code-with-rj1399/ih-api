package ai.interviewhq.api.web.dto;

import java.util.List;

public record PageResponse<T>(List<T> items, String nextCursor, int count) {
    public static <T> PageResponse<T> of(List<T> items, String nextCursor) {
        List<T> copy = items == null ? List.of() : List.copyOf(items);
        return new PageResponse<>(copy, nextCursor, copy.size());
    }
}
