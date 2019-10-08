package index.api;

public interface IIndexEntry extends Cloneable {
	Object getId();


	void setId(Object id);


	long getStart();


	void setStart(long start);


	long getSize();


	void setSize(long size);


	Object getProperty(String key);


	void setProperty(String key, Object value);


	IIndexEntry clone();
}
