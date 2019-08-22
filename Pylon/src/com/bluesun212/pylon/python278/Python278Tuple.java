package com.bluesun212.pylon.python278;

import java.util.LinkedList;
import java.util.List;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyTuple;
import com.bluesun212.pylon.types.PyType;

class Python278Tuple extends PyTuple {
	private LinkedList<Integer> addrList;
	
	public Python278Tuple(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
		
		// Read list size
		Buffer mem = base.getBuffer();
		int size = mem.read(address+8);
		
		// Read pointers in list
		addrList = new LinkedList<Integer>();
		for (int i = 0; i < size; i++) {
			int objAddr = mem.read(address + 12 + i * 4);
			
			// Invalid list
			// TODO: getTypeOf(objAddr) == null
			if (objAddr == 0) {
				mem.unlock();
				throw new IllegalArgumentException("Invalid tuple");
			}
			
			addrList.add(objAddr);
		}
		
		mem.unlock();
	}
	
	@Override
	public boolean update() {
		// Tuples are immutable, so they'll never change
		return true;
	}

	@Override
	public int size() {
		return addrList.size();
	}

	@Override
	public List<PyObject> get() {
		LinkedList<PyObject> list = new LinkedList<PyObject>();
		for (int i = 0; i < addrList.size(); i++) {
			list.add(base.getObject(addrList.get(i)));
		}
		
		return list;
	}
}
