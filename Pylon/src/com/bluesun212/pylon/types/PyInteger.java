package com.bluesun212.pylon.types;

public abstract class PyInteger extends PyObject implements PyReadable<Integer> {
	protected int value;

	@Override
	public Integer get() {
		return value;
	}
	
	@Override
	public String toString() {
		return "" + value;
	}
}
