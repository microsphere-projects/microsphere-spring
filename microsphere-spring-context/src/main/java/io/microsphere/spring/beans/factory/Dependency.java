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

import io.microsphere.annotation.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.size;
import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static java.util.Collections.emptyList;
import static org.springframework.util.Assert.hasText;

/**
 * Spring Bean Dependency
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class Dependency {

    final String beanName;

    @Nullable
    Dependency parent = null;

    final List<Dependency> children = newLinkedList();

    boolean duplicated = false;

    /**
     * Constructs a {@link Dependency} with the given bean name and no parent or children.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency dep = new Dependency("myBean");
     * }</pre>
     *
     * @param beanName the name of the Spring bean, must not be blank
     */
    protected Dependency(String beanName) {
        this(beanName, null, emptyList());
    }

    /**
     * Constructs a {@link Dependency} with the given bean name, parent, and children.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency parent = Dependency.create("parentBean");
     *   List<Dependency> kids = List.of(Dependency.create("child1"));
     *   Dependency dep = new Dependency("myBean", parent, kids);
     * }</pre>
     *
     * @param beanName the name of the Spring bean, must not be blank
     * @param parent   the parent dependency, or {@code null} if this is a root
     * @param children the initial collection of child dependencies
     */
    protected Dependency(String beanName, Dependency parent, Collection<Dependency> children) {
        hasText(beanName, "The bean name of dependency must not be blank");
        this.beanName = beanName;
        this.parent = parent;
        this.doAddChildren(children);
    }

    /**
     * Sets the parent dependency of this node.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency parent = Dependency.create("A");
     *   Dependency child = Dependency.create("B");
     *   child.setParent(parent);
     * }</pre>
     *
     * @param parent the parent dependency to set
     * @return this {@link Dependency} instance for fluent chaining
     */
    public Dependency setParent(Dependency parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Traverses up the dependency hierarchy to find and return the root dependency.
     * If this dependency has no parent, it returns itself.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency c = a.addChild("C").child("C");
     *   c.addChild("D");
     *   assertEquals(a, c.root()); // root of C is A
     *   assertEquals(a, a.root()); // root of A is itself
     * }</pre>
     *
     * @return the root {@link Dependency} in the hierarchy
     */
    public Dependency root() {
        Dependency root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    /**
     * Returns the parent dependency of this node, or {@code null} if this is the root.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency b = a.child("B");
     *   assertEquals(a, b.parent()); // parent of B is A
     * }</pre>
     *
     * @return the parent {@link Dependency}, or {@code null}
     */
    public Dependency parent() {
        return this.parent;
    }

    /**
     * Adds a child dependency with the specified bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency b = a.child("B"); // retrieve child B
     * }</pre>
     *
     * @param childBeanName the bean name of the child dependency
     * @return this {@link Dependency} instance for fluent chaining
     */
    public Dependency addChild(String childBeanName) {
        return this.doAddChild(create(childBeanName));
    }

    /**
     * Adds multiple child dependencies from the given bean names varargs.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChildren("B", "C", "D");
     * }</pre>
     *
     * @param childBeanNames the bean names of the child dependencies
     * @return this {@link Dependency} instance for fluent chaining
     */
    public Dependency addChildren(String... childBeanNames) {
        return this.addChildren(ofList(childBeanNames));
    }

    /**
     * Adds multiple child dependencies from the given {@link Iterable} of bean names.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   List<String> names = List.of("B", "C");
     *   a.addChildren(names);
     * }</pre>
     *
     * @param childBeanNames an iterable of bean names for the child dependencies
     * @return this {@link Dependency} instance for fluent chaining
     */
    public Dependency addChildren(Iterable<String> childBeanNames) {
        return this.doAddChildren(createList(childBeanNames));
    }

    /**
     * Finds and returns a child dependency by its bean name,
     * or {@code null} if no such child exists.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency b = a.child("B"); // returns child B
     * }</pre>
     *
     * @param childBeanName the bean name of the child to find
     * @return the child {@link Dependency}, or {@code null} if not found
     */
    public Dependency child(String childBeanName) {
        return child(childBeanName, false);
    }

    /**
     * Finds a child dependency by its bean name. If {@code addedIfAbsent} is {@code true}
     * and no child is found, a new standalone {@link Dependency} is created and returned
     * without being added to this dependency's children list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   Dependency b = a.child("B", true); // creates new standalone Dependency if B is absent
     * }</pre>
     *
     * @param childBeanName the bean name of the child to find
     * @param addedIfAbsent if {@code true}, creates a new standalone dependency when the child is absent
     * @return the child {@link Dependency}, or a new standalone instance if absent and {@code addedIfAbsent} is true,
     *         or {@code null} if absent and {@code addedIfAbsent} is false
     */
    public Dependency child(String childBeanName, boolean addedIfAbsent) {
        Dependency child = findChild(childBeanName);
        return addedIfAbsent && child == null ? create(childBeanName) : child;
    }

    /**
     * Searches through the direct children for a dependency matching the given bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency found = a.findChild("B"); // returns child B or null
     * }</pre>
     *
     * @param childBeanName the bean name to search for among children
     * @return the matching child {@link Dependency}, or {@code null} if not found
     */
    protected Dependency findChild(String childBeanName) {
        List<Dependency> children = this.children;
        for (Dependency child : children) {
            if (child.beanName.equals(childBeanName)) {
                return child;
            }
        }
        return null;
    }

    /**
     * Marks this dependency as duplicated.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency dep = Dependency.create("A");
     *   dep.duplicate(); // marks as duplicated
     * }</pre>
     *
     * @return this {@link Dependency} instance for fluent chaining
     */
    protected Dependency duplicate() {
        duplicated = true;
        return this;
    }

    /**
     * Adds a child dependency and sets this as its parent.
     * Subclasses may override this to customize child-addition behavior.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency parent = Dependency.create("A");
     *   Dependency child = Dependency.create("B");
     *   parent.doAddChild(child);
     * }</pre>
     *
     * @param child the child {@link Dependency} to add
     * @return this {@link Dependency} instance for fluent chaining
     */
    protected Dependency doAddChild(Dependency child) {
        child.setParent(this);
        this.children.add(child);
        return this;
    }

    /**
     * Adds multiple child dependencies from the given iterable.
     * Subclasses may override this to customize batch child-addition behavior.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency parent = Dependency.create("A");
     *   List<Dependency> kids = List.of(Dependency.create("B"), Dependency.create("C"));
     *   parent.doAddChildren(kids);
     * }</pre>
     *
     * @param children the iterable of child {@link Dependency} instances to add
     * @return this {@link Dependency} instance for fluent chaining
     */
    protected Dependency doAddChildren(Iterable<Dependency> children) {
        for (Dependency child : children) {
            doAddChild(child);
        }
        return this;
    }

    /**
     * Factory method to create a new {@link Dependency} with the specified bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency c = a.addChild("C").child("C");
     *   c.addChild("D");
     * }</pre>
     *
     * @param beanName the name of the Spring bean
     * @return a new {@link Dependency} instance
     */
    public static Dependency create(String beanName) {
        return new Dependency(beanName);
    }

    private static List<Dependency> createList(Iterable<String> beanNames) {
        int length = size(beanNames);
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

    /**
     * Compares this dependency to the specified object for equality based on the bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a1 = Dependency.create("A");
     *   Dependency a2 = Dependency.create("A");
     *   assertTrue(a1.equals(a2)); // same bean name
     * }</pre>
     *
     * @param o the object to compare with
     * @return {@code true} if the other object is a {@link Dependency} with the same bean name
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return beanName.equals(that.beanName);
    }

    /**
     * Returns a hash code based on the bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   int hash = a.hashCode();
     * }</pre>
     *
     * @return the hash code of the bean name
     */
    @Override
    public int hashCode() {
        return beanName.hashCode();
    }

    /**
     * Returns a string representation of this dependency, including its bean name
     * and children if any.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   String str = a.toString(); // e.g. "A[B]"
     * }</pre>
     *
     * @return a string representation of this dependency
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(beanName);
        if (!children.isEmpty()) {
            sb.append(children);
        }
        return sb.toString();
    }
}
