package webui.search;

import java.io.Serializable;
import java.util.List;

import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;

import sem.api.Identifiable;

public class GraphModel<NODETYPE extends Identifiable> extends DefaultDiagramModel implements Serializable {
  private static final long serialVersionUID = -2602957669882180200L;
  private NODETYPE selectedNode;


  public GraphModel() {
    setMaxConnections(-1);
    getDefaultConnectionOverlays().add(new ArrowOverlay(20, 20, 1, 1));

    final StraightConnector connector = new StraightConnector();
    connector.setPaintStyle("{strokeStyle:'#98AFC7', lineWidth:1}");
    connector.setHoverPaintStyle("{strokeStyle:'#5C738B'}");
    setDefaultConnector(connector);
  }


  /**
   * Gets the selected node or <code>null</code>.
   *
   * @return the selected node or <code>null</code>
   */
  public NODETYPE getSelectedNode() {
    return selectedNode;
  }


  /**
   * Sets the selected node.
   *
   * @param selectedNode the node
   */
  public void setSelectedNode(final NODETYPE selectedNode) {
    this.selectedNode = selectedNode;
  }


  /**
   * Clears the existing model and initializes it with the list of nodes.
   *
   * @param nodes the nodes for the new graph
   */
  public void newGraph(final List<NODETYPE> nodes) {
    clear();
    addNodes(nodes, 0);
  }


  /**
   * Adds the nodes to the graph horizontally in a line at the specified y-position.
   *
   * @param nodes the nodes
   * @param y the y-position
   */
  public void addNodes(final List<NODETYPE> nodes, final int y) {
    int x = 0;

    for (final NODETYPE node : nodes) {
      final NodeElement<NODETYPE> element = new NodeElement<NODETYPE>(node, (x += 300) + "px", (y + 50) + "px");
      element.setId(node.getId().toString());
      element.setDraggable(true);
      element.addEndPoint(createDotEndPoint(element, EndPointAnchor.AUTO_DEFAULT));
      addElement(element);
    }
  }


  /**
   * Connects the source node to the list of target nodes.
   *
   * @param source the source node
   * @param targetNodes the target nodes
   */
  public void connectNodes(final NODETYPE source, final List<NODETYPE> targetNodes, final String linkType) {
    // find source element
    final NodeElement<NODETYPE> sourceElement = findElement(source.getId().toString());

    for (final NODETYPE target : targetNodes) {
      // find the target element
      final NodeElement<NODETYPE> targetElement = findElement(target.getId().toString());
      final NodeConnection connection =
          new NodeConnection(sourceElement.getEndPoints().get(0), targetElement.getEndPoints().get(0), linkType);

      // add the connection as outgoing reference of the source
      sourceElement.addOutputConnection(connection);

      // add the connection as incoming reference of the target
      targetElement.addInputConnection(connection);

      // connect the elements
      connect(connection);
    }
  }


  private EndPoint createDotEndPoint(final NodeElement<NODETYPE> element, final EndPointAnchor anchor) {
    final NodeEndPoint<NODETYPE> endPoint = new NodeEndPoint<NODETYPE>(element, anchor);
    endPoint.setScope("node");
    endPoint.setSource(true);
    endPoint.setTarget(true);
    endPoint.setRadius(2);
    endPoint.setStyle("{fillStyle:'#98AFC7'}");
    endPoint.setHoverStyle("{fillStyle:'#5C738B'}");

    return endPoint;
  }


  @SuppressWarnings("unchecked")
  @Override
  public NodeElement<NODETYPE> findElement(final String id) {
    return (NodeElement<NODETYPE>) super.findElement(id);
  }


  @SuppressWarnings("unchecked")
  @Override
  public void disconnect(final Connection connection) {
    final NodeElement<NODETYPE> source = ((NodeEndPoint<NODETYPE>) connection.getSource()).getElement();
    final NodeElement<NODETYPE> target = ((NodeEndPoint<NODETYPE>) connection.getTarget()).getElement();

    source.removeOutputConnection((NodeConnection) connection);
    target.removeInputConnection((NodeConnection) connection);

    super.disconnect(connection);
  }


  /**
   * Removes the edge nodes (and recursively its children) of the specified node and link type.
   *
   * @param node the node
   * @param linkType the link type
   */
  @SuppressWarnings("unchecked")
  public void removeEdges(final NODETYPE node, final String linkType) {
    final NodeElement<NODETYPE> element = findElement(node.getId().toString());

    final List<Connection> connections = element.getOutputConnectionsForLinkType(linkType);

    if (connections != null) {
      for (final Connection connection : connections) {
        final NodeElement<NODETYPE> targetElement = ((NodeEndPoint<NODETYPE>) connection.getTarget()).getElement();
        removeEdges(targetElement.getData(), linkType);
        disconnect(connection);

        if (targetElement.getInputConnections().isEmpty()) {
          removeElement(targetElement);
        }
      }
    }
  }
}
