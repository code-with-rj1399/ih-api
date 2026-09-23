package ai.interviewhq.api.dynamo;

import java.util.List;

public class PageResult<T> {

    private final List<T> items;
    private final String nextCursor;

    public PageResult(List<T> items, String nextCursor) {
        this.items = items;
        this.nextCursor = nextCursor;
    }

    public List<T> getItems() {
        return items;
    }

    public String getNextCursor() {
        return nextCursor;
    }
}
