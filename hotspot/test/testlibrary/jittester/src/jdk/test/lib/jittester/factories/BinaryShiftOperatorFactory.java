/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.test.lib.jittester.factories;

import jdk.test.lib.Pair;
import jdk.test.lib.jittester.OperatorKind;
import jdk.test.lib.jittester.ProductionFailedException;
import jdk.test.lib.jittester.Type;
import jdk.test.lib.jittester.TypeList;
import jdk.test.lib.jittester.TypeUtil;
import jdk.test.lib.jittester.types.TypeInt;
import jdk.test.lib.jittester.types.TypeKlass;
import jdk.test.lib.jittester.types.TypeLong;
import jdk.test.lib.jittester.utils.PseudoRandom;

class BinaryShiftOperatorFactory extends BinaryOperatorFactory {
    BinaryShiftOperatorFactory(OperatorKind opKind, long complexityLimit, int operatorLimit,
            TypeKlass ownerClass, Type resultType, boolean exceptionSafe, boolean noconsts) {
        super(opKind, complexityLimit, operatorLimit, ownerClass, resultType, exceptionSafe, noconsts);
    }

    @Override
    protected boolean isApplicable(Type resultType) {
        return resultType.equals(new TypeInt()) || resultType.equals(new TypeLong());
    }

    @Override
    protected Pair<Type, Type> generateTypes() throws ProductionFailedException {
        Type leftType = resultType.equals(new TypeInt()) ? PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getBuiltInInt(), resultType)) : resultType;
        Type rightType = PseudoRandom.randomElement(TypeUtil.getImplicitlyCastable(TypeList.getBuiltInInt(), new TypeLong()));
        return new Pair<>(leftType, rightType);
    }
}
