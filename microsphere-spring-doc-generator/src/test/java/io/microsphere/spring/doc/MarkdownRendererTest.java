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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link MarkdownRenderer}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class MarkdownRendererTest {

    private MarkdownRenderer renderer;

    @BeforeEach
    void setUp() {
        renderer = new MarkdownRenderer();
    }

    @Test
    void testRenderInterface() {
        JavaComponentInfo info = createSampleInterface();
        String markdown = renderer.render(info);

        assertNotNull(markdown);
        // Title
        assertTrue(markdown.contains("# SampleInterface"));
        // Overview table
        assertTrue(markdown.contains("test-module"));
        assertTrue(markdown.contains("sample"));
        assertTrue(markdown.contains("Interface"));
        assertTrue(markdown.contains("1.0.0"));
        // Description
        assertTrue(markdown.contains("Description"));
        assertTrue(markdown.contains("sample interface"));
        // Code examples
        assertTrue(markdown.contains("Code Examples"));
        assertTrue(markdown.contains("```java"));
        // Version compatibility
        assertTrue(markdown.contains("Version Compatibility"));
        assertTrue(markdown.contains("Spring Framework"));
        // See also
        assertTrue(markdown.contains("See Also"));
    }

    @Test
    void testRenderAnnotation() {
        JavaComponentInfo info = createSampleAnnotation();
        String markdown = renderer.render(info);

        assertNotNull(markdown);
        assertTrue(markdown.contains("# SampleAnnotation"));
        assertTrue(markdown.contains("Annotation"));
        // Annotation auto-generated example should contain @
        assertTrue(markdown.contains("@SampleAnnotation"));
    }

    @Test
    void testRenderWithMethods() {
        JavaComponentInfo info = createSampleInterface();
        String markdown = renderer.render(info);

        assertTrue(markdown.contains("Public API"));
        assertTrue(markdown.contains("resolve"));
        assertTrue(markdown.contains("hasDependencies"));
    }

    @Test
    void testCleanDescription() {
        String html = "<p>First paragraph</p><p>Second paragraph</p>";
        String cleaned = renderer.cleanDescription(html);
        assertFalse(cleaned.contains("<p>"));
        assertFalse(cleaned.contains("</p>"));
    }

    @Test
    void testCleanDescriptionWithLinks() {
        String html = "Uses {@link BeanFactory} for resolution";
        String cleaned = renderer.cleanDescription(html);
        assertTrue(cleaned.contains("`BeanFactory`"));
    }

    @Test
    void testGenerateBasicExampleForInterface() {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setSimpleName("MyInterface");
        info.setComponentType(JavaComponentInfo.ComponentType.INTERFACE);
        info.setTypeParameters(List.of());

        String example = renderer.generateBasicExample(info);
        assertTrue(example.contains("implements MyInterface"));
    }

    @Test
    void testGenerateBasicExampleForAnnotation() {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setSimpleName("MyAnnotation");
        info.setComponentType(JavaComponentInfo.ComponentType.ANNOTATION);

        String example = renderer.generateBasicExample(info);
        assertTrue(example.contains("@MyAnnotation"));
    }

    @Test
    void testGeneratePageName() {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setQualifiedName("io.microsphere.spring.beans.BeanUtils");
        String pageName = renderer.generatePageName(info);
        assertTrue(pageName.contains("io-microsphere-spring-beans-BeanUtils"));
    }

    private JavaComponentInfo createSampleInterface() {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setModuleName("test-module");
        info.setPackageName("sample");
        info.setSimpleName("SampleInterface");
        info.setQualifiedName("sample.SampleInterface");
        info.setComponentType(JavaComponentInfo.ComponentType.INTERFACE);
        info.setDescription("A sample interface for testing");
        info.setSinceVersion("1.0.0");
        info.setAuthor("Mercy");
        info.setSeeReferences(List.of("java.util.Map", "java.util.Set"));
        info.setCodeExamples(List.of("SampleInterface impl = new SampleInterfaceImpl();"));

        JavaComponentInfo.MethodInfo resolve = new JavaComponentInfo.MethodInfo();
        resolve.setName("resolve");
        resolve.setReturnType("Map<String, Set<String>>");
        resolve.setParameters(List.of("String beanName"));
        resolve.setDescription("Resolve dependencies for the given bean name");
        resolve.setSinceVersion("1.0.0");

        JavaComponentInfo.MethodInfo hasDeps = new JavaComponentInfo.MethodInfo();
        hasDeps.setName("hasDependencies");
        hasDeps.setReturnType("boolean");
        hasDeps.setParameters(List.of("String beanName"));
        hasDeps.setDescription("Check if the given bean has dependencies");

        info.setPublicMethods(List.of(resolve, hasDeps));
        return info;
    }

    private JavaComponentInfo createSampleAnnotation() {
        JavaComponentInfo info = new JavaComponentInfo();
        info.setModuleName("test-module");
        info.setPackageName("sample");
        info.setSimpleName("SampleAnnotation");
        info.setQualifiedName("sample.SampleAnnotation");
        info.setComponentType(JavaComponentInfo.ComponentType.ANNOTATION);
        info.setDescription("A sample annotation for testing");
        info.setSinceVersion("1.0.0");
        return info;
    }
}
