package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyOldClass;
import com.bluesun212.pylon.types.PyString;
import com.bluesun212.pylon.types.PyTuple;
import com.bluesun212.pylon.types.PyType;

class Python278OldClass extends PyOldClass {
	public Python278OldClass(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		// Read class
		Buffer mem = base.getBuffer();
		int basesAddr = mem.read(address+8);
		int dictAddr = mem.read(address+12);
		int nameAddr = mem.read(address+16);
		mem.unlock();
		
		PyObject basesObj = base.getObject(basesAddr);
		PyObject nameObj = base.getObject(nameAddr);
		PyObject dictObj = base.getObject(dictAddr);
		if (basesObj instanceof PyTuple) {
			bases = ((PyTuple) basesObj).get();
		} else {
			throw new IllegalArgumentException("Invalid old class");
		}
		
		if (nameObj instanceof PyString) {
			name = ((PyString) nameObj).get();
		} else {
			throw new IllegalArgumentException("Invalid old class");
		}
		
		if (dictObj instanceof PyDict) {
			dict = (PyDict) dictObj;
		} else {
			throw new IllegalArgumentException("Invalid old class");
		}
	}
}
