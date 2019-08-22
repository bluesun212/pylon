package com.bluesun212.pylon;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.bluesun212.pylon.types.PyObject;
import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.WinBase.SYSTEM_INFO;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.MEMORY_BASIC_INFORMATION;
import com.sun.jna.ptr.IntByReference;

/* TODO:
 * Create a thread that monitors the memory regions in the process and updates the list
 * Multi-thread scanning
 * Remove reflection from getObject, speed it up
 * Make type cache visible to MemoryReader
 * 
 * Rename this class
 * Create memory management system for written types
 */

/**
 * The MemoryInterface is the base class for Pylon. It attaches to the specified
 * process, and scans for user-specified data. In more detail, it does the
 * following: 1. Opens a process with the given PID, and obtains its handle 2.
 * Makes a list of all memory regions in the process (COMMIT + any variation of
 * READ) 3. After scanFor() is called, all memory regions are scanned for any
 * aligned ints that are in the specified array.
 * 
 * @author Jared Jonas
 */
public class MemoryInterface {
	private HANDLE proc;
	private int procID;
	private LinkedList<MemoryModel> regions;
	private PyInterface pyInterface;
	private MemoryAllocator alloc;
	private long baseTypeAddr = 0;
	private boolean baseTypeAddrValidated = false;

	private static int bufferSize = 32;
	private Buffer[] buffers;

	/**
	 * Takes a string that matches the window's title and creates a MemoryReader
	 * that is attached to the underlying process the window is running from.
	 * 
	 * @param window
	 *            a string matching the window's title
	 * @return a MemoryReader instance
	 */
	public static MemoryInterface attach(String window) {
		IntByReference ibr = new IntByReference();
		HWND hwnd = User32.INSTANCE.FindWindow(null, window);
		User32.INSTANCE.GetWindowThreadProcessId(hwnd, ibr);
		return new MemoryInterface(ibr.getValue());
	}

	/**
	 * Attaches a new instance of MemoryReader to the specified process
	 * 
	 * @param processId
	 *            the PID
	 * @return a MemoryReader instance
	 */
	public static MemoryInterface attach(int processId) {
		return new MemoryInterface(processId);
	}

	private MemoryInterface(int processId) {
		// Get min/max application addresses
		SYSTEM_INFO info = new SYSTEM_INFO();
		Kernel32.INSTANCE.GetSystemInfo(info);
		long min = Pointer.nativeValue(info.lpMinimumApplicationAddress);
		long max = Pointer.nativeValue(info.lpMaximumApplicationAddress);

		// Open child process
		procID = processId;
		
		int rights = Kernel32.PROCESS_VM_OPERATION|Kernel32.PROCESS_VM_WRITE|Kernel32.PROCESS_VM_READ|Kernel32.PROCESS_QUERY_INFORMATION;
		proc = Kernel32.INSTANCE.OpenProcess(rights, false, processId);

		// Start going through memory pages
		regions = new LinkedList<MemoryModel>();

		while (min < max) {
			// Read memory query
			Pointer minAddress = Pointer.createConstant(min);
			MEMORY_BASIC_INFORMATION mbi = new MEMORY_BASIC_INFORMATION();
			SIZE_T size = new SIZE_T(48);

			Kernel32.INSTANCE.VirtualQueryEx(proc, minAddress, mbi, size);
			long regionSize = mbi.regionSize.longValue();

			// If region matches the specified parameters
			if (mbi.state.intValue() == Kernel32.MEM_COMMIT && (mbi.protect.intValue() == Kernel32.PAGE_READONLY
					|| mbi.protect.intValue() == Kernel32.PAGE_READWRITE
					|| mbi.protect.intValue() == Kernel32.PAGE_EXECUTE_READ
					|| mbi.protect.intValue() == Kernel32.PAGE_EXECUTE_READWRITE)) {
				regions.add(new MemoryModel(minAddress, mbi));
			}

			// Jump to next region
			min += regionSize;
		}

		// Create buffer stuff
		buffers = new Buffer[bufferSize];
		for (int i = 0; i < bufferSize; i++) {
			buffers[i] = new Buffer();
		}
		
		alloc = new MemoryAllocator(this);
	}
	
	public int getProcessID() {
		return procID;
	}
	
	public HANDLE getProcessHandle() {
		return proc;
	}

	/**
	 * Creates a reader corresponding to the input class.
	 * 
	 * @param version
	 *            the reader class
	 * @return whether the call succeeded
	 */
	public boolean setVersion(Class<? extends PyInterface> version) {
		try {
			Constructor<? extends PyInterface> ctor = version.getDeclaredConstructor(MemoryInterface.class);
			ctor.setAccessible(true);
			pyInterface = ctor.newInstance(this);
			
			// Just in case setBaseTypeAddress was called before this function
			setBaseTypeAddress(baseTypeAddr);
			return true;
		} catch (Exception e) { // TODO: Bad Jared
			e.printStackTrace();
			pyInterface = null;
			return false;
		}
	}
	
