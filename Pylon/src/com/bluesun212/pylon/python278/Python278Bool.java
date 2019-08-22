package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyBool;
import com.bluesun212.pylon.types.PyType;

class Python278Bool extends PyBool {
	public Python278Bool(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		Buffer mem = base.getBuffer();
		int ret = mem.read(address+8);
		mem.unlock();
		value = ret==1;
	}
}
