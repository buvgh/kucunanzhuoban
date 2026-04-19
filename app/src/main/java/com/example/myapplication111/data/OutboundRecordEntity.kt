package com.example.myapplication111.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "outbound_records",
    foreignKeys = [
        ForeignKey(
            entity = RecordDateEntity::class,
            parentColumns = ["id"],
            childColumns = ["dateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["dateId"])]
)
data class OutboundRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateId: Long,
    val name: String,
    val count: Double,
    val weightPerUnit: Double,
    val timestamp: Long = System.currentTimeMillis()
)
