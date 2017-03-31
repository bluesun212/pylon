package com.bluesun212.pylon.types;

public abstract class PyOldInstance extends PyObject {
	protected PyOldClass classobj;
	protected PyDict dict;
	
	public PyOldClass getClassObject() {
		return classobj;
	}
	
	public PyDict getDict() {
		return dict;
	}
	
	@Override
	public String toString() {
		return "Old instance of " + classobj.getName();
	}
}
