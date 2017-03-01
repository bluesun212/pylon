package com.bluesun212.pylon.types;

import java.util.HashMap;

/**
 * 
 * @author Jared Jonas
 */
public class PyOldInstance {
	private String type;
	private HashMap<Integer, Integer> dict;
	
	public PyOldInstance(String type, HashMap<Integer, Integer> dict) {
		this.type = type;
		this.dict = dict;
	}
	
	public String getType() {
		return type;
	}
	
	public HashMap<Integer, Integer> getDict() {
		return dict;
	}
	
	
	public String toString() {
		return "Instance of " + type;
	}
}