	/**
	 * Sets and validates the base type address.  This is the address
	 * in process memory where the base type is located.  The base type
	 * is the type of type objects.
	 * 
	 * @param baseTypeAddr the address pointing to the base type
	 * @return whether the validation succeeded
	 */
	public boolean setBaseTypeAddress(long baseTypeAddr) {
		this.baseTypeAddr = baseTypeAddr;
		baseTypeAddrValidated = false;
		
		if (pyInterface != null && pyInterface.validateBaseType(baseTypeAddr)) {
			baseTypeAddrValidated = true;
		}
		
		return baseTypeAddrValidated;
	}
	
	/**
	 * 
	 * 
	 * @return the base type address, or 0 if not validated
	 */
	public long getBaseTypeAddress() {
		return baseTypeAddrValidated ? baseTypeAddr : 0;
	}

	/**
	 * Takes an array of integer values and finds all occurrences in the memory.
	 * 
	 * @param arr
	 *            the input values
	 * @return a list of addresses
	 */
	public LinkedList<Long> scanFor(int[] arr) {
		LinkedList<Long> ret = new LinkedList<Long>();

		for (MemoryModel region : regions) {
			region.find(arr, ret);
		}

		return ret;
	}

	/**
	 * Takes a list of integer values and finds all occurrences in the memory.
	 * 
	 * @param list
	 *            the input values
	 * @return a list of addresses
	 */
	public LinkedList<Long> scanFor(LinkedList<Integer> list) {
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}

		return scanFor(arr);
	}
	
	public PyObject getObject(long address) {
		if (!baseTypeAddrValidated) {
			throw new IllegalStateException("The base type address has not been validated yet");
		}
		
		try {
			return pyInterface.getObject(address);
		} catch (IllegalArgumentException e) {}
		
		return null;
	}
	
	public ObjectFactory getFactory() {
		return pyInterface.getFactory();
	}
	
	public MemoryAllocator getMemoryAllocator() {
		return alloc;
	}

	public Buffer getBuffer() {
		for (int i = 0; i < buffers.length; i++) {
			if (!buffers[i].lock.get()) {
				buffers[i].lock.set(true);
				return buffers[i];
			}
		}
		
		// Don't bother locking the buffer, since it's not used in the array
		System.err.println("Creating new buffer in getBuffer()");
		return new Buffer();
	}

	public ExtendedBuffer getExtendedBuffer(long address, int size) {
		ExtendedBuffer buffer = new ExtendedBuffer(size);
		Kernel32.INSTANCE.ReadProcessMemory(proc, Pointer.createConstant(address), buffer, size, null);
		return buffer;
	}

	/**
	 * Reads a null-terminated string at the specified memory address.
	 * 
	 * @param address
	 *            the address of the string
	 * @param maxLength
	 *            its max length
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
	 * Reads a null-terminated string at the specified memory address, with a
	 * max length of 64 bytes.
	 * 
	 * @param address
	 *            the address of the string
	 * @return a string the address pointed to
	 */
	public String readNTString(long address) {
		return readNTString(address, 64);
	}

	public class Buffer {
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
		
		public void write(long address, int data) {
			back.setInt(0, data);
			Kernel32.INSTANCE.WriteProcessMemory(proc, Pointer.createConstant(address), back, 4, null);
		}

		public void unlock() {
			lock.set(false);
		}
	}
	
	public class ExtendedBuffer extends Memory {
		public ExtendedBuffer(int size) {
			super(size);
		}
		
		public void unlock() {
			dispose();
		}
	}

	private class MemoryModel {
		private Pointer address;
		private int regionSize;

		public MemoryModel(Pointer address, MEMORY_BASIC_INFORMATION mbi) {
			this.address = address;

			regionSize = mbi.regionSize.intValue();
		}

		public void find(int[] arr, LinkedList<Long> ret) {
			// Read memory
			if (regionSize == 0) {
				return;
			}

			Memory m = new Memory(regionSize);
			IntByReference ibr = new IntByReference();
			Kernel32.INSTANCE.ReadProcessMemory(proc, address, m, regionSize, ibr);
			int readSize = ibr.getValue();

			if (readSize < regionSize) {
				String addr = Long.toHexString(Pointer.nativeValue(address));
				System.out.println("Region size (" + regionSize + ") != read size (" + readSize + "): " + addr);
				regionSize = readSize;
			}

			// Interpret data
			int[] vals = m.getIntArray(0, regionSize / 4);

			for (int i = 1; i < vals.length; i++) {
				for (int x = 0; x < arr.length; x++) {
					if (arr[x] == vals[i]) {
						ret.add(Pointer.nativeValue(address) + i * 4 - 4); // TODO: Decide on whether this should be -4
						break;
					}
				}
			}
		}
	}
}
