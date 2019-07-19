package com.example.hookapp;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //修改Activity中Instrumentation的值 为自定义的MyInstrumentation
        Instrumentation instrumentation = (Instrumentation) Reflex.getFieldObject(Activity.class, MainActivity.this, "mInstrumentation");
        MyInstrumentation instrumentation1 = new MyInstrumentation(instrumentation);
        Reflex.setFieldObject(Activity.class, this, "mInstrumentation", instrumentation1);

        /*try {
            HookHelperUtils.hookAmn();

//            HookHelperUtils.hookAmn2();
//            hookActivityThread();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }*/
    }


    public class MyInstrumentation extends Instrumentation {

        private Instrumentation instrumentation;

        public MyInstrumentation(Instrumentation instrumentation) {
            this.instrumentation = instrumentation;
        }

        public ActivityResult execStartActivity(Context who, IBinder contextThread, IBinder token, Activity target, Intent intent, int requestCode, Bundle options) {
            Log.d("-----", "啦啦啦我是hook进来的!");
            Class[] classes = {Context.class, IBinder.class, IBinder.class, Activity.class, Intent.class, int.class, Bundle.class};
            Object[] objects = {who, contextThread, token, target, intent, requestCode, options};
            Log.d("-----", "啦啦啦我是hook进来的!!");
            return (ActivityResult) Reflex.invokeInstanceMethod(instrumentation, "execStartActivity", classes, objects);
        }
    }




    public void openMain2(View view) {
        Intent intent = new Intent(MainActivity.this, Main2Activity.class);
        startActivity(intent);
    }

    public void openMain3(View view) {
        Intent intent = new Intent(MainActivity.this, Main3Activity.class);
        startActivity(intent);
    }

}