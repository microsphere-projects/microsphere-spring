/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.doc;

import java.util.List;

/**
 * Renders {@link JavaComponentInfo} as Markdown suitable for GitHub Wiki pages.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class MarkdownRenderer {

    private static final String VERSION_COMPATIBILITY_TABLE =
            "| Branch | Spring Framework       | Java Version |\n" +
            "|--------|------------------------|--------------|\n" +
            "| 0.2.x  | 6.0, 6.1, 6.2, 7.0    | 17+          |\n" +
            "| 0.1.x  | 4.3.x - 5.3.x         | 8+           |\n";

    private static final String SPRING_PROFILES_TABLE =
            "| Profile              | Spring Version | Reactor Version |\n" +
            "|----------------------|----------------|-----------------|\n" +
            "| spring-framework-6.0 | 6.0.23         | 2022.0.22       |\n" +
            "| spring-framework-6.1 | 6.1.21         | 2023.0.19       |\n" +
            "| spring-framework-6.2 | 6.2.17         | 2024.0.16       |\n" +
            "| spring-framework-7.0 | 7.0.6          | 2025.0.4        |\n";

    /**
     * Render a single component as a Markdown wiki page
     *
     * @param info the component information
     * @return the Markdown content
     */
    public String render(JavaComponentInfo info) {
        StringBuilder md = new StringBuilder();

        renderTitle(md, info);
        renderOverview(md, info);
        renderDescription(md, info);
        renderCodeExamples(md, info);
        renderPublicAPI(md, info);
        renderVersionCompatibility(md, info);
        renderSeeAlso(md, info);

        return md.toString();
    }

    private void renderTitle(StringBuilder md, JavaComponentInfo info) {
        md.append("# ").append(info.getSimpleName()).append("\n\n");
    }

    private void renderOverview(StringBuilder md, JavaComponentInfo info) {
        md.append("## Overview\n\n");

        md.append("| Property | Value |\n");
        md.append("|----------|-------|\n");
        md.append("| **Module** | `").append(info.getModuleName()).append("` |\n");
        md.append("| **Package** | `").append(info.getPackageName()).append("` |\n");
        md.append("| **Type** | ").append(formatComponentType(info.getComponentType())).append(" |\n");
        md.append("| **Fully Qualified Name** | `").append(info.getQualifiedName()).append("` |\n");

        if (info.getSinceVersion() != null && !info.getSinceVersion().isEmpty()) {
            md.append("| **Since** | `").append(info.getSinceVersion()).append("` |\n");
        }

        if (info.getAuthor() != null && !info.getAuthor().isEmpty()) {
            md.append("| **Author** | ").append(info.getAuthor()).append(" |\n");
        }

        md.append("\n");

        // Type hierarchy
        if (info.getSuperClass() != null && !info.getSuperClass().isEmpty()) {
            md.append("**Extends:** `").append(info.getSuperClass()).append("`\n\n");
        }

        if (!info.getImplementedInterfaces().isEmpty()) {
            md.append("**Implements:** ");
            md.append(String.join(", ", info.getImplementedInterfaces().stream()
                    .map(i -> "`" + i + "`")
                    .toList()));
            md.append("\n\n");
        }

        if (!info.getTypeParameters().isEmpty()) {
            md.append("**Type Parameters:** ");
            md.append(String.join(", ", info.getTypeParameters().stream()
                    .map(tp -> "`<" + tp + ">`")
                    .toList()));
            md.append("\n\n");
        }
    }

    private void renderDescription(StringBuilder md, JavaComponentInfo info) {
        if (info.getDescription() == null || info.getDescription().isEmpty()) {
            return;
        }

        md.append("## Description\n\n");

        // Clean up the description: remove HTML tags that don't render well in Markdown
        String description = cleanDescription(info.getDescription());
        md.append(description).append("\n\n");
    }

    private void renderCodeExamples(StringBuilder md, JavaComponentInfo info) {
        List<String> examples = info.getCodeExamples();

        md.append("## Code Examples\n\n");

        if (examples.isEmpty()) {
            // Generate a basic usage example based on component type
            md.append("```java\n");
            md.append(generateBasicExample(info));
            md.append("```\n\n");
        } else {
            for (int i = 0; i < examples.size(); i++) {
                if (examples.size() > 1) {
                    md.append("### Example ").append(i + 1).append("\n\n");
                }
                md.append("```java\n");
                md.append(examples.get(i)).append("\n");
                md.append("```\n\n");
            }
        }
    }

    private void renderPublicAPI(StringBuilder md, JavaComponentInfo info) {
        List<JavaComponentInfo.MethodInfo> methods = info.getPublicMethods();
        if (methods.isEmpty()) {
            return;
        }

        md.append("## Public API\n\n");

        for (JavaComponentInfo.MethodInfo method : methods) {
            md.append("### `").append(method.getSignature()).append("`\n\n");

            if (method.getDescription() != null && !method.getDescription().isEmpty()) {
                md.append(cleanDescription(method.getDescription())).append("\n\n");
            }

            if (method.getSinceVersion() != null && !method.getSinceVersion().isEmpty()) {
                md.append("_Since: ").append(method.getSinceVersion()).append("_\n\n");
            }
        }
    }

    private void renderVersionCompatibility(StringBuilder md, JavaComponentInfo info) {
        md.append("## Version Compatibility\n\n");

        if (info.getSinceVersion() != null && !info.getSinceVersion().isEmpty()) {
            md.append("This component has been available since version **")
                    .append(info.getSinceVersion()).append("**.\n\n");
        }

        md.append("### Branch Compatibility\n\n");
        md.append(VERSION_COMPATIBILITY_TABLE);
        md.append("\n");

        md.append("### Tested Spring Framework Profiles\n\n");
        md.append(SPRING_PROFILES_TABLE);
        md.append("\n");
    }

    private void renderSeeAlso(StringBuilder md, JavaComponentInfo info) {
        if (info.getSeeReferences().isEmpty()) {
            return;
        }

        md.append("## See Also\n\n");
        for (String ref : info.getSeeReferences()) {
            md.append("- `").append(ref).append("`\n");
        }
        md.append("\n");
    }

    private String formatComponentType(JavaComponentInfo.ComponentType type) {
        if (type == null) {
            return "Unknown";
        }
        return switch (type) {
            case CLASS -> "Class";
            case ABSTRACT_CLASS -> "Abstract Class";
            case INTERFACE -> "Interface";
            case ANNOTATION -> "Annotation";
            case ENUM -> "Enum";
            case RECORD -> "Record";
        };
    }

    /**
     * Clean up description text for Markdown rendering
     */
    String cleanDescription(String description) {
        if (description == null) {
            return "";
        }
        // Convert common HTML to Markdown
        String cleaned = description;
        cleaned = cleaned.replaceAll("<p>\\s*", "\n\n");
        cleaned = cleaned.replaceAll("</p>", "");
        cleaned = cleaned.replaceAll("<h3>(.*?)</h3>", "### $1");
        cleaned = cleaned.replaceAll("<h4>(.*?)</h4>", "#### $1");
        cleaned = cleaned.replaceAll("<code>(.*?)</code>", "`$1`");
        cleaned = cleaned.replaceAll("<b>(.*?)</b>", "**$1**");
        cleaned = cleaned.replaceAll("<i>(.*?)</i>", "*$1*");
        cleaned = cleaned.replaceAll("<ul>", "");
        cleaned = cleaned.replaceAll("</ul>", "");
        cleaned = cleaned.replaceAll("<li>(.*?)</li>", "- $1");
        cleaned = cleaned.replaceAll("<br\\s*/?>", "\n");
        // Remove <pre>{@code ...}</pre> blocks (already in examples section)
        cleaned = cleaned.replaceAll("<pre>\\s*(?:\\{@code\\s*)?.*?(?:\\s*})?\\s*</pre>", "_(See Code Examples section)_");
        // Convert {@link ...} to code
        cleaned = cleaned.replaceAll("\\{@link\\s+(.*?)}", "`$1`");
        cleaned = cleaned.replaceAll("\\{@code\\s+(.*?)}", "`$1`");
        // Remove remaining HTML tags
        cleaned = cleaned.replaceAll("<[^>]+>", "");
        return cleaned.trim();
    }

    /**
     * Generate a basic usage example based on component type
     */
    String generateBasicExample(JavaComponentInfo info) {
        StringBuilder example = new StringBuilder();
        String simpleName = info.getSimpleName();

        switch (info.getComponentType()) {
            case INTERFACE:
                example.append("// Implement the ").append(simpleName).append(" interface\n");
                example.append("public class My").append(simpleName).append(" implements ").append(simpleName);
                if (!info.getTypeParameters().isEmpty()) {
                    example.append("<").append(String.join(", ", info.getTypeParameters())).append(">");
                }
                example.append(" {\n");
                example.append("    // TODO: Implement required methods\n");
                example.append("}\n");
                break;
            case ANNOTATION:
                example.append("// Apply the @").append(simpleName).append(" annotation\n");
                example.append("@").append(simpleName).append("\n");
                example.append("public class MyClass {\n");
                example.append("    // ...\n");
                example.append("}\n");
                break;
            case ABSTRACT_CLASS:
                example.append("// Extend the ").append(simpleName).append(" abstract class\n");
                example.append("public class My").append(simpleName).append(" extends ").append(simpleName);
                if (!info.getTypeParameters().isEmpty()) {
                    example.append("<").append(String.join(", ", info.getTypeParameters())).append(">");
                }
                example.append(" {\n");
                example.append("    // TODO: Implement abstract methods\n");
                example.append("}\n");
                break;
            case ENUM:
                example.append("// Use the ").append(simpleName).append(" enum\n");
                example.append(simpleName).append(" value = ").append(simpleName).append(".values()[0];\n");
                break;
            case RECORD:
                example.append("// Create an instance of the ").append(simpleName).append(" record\n");
                example.append(simpleName).append(" instance = new ").append(simpleName).append("(/* parameters */);\n");
                break;
            default:
                example.append("// Create and use ").append(simpleName).append("\n");
                example.append(simpleName).append(" instance = new ").append(simpleName).append("();\n");
                break;
        }

        return example.toString();
    }

    /**
     * Generate a wiki-safe filename for a component
     *
     * @param info the component information
     * @return a safe filename (without extension)
     */
    public String generatePageName(JavaComponentInfo info) {
        // Use qualified name with dots replaced by hyphens for uniqueness
        return info.getQualifiedName().replace('.', '-');
    }
}
