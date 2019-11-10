package com.windchopper.fs;

import java.nio.file.DirectoryStream;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

public class SftpDirectoryStream implements DirectoryStream<Path> {

    private final List<Path> paths;

    SftpDirectoryStream(List<Path> paths) {
        this.paths = paths;
    }

    @Override public Iterator<Path> iterator() {
        return paths.iterator();
    }

    @Override public void close() {
        // nothing to do
    }

}
