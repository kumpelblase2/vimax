package de.eternalwings.vima;

import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.dialect.function.VarArgsSQLFunction;
import org.hibernate.type.StandardBasicTypes;

public class CustomHSQLDialect extends HSQLDialect {

    public CustomHSQLDialect() {
        super();
        registerFunction("position_array", new VarArgsSQLFunction(StandardBasicTypes.INTEGER, "position_array(", " IN ", ")"));
        registerFunction("cardinality", new StandardSQLFunction("cardinality", StandardBasicTypes.INTEGER));
    }
}
