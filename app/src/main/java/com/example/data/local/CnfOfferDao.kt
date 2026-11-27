package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CnfOfferDao {
    @Query("SELECT * FROM cnf_offers ORDER BY createdAt DESC, id DESC")
    fun getAllOffers(): Flow<List<CnfOfferEntity>>

    @Query("SELECT * FROM cnf_offers ORDER BY createdAt DESC, id DESC")
    suspend fun getAllOffersOnce(): List<CnfOfferEntity>

    @Query("SELECT COUNT(*) FROM cnf_offers")
    suspend fun getOfferCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffer(offer: CnfOfferEntity): Long

    @Update
    suspend fun updateOffer(offer: CnfOfferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(offers: List<CnfOfferEntity>)

    @Query("DELETE FROM cnf_offers WHERE id = :id")
    suspend fun deleteOfferById(id: Long)

    @Query("DELETE FROM cnf_offers")
    suspend fun deleteAll()

    @Transaction
    suspend fun replaceAll(offers: List<CnfOfferEntity>) {
        deleteAll()
        if (offers.isNotEmpty()) {
            insertAll(offers)
        }
    }
}
