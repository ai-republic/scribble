package sem.api.importer;

import java.util.Map;
import java.util.Set;

import sem.api.SemanticLink;
import sem.api.SemanticNode;

/**
 * Converts the specified typed instance to a {@link SemanticNode}.
 * 
 * @author Torsten Oltmanns<br>
 *         (c) Copyright 2015 ai-republic GmbH, Germany
 * @param <T> the typed instance to convert
 */
public interface ISemanticItemImportConverter<T> {
	/**
	 * Converts the specified typed instance to a {@link SemanticNode}.
	 * 
	 * @param entry the typed instance
	 * @return a list or single {@link SemanticNode}
	 */
	Map<SemanticNode, Set<SemanticLink>> convert(T entry);
}
