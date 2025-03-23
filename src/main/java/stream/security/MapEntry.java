package stream.security;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@SuppressWarnings("unused")
public class MapEntry {
    String type;
    String id;
    Attrs attrs;
    List<String> exits;
    List<String> rules;
}
