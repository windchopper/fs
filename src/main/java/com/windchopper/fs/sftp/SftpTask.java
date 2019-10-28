package com.windchopper.fs.sftp;

import com.github.windchopper.common.util.stream.FailableConsumer;
import org.apache.commons.lang3.Functions;

import java.io.IOException;

public abstract class SftpTask<T> implements FailableConsumer<T, IOException>, Comparable<SftpTask<T>> {

    private final int weight;

    public SftpTask(int weight) {
        this.weight = weight;
    }

    @Override public int compareTo(SftpTask<T> task) {
        return Integer.compare(weight, task.weight);
    }

}
