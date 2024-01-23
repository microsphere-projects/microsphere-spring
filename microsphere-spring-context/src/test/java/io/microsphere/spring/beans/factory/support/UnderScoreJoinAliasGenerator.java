package io.microsphere.spring.beans.factory.support;

/**
 * join by "_"
 *
 * @since 1.0.0
 */
public class UnderScoreJoinAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String jointSymbol() {
        return "_";
    }
}
