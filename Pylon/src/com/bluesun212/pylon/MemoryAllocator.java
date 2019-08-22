package com.bluesun212.pylon;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.BaseTSD.SIZE_T;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public class MemoryAllocator {
	private static final int MIN_PART_SIZE = 4096;
	
	private MemoryInterface base;
	private ConcurrentSkipListSet<Partition> parts;
	private int align;
	
	/**
	 * Creates a MemoryAllocator attached to the specified process
	 * with an align size of 4.
	 * 
	 * @param base the memory interface
	 */
	public MemoryAllocator(MemoryInterface base) {
		this(base, 4);
	}
	
	/**
	 * Creates a MemoryAllocator attached to the specified process
	 * with the given align size, which aligns MemoryBlocks to only
	 * be on multiples of this value.
	 * 
	 * @param base the memory interface
	 * @param align the align size
	 */
	public MemoryAllocator(MemoryInterface base, int align) {
		this.base = base;
		this.align = align;
		parts = new ConcurrentSkipListSet<Partition>(new Comparator<Partition>() {
			public int compare(Partition o1, Partition o2) {
				return (int) (o1.getBaseAddress() - o2.getBaseAddress());
			}
		});
	}
	
	/**
	 * Allocates a portion of data in the other process.  It can then be written to
	 * and read from.  
	 * 
	 * @param size the block size to be allocated
	 * @return a MemoryBlock object representing this block in the other process' memory 
	 */
	public MemoryBlock allocate(int size) {
		// Make sure size is a multiple of ALIGN
		if (size <= 0) {
			return null;
		}
		
		int rem = size % align;
		if (rem != 0) {
			size += align - rem;
		}
		
		// Find a partition that can allocate the given size
		Iterator<Partition> iter = parts.iterator();
		while (iter.hasNext()) {
			MemoryBlock ret = iter.next().tryAllocate(size);
			if (ret != null) {
				return ret;
			}
		}
		
		// All partitions are full, so let's create a new partition
		Partition newPart = new Partition(base.getProcessHandle(), 
				Math.max(MIN_PART_SIZE, size));
		MemoryBlock ret = newPart.tryAllocate(size);
		parts.add(newPart);
		
		return ret;
	}
	
	public MemoryBlock allocateAndWrite(byte[] data) {
		if (data == null) {
			return null;
		}
		
		MemoryBlock mb = allocate(data.length);
		if (mb != null) {
			mb.write(data);
		}
		
		return mb;
	}
	
	/**
	 * The MemoryBlock 
	 * 
	 * @author Jared Jonas
	 */
	public class MemoryBlock {
		private int offset;
		private int size;
		private boolean free;
		private Object lock;
		
		private Partition part;
		private MemoryBlock previous;
		private MemoryBlock next;
		
		private Memory memory;
		private Pointer ptr;

		public MemoryBlock(int offset, int size, Partition part, MemoryBlock previous, MemoryBlock next) {
			this.offset = offset;
			this.size = size;
			this.part = part;
			this.previous = previous;
			this.next = next;
			this.free = true;
			this.part.incRef();
			this.memory = (Memory) part.memory.share(offset, size);
			this.ptr = part.ptr.share(offset);
			this.lock = new Object();
		}
		
		public Object getLock() {
			return lock;
		}
		
		public long getAddress() {
			return part.getBaseAddress() + offset;
		}
		
		public int getSize() {
			return size;
		}
		
		public boolean isFree() {
			return free;
		}
		
		public Memory getMemory() {
			return memory;
		}
		
		// Mutators
		public MemoryBlock split(int newSize) {
			synchronized (lock) {
				int rem = size - newSize;
				size = newSize;
				
				// No need to synchronize since it isnt in the list yet
				MemoryBlock newBlock = new MemoryBlock(offset + size, rem, part, this, next);
				memory = (Memory) part.memory.share(offset, size);
				next = newBlock;
				return newBlock;
			}
		}
		
		public void markAsUsed() {
			synchronized (lock) {
				free = false;
			}
		}
		
		public void markAsFree() {
			synchronized (lock) {
				free = true;
				
				// Test clear
				//write(new byte[size]);
				
				mergeNext();
				mergePrevious(); // Order is important
				
				// This is the only block left and its free,
				// so decref the partition
				if (next == null && previous == null) {
					part.decRef();
				}
			}
		}
		
		public boolean write(byte[] data) {
			synchronized (lock) {
				if (data.length > size) {
					return false;
				}
				
				memory.write(0, data, 0, data.length);
				Kernel32.INSTANCE.WriteProcessMemory(part.process, ptr, memory, data.length, null);
			}
			
			return true;
		}
		
		// Private methods
		private void mergePrevious() {
			if (previous != null) {
				synchronized (previous.lock) {
					if (previous.isFree()) {
						previous.size += size;
						previous.memory = (Memory) part.memory.share(offset, size);
						previous.next = next;
						if (next != null) {
							synchronized (next.lock) {
								next.previous = previous;
							}
						}
						
						part.decRef();
					}
				}
			}
		}
		
		private void mergeNext() {
			if (next != null) {
				synchronized (next.lock) {
					if (next.isFree()) {
						size += next.size;
						memory = (Memory) part.memory.share(offset, size);
						next = next.next;
						if (next != null) {
							next.previous = this;
						}
						
						part.decRef();
					}
				}
			}
		}
	}
	
	private class Partition {
		private HANDLE process;
		private Pointer ptr;
		private PartitionMemory memory;
		private AtomicInteger references;
		private MemoryBlock head;
		
		private Partition(HANDLE process, int size) {
			this.process = process; 
			
			ptr = Kernel32Ex.INSTANCE.VirtualAllocEx(process, Pointer.NULL, 
					new SIZE_T(size), 0x1000|0x2000, Kernel32.PAGE_EXECUTE_READWRITE);
			memory = new PartitionMemory(size);
			
			references = new AtomicInteger();
			head = new MemoryBlock(0, size, this, null, null);
			
			System.out.println("Created partition " + ptr);
		}
		
		private void incRef() {
			references.incrementAndGet();
		}
		
		private void decRef() {
			int refs = references.decrementAndGet();
			
			if (refs == 0 && parts.size() > 1) {
				parts.remove(this);
				memory.forceDispose();
				// TODO: Dealloc memory in other process now
			}
		}
		
		public long getBaseAddress() {
			return Pointer.nativeValue(ptr);
		}
		
		private MemoryBlock tryAllocate(int size) {
			MemoryBlock current = head;
			while (current != null) {
				synchronized (current.getLock()) {
					if (current.isFree()) {
						if (size <= current.getSize()) {
							if (size < current.getSize()) {
								current.split(size);
							}
							
							current.markAsUsed();
							return current;
						}
					}
					
					current = current.next;
				}
			}
			
			return null;
		}
		
		private class PartitionMemory extends Memory {
			public PartitionMemory(int size) {
				super(size);
			}

			public void forceDispose() {
				dispose();
			}
		}
	}
	
	private static interface Kernel32Ex extends StdCallLibrary {
		public static final Kernel32Ex INSTANCE = (Kernel32Ex)Native.loadLibrary("kernel32", Kernel32Ex.class, W32APIOptions.UNICODE_OPTIONS);
		Pointer VirtualAllocEx(HANDLE hProcess, Pointer lpAddress, SIZE_T dwSize, int flAllocationType, int flProtect); 
	}
}
