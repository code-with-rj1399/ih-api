package ai.interviewhq.api.common.pagination;
import org.junit.jupiter.api.Test; import static org.junit.jupiter.api.Assertions.*;
class PaginationRequestTest {
 @Test void defaultsTo25(){assertEquals(25,PaginationRequest.of(null,null).limit());}
 @Test void acceptsBounds(){assertEquals(1,PaginationRequest.of(1,null).limit());assertEquals(100,PaginationRequest.of(100,null).limit());}
 @Test void rejectsOutOfBounds(){assertThrows(IllegalArgumentException.class,()->PaginationRequest.of(0,null));assertThrows(IllegalArgumentException.class,()->PaginationRequest.of(101,null));}
}
