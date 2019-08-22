package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;

public abstract class PyType extends PyObject {
	public PyType(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	protected long addr;
	protected String name;
	protected String docs;
	protected int dictOffset;
	protected int itemSize;
	protected int basicSize;
	protected int flags;
	protected int methodsAddr;
	protected int membersAddr;
	protected int dictAddr;
	protected int tp_hash;
	protected int tp_call;
	protected int tp_string;
	protected int tp_getattro;
	protected int tp_setattro;
	protected int mro;
	
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
	
	public int getDictAddr() {
		return dictAddr;
	}
	
	public int getMethodsAddr() {
		return methodsAddr;
	}
	
	public int getMembersAddr() {
		return membersAddr;
	}
	
	public int getAttroFunc() {
		return tp_getattro;
	}
	
	public int getCallFunc() {
		return tp_call;
	}
	
	public int getStrFunc() {
		return tp_string;
	}
	
	public int getHashFunc() {
		return tp_hash;
	}
	
	public int getMRO() {
		return mro;
	}
	
	@Override
	public String toString() {
		return "Type " + name + "@" + Long.toHexString(address);
	}
}
