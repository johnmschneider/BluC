#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <stdint.h>
#include <stdbool.h>


char* hello = "hello world";
char* hello2 = "hello from indirect include";




static size_t SIZEOF_VOID_PTR = sizeof(void*);
char *a = "i am a test str";
char *b = "inside quote \"test\"";


 
 
 
 


 

struct Vector
{

    int sizeOfArray;
    int lastElemIndex;
    void** array;

    

    
    

    
    
    


    


    


    

}
;



void oxyC_class_Vector_ctor(struct Vector *this)
    {
        int initialAllocationSize = 100 * SIZEOF_VOID_PTR;
        this->sizeOfArray = initialAllocationSize;
        this->lastElemIndex = -1;
        this->array = malloc(initialAllocationSize);
    }
void oxyC_class_Vector_dtor(struct Vector *this)
    {
        free(this->array);
    }
int oxyC_class_Vector_size(struct Vector *this)
    {
        return this->lastElemIndex + 1;
    }
int oxyC_class_Vector_add(struct Vector *this, void* data)
    {
        int endIndex = this->sizeOfArray/SIZEOF_VOID_PTR;
        
        if (this->lastElemIndex == endIndex)
        {
            
            size_t newArraySize = (size_t)(2 * oxyC_class_Vector_size(this) * SIZEOF_VOID_PTR);
            void** newArray = (void**) malloc(newArraySize);
            this->sizeOfArray = newArraySize;

            for (int i = 0; i < endIndex + 1; i++)
            {
                newArray[i] = this->array[i];
            }
            
            free(this->array);
            this->array = newArray;
            oxyC_class_Vector_add(this, data);
        }
        else
        {
            this->lastElemIndex ++;
            this->array[this->lastElemIndex] = data;
        }
    }
void oxyC_class_Vector_remove(struct Vector *this, void* data)
    {
        for (int i = 0; i < oxyC_class_Vector_size(this); i++)
        {
            if (this->array[i] == data)
            {
                
                
                for (int i2 = i + 1; i2 < oxyC_class_Vector_size(this); i2++)
                {
                    this->array[i2 - 1] = this->array[i2];
                }

                this->lastElemIndex --;
                return;
            }
        }

        fprintf(stderr, "[Vector.remove]: element not found in vector\n");
    }
void* oxyC_class_Vector_get(struct Vector *this, int index)
    {
        return this->array[index];
    }
struct Throwable
{

    void* _data;
    struct String* _errMsg;
    
    

    
    

    
    

}
;



void oxyC_class_Throwable_this(struct Throwable *this, void* data, struct String* errMsg)
    {
        this->_data = data;
        this->_errMsg = errMsg;
    }
void* oxyC_class_Throwable_data(struct Throwable *this)
    {
        return this->_data;
    }
struct String* oxyC_class_Throwable_error(struct Throwable *this)
    {
        return this->_errMsg;
    }
struct String
{

    char* _cString;
    int _len;
    struct Throwable _t;
    struct String* _s;
    
    

    
    

    
    

    
    

    
    

    
    

    
    

}
;



void oxyC_class_String_this(struct String *this, char* value)
    {
        this->_len = 0;
        this->_s = malloc(sizeof(struct String));
        
        char c = value[0];
        for (int i = 0; c != '\0'; i++)
        {
            this->_len ++;
            c = value[i];
        }
        
        
        this->_cString = (char*) malloc(sizeof(char) * (oxyC_class_String_length(this) + 1));
        strcpy(this->_cString, value);
    }
void oxyC_class_String_tilde_this(struct String *this)
    {
        
    }
char* oxyC_class_String_cString(struct String *this)
    {
        return this->_cString;
    }
struct Throwable oxyC_class_String_charAt(struct String *this, int index)
    {
        if (index > oxyC_class_String_length(this))
        {
            fprintf(stderr, "[String.charAt]: index %i greater than length\n",
                index);
            
            
            char* returnee = this->_cString + (oxyC_class_String_length(this) + 1);
            
            oxyC_class_String_this(this->_s, "IndexOutOfBounds");
            oxyC_class_Throwable_this(&this->_t, returnee, this->_s);
        }
        else
        {
            char* returnee = this->_cString + index;
            
            oxyC_class_String_this(this->_s, "NoError");
            oxyC_class_Throwable_this(&this->_t, returnee, this->_s);
        }
        
        return this->_t;
    }
int oxyC_class_String_length(struct String *this)
    {
        return this->_len;
    }
void oxyC_class_String_append(struct String *this, char* appendThis)
    {
        int appendeeLen = 0;

        char c = appendThis[0];
        for (int i = 0; c != '\0'; i++)
        {
            appendeeLen ++;
            c = appendThis[i];
        }

        
        int newLen = oxyC_class_String_length(this) + 1 + appendeeLen + 1;

        
        this->_len = newLen - 1;
        char* oldVal = this->_cString;
        this->_cString = malloc(sizeof(char) * newLen);
        strcpy(this->_cString, oldVal);
        strcat(this->_cString, appendThis);
    }
bool oxyC_class_String_equals(struct String *this, char* rawStr)
    {
        bool isEqual = true;
        
        int len = oxyC_class_String_length(this);
        
        for (int i = 1; i < len; i++)
        {
            if (rawStr[i] == '\0')
            {
                isEqual = false;
            }
            else if (i == len - 1 && rawStr[i + 1] != '\0')
            {
                isEqual = false;
            }
            else if (this->_cString[i] != rawStr[i])
            {
            	isEqual = false;
            }
        }

        return isEqual;
    }

struct ReplacementNode
{

    char* context;
    char* nameBeforeMangle;
    char* nameAfterMangle;
    
    

    
}
;



void oxyC_class_ReplacementNode_ctor(struct ReplacementNode *this, char* cont, char* nameBefore, char* nameAfter)
    {
        this->context = cont;
        this->nameBeforeMangle = nameBefore;
        this->nameAfterMangle = nameAfter;
    }
struct EOFClass
{

    
}
;


int main(void)
{
    struct Vector v;
    oxyC_class_Vector_ctor(&v);
    
    char test = 'a';
    oxyC_class_Vector_add(&v, &test);
    printf("char == %c\n", (*((char*) oxyC_class_Vector_get(&v, 0))));
    printf("(that == %d)\n", (int)(*((char*) oxyC_class_Vector_get(&v, 0))));
    printf("we wanted %d\n", (int)('a'));
    oxyC_class_Vector_dtor(&v);

    struct ReplacementNode rn;
    oxyC_class_ReplacementNode_ctor(&rn, "global", "test1", "testA");
    
    
    if (1 == 1)
    {
        oxyC_class_ReplacementNode_ctor(&rn, "global", "test2", "testB");
    }

    struct String test2; oxyC_class_String_this(&test2, "hello");
    oxyC_class_String_this(&test2, "non-initializer constructor test");
            
    oxyC_class_String_append(&test2, " world!");
    printf("test.val == %s\n", oxyC_class_String_cString(&test2));
    
    struct String *test3 = malloc(sizeof(struct String));
    oxyC_class_String_this(test3, "hi");
    
    printf("%s\n", oxyC_class_String_cString(test3));
    struct Throwable result = oxyC_class_String_charAt(&test2, 6);
    struct String error = *(oxyC_class_Throwable_error(&result));
    if (oxyC_class_String_equals(&error, "NoError"))
    {
        printf("test.charAt(6) == \"%c\"", *(char*)(oxyC_class_Throwable_data(&result)));
    }
}
