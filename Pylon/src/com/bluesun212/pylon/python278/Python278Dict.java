package com.bluesun212.pylon.python278;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.sun.jna.Memory;

class Python278Dict extends PyDict {
	private MemoryReader mr;

	@Override
	protected boolean read(MemoryReader mr, long address) {
		this.mr = mr;
		return doRead();
	}

	private boolean doRead() {
		// Read size and table ptr
		Buffer mem = mr.getBuffer();
		int size = mem.read(address+16) * 12;
		int table = mem.read(address+20);
		mem.unlock();

		// TODO: Validate in a better way
		if (size < 0 || size > 40000) {
			return false;
		}

		// Read keys into a HashMap
		dict = new HashMap<Integer, Integer>();

		if (size > 0) {
			Memory m = mr.getExtendedBuffer(table+4, size);

			for (int i = 0; i < size; i += 12) {
				int theKey = m.getInt(i);
				int theValue = m.getInt(i + 4);

				// Add to list
				if (theKey != 0 && theValue != 0) {
					dict.put(theKey, theValue);
				}
			}

			return true;
		}

		return false;
	}

	@Override
	public Map<PyObject, PyObject> get() {
		doRead();
		
		HashMap<PyObject, PyObject> objDict = new HashMap<PyObject, PyObject>();
		for (Entry<Integer, Integer> pair : dict.entrySet()) {
			PyObject key = mr.getObject(pair.getKey());
			PyObject value = mr.getObject(pair.getValue());

			if (key != null && value != null) {
				objDict.put(key, value);
			}
		}

		return objDict;
	}

}
