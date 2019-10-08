package sem.graph;

import static org.assertj.core.api.Assertions.assertThat;
import graph.api.ResolveHit;
import graph.api.query.Complex;
import graph.api.query.Field;
import graph.api.query.Query;
import graph.orientdb.OrientDbGraphStore;

import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import sem.api.SemanticLink;
import sem.api.SemanticNode;
import sem.api.WordRelationType;
import cditest.CDITestBase;

public class ResolverTest extends CDITestBase {
  private final OrientDbGraphStore graph = inject(OrientDbGraphStore.class);
  private final SemanticLinkReferenceResolver underTest = inject(SemanticLinkReferenceResolver.class);

  @Ignore
  @Test
  public void testCompareAncestors() throws Exception {
    final List<SemanticNode> sources =
        graph.findNodes(
            Query.from(SemanticNode.class.getName()).where(
                Complex.complex(Field.withName("word").isEqualTo("Affe")).and(
                    Field.withName("context").containsText("Zoologie"))), SemanticNode.class);
    final List<SemanticNode> targets =
        graph.findNodes(Query.from(SemanticNode.class.getName()).where(Field.withName("word").isEqualTo("Primat")),
            SemanticNode.class);

    final SemanticNode source = sources.get(0);
    final SemanticNode target = targets.get(0);

    final float result = underTest.compareAncestors(graph, source, target);

    assertThat(result).isGreaterThan(0f);
  }

  @Ignore
  @Test
  public void testResolve() throws Exception {
    final List<SemanticNode> sources =
        graph.findNodes(
            Query.from(SemanticNode.class.getName()).where(
                Complex.complex(Field.withName("word").isEqualTo("Affe")).and(
                    Field.withName("context").containsText("Zoologie"))), SemanticNode.class);
    final List<SemanticNode> targets =
        graph.findNodes(Query.from(SemanticNode.class.getName()).where(Field.withName("word").isEqualTo("Primat")),
            SemanticNode.class);

    final SemanticNode source = sources.get(0);
    final SemanticNode target = targets.get(0);
    final SemanticLink link = new SemanticLink(source, target, WordRelationType.HYPERNYM.name());

    final List<ResolveHit<SemanticNode>> result = underTest.resolve(graph, link);

    assertThat(result).isNotNull();
    assertThat(result).isNotEmpty();
    assertThat(result.iterator().next().getNode().getContext()).contains("Zoologie");
  }
}
