package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class VanguardRepository(private val context: Context) {

    private val database: VanguardDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            VanguardDatabase::class.java,
            "vanguard_tactics_database"
        ).build()
    }

    val operatorDao = database.operatorDao()
    val weaponDao = database.weaponDao()
    val historyDao = database.simulationHistoryDao()

    val allOperators: Flow<List<OperatorEntity>> = operatorDao.getAllOperators()
    val allWeapons: Flow<List<WeaponEntity>> = weaponDao.getAllWeapons()
    val allHistory: Flow<List<SimulationHistoryEntity>> = historyDao.getHistory()

    suspend fun insertOperator(operator: OperatorEntity) {
        operatorDao.insertOperator(operator)
    }

    suspend fun deleteOperator(operator: OperatorEntity) {
        operatorDao.deleteOperator(operator)
    }

    suspend fun insertWeapon(weapon: WeaponEntity) {
        weaponDao.insertWeapon(weapon)
    }

    suspend fun deleteWeapon(weapon: WeaponEntity) {
        weaponDao.deleteWeapon(weapon)
    }

    suspend fun insertHistory(history: SimulationHistoryEntity) {
        historyDao.insertHistory(history)
    }

    suspend fun clearHistory() {
        historyDao.clearHistory()
    }

    suspend fun checkAndPrepopulate() {
        if (operatorDao.getCount() == 0) {
            val defaultOperators = listOf(
                OperatorEntity(
                    name = "Sgt. Caleb Miller",
                    role = "Assault",
                    bodyProportion = 1.05f,
                    skinTexture = "Rugged",
                    eyeColor = "Tactical Green",
                    hairStyle = "Buzzcut",
                    helmet = "Advanced NVG Helmet",
                    armor = "Carbon Chestplate",
                    backpack = "Assault Pack",
                    accentColorHex = "#3DDC84",
                    survivalRating = 88,
                    accuracy = 84
                ),
                OperatorEntity(
                    name = "Recon Ghost",
                    role = "Sniper",
                    bodyProportion = 0.98f,
                    skinTexture = "Camo Painted",
                    eyeColor = "Ice Blue",
                    hairStyle = "Undercut",
                    helmet = "Special Ops Cap",
                    armor = "Ghillie Hood",
                    backpack = "None",
                    accentColorHex = "#00E5FF",
                    survivalRating = 94,
                    accuracy = 96
                ),
                OperatorEntity(
                    name = "Viper",
                    role = "Recon",
                    bodyProportion = 0.95f,
                    skinTexture = "Smooth",
                    eyeColor = "Dark Amber",
                    hairStyle = "Recon Ponytail",
                    helmet = "None",
                    armor = "Lightweight Vest",
                    backpack = "Survival Pack",
                    accentColorHex = "#FFD600",
                    survivalRating = 80,
                    accuracy = 78
                ),
                OperatorEntity(
                    name = "Aegis",
                    role = "Support",
                    bodyProportion = 1.12f,
                    skinTexture = "Battle-Scarred",
                    eyeColor = "Hazel Brown",
                    hairStyle = "Braided Tactical",
                    helmet = "Lightweight Assault",
                    armor = "Reinforced Kevlar",
                    backpack = "Medic Rig",
                    accentColorHex = "#FF3D00",
                    survivalRating = 85,
                    accuracy = 72
                )
            )
            for (op in defaultOperators) {
                operatorDao.insertOperator(op)
            }
        }

        if (weaponDao.getCount() == 0) {
            val defaultWeapons = listOf(
                WeaponEntity(
                    name = "ARC-9 Sentinel",
                    type = "Assault Rifle",
                    damage = 42,
                    fireRate = 750,
                    recoil = 45,
                    range = 250,
                    magCapacity = 30,
                    scopeType = "2x Holo",
                    camo = "Digital Urban",
                    description = "Modular high-velocity rifle with low recoil and rapid semi-auto accuracy."
                ),
                WeaponEntity(
                    name = "Apex-50 Ghost",
                    type = "Sniper Rifle",
                    damage = 95,
                    fireRate = 50,
                    recoil = 85,
                    range = 800,
                    magCapacity = 5,
                    scopeType = "8x Ballistic",
                    camo = "Carbon Black",
                    description = "High-caliber bolt-action system delivering lethal single-shot terminal energy."
                ),
                WeaponEntity(
                    name = "Specter-9 Swift",
                    type = "SMG",
                    damage = 28,
                    fireRate = 950,
                    recoil = 30,
                    range = 80,
                    magCapacity = 40,
                    scopeType = "RDS",
                    camo = "Neon Circuit",
                    description = "Ultra-lightweight compact defense weapon perfect for quick sliding and flanking maneuvers."
                ),
                WeaponEntity(
                    name = "Kodiak-12 Breacher",
                    type = "Shotgun",
                    damage = 80,
                    fireRate = 120,
                    recoil = 90,
                    range = 25,
                    magCapacity = 8,
                    scopeType = "Iron Sights",
                    camo = "Desert Storm",
                    description = "Close-quarters heavy gauge semi-automatic scattergun. Devastating up close."
                )
            )
            for (wp in defaultWeapons) {
                weaponDao.insertWeapon(wp)
            }
        }
    }
}
