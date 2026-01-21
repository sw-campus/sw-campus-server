package com.swcampus.api.batch.scheduler;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job lectureStatusUpdateJob;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    // 매일 새벽 4시 실행 (KST)
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void runLectureStatusUpdateJob() {
        try {
            LocalDateTime now = LocalDateTime.now(KST);
            log.info("Batch Scheduler Started at {} (KST)", now);

            JobParameters jobParameters = new JobParametersBuilder()
                .addString("datetime", now.toString())
                .toJobParameters();

            jobLauncher.run(lectureStatusUpdateJob, jobParameters);

        } catch (Exception e) {
            log.error("Lecture Status Update Batch Job Failed at {} (KST)", LocalDateTime.now(KST), e);
        }
    }
}
