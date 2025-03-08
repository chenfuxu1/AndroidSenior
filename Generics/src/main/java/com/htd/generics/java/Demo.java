package com.htd.generics.java;

import androidx.annotation.NonNull;

import com.htd.generics.Fridge;
import com.htd.generics.Shop;
import com.htd.generics.Shop2;
import com.htd.generics.Tv;
import com.htd.generics.fruit.Apple;
import com.htd.generics.fruit.Banana;
import com.htd.generics.fruit.Fruit;
import com.htd.utils.Sout;

import java.util.ArrayList;
import java.util.Collections;
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

    // 泛型方法
    public static void test4() {
        // 这只能回收一种类型 Tv，现在 appleShop 想回收任意类型
        Shop<Apple, Tv> appleShop = new Shop<Apple, Tv>() {
            @Override
            public Apple buy() {
                return null;
            }

            @Override
            public void refund(Apple item) {

            }

            @NonNull
            @Override
            public List<Apple> recycle(Tv item) {
                return Collections.emptyList();
            }
        };
        List<Apple> tcl = appleShop.recycle(new Tv("TCL"));
        // 这只能回收一种类型 Fridge
        Shop<Apple, Fridge> appleShop2 = new Shop<Apple, Fridge>() {
            @Override
            public Apple buy() {
                return null;
            }

            @Override
            public void refund(Apple item) {

            }

            @NonNull
            @Override
            public List<Apple> recycle(Fridge item) {
                return Collections.emptyList();
            }
        };
        List<Apple> haiEr = appleShop2.recycle(new Fridge("haier"));
    }

    // 泛型方法
    public static void test5() {
        Shop2<Apple> appleShop2 = new Shop2<Apple>() {
            @Override
            public Apple buy() {
                return null;
            }

            @Override
            public void refund(Apple item) {

            }

            @Override
            public <E> E tradeIn(E item, float money) {
                return null;
            }
        };

        // 可以旧换新任意类型
        appleShop2.tradeIn(new Apple("苹果"), 100);
        // appleShop2.<Apple>tradeIn(new Apple("苹果"), 100); // 完整写法，因为有类型推断，可省略
        appleShop2.tradeIn(new Fridge("海尔"), 100);

        // 类型推断，泛型⽅法的实例化
        // 因为泛型⽅法也可以把类型参数的类型进⾏确定。
        // 具体呢 ？每⼀次泛型⽅法的调⽤就是⼀次对这个泛型⽅法的实例化。
        // 例如上⾯的例⼦，就是把 E 的类型确定为了 Tv
    }
}
