# BluC
A programming language that's essentially C with classes. The intent is to be a C and C++ alternative *while also* allowing access to the under-the-hood transpiled code, so you know exactly what the language's constructs are being transpiled into, and how to interop between them and C.

Until version 1 of the language is completed (and not an alpha/beta version 1), anything is subject to change. After version 1, backwards compatability will be heavily considered.

### Notice
The current build, while compilable, is minimally functional and is only uploaded to keep track of changes.

The BluC compiler is currently undergoing a major rewrite. This compiler is only a prototype, and the actual compiler will be self-hosted.

### License change
Versions from 0.2.1-alpha and greater (unless specified as a different license) are now licensed under the Apache 2.0 license. This is to allow greater program usage than the GPL provides.

### Version
The current version is <b>0.3.0-alpha</b><br/>
This follows the Semantic Versioning guidelines.

### Functionality status
The sections in this README are each tagged with a `Status:` indicating the level of functionality, each level represents the following:

| Status            | This feature is... |
| :--               | :--         |
|Planned            | ... expected to be added but has no code behind it yet. |
|Under construction | ... in the process of being added but is not yet usable in any capacity. |
|Semi-functional    | ... partially implemented but not finished, and shouldn't yet be used for important programs |
|Functional         | ... fully implemented and functional, fully compliant to its specification |

### Setup
*Currently*, a valid installation of gcc is needed for BluC to automatically compile the C file to an executable. Otherwise, BluC can still transpile the file to C with the "-c" flag being set, but will not be able to automatically compile it to an executable.

An LLVM IR backend is planned for the future for use in most applications since LLVM has extensive (but not exhaustive) architecture support. Once said backend is implemented, the default behavior will be to compile to an executable file for the host OS.

Download the jar file and call it via the command line with your arguments. As aforementioned, the java compiler is only a prototype and the official compiler will be self-hosted.

If the program cannot find the main class then you must run it with the command "java -jar pathToBluCJar.jar argumentsHere". It appears this is the case on Windows when java but not the JDK is installed, as the JDK evidently fixes the way Windows calls java.exe/javaw.exe.

### File extension and information
*Status: functional*<br/>
The ending for BluC source files is `.bluc` -- if you want to #include a bluc file, you must use this ending.

#### Include statement
*This feature _will_ be removed as soon as imports are functional*<br/>
*Status: functional*<br/>

There are no required headers for BluC source files except when linking a library. The #include preprocessor directive is temporary until the import system is functional. The import system will be able to handle C source files. 

Files ending in `.bluc` are processed as a BluC source file and are included only once -- even if there is no include guard. As in C, paths are relative to the current file's parent directory.

To include an BluC standard library file, use `#include <libraryFileName.bluc>`

To include your own source file, use `#include "fileNameHere.bluc"`

### Class syntax
*Status: semi-functional*<br/>
By default, class fields are private and functions are public.

#### Example class
*Status: functional*<br/>
```cpp
#include <stdio.h>

class Example
{
    int memberVariable;
    
    void setVariable(int newVal)
    {
        this->memberVariable = newVal;
    }
    
    int getVariable()
    {
        return this->memberVariable;
    }
}

int main(void)
{
    Example example;
    example.setVariable(22);
    
    printf("variable == %i\n", example.getVariable());
    return 0;
}
```
Output:
```
variable == 22
```
#### Explicit "this"
*Status: functional*<br/>
To access member variables or methods from within a class, an explicit reference to "this" is required. The requirement of an explicit reference to "this" is being reviewed (in a language design perspective).

#### Semicolons
*Status: functional*<br/>
Semicolons after a class are not required. Unlike structs, classes can't name instances of that class after the end brace of the class.

#### Constructor syntax
*Status: functional*<br/>
Constructors are denoted by their lack of return type and "function name" of "this".

```cpp
#include <stdio.h>

class Example
{
    int memberVariable;
    
    //constructor here
    this()
    {
        this->memberVariable = 22;
    }
    
    int getVariable()
    {
        return this->memberVariable;
    }
}

int main(void)
{
    Example example();
    
    printf("variable == %i\n", example.getVariable());
    return 0;
}

```
Output:
```
variable == 22
```

*Status: functional*<br/>
If a constructor (even a parameter-less constructor) is not explicitly called then the class member variables will store whatever was previously in memory at that location. This is to allow optional class members without significant overhead.

```cpp
#include <stdio.h>

class Example
{
    int memberVariable;
    
    //constructor here
    this()
    {
        this->memberVariable = 22;
    }
    
    int getVariable()
    {
        return this->memberVariable;
    }
}

int main(void)
{
    Example example;
    
    printf("variable == %i\n", example.getVariable());
    return 0;
}

```
Output:
```
Who knows?
```

*Status: functional*<br/>
Constructors can be called "late" (i.e. not at initialization) and are known as late constructors.

```cpp
#include <stdio.h>

class Example
{
    int memberVariable;
    
    //constructor here
    this()
    {
        this->memberVariable = 22;
    }
    
    int getVariable()
    {
        return this->memberVariable;
    }
}

int main(void)
{
    Example example;
    
    //late constructor call here
    example();
    
    printf("variable == %i\n", example.getVariable());
    return 0;
}

```
Output:
```
variable == 22
```

#### Destructor syntax
*Status: semi-functional*

Destructors are denoted by their lack of return type and function name of "~this". The tilde differentiates the destructor from any potential constructors. There can only be one parameter-less destructor per class.

```cpp
#include <stdio.h>

class Example
{
    int memberVariable;
    
    this()
    {
        this->memberVariable = 22;
    }
    
    //destructor here
    ~this()
    {
        printf("Destructor called\n");
    }
    
    int getVariable()
    {
        return this->memberVariable;
    }
}

int main(void)
{
    Example example;
    
    //late constructor call here
    example();
    
    printf("variable == %i\n", example.getVariable());
    return 0;
}
```
Output:
```
variable == 22
Destructor called
```

As of currently, there is no guarenteed order that destructors are called in, the only requirement is that they are called at the end of the current scope, or immediately before each return statement in the current scope.
