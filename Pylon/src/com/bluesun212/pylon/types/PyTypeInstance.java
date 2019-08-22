package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyTypeInstance extends PyObject {
	public PyTypeInstance(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	protected PyDict dict;
	
	public PyType getType() {
		return type;
	}
	
	public PyDict getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return "instance of " + type.getName();
	}
}
