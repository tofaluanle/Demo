package com.example;

/**
 * 用来研究内部类的demo类
 *
 * @auther 宋疆疆
 * @date 2015/10/23.
 */
public class TestInnerClass {

    public static void main(String[] args) {
        TestInnerClass test = new TestInnerClass();
        TestInnerClass.Inner inner = test.new Inner();
        int i = inner.INNER_I;
        int j = TestInnerClass.Inner.INNER_I;
    }

    public interface Task {
        void onTask();
    }

    static {
        System.out.println("Test static");
    }

    private int i;
    private static int static_i;

    public Task k() {
        //匿名内部类
        new TestInnerClass() {
            @Override
            public Task k() {
                return null;
            }
        };

        //局部内部类
        class MethodInner implements Task {

            @Override
            public void onTask() {

            }
        }

        return new MethodInner();
    }

    public static void static_k() {

    }


    /**
     * 使用TestInnerClass的实例test.new Inner这样实例化一个普通的内部类
     */
    public class Inner {

        //但是可以直接定义一个静态变量，为什么？
        public static final int INNER_I = 1;
        //不能用静态变量和方法
//        static int j;
//        static void k();
//        static {}
        public void m() {
            i = 10;
            static_i = 20;
            k();
            static_k();
        }

    }

    /**
     * 使用new TestInnerClass.Inner这样实例化一个静态内部类
     */
    public static class StaticInner {

        static int j;

        static {
            System.out.println("StaticInner static");
        }

        public void m() {
//            i = 20;
            static_i = 10;
//            k();
            static_k();
        }
    }
}
