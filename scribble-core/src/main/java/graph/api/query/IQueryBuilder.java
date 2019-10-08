package graph.api.query;

public interface IQueryBuilder<T> {
	T processQuery(Query query);
}
