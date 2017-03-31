package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyType;

class Python278Type extends PyType {
	public Python278Type() {
		
	}
	
	@Override
	protected boolean read(MemoryReader mr, long address) {
		// Read data from the struct in memory
		Buffer b = mr.getBuffer();
		
		addr = address;
		name = mr.readNTString(b.read(address+12));
		basicSize = b.read(address+16);
		itemSize = b.read(address+20);
		flags = b.read(address+84);
		
		// Validate name
		if (name.length() <= 0) {
			b.unlock();
			return false;
		}
		
		for (int i = 0; i < name.length(); i++) {
			char c = name.charAt(i);
			
			if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '.' && c != '_') {
				b.unlock();
				return false;
			}
		}
		
		
		int docsAddr = b.read(address+92);
		if (docsAddr != 0) {
			docs = mr.readNTString(docsAddr, 1024);
		}
		
		tp_call = b.read(address+64);
		tp_string = b.read(address+68);
		
		methodsAddr = b.read(address+120);
		dictAddr = b.read(address+136);
		dictOffset = b.read(address+144);
		
		b.unlock();
		return true;
	}
}