package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyOldInstance extends PyObject {
	public PyOldInstance(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	protected PyOldClass classobj;
	protected PyDict dict;
	
	public PyOldClass getClassObject() {
		return classobj;
	}
	
	public PyDict getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return "Old instance of " + classobj.getName();
	}
}
