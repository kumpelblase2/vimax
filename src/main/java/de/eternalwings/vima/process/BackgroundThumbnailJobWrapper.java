package de.eternalwings.vima.process;

import org.jobrunr.scheduling.JobScheduler;

import de.eternalwings.vima.job.BackgroundThumbnailJob;

public class BackgroundThumbnailJobWrapper {
    public static void scheduleThumbnailCleanupJob(JobScheduler jobScheduler, String recurringExpression) {
        jobScheduler.<BackgroundThumbnailJob>scheduleRecurrently(job -> job.execute(), recurringExpression);
    }
}
