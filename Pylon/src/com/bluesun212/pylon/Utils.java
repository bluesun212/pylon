package com.bluesun212.pylon;

import java.util.LinkedList;

import com.bluesun212.pylon.MemoryReader;
import com.bluesun212.pylon.types.PyType;

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
			PyType type = base.getReader().readType(addr);
			if (type != null && type.getName().equals(name)) {
				return type;
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
			PyType type = base.getReader().readType(addr);
			if (type != null) {
				types.add(type);
			}
		}
		
		return types;
	}
	
	public static LinkedList<Object> getAllInstancesEx(MemoryReader base, PyType type, ObjectFilter filter) {
		// Find all occurrences of the address in memory
		LinkedList<Long> raw = base.scanFor(new int[]{(int) type.getAddress()});
		LinkedList<Object> ret = new LinkedList<Object>();
		
		for (long addr : raw) {
			if (base.getReader().getTypeOf(addr).equals(type)) {
				Object thing = base.getReader().read(addr);
				
				if (thing != null && filter.filter(thing)) {
					ret.add(thing);
				}
			}
		}
		
		return ret;
	}
	
	public static Object getFirstInstanceEx(MemoryReader base, PyType type, ObjectFilter filter) {
		// Find all occurrences of the address in memory
		LinkedList<Long> raw = base.scanFor(new int[]{(int) type.getAddress()});
		
		for (long addr : raw) {
			if (base.getReader().getTypeOf(addr).equals(type)) {
				Object thing = base.getReader().read(addr);
				
				if (thing != null && filter.filter(thing)) {
					return thing;
				}
			}
		}
		
		return null;
	}
	
	public static Object getFirstInstance(MemoryReader base, PyType type) {
		return getFirstInstanceEx(base, type, new PassAllFilter());
	}
	
	public static LinkedList<Object> getAllInstances(MemoryReader base, PyType type) {
		return getAllInstancesEx(base, type, new PassAllFilter());
	}
	
	public static interface ObjectFilter {
		public abstract boolean filter(Object obj);
	}
	
	private static class PassAllFilter implements ObjectFilter {

		@Override
		public boolean filter(Object obj) {
			return true;
		}
	}
	
	/**
	 * DON'T USE THIS!
	 * 
	 * @param base the MemoryReader instance
	 * @param obj the object in question
	 * @return a string representation of obj
	 */
	public static String serialize(MemoryReader base, Object obj) {
		throw new RuntimeException("Not implemented anymore!");
	}
}
