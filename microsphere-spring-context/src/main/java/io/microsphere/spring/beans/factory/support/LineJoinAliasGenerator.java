package io.microsphere.spring.beans.factory.support;

/**
 * join by "-"
 *
 * @since 1.0.0
 */
public class LineJoinAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String jointSymbol() {
        return "-";
    }
}
