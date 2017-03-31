package com.bluesun212.pylon.types;

public abstract class PyOldClass extends PyObject {
	protected String name;
	protected PyDict dict;

	public String getName() {
		return name;
	}
	
	public PyDict getDict() {
		return dict;
	}
}
