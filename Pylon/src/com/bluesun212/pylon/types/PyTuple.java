package com.bluesun212.pylon.types;

import java.util.List;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyTuple extends PyObject implements PyReadable<List<PyObject>>, PyDataStructure {
	public PyTuple(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}
	
}