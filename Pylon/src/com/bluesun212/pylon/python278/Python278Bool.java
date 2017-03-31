package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyBool;

class Python278Bool extends PyBool {
	@Override
	protected boolean read(MemoryReader mr, long address) {
		this.address = address;
		
		Buffer mem = mr.getBuffer();
		int ret = mem.read(address+8);
		mem.unlock();
		value = ret==1;
		return true;
	}

}
