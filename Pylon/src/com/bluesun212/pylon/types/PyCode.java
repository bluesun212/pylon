package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyCode extends PyObject {
	protected int argcount;
	protected int nlocals;
	protected int stacksize;
	protected int flags;
	protected PyObject code;
	protected PyTuple consts;
	protected PyTuple names;
	protected PyTuple varnames;
	protected PyTuple freevars;
	protected PyTuple cellvars;
	protected String filename;
	protected String name;
	protected int firstlineno;
	protected String lnotab;
	
	public PyCode(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	public int getArgCount() {
		return argcount;
	}

	public int getLocalCount() {
		return nlocals;
	}

	public int getStackSize() {
		return stacksize;
	}

	public int getFlags() {
		return flags;
	}

	public PyObject getCodeObject() {
		return code;
	}

	public PyTuple getConstants() {
		return consts;
	}

	public PyTuple getNames() {
		return names;
	}

	public PyTuple getVarNames() {
		return varnames;
	}

	public PyTuple getFreeVars() {
		return freevars;
	}

	public PyTuple getCellVars() {
		return cellvars;
	}
	
	public String getFileName() {
		return filename;
	}

	public String getName() {
		return name;
	}

	public int getFirstLineNo() {
		return firstlineno;
	}

	public String getLnotab() {
		return lnotab;
	}	
}
