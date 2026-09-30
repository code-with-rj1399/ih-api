package ai.interviewhq.api.common;

import java.util.List;

public record ApiCollectionResponse<T>(
        List<T> items,
        Pagination pagination
) {
    public record Pagination(
            int limit,
            String nextCursor,
            boolean hasMore
    ) {
    }
}
