package de.eternalwings.vima.sqlite

import com.fasterxml.jackson.databind.ObjectMapper
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

class SQLiteArrayUserType : UserType {

    val objectMapper: ObjectMapper = ObjectMapper()

    override fun hashCode(x: Any) = x.hashCode()

    override fun equals(x: Any?, y: Any?): Boolean {
        return x == y
    }

    override fun deepCopy(value: Any?): Any? {
        return value
    }

    override fun replace(original: Any?, target: Any?, owner: Any): Any? {
        return original
    }

    override fun returnedClass() = List::class.java

    override fun assemble(cached: Serializable?, owner: Any): Any? {
        return cached
    }

    override fun disassemble(value: Any?): Serializable? {
        return value as? Serializable?
    }

    override fun nullSafeSet(st: PreparedStatement, value: Any?, index: Int,
                             session: SharedSessionContractImplementor) {
        val toSet: List<*>
        if(value != null) {
            toSet = value as List<*>
        } else {
            toSet = emptyList<Any>()
        }

        val jsonString = objectMapper.writeValueAsString(toSet)
        st.setString(index, jsonString)
    }

    override fun nullSafeGet(rs: ResultSet, names: Array<out String>,
                             session: SharedSessionContractImplementor, owner: Any): Any {
        val stringValue = rs.getString(names[0]) ?: return emptyList<Any>()
        return objectMapper.readValue(stringValue, List::class.java)
    }

    override fun isMutable() = true

    override fun sqlTypes() = intArrayOf(Types.VARCHAR)

}
