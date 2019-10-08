package graph.api;

import java.io.Serializable;

/**
 * Defines a hit when resolving a matching node.
 *
 * @author Torsten Oltmanns<br/>
 *         (c) Copyright 2015, ai-republic GmbH, Germany
 *
 */
public class ResolveHit<T> implements Comparable<ResolveHit<?>>, Serializable {
	private static final long serialVersionUID = 1L;
	private float probability;
	private T node;


	public ResolveHit() {
		//
	}


	public ResolveHit(final float probability, final T node) {
		this.probability = probability;
		this.node = node;
	}


	public float getProbability() {
		return probability;
	}


	public void setProbability(final float factor) {
		this.probability = factor;
	}


	public T getNode() {
		return node;
	}


	public void setNode(final T node) {
		this.node = node;
	}


	@Override
	public int compareTo(final ResolveHit<?> o) {
		return probability < o.probability ? 1 : probability == o.probability ? 0 : -1;
	}


	@Override
	public String toString() {
		return "ResolveHit [factor=" + probability + ", node=" + node + "]";
	}
}
