package graph.api.query;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Query extends Expression {
	private String classname;
	private Expression where;
	private String groupBy;
	private final Map<String, OrderBy> orderBy = new LinkedHashMap<>();
	private Long limit;
	private Long skip;
	private String nativeQuery;
	private List<Object> parameters = new ArrayList<>();

	public static enum OrderBy {
		ASC, DESC
	}


	Query() {
		// default constructor
	}


	public static Query from(final String name) {
		final Query query = new Query();
		query.classname = name;
		return query;
	}


	public static Query createNativeQuery(final String nativeQuery) {
		final Query query = new Query();
		query.nativeQuery = nativeQuery;
		return query;
	}


	public static Query createNativeQuery(final String nativeQuery, final List<Object> parameters) {
		final Query query = new Query();
		query.nativeQuery = nativeQuery;
		query.parameters = parameters;
		return query;
	}


	public Query where(final Expression exp) {
		this.where = exp;
		return this;
	}


	public Query groupBy(final String groupBy) {
		this.groupBy = groupBy;
		return this;
	}


	public Query orderBy(final String orderBy, final OrderBy direction) {
		this.orderBy.put(orderBy, direction);
		return this;
	}


	public Query limit(final long limit) {
		this.limit = limit;
		return this;
	}


	public Query skip(final long skip) {
		this.skip = skip;
		return this;
	}


	public String getClassname() {
		return classname;
	}


	public Expression getWhere() {
		return where;
	}


	public String getGroupBy() {
		return groupBy;
	}


	public Map<String, OrderBy> getOrderBy() {
		return orderBy;
	}


	public Long getLimit() {
		return limit;
	}


	public Long getSkip() {
		return skip;
	}


	public String getNativeQuery() {
		return nativeQuery;
	}


	public List<Object> getParameters() {
		return parameters;
	}


	public void setParameters(final List<Object> parameters) {
		this.parameters = parameters;
	}


	@Override
	public String toString() {
		return "Query [classname=" + classname + ", where=" + where + ", groupBy=" + groupBy + ", orderBy=" + orderBy + ", limit=" + limit + ", skip=" + skip + "]";
	}
}
