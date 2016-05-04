package K.Common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import jodd.datetime.JDateTime;

import java.io.IOException;

/**
 * Created by kk on 16/2/21.
 */
public class JDateTimeJsonDeserializer extends JsonDeserializer<JDateTime> {
    @Override
    public JDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String v = p.readValueAs(String.class);
        return new JDateTime(v, "YYYY-MM-DD hh:mm:ss");
    }
}
