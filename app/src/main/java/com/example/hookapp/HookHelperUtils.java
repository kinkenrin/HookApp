package com.example.hookapp;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by jinxianlun
 * on 2019-07-18
 */
public class HookHelperUtils {

    public static final String TUREINTENT = "tureintent";

    public static void hookAmn() throws ClassNotFoundException {
        Object gDefault = Reflex.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");
        Object mInstance = Reflex.getFieldObject("android.util.Singleton", gDefault, "mInstance");

        Class<?> classInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(classInterface.getClassLoader(),
                new Class<?>[]{classInterface}, new AMNInvocationHanlder(mInstance));
        Reflex.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);
    }

    public static void hookAmn2() throws ClassNotFoundException {
        Object gDefault = Reflex.getStaticFieldObject("android.app.ActivityManagerNative", "gDefault");
        Object mInstance = Reflex.getFieldObject("android.util.Singleton", gDefault, "mInstance");

        Class<?> classInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(classInterface.getClassLoader(),
                new Class<?>[]{classInterface}, new AMNInvocationHanlder1(mInstance));
        Reflex.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);
    }

    public static void hookActivityThread() {
        //获取当前的ActivityThread对象
        Object currentActivityThread = Reflex.getStaticFieldObject("android.app.ActivityThread", "sCurrentActivityThread");
        //获取对象的mH对象
        Handler mH = (Handler) Reflex.getFieldObject(currentActivityThread, "mH");
        //将mH替换为我们的自己自定义的MyCallback
        Reflex.setFieldObject(Handler.class, mH, "mCallback", new MyCallback(mH));
    }

    private static class MyCallback implements Handler.Callback {
        Handler mBase;

        public MyCallback(Handler mH) {
            mBase = mH;
        }

        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    handleLaunchActivity(msg);
                    break;
                default:
                    break;

            }
            mBase.handleMessage(msg);
            return true;
        }

        private void handleLaunchActivity(Message msg) {
            Object obj = msg.obj;
            Intent intent = (Intent) Reflex.getFieldObject(obj, "intent");
            Intent targetIntent = intent.getParcelableExtra(HookHelperUtils.TUREINTENT);
            intent.setComponent(targetIntent.getComponent());
        }
    }

    /*public static void hookAmnAndroidP() throws ClassNotFoundException {
        Object gDefault = Reflex.getStaticFieldObject("android.app.ActivityManager", "IActivityManagerSingleton");
        Object mInstance = Reflex.getFieldObject("android.util.Singleton", gDefault, "mInstance");

        Class<?> classInterface = Class.forName("android.app.IActivityManager");
        Object proxy = Proxy.newProxyInstance(classInterface.getClassLoader(),
                new Class<?>[]{classInterface}, new AMNInvocationHanlder(mInstance));

        Reflex.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);
    }

    public static void hookAmnAndroidP2() throws ClassNotFoundException {
        Object gDefault = Reflex.getStaticFieldObject("android.app.ActivityManager", "IActivityManagerSingleton");
        Object mInstance = Reflex.getFieldObject("android.util.Singleton", gDefault, "mInstance");

        Class<?> classInterface = Class.forName("android.app.IActivityManager");

        Object proxy = Proxy.newProxyInstance(classInterface.getClassLoader(),
                new Class<?>[]{classInterface}, new AMNInvocationHanlder1(mInstance));
        Reflex.setFieldObject("android.util.Singleton", gDefault, "mInstance", proxy);
    }*/

    public static class AMNInvocationHanlder implements InvocationHandler {

        private String actionName = "startActivity";

        private Object target;

        public AMNInvocationHanlder(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.getName().equals(actionName)) {
                Log.d("---", "啦啦啦我是hook AMN进来的");
                return method.invoke(target, args);
            }

            return method.invoke(target, args);
        }

    }

    public static class AMNInvocationHanlder1 implements InvocationHandler {

        private String actionName = "startActivity";

        private Object target;

        public AMNInvocationHanlder1(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

            if (method.getName().equals(actionName)) {

                //取出真实的目标Intent
                Intent intent;
                int index = 0;
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Intent) {
                        index = i;
                        break;
                    }
                }
                intent = (Intent) args[index];
                String packageName = intent.getComponent().getPackageName();

                //替换冒充者
                Intent newIntent = new Intent();
                ComponentName componentName = new ComponentName(packageName, Main2Activity.class.getName());
                newIntent.setComponent(componentName);
                newIntent.putExtra(HookHelperUtils.TUREINTENT, intent);
                args[index] = newIntent;

                return method.invoke(target, args);
            }

            return method.invoke(target, args);
        }

    }
}
