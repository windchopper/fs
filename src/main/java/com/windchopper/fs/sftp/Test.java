package com.windchopper.fs.sftp;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;

import static java.util.Collections.emptyMap;

public class Test {

    public static void main(String... args) throws IOException {
        URI uri = URI.create("sftp://oracle:Oracle33@vs-c06-szp-test-app.otr.ru");
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, emptyMap())) {
            fileSystem.getRootDirectories()
                .forEach(System.out::println);
        }
    }

}
