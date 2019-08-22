package com.bluesun212.pylon.types;

import java.util.List;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyOldClass extends PyObject {
	public PyOldClass(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	protected String name;
	protected PyDict dict;
	protected List<PyObject> bases;

	public List<PyObject> getBaseObjects() {
		return bases;
	}
	
	public String getName() {
		return name;
	}
	
	public PyDict getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return "Old class " + name + "@" + Long.toHexString(address);
	}
}
