package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyTypeInstance;

class Python278TypeInstance extends PyTypeInstance {
	private long dictAddr;

	public Python278TypeInstance(long dictAddr) {
		this.dictAddr = dictAddr;
	}

	@Override
	protected boolean read(MemoryReader mr, long address) {
		PyObject theDict = mr.getObject(dictAddr);
		
		if (theDict instanceof PyDict) {
			dict = (PyDict) theDict;
			return true;
		}
		
		return false;
	}

}
