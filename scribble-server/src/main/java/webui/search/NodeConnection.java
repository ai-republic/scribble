package webui.search;

import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.endpoint.EndPoint;

public class NodeConnection extends Connection {
	private static final long serialVersionUID = -507324903011740387L;
	private String linkType;


	public NodeConnection() {
	}


	public NodeConnection(final EndPoint source, final EndPoint target, final String linkType) {
		super(source, target);
		this.linkType = linkType;
	}


	/**
	 * @return the linkType
	 */
	public String getLinkType() {
		return linkType;
	}


	/**
	 * @param linkType the linkType to set
	 */
	public void setLinkType(final String linkType) {
		this.linkType = linkType;
	}

}
