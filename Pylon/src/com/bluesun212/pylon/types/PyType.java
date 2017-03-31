package com.bluesun212.pylon.types;

public abstract class PyType extends PyObject {
	protected long addr;
	protected String name;
	protected String docs;
	protected int dictOffset;
	protected int itemSize;
	protected int basicSize;
	protected int flags;
	protected int methodsAddr;
	protected int dictAddr;
	protected int tp_call;
	protected int tp_string;
	
	public PyType() {
		
	}
	
	public long getAddress() {
		return addr;
	}

	public String getName() {
		return name;
	}
	
	public String getDocstring() {
		return docs;
	}
	
	public int getDictOffset() {
		return dictOffset;
	}
}
