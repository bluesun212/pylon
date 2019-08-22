package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyBool extends PyObject implements PyReadable<Boolean> {
	public PyBool(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

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
