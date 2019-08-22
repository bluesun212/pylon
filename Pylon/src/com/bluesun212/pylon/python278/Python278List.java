package com.bluesun212.pylon.python278;

import java.util.LinkedList;
import java.util.List;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyList;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyType;

class Python278List extends PyList {
	private LinkedList<Integer> addrList;
	
	public Python278List(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		if (!update()) {
			throw new IllegalArgumentException("Invalid list");
		}
	}
	
	@Override
	public int size() {
		Buffer mem = base.getBuffer();
		int size = mem.read(address+8);
		mem.unlock();
		
		return size;
	}
	
	@Override
	public boolean update() {
		// Read list size
		Buffer mem = base.getBuffer();
		int size = size();
		
		// Read list address
		int listAddr = mem.read(address+12);
		addrList = new LinkedList<Integer>();
		
		// Read elements
		for (int i = 0; i < size; i++) {
			int objAddr = mem.read(listAddr + i * 4);
			
			// TODO: Check type
			// Invalid list
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
		update();
		
		LinkedList<PyObject> list = new LinkedList<PyObject>();
		for (int i = 0; i < addrList.size(); i++) {
			list.add(base.getObject(addrList.get(i)));
		}
		
		return list;
	}
}
