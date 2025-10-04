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
 * The enumeration for the released Spring versions since 3.2
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public enum SpringVersion {

    SPRING_3_2,

    SPRING_3_2_0,

    SPRING_3_2_1,

    SPRING_3_2_2,

    SPRING_3_2_3,

    SPRING_3_2_4,

    SPRING_3_2_5,

    SPRING_3_2_6,

    SPRING_3_2_7,

    SPRING_3_2_8,

    SPRING_3_2_9,

    SPRING_3_2_10,

    SPRING_3_2_11,

    SPRING_3_2_12,

    SPRING_3_2_13,

    SPRING_3_2_14,

    SPRING_3_2_15,

    SPRING_3_2_16,

    SPRING_3_2_17,

    SPRING_3_2_18,

    SPRING_4,

    SPRING_4_0,

    SPRING_4_0_0,

    SPRING_4_0_1,

    SPRING_4_0_2,

    SPRING_4_0_3,

    SPRING_4_0_4,

    SPRING_4_0_5,

    SPRING_4_0_6,

    SPRING_4_0_7,

    SPRING_4_0_8,

    SPRING_4_0_9,

    SPRING_4_1,

    SPRING_4_1_0,

    SPRING_4_1_1,

    SPRING_4_1_2,

    SPRING_4_1_3,

    SPRING_4_1_4,

    SPRING_4_1_5,

    SPRING_4_1_6,

    SPRING_4_1_7,

    SPRING_4_1_8,

    SPRING_4_1_9,

    SPRING_4_2,

    SPRING_4_2_0,

    SPRING_4_2_1,

    SPRING_4_2_2,

    SPRING_4_2_3,

    SPRING_4_2_4,

    SPRING_4_2_5,

    SPRING_4_2_6,

    SPRING_4_2_7,

    SPRING_4_2_8,

    SPRING_4_2_9,

    SPRING_4_3,

    SPRING_4_3_0,

    SPRING_4_3_1,

    SPRING_4_3_2,

    SPRING_4_3_3,

    SPRING_4_3_4,

    SPRING_4_3_5,

    SPRING_4_3_6,

    SPRING_4_3_7,

    SPRING_4_3_8,

    SPRING_4_3_9,

    SPRING_4_3_10,

    SPRING_4_3_11,

    SPRING_4_3_12,

    SPRING_4_3_13,

    SPRING_4_3_14,

    SPRING_4_3_15,

    SPRING_4_3_16,

    SPRING_4_3_17,

    SPRING_4_3_18,

    SPRING_4_3_19,

    SPRING_4_3_20,

    SPRING_4_3_21,

    SPRING_4_3_22,

    SPRING_4_3_23,

    SPRING_4_3_24,

    SPRING_4_3_25,

    SPRING_4_3_26,

    SPRING_4_3_27,

    SPRING_4_3_28,

    SPRING_4_3_29,

    SPRING_4_3_30,

    SPRING_5,

    SPRING_5_0,

    SPRING_5_0_0,

    SPRING_5_0_1,

    SPRING_5_0_2,

    SPRING_5_0_3,

    SPRING_5_0_4,

    SPRING_5_0_5,

    SPRING_5_0_6,

    SPRING_5_0_7,

    SPRING_5_0_8,

    SPRING_5_0_9,

    SPRING_5_0_10,

    SPRING_5_0_11,

    SPRING_5_0_12,

    SPRING_5_0_13,

    SPRING_5_0_14,

    SPRING_5_0_15,

    SPRING_5_0_16,

    SPRING_5_0_17,

    SPRING_5_0_18,

    SPRING_5_0_19,

    SPRING_5_0_20,

    SPRING_5_1,

    SPRING_5_1_0,

    SPRING_5_1_1,

    SPRING_5_1_2,

    SPRING_5_1_3,

    SPRING_5_1_4,

    SPRING_5_1_5,

    SPRING_5_1_6,

    SPRING_5_1_7,

    SPRING_5_1_8,

    SPRING_5_1_9,

    SPRING_5_1_10,

    SPRING_5_1_11,

    SPRING_5_1_12,

    SPRING_5_1_13,

    SPRING_5_1_14,

    SPRING_5_1_15,

    SPRING_5_1_16,

    SPRING_5_1_17,

    SPRING_5_1_18,

    SPRING_5_1_19,

    SPRING_5_1_20,

    SPRING_5_2,

    SPRING_5_2_0,

    SPRING_5_2_1,

    SPRING_5_2_2,

    SPRING_5_2_3,

    SPRING_5_2_4,

    SPRING_5_2_5,

    SPRING_5_2_6,

    SPRING_5_2_7,

    SPRING_5_2_8,

    SPRING_5_2_9,

    SPRING_5_2_10,

    SPRING_5_2_11,

    SPRING_5_2_12,

    SPRING_5_2_13,

    SPRING_5_2_14,

    SPRING_5_2_15,

    SPRING_5_2_16,

    SPRING_5_2_17,

    SPRING_5_2_18,

    SPRING_5_2_19,

    SPRING_5_2_20,

    SPRING_5_2_21,

    SPRING_5_2_22,

    SPRING_5_2_23,

    SPRING_5_2_24,

    SPRING_5_2_25,

    SPRING_5_3,

    SPRING_5_3_0,

    SPRING_5_3_1,

    SPRING_5_3_2,

    SPRING_5_3_3,

    SPRING_5_3_4,

    SPRING_5_3_5,

    SPRING_5_3_6,

    SPRING_5_3_7,

    SPRING_5_3_8,

    SPRING_5_3_9,

    SPRING_5_3_10,

    SPRING_5_3_11,

    SPRING_5_3_12,

    SPRING_5_3_13,

    SPRING_5_3_14,

    SPRING_5_3_15,

    SPRING_5_3_16,

    SPRING_5_3_17,

    SPRING_5_3_18,

    SPRING_5_3_19,

    SPRING_5_3_20,

    SPRING_5_3_21,

    SPRING_5_3_22,

    SPRING_5_3_23,

    SPRING_5_3_24,

    SPRING_5_3_25,

    SPRING_5_3_26,

    SPRING_5_3_27,

    SPRING_5_3_28,

    SPRING_5_3_29,

    SPRING_5_3_30,

    SPRING_5_3_31,

    SPRING_5_3_32,

    SPRING_5_3_33,

    SPRING_5_3_34,

    SPRING_5_3_35,

    SPRING_5_3_36,

    SPRING_5_3_37,

    SPRING_5_3_38,

    SPRING_5_3_39,

    CURRENT(of(org.springframework.core.SpringVersion.getVersion()));;

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
