package stream.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Main {

    static List<TestCase> testCases = loadJsonList("C:\\Users\\andreym\\IdeaProjects\\security\\src\\main\\java" + "\\stream\\security\\test_cases.json",
            new TypeReference<List<TestCase>>() {
            });
    static List<MapEntry> entries = loadJsonList(
            "C:\\Users\\andreym\\IdeaProjects\\security\\src\\main" + "\\java\\stream\\security\\map_entities.json",
            new TypeReference<List<MapEntry>>() {
            });

    private static List<Node> nodes = new ArrayList<>();
    private static Map<String, Node> mapNameToNode = new HashMap<>();

    @SneakyThrows
    private static <E> List<E> loadJsonList(String pathName, TypeReference<List<E>> typeReference) {
        return new ObjectMapper().readValue(new File(pathName), typeReference);
    }

    public static void main(String[] args) {
        initializeNodes();
        for (TestCase test : testCases) {
            List<String> checkedPath = new ArrayList<>();
            List<String> path = findPath(test.src, test.dst, new ArrayList<>(), checkedPath);
            if (!comparePath(path, test.expectedPath)) {
                log.error("{}", test);
                log.error("actual path {}", path);
                if (path.isEmpty())
                    log.error("max checked path {}", checkedPath);
            } else {
                log.info("{}", test);
                log.info("actual path {}", path);
            }
        }
    }

    private static boolean comparePath(List<String> path, List<String> expectedPath) {
        if (path == null)
            path = new ArrayList<>();
        if (expectedPath == null)
            expectedPath = new ArrayList<>();
        if (path.size() != expectedPath.size())
            return false;
        for (int i = 0; i < path.size(); i++) {
            if (!path.get(i).equals(expectedPath.get(i)))
                return false;
        }
        return true;
    }

    private static void makeAccessibleFromTo(String src, String dst) {
        Node srcNode = mapNameToNode.get(src);
        assert srcNode != null;
        Node dstNode = mapNameToNode.get(dst);
        assert dstNode != null;
        srcNode.accessTo.add(dstNode);
    }

    private static void initializeNodes() {
        nodes = entries.stream().map(e -> Node.builder().name(e.id).type(e.type).build()).toList();
        mapNameToNode = nodes.stream().collect(Collectors.toMap(n -> n.name, n -> n));
        entries.forEach(entry -> {
            if (entry.attrs.city == null)
                entry.exits.forEach(exit -> makeAccessibleFromTo(entry.id, exit));
            else {
                makeAccessibleFromTo(entry.id, entry.attrs.city);
                makeAccessibleFromTo(entry.attrs.city, entry.id);
            }
        });
    }

    static <E> List<E> of(E e1, List<E> eList) {
        List<E> result = new ArrayList<>();
        result.add(e1);
        result.addAll(eList);
        return result;
    }

    public static List<String> findPath(String src, String dest, List<String> checked, List<String> maxPathChecked) {
        if (checked.contains(src) || checked.contains(dest))
            return new ArrayList<>(); // loop in paths or error in parameter
        if (src.equals(dest)) // reached
            return List.of(dest);
        Node srcNode = mapNameToNode.get(src);
        assert srcNode != null;
        List<String> newChecked = new ArrayList<>(checked);
        newChecked.add(src);
        if (newChecked.size() > maxPathChecked.size()) {
            maxPathChecked.clear();
            maxPathChecked.addAll(newChecked);
        }
        Map<Node, List<String>> mapNodeToPath = srcNode.accessTo.stream()
                .collect(Collectors.toMap(n -> n, n -> findPath(n.name, dest, newChecked, maxPathChecked)));
        Optional<Map.Entry<Node, List<String>>> shortest = mapNodeToPath.entrySet().stream()
                .filter(e -> !e.getValue().isEmpty()).min(Comparator.comparing(e -> e.getValue().size()));
        return shortest.map(Map.Entry::getValue).map(l -> of(src, l)).orElse(new ArrayList<>());
    }
}