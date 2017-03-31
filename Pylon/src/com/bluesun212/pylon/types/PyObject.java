package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryReader;

public abstract class PyObject {
	protected int refCount;
	protected long address;
	protected PyType type;
	
	public PyObject() {
		
	}
	
	public void setHead(int refCount, long address, PyType type) {
		if (this.address == 0) {
			this.refCount = refCount;
			this.address = address;
			this.type = type;
		}
	}
	
	protected abstract boolean read(MemoryReader mr, long address);
	
	public long getAddress() {
		return address;
	}
	
	public int getReferenceCount() {
		return refCount;
	}
	
	public PyType getType() {
		return type;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof PyObject) {
			return address == ((PyObject) other).address;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return type.getName() + "@" + Long.toHexString(address);
	}
}
