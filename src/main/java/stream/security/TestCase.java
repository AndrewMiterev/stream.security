package stream.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@SuppressWarnings("unused")
public class TestCase {
    @JsonProperty("tc_id")
    Integer tcId;
    String src;
    String dst;
    @JsonProperty("expect_existence")
    boolean expectExistence;
    @JsonProperty("expected_path")
    List<String> expectedPath;
}
