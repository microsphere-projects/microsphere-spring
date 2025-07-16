package io.microsphere.spring.beans.factory.support;

/**
 * A class that generates aliases by joining parts with an underscore ("_").
 *
 * <p> This class extends the {@link JoinAliasGenerator} and uses the underscore character as the
 * separator between parts when generating bean aliases. It is useful in scenarios where bean aliases
 * need to follow a specific naming convention, such as using underscores to separate logical parts of
 * the alias name.
 *
 * <p> <b>Example:</b> If the bean name parts are "userService" and "v1", the generated alias will be
 * "userService_v1".
 *
 * @author <a href="mailto:15868175516@163.com">qi.li</a>
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class UnderScoreJoinAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "_";
    }
}
