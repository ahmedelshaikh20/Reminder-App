package com.udacity.project4

import android.app.Activity
import android.app.Application
import android.app.PendingIntent.getActivity
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application
  private val dataBindingIdlingResource = DataBindingIdlingResource()

  /**
   * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
   * are not scheduled in the main Looper (for example when executed on a different thread).
   */
  @Before
  fun registerIdlingResource() {
    IdlingRegistry.getInstance().register(dataBindingIdlingResource)
  }

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }
  @After
  fun unregisterIdlingResource() {
    IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
  }


 // I Got error here in line 107
 // at androidx.test.core.app.ActivityScenario.launch(ActivityScenario.java:189)

  @Test
  fun testErrorSelectLocation() {
    val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
    dataBindingIdlingResource.monitorActivity(activityScenario)
    onView(withId(R.id.addReminderFAB)).perform(click())

    onView(withId(R.id.reminderTitle)).perform(replaceText("lool"))
    onView(withId(R.id.reminderDescription)).perform(replaceText("lool"))
    Thread.sleep(3000)

    onView(withId(R.id.saveReminder)).perform(click())

    val snackBarMessage = appContext.getString(R.string.err_select_location)
    onView(withText(snackBarMessage)).check(matches(isDisplayed()))

    activityScenario.close()
  }

  @Test
  fun testReminderSavedToastMessage() {
    val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
    dataBindingIdlingResource.monitorActivity(activityScenario)

    onView(withId(R.id.addReminderFAB)).perform(click())

    onView(withId(R.id.reminderTitle)).perform(replaceText("dummy"))
    onView(withId(R.id.reminderDescription)).perform(replaceText("dummy"))
    onView(withId(R.id.selectLocation)).perform(click())
    onView(withId(R.id.map)).perform(click())

    Thread.sleep(3000)
    onView(withId(R.id.map)).perform(click())

    onView(withId(R.id.Save_Button)).perform(click())
    onView(withId(R.id.saveReminder)).perform(click())

    onView(withText(R.string.reminder_saved)).inRoot(withDecorView(not(`is`(getActivity(activityScenario)?.window?.decorView))))
      .check(matches(isDisplayed()))

    activityScenario.close()
  }
  private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
    var activity: Activity? = null
    activityScenario.onActivity {
      activity = it
    }
    return activity
  }

}
