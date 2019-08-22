package com.bluesun212.pylon.python278;

import java.util.HashMap;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.PyInterface;
import com.bluesun212.pylon.MemoryInterface.Buffer;
import com.bluesun212.pylon.ObjectAllocator;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyType;

/**
 * A subclass of Reader that is able to read memory from a compiled Python 2.7.8 x86 program.
 * 
 * @author Jared Jonas
 */
public class Python278 extends PyInterface {
	private static HashMap<String, TypeFactory> implementedTypes = new HashMap<String, TypeFactory>();
	
	private HashMap<Integer, PyType> typeCache;
	private int baseTypeAddr;
	
	public Python278(MemoryInterface mi) {
		super(mi);
		
		typeCache = new HashMap<Integer, PyType>();
		baseTypeAddr = 0;
	}
	
	@Override
	protected void createFactory() {
		alloc = new ObjectAllocator(base, this);
		factory = new Python278ObjectFactory(base, alloc);
	}
	
	static {
		implementedTypes.put("bool", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Bool(base, address, type);
			}
		});
		
		implementedTypes.put("dict", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Dict(base, address, type);
			}
		});
		
		implementedTypes.put("float", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Float(base, address, type);
			}
		});
		
		implementedTypes.put("int", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Integer(base, address, type);
			}
		});
		
		implementedTypes.put("list", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278List(base, address, type);
			}
		});
		
		implementedTypes.put("object", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Object(base, address, type);
			}
		});
		
		implementedTypes.put("classobj", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278OldClass(base, address, type);
			}
		});
		
		implementedTypes.put("instance", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278OldInstance(base, address, type);
			}
		});
		
		implementedTypes.put("str", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278String(base, address, type);
			}
		});
		
		implementedTypes.put("tuple", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Tuple(base, address, type);
			}
		});
		
		implementedTypes.put("type", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Type(base, address, type);
			}
		});
		
		implementedTypes.put("function", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Function(base, address, type);
			}
		});
		
		implementedTypes.put("code", new TypeFactory() {
			public PyObject construct(MemoryInterface base, long address, PyType type) {
				return new Python278Code(base, address, type);
			}
		});
	}
	
	@Override
	public PyObject getObject(long address) {
		//System.out.println(Long.toHexString(address));
		if (baseTypeAddr == 0) {
			throw new IllegalArgumentException("Base type address is NULL");
		}
		
		if (address == 0) {
			throw new IllegalArgumentException("Address is NULL");
		}
		
		// Read type address
		Buffer b = base.getBuffer();
		int refCnt = b.read(address);
		int typeAddr = b.read(address+4);
		b.unlock();
		
		if (refCnt <= 0 || typeAddr == 0) {
			throw new IllegalArgumentException("Type address is NULL or ref count is negative");
		}
		
		// Get type instance
		PyType type = typeCache.get(typeAddr);
		if (type == null) {
			if (!checkType(typeAddr, "type")) {
				throw new IllegalArgumentException("Object type's type is not base type");
			}
			
			// Add type to the type cache
			type = (PyType) getObject(typeAddr);
			typeCache.put(typeAddr, type);
		}
		
		TypeFactory tf = implementedTypes.get(type.getName());
		PyObject ret = null;
		if (tf != null) {
			ret = tf.construct(base, address, type);
		} else { // Check if this is an instance
			if (type.getDictOffset() > 0) {
				Buffer mem = base.getBuffer();
				int dictAddr = mem.read(address + type.getDictOffset());
				mem.unlock();
				
				if (dictAddr != 0 && checkType(dictAddr, "dict")) {
					ret = new Python278TypeInstance(base, address, type, dictAddr);
				}
			}
		}
		
		if (ret == null) {
			ret = new Python278Object(base, address, type);
		}
		
		return ret;
	}
	
	@Override
	public boolean validateBaseType(long addr) {
		if (addr > 0) {
			Buffer m = base.getBuffer();
			int typeAddr = m.read(addr+4);
			m.unlock();
			
			if (addr == typeAddr && checkType((int) addr, "type")) {
				baseTypeAddr = (int) addr;
				PyType type = new Python278Type(base, baseTypeAddr, null);
				typeCache.put(baseTypeAddr, type);
				return true;
			}
		}
		
		return false;
	}
	
	private boolean checkType(int objAddr, String expectedType) {
		Buffer m = base.getBuffer();
		int objRefCnt = m.read(objAddr);
		int typeAddr = m.read(objAddr+4);
		int typeRefCnt = m.read(typeAddr);
		int nameAddr = m.read(typeAddr+12);
		m.unlock();
		
		if (objAddr == 0 || typeAddr == 0 || nameAddr == 0 
				|| objRefCnt <= 0 || typeRefCnt <= 0) {
			return false;
		}
		
		return base.readNTString(nameAddr).equals(expectedType);
	}
	
	private static interface TypeFactory {
		public abstract PyObject construct(MemoryInterface base, long address, PyType type);
	}
}