/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License,
 Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 software
 * distributed under the License is distributed on an "AS IS" BASIS,

 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.core;

import io.microsphere.util.Version;

import static io.microsphere.constants.SymbolConstants.DOT_CHAR;
import static io.microsphere.constants.SymbolConstants.UNDER_SCORE_CHAR;
import static io.microsphere.util.Version.of;

/**
 * The enumeration for the released Spring versions since 6.0
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public enum SpringVersion {

    SPRING_6_0,

    SPRING_6_0_0,

    SPRING_6_0_1,

    SPRING_6_0_2,

    SPRING_6_0_3,

    SPRING_6_0_4,

    SPRING_6_0_5,

    SPRING_6_0_6,

    SPRING_6_0_7,

    SPRING_6_0_8,

    SPRING_6_0_9,

    SPRING_6_0_10,

    SPRING_6_0_11,

    SPRING_6_0_12,

    SPRING_6_0_13,

    SPRING_6_0_14,

    SPRING_6_0_15,

    SPRING_6_0_16,

    SPRING_6_0_17,

    SPRING_6_0_18,

    SPRING_6_0_19,

    SPRING_6_0_20,

    SPRING_6_0_21,

    SPRING_6_0_22,

    SPRING_6_0_23,

    SPRING_6_1,

    SPRING_6_1_0,

    SPRING_6_1_1,

    SPRING_6_1_2,

    SPRING_6_1_3,

    SPRING_6_1_4,

    SPRING_6_1_5,

    SPRING_6_1_6,

    SPRING_6_1_7,

    SPRING_6_1_8,

    SPRING_6_1_9,

    SPRING_6_1_10,

    SPRING_6_1_11,

    SPRING_6_1_12,

    SPRING_6_1_13,

    SPRING_6_1_14,

    SPRING_6_1_15,

    SPRING_6_1_16,

    SPRING_6_1_17,

    SPRING_6_1_18,

    SPRING_6_1_19,

    SPRING_6_1_20,

    SPRING_6_1_21,

    SPRING_6_2,

    SPRING_6_2_0,

    SPRING_6_2_1,

    SPRING_6_2_2,

    SPRING_6_2_3,

    SPRING_6_2_4,

    SPRING_6_2_5,

    SPRING_6_2_6,

    SPRING_6_2_7,

    SPRING_6_2_8,

    SPRING_6_2_9,

    SPRING_6_2_10,

    SPRING_6_2_11,

    SPRING_6_2_12,

    SPRING_7_0,

    SPRING_7_0_0,

    CURRENT(of(org.springframework.core.SpringVersion.getVersion()));

    private final Version version;

    SpringVersion() {
        this.version = resolveVersion(name());
    }

    SpringVersion(Version version) {
        this.version = version;
    }

    static Version resolveVersion(String name) {
        String version = name.substring(7).replace(UNDER_SCORE_CHAR, DOT_CHAR);
        return of(version);
    }

    public Version getVersion() {
        return version;
    }

    public int getMajor() {
        return version.getMajor();
    }

    public int getMinor() {
        return version.getMinor();
    }

    public int getPatch() {
        return version.getPatch();
    }

    public boolean gt(SpringVersion that) {
        return version.gt(that.version);
    }

    public boolean isGreaterThan(SpringVersion that) {
        return version.isGreaterThan(that.version);
    }

    public boolean ge(SpringVersion that) {
        return version.ge(that.version);
    }

    public boolean isGreaterOrEqual(SpringVersion that) {
        return version.isGreaterOrEqual(that.version);
    }

    public boolean lt(SpringVersion that) {
        return version.lt(that.version);
    }

    public boolean isLessThan(SpringVersion that) {
        return version.isLessThan(that.version);
    }

    public boolean le(SpringVersion that) {
        return version.le(that.version);
    }

    public boolean isLessOrEqual(SpringVersion that) {
        return version.isLessOrEqual(that.version);
    }

    public boolean eq(SpringVersion that) {
        return version.eq(that.version);
    }

    public boolean equals(SpringVersion that) {
        return version.equals(that.version);
    }
}
