package ai.interviewhq.api.common.observability;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PublicApiObservabilityFilterTest {
    @Test
    void filtersOnlyVersionedPublicApiRequests() throws Exception {
        var filter = new PublicApiObservabilityFilter();
        var request = new MockHttpServletRequest("GET", "/api/v1/questions");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
        verify(chain).doFilter(request, response);
    }

    @Test
    void ignoresNonPublicApiRequests() throws Exception {
        var filter = new PublicApiObservabilityFilter();
        var request = new MockHttpServletRequest("GET", "/api/hello");
        var response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);

        assertDoesNotThrow(() -> filter.doFilter(request, response, chain));
        verify(chain).doFilter(request, response);
    }
}
