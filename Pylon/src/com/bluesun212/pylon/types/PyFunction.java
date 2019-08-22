package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyFunction extends PyObject {
	protected PyCode code;
	protected PyDict globals;
	protected PyTuple defaults;
	protected PyTuple closure;
	protected PyObject doc;
	protected PyString name;
	protected PyDict dict;
	
	public PyFunction(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	public PyCode getCode() {
		return code;
	}

	public PyDict getGlobals() {
		return globals;
	}

	public PyTuple getDefaults() {
		return defaults;
	}

	public PyTuple getClosure() {
		return closure;
	}

	public PyObject getDoc() {
		return doc;
	}

	public PyString getName() {
		return name;
	}

	public PyDict getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return "def " + getName() + "@" + Long.toHexString(getAddress());
	}
}
