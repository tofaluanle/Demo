package com.example;

public class MyClass {

    public static void main(String[] args) {
        TestInnerClass test = new TestInnerClass();
        TestInnerClass.Inner inner = test.new Inner();
        int i = inner.INNER_I;

        int j = TestInnerClass.Inner.INNER_I;
    }
}
