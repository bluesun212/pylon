package com.bluesun212.pylon.types;

import java.util.HashMap;

public class PyTypeInstance {
	private PyType type;
	private HashMap<Integer, Integer> dict;
	
	public PyTypeInstance(PyType type, HashMap<Integer, Integer> dict) {
		this.type = type;
		this.dict = dict;
	}
	
	public PyType getType() {
		return type;
	}
	
	public HashMap<Integer, Integer> getDict() {
		return dict;
	}
	
	public String toString() {
		return "Instance of " + type.getName();
	}
}
