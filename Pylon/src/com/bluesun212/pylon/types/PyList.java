package com.bluesun212.pylon.types;

import java.util.List;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyList extends PyObject implements PyReadable<List<PyObject>>, PyDataStructure {
	public PyList(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}
}
