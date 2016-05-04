package K.Common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import jodd.datetime.JDateTime;

import java.io.IOException;

/**
 * Created by kk on 16/2/21.
 */
public class JDateTimeJsonSerializer extends JsonSerializer<JDateTime> {
    @Override
    public void serialize(JDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.toString("YYYY-MM-DD hh:mm:ss"));
    }
}
