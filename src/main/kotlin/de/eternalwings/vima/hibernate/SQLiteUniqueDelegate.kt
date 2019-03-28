package de.eternalwings.vima.hibernate

import org.hibernate.boot.Metadata
import org.hibernate.dialect.unique.DefaultUniqueDelegate
import org.hibernate.mapping.UniqueKey

class SQLiteUniqueDelegate(dialect: SQLiteDialect) : DefaultUniqueDelegate(dialect) {

    override fun getAlterTableToAddUniqueKeyCommand(uniqueKey: UniqueKey?, metadata: Metadata?): String {
        return ""
    }

    override fun getAlterTableToDropUniqueKeyCommand(uniqueKey: UniqueKey?, metadata: Metadata?): String {
        return ""
    }

}
