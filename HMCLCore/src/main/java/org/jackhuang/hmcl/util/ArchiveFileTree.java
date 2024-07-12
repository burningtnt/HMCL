/*
 * Hello Minecraft! Launcher
 * Copyright (C) 2024 huangyuhui <huanghongxun2008@126.com> and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl.util;

import org.apache.commons.compress.archivers.ArchiveEntry;

import java.io.IOException;
import java.util.*;

/**
 * @author Glavo
 */
public final class ArchiveFileTree<F, E extends ArchiveEntry> {

    private final F file;
    private final Dir<E> root = new Dir<>();

    public ArchiveFileTree(F file) {
        this.file = file;
    }

    public F getFile() {
        return file;
    }

    public Dir<E> getRoot() {
        return root;
    }

    public void addEntry(E entry) throws IOException {
        addEntry(entry, Arrays.asList(entry.getName().split("/")));
    }

    public void addEntry(E entry, List<String> path) throws IOException {
        for (String item : path) {
            if (item.isEmpty() || item.equals(".") || item.equals("..")) {
                throw new IOException("Invalid name: " + entry.getName());
            }
        }

        Dir<E> dir = root;

        for (int i = 0, end = entry.isDirectory() ? path.size() : path.size() - 1; i < end; i++) {
            String item = path.get(i);

            if (dir.files.containsKey(item)) {
                throw new IOException("A file and a directory have the same name: " + entry.getName());
            }

            dir = dir.subDirs.computeIfAbsent(item, name -> new Dir<>());
        }

        if (entry.isDirectory()) {
            if (dir.entry != null) {
                throw new IOException("Duplicate entry: " + entry.getName());
            }
            dir.entry = entry;
        } else {
            String fileName = path.get(path.size() - 1);

            if (dir.subDirs.containsKey(fileName)) {
                throw new IOException("A file and a directory have the same name: " + entry.getName());
            }

            if (dir.files.containsKey(fileName)) {
                throw new IOException("Duplicate entry: " + entry.getName());
            }

            dir.files.put(fileName, entry);
        }
    }

    public static final class Dir<E extends ArchiveEntry> {
        E entry;

        final Map<String, Dir<E>> subDirs = new HashMap<>();
        final Map<String, E> files = new HashMap<>();
    }
}
