package com.bluesun212.pylon;

import com.bluesun212.pylon.types.PyObject;

/**
 * This class transforms addresses of Python objects in the other process's memory into Java objects representing the data/pointers in those objects.
 * Keep in mind that trying to read a primitive type at the wrong address will result in undefined behavior. 
 * Additionally, calling a read operation on an invalid non-primitive type will result in null being returned, or an Exception.
 * 
 * @author Jared Jonas
 *
 */
public abstract class Reader {
	protected MemoryReader mr;
	
	public Reader(MemoryReader mr) {
		this.mr = mr;
	}
	
	public abstract PyObject createTypeInstance(long address, String expectedType);
	public abstract boolean readObjectHead(long address, PyObject inst);
}