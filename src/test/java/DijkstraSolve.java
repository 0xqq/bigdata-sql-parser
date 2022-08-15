import com.google.common.graph.*;

public final class DijkstraSolve {
    public static void main(String[] args) {
        MutableValueGraph<String, Integer> g = ValueGraphBuilder.directed().build();
        g.putEdgeValue("a", "b", 4);
        g.putEdgeValue("a", "d", 2);
        g.putEdgeValue("c", "b", 3);
        g.putEdgeValue("c", "z", 2);
        g.putEdgeValue("e", "d", 3);
        g.putEdgeValue("e", "z", 1);
        g.putEdgeValue("e", "b", 3);
        g.putEdgeValue("e1", "b", 1);
        g.putEdgeValue("e1", "1b", 3);

        Graph<String> stringGraph = g.asGraph();
        Graphs.reachableNodes(stringGraph, "c").forEach(System.out::println);

        System.out.println("---------");
        Graph<String> transpose = Graphs.transpose(stringGraph);
        Graphs.reachableNodes(transpose, "b").forEach(System.out::println);
    }
}