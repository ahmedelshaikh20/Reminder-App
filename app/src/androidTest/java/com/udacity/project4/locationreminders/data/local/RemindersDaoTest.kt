package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.TestResult

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
//using in-memory database.
//IM (in-memory) database is good option because it does not leave any trace back and you are sure that you will get empty tables before each test (generally a good practice).
  private lateinit var database: RemindersDatabase

  @get:Rule
  val instantExecutor = InstantTaskExecutorRule()
  @Before
  fun initDb() {
    database = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(),
      RemindersDatabase::class.java).build()
  }
  @After
  fun closeDb(){
    database.close()
  }

  @Test
  fun insertBufferoosSavesData() {
    runBlocking {
      //Given Reminder we add it to the database
      val reminder = ReminderDTO("Hello", "World", "EGYPT", 13.0, 12.0, "UNKONWN")
      val dao = database.reminderDao()
      dao.saveReminder(reminder)
     val reminder_from_db =dao.getReminderById(reminder.id)

      assertThat(reminder_from_db as ReminderDTO, notNullValue())
      assertThat(reminder_from_db.id, `is`(reminder.id))
      assertThat(reminder_from_db.description, `is`(reminder.description))
      assertThat(reminder_from_db.location, `is`(reminder.location))


    }

  }
}
