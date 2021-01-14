package de.eternalwings.vima.process;

import org.jobrunr.scheduling.JobScheduler;

import java.nio.file.Path;

import de.eternalwings.vima.job.BackgroundImportJob;

public class RunImportJobWrapper {
    public static void enqueueImportJob(JobScheduler scheduler, Integer videoId, Path filePath) {
        scheduler.<BackgroundImportJob>enqueue(importJob -> importJob.execute(videoId, filePath));
    }
}
