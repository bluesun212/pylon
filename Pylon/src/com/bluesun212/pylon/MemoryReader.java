package com.bluesun212.pylon;
import java.lang.reflect.Constructor;
import java.util.LinkedList;

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

/**
 * The MemoryReader is the base class for Pylon.  It attaches to the specified process,
 * and scans for user-specified data.  In more detail, it does the following:
 * 1. Opens a process with the given PID, and obtains its handle
 * 2. Makes a list of all memory regions in the process (COMMIT + any variation of READ)
 * 3. After scanFor() is called, all memory regions are scanned for any aligned ints that are in the specified array.
 * 
 * @author Jared Jonas
 */
public class MemoryReader {
	private HANDLE proc;
	private LinkedList<MemoryModel> regions;
	private Reader reader;
	
	/**
	 * Takes a string that matches the window's title and creates a MemoryReader
	 * that is attached to the underlying process the window is running from.
	 * 
	 * @param window a string matching the window's title
	 * @return a MemoryReader instance
	 */
	public static MemoryReader attach(String window) {
		IntByReference ibr = new IntByReference();
		HWND hwnd = User32.INSTANCE.FindWindow(null, window);
		User32.INSTANCE.GetWindowThreadProcessId(hwnd, ibr);
		return new MemoryReader(ibr.getValue());
	}
	
	/**
	 * Attaches a new instance of MemoryReader to the specified process
	 * 
	 * @param processId the PID
	 * @return a MemoryReader instance
	 */
	public static MemoryReader attach(int processId) {
		return new MemoryReader(processId);
	}
	
	private MemoryReader(int processId) {
		// Get min/max application addresses
		SYSTEM_INFO info = new SYSTEM_INFO();
		Kernel32.INSTANCE.GetSystemInfo(info);
		long min = Pointer.nativeValue(info.lpMinimumApplicationAddress);
		long max = Pointer.nativeValue(info.lpMaximumApplicationAddress);

		// Open child process
		proc = Kernel32.INSTANCE.OpenProcess(Kernel32.PROCESS_QUERY_INFORMATION | Kernel32.PROCESS_VM_READ, false, processId);

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
			if (mbi.state.intValue() == Kernel32.MEM_COMMIT &&
				(mbi.protect.intValue() == Kernel32.PAGE_READONLY ||
				mbi.protect.intValue() == Kernel32.PAGE_READWRITE ||
				mbi.protect.intValue() == Kernel32.PAGE_EXECUTE_READ ||
				mbi.protect.intValue() == Kernel32.PAGE_EXECUTE_READWRITE)) {
				regions.add(new MemoryModel(minAddress, mbi));
			}

			// Jump to next region
			min += regionSize;
		}
	}
	
	/**
	 * Creates a reader corresponding to the input class.
	 * 
	 * @param version the reader class
	 * @return whether the call succeeded
	 */
	public boolean createReader(Class<? extends Reader> version) {
		try {
			Constructor<? extends Reader> ctor = version.getDeclaredConstructor(HANDLE.class);
			ctor.setAccessible(true);
			
			reader = ctor.newInstance(proc);
			return true;
		} catch (Exception e) {
			reader = null;
			return false;
		}
	}
	
	/**
	 * Returns a Reader matching the specified Python version.
	 * 
	 * @return a Reader instance
	 */
	public Reader getReader() {
		if (reader == null) {
			throw new RuntimeException("MemoryReader.createReader has not been called!");
		}
		
		return reader;
	}
	
	/**
	 * Takes an array of integer values and finds all occurrences in the memory.
	 * 
	 * @param arr the input values
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
	 * @param list the input values
	 * @return a list of addresses
	 */
	public LinkedList<Long> scanFor(LinkedList<Integer> list) {
		int[] arr = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			arr[i] = list.get(i);
		}
		
		return scanFor(arr);
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
				System.out.println("Region size ("+regionSize+") != read size ("+readSize+"): "+addr);
				regionSize = readSize;
			}
			
			// Interpret data
			int[] vals = m.getIntArray(0, regionSize / 4);
			
			for (int i = 1; i < vals.length; i++) {
				for (int x = 0; x < arr.length; x++) {
					if (arr[x] == vals[i]) {
						ret.add(Pointer.nativeValue(address) + i * 4 - 4);
						break;
					}
				}
			}
		}
	}
}
