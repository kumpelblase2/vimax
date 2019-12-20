package de.eternalwings.vima.repository

import de.eternalwings.vima.domain.PluginInformation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface PluginInformationRepository : JpaRepository<PluginInformation,Int> {
    fun findByName(name: String): PluginInformation?

    @Modifying
    @Query("UPDATE PluginInformation p SET p.enabled = ?2, p.enabledAt = ?3, p.disabledAt = ?4 WHERE p.name = ?1")
    fun updateEnabledState(name: String, enabled: Boolean, enabledAt: LocalDateTime?, disabledAt: LocalDateTime?)
}
