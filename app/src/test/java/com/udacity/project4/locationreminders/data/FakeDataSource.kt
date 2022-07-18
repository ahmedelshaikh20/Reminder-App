package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {

  var remindersData: ArrayList< ReminderDTO> = ArrayList()
   var forceError : Boolean  = false

  fun forceErr(){
    forceError=true
  }
  fun ResetErr(){
    forceError = false
  }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
      return try {
        if(forceError) {
          throw Exception("Reminders not found")
        }
        Result.Success((remindersData))
      } catch (ex: Exception) {
        Result.Error(ex.localizedMessage)
      }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
      if(reminder!=null)
remindersData.add(reminder)


    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
for (reminder in remindersData){

  if(forceError == true){
    throw Exception("Reminders not found")
  }
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
