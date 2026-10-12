package com.example.android.features.menu

import android.content.Context
import androidx.room3.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.core.data.AppDatabase
import com.example.android.features.menu.data.local.MenuDao
import com.example.android.features.menu.data.local.MenuEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MenuDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: MenuDao

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        db = Room.inMemoryDatabaseBuilder<AppDatabase>(context as Context).build()
        dao = db.menuDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeAll_returnsRowsOrderedByPosition() = runBlocking {
        dao.upsertAll(listOf(entity("profile", 2), entity("home", 1)))

        assertEquals(listOf("home", "profile"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun upsertAll_updatesExistingRowsByPrimaryKey() = runBlocking {
        dao.upsertAll(listOf(entity("home", 1)))
        dao.upsertAll(listOf(entity("home", 1).copy(text = "Pedir taxi ahora")))

        val rows = dao.observeAll().first()

        assertEquals(1, rows.size)
        assertEquals("Pedir taxi ahora", rows.first().text)
    }

    @Test
    fun replaceAll_removesRowsMissingFromTheNewList() = runBlocking {
        dao.upsertAll(listOf(entity("home", 1), entity("old", 2)))

        dao.replaceAll(listOf(entity("home", 1), entity("support", 2)))

        assertEquals(listOf("home", "support"), dao.observeAll().first().map { it.id })
    }

    @Test
    fun clearAllTables_emptiesTheMenu() = runBlocking {
        dao.upsertAll(listOf(entity("home", 1)))

        db.clearAllTables()

        assertTrue(dao.observeAll().first().isEmpty())
    }

    private fun entity(id: String, position: Int) = MenuEntity(
        id = id,
        text = "Texto $id",
        icon = "home",
        deeplink = "app-taxi://passenger/$id",
        position = position,
        updatedAt = 0L
    )
}
