package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyInteger extends PyObject implements PyReadable<Integer> {
	public PyInteger(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

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
