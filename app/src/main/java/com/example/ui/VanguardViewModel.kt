package com.example.ui

import android.app.Application
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

// Classes for Real-time Canvas effects
data class CanvasParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val color: Color,
    var alpha: Float,
    var radius: Float,
    var life: Int,
    val maxLife: Int
)

data class BulletLine(
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    var life: Int,
    val maxLife: Int
)

data class ShellCasing(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var rotation: Float,
    val rotationSpeed: Float,
    var alpha: Float,
    var life: Int
)

data class OperatorState(
    val id: String,
    val name: String,
    var x: Float,
    var y: Float,
    var targetX: Float,
    var targetY: Float,
    var health: Int,
    var state: String, // "Sprinting", "Aiming", "Crouching", "Sliding", "Vaulting", "Crawling"
    val isHostile: Boolean,
    val role: String,
    var isAlive: Boolean = true,
    val color: Color
)

class VanguardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VanguardRepository(application)

    // Data lists observed from database
    val operators: StateFlow<List<OperatorEntity>> = repository.allOperators
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weapons: StateFlow<List<WeaponEntity>> = repository.allWeapons
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val history: StateFlow<List<SimulationHistoryEntity>> = repository.allHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Operator and Weapon for barracks/armory editors
    private val _selectedOperator = MutableStateFlow<OperatorEntity?>(null)
    val selectedOperator: StateFlow<OperatorEntity?> = _selectedOperator.asStateFlow()

    private val _selectedWeapon = MutableStateFlow<WeaponEntity?>(null)
    val selectedWeapon: StateFlow<WeaponEntity?> = _selectedWeapon.asStateFlow()

    // Interactive simulator states
    private val _simRunning = MutableStateFlow(false)
    val simRunning: StateFlow<Boolean> = _simRunning.asStateFlow()

    private val _simLocation = MutableStateFlow("Lush Forest")
    val simLocation: StateFlow<String> = _simLocation.asStateFlow()

    private val _simWeather = MutableStateFlow("Sunset")
    val simWeather: StateFlow<String> = _simWeather.asStateFlow()

    private val _simAliveCount = MutableStateFlow(100)
    val simAliveCount: StateFlow<Int> = _simAliveCount.asStateFlow()

    private val _simKills = MutableStateFlow(0)
    val simKills: StateFlow<Int> = _simKills.asStateFlow()

    private val _simDamage = MutableStateFlow(0)
    val simDamage: StateFlow<Int> = _simDamage.asStateFlow()

    private val _simLogs = MutableStateFlow<List<String>>(emptyList())
    val simLogs: StateFlow<List<String>> = _simLogs.asStateFlow()

    private val _simPlacement = MutableStateFlow<Int?>(null)
    val simPlacement: StateFlow<Int?> = _simPlacement.asStateFlow()

    // Real-time map elements for Canvas
    private val _operatorsOnMap = MutableStateFlow<List<OperatorState>>(emptyList())
    val operatorsOnMap: StateFlow<List<OperatorState>> = _operatorsOnMap.asStateFlow()

    // Particle, Bullet tracers and shell casings on simulation canvas
    val particles = MutableStateFlow<List<CanvasParticle>>(emptyList())
    val bulletTracers = MutableStateFlow<List<BulletLine>>(emptyList())
    val shellCasings = MutableStateFlow<List<ShellCasing>>(emptyList())

    // Screen shake / muzzle flash highlight triggers
    private val _cameraShakeIntensity = MutableStateFlow(0f)
    val cameraShakeIntensity: StateFlow<Float> = _cameraShakeIntensity.asStateFlow()

    private val _muzzleFlashPos = MutableStateFlow<Pair<Float, Float>?>(null)
    val muzzleFlashPos: StateFlow<Pair<Float, Float>?> = _muzzleFlashPos.asStateFlow()

    private var simJob: Job? = null
    private var fxJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkAndPrepopulate()
            // Set initial selections
            operators.filter { it.isNotEmpty() }.first().let {
                _selectedOperator.value = it.firstOrNull()
            }
            weapons.filter { it.isNotEmpty() }.first().let {
                _selectedWeapon.value = it.firstOrNull()
            }
        }
        startFxTicker()
    }

    fun selectOperator(operator: OperatorEntity) {
        _selectedOperator.value = operator
    }

    fun selectWeapon(weapon: WeaponEntity) {
        _selectedWeapon.value = weapon
    }

    // Insert Operator
    fun addOperator(op: OperatorEntity) {
        viewModelScope.launch {
            repository.insertOperator(op)
            _selectedOperator.value = op
        }
    }

    // Delete Operator
    fun deleteOperator(op: OperatorEntity) {
        viewModelScope.launch {
            repository.deleteOperator(op)
            if (_selectedOperator.value?.id == op.id) {
                _selectedOperator.value = operators.value.firstOrNull { it.id != op.id }
            }
        }
    }

    // Insert Weapon
    fun addWeapon(wp: WeaponEntity) {
        viewModelScope.launch {
            repository.insertWeapon(wp)
            _selectedWeapon.value = wp
        }
    }

    // Delete Weapon
    fun deleteWeapon(wp: WeaponEntity) {
        viewModelScope.launch {
            repository.deleteWeapon(wp)
            if (_selectedWeapon.value?.id == wp.id) {
                _selectedWeapon.value = weapons.value.firstOrNull { it.id != wp.id }
            }
        }
    }

    // Clear Simulation Logs/History
    fun clearHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Change environment parameters
    fun setLocation(loc: String) {
        _simLocation.value = loc
        addLog("ENVIRONMENT UPDATED: Region set to $loc")
    }

    fun setWeather(w: String) {
        _simWeather.value = w
        addLog("ENVIRONMENT UPDATED: Atmospheric conditions set to $w")
        TacticalAudio.playAlert()
    }

    private fun addLog(message: String) {
        _simLogs.value = (listOf(message) + _simLogs.value).take(40)
    }

    // Realtime Visual Effects Ticker (60FPS loop)
    private fun startFxTicker() {
        fxJob = viewModelScope.launch {
            while (true) {
                delay(16) // ~60fps
                
                // Update particles
                particles.value = particles.value.mapNotNull { p ->
                    p.x += p.vx
                    p.y += p.vy
                    p.vx *= 0.98f
                    p.vy *= 0.98f
                    p.alpha = (1.0f - (p.life.toFloat() / p.maxLife)).coerceIn(0f, 1f)
                    p.life++
                    if (p.life < p.maxLife) p else null
                }

                // Update bullet tracers
                bulletTracers.value = bulletTracers.value.mapNotNull { b ->
                    b.life++
                    if (b.life < b.maxLife) b else null
                }

                // Update shell casings (gravity applied!)
                shellCasings.value = shellCasings.value.mapNotNull { s ->
                    s.x += s.vx
                    s.y += s.vy
                    s.vy += 0.3f // Gravity pull
                    s.vx *= 0.99f
                    s.rotation += s.rotationSpeed
                    s.life++
                    if (s.life < 40) s else null
                }

                // Camera shake decay
                if (_cameraShakeIntensity.value > 0.05f) {
                    _cameraShakeIntensity.value *= 0.85f
                } else {
                    _cameraShakeIntensity.value = 0f
                }
            }
        }
    }

    fun triggerMuzzleFlash(x: Float, y: Float) {
        _muzzleFlashPos.value = Pair(x, y)
        _cameraShakeIntensity.value = 5f
        
        // Emit bullet shell casing
        val shell = ShellCasing(
            x = x,
            y = y,
            vx = Random.nextFloat() * -3f - 1f,
            vy = Random.nextFloat() * -4f - 2f,
            rotation = Random.nextFloat() * 360f,
            rotationSpeed = Random.nextFloat() * 20f - 10f,
            alpha = 1.0f,
            life = 0
        )
        shellCasings.value = shellCasings.value + shell

        // Emit muzzle spark particles
        val newParticles = List(8) {
            CanvasParticle(
                x = x,
                y = y,
                vx = (Random.nextFloat() * 4f - 1f) * 2f,
                vy = (Random.nextFloat() * 4f - 2f) * 2f,
                color = Color(0xFFFFCC00),
                alpha = 1.0f,
                radius = Random.nextFloat() * 4f + 2f,
                life = 0,
                maxLife = Random.nextInt(15) + 10
            )
        }
        particles.value = particles.value + newParticles

        viewModelScope.launch {
            delay(50)
            _muzzleFlashPos.value = null
        }
    }

    fun spawnExplosion(x: Float, y: Float) {
        _cameraShakeIntensity.value = 18f
        TacticalAudio.playExplosion()

        // Ring of glowing red/orange particles
        val newParticles = List(25) {
            val angle = Random.nextFloat() * 2 * Math.PI
            val speed = Random.nextFloat() * 6f + 3f
            CanvasParticle(
                x = x,
                y = y,
                vx = (Math.cos(angle) * speed).toFloat(),
                vy = (Math.sin(angle) * speed).toFloat(),
                color = if (Random.nextBoolean()) Color(0xFFFF4500) else Color(0xFFFFCC00),
                alpha = 1.0f,
                radius = Random.nextFloat() * 8f + 4f,
                life = 0,
                maxLife = Random.nextInt(20) + 15
            )
        }
        particles.value = particles.value + newParticles
    }

    // Run active battle royale deployment
    fun startSimulation(activeOps: List<OperatorEntity>, activeWeapon: WeaponEntity?) {
        if (activeOps.isEmpty()) {
            addLog("CRITICAL ERROR: No squad operators selected for drop.")
            return
        }
        simJob?.cancel()
        _simRunning.value = true
        _simPlacement.value = null
        _simAliveCount.value = 100
        _simKills.value = 0
        _simDamage.value = 0
        _simLogs.value = emptyList()

        val weapon = activeWeapon ?: WeaponEntity(name = "Fists", type = "Melee", damage = 15, fireRate = 100, recoil = 5, range = 5, magCapacity = 1, scopeType = "Iron Sights", camo = "Default", description = "No weapon configured")

        addLog("SQUAD DEPLOYMENT INITIATED IN ${_simLocation.value.uppercase()}")
        addLog("CURRENT WEATHER: ${_simWeather.value.uppercase()} - VISIBILITY RESTRICTED")
        addLog("AIRBORNE DROP SEQUENCE STARTING...")

        // Spawn 10 operators on map
        val mapOps = mutableListOf<OperatorState>()
        
        // Friendly Squad
        activeOps.take(4).forEachIndexed { index, op ->
            mapOps.add(
                OperatorState(
                    id = "friendly_$index",
                    name = op.name,
                    x = 100f + Random.nextFloat() * 80f,
                    y = 100f + Random.nextFloat() * 80f,
                    targetX = 100f + Random.nextFloat() * 80f,
                    targetY = 100f + Random.nextFloat() * 80f,
                    health = 100,
                    state = "Crawling",
                    isHostile = false,
                    role = op.role,
                    color = Color(op.accentColorHex.substring(1).toLong(16) or 0xFF000000)
                )
            )
        }

        // Hostile Teams
        val hostileNames = listOf("Alpha Prime", "Wraith-1", "Voodoo Rogue", "Shadow-6", "Crimson Core", "Kestrel Squad")
        for (i in 0 until 8) {
            mapOps.add(
                OperatorState(
                    id = "hostile_$i",
                    name = hostileNames[i % hostileNames.size] + " [#" + (i+1) + "]",
                    x = 50f + Random.nextFloat() * 400f,
                    y = 50f + Random.nextFloat() * 400f,
                    targetX = 50f + Random.nextFloat() * 400f,
                    targetY = 50f + Random.nextFloat() * 400f,
                    health = 100,
                    state = "Sprinting",
                    isHostile = true,
                    role = "Assault",
                    color = Color(0xFFFF2A2A)
                )
            )
        }

        _operatorsOnMap.value = mapOps

        simJob = viewModelScope.launch {
            delay(1500)
            addLog("DEPLOYMENT CONFIRMED: TOUCHDOWN SUCCESSFUL.")
            TacticalAudio.playAlert()

            var ticks = 0
            val dropEvents = mutableListOf<String>()
            dropEvents.add("00:00 - Touched down in ${_simLocation.value}")

            while (_simRunning.value) {
                delay(800) // Fast tactical combat steps
                ticks++

                // Decline alive count randomly to simulate other players dying
                if (_simAliveCount.value > 15) {
                    val died = Random.nextInt(4) + 1
                    _simAliveCount.value = (_simAliveCount.value - died).coerceAtLeast(15)
                }

                // Update map positions & behaviors
                val currentMapOps = _operatorsOnMap.value.map { op ->
                    if (!op.isAlive) return@map op

                    // Move closer to target
                    val dx = op.targetX - op.x
                    val dy = op.targetY - op.y
                    val dist = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

                    if (dist < 15f) {
                        // Choose new tactical target destination
                        op.targetX = 50f + Random.nextFloat() * 400f
                        op.targetY = 50f + Random.nextFloat() * 400f
                        // Random state shift: sliding, sprinting, vaulting, crouching
                        val states = listOf("Sprinting", "Aiming", "Crouching", "Sliding", "Vaulting", "Crawling")
                        op.state = states[Random.nextInt(states.size)]
                    } else {
                        val step = if (op.state == "Sprinting") 30f else if (op.state == "Crouching" || op.state == "Crawling") 8f else 18f
                        op.x += (dx / dist) * step
                        op.y += (dy / dist) * step
                    }
                    op
                }

                _operatorsOnMap.value = currentMapOps

                // Search for engagements (hostiles close to friendlies)
                val aliveFriendlies = currentMapOps.filter { !it.isHostile && it.isAlive }
                val aliveHostiles = currentMapOps.filter { it.isHostile && it.isAlive }

                if (aliveFriendlies.isEmpty()) {
                    // Squad wiped! Defeat
                    val finalPlacement = _simAliveCount.value + Random.nextInt(4) + 1
                    _simPlacement.value = finalPlacement
                    _simRunning.value = false
                    addLog("TACTICAL ALERT: SQUAD ELIMINATED. PLACEMENT: #$finalPlacement")
                    TacticalAudio.playAlert()

                    saveSimulationResult(
                        placement = finalPlacement,
                        kills = _simKills.value,
                        damage = _simDamage.value,
                        accuracy = weapon.damage + 12 + Random.nextInt(15),
                        events = dropEvents
                    )
                    break
                }

                if (aliveHostiles.isEmpty()) {
                    // Victory!
                    _simPlacement.value = 1
                    _simAliveCount.value = 1
                    _simRunning.value = false
                    addLog("CRITICAL INTEL: ZONE SECURED. VICTORY CHAMPIONS!")
                    TacticalAudio.playAlert()

                    saveSimulationResult(
                        placement = 1,
                        kills = _simKills.value,
                        damage = _simDamage.value,
                        accuracy = 88,
                        events = dropEvents
                    )
                    break
                }

                // Trigger casual combat chance
                if (Random.nextFloat() < 0.6f) {
                    val shooter = aliveFriendlies.random()
                    val target = aliveHostiles.random()

                    // Play gunshot sound
                    if (weapon.type == "Sniper Rifle") {
                        TacticalAudio.playSniperShot()
                    } else {
                        TacticalAudio.playGunshot()
                    }

                    // Draw tracer trace
                    val flashX = shooter.x + (Random.nextFloat() * 10f - 5f)
                    val flashY = shooter.y + (Random.nextFloat() * 10f - 5f)
                    triggerMuzzleFlash(flashX, flashY)

                    val tracer = BulletLine(
                        x1 = flashX,
                        y1 = flashY,
                        x2 = target.x,
                        y2 = target.y,
                        life = 0,
                        maxLife = 10
                    )
                    bulletTracers.value = bulletTracers.value + tracer

                    // Handle hit math
                    val isHit = Random.nextFloat() < (shooter.health / 150f + 0.5f)
                    if (isHit) {
                        val dmg = Random.nextInt(weapon.damage / 2, weapon.damage) + 10
                        target.health -= dmg
                        _simDamage.value += dmg

                        // Emit blood/impact dust particles
                        val hitParticles = List(6) {
                            CanvasParticle(
                                x = target.x,
                                y = target.y,
                                vx = Random.nextFloat() * 3f - 1.5f,
                                vy = Random.nextFloat() * 3f - 1.5f,
                                color = Color(0xFFFF3B30),
                                alpha = 1.0f,
                                radius = Random.nextFloat() * 3f + 1f,
                                life = 0,
                                maxLife = 12
                            )
                        }
                        particles.value = particles.value + hitParticles

                        if (target.health <= 0) {
                            target.isAlive = false
                            _simKills.value += 1
                            _simAliveCount.value = (_simAliveCount.value - 1).coerceAtLeast(1)
                            addLog("${shooter.name} ELIMINATED ${target.name} WITH ${weapon.name}!")
                            dropEvents.add("${formatTime(ticks)} - ${shooter.name} killed ${target.name}")
                            spawnExplosion(target.x, target.y)
                        } else {
                            // Hit warning
                            addLog("${shooter.name} engaged ${target.name} (Hit: -$dmg HP)")
                        }
                    } else {
                        addLog("${shooter.name} missed shots due to weapon recoil.")
                        // Suppressive shell particles
                        val dirtParticles = List(4) {
                            CanvasParticle(
                                x = target.x + Random.nextInt(20) - 10f,
                                y = target.y + Random.nextInt(20) - 10f,
                                vx = Random.nextFloat() * 2f - 1f,
                                vy = Random.nextFloat() * -3f - 1f,
                                color = Color(0xFF8D6E63),
                                alpha = 0.8f,
                                radius = Random.nextFloat() * 3f + 2f,
                                life = 0,
                                maxLife = 15
                            )
                        }
                        particles.value = particles.value + dirtParticles
                    }
                }

                // Hostiles counter attack
                if (Random.nextFloat() < 0.45f) {
                    val shooter = aliveHostiles.random()
                    val target = aliveFriendlies.random()

                    TacticalAudio.playGunshot()
                    triggerMuzzleFlash(shooter.x, shooter.y)

                    val tracer = BulletLine(
                        x1 = shooter.x,
                        y1 = shooter.y,
                        x2 = target.x,
                        y2 = target.y,
                        life = 0,
                        maxLife = 10
                    )
                    bulletTracers.value = bulletTracers.value + tracer

                    val isHit = Random.nextFloat() < 0.4f
                    if (isHit) {
                        val dmg = Random.nextInt(15, 30)
                        target.health -= dmg
                        addLog("${shooter.name} hit ${target.name} (-$dmg HP)")

                        // Red flash feedback
                        val hitParticles = List(5) {
                            CanvasParticle(
                                x = target.x,
                                y = target.y,
                                vx = Random.nextFloat() * 3f - 1.5f,
                                vy = Random.nextFloat() * 3f - 1.5f,
                                color = Color(0xFFFF3B30),
                                alpha = 1.0f,
                                radius = Random.nextFloat() * 2f + 1f,
                                life = 0,
                                maxLife = 10
                            )
                        }
                        particles.value = particles.value + hitParticles

                        if (target.health <= 0) {
                            target.isAlive = false
                            _simAliveCount.value = (_simAliveCount.value - 1).coerceAtLeast(1)
                            addLog("TACTICAL ALERT: ${target.name} WAS ELIMINATED!")
                            dropEvents.add("${formatTime(ticks)} - ${target.name} was eliminated")
                            spawnExplosion(target.x, target.y)
                        }
                    } else {
                        // Tactical maneuver log
                        val maneuvers = listOf("slides behind rock cover", "vaults concrete obstacle", "crawls in tall foliage", "reloads tactical magazine")
                        val move = maneuvers[Random.nextInt(maneuvers.size)]
                        addLog("${target.name} $move under heavy fire.")
                    }
                }
            }
        }
    }

    private fun formatTime(ticks: Int): String {
        val totalSecs = ticks * 15
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private suspend fun saveSimulationResult(placement: Int, kills: Int, damage: Int, accuracy: Int, events: List<String>) {
        val jsonLog = moshiSerialize(events)
        val historyItem = SimulationHistoryEntity(
            placement = placement,
            kills = kills,
            damageDealt = damage,
            accuracyPercent = accuracy,
            weather = _simWeather.value,
            location = _simLocation.value,
            gameLogJson = jsonLog
        )
        repository.insertHistory(historyItem)
    }

    fun stopSimulation() {
        simJob?.cancel()
        _simRunning.value = false
        addLog("OPERATIONAL ALERT: Deployment aborted by Commander.")
    }

    // Simple robust local serializer for lists of events
    private fun moshiSerialize(events: List<String>): String {
        return events.joinToString(";")
    }

    fun deserializeEvents(json: String): List<String> {
        if (json.isEmpty()) return emptyList()
        return json.split(";")
    }
}
