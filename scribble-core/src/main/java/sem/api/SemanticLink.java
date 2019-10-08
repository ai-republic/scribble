package sem.api;

import graph.api.annotation.GraphLink;
import graph.api.annotation.Id;
import graph.api.annotation.LinkFunction;
import graph.api.annotation.LinkType;
import graph.api.annotation.SourceNode;
import graph.api.annotation.TargetNode;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implements a semantic link associating two nodes linked by relations. The link can have a function and parameters for programmatic changes.
 *
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015 ai-republic GmbH, Germany
 *
 */
@GraphLink
public class SemanticLink implements Serializable {
	private static final long serialVersionUID = 883006359297701223L;
	@Id
	private Object id;
	@SourceNode
	private SemanticNode source;
	@TargetNode
	private SemanticNode target;
	@LinkType
	private String type;
	private Map<String, Object> parameters = new LinkedHashMap<>();
	@LinkFunction
	private ILinkFunction function;


	public SemanticLink() {
	}


	public SemanticLink(final SemanticNode source, final SemanticNode target, final String type) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
	}


	public SemanticLink(final SemanticNode source, final SemanticNode target, final String type, final ILinkFunction function) {
		this.setSource(source);
		this.setTarget(target);
		this.setType(type);
		this.setFunction(function);
	}


	/**
	 * @return the id
	 */
	public Object getId() {
		return id;
	}


	/**
	 * @param id the id to set
	 */
	public void setId(final Object id) {
		this.id = id;
	}


	public void addParameter(final String key, final Object value) {
		parameters.put(key, value);
	}


	/**
	 * @return the parameter
	 */
	public Map<String, Object> getParameters() {
		return parameters;
	}


	/**
	 * @param parameter the parameter to set
	 */
	public void setParameters(final Map<String, Object> parameter) {
		this.parameters = parameter;
	}


	/**
	 * @return the function
	 */
	public ILinkFunction getFunction() {
		return function;
	}


	/**
	 * @param function the function to set
	 */
	public void setFunction(final ILinkFunction function) {
		this.function = function;
	}


	/**
	 * @return the source
	 */
	public SemanticNode getSource() {
		return source;
	}


	/**
	 * @param source the source to set
	 */
	public void setSource(final SemanticNode source) {
		this.source = source;
	}


	/**
	 * @return the target
	 */
	public SemanticNode getTarget() {
		return target;
	}


	/**
	 * @param target the target to set
	 */
	public void setTarget(final SemanticNode target) {
		this.target = target;
	}


	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}


	/**
	 * @param type the type to set
	 */
	public void setType(final String type) {
		this.type = type;
	}


	@Override
	public String toString() {
		return "SemanticLink [id=" + id + ", source=" + source + ", target=" + target + ", type=" + type + ", parameters=" + parameters + ", function=" + function + "]";
	}
}
