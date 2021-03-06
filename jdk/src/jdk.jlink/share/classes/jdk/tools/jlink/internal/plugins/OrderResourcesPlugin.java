/*
 * Copyright (c) 2015, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
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
package jdk.tools.jlink.internal.plugins;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.ToIntFunction;
import jdk.tools.jlink.plugin.PluginException;
import jdk.tools.jlink.plugin.Pool;
import jdk.tools.jlink.plugin.Pool.ModuleData;
import jdk.tools.jlink.plugin.Pool.ModuleDataType;
import jdk.tools.jlink.plugin.TransformerPlugin;
import jdk.tools.jlink.internal.Utils;

/**
 *
 * Order Resources plugin
 */
public final class OrderResourcesPlugin implements TransformerPlugin {
    public static final String NAME = "order-resources";
    private final List<ToIntFunction<String>> filters;
    private final Map<String, Integer> orderedPaths;

    public OrderResourcesPlugin() {
        this.filters = new ArrayList<>();
        this.orderedPaths = new HashMap<>();
    }

    @Override
    public String getName() {
        return NAME;
    }

    static class SortWrapper {
        private final ModuleData resource;
        private final int ordinal;

        SortWrapper(ModuleData resource, int ordinal) {
            this.resource = resource;
            this.ordinal = ordinal;
        }

        ModuleData getResource() {
            return resource;
        }

        String getPath() {
            return resource.getPath();
        }

        int getOrdinal() {
            return ordinal;
        }
    }

    private String stripModule(String path) {
        if (path.startsWith("/")) {
            int index = path.indexOf('/', 1);

            if (index != -1) {
                return path.substring(index + 1);
            }
        }

        return path;
    }

    private int getOrdinal(ModuleData resource) {
        String path = resource.getPath();

        Integer value = orderedPaths.get(stripModule(path));

        if (value != null) {
            return value;
        }

        for (ToIntFunction<String> function : filters) {
            int ordinal = function.applyAsInt(path);

            if (ordinal != Integer.MAX_VALUE) {
                return ordinal;
            }
        }

        return Integer.MAX_VALUE;
    }

    private static int compare(SortWrapper wrapper1, SortWrapper wrapper2) {
        int compare = wrapper1.getOrdinal() - wrapper2.getOrdinal();

        if (compare != 0) {
            return compare;
        }

        return wrapper1.getPath().compareTo(wrapper2.getPath());
    }

    @Override
    public void visit(Pool in, Pool out) {
        in.getContent().stream()
                .filter(resource -> resource.getType()
                        .equals(ModuleDataType.CLASS_OR_RESOURCE))
                .map((resource) -> new SortWrapper(resource, getOrdinal(resource)))
                .sorted(OrderResourcesPlugin::compare)
                .forEach((wrapper) -> out.add(wrapper.getResource()));
        in.getContent().stream()
                .filter(other -> !other.getType()
                        .equals(ModuleDataType.CLASS_OR_RESOURCE))
                .forEach((other) -> out.add(other));
    }

    @Override
    public Set<PluginType> getType() {
        Set<PluginType> set = new HashSet<>();
        set.add(CATEGORY.SORTER);

        return Collections.unmodifiableSet(set);
    }

    @Override
    public String getDescription() {
        return PluginsResourceBundle.getDescription(NAME);
    }

    @Override
    public boolean hasArguments() {
        return true;
    }

    @Override
    public String getArgumentsDescription() {
       return PluginsResourceBundle.getArgument(NAME);
    }

    @Override
    public void configure(Map<String, String> config) {
        String val = config.get(NAME);
        String[] patterns = Utils.listParser.apply(val);
        int ordinal = 0;

        for (String pattern : patterns) {
            if (pattern.startsWith("@")) {
                File file = new File(pattern.substring(1));

                if (file.exists()) {
                    List<String> lines;

                    try {
                        lines = Files.readAllLines(file.toPath());
                    } catch (IOException ex) {
                        throw new PluginException(ex);
                    }

                    for (String line : lines) {
                        if (!line.startsWith("#")) {
                            orderedPaths.put(line + ".class", ordinal++);
                        }
                    }
                }
            } else {
                boolean endsWith = pattern.startsWith("*");
                boolean startsWith = pattern.endsWith("*");
                ToIntFunction<String> function;
                final int result = ordinal++;

                if (startsWith && endsWith) {
                    final String string = pattern.substring(1, pattern.length() - 1);
                    function = (path)-> path.contains(string) ? result : Integer.MAX_VALUE;
                } else if (startsWith) {
                    final String string = pattern.substring(0, pattern.length() - 1);
                    function = (path)-> path.startsWith(string) ? result : Integer.MAX_VALUE;
                } else if (endsWith) {
                    final String string = pattern.substring(1);
                    function = (path)-> path.endsWith(string) ? result : Integer.MAX_VALUE;
                } else {
                    final String string = pattern;
                    function = (path)-> path.equals(string) ? result : Integer.MAX_VALUE;
                }

                filters.add(function);
            }
        }
    }
}
