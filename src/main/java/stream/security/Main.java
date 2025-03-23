package stream.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

@Slf4j
public class Main {
    @SuppressWarnings("Convert2Diamond") // type 4 List<TestCase>
    private static final List<TestCase> testCases = loadJsonList(
            "C:\\Users\\andreym\\IdeaProjects\\security\\src\\main\\java" + "\\stream\\security\\test_cases.json",
            new TypeReference<List<TestCase>>() {
            });
    @SuppressWarnings("Convert2Diamond")
    private static final List<MapEntry> entries = loadJsonList(
            "C:\\Users\\andreym\\IdeaProjects\\security\\src\\main" + "\\java\\stream\\security\\map_entities.json",
            new TypeReference<List<MapEntry>>() {
            });

    private static final Map<String, Node> mapNameToNode = initializeMapNameToNode();

    @SneakyThrows
    private static <E> List<E> loadJsonList(final String pathName, final TypeReference<List<E>> typeReference) {
        return new ObjectMapper().readValue(new File(pathName), typeReference);
    }

    public static void main(String[] args) {
        for (TestCase test : testCases) {
            final List<String> maxCheckedPath = new ArrayList<>();
            final List<String> path = findPath(test.src, test.dst, new ArrayList<>(), maxCheckedPath);
            if (path.equals(test.expectedPath)) {
                log.info("{}", test);
                log.info("actual path {}", path);
            } else {
                log.error("{}", test);
                log.error("actual path {}", path);
                if (path.isEmpty())
                    log.error("max checked path {}", maxCheckedPath);
            }
        }
    }

    private static Map<String, Node> initializeMapNameToNode() {
        // map creation
        final Map<String, Node> mapNameToNode = entries.stream()
                .map(e -> Node.builder().name(e.id).type(e.type).build())
                .collect(Collectors.toMap(n -> n.name, n -> n));

        // accessible setter
        final BiConsumer<String, String> makeAccessibleFromTo = (src, dst) -> {
            final Node srcNode = mapNameToNode.get(src);
            assert srcNode != null;
            final Node dstNode = mapNameToNode.get(dst);
            assert dstNode != null;
            srcNode.accessTo.add(dstNode);
        };

        // second step initialization / access nodes from - to
        entries.forEach(entry -> {
            if (entry.attrs.city == null)
                entry.exits.forEach(exit -> makeAccessibleFromTo.accept(entry.id, exit));
            else {
                makeAccessibleFromTo.accept(entry.id, entry.attrs.city);
                makeAccessibleFromTo.accept(entry.attrs.city, entry.id);
            }
        });
        return mapNameToNode;
    }

    // because the standard library List.of(...) does not contain such a method
    private static <E> List<E> of(final E element, final List<E> elementsList) {
        List<E> result = new ArrayList<>();
        result.add(element);
        result.addAll(elementsList);
        return result;
    }

    public static List<String> findPath(final String src, final String dest, final List<String> nodeChecked, final List<String> maxPathAccumulator) {
        if (nodeChecked.contains(src) || nodeChecked.contains(dest))
            return new ArrayList<>(); // loop in paths or error in parameters
        if (src.equals(dest)) // reached destination
            return List.of(dest);
        final Node srcNode = mapNameToNode.get(src);
        assert srcNode != null;
        final List<String> newChecked = new ArrayList<>(nodeChecked);
        newChecked.add(src);
        if (newChecked.size() > maxPathAccumulator.size()) {
            // re-create maximum path
            maxPathAccumulator.clear();
            maxPathAccumulator.addAll(newChecked);
        }
        final Map<Node, List<String>> mapNodeToPath = srcNode.accessTo.stream()
                .collect(Collectors.toMap(n -> n, n -> findPath(n.name, dest, newChecked, maxPathAccumulator)));
        final Optional<Map.Entry<Node, List<String>>> shortestPath = mapNodeToPath.entrySet()
                .stream()
                .filter(e -> !e.getValue().isEmpty())
                .min(Comparator.comparing(e -> e.getValue().size()));
        return shortestPath
                .map(Map.Entry::getValue)
                .map(l -> of(src, l))
                .orElse(List.of());
    }
}