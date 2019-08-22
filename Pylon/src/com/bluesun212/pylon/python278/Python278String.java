package com.bluesun212.pylon.python278;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.MemoryInterface.ExtendedBuffer;
import com.bluesun212.pylon.types.PyString;
import com.bluesun212.pylon.types.PyType;

class Python278String extends PyString {
	public Python278String(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		// Read string length
		Buffer mem = base.getBuffer();
		int length = mem.read(address+8);
		mem.unlock();
		
		if (length <= 0) {
			val = "";
		} else {
			// Read the string data
			ExtendedBuffer strData = base.getExtendedBuffer(address+20, length); // 24 not 20 in py3
			String raw;
			try {
				byte[] buff = strData.getByteArray(0, length);
				raw = new String(buff, "ISO-8859-1");
			} catch (Throwable e) {
				throw new IllegalArgumentException("Invalid string");
			} finally {
				strData.unlock();
			}
			
			// Truncate string
			if (raw.length() > length) {
				raw = raw.substring(0, length);
			}
			
			val = raw;
		}
	}
}
