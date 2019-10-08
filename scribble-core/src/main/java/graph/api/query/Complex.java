package graph.api.query;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Complex extends Expression {
	private final Map<Type, Expression> callstack = Collections.synchronizedMap(new LinkedHashMap<>());

	public static enum Type {
		START, AND, OR, NOT
	}


	Complex() {
		// default constructor
	}


	public static Complex complex(final Expression exp) {
		final Complex complex = new Complex();
		complex.callstack.put(Type.START, exp);
		return complex;
	}


	public Complex and(final Expression exp) {
		callstack.put(Type.AND, exp);
		return this;
	}


	public Complex or(final Expression exp) {
		callstack.put(Type.OR, exp);
		return this;
	}


	public Complex not(final Expression exp) {
		callstack.put(Type.NOT, exp);
		return this;
	}


	public Map<Type, Expression> getCallstack() {
		return callstack;
	}


	@Override
	public String toString() {
		return "Complex [callstack=" + callstack + "]";
	}

}
