package ai.interviewhq.api.common.pagination;
public record PaginationRequest(int limit,String cursor) {
    public static final int DEFAULT_LIMIT=25, MAX_LIMIT=100;
    public PaginationRequest { if(limit<1||limit>MAX_LIMIT) throw new IllegalArgumentException("limit must be between 1 and "+MAX_LIMIT); if(cursor!=null&&cursor.isBlank()) throw new IllegalArgumentException("cursor must not be blank"); }
    public static PaginationRequest of(Integer limit,String cursor){return new PaginationRequest(limit==null?DEFAULT_LIMIT:limit,cursor);}
}
