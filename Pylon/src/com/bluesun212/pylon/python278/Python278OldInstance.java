package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyOldClass;
import com.bluesun212.pylon.types.PyOldInstance;
import com.bluesun212.pylon.types.PyType;

class Python278OldInstance extends PyOldInstance {
	public Python278OldInstance(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		Buffer mem = base.getBuffer();
		int classAddr = mem.read(address+8);
		int dictAddr = mem.read(address+12);
		mem.unlock();
		
		// Read class
		PyObject clazz = base.getObject(classAddr);
		if (clazz instanceof PyOldClass) {
			classobj = (PyOldClass) clazz;
		} else {
			throw new IllegalArgumentException("Invalid class obj in old instance");
		}
		
		// Read dict
		PyObject dictObj = base.getObject(dictAddr);
		if (dictObj instanceof PyDict) {
			dict = (PyDict) dictObj;
		} else {
			throw new IllegalArgumentException("Invalid dict in old instance");
		}
	}

}
