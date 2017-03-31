package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyObject;

public class Python278Object extends PyObject {

	@Override
	protected boolean read(MemoryReader mr, long address) {
		return true;
	}

}
