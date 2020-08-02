package com.wada811.lifecycledispose.test

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit.SECONDS

class DisposeNoViewFragment : Fragment() {
    companion object {
        fun createBundle(
            disposeStrategy: DisposeStrategy,
            subscribeWhenLifecycleEvent: FragmentLifecycleEvent
        ) = Bundle().also {
            it.putInt(DisposeNoViewFragment::disposeStrategy.name, disposeStrategy.ordinal)
            it.putInt(DisposeNoViewFragment::subscribeWhenLifecycleEvent.name, subscribeWhenLifecycleEvent.ordinal)
        }
    }

    val disposeStrategy: DisposeStrategy by lazy {
        DisposeStrategy.values()[requireArguments().getInt(this::disposeStrategy.name, -1)]
    }
    val subscribeWhenLifecycleEvent: FragmentLifecycleEvent by lazy {
        FragmentLifecycleEvent.values()[requireArguments().getInt(this::subscribeWhenLifecycleEvent.name, -1)]
    }
    var disposedLifecycleState: Lifecycle.State? = null

    private fun subscribe(event: FragmentLifecycleEvent) {
        println("state: ${lifecycle.currentState}, event: $event")
        if (subscribeWhenLifecycleEvent == event) {
            disposeStrategy.dispose(
                this,
                Observable.interval(1, SECONDS)
                    .doOnSubscribe { println("subscribe: ${lifecycle.currentState}") }
                    .doOnDispose { println("dispose: ${lifecycle.currentState}") }
                    .doOnDispose { disposedLifecycleState = lifecycle.currentState }
                    .subscribe()
            )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        subscribe(FragmentLifecycleEvent.OnAttach)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        subscribe(FragmentLifecycleEvent.OnCreate)
    }

    override fun onStart() {
        super.onStart()
        subscribe(FragmentLifecycleEvent.OnStart)
    }

    override fun onResume() {
        super.onResume()
        subscribe(FragmentLifecycleEvent.OnResume)
    }

    override fun onPause() {
        super.onPause()
        subscribe(FragmentLifecycleEvent.OnPause)
    }

    override fun onStop() {
        super.onStop()
        subscribe(FragmentLifecycleEvent.OnStop)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribe(FragmentLifecycleEvent.OnDestroy)
    }
}