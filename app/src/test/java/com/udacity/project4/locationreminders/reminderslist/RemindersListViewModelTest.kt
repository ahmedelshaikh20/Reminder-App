package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.pauseDispatcher
import kotlinx.coroutines.test.resumeDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.not
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.P])
class RemindersListViewModelTest {
  lateinit var reminderViewModel: RemindersListViewModel
lateinit var fakeDataSource : FakeDataSource
  // Executes each task synchronously using Architecture Components.
  @get:Rule
  var instantExecutorRule = InstantTaskExecutorRule()

  @get:Rule
  var mainCoroutineRule = MainCoroutineRule()
  init {
    stopKoin()
    fakeDataSource = FakeDataSource()
    reminderViewModel = RemindersListViewModel(Application(), fakeDataSource)
  }

  @Test
  fun CheckLoading_AfterLoadingReminder() = runBlocking {

    mainCoroutineRule.pauseDispatcher()
    reminderViewModel.loadReminders()
    MatcherAssert.assertThat(reminderViewModel.showLoading.value, CoreMatchers.`is`(true))
    mainCoroutineRule.resumeDispatcher()
    fakeDataSource.forceErr()
    MatcherAssert.assertThat(reminderViewModel.showLoading.value, CoreMatchers.`is`(false))


  }

}
