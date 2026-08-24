package br.com.edmundo.desafiomb.core.data.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import br.com.edmundo.desafiomb.core.data.local.CmcDatabase

fun createInMemoryDatabase(): CmcDatabase = Room.inMemoryDatabaseBuilder(
    ApplicationProvider.getApplicationContext(),
    CmcDatabase::class.java,
).allowMainThreadQueries().build()
