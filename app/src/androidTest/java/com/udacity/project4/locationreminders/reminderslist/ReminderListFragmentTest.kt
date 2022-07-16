package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import android.app.Application
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderFragment
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
  private lateinit var repository : ReminderDataSource
  private lateinit var appContext : Application

  @get:Rule
  val instantExecutor = InstantTaskExecutorRule()


 // Koin = Service Provider
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


repository = get().koin.get() as ReminderDataSource


    //clear the data to start fresh
    runBlocking {
      repository.deleteAllReminders()
    }
  }


  @Test
  fun Navigateto_save_reminder()  {
    // GIVEN - On the home screen
    val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
    val navController = mock(NavController::class.java)
    scenario.onFragment {
      Navigation.setViewNavController(it.view!!, navController)
    }
    // WHEN - Click on the "+" button
    onView(withId(R.id.addReminderFAB)).perform(click())
    verify(navController).navigate(
      ReminderListFragmentDirections.toSaveReminder())

  }

@Test
fun Displayed_Data_UI(){
  runBlocking {
    val reminder = ReminderDTO("Hello", "World", "EGYPT", 13.0, 12.0, "UNKONWN")
    repository.saveReminder(reminder)

      //    val fragmentArgs = bundleOf(repository.getReminders())

    launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

    onView(withId(R.id.reminderCardView)).check(matches(isDisplayed()))
  }
}

  @Test
  fun addReminder_Empty_Title_Error(){
    runBlocking {
      launchFragmentInContainer<SaveReminderFragment>(Bundle(), R.style.AppTheme)

      onView(withId(R.id.reminderTitle)).perform(ViewActions.replaceText(""))
      onView(withId(R.id.reminderDescription)).perform(ViewActions.replaceText(""))
      onView(withId(R.id.saveReminder)).perform(click())



      onView(withText("Please enter title")).check(matches(isDisplayed()));
    }
  }





}
