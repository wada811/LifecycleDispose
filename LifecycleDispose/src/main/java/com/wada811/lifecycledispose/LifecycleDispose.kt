@file:Suppress("unused")

package com.wada811.lifecycledispose

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.INITIALIZED
import androidx.lifecycle.Lifecycle.State.RESUMED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
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
fun <T : Disposable> T.disposeOnLifecycle(activity: FragmentActivity): T =
    disposeOnLifecycleEvent(activity, when (activity.lifecycle.currentState) {
        INITIALIZED -> ON_DESTROY // onCreate
        CREATED -> ON_STOP // onStart, onStop
        STARTED, RESUMED -> ON_PAUSE // onResume, onPause
        DESTROYED -> ON_DESTROY // onDestroy
    })

fun <T : Disposable> T.disposeOnStop(activity: FragmentActivity): T = disposeOnLifecycleEvent(activity, ON_STOP)
fun <T : Disposable> T.disposeOnPause(activity: FragmentActivity): T = disposeOnLifecycleEvent(activity, ON_PAUSE)
fun <T : Disposable> T.disposeOnDestroy(activity: FragmentActivity): T = disposeOnLifecycleEvent(activity, ON_DESTROY)

private fun <T : Disposable> T.disposeOnLifecycleEvent(activity: FragmentActivity, lifecycleEvent: Event): T =
    disposeOnLifecycleEvents(activity.lifecycle, listOf(lifecycleEvent, ON_DESTROY))

/**
 * Dispose on corresponding lifecycle event.
 *
 * ```
 * | Subscribe     | ViewLifecycle.State   | Dispose       | Lifecycle.State | Dispose       |
 * | ------------- | --------------------- | ------------- | --------------- | ------------- |
 * | onAttach      | IllegalStateException | onDestroy     | INITIALIZED     | onDestroy     |
 * | onCreate      | IllegalStateException | onDestroy     | INITIALIZED     | onDestroy     |
 * | onCreateView  | IllegalStateException | onDestroyView | CREATED         | onDestroy     |
 * | onViewCreated | INITIALIZED           | onDestroyView | not called      | not called    |
 * | onStart       | CREATED               | onStop        | CREATED         | onStop        |
 * | onResume      | STARTED               | onPause       | STARTED         | onPause       |
 * | onPause       | STARTED               | onDestroyView | STARTED         | onDestroy     |
 * | onStop        | CREATED               | onDestroyView | CREATED         | onDestroy     |
 * | onDestroyView | DESTROYED             | onDestroyView | not called      | not called    |
 * | onDestroy     | IllegalStateException | onDestroy     | DESTROYED       | onDestroy     |
 * ```
 */
fun <T : Disposable> T.disposeOnLifecycle(fragment: Fragment): T =
    disposeOnLifecycleEvent(fragment, when (fragment.viewLifecycleOrLifecycle.currentState) {
        INITIALIZED -> ON_DESTROY // onAttach, onCreate, onViewCreated
        CREATED -> null // onCreateView: ON_DESTROY, onStart, onStop: ON_STOP
        STARTED, RESUMED -> ON_PAUSE // onResume, onPause
        DESTROYED -> ON_DESTROY // onDestroyView, onDestroy
    })

fun <T : Disposable> T.disposeOnStop(fragment: Fragment): T = disposeOnLifecycleEvent(fragment, ON_STOP)
fun <T : Disposable> T.disposeOnPause(fragment: Fragment): T = disposeOnLifecycleEvent(fragment, ON_PAUSE)
fun <T : Disposable> T.disposeOnDestroy(fragment: Fragment): T = disposeOnLifecycleEvent(fragment, ON_DESTROY)

private fun <T : Disposable> T.disposeOnLifecycleEvent(fragment: Fragment, lifecycleEvent: Event?): T =
    if (fragment.viewLifecycleOwnerLiveData.value != null) {
        val lifecycle = fragment.viewLifecycleOwner.lifecycle
        // 1: In onStart, Fragment has view.
        disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent ?: /* 1 */ON_STOP, ON_DESTROY))
    } else {
        val lifecycle = fragment.lifecycle
        when (lifecycle.currentState) {
            INITIALIZED -> disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_DESTROY))
            CREATED -> doOnLifecycleEvents(lifecycle, listOf(ON_START, ON_DESTROY)) {
                if (fragment.viewLifecycleOwnerLiveData.value != null) {
                    // 2: In onCreateView, Fragment has view.
                    //  2-1: If disposeOnPause/disposeOnStop/disposeOnDestroy called, dispose in lifecycleEvent
                    //  2-2: If disposeOnLifecycle called, dispose in onDestroyView
                    disposeOnLifecycleEvents(fragment.viewLifecycleOwner.lifecycle, listOfNotNull(/* 2-1 */lifecycleEvent, /* 2-2 */ON_DESTROY))
                } else {
                    // 3: In onStart, Fragment has no view.
                    //  3-1: If disposeOnPause/disposeOnStop/disposeOnDestroy called, dispose in lifecycleEvent
                    //  3-2: If disposeOnLifecycle called, dispose in onStop
                    // 4: In onStop, Fragment has no view.
                    //       If disposeOnPause/disposeOnStop/disposeOnDestroy called, dispose in onDestroy
                    //       If disposeOnLifecycle called, dispose in onDestroy
                    disposeOnLifecycleEvents(lifecycle, listOfNotNull(/* 3-1 */lifecycleEvent ?: /* 3-2 */ON_STOP, /* 4 */ON_DESTROY))
                }
            }
            else -> disposeOnLifecycleEvents(lifecycle, listOfNotNull(lifecycleEvent, ON_DESTROY))
        }
    }

private val Fragment.viewLifecycleOrLifecycle: Lifecycle
    get() = if (viewLifecycleOwnerLiveData.value != null) viewLifecycleOwner.lifecycle else lifecycle

private fun <T : Disposable> T.disposeOnLifecycleEvents(lifecycle: Lifecycle, lifecycleEvents: List<Event>): T =
    if (lifecycle.currentState == DESTROYED) safeDispose() else doOnLifecycleEvents(lifecycle, lifecycleEvents) { safeDispose() }

private fun <T : Disposable> T.doOnLifecycleEvents(lifecycle: Lifecycle, lifecycleEvents: List<Event>, onEvent: T.() -> Unit): T = this.also {
    lifecycle.addObserver(object : LifecycleEventObserver {
        override fun onStateChanged(source: LifecycleOwner, event: Event) {
            if (event in lifecycleEvents) {
                lifecycle.removeObserver(this)
                onEvent()
            }
        }
    })
}

private fun <T : Disposable> T.safeDispose(): T = this.also {
    if (!isDisposed) {
        dispose()
    }
}
