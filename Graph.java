import java.util.ArrayDeque;
import java.util.Queue;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;

class Graph<Node> {
	static class NodeInfo<Node> {
		final double value;
		final Set<Node> edges;
		NodeInfo(double v) { value = v; edges = new HashSet<Node>(); }
	}

	private final Map<Node, NodeInfo<Node>> nodes = new HashMap<Node, NodeInfo<Node>>();

	void addNode(Node node, double value) {
		nodes.put(node, new NodeInfo<Node>(value));
	}

	void addEdge(Node a, Node b) {
		nodes.get(a).edges.add(b);
		nodes.get(b).edges.add(a);
	}

	List<Node> maxDisconnectedSubset() {
		List<Node> result = new ArrayList<Node>();
		Set<Node> visited = new HashSet<Node>();
		for (Node a : nodes.keySet()) {
			if (!visited.contains(a)) {
				visited.add(a);
				result.addAll(maxSubset(extractComponent(a, visited)).set);
			}
		}
		return result;
	}

	private List<Node> extractComponent(Node a, Set<Node> visited) {
		List<Node> component = new ArrayList<Node>();
		component.add(a);
		Queue<Node> queue = new ArrayDeque<Node>();
		queue.add(a);
		while (!queue.isEmpty()) {
			for (Node c : nodes.get(queue.remove()).edges) {
				if (!visited.contains(c)) {
					visited.add(c);
					queue.add(c);
					component.add(c);
				}
			}
		}
		return component;
	}

	static class Subset<Node> {
		final List<Node> set = new ArrayList<Node>();
		double value = 0;
	}

	private Subset<Node> maxSubset(List<Node> component) {
		if (component.size() == 0) {
			return new Subset<Node>();
		}
		Node a = component.get(0);
		if (component.size() == 1) {
			Subset<Node> s = new Subset<Node>();
			s.set.add(a);
			s.value += nodes.get(a).value;
			return s;
		}

		List<Node> q = component.subList(1, component.size());
		assert q.size() < component.size();
		Subset<Node> sa = maxSubset(q);

		NodeInfo ani = nodes.get(a);
		ArrayList<Node> w = new ArrayList<Node>();
		for (Node n : q) {
			if (!ani.edges.contains(n)) {
				w.add(n);
			}
		}
		assert w.size() < component.size();
		Subset<Node> sb = maxSubset(w);
		sb.set.add(a);
		sb.value += nodes.get(a).value;

		if (sa.value > sb.value) return sa;
		if (sb.value > sa.value) return sb;
		if (sa.set.size() > sb.set.size()) return sa;
		return sb;
	}
}
