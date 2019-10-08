package graph.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Interface for a reference resolver which tries to find the most probable node for a linked target reference node.
 *
 * @author Torsten Oltmanns<br/>
 *         (c) Copyright 2015, ai-republic GmbH, Germany
 *
 */
public interface IReferenceResolver<LINK> extends Serializable {

	/**
	 * Resolves the specified link's target reference node and returns an sorted map with all the actual nodes associated with a factor describing the probability. The map is sorted by the highest
	 * probability first.
	 *
	 * @param graph the graph store to search
	 * @param link the link which target node needs to be resolved
	 * @return a sorted map with all the actual nodes associated with a factor describing the probability or <code>null</code> if none could be resolved
	 * @throws IOException if an error occurs
	 */
	@Nonnull
	<NODE> List<ResolveHit<NODE>> resolve(@Nonnull final IGraphStore graph, @Nonnull LINK link) throws IOException;
}
