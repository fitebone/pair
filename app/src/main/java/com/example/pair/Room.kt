package com.example.pair

import androidx.room.*
import androidx.room.Dao
import kotlinx.serialization.Serializable

data class Peer (
    val endpointID: String,
    val uuid: String,
    val username: String,
)

@Entity(tableName = "swaps")
data class Swap (
    @PrimaryKey val id: String,
    val peerID_1: String,
    val peerID_2: String,
    val created: Long,
)

// Single piece of contact info; label with data
@Entity(tableName = "contact_info", primaryKeys = ["owner", "id"])
data class ContactInfo (
    val id: Int,
    val owner: String,
    val label: String,
    val data: String,
)

// Simple cards table, one of these is the local user account card
@Serializable
@Entity(tableName = "cards")
data class Card (
    @PrimaryKey val id: String,
    val username: String,
    val created: Long,
    //val pic: String,
    val card_count: Int, // TODO: what other stats to add?
)

@Dao
interface UserDao {
    // swap //
    @Insert
    fun insertSwap(swap: Swap)

    @Delete
    fun deleteSwap(swap: Swap)

    @Query("SELECT * FROM swaps WHERE :peerID = peerID_2 AND created = ( SELECT MAX(created) from swaps) LIMIT 1")
    fun getLatestSwap(peerID: String): Swap

    @Query("SELECT * FROM swaps")
    fun getAllSwaps(): List<Swap>

    @Query("SELECT COUNT(1) FROM swaps WHERE peerID_2 = :peerID OR peerID_1 = :peerID")
    fun doesSwapExistWithPeer(peerID: String): Int

    // CONTACT INFO //
    @Insert
    fun insertCI(info: ContactInfo)

    @Insert
    fun insertAllCI(vararg info: ContactInfo)

    @Delete
    fun deleteCI(info: ContactInfo)

    @Query("SELECT * FROM contact_info")
    fun getAllCI(): List<ContactInfo>

    // CARDS //
    @Insert
    fun insertCard(card: Card)

    @Update
    fun updateCard(card: Card)

    @Delete
    fun deleteCard(card: Card)

    @Query("SELECT * FROM cards WHERE id = :id")
    fun getCard(id: String): Card

    @Query("SELECT * FROM cards WHERE id != :id")
    fun getAllCards(id: String): List<Card>

    @Query("SELECT COUNT(1) FROM cards WHERE id = :id")
    fun checkCardExists(id: String): Int

    // NUKE DB //
    @Query("DELETE FROM cards WHERE id != :id")
    fun nukeCards(id: String)

    @Query("DELETE FROM swaps")
    fun nukeSwaps()

    @Query("DELETE FROM contact_info")
    fun nukeContactInfo()

    //@Query("SELECT * FROM users")
    //fun getAll(): List<User>
}

@Database(
    version = 1,
    entities = [Swap::class, ContactInfo::class, Card::class])
//@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}