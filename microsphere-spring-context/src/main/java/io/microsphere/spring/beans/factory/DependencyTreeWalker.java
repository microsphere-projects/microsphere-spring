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

import java.util.Iterator;
import java.util.List;

/**
 * {@link Dependency} Tree Walker
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DependencyTreeWalker {

    public Dependency walk(Dependency dependency) {
        List<Dependency> children = dependency.children;
        int size = children.size();
        for (int i = 0; i < size; i++) {
            Dependency child = children.get(i);
            List<Dependency> siblings = children.subList(i + 1, size);
            walk(child, siblings);
        }
        removeIfDuplicated(dependency);
        return dependency;
    }

    private void removeIfDuplicated(Dependency dependency) {
        List<Dependency> children = dependency.children;
        Iterator<Dependency> iterator = children.iterator();
        while (iterator.hasNext()) {
            Dependency child = iterator.next();
            removeIfDuplicated(child);
            if (child.duplicated) {
                iterator.remove();
            }
        }
    }

    private void walk(Dependency child, List<Dependency> siblings) {
        if (CollectionUtils.isEmpty(siblings)) {
            return;
        }
        if (siblings.contains(child)) {
            child.duplicate();
        }
        for (Dependency sibling : siblings) {
            if (child.equals(sibling)) {
                child.duplicate();
                mergeChildren(child, sibling);
            }
            // Recursive call
            walk(child, sibling.children);
        }
    }

    private void mergeChildren(Dependency child, Dependency sibling) {
        List<Dependency> sources = child.children;
        List<Dependency> targets = sibling.children;
        if (!sources.equals(targets)) {
            for (Dependency source : sources) {
                if (!targets.contains(source)) {
                    targets.add(source);
                }
            }
        }

    }
}
