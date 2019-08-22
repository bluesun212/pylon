package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyCode;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyFunction;
import com.bluesun212.pylon.types.PyString;
import com.bluesun212.pylon.types.PyTuple;
import com.bluesun212.pylon.types.PyType;

public class Python278Function extends PyFunction {

	public Python278Function(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		Buffer m = base.getBuffer();
		int codeAddr = m.read(address+8);
		int globalsAddr = m.read(address+12);
		int defaultAddr = m.read(address+16);
		int closureAddr = m.read(address+20);
		int docAddr = m.read(address+24);
		int nameAddr = m.read(address+28);
		int dictAddr = m.read(address+32);
		m.unlock();
		
		code = (PyCode) base.getObject(codeAddr);
		globals = (PyDict) base.getObject(globalsAddr);
		
		try {
			defaults = (PyTuple) base.getObject(defaultAddr);
		} catch (Exception e) {
			//System.err.println("defaults is null");
		}
		
		try {
			closure = (PyTuple) base.getObject(closureAddr);
		} catch (Exception e) {
			//System.err.println("closure is null");
		}
		
		doc = base.getObject(docAddr);
		name = (PyString) base.getObject(nameAddr);
		
		try {
			dict = (PyDict) base.getObject(dictAddr);
		} catch (Exception e) {
			//System.err.println("dict is null");
		}
	}

}
