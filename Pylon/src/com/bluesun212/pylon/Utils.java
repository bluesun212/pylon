package com.bluesun212.pylon;

import java.util.LinkedList;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyOldInstance;
import com.bluesun212.pylon.types.PyType;
import com.bluesun212.pylon.types.PyTypeInstance;

/**
 * This class contains a bunch of useful methods to find Python instances and types.
 * 
 * @author Jared Jonas
 */
public class Utils {
	/**
	 * Finds a type by the given name in the program, and returns an object representing that type.
	 * 
	 * @param base the MemoryReader instance
	 * @param typeAddress the address of the "type" type
	 * @param name the type being looked for
	 * @return the PyType object
	 */
	public static PyType getTypeByName(MemoryReader base, int typeAddress, String name) {
		// Find all types, then find the one with the given name
		LinkedList<Long> raw = base.scanFor(new int[]{typeAddress});
		
		for (long addr : raw) {
			PyObject typeObj = base.getObject(addr);
			if (typeObj instanceof PyType) {
				PyType type = (PyType) typeObj;
				if (type.getName().equals(name)) {
					return type;
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Creates a list containing every defined type in the specified program.
	 * 
	 * @param base the MemoryReader instance
	 * @param typeAddress the address of the "type" type
	 * @return a list of addresses 
	 */
	public static LinkedList<PyType> getAllTypes(MemoryReader base, int typeAddress) {
		// Find all types
		LinkedList<Long> raw = base.scanFor(new int[]{typeAddress});
		LinkedList<PyType> types = new LinkedList<PyType>();
		
		for (long addr : raw) {
			PyObject typeObj = base.getObject(addr);
			if (typeObj instanceof PyType) {
				types.add((PyType)typeObj);
			}
		}
		
		return types;
	}
	
	public static LinkedList<PyObject> getAllInstancesEx(MemoryReader base, PyType type, ObjectFilter filter) {
		// Find all occurrences of the address in memory
		LinkedList<Long> raw = base.scanFor(new int[]{(int) type.getAddress()});
		LinkedList<PyObject> ret = new LinkedList<PyObject>();
		
		for (long addr : raw) {
			PyObject typeObj = base.getObject(addr);
			
			if (typeObj != null && typeObj.getType().equals(type)) {
				if (filter.filter(typeObj)) {
					ret.add(typeObj);
				}
			}
		}
		
		return ret;
	}
	
	public static PyObject getFirstInstanceEx(MemoryReader base, PyType type, ObjectFilter filter) {
		// Find all occurrences of the address in memory
		LinkedList<Long> raw = base.scanFor(new int[]{(int) type.getAddress()});
		
		for (long addr : raw) {
			PyObject typeObj = base.getObject(addr);
			if (typeObj != null && typeObj.getType().equals(type)) {
				if (filter.filter(typeObj)) {
					return typeObj;
				}
			}
		}
		
		return null;
	}
	
	public static PyObject getFirstInstance(MemoryReader base, PyType type) {
		return getFirstInstanceEx(base, type, new PassAllFilter());
	}
	
	public static LinkedList<PyObject> getAllInstances(MemoryReader base, PyType type) {
		return getAllInstancesEx(base, type, new PassAllFilter());
	}
	
	public static PyObject getNestedObject(PyObject baseObj, String name) {
		PyObject currObj = baseObj;
		String[] names = name.split("\\.");
		
		for (int i = 0; i < names.length; i++) {
			if (currObj == null) {
				return null;
			}
			
			PyDict dict = getDict(currObj);
			currObj = dict.getPyObject(names[i]);
		}
		
		return currObj;
	}
	
	public static boolean instanceOf(PyObject obj, String typeName) {
		if (obj.getType().getName().equals(typeName)) {
			return true;
		} else if (obj instanceof PyOldInstance && ((PyOldInstance) obj).getClassObject().getName().equals(typeName)) {
			return true;
		}
		
		return false;
	}
	
	public static PyDict getDict(PyObject obj) {
		if (obj instanceof PyDict) {
			return (PyDict) obj;
		} else if (obj instanceof PyOldInstance) {
			return ((PyOldInstance) obj).getDict();
		} else if (obj instanceof PyTypeInstance) {
			return ((PyTypeInstance) obj).getDict();
		}
		
		return null;
	}
	
	private static class PassAllFilter implements ObjectFilter {
		@Override
		public boolean filter(PyObject obj) {
			return true;
		}
	}
}
