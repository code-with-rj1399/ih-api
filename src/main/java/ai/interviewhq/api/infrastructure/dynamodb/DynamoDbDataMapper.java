package ai.interviewhq.api.infrastructure.dynamodb;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import java.util.*;

public final class DynamoDbDataMapper {
    private DynamoDbDataMapper() {}
    public static Map<String,Object> unwrapMap(Map<String,AttributeValue> item){
        AttributeValue data=item.get("data");
        if(data==null||data.m()==null)return Map.of();
        return unwrap(data.m());
    }
    public static Object unwrap(AttributeValue v){
        if(v==null)return null;
        if(v.s()!=null)return v.s();
        if(v.n()!=null)return v.n();
        if(v.bool()!=null)return v.bool();
        if(v.hasSs())return v.ss();
        if(v.hasNs())return v.ns();
        if(v.hasL())return v.l().stream().map(DynamoDbDataMapper::unwrap).toList();
        if(v.hasM())return unwrap(v.m());
        return null;
    }
    public static <T>T convert(Map<String,Object> data,Class<T> type,ObjectMapper mapper){return mapper.convertValue(data,type);}
}
