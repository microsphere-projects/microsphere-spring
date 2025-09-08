package io.microsphere.spring.beans.factory.support;

/**
 * {@link HyphenAliasGenerator} is an implementation of {@link JoinAliasGenerator}
 * that uses a hyphen ("-") as the separator between bean name parts.
 *
 * <p>
 * This class is typically used in Spring's bean alias generation process to create aliases
 * by joining parts of bean names with a hyphen. For example, a bean named "userService"
 * might be aliased as "user-service" if it's split into parts and joined using this generator.
 * </p>
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Given a bean name "myCustomService"
 * String[] parts = {"my", "custom", "service"};
 * String alias = new HyphenAliasGenerator().generate(parts);
 * System.out.println(alias); // Output: "my-custom-service"
 * }</pre>
 *
 * @author <a href="mailto:15868175516@163.com">qi.li</a>
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class HyphenAliasGenerator extends JoinAliasGenerator {

    @Override
    protected String delimiter() {
        return "-";
    }
}
