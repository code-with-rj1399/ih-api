package ai.interviewhq.api.observability;
import jakarta.servlet.FilterChain; import jakarta.servlet.ServletException; import jakarta.servlet.http.*; import org.slf4j.Logger; import org.slf4j.LoggerFactory; import org.springframework.stereotype.Component; import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component public class PublicApiRequestLoggingFilter extends OncePerRequestFilter {
 private static final Logger log=LoggerFactory.getLogger(PublicApiRequestLoggingFilter.class);
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  if(!req.getRequestURI().startsWith("/api/v1/")){chain.doFilter(req,res);return;} long start=System.nanoTime(); try{chain.doFilter(req,res);}finally{log.info("public_api method={} path={} status={} durationMs={}",req.getMethod(),req.getRequestURI(),res.getStatus(),(System.nanoTime()-start)/1_000_000);}}
}
