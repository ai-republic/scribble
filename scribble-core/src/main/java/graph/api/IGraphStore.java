package graph.api;

import graph.api.query.Query;

import java.io.IOException;
import java.util.List;

/**
 * Defines the methods to access the graph-store.
 *
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015 ai-republic GmbH, Germany
 *
 */
public interface IGraphStore {
	/**
	 * Stores (create or update) the specified node in the graph.
	 *
	 * @param node the node to store
	 * @throws IOException if an error occurs storing the node
	 */
	String persistNode(Object node) throws IOException;


	/**
	 * Stores the specified link in the graph.
	 *
	 * @param link the link
	 * @throws IOException if an error occurs storing the link
	 */
	String persistLink(final Object link) throws IOException;


	/**
	 * Finds nodes in the graph matching the specified query.
	 *
	 * @param query the query
	 * @param resultType the type of the result
	 * @return the nodes matching the query
	 * @throws Exception if an error occurs
	 */
	<RESULT> List<RESULT> findNodes(Query query, Class<RESULT> resultType) throws Exception;


	/**
	 * Finds links in the graph matching the specified query.
	 *
	 * @param query the query
	 * @param resultType the type of the result
	 * @return the nodes matching the query
	 * @throws Exception if an error occurs
	 */
	<RESULT> List<RESULT> findLinks(Query query, Class<RESULT> resultType) throws Exception;


	/**
	 * Find the object in the graph for the specified id.
	 *
	 * @param id the id
	 * @return the object or null
	 * @throws Exception if an error occurs storing the object
	 */
	Object findNodeById(Object id) throws Exception;


	/**
	 * Gets the types of edges for the node with the specified id.
	 * 
	 * @param id the nodes id
	 * @return the list of edge types
	 * @throws Exception if an error occurs
	 */
	List<String> getLinkTypes(Object id) throws Exception;


	/**
	 * Gets the connected nodes from the specified node with the specified link type.
	 * 
	 * @param id the node id
	 * @param linkType the link type
	 * @return the list of connected nodes
	 * @throws Exception if an error occurs
	 */
	<T> List<T> getConnectedNodes(final Object id, final String linkType) throws Exception;


	/**
	 * Closes the graph store.
	 *
	 * @throws IOException if an error occurs closing the store
	 */
	void close() throws IOException;

}
