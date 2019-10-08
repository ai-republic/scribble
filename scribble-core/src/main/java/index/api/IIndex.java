package index.api;

import java.io.IOException;

public interface IIndex {
	void open() throws IOException;


	void add(IIndexEntry indexEntry) throws IOException;


	IIndexEntry find(Object id) throws IOException;


	void remove(Object id) throws IOException;


	void close() throws IOException;

}
