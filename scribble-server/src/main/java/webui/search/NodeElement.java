package webui.search;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.Element;

public class NodeElement<DATA> extends Element implements Serializable {
	private static final long serialVersionUID = 1690645441322837687L;
	private Map<String, List<Connection>> inputConnections = new HashMap<>();
	private Map<String, List<Connection>> outputConnections = new HashMap<>();


	/**
	 * Constructor.
	 */
	public NodeElement() {
	}


	/**
	 * Constructor setting the node.
	 *
	 * @param node the node
	 */
	public NodeElement(final DATA node) {
		super(node);
	}


	/**
	 * Constructor setting the node.
	 *
	 * @param node the node
	 */
	public NodeElement(final DATA node, final String x, final String y) {
		super(node, x, y);
	}


	/**
	 * @return the node
	 */
	@SuppressWarnings("unchecked")
	@Override
	public DATA getData() {
		return (DATA) super.getData();
	}


	/**
	 * @return the inputs
	 */
	public Map<String, List<Connection>> getInputConnections() {
		return inputConnections;
	}


	/**
	 * @param inputs the inputs to set
	 */
	public void setInputConnections(final Map<String, List<Connection>> inputs) {
		inputConnections = inputs;
	}


	public void addInputConnection(final NodeConnection connection) {
		List<Connection> connections = inputConnections.get(connection.getLinkType());

		if (connections == null) {
			connections = new ArrayList<>();
			inputConnections.put(connection.getLinkType(), connections);
		}

		connections.add(connection);
	}


	public void removeInputConnection(final NodeConnection connection) {
		final List<Connection> connections = inputConnections.get(connection.getLinkType());

		if (connections != null) {
			connections.remove(connection);
		}
	}


	public List<Connection> getInputConnectionsForLinkType(final String linkType) {
		return inputConnections.get(linkType);
	}


	/**
	 * @return the outputs
	 */
	public Map<String, List<Connection>> getOutputConnections() {
		return outputConnections;
	}


	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputConnections(final Map<String, List<Connection>> outputs) {
		outputConnections = outputs;
	}


	public void addOutputConnection(final NodeConnection connection) {
		List<Connection> connections = outputConnections.get(connection.getLinkType());

		if (connections == null) {
			connections = new ArrayList<>();
			outputConnections.put(connection.getLinkType(), connections);
		}

		connections.add(connection);
	}


	public void removeOutputConnection(final NodeConnection connection) {
		final List<Connection> connections = outputConnections.get(connection.getLinkType());

		if (connections != null) {
			connections.remove(connection);
		}
	}


	public List<Connection> getOutputConnectionsForLinkType(final String linkType) {
		return outputConnections.get(linkType);
	}
}
