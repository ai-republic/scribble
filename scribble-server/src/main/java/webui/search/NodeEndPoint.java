package webui.search;

import org.primefaces.model.diagram.endpoint.DotEndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;

public class NodeEndPoint<NODETYPE> extends DotEndPoint {
	private static final long serialVersionUID = 1497802003335994240L;
	private NodeElement<NODETYPE> element;


	public NodeEndPoint() {
		super(EndPointAnchor.AUTO_DEFAULT);
	}


	public NodeEndPoint(final NodeElement<NODETYPE> element, final EndPointAnchor anchor) {
		super(anchor);
		this.element = element;
	}


	/**
	 * @return the element
	 */
	public NodeElement<NODETYPE> getElement() {
		return element;
	}


	/**
	 * @param element the element to set
	 */
	public void setElement(final NodeElement<NODETYPE> element) {
		this.element = element;
	}

}
