package de.eternalwings.vima.hsql

import org.hibernate.HibernateException
import org.hibernate.engine.spi.SharedSessionContractImplementor
import org.hibernate.usertype.UserType
import java.io.Serializable
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Types

class StringArrayType : UserType {
    private val arrayTypes = intArrayOf(Types.ARRAY)

    override fun sqlTypes() = arrayTypes
    override fun returnedClass() = Array<String>::class.java

    @Throws(HibernateException::class)
    override fun equals(x: Any?, y: Any?): Boolean {
        return if (x == null) y == null else x == y
    }

    @Throws(HibernateException::class)
    override fun hashCode(x: Any?): Int {
        return x?.hashCode() ?: 0
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeGet(rs: ResultSet?, names: Array<out String>?,
                             session: SharedSessionContractImplementor, owner: Any?)
            : Any? {
        // get the first column names
        if (names != null && names.isNotEmpty() && rs != null && rs.getArray(names[0]) != null) {
            val results = rs.getArray(names[0]).array as Array<Object>
            return (results.toList() as List<String>).toTypedArray()
        }
        return null
    }

    @Throws(HibernateException::class, SQLException::class)
    override fun nullSafeSet(st: PreparedStatement?, value: Any?, index: Int,
                             session: SharedSessionContractImplementor) {
        // setting the column with string array
        if (value != null && st != null) {
            val castObject = value as Array<String>?
            val array = session.connection().createArrayOf("varchar", castObject)
            st.setArray(index, array)
        } else {
            st!!.setNull(index, arrayTypes[0])
        }
    }

    @Throws(HibernateException::class)
    override fun deepCopy(value: Any?): Any? {
        return if (value == null) null else (value as Array<String>).clone()
    }

    override fun isMutable(): Boolean {
        return false
    }

    @Throws(HibernateException::class)
    override fun disassemble(value: Any): Serializable {
        return value as Serializable
    }

    @Throws(HibernateException::class)
    override fun assemble(cached: Serializable, owner: Any): Any {
        return cached
    }

    @Throws(HibernateException::class)
    override fun replace(original: Any, target: Any, owner: Any): Any {
        return original
    }
}
