package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyType;
import com.bluesun212.pylon.types.PyTypeInstance;

class Python278TypeInstance extends PyTypeInstance {
	public Python278TypeInstance(MemoryInterface base, long address, PyType type, long dictAddr) {
		super(base, address, type);
		dict = (PyDict) base.getObject(dictAddr);
	}

}
