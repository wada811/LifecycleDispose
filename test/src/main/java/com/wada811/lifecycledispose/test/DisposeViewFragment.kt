package com.wada811.lifecycledispose.test

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit.SECONDS

class DisposeViewFragment : Fragment() {
    companion object {
        fun createBundle(
            disposeStrategy: DisposeStrategy,
            subscribeWhenLifecycleEvent: FragmentLifecycleEvent
        ) = Bundle().also {
            it.putInt(DisposeViewFragment::disposeStrategy.name, disposeStrategy.ordinal)
            it.putInt(DisposeViewFragment::subscribeWhenLifecycleEvent.name, subscribeWhenLifecycleEvent.ordinal)
        }
    }

    val disposeStrategy: DisposeStrategy by lazy {
        DisposeStrategy.values()[requireArguments().getInt(this::disposeStrategy.name, -1)]
    }
    val subscribeWhenLifecycleEvent: FragmentLifecycleEvent by lazy {
        FragmentLifecycleEvent.values()[requireArguments().getInt(this::subscribeWhenLifecycleEvent.name, -1)]
    }
    var disposedLifecycleState: Lifecycle.State? = null

    private val currentState: String
        get() = if (viewLifecycleOwnerLiveData.value != null) "${viewLifecycleOwner.lifecycle.currentState}" else "IllegalStateException"

    private fun subscribe(event: FragmentLifecycleEvent) {
        println("state: $currentState, event: $event")
        if (subscribeWhenLifecycleEvent == event) {
            disposeStrategy.dispose(
                this,
                Observable.interval(1, SECONDS)
                    .doOnSubscribe { println("subscribe: $currentState") }
                    .doOnDispose { println("dispose: $currentState") }
                    .doOnDispose { disposedLifecycleState = if (viewLifecycleOwnerLiveData.value != null) viewLifecycleOwner.lifecycle.currentState else lifecycle.currentState }
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        subscribe(FragmentLifecycleEvent.OnCreateView)
        return View(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        subscribe(FragmentLifecycleEvent.OnViewCreated)
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

    override fun onDestroyView() {
        super.onDestroyView()
        subscribe(FragmentLifecycleEvent.OnDestroyView)
    }

    override fun onDestroy() {
        super.onDestroy()
        subscribe(FragmentLifecycleEvent.OnDestroy)
    }
}