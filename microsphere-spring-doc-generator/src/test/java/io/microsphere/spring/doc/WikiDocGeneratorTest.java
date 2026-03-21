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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link WikiDocGenerator}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class WikiDocGeneratorTest {

    private WikiDocGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new WikiDocGenerator();
    }

    @Test
    void testGenerateWithProjectRoot(@TempDir Path outputDir) throws IOException {
        // Use the actual project root for a real integration test
        Path projectRoot = Path.of("").toAbsolutePath().getParent();

        // Only run if we can find the project root with modules
        if (!Files.isDirectory(projectRoot.resolve("microsphere-spring-context"))) {
            // Try current directory
            projectRoot = Path.of("").toAbsolutePath();
        }

        if (!Files.isDirectory(projectRoot.resolve("microsphere-spring-context"))) {
            // Skip if project root not found
            return;
        }

        List<Path> pages = generator.generate(projectRoot, outputDir);

        // Should generate pages
        assertFalse(pages.isEmpty());

        // Should have Home.md index page
        assertTrue(Files.exists(outputDir.resolve("Home.md")));

        // Should have _Sidebar.md
        assertTrue(Files.exists(outputDir.resolve("_Sidebar.md")));

        // Home.md should contain module sections
        String homeContent = Files.readString(outputDir.resolve("Home.md"));
        assertTrue(homeContent.contains("microsphere-spring-context"));
        assertTrue(homeContent.contains("Version Compatibility"));

        // Check that individual pages were created
        long mdFileCount = Files.list(outputDir)
                .filter(p -> p.toString().endsWith(".md"))
                .count();
        assertTrue(mdFileCount > 10, "Should generate more than 10 wiki pages, got: " + mdFileCount);
    }

    @Test
    void testGenerateIndexPage(@TempDir Path outputDir) throws IOException {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setModuleName("test-module");
        info.setPackageName("sample");
        info.setSimpleName("TestComponent");
        info.setQualifiedName("sample.TestComponent");
        info.setComponentType(JavaComponentInfo.ComponentType.CLASS);
        info.setDescription("Test component");
        info.setSinceVersion("1.0.0");

        Path indexPage = generator.generateIndexPage(outputDir, List.of(info));

        assertTrue(Files.exists(indexPage));
        String content = Files.readString(indexPage);
        assertTrue(content.contains("TestComponent"));
        assertTrue(content.contains("test-module"));
        assertTrue(content.contains("Version Compatibility"));
    }

    @Test
    void testGenerateSidebarPage(@TempDir Path outputDir) throws IOException {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setModuleName("test-module");
        info.setPackageName("sample");
        info.setSimpleName("TestComponent");
        info.setQualifiedName("sample.TestComponent");
        info.setComponentType(JavaComponentInfo.ComponentType.CLASS);

        Path sidebarPage = generator.generateSidebarPage(outputDir, List.of(info));

        assertTrue(Files.exists(sidebarPage));
        String content = Files.readString(sidebarPage);
        assertTrue(content.contains("Navigation"));
        assertTrue(content.contains("TestComponent"));
        assertTrue(content.contains("test-module"));
    }
}
