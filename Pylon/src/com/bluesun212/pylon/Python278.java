package com.bluesun212.pylon;

import java.util.HashMap;
import java.util.LinkedList;

import com.bluesun212.pylon.types.PyOldInstance;
import com.bluesun212.pylon.types.PyType;
import com.bluesun212.pylon.types.PyTypeInstance;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

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
	private HashMap<Long, PyType> typeTable;
	
	private Python278(HANDLE proc) {
		super(proc);
		typeTable = new HashMap<Long, PyType>();
	}
	
	// Type methods
	@Override
	public PyType readType(long address) {
		// Check to see if this address is a key in the type table
		if (typeTable.containsKey(address)) {
			return typeTable.get(address);
		}
		
		// Check if this is the "type" type, and it hasn't been added to the table
		Buffer mem = getBuffer();
		long typeAddress = mem.read(address+4);
		mem.unlock();
		
		if (typeAddress == address) {
			return createType(address);
		}
		
		// Check if this is otherwise a valid type object
		PyType parent = readType(typeAddress);
		if (parent != null && parent.getName().equals("type")) {
			return createType(address);
		}
		
		// Invalid
		return null;
	}
	
	private PyType createType(long address) {
		try {
			PyType ret = new Python278Type(this, address);
			typeTable.put(address, ret);
			return ret;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
	
	@Override
	public PyType getTypeOf(long address) {
		// Read type at the typical type addresss
		Buffer mem = getBuffer();
		long typeAddress = mem.read(address+4);
		mem.unlock();
		
		return readType(typeAddress);
	}
	
	@Override
	public boolean isTypeInstance(long address) {
		// Check to see if this is a type
		PyType type = getTypeOf(address);
		if (type == null) {
			return false;
		}
		
		// Ensure the dict at the dict offset points to a real dict
		Buffer mem = getBuffer();
		int dictAddr = mem.read(address + type.getDictOffset());
		mem.unlock();
		
		return isTypeOf(dictAddr, "dict");
	}
	
	@Override
	public PyTypeInstance readTypeInstance(long address) {
		// Check to see if this is a type
		PyType type = getTypeOf(address);
		if (type == null) {
			return null;
		}
		
		// Ensure the dict at the dict offset points to a real dict
		HashMap<Integer, Integer> dict = new HashMap<Integer, Integer>();
		if (type.getDictOffset() > 0) {
			Buffer mem = getBuffer();
			int dictAddr = mem.read(address + type.getDictOffset());
			mem.unlock();
			
			if (dictAddr > 0) {
				dict = readDict(dictAddr);
				if (dict == null) {
					return null;
				}
			}
		}
		
		return new PyTypeInstance(type, dict);
	}
	
	// Python objects
	@Override
	public int readInt(long address) {
		Buffer mem = getBuffer();
		int ret = mem.read(address+8);
		mem.unlock();
		return ret;
	}
	
	@Override
	public double readFloat(long address) {
		Memory buffer = new Memory(8);
		Kernel32.INSTANCE.ReadProcessMemory(proc, Pointer.createConstant(address+8), buffer, 8, null);
		return buffer.getDouble(0);
	}
	
	@Override
	public boolean readBool(long address) {
		Buffer mem = getBuffer();
		int ret = mem.read(address+8);
		mem.unlock();
		return ret==1;
	}
	
	@Override
	public String readString(long address) {
		if (!isTypeOf(address, "str")) {
			return null;
		}
		
		// Read string length
		Buffer mem = getBuffer();
		int length = mem.read(address+8);
		mem.unlock();
		
		if (length <= 0) {
			return "";
		}
		
		// Read the string data
		Memory strData = new Memory(length);
		Kernel32.INSTANCE.ReadProcessMemory(proc, Pointer.createConstant(address+20), strData, length, null); // 24 not 20 in py3
		
		// Read and validate string
		String raw;
		try {
			raw = strData.getString(0);
		} catch (Error e) {
			return null;
		}
		
		if (raw.length() > length) {
			raw = raw.substring(0, length);
		}
		
		return raw;
	}
	
	@Override
	public LinkedList<Integer> readTuple(long address) {
		// Check type
		if (!isTypeOf(address, "tuple")) {
			return null;
		}
		
		// Read list size
		Buffer mem = getBuffer();
		int size = mem.read(address+8);
		
		// Read pointers in list
		LinkedList<Integer> list = new LinkedList<Integer>();
		for (int i = 0; i < size; i++) {
			int objAddr = mem.read(address + 12 + i * 4);
			
			// Invalid list
			if (objAddr == 0 || getTypeOf(objAddr) == null) {
				mem.unlock();
				return null;
			}
			
			list.add(objAddr);
		}
		
		mem.unlock();
		return list;
	}
	
	@Override
	public LinkedList<Integer> readList(long address) {
		// Check type
		if (!isTypeOf(address, "list")) {
			return null;
		}
		
		// Read list size
		Buffer mem = getBuffer();
		int size = mem.read(address+8);
		
		// Read list address
		int listAddr = mem.read(address+12);
		LinkedList<Integer> list = new LinkedList<Integer>();
		
		// Read elements
		for (int i = 0; i < size; i++) {
			int objAddr = mem.read(listAddr + i * 4);
			
			// Invalid list
			if (objAddr == 0 || getTypeOf(objAddr) == null) {
				mem.unlock();
				return null;
			}
			
			list.add(objAddr);
		}
		
		mem.unlock();
		return list;
	}
	
	@Override
	public HashMap<Integer, Integer> readDict(long address) {
		// Check that address points to a dict object
		if (!isTypeOf(address, "dict")) {
			return null;
		}
		
		// Read size and table ptr
		Buffer mem = getBuffer();
		int size = mem.read(address+16) * 12;
		int table = mem.read(address+20);
		mem.unlock();
		
		if (size < 0 || size > 40000) {
			return null;
		}
		
		
		// Read keys into a HashMap
		HashMap<Integer, Integer> dict = new HashMap<Integer, Integer>();
		
		if (size > 0) {
			Memory m = new Memory(size);
			Kernel32.INSTANCE.ReadProcessMemory(proc, Pointer.createConstant(table+4), m, size, null);
			
			for (int i = 0; i < size; i += 12) {
				int theKey = m.getInt(i);
				int theValue = m.getInt(i + 4);
				
				// Add to list
				if (theKey != 0 && theValue != 0) {
					dict.put(theKey, theValue);
				}
			}
		}
		
		return dict;
	}
	
	// TODO: Create class object
	@Override
	public String readOldClass(long address) {
		// Assert that this is a class
		if (!isTypeOf(address, "classobj")) {
			return null;
		}
		
		// Read class
		Buffer mem = getBuffer();
		int nameAddr = mem.read(address+16);
		mem.unlock();
		
		return readString(nameAddr);
	}
	
	@Override
	public PyOldInstance readOldInstance(long address) {
		// Read old instance
		if (!isTypeOf(address, "instance")) {
			return null;
		}
		
		Buffer mem = getBuffer();
		int classAddr = mem.read(address+8);
		int dictAddr = mem.read(address+12);
		mem.unlock();
		
		String classType = readOldClass(classAddr);
		return new PyOldInstance(classType, readDict(dictAddr));
	}
	
	private static class Python278Type extends PyType {
		private Python278Type(Reader r, long address) {
			// Read data from the struct in memory
			Buffer b = r.getBuffer();
			
			addr = address;
			name = r.readNTString(b.read(address+12));
			basicSize = b.read(address+16);
			itemSize = b.read(address+20);
			flags = b.read(address+84);
			
			if (name.length() <= 0) {
				b.unlock();
				throw new IllegalArgumentException("Not a valid type object!");
			}
			
			for (int i = 0; i < name.length(); i++) {
				char c = name.charAt(i);
				
				if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c != '.' && c != '_') {
					b.unlock();
					throw new IllegalArgumentException("Not a valid type object!");
				}
			}
			
			int docsAddr = b.read(address+92);
			if (docsAddr != 0) {
				docs = r.readNTString(docsAddr, 1024);
			}
			
			tp_call = b.read(address+64);
			tp_string = b.read(address+68);
			
			methodsAddr = b.read(address+120);
			dictAddr = b.read(address+136);
			dictOffset = b.read(address+144);
			
			b.unlock();
		}
	}
}