/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
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

/*
 * @test
 * @bug 8032884
 * @summary Globalbindings optionalProperty="primitive" does not work when minOccurs=0
 * @run shell compile-schema.sh
 * @compile -addmods java.xml.bind XjcOptionalPropertyTest.java
 * @run main/othervm XjcOptionalPropertyTest
 */

import java.io.IOException;
import java.lang.reflect.Method;

public class XjcOptionalPropertyTest {

    public static void main(String[] args) throws IOException {

        generated.Foo foo = new generated.Foo();
        log("foo = " + foo);
        Method[] methods = foo.getClass().getMethods();
        log("Found [" + methods.length + "] methods");
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (method.getName().equals("setFoo")) {
                log("Checking method [" + method.getName() + "]");
                Class[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1)
                    fail("more than 1 parameter");
                if (!parameterTypes[0].isPrimitive()) {
                    fail("Found [" + parameterTypes[0].getName() + "], but expected primitive!");
                }
                break;
            }
        }
        log("TEST PASSED.");

    }

    private static void fail(String message) {
        throw new RuntimeException(message);
    }

    private static void log(String msg) {
        System.out.println(msg);
    }

}
