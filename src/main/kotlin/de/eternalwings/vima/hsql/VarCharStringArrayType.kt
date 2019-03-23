package de.eternalwings.vima.hsql

import com.vladmihalcea.hibernate.type.array.internal.ArraySqlTypeDescriptor
import com.vladmihalcea.hibernate.type.array.internal.StringArrayTypeDescriptor
import org.hibernate.type.AbstractSingleColumnStandardBasicType
import org.hibernate.usertype.DynamicParameterizedType
import java.util.Properties

class VarCharStringArrayType : AbstractSingleColumnStandardBasicType<Array<String>>(
        ArraySqlTypeDescriptor.INSTANCE, VarCharStringArrayTypeDescriptor.INSTANCE),
        DynamicParameterizedType {
    override fun getName(): String {
        return "varchar-array"
    }

    override fun registerUnderJavaType() = true

    override fun setParameterValues(parameters: Properties?) {
        (javaTypeDescriptor as VarCharStringArrayTypeDescriptor).setParameterValues(parameters)
    }

    companion object {
        val INSTANCE = VarCharStringArrayType()
    }
}

class VarCharStringArrayTypeDescriptor : StringArrayTypeDescriptor() {
    override fun getSqlArrayType() = "varchar"

    companion object {
        val INSTANCE = VarCharStringArrayTypeDescriptor()
    }
}
