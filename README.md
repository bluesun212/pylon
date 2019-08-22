# Pylon
A Java library for cross-process Python introspection

### Basic description
Pylon is a Java library that uses Sun's JNA libraries to read Python data from running Python processes on Windows.  The principle idea is similar to other common memory introspection programs.  Pylon will scan all memory regions in a process for user-specified type addresses, creating a list of instances of said type.  Pylon will also read objects at a specified address and will create an instance of a Java class that represents the Python instance in that moment.  Using these two features, users will be able to read any arbitrary Python data stored in the process' memory.

### Motivation
The most common form of obtaining Python data from a running process is to attach a thread to the process, grab grab the python.dll handle, obtain the GIL (Global Interpreter Lock), and run arbitrary Python code to fetch arbitrary data.  However, there are certain Python executable packagers that have their own custom-built Python runtime DLL with any convenience methods (Like `PyRun_*` or `PyEval_*`) removed.  Although it is possible to find the handles of other useful methods (eg. `PyObject_call`), it is impossible to run these functions without pointers to pre-existing Python objects.  Without Pylon, it would be extremely difficult to find the pointers to the Python objects the user needs.  

### Usage
###### 1. Attaching to the process
It is imperitive to know what version of Python that the process in question is using.  It is not difficult to find this through viewing the process memory in a conventional memory viewer, like Cheat Engine.  One also needs a PID or window title to find the process.  Then, one would use the following code to attach to the process:
```
MemoryReader mr = MemoryReader.attach([PID/WINDOW TITLE]);
mr.createReader(Python278.class); // The only version supported so far
```

###### 2. Finding the desired object
The next step would be to run a memory scan to locate a certain instance of a type that contains the data the user is looking for.  The functions in `Utils` would be the most useful in doing so.

###### 3. Reading the data in said object
Obtaining the data from a specified object is quite easy.  The user can use ```MemoryReader.getObject(address)``` to instantiate a Java object representing the Python object at the address specified. Then, the user can cast the object to the required container class (e.g. ```PyInteger``` or ```PyTypeInstance```) and use the class methods to extract the required data.

The ```Utils``` class is full of convenience methods to get data from type instances and data types.

###### 4. Viewing object data using the `ObjectViewer`
To view all the data in a data structure or a type object, one can create a new ```ObjectViewer``` using a ```MemoryReader``` and a ```PyObject``` as arguments.  This will spawn a new window where the user can browse all the data contained in the passed object and any data from objects that it references.

## Version Changes
### New in version 2
###### CHANGES
`PyObject`:
* Got rid of `setHead` and `read`, and used a constructor instead
	* This is because most types are immutable, so read would only be called once anyways.  
* Changed `getReferenceCount` to `readReferenceCount`, since they are dynamic
* All subclasses of `PyObject` were updated to reflect this

`PyDict`: Added an abstract function to get multiple objects 

`PyOldClass`: Added `getBaseObjects()` and `toString()`

`PYType`: Added `membersAddr`,` tp_hash`, `tp_getattro`, `tp_setattro`, `mro` fields and more getters

`Py278Dict`: 
* `size()`: Fixed an error that returned the wrong number
* `update()`: size uses the `mask+1` instead of the `size()` func
* Reading from the table has been improved slightly

`Python278OldClass`: The dict is now read in, and the initialization is more robust

`Python278String`: Converts byte array to a string using ISO-8859-1 encoding now

`Python278Type`: Fixed some read offsets that were incorrect

`ObjectTree`: Moved some functionality into ObjectNode

`Utils`: Methods that required a base type address argument now use `MemoryInterface.getBaseTypeAddress()`

`Reader/PyInterface`: 
* Renamed to `PyInterface`
* `readObjectHead` and `createTypeInstance` have been removed in favor of a new system, which is simply `getObject()`.  

`Python278`: More robust type checking and validation has been implemented

`MemoryReader/MemoryInterface`:
* Renamed to `MemoryInterface`
* `getBuffer()` has been simplified
* `getObject(long, String)` has been removed
* `createReader()` was renamed to `setVersion()`

###### ADDITIONS
`MemoryInterface`: 
* Added `ExtendedBuffer`, and changed `getExtendedBuffer()` to return this
	*This way these buffers can be disposed of when done with
* Added `Buffer.write()`, `getMemoryAllocator()`, `getFactory()`
`getProcessID()`, `getProcessHandle()`
* Added `get/setBaseTypeAddress()`, since this address was important to `Utils`

`ObjectTree`: Added a context menu and began work on an auto update feature

`ObjectFilter`: Added `PassAllFilter`

`PyInterface`: 
* `createFactory()`, `getFactory()`, and `validateBaseType()` have been added
* An `ObjectAllocator` field has been added

`Python278`: `implementedTypes` and `TypeFactory` now controls the instantiation of objects

Added `PyCode`, representing a code object

Added `PyFunction`, representing a function object

Added `MemoryAllocator`, a class that manages blocks of memory in the attached process

Added `ObjectAllocator`, a class that manages garbage collection of python objects
	residing in the `MemoryBlock`s allocated by `MemoryAllocator`
	
Added `ObjectFactory`, which will construct specific types in the process' memory
	and return a `PyObject` pointing to it
