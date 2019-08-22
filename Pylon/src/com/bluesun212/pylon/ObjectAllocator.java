package com.bluesun212.pylon;

import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.bluesun212.pylon.MemoryAllocator.MemoryBlock;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.types.PyObject;

public class ObjectAllocator implements Runnable {
	private MemoryAllocator alloc;
	private PyInterface pint;
	private MemoryInterface base;
	
	private ReferenceQueue<PyObject> refQueue;
	private ConcurrentHashMap<PhantomReference<PyObject>, MemoryBlock> refMap;
	private ConcurrentHashMap<Long, MemoryBlock> addrMap;
	
	private boolean running = false;
	
	public ObjectAllocator(MemoryInterface base, PyInterface pint) {
		this.base = base;
		this.pint = pint;
		this.alloc = base.getMemoryAllocator();		
		
		refQueue = new ReferenceQueue<PyObject>();
		refMap = new ConcurrentHashMap<PhantomReference<PyObject>, MemoryBlock>();
		addrMap = new ConcurrentHashMap<Long, MemoryBlock>();
		start();
	}
	
	/**
	 * Creates a PyObject given the data given in the byte array.
	 * This method expects an initial reference count of 1, and will
	 * decrement the reference count once the returned object is 
	 * unaccessible in memory.  If the object's reference count 
	 * reaches 0, the MemoryBlock attached to this object will
	 * be marked as free.
	 * 
	 * @param data a byte array to be written to the Python process
	 * @return a PyObject pointing to the object created with this data
	 */
	public PyObject allocateObject(byte[] data) {
		MemoryBlock block = alloc.allocateAndWrite(data);
		PyObject ret = pint.getObject(block.getAddress());
		PhantomReference<PyObject> ref = new PhantomReference<PyObject>(ret, refQueue);
		refMap.put(ref, block);
		addrMap.put(block.getAddress(), block);
		return ret;
	}
	
	/**
	 * Increases the reference count of the parent's children, and creates
	 * a phantom reference to the parent, linked with the child.  That way,
	 * when the parent is dereferenced, the children will all also get
	 * dereferenced.
	 * 
	 * @param parent the parent object with referenced objects
	 * @param children the data structure containing the parent's children
	 */
	public void handleReferencedObjects(PyObject parent, Collection<PyObject> children) {
		for (PyObject po : children) {
			MemoryBlock mb = addrMap.get(po.getAddress());
			if (mb != null) {
				incRef(mb);
				PhantomReference<PyObject> ref = new PhantomReference<PyObject>(parent, refQueue);
				refMap.put(ref, mb);
			}
		}
	}
	
	/**
	 * Starts the garbage collector thread.
	 */
	public void start() {	
		if (!running) {
			running = true;
			Thread t = new Thread(this);
			t.setName("Pylon ObjectAllocator GC thread");
			t.setDaemon(true);
			t.start();
		}
	}
	
	/**
	 * Stops the garbage collector thread.
	 */
	public void stop() {
		running = false;
	}
	
	/**
	 * 
	 * @param mb the memory block containing a PyObject
	 * @return the reference count in the block
	 */
	public int getRefCount(MemoryBlock mb) {
		long addr = mb.getAddress();
		Buffer b = base.getBuffer();
		int refCnt = b.read(addr);
		b.unlock();
		
		return refCnt;
	}
	
	public int decRef(MemoryBlock mb) {
		System.out.println("Decref " + Long.toHexString(mb.getAddress()));
		long addr = mb.getAddress();
		Buffer b = base.getBuffer();
		
		int refCnt = b.read(addr) - 1;
		if (refCnt <= 0) {
			refCnt = 0;
		}
		
		base.getBuffer().write(addr, refCnt);
		b.unlock();
		
		return refCnt;
	}
	
	public int incRef(MemoryBlock mb) {
		System.out.println("Incref " + Long.toHexString(mb.getAddress()));
		long addr = mb.getAddress();
		Buffer b = base.getBuffer();
		
		int refCnt = b.read(addr) + 1;
		base.getBuffer().write(addr, refCnt);
		b.unlock();
		
		return refCnt;
	}

	@Override
	public void run() {
		LinkedList<Long> removedKeys = new LinkedList<Long>();
		
		while (running) {
			Reference<? extends PyObject> ref;
			while ((ref = refQueue.poll()) != null) {
				MemoryBlock mb = refMap.remove(ref);
				if (mb != null) {
					decRef(mb);
				} else {
					System.out.println("Ref in queue but not in map");
				}
			}
			
			for (Entry<Long, MemoryBlock> entry : addrMap.entrySet()) {
				if (getRefCount(entry.getValue()) <= 0) {
					System.out.println("Finalizing " + Long.toHexString(entry.getKey()));
					entry.getValue().markAsFree();
					removedKeys.push(entry.getKey());
				}
			}
			
			while (!removedKeys.isEmpty()) {
				addrMap.remove(removedKeys.pop());
			}
			
			try {
				Thread.sleep(1l);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
