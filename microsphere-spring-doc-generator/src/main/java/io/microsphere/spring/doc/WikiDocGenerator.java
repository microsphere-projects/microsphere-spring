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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Main entry point for generating GitHub Wiki documentation from Java source files.
 * <p>
 * Scans all modules in the project, parses the Java source code, and generates
 * Markdown wiki pages — one document per public Java component.
 * </p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * java -jar microsphere-spring-doc-generator.jar <projectRoot> <outputDir>
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class WikiDocGenerator {

    /**
     * List of modules to scan for documentation generation
     */
    static final List<String> MODULES = List.of(
            "microsphere-spring-context",
            "microsphere-spring-web",
            "microsphere-spring-webmvc",
            "microsphere-spring-webflux",
            "microsphere-spring-jdbc",
            "microsphere-spring-guice",
            "microsphere-spring-test",
            "microsphere-spring-compatible"
    );

    private final JavaSourceParser parser;

    private final MarkdownRenderer renderer;

    public WikiDocGenerator() {
        this(new JavaSourceParser(), new MarkdownRenderer());
    }

    public WikiDocGenerator(JavaSourceParser parser, MarkdownRenderer renderer) {
        this.parser = parser;
        this.renderer = renderer;
    }

    /**
     * Generate wiki documentation for all modules in the project
     *
     * @param projectRoot the root directory of the project
     * @param outputDir   the directory where wiki pages will be written
     * @return the list of generated wiki page paths
     * @throws IOException if files cannot be read or written
     */
    public List<Path> generate(Path projectRoot, Path outputDir) throws IOException {
        Files.createDirectories(outputDir);

        List<JavaComponentInfo> allComponents = new ArrayList<>();

        for (String moduleName : MODULES) {
            Path moduleDir = projectRoot.resolve(moduleName);
            if (!Files.isDirectory(moduleDir)) {
                System.out.println("Skipping module (not found): " + moduleName);
                continue;
            }

            List<Path> sourceFiles = parser.findJavaSourceFiles(moduleDir);
            System.out.println("Module " + moduleName + ": found " + sourceFiles.size() + " source files");

            for (Path sourceFile : sourceFiles) {
                try {
                    List<JavaComponentInfo> components = parser.parse(sourceFile, moduleName);
                    allComponents.addAll(components);
                } catch (Exception e) {
                    System.err.println("Warning: Failed to parse " + sourceFile + ": " + e.getMessage());
                }
            }
        }

        System.out.println("\nTotal components found: " + allComponents.size());

        // Generate wiki pages
        List<Path> generatedPages = new ArrayList<>();

        for (JavaComponentInfo component : allComponents) {
            String markdown = renderer.render(component);
            String pageName = renderer.generatePageName(component);
            Path pagePath = outputDir.resolve(pageName + ".md");

            Files.writeString(pagePath, markdown);
            generatedPages.add(pagePath);
        }

        // Generate index page (Home.md)
        Path indexPage = generateIndexPage(outputDir, allComponents);
        generatedPages.add(indexPage);

        // Generate sidebar navigation (_Sidebar.md)
        Path sidebarPage = generateSidebarPage(outputDir, allComponents);
        generatedPages.add(sidebarPage);

        System.out.println("Generated " + generatedPages.size() + " wiki pages in: " + outputDir);
        return generatedPages;
    }

    /**
     * Generate the index (Home) page for the wiki
     */
    Path generateIndexPage(Path outputDir, List<JavaComponentInfo> components) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("# Microsphere Spring - API Documentation\n\n");
        md.append("Welcome to the Microsphere Spring project wiki. ");
        md.append("This documentation is auto-generated from the project source code.\n\n");

        md.append("## Modules\n\n");

        // Group components by module
        Map<String, List<JavaComponentInfo>> byModule = new TreeMap<>();
        for (JavaComponentInfo comp : components) {
            byModule.computeIfAbsent(comp.getModuleName(), k -> new ArrayList<>()).add(comp);
        }

        for (Map.Entry<String, List<JavaComponentInfo>> entry : byModule.entrySet()) {
            String moduleName = entry.getKey();
            List<JavaComponentInfo> moduleComponents = entry.getValue();
            moduleComponents.sort(Comparator.comparing(JavaComponentInfo::getQualifiedName));

            md.append("### ").append(moduleName).append("\n\n");
            md.append("| Component | Type | Since | Description |\n");
            md.append("|-----------|------|-------|-------------|\n");

            for (JavaComponentInfo comp : moduleComponents) {
                String pageName = renderer.generatePageName(comp);
                String type = formatComponentType(comp.getComponentType());
                String since = comp.getSinceVersion() != null ? comp.getSinceVersion() : "-";
                String desc = comp.getDescription() != null ? truncate(comp.getDescription(), 80) : "-";
                // Remove newlines and pipe chars that would break the table
                desc = desc.replace("\n", " ").replace("|", "\\|");

                md.append("| [").append(comp.getSimpleName()).append("](").append(pageName).append(") ");
                md.append("| ").append(type).append(" ");
                md.append("| ").append(since).append(" ");
                md.append("| ").append(desc).append(" |\n");
            }

            md.append("\n");
        }

        md.append("## Version Compatibility\n\n");
        md.append("| Branch | Spring Framework       | Java Version |\n");
        md.append("|--------|------------------------|--------------|\n");
        md.append("| 0.2.x  | 6.0, 6.1, 6.2, 7.0    | 17+          |\n");
        md.append("| 0.1.x  | 4.3.x - 5.3.x         | 8+           |\n");
        md.append("\n");

        md.append("---\n\n");
        md.append("*This documentation was auto-generated from the project source code.*\n");

        Path indexPath = outputDir.resolve("Home.md");
        Files.writeString(indexPath, md.toString());
        return indexPath;
    }

    /**
     * Generate the sidebar navigation page
     */
    Path generateSidebarPage(Path outputDir, List<JavaComponentInfo> components) throws IOException {
        StringBuilder md = new StringBuilder();
        md.append("## Navigation\n\n");
        md.append("- [[Home]]\n\n");

        // Group by module
        Map<String, List<JavaComponentInfo>> byModule = new TreeMap<>();
        for (JavaComponentInfo comp : components) {
            byModule.computeIfAbsent(comp.getModuleName(), k -> new ArrayList<>()).add(comp);
        }

        for (Map.Entry<String, List<JavaComponentInfo>> entry : byModule.entrySet()) {
            String moduleName = entry.getKey();
            List<JavaComponentInfo> moduleComponents = entry.getValue();
            moduleComponents.sort(Comparator.comparing(JavaComponentInfo::getSimpleName));

            md.append("### ").append(moduleName).append("\n\n");
            for (JavaComponentInfo comp : moduleComponents) {
                String pageName = renderer.generatePageName(comp);
                md.append("- [[").append(comp.getSimpleName()).append("|").append(pageName).append("]]\n");
            }
            md.append("\n");
        }

        Path sidebarPath = outputDir.resolve("_Sidebar.md");
        Files.writeString(sidebarPath, md.toString());
        return sidebarPath;
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

    private String truncate(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        // Take first line only
        int newline = text.indexOf('\n');
        String firstLine = newline > 0 ? text.substring(0, newline) : text;
        if (firstLine.length() <= maxLength) {
            return firstLine;
        }
        return firstLine.substring(0, maxLength - 3) + "...";
    }

    /**
     * Main entry point
     *
     * @param args [0] = project root path, [1] = output directory for wiki pages
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: WikiDocGenerator <projectRoot> <outputDir>");
            System.exit(1);
        }

        Path projectRoot = Paths.get(args[0]).toAbsolutePath();
        Path outputDir = Paths.get(args[1]).toAbsolutePath();

        if (!Files.isDirectory(projectRoot)) {
            System.err.println("Error: Project root is not a directory: " + projectRoot);
            System.exit(1);
        }

        System.out.println("Generating wiki documentation...");
        System.out.println("Project root: " + projectRoot);
        System.out.println("Output directory: " + outputDir);
        System.out.println();

        WikiDocGenerator generator = new WikiDocGenerator();
        List<Path> pages = generator.generate(projectRoot, outputDir);

        System.out.println("\nDone! Generated " + pages.size() + " pages.");
    }
}
