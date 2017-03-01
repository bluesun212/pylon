# Pylon
A Java library for cross-process Python introspection

### Basic description
Pylon is a Java library that uses Sun's JNA libraries to read Python data from running Python processes on Windows.  The principle idea is similar to other common memory introspection technologies.  First, Pylon scans all memory regions in a specified process for certain key bytes, and records their address.  After, Pylon is able to scan for all instances of a type, and able to read data at user-specified addresses.  

### Motivation
The most common form of obtaining Python data from a running process is to attach a thread to the process, grab the Python DLL's handle, obtain the GIL (Global Interpreter Lock), and run arbitrary Python code that can use reflection to get specified data.  However, there exist certain Python executable packagers that have their own custom-built Python runtime DLL with any convenience methods (Like `PyRun_*` or `PyEval_*`) removed.  Although it is possible to find the handles of other useful methods (eg. `PyObject_call`), it is impossible to run these functions without pointers to pre-existing Python objects.  Without Pylon, it would be extremely difficult to find the pointers to the Python objects one needs.  

### Usage
###### 1. Attaching to the process
It is imperitive to know what version of Python that the process in question is using.  It is not difficult to find this through viewing the process memory in a conventional memory viewer, like Cheat Engine.  One also needs a PID or window title to find the process.  Then, one would use the following code to attach to the process:
```
MemoryReader mr = MemoryReader.attach([PID/WINDOW TITLE]);
mr.createReader(Python278.class); // The only version supported so far
```

###### 2. Finding the desired object
The next step would be to run a memory scan to locate a certain instance of a type, or something.  The functions in `Utils` would be the most useful in doing so.

###### 3. Reading the data in said object
Obtaining the data from a specified object is not so bad.  Eg, if it is a primitive type:
```
Object data = mr.getReader().read(address);
```

If it is an instance of a non-primitive type, then one could do something like so:
```
PyTypeInstance pti = mr.getReader().readInstance(address);
```
Then, one could look through the object's dictionary to find the right key/value pair.  
**NOTE:** I'm working on a convenience method to do this easier, and it should be done relatively soon.

###### 4. Viewing object data using the `ObjectViewer`
Coming soon!
