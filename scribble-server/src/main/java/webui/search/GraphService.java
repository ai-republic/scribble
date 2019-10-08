package webui.search;

import graph.orientdb.OrientDbGraphStore;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import sem.api.SemanticNode;

@ApplicationScoped
public class GraphService {
	@Inject
	private OrientDbGraphStore graphStore;


	public OrientDbGraphStore getGraphStore() {
		return graphStore;
	}


	public void setGraphStore(final OrientDbGraphStore graphStore) {
		this.graphStore = graphStore;
	}


	public List<String> getEdgeTypes(final SemanticNode node) throws Exception {
		return graphStore.getLinkTypes(node.getId());
	}


	public List<SemanticNode> getConnectedNodes(final SemanticNode node, final String edgeType) throws Exception {
		// switch (edgeType) {
		// case "TRANSLATION": {
		// final SemanticNode t1 = new SemanticNode("Test_T1");
		// t1.setId("t1");
		// t1.setContext(Arrays.asList("Translation1"));
		// final SemanticNode t2 = new SemanticNode("Test_T2");
		// t2.setId("t2");
		// t2.setContext(Arrays.asList("Translation2"));
		// return Arrays.asList(t1, t2);
		// }
		// case "HYPONYM": {
		// final SemanticNode hypo1 = new SemanticNode("Test_Hypo1");
		// hypo1.setId("hypo1");
		// hypo1.setContext(Arrays.asList("Hypo1"));
		// final SemanticNode hypo2 = new SemanticNode("Test_Hypo2");
		// hypo2.setId("hypo2");
		// hypo2.setContext(Arrays.asList("Hypo2"));
		// return Arrays.asList(hypo1, hypo2);
		// }
		// case "HYPERNYM": {
		// final SemanticNode hyper1 = new SemanticNode("Test_Hyper1");
		// hyper1.setId("hyper1");
		// hyper1.setContext(Arrays.asList("Hyper1"));
		// return Arrays.asList(hyper1);
		// }
		// default:
		// return Collections.emptyList();
		// }

		final List<SemanticNode> nodes = graphStore.getConnectedNodes(node.getId(), edgeType);
		return nodes;
	}
}
