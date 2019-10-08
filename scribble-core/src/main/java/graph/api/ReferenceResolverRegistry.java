package graph.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.enterprise.context.ApplicationScoped;

/**
 * Registry to produce a {@link IReferenceResolver} for a node class.
 *
 * @author Torsten Oltmanns<br/>
 *         (c) Copyright 2015, ai-republic GmbH, Germany
 *
 */
@ApplicationScoped
public class ReferenceResolverRegistry implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Map<Class<?>, IReferenceResolver<?>> resolvers = new HashMap<>();


	/**
	 * Registers the resolver and creates an instance by calling the default constructor.
	 *
	 * @param resolverClass the {@link IReferenceResolver} class
	 */
	public <LINK> void register(@Nonnull final Class<LINK> linkClass, @Nonnull final Class<? extends IReferenceResolver<LINK>> resolverClass) {
		try {
			resolvers.put(linkClass, resolverClass.newInstance());
		} catch (final Exception e) {
			throw new RuntimeException("ReferenceResolver could not be created in registry: " + resolverClass, e);
		}
	}


	/**
	 * Gets the registered {@link IReferenceResolver} for the specified link.
	 *
	 * @param link the link object
	 * @return the registered {@link IReferenceResolver} for the specified link class or <code>null</code> if no resolver is registered for the link class
	 */
	@SuppressWarnings("unchecked")
	public @Nullable <LINK> IReferenceResolver<LINK> getReferenceResolver(@Nonnull final LINK link) {
		return (IReferenceResolver<LINK>) resolvers.get(link.getClass());
	}
}
