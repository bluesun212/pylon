package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyCode;
import com.bluesun212.pylon.types.PyTuple;
import com.bluesun212.pylon.types.PyType;

public class Python278Code extends PyCode {
	public Python278Code(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		Buffer m = base.getBuffer();
		argcount = m.read(address+8);
		nlocals = m.read(address+12);
		stacksize = m.read(address+16);
		flags = m.read(address+20);
		code = base.getObject(m.read(address+24));
		consts = (PyTuple) base.getObject(m.read(address+28));
		names = (PyTuple) base.getObject(m.read(address+32));
		varnames = (PyTuple) base.getObject(m.read(address+36));
		freevars = (PyTuple) base.getObject(m.read(address+40));
		cellvars = (PyTuple) base.getObject(m.read(address+44));
		filename = base.getObject(m.read(address+48)).toString();
		name = base.getObject(m.read(address+52)).toString();
		firstlineno = m.read(address+56);
		lnotab = "";//base.getObject(address+60).toString();
		m.unlock();
	}
}
