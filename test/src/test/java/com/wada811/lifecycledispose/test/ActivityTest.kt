package com.wada811.lifecycledispose.test

import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Lifecycle.Event.ON_CREATE
import androidx.lifecycle.Lifecycle.Event.ON_DESTROY
import androidx.lifecycle.Lifecycle.Event.ON_PAUSE
import androidx.lifecycle.Lifecycle.Event.ON_RESUME
import androidx.lifecycle.Lifecycle.Event.ON_START
import androidx.lifecycle.Lifecycle.Event.ON_STOP
import androidx.lifecycle.Lifecycle.State.CREATED
import androidx.lifecycle.Lifecycle.State.DESTROYED
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.test.core.app.ActivityScenario
import com.google.common.truth.Truth
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Test disposing on corresponding lifecycle event.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">Handling Lifecycles with Lifecycle-Aware Components | Android Developers</a>
 */
@Config(sdk = [Build.VERSION_CODES.P])
@RunWith(ParameterizedRobolectricTestRunner::class)
class ActivityTest(
    private val disposeStrategy: DisposeStrategy,
    private val subscribeLifecycleEvent: Lifecycle.Event,
    private val expectedLifecycleState: Lifecycle.State
) {

    @Test
    fun test() {
        var activity: DisposeActivity? = null
        ActivityScenario.launch<DisposeActivity>(DisposeActivity.createIntent(
            disposeStrategy,
            subscribeLifecycleEvent
        )).use { scenario ->
            scenario.onActivity { activity = it }
            scenario.moveToState(DESTROYED)
            Truth.assertThat(activity!!.disposedLifecycleState).isEqualTo(expectedLifecycleState)
            activity = null
        }
    }


    companion object {
        @Suppress("unused")
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "Strategy: {0}, subscribe: {1}, dispose: {2}")
        fun parameters(): Iterable<Array<Any>> {
            val parameters = mutableListOf<Array<Any>>()
            // OnLifecycle
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, ON_CREATE, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, ON_START, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, ON_RESUME, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, ON_PAUSE, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, ON_STOP, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnLifecycle, ON_DESTROY, DESTROYED))
            // OnPause
            parameters.add(arrayOf(DisposeStrategy.OnPause, ON_CREATE, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, ON_START, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, ON_RESUME, STARTED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, ON_PAUSE, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, ON_STOP, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnPause, ON_DESTROY, DESTROYED))
            // OnStop
            parameters.add(arrayOf(DisposeStrategy.OnStop, ON_CREATE, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, ON_START, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, ON_RESUME, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, ON_PAUSE, CREATED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, ON_STOP, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnStop, ON_DESTROY, DESTROYED))
            // OnDestroy
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, ON_CREATE, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, ON_START, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, ON_RESUME, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, ON_PAUSE, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, ON_STOP, DESTROYED))
            parameters.add(arrayOf(DisposeStrategy.OnDestroy, ON_DESTROY, DESTROYED))
            return parameters
        }
    }
}
