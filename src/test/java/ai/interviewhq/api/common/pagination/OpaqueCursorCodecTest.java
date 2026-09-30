package ai.interviewhq.api.common.pagination;
import com.fasterxml.jackson.databind.ObjectMapper; import ai.interviewhq.api.common.exception.PaginationCursorException; import org.junit.jupiter.api.Test; import java.util.Map; import static org.junit.jupiter.api.Assertions.*;
class OpaqueCursorCodecTest {
 private final OpaqueCursorCodec codec=new OpaqueCursorCodec(new ObjectMapper());
 @Test void roundTripsKey(){Map<String,String> k=Map.of("pk","QINDEX#EXTRACTED","sk","2026-09-30T00:00:00Z#1");String c=codec.encode(k);assertTrue(c.matches("[A-Za-z0-9_-]+"));assertEquals(k,codec.decode(c));}
 @Test void rejectsMalformed(){assertThrows(PaginationCursorException.class,()->codec.decode("not-json"));}
 @Test void rejectsEmptyJsonObject(){String c=codec.encode(Map.of("x","y"));assertNotNull(c);assertNotNull(codec.decode(c));}
}
