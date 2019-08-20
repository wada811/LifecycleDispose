@file:Suppress("unused")

package com.wada811.lifecycledispose

import androidx.annotation.Keep
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
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
fun <TDisposable : Disposable> TDisposable.disposeOnLifecycle(activity: FragmentActivity): TDisposable =
    this.also {
        disposeOnLifecycleEvent(activity, when (activity.lifecycle.currentState) {
            INITIALIZED -> ON_DESTROY // onCreate
            CREATED -> ON_STOP // onStart, onStop
            STARTED, RESUMED -> ON_PAUSE // onResume, onPause
            else -> ON_DESTROY // onDestroy
        })
    }

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnStop(activity: FragmentActivity): TDisposable = disposeOnLifecycleEvent(activity, ON_STOP)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnPause(activity: FragmentActivity): TDisposable = disposeOnLifecycleEvent(activity, ON_PAUSE)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnDestroy(activity: FragmentActivity): TDisposable = disposeOnLifecycleEvent(activity, ON_DESTROY)


@Keep
private fun <TDisposable : Disposable> TDisposable.disposeOnLifecycleEvent(activity: FragmentActivity, lifecycleEvent: Lifecycle.Event): TDisposable =
    this.also {
        when (activity.lifecycle.currentState) {
            DESTROYED -> dispose()
            else -> disposeOnLifecycleEvents(activity.lifecycle, lifecycleEvent, ON_DESTROY)
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
fun <TDisposable : Disposable> TDisposable.disposeOnLifecycle(fragment: Fragment): TDisposable =
    this.also {
        val lifecycle = if (fragment.isViewCreated()) fragment.viewLifecycleOwner.lifecycle else fragment.lifecycle
        disposeOnLifecycleEvent(fragment, when (lifecycle.currentState) {
            INITIALIZED -> ON_DESTROY // onCreate, onCreateView
            CREATED -> ON_STOP // onStart, onStop
            STARTED, RESUMED -> ON_PAUSE // onResume, onPause
            else -> ON_DESTROY // onDestroyView, onDestroy
        })
    }

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnStop(fragment: Fragment): TDisposable = disposeOnLifecycleEvent(fragment, ON_STOP)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnPause(fragment: Fragment): TDisposable = disposeOnLifecycleEvent(fragment, ON_PAUSE)

@Keep
fun <TDisposable : Disposable> TDisposable.disposeOnDestroy(fragment: Fragment): TDisposable = disposeOnLifecycleEvent(fragment, ON_DESTROY)

private fun <TDisposable : Disposable> TDisposable.disposeOnLifecycleEvent(fragment: Fragment, lifecycleEvent: Lifecycle.Event): TDisposable =
    this.also {
        if (fragment.isViewCreated()) {
            val lifecycle = fragment.viewLifecycleOwner.lifecycle
            when (lifecycle.currentState) {
                INITIALIZED -> throw IllegalStateException("Fragment's view should have created.")
                // onStart, onStop
                CREATED -> disposeOnLifecycleEvents(lifecycle, lifecycleEvent, ON_DESTROY)
                // onResume, onPause
                STARTED, RESUMED -> disposeOnLifecycleEvents(lifecycle, lifecycleEvent, ON_DESTROY)
                // onDestroyView
                else -> dispose()
            }
        } else {
            val lifecycle = fragment.lifecycle
            when (lifecycle.currentState) {
                INITIALIZED -> disposeOnLifecycleEvents(lifecycle, ON_DESTROY)
                CREATED -> doOnLifecycleEvents(lifecycle, { event ->
                    // Evaluate which Fragment has view after onCreateView.
                    if (event == ON_START) {
                        if (fragment.isViewCreated()) {
                            // onCreateView, onViewCreated, onActivityCreated
                            disposeOnLifecycleEvent(fragment, lifecycleEvent)
                        } else {
                            // onStart
                            disposeOnLifecycleEvent(fragment, lifecycleEvent)
                        }
                    } else if (event == lifecycleEvent || event == ON_DESTROY) {
                        // onStop
                        dispose()
                    }
                }, ON_START, lifecycleEvent, ON_DESTROY)
                // onResume, onPause
                STARTED, RESUMED -> disposeOnLifecycleEvents(lifecycle, lifecycleEvent, ON_DESTROY)
                // onDestroy
                else -> dispose()
            }
        }
    }

private fun Disposable.disposeOnLifecycleEvents(lifecycle: Lifecycle, vararg lifecycleEvents: Lifecycle.Event) {
    doOnLifecycleEvents(lifecycle, { dispose() }, *lifecycleEvents)
}

private fun Disposable.doOnLifecycleEvents(
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
        this.view != null && this.viewLifecycleOwner.lifecycle.currentState.isAtLeast(CREATED)
    } catch (e: Exception) {
        false
    }
}
