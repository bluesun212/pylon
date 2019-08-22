package com.bluesun212.pylon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bluesun212.pylon.types.PyBool;
import com.bluesun212.pylon.types.PyDict;
import com.bluesun212.pylon.types.PyFloat;
import com.bluesun212.pylon.types.PyInteger;
import com.bluesun212.pylon.types.PyList;
import com.bluesun212.pylon.types.PyObject;
import com.bluesun212.pylon.types.PyString;
import com.bluesun212.pylon.types.PyTuple;

public abstract class ObjectFactory {
	protected ObjectAllocator alloc;
	protected MemoryInterface base;
	
	public ObjectFactory(MemoryInterface base, ObjectAllocator alloc) {
		this.base = base;
		this.alloc = alloc;
	}
	
	public PyBool constructBool(boolean value) {
		throw new IllegalStateException("Not implemented");
	}
	
	public PyInteger constructInt(int integer) {
		byte[] data = doConstructInt(integer);
		return (PyInteger) alloc.allocateObject(data);
	}
	
	public PyFloat constructFloat(double value) {
		byte[] data = doConstructFloat(value);
		return (PyFloat) alloc.allocateObject(data);
	}
	
	public PyString constructString(String str) {
		byte[] data = doConstructString(str);
		return (PyString) alloc.allocateObject(data);
	}
	
	public PyDict constructDict(Map<PyObject, PyObject> map) {
		byte[] data = doConstructDict(map);
		PyDict obj = (PyDict) alloc.allocateObject(data);
		alloc.handleReferencedObjects(obj, map.keySet());
		alloc.handleReferencedObjects(obj, map.values());
		return obj;
	}
	
	public PyList constructList(List<PyObject> list) {
		byte[] data = doConstructList(list);
		PyList obj = (PyList) alloc.allocateObject(data);
		alloc.handleReferencedObjects(obj, list);
		return obj;
	}
	
	public PyTuple constructTuple(List<PyObject> list) {
		byte[] data = doConstructTuple(list);
		PyTuple obj = (PyTuple) alloc.allocateObject(data);
		alloc.handleReferencedObjects(obj, list);
		return obj;
	}
	
	// Convenience methods
	public PyObject constructObject(Object obj) {
		if (obj instanceof Boolean) {
			return constructBool((boolean) obj);
		} else if (obj instanceof Float) {
			return constructFloat((double) obj);
		} else if (obj instanceof Integer) {
			return constructInt((int) obj);
		} else if (obj instanceof String) {
			return constructString((String) obj);
		}
		
		throw new IllegalArgumentException("Unsupported object being constructed");
	}
	
	public PyList constructListGeneric(List<?> objs) {
		ArrayList<PyObject> constrObjs = new ArrayList<PyObject>();
		
		for (Object obj : objs) {
			constrObjs.add(constructObject(obj));
		}
		
		return constructList(constrObjs);
	}
	
	public PyTuple constructTupleGeneric(List<?> objs) {
		ArrayList<PyObject> constrObjs = new ArrayList<PyObject>();
		
		for (Object obj : objs) {
			constrObjs.add(constructObject(obj));
		}
		
		return constructTuple(constrObjs);
	}
	
	public PyTuple constructTupleGeneric(Object[] objs) {
		return constructTupleGeneric(Arrays.asList(objs));
	}
	
	public PyDict constructDictGeneric(Map<?, ?> map) {
		HashMap<PyObject, PyObject> constrObjs = new HashMap<PyObject, PyObject>();
		
		for (Entry<?, ?> obj : map.entrySet()) {
			constrObjs.put(constructObject(obj.getKey()), constructObject(obj.getValue()));
		}
		
		return constructDict(constrObjs);
	}
	
	// Abstract methods
	protected abstract byte[] doConstructBool(boolean value);
	protected abstract byte[] doConstructInt(int integer);
	protected abstract byte[] doConstructFloat(double value);
	protected abstract byte[] doConstructString(String str);
	
	protected abstract byte[] doConstructDict(Map<PyObject, PyObject> map);
	protected abstract byte[] doConstructList(List<PyObject> list);
	protected abstract byte[] doConstructTuple(List<PyObject> list);
	
}
