package com.bluesun212.pylon.types;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.bluesun212.pylon.MemoryInterface;
import com.bluesun212.pylon.ObjectFilter;

public abstract class PyDict extends PyObject implements PyReadable<Map<PyObject, PyObject>>, PyDataStructure {
	public PyDict(MemoryInterface base, long address, PyType type) {
		super(base, address, type);
	}

	protected Map<Integer, Integer> dict;
	
	public abstract PyObject getPyObject(String val);
	public abstract PyObject[] getPyObject(String[] vals);
	
	public Object getObject(String val, Object def) {
		PyObject pobj = getPyObject(val);
		
		if (pobj instanceof PyReadable) {
			return ((PyReadable<?>) pobj).get();
		}
		
		return def;
	}
	
	public LinkedList<PyObject> getMatchingValues(ObjectFilter filter) {
		LinkedList<PyObject> ret = new LinkedList<PyObject>();
		
		for (Entry<PyObject, PyObject> pair : get().entrySet()) {
			if (filter.filter(pair.getValue())) {
				ret.add(pair.getValue());
			}
		}
		
		return ret;
	}
}
