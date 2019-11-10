package com.github.windchopper.fs;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

import static java.util.Collections.emptyMap;

public class Test {

    public static void main(String... args) throws IOException {
        URI uri = URI.create("sftp://oracle:Oracle33@vs-c06-szp-test-app.otr.ru");
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, emptyMap())) {
            Path homePath = fileSystem.getPath("/home/oracle");
            System.out.printf("home: %s%n", homePath);

            Path parentPath = homePath.getParent();
            System.out.printf("parent: %s%n", parentPath);

            Path homeRootPath = homePath.getRoot();
            System.out.printf("root: %s%n", homeRootPath);

            Path nonAbsoluteHomePath = fileSystem.getPath("oracle");
            System.out.printf("non-absolute home: %s%n", nonAbsoluteHomePath);

            System.out.printf("home starts with parent: %s%n", homePath.startsWith(parentPath));
            System.out.printf("home starts with root: %s%n", homePath.startsWith(homeRootPath));
            System.out.printf("parent starts with home: %s%n", parentPath.startsWith(homePath));
            System.out.printf("home ends with non-absolute home: %s%n", homePath.endsWith(nonAbsoluteHomePath));
            System.out.printf("non-absolute home ends with home: %s%n", nonAbsoluteHomePath.endsWith(homePath));

            System.out.printf("%n");

            for (Path rootPath : fileSystem.getRootDirectories()) {
                list(rootPath);
            }
        }
    }

    static void list(Path path) throws IOException {
        for (Iterator<Path> i = Files.list(path).iterator(); i.hasNext(); ) {
            Path childPath = i.next();
            System.out.printf("%s%n", childPath);
            if (Files.isDirectory(childPath)) {
                list(childPath);
            }
        }
    }

}
