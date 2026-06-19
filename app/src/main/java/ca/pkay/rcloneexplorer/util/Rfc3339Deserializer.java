package ca.pkay.rcloneexplorer.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import io.github.x0b.rfc3339.Rfc3339Date;
import java.io.IOException;
import java.util.Date;

/**
 * Jackson deserializer that parses RFC 3339 timestamps from rclone API responses.
 */
public class Rfc3339Deserializer extends JsonDeserializer<Date> {
    @Override
    public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return Rfc3339Date.parse(p.getValueAsString());
    }
}
