@file:Suppress("unused")

package com.wada811.lifecycledisposable

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.Lifecycle.Event.ON_DESTROY
import android.arch.lifecycle.Lifecycle.Event.ON_PAUSE
import android.arch.lifecycle.Lifecycle.Event.ON_START
import android.arch.lifecycle.Lifecycle.Event.ON_STOP
import android.arch.lifecycle.Lifecycle.State.CREATED
import android.arch.lifecycle.Lifecycle.State.DESTROYED
import android.arch.lifecycle.Lifecycle.State.INITIALIZED
import android.arch.lifecycle.Lifecycle.State.RESUMED
import android.arch.lifecycle.Lifecycle.State.STARTED
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
import android.support.annotation.Keep
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import io.reactivex.disposables.Disposable

/**
 * Dispose on corresponding lifecycle event.
 *
 * ```
 * | Subscribe | Lifecycle.State | Dispose   |
 * | --------- | --------------- | --------- |
 * | onCreate  | INITIALIZED     | onDestroy |
 * | onStart   | CREATED         | onStop    |
 * | onResume  | STARTED         | onPause   |
 * | onPause   | STARTED         | onDestroy |
 * | onStop    | CREATED         | onDestroy |
 * | onDestroy | DESTROYED       | onDestroy |
 * ```
 */
@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnLifecycle(activity: FragmentActivity, lifecycleEvent: Lifecycle.Event? = null): TDisposable =
    this.also {
        when (activity.lifecycle.currentState) {
            // onCreate
            Lifecycle.State.INITIALIZED -> disposeOnLifecycleEvent(activity.lifecycle, lifecycleEvent ?: ON_DESTROY, ON_DESTROY)
            // onStart, onStop
            Lifecycle.State.CREATED -> disposeOnLifecycleEvent(activity.lifecycle, lifecycleEvent ?: ON_STOP, ON_DESTROY)
            // onPostCreate, onResume, onPause
            Lifecycle.State.STARTED -> disposeOnLifecycleEvent(activity.lifecycle, lifecycleEvent ?: ON_PAUSE, ON_DESTROY)
            // onPostResume
            Lifecycle.State.RESUMED -> disposeOnLifecycleEvent(activity.lifecycle, lifecycleEvent ?: ON_PAUSE, ON_DESTROY)
            // onDestroy
            Lifecycle.State.DESTROYED -> dispose()
        }
    }

/**
 * Dispose on corresponding lifecycle event.
 *
 * ```
 * | Subscribe     | Lifecycle.State | Dispose       |
 * | ------------- | --------------- | ------------- |
 * | onCreate      | INITIALIZED     | onDestroy     |
 * | onCreateView  | INITIALIZED     | onDestroyView |
 * | onStart       | CREATED         | onStop        |
 * | onResume      | STARTED         | onPause       |
 * | onPause       | STARTED         | onDestroyView |
 * | onStop        | CREATED         | onDestroyView |
 * | onDestroyView | DESTROYED       | onDestroyView |
 * | onDestroy     | DESTROYED       | onDestroy     |
 * ```
 */
@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnLifecycle(fragment: Fragment, lifecycleEvent: Lifecycle.Event? = null): TDisposable =
    this.also {
        if (fragment.isViewCreated()) {
            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            when (lifecycle.currentState) {
                INITIALIZED -> throw IllegalStateException("Fragment' view should have created.")
                // onStart, onStop
                CREATED -> disposeOnLifecycleEvent(lifecycle, lifecycleEvent ?: ON_STOP, ON_DESTROY)
                // onResume, onPause
                STARTED, RESUMED -> disposeOnLifecycleEvent(lifecycle, lifecycleEvent ?: ON_PAUSE, ON_DESTROY)
                // onDestroyView, onDestroy
                DESTROYED -> dispose()
            }
        } else {
            val lifecycle = fragment.lifecycle
            when (lifecycle.currentState) {
                INITIALIZED -> disposeOnLifecycleEvent(lifecycle, ON_DESTROY)
                CREATED -> disposeOnLifecycleEvent(lifecycle, { event ->
                    // Evaluate which Fragment has view after onCreateView.
                    if (event == ON_START) {
                        if (fragment.isViewCreated()) {
                            // onCreateView, onViewCreated, onActivityCreated
                            disposeOnLifecycle(fragment, lifecycleEvent ?: ON_DESTROY)
                        } else {
                            // onStart
                            disposeOnLifecycle(fragment, lifecycleEvent ?: ON_STOP)
                        }
                    } else if (event == lifecycleEvent || event == ON_DESTROY) {
                        // onStop
                        dispose()
                    }
                }, ON_START, lifecycleEvent ?: ON_STOP, ON_DESTROY)
                // onResume, onPause
                STARTED, RESUMED -> disposeOnLifecycleEvent(lifecycle, lifecycleEvent ?: ON_PAUSE, ON_DESTROY)
                // onDestroyView, onDestroy
                DESTROYED -> dispose()
            }
        }
    }

private fun Disposable.disposeOnLifecycleEvent(lifecycle: Lifecycle, vararg lifecycleEvents: Lifecycle.Event) {
    disposeOnLifecycleEvent(lifecycle, { dispose() }, *lifecycleEvents)
}

private fun Disposable.disposeOnLifecycleEvent(
    lifecycle: Lifecycle,
    onEvent: Disposable.(Lifecycle.Event) -> Unit,
    vararg lifecycleEvents: Lifecycle.Event
) {
    lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
        fun onLifecycleEvent(@Suppress("UNUSED_PARAMETER") source: LifecycleOwner, event: Lifecycle.Event) {
            if (event in lifecycleEvents) {
                lifecycle.removeObserver(this)
                onEvent(event)
            }
        }
    })
}

private fun Fragment.isViewCreated(): Boolean {
    return try {
        this.view != null && this.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.CREATED)
    } catch (e: Exception) {
        false
    }
}
