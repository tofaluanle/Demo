package com.example.innerclass;

/**
 * 测试内部类生成class文件的命名规则
 */
public class MyClass {

    public static void main(String[] args) {
        new Test.ITest(){};//MyClass$1.class
        new Test.ITest(){};//MyClass$2.class

        class Inner9{};//MyClass$1Inner9.class
    }

    public void m() {
        new Test.ITest(){};//MyClass$3.class
        class Inner7{};//MyClass$1Inner7.class
    }

    class Inner4{
        Thread t = new Thread(){};//MyClass$Inner4$1.class
        public void k() {
            class Inner37{};//MyClass$Inner4$1Inner37.class
        }
    };//MyClass$Inner4.class

    static class StaticInner6{}//MyClass$StaticInner6.class
}
