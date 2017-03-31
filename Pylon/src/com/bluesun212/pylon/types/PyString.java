package com.bluesun212.pylon.types;

public abstract class PyString extends PyObject implements PyReadable<String> {
	protected String val;
	
	@Override
	public String get() {
		return val;
	}
	
	@Override
	public String toString() {
		return val;
	}
}
