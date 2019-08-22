package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyType;

class Python278Type extends PyType {
	
	public Python278Type(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		if (type == null) {
			this.type = this;
		}
		
		// Read data from the struct in memory
		Buffer b = base.getBuffer();
		
		addr = address;
		name = base.readNTString(b.read(address+12));
		basicSize = b.read(address+16);
		itemSize = b.read(address+20);
		flags = b.read(address+84);
		
		// Validate name
		if (name.length() <= 0) {
			b.unlock();
			throw new IllegalArgumentException("Invalid type object");
		}
		
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			
			if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '.' && c != '_') {
				b.unlock();
				throw new IllegalArgumentException("Invalid type object");
			}
		}
		
		
		int docsAddr = b.read(address+88);
		if (docsAddr != 0) {
			docs = base.readNTString(docsAddr, 1024);
		}
		
		tp_hash = b.read(address+60);
		tp_call = b.read(address+64);
		tp_string = b.read(address+68);
		tp_getattro = b.read(address+72);
		
		methodsAddr = b.read(address+116);
		membersAddr = b.read(address+120);
		dictAddr = b.read(address+132);
		dictOffset = b.read(address+144);
		mro = b.read(address+144+28);
		
		b.unlock();
	}
}