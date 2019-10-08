package sem.graph;

import graph.api.IGraphStore;
import graph.api.IReferenceResolver;
import graph.api.ResolveHit;
import graph.api.query.Field;
import graph.api.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import sem.api.SemanticLink;
import sem.api.SemanticNode;

/**
 * Implementation for a reference resolver which tries to find the most probable node for a reference node.
 *
 * @author Torsten Oltmanns<br/>
 *         (c) Copyright 2015, ai-republic GmbH, Germany
 *
 */
public class SemanticLinkReferenceResolver implements IReferenceResolver<SemanticLink> {
	private static final long serialVersionUID = 1L;


	@SuppressWarnings("unchecked")
	@Override
	public @Nullable List<ResolveHit<SemanticNode>> resolve(@Nonnull final IGraphStore graph, @Nonnull final SemanticLink link) throws IOException {
		final List<ResolveHit<SemanticNode>> result = new ArrayList<>();
		final SemanticNode target = link.getTarget();
		final Query query = Query.from(target.getClass().getName()).where(Field.withName("word").isEqualTo(target.getWord()));

		// query store
		try {
			final List<SemanticNode> nodes = graph.findNodes(query, SemanticNode.class);

			for (final SemanticNode node : nodes) {
				final float probability = determineProbability(graph, link, node);
				result.add(new ResolveHit<SemanticNode>(probability, node));
			}
		} catch (final Exception e) {
			throw new IOException("Error finding actual target node for link: " + link, e);
		}

		Collections.sort(result);

		return result;
	}


	/**
	 * Tries to determine the probability that the reference node matches to the match node.
	 *
	 * @param graph the graph store
	 * @param refNode the reference node
	 * @param matchNode the match node
	 * @return the probability
	 */
	protected float determineProbability(@Nonnull final IGraphStore graph, final SemanticLink link, final SemanticNode matchNode) {
		final List<String> requiredContext = link.getTarget().getContext();
		final List<String> matchContext = matchNode.getContext();

		// check for similarities in the nouns of the context description
		float probability = compareContexts(requiredContext, matchContext);

		// check if common ancestors (hypernyms) from link-source and matchnode exist
		probability += compareAncestors(graph, link.getSource(), matchNode);

		return probability;
	}


	/**
	 * Compare whether there are matches in the two specified contexts.
	 * 
	 * @param requiredContext the context which is required
	 * @param matchContext the context to compare with the required context
	 * @return the probability
	 */
	protected float compareContexts(final List<String> requiredContext, final List<String> matchContext) {
		float probability = 0f;

		if (matchContext != null && requiredContext != null) {
			for (final String context : requiredContext) {
				if (context != null) {
					if (matchContext.contains(context)) {
						probability += 1f;
					}
				}
			}
		}

		return probability;
	}


	/**
	 * Compares whether the source node and the match node have common ancestors.
	 * 
	 * @param graph the graph store
	 * @param sourceNode the source node
	 * @param matchNode the match node
	 * @return the probability
	 */
	protected float compareAncestors(@Nonnull final IGraphStore graph, final SemanticNode sourceNode, final SemanticNode matchNode) {
		float probability = 0f;
		try {
			// search up to 3 ancestors (hypernyms) of the source node
			final Query sourceQuery = Query.createNativeQuery("select from ( traverse out('HYPERNYM') from " + sourceNode.getId() + " ) where $depth <= 3");
			final List<SemanticNode> sourceResult = graph.findNodes(sourceQuery, SemanticNode.class);

			// search up to 3 ancestors (hypernyms) of the match target node
			final Query targetQuery = Query.createNativeQuery("select from ( traverse out('HYPERNYM') from " + matchNode.getId() + " ) where $depth <= 3");
			final List<SemanticNode> targetResult = graph.findNodes(targetQuery, SemanticNode.class);

			for (final SemanticNode sourceObj : sourceResult) {
				for (final SemanticNode targetObj : targetResult) {
					// compare whether the source and target words match
					if (sourceObj.getWord().equals(targetObj.getWord())) {
						probability += 1f;
						// and - for more accuracy - if the contexts match
						probability += compareContexts(sourceNode.getContext(), targetObj.getContext());
					}
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return probability;
	}
}
