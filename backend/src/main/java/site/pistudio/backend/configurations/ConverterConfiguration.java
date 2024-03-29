package site.pistudio.backend.configurations;



import com.google.cloud.spring.data.datastore.core.convert.DatastoreCustomConversions;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;


@Configuration
public class ConverterConfiguration {

    static final Converter<LocalDateTime, String> LOCAL_DATE_TIME_STRING_CONVERTER_STRING_CONVERTER =
            new Converter<LocalDateTime, String>() {
                @Override
                public String convert(LocalDateTime localDateTime) {
                    return localDateTime.toString();
                }
            };

    static final Converter<String, LocalDateTime> STRING_LOCAL_DATE_TIME_CONVERTER_CONVERTER =
            new Converter<String, LocalDateTime>() {
                @Override
                public LocalDateTime convert(String s) {
                    return LocalDateTime.parse(s);
                }
            };

    static final Converter<UUID, String> UUID_STRING_CONVERTER = new Converter<UUID, String>() {
        @Override
        public String convert(UUID uuid) {
            return uuid.toString();
        }
    };

    static final Converter<String, UUID> STRING_UUID_CONVERTER = new Converter<String, UUID>() {
        @Override
        public UUID convert(String s) {
            return UUID.fromString(s);
        }
    };

static final Converter<byte[], String> BYTES_STRING_CONVERTER = new Converter<byte[], String>() {
    @Override
    public String convert(byte[] source) {
        return Base64.getUrlEncoder().encodeToString(source);
    }
};

static final Converter<String, byte[]> STRING_BYTES_CONVERTER = new Converter<String, byte[]>() {
    @Override
    public byte[] convert(String source) {
        return Base64.getUrlDecoder().decode(source);
    }
};

    @Bean
    public DatastoreCustomConversions datastoreCustomConversions() {
        return new DatastoreCustomConversions(
                Arrays.asList(LOCAL_DATE_TIME_STRING_CONVERTER_STRING_CONVERTER,
                        STRING_LOCAL_DATE_TIME_CONVERTER_CONVERTER,
                        UUID_STRING_CONVERTER,
                        STRING_UUID_CONVERTER,
                        BYTES_STRING_CONVERTER,
                        STRING_BYTES_CONVERTER
                        ));
    }
}
