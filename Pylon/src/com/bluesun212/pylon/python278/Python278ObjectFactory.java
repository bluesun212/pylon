package com.bluesun212.pylon.python278;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.ObjectAllocator;
import com.bluesun212.pylon.ObjectFactory;
import com.bluesun212.pylon.Utils;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyType;

public class Python278ObjectFactory extends ObjectFactory {
	private boolean addrLoaded;
	private int intTypeAddr;
	private int tupleTypeAddr;
	private int strTypeAddr;

	public Python278ObjectFactory(MemoryInterface base, ObjectAllocator alloc) {
		super(base, alloc);
		addrLoaded = false;
	}
	
	private void loadTypeAddresses() {
		if (addrLoaded) {
			return;
		}
		
		System.out.println("loadTypeAddresses: starting");
		LinkedList<PyType> types = Utils.getAllTypes(base);
		for (PyType type : types) {
			if (type.getName().equals("int")) {
				intTypeAddr = (int) type.getAddress();
			} else if (type.getName().equals("tuple")) {
				tupleTypeAddr = (int) type.getAddress();
			} else if (type.getName().equals("str")) {
				strTypeAddr = (int) type.getAddress();
			}
		}
		
		System.out.println("loadTypeAddresses: done");
		addrLoaded = true;
	}

	@Override
	protected byte[] doConstructBool(boolean value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte[] doConstructInt(int integer) {
		// TODO Auto-generated method stub
		loadTypeAddresses();
		ByteBuffer bb = ByteBuffer.allocate(12);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(1);
		bb.putInt(intTypeAddr);
		bb.putInt(integer);
		return bb.array();
	}

	@Override
	protected byte[] doConstructFloat(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte[] doConstructString(String str) {
		// TODO Auto-generated method stub
		int len = str.length();
		byte[] bytes = null;
		
		try {
			bytes = str.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {}
		
		loadTypeAddresses();
		ByteBuffer bb = ByteBuffer.allocate(24+len);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(1);
		bb.putInt(strTypeAddr);
		bb.putInt(str.length());
		bb.putInt(-1);
		bb.putInt(0);
		bb.put(bytes);
		bb.put((byte) 0);
		return bb.array();
	}

	@Override
	protected byte[] doConstructDict(Map<PyObject, PyObject> map) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte[] doConstructList(List<PyObject> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected byte[] doConstructTuple(List<PyObject> list) {
		int size = list.size();
		
		loadTypeAddresses();
		ByteBuffer bb = ByteBuffer.allocate(16 + size*4);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.putInt(1);
		bb.putInt(tupleTypeAddr);
		bb.putInt(size);
		for (int i = 0; i < size; i++) {
			bb.putInt((int) list.get(i).getAddress());
		}
		
		return bb.array();
	}
}
