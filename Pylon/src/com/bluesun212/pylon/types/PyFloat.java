package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyFloat extends PyObject implements PyReadable<Double> {
	public PyFloat(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	protected double value;
	
	@Override
	public Double get() {
		return value;
	}

	@Override
	public String toString() {
		return "" + value;
	}
}
