#include <stdio.h>

struct Example
{
    int memberVariable;
};

void oxyC_class_Example_this(struct Example *this)
    {
        this->memberVariable = 22;
    }

void oxyC_class_Example_tilde_this(struct Example *this)
    {
        printf("Destructor called\n");
    }

int oxyC_class_Example_getVariable(struct Example *this)
    {
        return this->memberVariable;
    }

int main(void)
{
    struct Example ex;
    
    
    oxyC_class_Example_this(&ex);
    
    printf("variable == %i\n", oxyC_class_Example_getVariable(&ex));
    return 0;
 oxyC_class_Example_tilde_this(&ex);
}

