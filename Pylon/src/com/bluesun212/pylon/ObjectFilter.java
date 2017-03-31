package com.bluesun212.pylon;

import com.bluesun212.pylon.types.PyObject;

public interface ObjectFilter {
	public abstract boolean filter(PyObject obj);
}