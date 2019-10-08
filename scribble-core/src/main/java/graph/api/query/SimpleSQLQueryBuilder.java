package graph.api.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;

import graph.api.query.Query.OrderBy;

@ApplicationScoped
public class SimpleSQLQueryBuilder implements IQueryBuilder<String> {
  protected String buildExpression(String result, final Expression expr, final List<Object> param) {
    if (expr instanceof Query) {
      final Query query = (Query) expr;
      return processQuery(result, query, param);
    } else if (expr instanceof Complex) {
      final Complex complex = (Complex) expr;
      result = processComplex(result, complex, param);
    } else if (expr instanceof Field) {
      final Field field = (Field) expr;
      return processField(result, field, param);
    }

    return result;
  }


  @Override
  public String processQuery(final Query query) {
    if (query.getNativeQuery() != null) {
      return query.getNativeQuery();
    }

    if (query.getParameters() == null) {
      query.setParameters(new ArrayList<>());
    }

    return processQuery("", query, query.getParameters());
  }


  protected String processQuery(final String result, final Query query, final List<Object> param) {
    StringBuilder buf = new StringBuilder(result);
    buf.append("SELECT FROM ").append(query.getClassname());

    // WHERE
    if (query.getWhere() != null) {
      buf.append(" WHERE ");
      buf = new StringBuilder(buildExpression(buf.toString(), query.getWhere(), param));
    }

    // SKIP
    if (query.getSkip() != null) {
      buf.append(" SKIP ").append(query.getSkip());
    }

    // LIMIT
    if (query.getLimit() != null) {
      buf.append(" LIMIT ").append(query.getLimit());
    }

    // ORDER BY
    if (!query.getOrderBy().isEmpty()) {
      buf.append(" ORDER BY ");

      final StringBuilder orderBy = new StringBuilder();

      for (final Map.Entry<String, OrderBy> entry : query.getOrderBy().entrySet()) {
        if (orderBy.length() > 0) {
          orderBy.append(", ");
        }

        orderBy.append(entry.getKey()).append(" ").append(entry.getValue().name());
      }

      buf.append(orderBy);
    }

    return buf.toString();
  }


  protected String processComplex(final String result, final Complex complex, final List<Object> param) {
    StringBuilder buf = new StringBuilder(result);

    for (final Map.Entry<Complex.Type, Expression> entry : complex.getCallstack().entrySet()) {
      switch (entry.getKey()) {
        case START:
          buf.append("(");
          break;
        case AND:
          buf.append(" AND ");
          break;
        case OR:
          buf.append(" OR ");
          break;
        case NOT:
          buf.append(" NOT ");
          break;
      }

      buf = new StringBuilder(buildExpression(buf.toString(), entry.getValue(), param));
    }

    buf.append(")");

    return buf.toString();
  }


  protected String processField(final String result, final Field field, final List<Object> param) {
    final StringBuilder buf = new StringBuilder(result);

    buf.append(field.getName());

    switch (field.getComparator()) {
      case EQ:
        buf.append("=?");
        param.add(field.getValue());
        break;
      case NEQ:
        buf.append("!=?");
        param.add(field.getValue());
        break;
      case GT:
        buf.append(">?");
        param.add(field.getValue());
        break;
      case GTE:
        buf.append(">=?");
        param.add(field.getValue());
        break;
      case LT:
        buf.append("<?");
        param.add(field.getValue());
        break;
      case LTE:
        buf.append("<=?");
        param.add(field.getValue());
        break;
      case IN:
        buf.append(" IN (?)");
        param.add(field.getValues());
        break;
      case BETWEEN:
        buf.append(" BETWEEN ? AND ?");
        param.add(field.getValues().get(0));
        param.add(field.getValues().get(1));
        break;
      case NOT_IN:
        buf.append(" NOT IN (?)");
        param.add(field.getValues());
        break;
      case CONTAINS:
        buf.append(" CONTAINS (?)");
        param.add(field.getValues());
        break;
      case CONTAINS_KEY:
        buf.append(" NOT CONTAINS (?)");
        param.add(field.getValues());
        break;
      case CONTAINS_VALUE:
        buf.append(" CONTAINSVALUE ?");
        param.add(field.getValue());
        break;
      case CONTAINS_TEXT:
        buf.append(" CONTAINSTEXT '");
        buf.append(field.getValue());
        buf.append("'");
        break;
      case LIKE:
        buf.append(" LIKE '");
        buf.append(field.getValue());
        buf.append("'");
        break;
      case INSTANCOF:
        buf.append(" INSTANCEOF '");
        buf.append(field.getValue());
        buf.append("'");
        break;
      case IS_NULL:
        buf.append(" IS NULL");
        break;
      case MATCHES:
        buf.append(" MATCHES '");
        buf.append(field.getValue());
        buf.append("'");
        break;
    }

    return buf.toString();
  }


  public static void main(final String[] args) {
    final Query query = Query.from("TEST")
        .where(Complex
            .complex(
                Complex.complex(Field.withName("f1").isEqualTo("test")).and(Field.withName("F2").isLessOrEqualTo(3)))
            .or(Field.withName("F3").notIn(1, 2, 3)));

    final List<Object> param = new ArrayList<>();

    System.out.println("QUERY: " + new SimpleSQLQueryBuilder().processQuery("", query, param) + "\nPARAM: " + param);
  }
}
