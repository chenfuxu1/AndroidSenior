package com.htd.retrofit;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.cfx.myapplication.R;
import com.htd.retrofit.utils.Logit;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        requestNetwork();
    }

    private void requestNetwork() {
        GithubService githubService = GithubRetrofit.INSTANCE.getGithub();
        Call<List<Repo>> repos = githubService.listRepos("chenfuxu1");
        repos.enqueue(new Callback<List<Repo>>() {
            @Override
            public void onResponse(Call<List<Repo>> call, Response<List<Repo>> response) {
                Logit.INSTANCE.d(TAG, "htd Thread.currentThread().getName() " + Thread.currentThread().getName());
                Logit.INSTANCE.d(TAG, "htd onResponse response: " + response.body());
            }

            @Override
            public void onFailure(Call<List<Repo>> call, Throwable throwable) {
                Logit.INSTANCE.d(TAG, "htd throwable: " + throwable);
            }
        });

        // rxjava
        Observable<List<Repo>> octocat = githubService.listReposRx("octocat");
        octocat.subscribe();
    }
}

/*
1.入参为 GithubService 的 class
2.返回的结果是 GithubService 的代理对象
public <T> T create(final Class<T> service) {
    // 1.主要验证传进来的 service 是不是一个接口，如果不是接口就直接报错了
    validateServiceInterface(service);
    // 2.动态代理
    // 代理：会创建一个类，生成一个对象，这个类实现了 service 的接口，这个类就会代理这些接口的实现，这个对象就是实际的代理，它会代理那些方法
    // 动态代理：这个类是运行时生成的
    return (T)
        Proxy.newProxyInstance(
            service.getClassLoader(), // 参数 1：类加载器，不用关注
            new Class<?>[] {service}, // 参数 2：service 的 class，提供 service 的接口
            new InvocationHandler() {
              private final Object[] emptyArgs = new Object[0];

              @Override
              public @Nullable Object invoke(Object proxy, Method method, @Nullable Object[] args)
                  throws Throwable {
                // If the method is a method from Object then defer to normal invocation.
                // 代理声明只代理接口里面的方法，如果是 Object  的方法就直接调用，不进行代理
                if (method.getDeclaringClass() == Object.class) {
                  return method.invoke(this, args);
                }
                args = args != null ? args : emptyArgs;
                Reflection reflection = Platform.reflection;
                // 如果是 java8 的默认方法，也直接调用，不去进行代理
                return reflection.isDefaultMethod(method)
                    ? reflection.invokeDefaultMethod(method, service, proxy, args)
                    // 3.核心方法，调用 loadServiceMethod
                    : loadServiceMethod(service, method).invoke(proxy, args);
              }
            });
 }

// 主要验证传进来的 service 是不是一个接口
private void validateServiceInterface(Class<?> service) {
    // 1.如果不是接口，直接抛出异常
    if (!service.isInterface()) {
      throw new IllegalArgumentException("API declarations must be interfaces.");
    }

    Deque<Class<?>> check = new ArrayDeque<>(1);
    // 2.如果是一个接口，会将接口添加到队列中
    check.add(service);
    while (!check.isEmpty()) {
      // 3.取出刚才添加的接口
      Class<?> candidate = check.removeFirst();
      // 4.判断接口有没有泛型参数，如果有泛型参数，直接报错
      if (candidate.getTypeParameters().length != 0) {
        StringBuilder message =
            new StringBuilder("Type parameters are unsupported on ").append(candidate.getName());
        if (candidate != service) {
          message.append(" which is an interface of ").append(service.getName());
        }
        throw new IllegalArgumentException(message.toString());
      }
      // 5.如果 service 有父接口，重复循环，目的将该接口的所有接口都遍历判断一遍是否有泛型参数
      Collections.addAll(check, candidate.getInterfaces());
    }

    // 6.validateEagerly 相当于 debug 和 release 的区别，方便调试，快速验证问题，提前暴露开发者写的方法有无问题
    if (validateEagerly) {
      Reflection reflection = Platform.reflection;
      // 7.遍历 service 的所有方法
      for (Method method : service.getDeclaredMethods()) {
        // 默认方法、静态方法、合成类或合成字段会进行过滤
        if (!reflection.isDefaultMethod(method)
            && !Modifier.isStatic(method.getModifiers())
            && !method.isSynthetic()) {
          // 8.调用 loadServiceMethod 去进行方法加载，如果有问题，那就会提前暴露
          loadServiceMethod(service, method);
        }
      }
    }
  }

// 加载 service 接口的方法, 就是个带缓存的加载，先去 map 里面取，如果有就直接返回，如果没有就去加载，加载完放进 map 中
// 所以，这里的主要方法就是 ServiceMethod.parseAnnotations(this, service, method) 去获取方法
ServiceMethod<?> loadServiceMethod(Class<?> service, Method method) {
  while (true) {
    // Note: Once we are minSdk 24 this whole method can be replaced by computeIfAbsent.
    Object lookup = serviceMethodCache.get(method);

    if (lookup instanceof ServiceMethod<?>) {
      // Happy path: method is already parsed into the model.
      return (ServiceMethod<?>) lookup;
    }

    if (lookup == null) {
      // Map does not contain any value. Try to put in a lock for this method. We MUST synchronize
      // on the lock before it is visible to others via the map to signal we are doing the work.
      Object lock = new Object();
      synchronized (lock) {
        // 如果 Map 中不存在该键（key），则将键值对添加到 Map 中，并返回 null；如果 Map 中已经存在该键，则不进行任何操作，并返回旧值‌
        lookup = serviceMethodCache.putIfAbsent(method, lock);
        if (lookup == null) {
          // On successful lock insertion, perform the work and update the map before releasing.
          // Other threads may be waiting on lock now and will expect the parsed model.
          ServiceMethod<Object> result;
          try {
            result = ServiceMethod.parseAnnotations(this, service, method);
          } catch (Throwable e) {
            // Remove the lock on failure. Any other locked threads will retry as a result.
            serviceMethodCache.remove(method);
            throw e;
          }
          serviceMethodCache.put(method, result);
          return result;
        }
      }
    }

    // Either the initial lookup or the attempt to put our lock in the map has returned someone
    // else's lock. This means they are doing the parsing, and will update the map before
    // releasing
    // the lock. Once we can take the lock, the map is guaranteed to contain the model or null.
    // Note: There's a chance that our effort to put a lock into the map has actually returned a
    // finished model instead of a lock. In that case this code will perform a pointless lock and
    // redundant lookup in the map of the same instance. This is rare, and ultimately harmless.
    synchronized (lookup) {
      Object result = serviceMethodCache.get(method);
      if (result == null) {
        // The other thread failed its parsing. We will retry (and probably also fail).
        continue;
      }
      return (ServiceMethod<?>) result;
    }
  }
}
 */