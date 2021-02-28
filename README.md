# BluC
A programming language that's essentially C with classes. The intent is to be a C and C++ alternative *while also* allowing access to the under-the-hood transpiled code, so you know exactly what the language's constructs are being transpiled into, and how to interop between them and C. Until version 1 of the language is completed, anything is subject to change. After version 1, backwards compatability will be heavily considered.

### Notice
The current build, while compilable, is minimally functional and is only uploaded to keep track of changes.

The BluC compiler is currently undergoing a major rewrite. This compiler is only a prototype, and the actual compiler will be self-hosted.

### Introduction of Semantic Versioning
I feel the codebase is sufficiently large and designed in such a way that Semantic Versioning between each github push would be highly beneficial.

This project will now follow the rules of Semantic Versioning.

### Version
The current version is <b>0.1.0-alpha</b><br/>
This follows the Semantic Versioning guidelines.
### Setup
A valid installation of gcc is needed for BluC to automatically compile the C file to an exe. Otherwise, BluC can still transpile the file to C with the "-c" flag being set, but will not be able to automatically compile it to an exe.

Download the jar file and call it via the command line with your arguments. 

If the program cannot find the main class then you must run it with the command "java -jar pathToBluCJar.jar argumentsHere"

### File extension and information
*Status: functional*<br/>
The ending for BluC source files is `.bluc` -- if you want to #include a bluc file, you must use this ending.

#### Include statement
*This feature _will_ be removed as soon as imports are functional*<br/>
*Status: functional*<br/>

There are not headers for BluC source files, just the source files themselves. The #include preprocessor directive is temporary until the import system is functional. The import system will be able to handle C source files. 

Files ending in `.bluc` are processed as a BluC source file and are included only once -- even if there is no include guard. As in c, paths are relative to the current file's parent directory.

To include an BluC standard library file, use `include <libraryFileName.bluc>`

To include your own source file, use `include "fileNameHere.bluc"`

### Class syntax
*Status: functional*<br/>
By default, class fields are private and functions are public.

#### Example class
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
*Status: under construction*

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
