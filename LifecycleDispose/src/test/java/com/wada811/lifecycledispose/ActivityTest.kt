package com.wada811.lifecycledispose

import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.wada811.lifecycledispose.infra.TestActivity
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test disposing on corresponding lifecycle event.
 *
 * @see <a href="https://developer.android.com/topic/libraries/architecture/lifecycle#lc">Handling Lifecycles with Lifecycle-Aware Components | Android Developers</a>
 */
@RunWith(AndroidJUnit4::class)
class ActivityTest {

    @get:Rule
    val rule: ActivityScenarioRule<TestActivity> = ActivityScenarioRule(TestActivity::class.java)

    @Test
    fun onCreate() {
        rule.scenario.use { scenario ->
            scenario.onActivity { activity ->
                activity.onCreateDoOnDispose = {
                    Assert.assertEquals(Lifecycle.State.DESTROYED, activity.lifecycle.currentState)
                }
            }
        }
    }

    @Test
    fun onStart() {
        rule.scenario.use { scenario ->
            scenario.onActivity { activity ->
                activity.onStartDoOnDispose = {
                    Assert.assertEquals(Lifecycle.State.CREATED, activity.lifecycle.currentState)
                }
            }
        }
    }

    @Test
    fun onResume() {
        rule.scenario.use { scenario ->
            scenario.onActivity { activity ->
                activity.onResumeDoOnDispose = {
                    Assert.assertEquals(Lifecycle.State.STARTED, activity.lifecycle.currentState)
                }
            }
        }
    }

    @Test
    fun onPause() {
        rule.scenario.use { scenario ->
            scenario.onActivity { activity ->
                activity.onPauseDoOnDispose = {
                    Assert.assertEquals(Lifecycle.State.DESTROYED, activity.lifecycle.currentState)
                }
            }
        }
    }

    @Test
    fun onStop() {
        rule.scenario.use { scenario ->
            scenario.onActivity { activity ->
                activity.onStopDoOnDispose = {
                    Assert.assertEquals(Lifecycle.State.DESTROYED, activity.lifecycle.currentState)
                }
            }
        }
    }

    @Test
    fun onDestroy() {
        rule.scenario.use { scenario ->
            scenario.onActivity { activity ->
                activity.onDestroyDoOnDispose = {
                    Assert.assertEquals(Lifecycle.State.DESTROYED, activity.lifecycle.currentState)
                }
            }
        }
    }
}
