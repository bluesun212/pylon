package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyInteger;

class Python278Integer extends PyInteger {
	@Override
	protected boolean read(MemoryReader mr, long address) {
		this.address = address;
		
		Buffer mem = mr.getBuffer();
		this.value = mem.read(address+8);
		mem.unlock();
		return true;
	}

}
