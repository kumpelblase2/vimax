package de.eternalwings.vima.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import org.springframework.data.domain.Persistable
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.MappedSuperclass
import javax.persistence.PrePersist
import javax.persistence.PreUpdate
import javax.persistence.Version

@MappedSuperclass
abstract class BasePersistable<T : Serializable> : Persistable<T> {
    @Id @GeneratedValue
    private var id: T? = null

    var creationTime: LocalDateTime? = null
    var updateTime: LocalDateTime? = null
    @Version
    var version: Int? = null

    override fun getId(): T? = id

    @JsonIgnore
    override fun isNew() = id != null

    @PrePersist
    fun updateCreationDate() {
        creationTime = LocalDateTime.now()
        updateTime = LocalDateTime.now()
    }

    @PreUpdate
    fun updateUpdateDate() {
        updateTime = LocalDateTime.now()
    }
}
