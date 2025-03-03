package com.htd.generics.java;

import com.htd.generics.fruit.Apple;
import com.htd.generics.fruit.Banana;
import com.htd.generics.fruit.Fruit;
import com.htd.utils.Sout;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: Android Senior
 * Create By: Chen.F.X
 * DateTime: 2025-03-02 23:40
 * <p>
 * Desc:
 *
 * kotlin 中的 out T 等同于 Java 的 ? extends T
 * kotlin 中的 in T 等同于 Java 的 ? super T
 */
public class Demo {
    private static final String TAG = "Demo";

    public static void test1() {
        Fruit[] fruits = new Apple[5];
        // fruits[0] = new Banana("香蕉");
        // Sout.INSTANCE.d(TAG, "cfx " + fruits[0]);
    }

    // 类型擦除
    public static void test2() {
        // 等价于 ArrayList fruits = new ArrayList();
        ArrayList<Fruit> fruits = (ArrayList) new ArrayList<Apple>();
        fruits.add(new Banana("香蕉"));
        Sout.INSTANCE.d(TAG, "cfx " + fruits.get(0));
    }

    // ? super
    // 类型下界
    public static void test3() {
        List<? super Apple> apples = new ArrayList<Fruit>();
        apples.add(new Apple("香蕉"));
        apples.get(0);

        Apple apple = new Apple("苹果");
        List<Fruit> fruit = new ArrayList<>();
        fruit.add(apple);

        apple.addMeToList(fruit);
    }
}
