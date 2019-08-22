package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyString extends PyObject implements PyReadable<String> {
	public PyString(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

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
