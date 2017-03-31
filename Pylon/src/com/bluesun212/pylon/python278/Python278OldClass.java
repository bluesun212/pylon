package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyOldClass;
import com.bluesun212.pylon.types.PyString;

class Python278OldClass extends PyOldClass {
	@Override
	protected boolean read(MemoryReader mr, long address) {
		// Read class
		Buffer mem = mr.getBuffer();
		int nameAddr = mem.read(address+16);
		mem.unlock();
		
		// TODO: Read in dict too
		PyObject pyString = mr.getObject(nameAddr);
		if (pyString instanceof PyString) {
			name = ((PyString) pyString).get();
		} else {
			return false;
		}
		
		return true;
	}
}
