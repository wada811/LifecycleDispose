package com.wada811.lifecycledispose.infra

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.wada811.lifecycledispose.disposeOnLifecycle
import io.reactivex.Observable
import java.util.concurrent.TimeUnit

class TestNoViewFragment : Fragment() {
    internal var onCreateDoOnDispose: () -> Unit = {}
    internal var onCreateViewDoOnDispose: () -> Unit = {}
    internal var onStartDoOnDispose: () -> Unit = {}
    internal var onResumeDoOnDispose: () -> Unit = {}
    internal var onPauseDoOnDispose: () -> Unit = {}
    internal var onStopDoOnDispose: () -> Unit = {}
    internal var onDestroyViewDoOnDispose: () -> Unit = {}
    internal var onDestroyDoOnDispose: () -> Unit = {}

    private fun lifecycleState(): String = lifecycle.currentState.name.padEnd(11)

    private fun viewLifecycleState(): String {
        return try {
            if (this.view != null) {
                viewLifecycleOwner.lifecycle.currentState.name.padEnd(12)
            } else {
                "null"
            }
        } catch (e: Exception) {
            e.javaClass.simpleName.substring(0, 12)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.v(this.javaClass.simpleName, "onAttach: Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onCreate     : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onCreate     : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onCreateDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onCreateView : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onCreateView : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onCreateViewDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(this.javaClass.simpleName, "onActivityCreated: Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()}")
    }

    override fun onStart() {
        super.onStart()
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onStart      : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onStart      : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onStartDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onResume() {
        super.onResume()
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onResume     : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onResume     : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onResumeDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onPause() {
        super.onPause()
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onPause      : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onPause      : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onPauseDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onStop() {
        super.onStop()
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onStop       : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onStop       : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onStopDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onDestroyView: Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onDestroyView: Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onDestroyViewDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        Observable.interval(1, TimeUnit.SECONDS)
            .doOnSubscribe { Log.d(this.javaClass.simpleName, "onDestroy    : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Subscribe") }
            .doOnDispose { Log.d(this.javaClass.simpleName, "onDestroy    : Lifecycle: ${lifecycleState()}, ViewLifecycle: ${viewLifecycleState()} on Dispose") }
            .doOnDispose { onDestroyDoOnDispose() }
            .subscribe()
            .disposeOnLifecycle(this)
    }
}
