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
package io.microsphere.spring.beans.factory;

import io.microsphere.collection.CollectionUtils;
import io.microsphere.util.ArrayUtils;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static java.util.Collections.emptyList;

/**
 * Spring Bean Dependency
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Dependency {

    final String beanName;

    List<Dependency> upstream;

    List<Dependency> downstream;

    @Nullable
    @Deprecated
    Dependency parent = null;

    @Deprecated
    final List<Dependency> children = newLinkedList();

    boolean duplicated = false;

    protected Dependency(String beanName) {
        this(beanName, null, emptyList());
    }

    protected Dependency(String beanName, Dependency parent, String... dependentBeanNames) {
        this(beanName, parent, createList(dependentBeanNames));
    }

    protected Dependency(String beanName, Dependency parent, Collection<Dependency> children) {
        Assert.hasText(beanName, "The bean name of dependency must not be blank");
        this.beanName = beanName;
        this.parent = parent;
        this.doAddChildren(children);
    }

    public Dependency setParent(Dependency parent) {
        this.parent = parent;
        return this;
    }

    public Dependency root() {
        Dependency root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public Dependency parent() {
        return this.parent;
    }

    public Dependency addChild(String childBeanName) {
        return this.doAddChild(create(childBeanName));
    }

    public Dependency addChildren(String... childBeanNames) {
        return this.doAddChildren(createList(childBeanNames));
    }

    public Dependency addChildren(Iterable<String> childBeanNames) {
        return this.doAddChildren(createList(childBeanNames));
    }

    public Dependency child(String childBeanName) {
        return child(childBeanName, false);
    }

    public Dependency child(String childBeanName, boolean addedIfAbsent) {
        Dependency child = findChild(childBeanName);
        return addedIfAbsent && child == null ? create(childBeanName) : child;
    }

    protected Dependency findChild(String childBeanName) {
        List<Dependency> children = this.children;
        for (Dependency child : children) {
            if (child.beanName.equals(childBeanName)) {
                return child;
            }
        }
        return null;
    }

    protected Dependency duplicate() {
        duplicated = true;
        return this;
    }

    protected Dependency doAddChild(Dependency child) {
        child.setParent(this);
        this.children.add(child);
        return this;
    }

    protected Dependency doAddChildren(Dependency... children) {
        this.doAddChildren(Arrays.asList(children));
        return this;
    }

    protected Dependency doAddChildren(Iterable<Dependency> children) {
        for (Dependency child : children) {
            doAddChild(child);
        }
        return this;
    }

    public static Dependency create(String beanName) {
        return new Dependency(beanName);
    }

    public static Dependency create(String beanName, Dependency parent, String... dependentBeanNames) {
        return new Dependency(beanName, parent, dependentBeanNames);
    }

    private static List<Dependency> createList(Iterable<String> beanNames) {
        int length = CollectionUtils.size(beanNames);
        if (length < 1) {
            return emptyList();
        }
        List<Dependency> dependencies = newArrayList(length);

        Iterator<String> iterator = beanNames.iterator();
        while (iterator.hasNext()) {
            dependencies.add(create(iterator.next()));
        }
        return dependencies;
    }

    private static List<Dependency> createList(String[] beanNames) {
        int length = ArrayUtils.length(beanNames);
        if (length < 1) {
            return emptyList();
        }
        List<Dependency> dependencies = newArrayList(length);
        for (int i = 0; i < length; i++) {
            dependencies.add(create(beanNames[i]));
        }
        return dependencies;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return beanName.equals(that.beanName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(beanName);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(beanName);
        if (!children.isEmpty()) {
            sb.append(children);
        }
        return sb.toString();
    }
}
