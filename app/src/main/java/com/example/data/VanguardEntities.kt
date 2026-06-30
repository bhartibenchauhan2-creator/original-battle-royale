package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // "Assault", "Sniper", "Recon", "Support"
    val bodyProportion: Float, // 1.0 = Standard athletic, scale slider
    val skinTexture: String, // "Smooth", "Rugged", "Battle-Scarred", "Camo Painted"
    val eyeColor: String, // "Ice Blue", "Tactical Green", "Hazel Brown", "Dark Amber"
    val hairStyle: String, // "Undercut", "Buzzcut", "Braided Tactical", "Recon Ponytail"
    val helmet: String, // "None", "Lightweight Assault", "Advanced NVG Helmet", "Special Ops Cap"
    val armor: String, // "Carbon Chestplate", "Lightweight Vest", "Reinforced Kevlar", "Ghillie Hood"
    val backpack: String, // "Survival Pack", "Assault Pack", "Medic Rig", "None"
    val accentColorHex: String = "#3DDC84", // UI highlight color
    val survivalRating: Int = 75, // 0 to 100
    val accuracy: Int = 80 // 0 to 100
)

@Entity(tableName = "weapons")
data class WeaponEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Assault Rifle", "Sniper Rifle", "SMG", "Shotgun", "Pistol"
    val damage: Int,
    val fireRate: Int,
    val recoil: Int, // 0-100 (lower is better recoil reduction)
    val range: Int,
    val magCapacity: Int,
    val scopeType: String, // "RDS", "2x Holo", "4x ACOG", "8x Ballistic", "Iron Sights"
    val camo: String, // "Digital Urban", "Forest Ambush", "Carbon Black", "Desert Storm", "Neon Circuit"
    val description: String
)

@Entity(tableName = "simulation_history")
data class SimulationHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val placement: Int, // 1 to 100 (1 is Champion)
    val kills: Int,
    val damageDealt: Int,
    val accuracyPercent: Int,
    val weather: String, // "Rain", "Fog", "Sunset", "Thunderstorm", "Clear"
    val location: String, // "Lush Forest", "Abandoned City", "Industrial Sector", "Military Compound"
    val gameLogJson: String // Serialized tactical steps for retro playback
)

@Dao
interface OperatorDao {
    @Query("SELECT * FROM operators ORDER BY name ASC")
    fun getAllOperators(): Flow<List<OperatorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOperator(operator: OperatorEntity)

    @Delete
    suspend fun deleteOperator(operator: OperatorEntity)

    @Query("SELECT COUNT(*) FROM operators")
    suspend fun getCount(): Int
}

@Dao
interface WeaponDao {
    @Query("SELECT * FROM weapons ORDER BY type ASC, name ASC")
    fun getAllWeapons(): Flow<List<WeaponEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeapon(weapon: WeaponEntity)

    @Delete
    suspend fun deleteWeapon(weapon: WeaponEntity)

    @Query("SELECT COUNT(*) FROM weapons")
    suspend fun getCount(): Int
}

@Dao
interface SimulationHistoryDao {
    @Query("SELECT * FROM simulation_history ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<SimulationHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: SimulationHistoryEntity)

    @Query("DELETE FROM simulation_history")
    suspend fun clearHistory()
}

@Database(
    entities = [OperatorEntity::class, WeaponEntity::class, SimulationHistoryEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VanguardDatabase : RoomDatabase() {
    abstract fun operatorDao(): OperatorDao
    abstract fun weaponDao(): WeaponDao
    abstract fun simulationHistoryDao(): SimulationHistoryDao
}
