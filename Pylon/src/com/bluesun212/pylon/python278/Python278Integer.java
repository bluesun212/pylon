package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyInteger;
import com.bluesun212.pylon.types.PyType;

class Python278Integer extends PyInteger {
	public Python278Integer(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		Buffer mem = base.getBuffer();
		this.value = mem.read(address+8);
		mem.unlock();
	}

}
