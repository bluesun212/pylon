package com.bluesun212.pylon.types;

import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.bluesun212.pylon.ObjectFilter;

public abstract class PyDict extends PyObject implements PyReadable<Map<PyObject, PyObject>> {
	protected Map<Integer, Integer> dict;
	
	public int size() {
		return dict.size();
	}
	
	public PyObject getPyObject(String val) {
		Map<PyObject, PyObject> map = get();
		for (Entry<PyObject, PyObject> pair : map.entrySet()) {
			if (pair.getKey().toString().equals(val)) {
				return pair.getValue();
			}
		}
		
		return null;
	}
	
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
