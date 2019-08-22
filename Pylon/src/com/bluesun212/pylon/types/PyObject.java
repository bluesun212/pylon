package com.bluesun212.pylon.types;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;

public abstract class PyObject {
	protected long address;
	protected PyType type;
	protected MemoryInterface base;
	
	protected PyObject(MemoryInterface base, long address, PyType type) {
		if (address == 0) {
			throw new IllegalArgumentException("Address is NULL, cannot create object");
		}
		
		this.base = base;
		this.address = address;
		this.type = type;
	}
	
	public long getAddress() {
		return address;
	}
	
	public int readReferenceCount() {
		Buffer b = base.getBuffer();
		int refCnt = b.read(address);
		b.unlock();
		
		return refCnt;
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
