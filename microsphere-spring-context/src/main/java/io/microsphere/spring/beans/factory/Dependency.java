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
     * Constructs a {@link Dependency} with the specified bean name, no parent, and no children.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = new Dependency("A");
     * }</pre>
     *
     * @param beanName the name of the Spring bean; must not be blank
     * @throws IllegalArgumentException if {@code beanName} is blank
     */
    protected Dependency(String beanName) {
        this(beanName, null, emptyList());
    }

    /**
     * Constructs a {@link Dependency} with the specified bean name, parent, and children.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency parent = Dependency.create("A");
     *   List<Dependency> children = Arrays.asList(Dependency.create("B"));
     *   Dependency dep = new Dependency("C", parent, children);
     * }</pre>
     *
     * @param beanName the name of the Spring bean; must not be blank
     * @param parent   the parent dependency, or {@code null} if this is a root
     * @param children the initial child dependencies to add
     * @throws IllegalArgumentException if {@code beanName} is blank
     */
    protected Dependency(String beanName, Dependency parent, Collection<Dependency> children) {
        hasText(beanName, "The bean name of dependency must not be blank");
        this.beanName = beanName;
        this.parent = parent;
        this.doAddChildren(children);
    }

    /**
     * Sets the parent of this dependency.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   Dependency b = Dependency.create("B");
     *   b.setParent(a);
     * }</pre>
     *
     * @param parent the parent dependency to set
     * @return this dependency for method chaining
     */
    public Dependency setParent(Dependency parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Returns the root dependency of this dependency tree by traversing up the parent chain.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   Dependency c = a.addChild("B")
     *           .addChild("C")
     *           .child("C")
     *           .addChildren("D", "E");
     *   Dependency root = c.root();
     *   // root is the same instance as a
     * }</pre>
     *
     * @return the root dependency (the ancestor with no parent); returns {@code this} if it has no parent
     */
    public Dependency root() {
        Dependency root = this;
        while (root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    /**
     * Returns the parent dependency of this dependency.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("C");
     *   Dependency parentOfC = a.child("C").parent();
     *   // parentOfC equals a
     * }</pre>
     *
     * @return the parent dependency, or {@code null} if this is the root
     */
    public Dependency parent() {
        return this.parent;
    }

    /**
     * Creates and adds a child dependency with the given bean name to this dependency.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B").addChild("C");
     *   // a now has children "B" and "C"
     * }</pre>
     *
     * @param childBeanName the bean name of the child dependency to add
     * @return this dependency for method chaining
     */
    public Dependency addChild(String childBeanName) {
        return this.doAddChild(create(childBeanName));
    }

    /**
     * Creates and adds multiple child dependencies with the given bean names to this dependency.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency c = Dependency.create("C");
     *   c.addChildren("D", "E");
     *   // c now has children "D" and "E"
     * }</pre>
     *
     * @param childBeanNames the bean names of the child dependencies to add
     * @return this dependency for method chaining
     */
    public Dependency addChildren(String... childBeanNames) {
        return this.addChildren(ofList(childBeanNames));
    }

    /**
     * Creates and adds multiple child dependencies from an {@link Iterable} of bean names.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency c = Dependency.create("C");
     *   List<String> names = Arrays.asList("D", "E");
     *   c.addChildren(names);
     *   // c now has children "D" and "E"
     * }</pre>
     *
     * @param childBeanNames the bean names of the child dependencies to add
     * @return this dependency for method chaining
     */
    public Dependency addChildren(Iterable<String> childBeanNames) {
        return this.doAddChildren(createList(childBeanNames));
    }

    /**
     * Returns the child dependency with the specified bean name.
     * Equivalent to calling {@code child(childBeanName, false)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B").addChild("C");
     *   Dependency c = a.child("C");
     *   // c is the child dependency named "C"
     * }</pre>
     *
     * @param childBeanName the bean name of the child to look up
     * @return the child dependency, or {@code null} if not found
     */
    public Dependency child(String childBeanName) {
        return child(childBeanName, false);
    }

    /**
     * Returns the child dependency with the specified bean name, optionally creating one if absent.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency b = a.child("B", false);   // returns existing child "B"
     *   Dependency c = a.child("C", true);     // creates a new Dependency for "C"
     *   Dependency d = a.child("D", false);    // returns null (not found)
     * }</pre>
     *
     * @param childBeanName  the bean name of the child to look up
     * @param addedIfAbsent  if {@code true} and no child is found, a new independent {@link Dependency} is created
     *                       and returned (it is <em>not</em> added to this dependency's children and has no parent set)
     * @return the child dependency, a newly created dependency if {@code addedIfAbsent} is {@code true} and the child
     *         was not found, or {@code null} if the child was not found and {@code addedIfAbsent} is {@code false}
     */
    public Dependency child(String childBeanName, boolean addedIfAbsent) {
        Dependency child = findChild(childBeanName);
        return addedIfAbsent && child == null ? create(childBeanName) : child;
    }

    /**
     * Searches for a direct child dependency with the specified bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B");
     *   Dependency found = a.findChild("B");   // returns child "B"
     *   Dependency missing = a.findChild("X"); // returns null
     * }</pre>
     *
     * @param childBeanName the bean name of the child to find
     * @return the matching child dependency, or {@code null} if no child with that bean name exists
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
     * Marks this dependency as duplicated and returns it.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   Dependency dup = a.duplicate();
     *   // dup is the same instance as a, now marked as duplicated
     * }</pre>
     *
     * @return this dependency, marked as duplicated
     */
    protected Dependency duplicate() {
        duplicated = true;
        return this;
    }

    /**
     * Adds the given child dependency to this dependency, setting this dependency as its parent.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   Dependency b = Dependency.create("B");
     *   a.doAddChild(b);
     *   // b.parent() now equals a
     * }</pre>
     *
     * @param child the child dependency to add
     * @return this dependency for method chaining
     */
    protected Dependency doAddChild(Dependency child) {
        child.setParent(this);
        this.children.add(child);
        return this;
    }

    /**
     * Adds each dependency in the given iterable as a child of this dependency.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   List<Dependency> kids = Arrays.asList(Dependency.create("B"), Dependency.create("C"));
     *   a.doAddChildren(kids);
     *   // a now has children "B" and "C"
     * }</pre>
     *
     * @param children the child dependencies to add
     * @return this dependency for method chaining
     */
    protected Dependency doAddChildren(Iterable<Dependency> children) {
        for (Dependency child : children) {
            doAddChild(child);
        }
        return this;
    }

    /**
     * Factory method that creates a new {@link Dependency} with the given bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B").addChild("C");
     * }</pre>
     *
     * @param beanName the name of the Spring bean; must not be blank
     * @return a new {@link Dependency} instance
     * @throws IllegalArgumentException if {@code beanName} is blank
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
     * Compares this dependency to the specified object for equality.
     * Two dependencies are considered equal if they have the same bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   Dependency a2 = Dependency.create("A");
     *   boolean eq = a.equals(a2); // true
     * }</pre>
     *
     * @param o the object to compare with
     * @return {@code true} if the given object is a {@link Dependency} with the same bean name
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Dependency that = (Dependency) o;
        return beanName.equals(that.beanName);
    }

    /**
     * Returns the hash code of this dependency, based on the bean name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a1 = Dependency.create("A");
     *   Dependency a2 = Dependency.create("A");
     *   boolean same = a1.hashCode() == a2.hashCode(); // true
     * }</pre>
     *
     * @return the hash code computed from the bean name
     */
    @Override
    public int hashCode() {
        return beanName.hashCode();
    }

    /**
     * Returns a string representation of this dependency, consisting of the bean name
     * followed by its children (if any).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Dependency a = Dependency.create("A");
     *   a.addChild("B").addChild("C");
     *   String str = a.toString(); // "A[B, C]"
     * }</pre>
     *
     * @return the string representation of this dependency
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
