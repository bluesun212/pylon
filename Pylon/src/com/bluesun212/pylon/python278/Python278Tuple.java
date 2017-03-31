package com.bluesun212.pylon.python278;

import java.util.LinkedList;
import java.util.List;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyList;
import com.bluesun212.pylon.types.PyObject;

class Python278Tuple extends PyList {
	private LinkedList<Integer> addrList;
	private MemoryReader mr;
	
	@Override
	protected boolean read(MemoryReader mr, long address) {
		this.mr = mr;
		
		// Read list size
		Buffer mem = mr.getBuffer();
		int size = mem.read(address+8);
		
		// Read pointers in list
		addrList = new LinkedList<Integer>();
		for (int i = 0; i < size; i++) {
			int objAddr = mem.read(address + 12 + i * 4);
			
			// Invalid list
			// TODO: getTypeOf(objAddr) == null
			if (objAddr == 0) {
				mem.unlock();
				return false;
			}
			
			addrList.add(objAddr);
		}
		
		mem.unlock();
		return true;
	}

	@Override
	public List<PyObject> get() {
		LinkedList<PyObject> list = new LinkedList<PyObject>();
		for (int i = 0; i < addrList.size(); i++) {
			list.add(mr.getObject(addrList.get(i)));
		}
		
		return list;
	}

}
