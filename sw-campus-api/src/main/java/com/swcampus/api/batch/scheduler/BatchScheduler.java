package com.swcampus.api.batch.scheduler;

import java.time.LocalDateTime;

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

    // 매일 자정 실행 (0 0 0 * * *)
    @Scheduled(cron = "0 0 0 * * *")
    public void runLectureStatusUpdateJob() {
        try {
            log.info("Batch Scheduler Started at {}", LocalDateTime.now());
            
            JobParameters jobParameters = new JobParametersBuilder()
                .addString("datetime", LocalDateTime.now().toString())
                .toJobParameters();

            jobLauncher.run(lectureStatusUpdateJob, jobParameters);
            
        } catch (Exception e) {
            log.error("Lecture Status Update Batch Job Failed at {}", LocalDateTime.now(), e);
        }
    }
}
