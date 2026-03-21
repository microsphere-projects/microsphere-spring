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

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.nodeTypes.NodeWithJavadoc;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import com.github.javaparser.javadoc.description.JavadocDescription;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Parses Java source files using JavaParser and extracts component information.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JavaSourceParser {

    private static final Pattern PRE_CODE_PATTERN = Pattern.compile(
            "<pre>\\s*(?:\\{@code\\s*)?(.*?)(?:\\s*})?\\s*</pre>",
            Pattern.DOTALL
    );

    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile(
            "\\{@code\\s+(.*?)\\s*}",
            Pattern.DOTALL
    );

    /**
     * Pattern for extracting code blocks from raw Javadoc comment text.
     * This handles cases where JavaParser's Javadoc parser truncates {@code} blocks
     * containing curly braces.
     */
    private static final Pattern RAW_PRE_CODE_PATTERN = Pattern.compile(
            "<pre>\\{@code\\s*\\n(.*?)\\n\\s*\\}</pre>",
            Pattern.DOTALL
    );

    public JavaSourceParser() {
        ParserConfiguration config = new ParserConfiguration();
        config.setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);
        StaticJavaParser.setConfiguration(config);
    }

    /**
     * Parse a Java source file and extract component information
     *
     * @param sourceFile the path to the Java source file
     * @param moduleName the name of the module this file belongs to
     * @return a list of component info objects (one per top-level type declaration)
     * @throws IOException if the file cannot be read
     */
    public List<JavaComponentInfo> parse(Path sourceFile, String moduleName) throws IOException {
        List<JavaComponentInfo> components = new ArrayList<>();

        CompilationUnit cu = StaticJavaParser.parse(sourceFile);
        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        for (TypeDeclaration<?> type : cu.getTypes()) {
            if (!type.isPublic()) {
                continue;
            }

            JavaComponentInfo info = new JavaComponentInfo();
            info.setModuleName(moduleName);
            info.setPackageName(packageName);
            info.setSimpleName(type.getNameAsString());
            info.setQualifiedName(packageName.isEmpty() ? type.getNameAsString() : packageName + "." + type.getNameAsString());

            extractComponentType(type, info);
            extractJavadocInfo(type, info);
            extractMethodInfo(type, info);

            components.add(info);
        }

        return components;
    }

    private void extractComponentType(TypeDeclaration<?> type, JavaComponentInfo info) {
        if (type instanceof AnnotationDeclaration) {
            info.setComponentType(JavaComponentInfo.ComponentType.ANNOTATION);
        } else if (type instanceof EnumDeclaration) {
            info.setComponentType(JavaComponentInfo.ComponentType.ENUM);
        } else if (type instanceof RecordDeclaration) {
            info.setComponentType(JavaComponentInfo.ComponentType.RECORD);
        } else if (type instanceof ClassOrInterfaceDeclaration cid) {
            if (cid.isInterface()) {
                info.setComponentType(JavaComponentInfo.ComponentType.INTERFACE);
            } else if (cid.isAbstract()) {
                info.setComponentType(JavaComponentInfo.ComponentType.ABSTRACT_CLASS);
            } else {
                info.setComponentType(JavaComponentInfo.ComponentType.CLASS);
            }

            // Extract implemented interfaces
            List<String> interfaces = cid.getImplementedTypes().stream()
                    .map(t -> t.asString())
                    .collect(Collectors.toList());
            info.setImplementedInterfaces(interfaces);

            // Extract super class
            cid.getExtendedTypes().stream()
                    .findFirst()
                    .ifPresent(t -> info.setSuperClass(t.asString()));

            // Extract type parameters
            List<String> typeParams = cid.getTypeParameters().stream()
                    .map(tp -> tp.asString())
                    .collect(Collectors.toList());
            info.setTypeParameters(typeParams);
        }
    }

    private void extractJavadocInfo(TypeDeclaration<?> type, JavaComponentInfo info) {
        Optional<Javadoc> javadocOpt = ((NodeWithJavadoc<?>) type).getJavadoc();
        if (javadocOpt.isEmpty()) {
            return;
        }

        Javadoc javadoc = javadocOpt.get();

        // Extract description
        JavadocDescription description = javadoc.getDescription();
        String descText = description.toText().trim();
        info.setDescription(descText);

        // Extract code examples from description first
        List<String> examples = extractCodeExamples(descText);

        // If no examples found from parsed description, try raw comment text
        // (JavaParser's Javadoc parser may truncate {@code} blocks containing curly braces)
        if (examples.isEmpty()) {
            Optional<Comment> rawComment = type.getComment();
            if (rawComment.isPresent()) {
                String rawText = rawComment.get().getContent();
                examples = extractCodeExamplesFromRaw(rawText);
            }
        }
        info.setCodeExamples(examples);

        // Extract block tags
        for (JavadocBlockTag tag : javadoc.getBlockTags()) {
            switch (tag.getTagName()) {
                case "since":
                    info.setSinceVersion(tag.getContent().toText().trim());
                    break;
                case "author":
                    info.setAuthor(tag.getContent().toText().trim());
                    break;
                case "see":
                    info.getSeeReferences().add(tag.getContent().toText().trim());
                    break;
                default:
                    break;
            }
        }
    }

    private void extractMethodInfo(TypeDeclaration<?> type, JavaComponentInfo info) {
        List<JavaComponentInfo.MethodInfo> methods = new ArrayList<>();

        boolean isInterface = type instanceof ClassOrInterfaceDeclaration cid && cid.isInterface();

        for (MethodDeclaration method : type.getMethods()) {
            // Interface methods are implicitly public (unless they are private)
            boolean isPublic = method.hasModifier(Modifier.Keyword.PUBLIC)
                    || (isInterface && !method.hasModifier(Modifier.Keyword.PRIVATE));

            if (!isPublic) {
                continue;
            }

            JavaComponentInfo.MethodInfo methodInfo = new JavaComponentInfo.MethodInfo();
            methodInfo.setName(method.getNameAsString());
            methodInfo.setReturnType(method.getTypeAsString());

            List<String> params = method.getParameters().stream()
                    .map(p -> p.getTypeAsString() + " " + p.getNameAsString())
                    .collect(Collectors.toList());
            methodInfo.setParameters(params);

            // Extract method Javadoc
            method.getJavadoc().ifPresent(javadoc -> {
                methodInfo.setDescription(javadoc.getDescription().toText().trim());
                for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                    if ("since".equals(tag.getTagName())) {
                        methodInfo.setSinceVersion(tag.getContent().toText().trim());
                    }
                }
            });

            methods.add(methodInfo);
        }

        info.setPublicMethods(methods);
    }

    /**
     * Extract code examples from Javadoc description text.
     * Looks for {@code <pre>} blocks and {@code @code} blocks.
     *
     * @param descriptionText the Javadoc description text
     * @return list of extracted code examples
     */
    List<String> extractCodeExamples(String descriptionText) {
        List<String> examples = new ArrayList<>();

        // Match <pre>{@code ...}</pre> blocks
        Matcher preMatcher = PRE_CODE_PATTERN.matcher(descriptionText);
        while (preMatcher.find()) {
            String code = preMatcher.group(1).trim();
            if (!code.isEmpty()) {
                examples.add(code);
            }
        }

        // If no <pre> blocks found, look for standalone {@code ...} blocks (multiline)
        if (examples.isEmpty()) {
            Matcher codeMatcher = CODE_BLOCK_PATTERN.matcher(descriptionText);
            while (codeMatcher.find()) {
                String code = codeMatcher.group(1).trim();
                if (!code.isEmpty() && code.contains("\n")) {
                    examples.add(code);
                }
            }
        }

        return examples;
    }

    /**
     * Extract code examples from raw Javadoc comment text.
     * Used as a fallback when the parsed Javadoc description loses content
     * (e.g., when {@code @code} blocks contain curly braces).
     *
     * @param rawComment the raw Javadoc comment content (without the comment delimiters)
     * @return list of extracted code examples
     */
    List<String> extractCodeExamplesFromRaw(String rawComment) {
        List<String> examples = new ArrayList<>();

        // Strip leading " * " from each line to get clean Javadoc text
        String cleaned = rawComment.lines()
                .map(line -> line.replaceFirst("^\\s*\\*\\s?", ""))
                .collect(Collectors.joining("\n"));

        Matcher rawMatcher = RAW_PRE_CODE_PATTERN.matcher(cleaned);
        while (rawMatcher.find()) {
            String code = rawMatcher.group(1).trim();
            if (!code.isEmpty()) {
                examples.add(code);
            }
        }

        // Also try the standard patterns on the cleaned text
        if (examples.isEmpty()) {
            examples = extractCodeExamples(cleaned);
        }

        return examples;
    }

    /**
     * Scan a module's source directory for Java files
     *
     * @param moduleDir the module root directory
     * @return list of paths to Java source files
     * @throws IOException if the directory cannot be read
     */
    public List<Path> findJavaSourceFiles(Path moduleDir) throws IOException {
        Path srcMain = moduleDir.resolve("src").resolve("main").resolve("java");
        if (!Files.isDirectory(srcMain)) {
            return List.of();
        }

        try (var stream = Files.walk(srcMain)) {
            return stream
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
        }
    }
}
