package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyFloat;
import com.sun.jna.Memory;

class Python278Float extends PyFloat {

	@Override
	protected boolean read(MemoryReader mr, long address) {
		this.address = address;
		
		Memory buffer = mr.getExtendedBuffer(address+8, 8);
		value = buffer.getDouble(0);
		return true;
	}

}
