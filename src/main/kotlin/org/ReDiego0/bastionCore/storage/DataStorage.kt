package org.ReDiego0.bastionCore.storage

import org.ReDiego0.bastionCore.data.PlayerData
import java.util.*

interface DataStorage {
    fun init()
    fun close()

    fun loadPlayer(uuid: UUID, name: String): PlayerData
    fun savePlayer(data: PlayerData)
}