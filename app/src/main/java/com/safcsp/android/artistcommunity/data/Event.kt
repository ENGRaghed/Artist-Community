package com.safcsp.android.artistcommunity.data


data class Event(val publisher : String,
                 val id : String,
                 val photo : String,
                 val title : String,
                 val description : String,
                 val startDate : Long,
                 val dueDate: Long ,
                 val location: Latlng)