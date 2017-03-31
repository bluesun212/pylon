package com.bluesun212.pylon.python278;

import java.util.HashMap;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.Reader;
import com.bluesun212.pylon.MemoryReader.Buffer;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyType;

/* TODO LIST:
 * 	Custom Tuple, List, Dict types
 *  Add more fields in PyType
 *  Create old class and instance types
 */
/**
 * A subclass of Reader that is able to read memory from a compiled Python 2.7.8 x86 program.
 * 
 * @author Jared Jonas
 */
public class Python278 extends Reader {
	public Python278(MemoryReader mr) {
		super(mr);
	}
	
	@Override
	public PyObject createTypeInstance(long address, String expectedType) {
		if (address == 0) {
			return null;
		}
		
		// Get type string
		Buffer b = mr.getBuffer();
		int typeAddr = b.read(address+4);
		int strAddr = b.read(typeAddr+12);
		b.unlock();
		
		String typeStr = mr.readNTString(strAddr);
		if (typeStr == null || (expectedType != null && !typeStr.equals(expectedType))) {
			return null;
		}
		
		// Get class
		if (typeStr.equals("bool")) {
			return new Python278Bool();
		} else if (typeStr.equals("dict")) {
			return new Python278Dict();
		} else if (typeStr.equals("float")) {
			return new Python278Float();
		} else if (typeStr.equals("int")) {
			return new Python278Integer();
		} else if (typeStr.equals("list")) {
			return new Python278List();
		} else if (typeStr.equals("classobj")) {
			return new Python278OldClass();
		} else if (typeStr.equals("instance")) {
			return new Python278OldInstance();
		} else if (typeStr.equals("str")) {
			return new Python278String();
		} else if (typeStr.equals("tuple")) {
			return new Python278Tuple();
		} else if (typeStr.equals("type")) {
			return new Python278Type();
		} else if (typeStr.equals("object")) {
			return new Python278Object();
		}
		
		// Check if it's an instance
		PyObject typeObj = mr.getObject(typeAddr, "type");
		
		if (typeObj instanceof PyType) {
			PyType type = (PyType) typeObj;
			if (type.getDictOffset() > 0) {
				Buffer mem = mr.getBuffer();
				int dictAddr = mem.read(address + type.getDictOffset());
				mem.unlock();
				
				if (dictAddr > 0) {
					return new Python278TypeInstance(dictAddr);
				}
			}
		}
		
		return new Python278Object();
	}
	
	private HashMap<Long, PyType> typeCache = new HashMap<Long, PyType>();

	public boolean readObjectHead(long address, PyObject inst) {
		Buffer b = mr.getBuffer();
		int refCount = b.read(address);
		int typeAddr = b.read(address+4);
		b.unlock();
		
		PyObject typeObj = typeCache.get(address);
		if (typeObj == null) {
			if (typeAddr != address) {
				typeObj = mr.getObject(typeAddr, "type");
			} else {
				typeObj = inst;
			}
			
			if (typeObj instanceof PyType) {
				typeCache.put(address, (PyType) typeObj);
			} else {
				return false;
			}
		}
		
		inst.setHead(refCount, address, (PyType) typeObj);
		return true;
	}
}