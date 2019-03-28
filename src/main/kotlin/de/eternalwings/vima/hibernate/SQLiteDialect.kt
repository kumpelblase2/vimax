package de.eternalwings.vima.hibernate

import org.hibernate.dialect.Dialect
import org.hibernate.dialect.identity.IdentityColumnSupport
import org.hibernate.dialect.unique.UniqueDelegate
import java.sql.Types

class SQLiteDialect : Dialect() {
    init {
        registerColumnType(Types.BIT, "integer")
        registerColumnType(Types.TINYINT, "tinyint")
        registerColumnType(Types.SMALLINT, "smallint")
        registerColumnType(Types.INTEGER, "integer")
        registerColumnType(Types.BIGINT, "bigint")
        registerColumnType(Types.FLOAT, "float")
        registerColumnType(Types.REAL, "real")
        registerColumnType(Types.DOUBLE, "double")
        registerColumnType(Types.NUMERIC, "numeric")
        registerColumnType(Types.DECIMAL, "decimal")
        registerColumnType(Types.CHAR, "char")
        registerColumnType(Types.VARCHAR, "varchar")
        registerColumnType(Types.LONGVARCHAR, "longvarchar")
        registerColumnType(Types.DATE, "date")
        registerColumnType(Types.TIME, "time")
        registerColumnType(Types.TIMESTAMP, "timestamp")
        registerColumnType(Types.BINARY, "blob")
        registerColumnType(Types.VARBINARY, "blob")
        registerColumnType(Types.LONGVARBINARY, "blob")
        registerColumnType(Types.BLOB, "blob")
        registerColumnType(Types.CLOB, "clob")
        registerColumnType(Types.BOOLEAN, "integer")
    }

    override fun getIdentityColumnSupport() = SQLiteIdentityColumnSupport()

    override fun hasAlterTable() = false

    override fun dropConstraints() = false

    override fun getDropForeignKeyString() = ""

    override fun getAddForeignKeyConstraintString(constraintName: String?, foreignKey: Array<String>,
                                                  referencedTable: String?, primaryKey: Array<String>,
                                                  referencesPrimaryKey: Boolean) = ""

    override fun getAddPrimaryKeyConstraintString(constraintName: String?) = ""

    override fun getForUpdateString() = ""

    override fun getAddColumnString() = "add column"

    override fun supportsOuterJoinForUpdate() = false

    override fun supportsIfExistsBeforeTableName() = true

    override fun supportsCascadeDelete() = false

    override fun getUniqueDelegate() = SQLiteUniqueDelegate(this)
}
