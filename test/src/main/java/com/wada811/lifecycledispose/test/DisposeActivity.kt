package com.wada811.lifecycledispose.test

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit.SECONDS

class DisposeActivity : FragmentActivity() {
    companion object {
        fun createIntent(
            disposeStrategy: DisposeStrategy,
            subscribeWhenLifecycleEvent: Lifecycle.Event
        ) = Intent("com.wada811.lifecycledispose.test.ACTION_TEST").also {
            it.putExtra(DisposeActivity::disposeStrategy.name, disposeStrategy.ordinal)
            it.putExtra(DisposeActivity::subscribeWhenLifecycleEvent.name, subscribeWhenLifecycleEvent.ordinal)
        }
    }

    val disposeStrategy: DisposeStrategy by lazy {
        DisposeStrategy.values()[intent.getIntExtra(this::disposeStrategy.name, -1)]
    }
    val subscribeWhenLifecycleEvent: Lifecycle.Event by lazy {
        Lifecycle.Event.values()[intent.getIntExtra(this::subscribeWhenLifecycleEvent.name, -1)]
    }
    var disposedLifecycleState: Lifecycle.State? = null

    private fun subscribe(event: Lifecycle.Event) {
        println("state: ${lifecycle.currentState}, event: $event")
        if (subscribeWhenLifecycleEvent == event) {
            disposeStrategy.dispose(
                this,
                Observable.interval(1, SECONDS)
                    .doOnDispose { disposedLifecycleState = lifecycle.currentState }
                    .subscribe()
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribe(ON_CREATE)
    }

    override fun onStart() {
        super.onStart()
        subscribe(ON_START)
    }

    override fun onResume() {
        super.onResume()
        subscribe(ON_RESUME)
    }

    override fun onPause() {
        super.onPause()
        subscribe(ON_PAUSE)
    }

    override fun onStop() {
        super.onStop()
        subscribe(ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribe(ON_DESTROY)
    }
}
