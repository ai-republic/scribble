package webui.search;

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;

import sem.api.SemanticNode;

@SessionScoped
public class GraphModelProducer implements Serializable {
  private static final long serialVersionUID = 2321982793435786209L;
  private final GraphModel<SemanticNode> graphModel = new GraphModel<>();

  /**
   * Produces the injectable instance of the {@link GraphModel}.
   * 
   * @return the {@link GraphModel}
   */
  @Produces
  public GraphModel<SemanticNode> getGraphModel() {
    return graphModel;
  }
}
