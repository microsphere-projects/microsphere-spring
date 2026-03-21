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

import java.util.ArrayList;
import java.util.List;

/**
 * Holds extracted information about a Java component for documentation generation.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class JavaComponentInfo {

    /**
     * The type of Java component
     */
    public enum ComponentType {
        CLASS,
        ABSTRACT_CLASS,
        INTERFACE,
        ANNOTATION,
        ENUM,
        RECORD
    }

    private String moduleName;

    private String packageName;

    private String simpleName;

    private String qualifiedName;

    private ComponentType componentType;

    private String description;

    private String sinceVersion;

    private String author;

    private List<String> seeReferences = new ArrayList<>();

    private List<String> codeExamples = new ArrayList<>();

    private List<MethodInfo> publicMethods = new ArrayList<>();

    private List<String> implementedInterfaces = new ArrayList<>();

    private String superClass;

    private List<String> typeParameters = new ArrayList<>();

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setSimpleName(String simpleName) {
        this.simpleName = simpleName;
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public ComponentType getComponentType() {
        return componentType;
    }

    public void setComponentType(ComponentType componentType) {
        this.componentType = componentType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSinceVersion() {
        return sinceVersion;
    }

    public void setSinceVersion(String sinceVersion) {
        this.sinceVersion = sinceVersion;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public List<String> getSeeReferences() {
        return seeReferences;
    }

    public void setSeeReferences(List<String> seeReferences) {
        this.seeReferences = seeReferences;
    }

    public List<String> getCodeExamples() {
        return codeExamples;
    }

    public void setCodeExamples(List<String> codeExamples) {
        this.codeExamples = codeExamples;
    }

    public List<MethodInfo> getPublicMethods() {
        return publicMethods;
    }

    public void setPublicMethods(List<MethodInfo> publicMethods) {
        this.publicMethods = publicMethods;
    }

    public List<String> getImplementedInterfaces() {
        return implementedInterfaces;
    }

    public void setImplementedInterfaces(List<String> implementedInterfaces) {
        this.implementedInterfaces = implementedInterfaces;
    }

    public String getSuperClass() {
        return superClass;
    }

    public void setSuperClass(String superClass) {
        this.superClass = superClass;
    }

    public List<String> getTypeParameters() {
        return typeParameters;
    }

    public void setTypeParameters(List<String> typeParameters) {
        this.typeParameters = typeParameters;
    }

    /**
     * Holds information about a public method
     */
    public static class MethodInfo {

        private String name;

        private String returnType;

        private List<String> parameters = new ArrayList<>();

        private String description;

        private String sinceVersion;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getReturnType() {
            return returnType;
        }

        public void setReturnType(String returnType) {
            this.returnType = returnType;
        }

        public List<String> getParameters() {
            return parameters;
        }

        public void setParameters(List<String> parameters) {
            this.parameters = parameters;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getSinceVersion() {
            return sinceVersion;
        }

        public void setSinceVersion(String sinceVersion) {
            this.sinceVersion = sinceVersion;
        }

        /**
         * Get the method signature as a formatted string
         *
         * @return the method signature
         */
        public String getSignature() {
            StringBuilder sb = new StringBuilder();
            sb.append(returnType).append(" ").append(name).append("(");
            sb.append(String.join(", ", parameters));
            sb.append(")");
            return sb.toString();
        }
    }
}
