package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.ExtendedBuffer;
import com.bluesun212.pylon.types.PyFloat;
import com.bluesun212.pylon.types.PyType;

class Python278Float extends PyFloat {

	public Python278Float(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		ExtendedBuffer buffer = base.getExtendedBuffer(address+8, 8);
		value = buffer.getDouble(0);
		buffer.unlock();
	}

}
