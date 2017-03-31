package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyString;
import com.sun.jna.Memory;

class Python278String extends PyString {
	@Override
	protected boolean read(MemoryReader mr, long address) {
		// Read string length
		Buffer mem = mr.getBuffer();
		int length = mem.read(address+8);
		mem.unlock();
		
		if (length <= 0) {
			val = "";
			return true;
		}
		
		// Read the string data
		Memory strData = mr.getExtendedBuffer(address+20, length); // 24 not 20 in py3
		String raw;
		try {
			raw = strData.getString(0);
		} catch (Throwable e) {
			return false;
		}
		
		// Truncate string
		if (raw.length() > length) {
			raw = raw.substring(0, length);
		}
		
		val = raw;
		return true;
	}
}
