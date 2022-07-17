package com.udacity.project4.locationreminders.data.local

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
  private lateinit var database: RemindersDatabase
private lateinit var reminderRepo :  RemindersLocalRepository
  @get:Rule
  val instantExecutor = InstantTaskExecutorRule()
  @Before
  fun initDb() {
    database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
      RemindersDatabase::class.java).allowMainThreadQueries()
      .build()
     reminderRepo = RemindersLocalRepository(database.reminderDao())

  }
  @After
  fun closeDb(){
    database.close()
  }




  @Test
  fun RepoSaveReminder(){
    runBlocking {
   // Given Reminder
    val reminder = ReminderDTO("Hello", "World", "EGYPT", 13.0, 12.0, "UNKONWN")
    reminderRepo.saveReminder(reminder)

    val result_reminder_from_repo = reminderRepo.getReminder(reminder.id) as Result.Success<ReminderDTO>
val reminder_from_repo = result_reminder_from_repo.data
      Log.d("Hello" , reminder_from_repo.toString())

     assertThat(reminder_from_repo  , notNullValue())
 assertThat(reminder_from_repo.id, `is`(reminder.id))
    assertThat(reminder_from_repo.description, `is`(reminder.description))
    assertThat(reminder_from_repo.location, `is`(reminder.location))


  }}
  @Test
  fun Error_RepoSaveReminder(){
    runBlocking {
      // Given Reminder
      val reminder = reminderRepo.getReminder("dummy") as Result.Error


      assertThat(reminder.message  ,`is` ("Reminder not found!"))



    }}

}

