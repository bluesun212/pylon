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
public abstract class PyInterface {
	protected MemoryInterface base;
	protected ObjectAllocator alloc;
	protected ObjectFactory factory;
	
	public PyInterface(MemoryInterface base) {
		this.base = base;
		createFactory();
	}
	
	public abstract PyObject getObject(long address);
	public abstract boolean validateBaseType(long addr);
	protected abstract void createFactory();

	public ObjectFactory getFactory() {
		return factory;
	}
}