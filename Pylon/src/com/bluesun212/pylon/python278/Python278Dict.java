package com.bluesun212.pylon.python278;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.MemoryInterface.ExtendedBuffer;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyType;

class Python278Dict extends PyDict {
	public Python278Dict(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		if (!update()) {
			throw new IllegalArgumentException("Invalid dict");
		}
	}

	@Override
	public int size() {
		Buffer mem = base.getBuffer();
		int size = mem.read(address+12);
		mem.unlock();
		
		return size;
	}

	@Override
	public boolean update() {
		// Read table ptr
		Buffer mem = base.getBuffer();
		int size = mem.read(address+16)+1;
		int table = mem.read(address+20);
		mem.unlock();
		
		// TODO: Validate in a better way
		if (size < 0 || size > 40000) {
			return false;
		}

		// Read keys into a HashMap
		dict = new HashMap<Integer, Integer>();

		if (size > 0) {
			ExtendedBuffer m = base.getExtendedBuffer(table, size*12);

			for (int i = 0; i < size; i++) {
				int theKey = m.getInt(i*12+4);
				int theValue = m.getInt(i*12+8);

				// Add to list
				if (theKey != 0 && theValue != 0) {
					dict.put(theKey, theValue);
				}
			}
			
			m.unlock();
		}
		
		return true;
	}

	@Override
	public Map<PyObject, PyObject> get() {
		update();
		
		HashMap<PyObject, PyObject> objDict = new HashMap<PyObject, PyObject>();
		for (Entry<Integer, Integer> pair : dict.entrySet()) {
			PyObject key = base.getObject(pair.getKey());
			PyObject value = base.getObject(pair.getValue());

			if (key != null && value != null) {
				objDict.put(key, value);
			}
		}

		return objDict;
	}

	@Override
	public PyObject getPyObject(String val) {
		update();
		
		for (Entry<Integer, Integer> pair : dict.entrySet()) {
			PyObject key = base.getObject(pair.getKey());

			if (key != null && key.toString().equals(val)) {
				return base.getObject(pair.getValue());
			}
		}

		return null;
	}

	@Override
	public PyObject[] getPyObject(String[] vals) {
		update();
		
		PyObject[] ret = new PyObject[vals.length];
		
		for (Entry<Integer, Integer> pair : dict.entrySet()) {
			PyObject key = base.getObject(pair.getKey());
			
			
			if (key != null) {
				int index = arrayIndexOf(vals, key.toString());
				
				if (index != -1) {
					ret[index] = base.getObject(pair.getValue());
				}
			}
		}

		return ret;
	}
	
	private int arrayIndexOf(String[] vals, String val) {
		for (int i = 0; i < vals.length; i++) {
			if (vals[i].equals(val)) {
				return i;
			}
		}
		
		return -1;
	}
}
