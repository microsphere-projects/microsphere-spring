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
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link JavaSourceParser}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class JavaSourceParserTest {

    private JavaSourceParser parser;

    @BeforeEach
    void setUp() {
        parser = new JavaSourceParser();
    }

    @Test
    void testParseInterface() throws IOException {
        Path sampleFile = Path.of("src/test/resources/sample/SampleInterface.java");
        List<JavaComponentInfo> components = parser.parse(sampleFile, "test-module");

        assertEquals(1, components.size());

        JavaComponentInfo info = components.get(0);
        assertEquals("SampleInterface", info.getSimpleName());
        assertEquals("sample.SampleInterface", info.getQualifiedName());
        assertEquals("sample", info.getPackageName());
        assertEquals("test-module", info.getModuleName());
        assertEquals(JavaComponentInfo.ComponentType.INTERFACE, info.getComponentType());
        assertEquals("1.0.0", info.getSinceVersion());
        assertNotNull(info.getDescription());
        assertTrue(info.getDescription().contains("sample interface for testing"));
        assertFalse(info.getSeeReferences().isEmpty());
        assertFalse(info.getCodeExamples().isEmpty());

        // Check public methods
        assertEquals(2, info.getPublicMethods().size());

        JavaComponentInfo.MethodInfo resolveMethod = info.getPublicMethods().stream()
                .filter(m -> "resolve".equals(m.getName()))
                .findFirst()
                .orElseThrow();
        assertEquals("Map<String,Set<String>>", resolveMethod.getReturnType());
        assertEquals("1.0.0", resolveMethod.getSinceVersion());
    }

    @Test
    void testParseAnnotation() throws IOException {
        Path sampleFile = Path.of("src/test/resources/sample/SampleAnnotation.java");
        List<JavaComponentInfo> components = parser.parse(sampleFile, "test-module");

        assertEquals(1, components.size());

        JavaComponentInfo info = components.get(0);
        assertEquals("SampleAnnotation", info.getSimpleName());
        assertEquals(JavaComponentInfo.ComponentType.ANNOTATION, info.getComponentType());
        assertEquals("1.0.0", info.getSinceVersion());
        assertFalse(info.getCodeExamples().isEmpty());
    }

    @Test
    void testParseAbstractClass() throws IOException {
        Path sampleFile = Path.of("src/test/resources/sample/SampleAbstractClass.java");
        List<JavaComponentInfo> components = parser.parse(sampleFile, "test-module");

        assertEquals(1, components.size());

        JavaComponentInfo info = components.get(0);
        assertEquals("SampleAbstractClass", info.getSimpleName());
        assertEquals(JavaComponentInfo.ComponentType.ABSTRACT_CLASS, info.getComponentType());
        assertEquals("1.0.0", info.getSinceVersion());
        assertFalse(info.getImplementedInterfaces().isEmpty());
        assertTrue(info.getImplementedInterfaces().contains("SampleInterface"));
        assertFalse(info.getTypeParameters().isEmpty());
    }

    @Test
    void testExtractCodeExamples() {
        String text = "Some text\n<pre>{@code\nString x = \"hello\";\nSystem.out.println(x);\n}</pre>\nMore text";
        List<String> examples = parser.extractCodeExamples(text);
        assertEquals(1, examples.size());
        assertTrue(examples.get(0).contains("String x"));
    }

    @Test
    void testExtractCodeExamplesEmpty() {
        String text = "Simple description with no code blocks";
        List<String> examples = parser.extractCodeExamples(text);
        assertTrue(examples.isEmpty());
    }
}
