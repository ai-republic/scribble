package webui.search;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import graph.api.query.Query;
import sem.api.SemanticNode;

@Named
@SessionScoped
public class SearchBb implements Serializable {
  private static final long serialVersionUID = 7285921862124468208L;
  private static Logger LOG = LoggerFactory.getLogger(SearchBb.class);
  private String searchText = "";
  @Inject
  private GraphService graphService;
  @Inject
  private GraphModel<SemanticNode> graphModel;


  /**
   * Action to get autocomplete suggestions for the specified sub-string.
   *
   * @param subString the sub-string
   * @return the list of autocomplete suggestions
   */
  public List<String> autoComplete(final String subString) {
    LOG.info("Calling actionAutoComplete...");

    final Query query = Query.createNativeQuery(
        "select word from " + SemanticNode.class.getName() + " where word like '" + subString + "%' limit 10");
    // Query.from(SemanticNode.class.getName()).where(Field.withName("word").like(subString + "%")).limit(10);
    // .orderBy("word", OrderBy.ASC);

    try {
      return graphService.getGraphStore().findNodes(query, String.class);
    } catch (final Exception e) {
      SearchBb.LOG.error("Error searching for autoComplete: " + subString + ", Query: " + query, e);
    }

    return Collections.emptyList();
  }


  /**
   * Action when the search is to be performed.
   */
  public void actionSearch() {
    SearchBb.LOG.info("Calling actionSearch...");

    final Query query =
        Query.createNativeQuery("select from " + SemanticNode.class.getName() + " where word='" + searchText + "'");

    try {
      final List<SemanticNode> result = graphService.getGraphStore().findNodes(query, SemanticNode.class);
      graphModel.newGraph(result);
    } catch (final Exception e) {
      SearchBb.LOG.error("Error searching for text: " + searchText + ", Query: " + query, e);
    }
  }


  public String getSearchText() {
    return searchText;
  }


  public void setSearchText(final String searchText) {
    this.searchText = searchText;
  }
}
