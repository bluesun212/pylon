package com.bluesun212.pylon.types;

public abstract class PyTypeInstance extends PyObject {
	protected PyDict dict;
	
	public PyType getType() {
		return type;
	}
	
	public PyDict getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return "instance of " + type.getName();
	}
}
