#ifndef MACRO_TESTS_1_OXH
    #define MACRO_TESTS_1_OXH
    #define mac1(x, y) x##y
    #define mac2(a, b) mac1(k, v)
    #define mac3(n, m) m##n
#endif