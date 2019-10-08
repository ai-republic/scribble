package webui.search;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sem.api.SemanticNode;

@Named
@SessionScoped
public class GraphBb implements Serializable {
  private static final long serialVersionUID = 8125386081892519843L;
  private static final Logger LOG = LoggerFactory.getLogger(GraphBb.class);
  @Inject
  private GraphService graphService;
  @Inject
  private GraphModel<SemanticNode> graphModel;


  /**
   * Gets the context menu for the selected node. If no node is selected <code>null</code> is returned.
   *
   * @return the context menu for the selected node or <code>null</code> if no node is selected
   */
  public MenuModel getMenuModel() {
    // get selected node
    final SemanticNode node = graphModel.getSelectedNode();

    // check if a node was selected
    if (node == null) {
      // otherwise present no context menu
      return null;
    }

    try {
      final MenuModel menuModel = new DefaultMenuModel();

      List<String> linkTypes;

      // get the types of links available for the node
      linkTypes = graphService.getEdgeTypes(node);

      GraphBb.LOG.debug("Link types for node " + node.getId() + ": " + linkTypes);

      // First submenu
      final DefaultSubMenu showEdgesSubmenu = new DefaultSubMenu("Show edges");
      final DefaultSubMenu hideEdgesSubmenu = new DefaultSubMenu("Hide edges");

      // create a show/hide menu for each link type
      for (final String linkType : linkTypes) {
        DefaultMenuItem item = new DefaultMenuItem(linkType);
        item.setCommand("#{graphBb.showEdges}");
        item.setParam("linkType", linkType);
        item.setParam("nodeId", node.getId());
        item.setPartialSubmit(true);
        item.setUpdate(":graphPanel");
        showEdgesSubmenu.addElement(item);

        item = new DefaultMenuItem(linkType);
        item.setCommand("#{graphBb.hideEdges}");
        item.setParam("linkType", linkType);
        item.setParam("nodeId", node.getId());
        item.setPartialSubmit(true);
        item.setUpdate(":graphPanel");
        hideEdgesSubmenu.addElement(item);
      }

      menuModel.addElement(showEdgesSubmenu);
      menuModel.addElement(hideEdgesSubmenu);

      // create a remove node menu item
      final DefaultMenuItem item = new DefaultMenuItem("Remove node");
      item.setCommand("#{graphBb.removeNode()}");
      item.setParam("nodeId", node.getId());
      item.setPartialSubmit(true);
      item.setUpdate(":graphPanel");
      menuModel.addElement(item);

      menuModel.generateUniqueIds();
      return menuModel;
    } catch (final Exception e) {
      GraphBb.LOG.error("Error getting edge-types for node: " + node, e);
      return null;
    }
  }


  /**
   * Call to set the selected node which is determined from the <code>nodeId</code> parameter.
   */
  public void selectNode() {
    try {
      // get the node id from the parameters
      final Map<String, String> params =
          FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
      final String nodeId = params.get("nodeId");

      // set the selected node
      graphModel.setSelectedNode(graphModel.findElement(nodeId).getData());
      GraphBb.LOG.info("Selected node: " + nodeId);
    } catch (final Exception e) {
      GraphBb.LOG.error("Couldn't find diagram element for selected node", e);
    }
    // populate if not already loaded
  }


  /**
   * Gets the model for the graph.
   *
   * @return the {@link GraphModel}
   */
  public GraphModel<SemanticNode> getGraphModel() {
    return graphModel;
  }


  /**
   * Call to show the edge nodes of the node and link type as set in the parameters (linkType/nodeId).
   */
  public void showEdges() {
    final Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    final String linkType = params.get("linkType");
    final String nodeId = params.get("nodeId");
    GraphBb.LOG.debug("Show links for node " + nodeId + " and link-type " + linkType);

    try {
      final NodeElement<SemanticNode> element = graphModel.findElement(nodeId);
      final List<SemanticNode> nodes = graphService.getConnectedNodes(element.getData(), linkType);
      String yPos = element.getY();
      yPos = yPos.substring(0, yPos.length() - 2);

      graphModel.addNodes(nodes, Integer.valueOf(yPos) + 250);

      graphModel.connectNodes(element.getData(), nodes, linkType);

    } catch (final Exception e) {
      GraphBb.LOG.error("Couldn't find connected nodes for node: " + graphModel.getSelectedNode(), e);
    }
  }


  /**
   * Call to hide the edge nodes (and recursively its children) of the node and link type as set in the parameters
   * <code>linkType</code> and <code>nodeId</code>.
   */
  public void hideEdges() {
    final Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    final String linkType = params.get("linkType");
    final String nodeId = params.get("nodeId");
    GraphBb.LOG.debug("Hide links for node " + nodeId + " and link-type " + linkType);

    graphModel.removeEdges(graphModel.findElement(nodeId).getData(), linkType);
  }


  /**
   * Call to remove the node (and recursively its children) as set in the parameter <code>nodeId</code.
   */
  protected void removeNode() {
    final Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
    final String nodeId = params.get("nodeId");
    GraphBb.LOG.debug("Remove node " + nodeId);

    graphModel.removeElement(graphModel.findElement(nodeId));
  }
}
