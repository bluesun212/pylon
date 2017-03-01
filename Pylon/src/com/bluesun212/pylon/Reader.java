package com.bluesun212.pylon;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bluesun212.pylon.types.PyOldInstance;
import com.bluesun212.pylon.types.PyType;
import com.bluesun212.pylon.types.PyTypeInstance;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.HANDLE;

/**
 * This class transforms addresses of Python objects in the other process's memory into Java objects representing the data/pointers in those objects.
 * Keep in mind that trying to read a primitive type at the wrong address will result in undefined behavior. 
 * Additionally, calling a read operation on an invalid non-primitive type will result in null being returned, or an Exception.
 * 
 * @author Jared Jonas
 *
 */
public abstract class Reader {
	protected HANDLE proc;
	
	private Buffer[] buffers;
	private int current;
	private Object lock;

	protected Reader(HANDLE proc) {
		this.proc = proc;
		
		buffers = new Buffer[8];
		for (int i = 0; i < 8; i++) {
			buffers[i] = new Buffer();
		}
		
		lock = new Object();
		current = 0;
	}

	// Python objects
	/**
	 * Takes an address in memory and creates an object corresponding to the type of the said object.
	 * 
	 * @param address address pointing to the object
	 * @return the representation of the object
	 */
	public Object read(long address) {
		PyType type = getTypeOf(address);
		if (type == null) {
			return "null";
		}
		
		String typeName = type.getName();
		
		if (typeName.equals("NoneType")) {
			return "NONE";
		} else if (typeName.equals("int")) {
			return readInt(address);
		} else if (typeName.equals("float")) {
			return readFloat(address);
		} else if (typeName.equals("bool")) {
			return readBool(address);
		} else if (typeName.equals("str")) {
			return readString(address);
		} else if (typeName.equals("tuple")) {
			return readTuple(address);
		} else if (typeName.equals("list")) {
			return readList(address);
		} else if (typeName.equals("dict")) {
			return readDict(address);
		} else if (typeName.equals("instance")) {
			return readOldInstance(address);
		} else if (isTypeInstance(address)) {
			return (readTypeInstance(address));
		} else {
			return getTypeOf(address).getName() + "@" + Long.toHexString(address);
		}
	}
	
	// Type methods
	/**
	 * Reads a PyType at the specified address
	 * 
	 * @param address address pointing to the object
	 * @return the type object
	 */
	public abstract PyType readType(long address);
	
	/**
	 * Returns the type of an object at the specified address
	 * 
	 * @param address address pointing to the object
	 * @return the type object
	 */
	public abstract PyType getTypeOf(long address);
	
	/**
	 * A convenience method that determines whether the object at the specified
	 * address is an instance of the type with the specified name
	 * 
	 * @param address address pointing to the object
	 * @return if this object's type's name is the given name
	 */
	public boolean isTypeOf(long address, String name) {
		// Check to see if this type matches the given type name
		PyType type = getTypeOf(address);
		return type != null && type.getName().equals(name);
	}
	
	/**
	 * Determines whether the object in memory is an instance of a type
	 * 
	 * @param address address pointing to the object
	 * @return whether this object is an instance of a type
	 */
	public abstract boolean isTypeInstance(long address);
	
	/**
	 * Creates a PyTypeInstance corresponding to this address
	 * 
	 * @param address address pointing to the object
	 * @return the PyTypeInstance representating the object
	 */
	public abstract PyTypeInstance readTypeInstance(long address);
	
	/**
	 * Reads an integer at the specified memory address
	 * 
	 * @param address address pointing to the integer
	 * @return an integer with the value of the object
	 */
	public abstract int readInt(long address);
	
	/**
	 * Reads a float at the specified memory address
	 * 
	 * @param address address pointing to the float
	 * @return a float with the value at the address
	 */
	public abstract double readFloat(long address);
	
	/**
	 * Reads a boolean value
	 * 
	 * @param address address pointing to the object
	 * @return a boolean
	 */
	public abstract boolean readBool(long address);
	
	/**
	 * Reads a string at the specified address
	 * 
	 * @param address address pointing to the object
	 * @return a string read at the address
	 */
	public abstract String readString(long address);
	
	/**
	 * Reads a tuple (an immutable list) at the address
	 * 
	 * @param address address pointing to the object
	 * @return a list containing the pointers held in the tuple
	 */
	public abstract LinkedList<Integer> readTuple(long address);
	
	/**
	 * Reads a python list/array
	 * 
	 * @param address address pointing to the object
	 * @return a list containing the pointers held in the python list
	 */
	public abstract LinkedList<Integer> readList(long address);
	
	/**
	 * Reads a python dictionary object
	 * 
	 * @param address address pointing to the object
	 * @return a hashmap containing all the name/value address pairs in the dictionary
	 */
	public abstract HashMap<Integer, Integer> readDict(long address);
	
	// FIXME create a PyClassObj class
	/**
	 * Reads an old-style Python class in memory
	 * 
	 * @param address address pointing to the object
	 * @return the representation of the class object
	 */
	public abstract String readOldClass(long address);
	
	/**
	 * Reads a old-style class instance
	 * 
	 * @param address address pointing to the object
	 * @return the representation of the object
	 */
	public abstract PyOldInstance readOldInstance(long address);
	
	/**
	 * Reads a null-terminated string at the specified memory address.
	 * 
	 * @param address the address of the string
	 * @param maxLength its max length
	 * @return a string the address pointed to
	 */
	public String readNTString(long address, int maxLength) {
		try {
			Memory buffer = new Memory(maxLength);
			Kernel32.INSTANCE.ReadProcessMemory(proc, Pointer.createConstant(address), buffer, maxLength, null);
			String name = buffer.getString(0);
			
			if (name.length() > maxLength) {
				name = name.substring(0, maxLength);
			}
			
			return name;
		} catch (Error e) {
			return "";
		}
	}
	
	/**
	 * Reads a null-terminated string at the specified memory address, with a max length of 64 bytes.
	 * 
	 * @param address the address of the string
	 * @return a string the address pointed to
	 */
	public String readNTString(long address) {
		return readNTString(address, 64);
	}
	
	protected Buffer getBuffer() {
		synchronized (lock) {
			// Find an unused buffer
			int old = current;
			
			do {
				current = (current+1) % buffers.length;
			} while (buffers[current].lock.get() && current != old);
			
			// If all are used, create a new one
			Buffer b = buffers[current];
			if (current == old) {
				System.err.println("New buffer created");
				new Throwable().printStackTrace(System.err);
				b = new Buffer();
			}
			
			// Lock and return
			b.lock.set(true);
			return b;
		}
	}
	
	protected class Buffer {
		private Memory back;
		private AtomicBoolean lock;
		
		public Buffer() {
			back = new Memory(4);
			lock = new AtomicBoolean();
		}
		
		public int read(long address) {
			Kernel32.INSTANCE.ReadProcessMemory(proc, Pointer.createConstant(address), back, 4, null);
			return back.getInt(0);
		}
		
		public void unlock() {
			lock.set(false);
		}
	}
}