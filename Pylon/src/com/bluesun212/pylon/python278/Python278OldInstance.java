package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyOldClass;
import com.bluesun212.pylon.types.PyOldInstance;

class Python278OldInstance extends PyOldInstance {
	@Override
	protected boolean read(MemoryReader mr, long address) {
		Buffer mem = mr.getBuffer();
		int classAddr = mem.read(address+8);
		int dictAddr = mem.read(address+12);
		mem.unlock();
		
		// Read class
		PyObject clazz = mr.getObject(classAddr);
		if (clazz instanceof PyOldClass) {
			classobj = (PyOldClass) clazz;
		} else {
			return false;
		}
		
		// Read dict
		PyObject dictObj = mr.getObject(dictAddr);
		if (dictObj instanceof PyDict) {
			dict = (PyDict) dictObj;
		} else {
			return false;
		}
		
		return true;
	}

}
