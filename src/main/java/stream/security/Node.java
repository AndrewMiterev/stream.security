package stream.security;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Builder
public class Node {
    String name;
    String type;
    @Builder.Default
    List<Node> accessTo = new ArrayList<>();

    @Override
    public String toString() {
        final String collect = accessTo.stream().map(n -> n.name).collect(Collectors.joining(","));
        return "Node{" + "name='" + name + '\'' + ", type='" + type + '\'' + ", accessTo=" + collect + '}';
    }
}
