package com.github.windchopper.fs.sftp;

import com.github.windchopper.common.util.stream.FailableConsumer;
import com.github.windchopper.common.util.stream.FailableFunction;
import org.apache.commons.lang3.Functions;

import java.nio.file.Path;
import java.nio.file.ProviderMismatchException;
import java.util.logging.Logger;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

public interface SftpRoutines {

    Logger logger = Logger.getLogger(SftpRoutines.class.getPackageName());

    default <E extends Throwable> void checkPathAndAccept(Path path, FailableConsumer<SftpPath, E> consumer) throws E {
        if (path instanceof SftpPath) {
            consumer.accept((SftpPath) path);
        }

        throw foreignPath(path);
    }

    default <R, E extends Throwable> R checkPathAndApply(Path path, FailableFunction<SftpPath, R, E> function) throws E {
        if (path instanceof SftpPath) {
            return function.apply((SftpPath) path);
        }

        throw foreignPath(path);
    }

    default <R, E extends Throwable> R checkPathAndApply(Path path1st, Path path2nd, Functions.FailableBiFunction<SftpPath, SftpPath, R, E> function) throws E {
        if (path1st instanceof SftpPath && path2nd instanceof SftpPath) {
            return function.apply((SftpPath) path1st, (SftpPath) path2nd);
        }

        throw foreignPath(path1st, path2nd);
    }

    /*
     *
     */

    private ProviderMismatchException foreignPath(Path... paths) {
        return new ProviderMismatchException(stream(paths)
            .map(Object::getClass)
            .filter(type -> type != SftpPath.class)
            .map(Class::getCanonicalName)
            .collect(joining(", ", "Path of type(s) ", " is not belonging to used provider")));
    }

}
