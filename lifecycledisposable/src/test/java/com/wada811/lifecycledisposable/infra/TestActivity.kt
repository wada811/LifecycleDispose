package com.wada811.lifecycledisposable.infra

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.wada811.lifecycledisposable.disposeOnLifecycle
import io.reactivex.Observable
import java.util.concurrent.TimeUnit.SECONDS

class TestActivity : FragmentActivity() {
    internal var onCreateDoOnDispose: () -> Unit = {}
    internal var onStartDoOnDispose: () -> Unit = {}
    internal var onResumeDoOnDispose: () -> Unit = {}
    internal var onPauseDoOnDispose: () -> Unit = {}
    internal var onStopDoOnDispose: () -> Unit = {}
    internal var onDestroyDoOnDispose: () -> Unit = {}

    private fun lifecycleState(): String = lifecycle.currentState.name.padEnd(11)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(TestNoViewFragment(), TestNoViewFragment::class.java.simpleName)
                .commit()
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, TestViewFragment())
                .commit()
        }

        Observable.interval(1, SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onCreate : ${lifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onCreate : ${lifecycleState()} on Dispose") }
            .doOnDispose { onCreateDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.v(this.javaClass.simpleName, "onPostCreate : ${lifecycleState()}")
    }

    override fun onStart() {
        super.onStart()
        Observable.interval(1, SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onStart  : ${lifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onStart  : ${lifecycleState()} on Dispose") }
            .doOnDispose { onStartDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onResume() {
        super.onResume()
        Observable.interval(1, SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onResume : ${lifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onResume : ${lifecycleState()} on Dispose") }
            .doOnDispose { onResumeDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onPostResume() {
        super.onPostResume()
        Log.v(this.javaClass.simpleName, "onPostResume : ${lifecycleState()}")
    }

    override fun onPause() {
        super.onPause()
        Observable.interval(1, SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onPause  : ${lifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onPause  : ${lifecycleState()} on Dispose") }
            .doOnDispose { onPauseDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onStop() {
        super.onStop()
        Observable.interval(1, SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onStop   : ${lifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onStop   : ${lifecycleState()} on Dispose") }
            .doOnDispose { onStopDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Observable.interval(1, SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onDestroy: ${lifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onDestroy: ${lifecycleState()} on Dispose") }
            .doOnDispose { onDestroyDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }
}
