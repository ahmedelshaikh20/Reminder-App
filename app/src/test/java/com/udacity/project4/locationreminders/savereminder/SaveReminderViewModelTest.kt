package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.P])

class SaveReminderViewModelTest {


  lateinit var saveReminderViewModel: SaveReminderViewModel

  // Executes each task synchronously using Architecture Components.
  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  var mainCoroutineRule = MainCoroutineRule()
  init {
    stopKoin()
    val FakeDataSource = FakeDataSource()
    saveReminderViewModel = SaveReminderViewModel(Application(), FakeDataSource)
  }


  @Test
  fun ValdiateEnteredDataFunc_Error () = runBlocking {
    val reminder = ReminderDataItem("", "World", "EGYPT", 13.0, 12.0, "UNKONWN")

    val result = saveReminderViewModel.validateEnteredData(reminder)
    MatcherAssert.assertThat(result, CoreMatchers.`is`(false))
  }

}
