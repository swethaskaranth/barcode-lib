package com.poc.newlandscanner

import android.app.Activity
import android.app.Application
import android.os.Bundle

open class ActivityLifecycleAdapter: Application.ActivityLifecycleCallbacks {
    override fun onActivityPaused(activity: Activity?) {
        //TODO any action want to handle when activity onActivityPaused  globally
    }

    override fun onActivityResumed(activity: Activity?) {
        //TODO any action want to handle when activity onActivityResumed  globally
    }

    override fun onActivityStarted(activity: Activity?) {
        //TODO any action want to handle when activity onActivityStarted  globally
    }

    override fun onActivityDestroyed(activity: Activity?) {
        //TODO any action want to handle when activity onActivityDestroyed  globally
    }

    override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
        //TODO any action want to handle when activity onActivitySaveInstanceState  globally
    }

    override fun onActivityStopped(activity: Activity?) {
        //TODO any action want to handle when activity onActivityStopped  globally
    }

    override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
        //TODO any action want to handle when activity onActivityCreated  globally
    }
}