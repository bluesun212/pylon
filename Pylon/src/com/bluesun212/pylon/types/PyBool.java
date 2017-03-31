package com.bluesun212.pylon.types;

public abstract class PyBool extends PyObject implements PyReadable<Boolean> {
	protected boolean value;

	@Override
	public Boolean get() {
		return value;
	}

	@Override
	public String toString() {
		return "" + value;
	}
}
