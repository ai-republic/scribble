package index.api;

import java.io.IOException;

public interface IIndexSegment {
	IIndexEntry get(Object id) throws IOException;


	void add(IIndexEntry indexEntry) throws IOException;


	void remove(Object id) throws IOException;


	void rebuild() throws IOException;


	void close() throws IOException;
}
