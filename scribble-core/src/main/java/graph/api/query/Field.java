package graph.api.query;

import java.util.Arrays;
import java.util.List;

public class Field extends Expression {
	private String name;
	private Object value;
	private List<Object> values;
	private Comparator comparator;

	public static enum Comparator {
		EQ, NEQ, LT, LTE, GT, GTE, BETWEEN, CONTAINS, CONTAINS_KEY, CONTAINS_VALUE, CONTAINS_TEXT, IN, NOT_IN, LIKE, IS_NULL, MATCHES, INSTANCOF
	}


	Field() {
		// default constructor
	}


	public static Field withName(final String name) {
		final Field field = new Field();
		field.name = name;
		return field;
	}


	public Field isEqualTo(final Object value) {
		this.comparator = Comparator.EQ;
		this.value = value;
		return this;
	}


	public Field isNotEqualTo(final Object value) {
		this.comparator = Comparator.NEQ;
		this.value = value;
		return this;
	}


	public Field isLessThan(final Object value) {
		this.comparator = Comparator.LT;
		this.value = value;
		return this;
	}


	public Field isLessOrEqualTo(final Object value) {
		this.comparator = Comparator.LTE;
		this.value = value;
		return this;
	}


	public Field isGreaterThan(final Object value) {
		this.comparator = Comparator.GT;
		this.value = value;
		return this;
	}


	public Field isGreaterOrEqualTo(final Object value) {
		this.comparator = Comparator.GTE;
		this.value = value;
		return this;
	}


	public Field isInBetween(final Object start, final Object end) {
		this.comparator = Comparator.GTE;
		this.values = Arrays.asList(start, end);
		return this;
	}


	public Field contains(final Object... values) {
		this.comparator = Comparator.CONTAINS;
		this.values = Arrays.asList(values);
		return this;
	}


	public Field containsKey(final String value) {
		this.comparator = Comparator.CONTAINS_KEY;
		this.value = value;
		return this;
	}


	public Field containsValue(final Object value) {
		this.comparator = Comparator.CONTAINS_VALUE;
		this.value = value;
		return this;
	}


	public Field containsText(final String value) {
		this.comparator = Comparator.CONTAINS_TEXT;
		this.value = value;
		return this;
	}


	public Field in(final Object... values) {
		this.comparator = Comparator.IN;
		this.values = Arrays.asList(values);
		return this;
	}


	public Field notIn(final Object... values) {
		this.comparator = Comparator.NOT_IN;
		this.values = Arrays.asList(values);
		return this;
	}


	public Field like(final String value) {
		this.comparator = Comparator.LIKE;
		this.value = value;
		return this;
	}


	public Field isNull() {
		this.comparator = Comparator.IS_NULL;
		return this;
	}


	public Field matches(final String value) {
		this.comparator = Comparator.MATCHES;
		this.value = value;
		return this;
	}


	public String getName() {
		return name;
	}


	public Comparator getComparator() {
		return comparator;
	}


	public Object getValue() {
		return value;
	}


	public List<Object> getValues() {
		return values;
	}


	@Override
	public String toString() {
		return "Field [name=" + name + ", comparator=" + comparator + ", value=" + value + ", values=" + values + "]";
	}
}
