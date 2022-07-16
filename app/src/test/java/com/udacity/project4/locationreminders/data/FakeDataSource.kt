package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

  var remindersData: ArrayList< ReminderDTO> = ArrayList()

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
      if(remindersData.isEmpty()!=true)
      return Result.Success(remindersData.toList())
      else
        return Result.Error("List of reminders is empty")

    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
      if(reminder!=null)
remindersData.add(reminder)


    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
for (reminder in remindersData){
  if(reminder.id == id){
    return Result.Success(reminder)
  }
}
      return Result.Error("No Reminder Found")

    }

    override suspend fun deleteAllReminders() {
remindersData.clear()
    }


}
