package com.bluesun212.pylon.types;


public abstract class PyFloat extends PyObject implements PyReadable<Double> {
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
