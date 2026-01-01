package com.swcampus.api.batch.job;

import com.swcampus.domain.lecture.LectureRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class LectureJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final LectureRepository lectureRepository;

    @Bean
    public Job lectureStatusUpdateJob() {
        return new JobBuilder("lectureStatusUpdateJob", jobRepository)
            .start(lectureStatusUpdateStep())
            .build();
    }

    @Bean
    public Step lectureStatusUpdateStep() {
        return new StepBuilder("lectureStatusUpdateStep", jobRepository)
            .tasklet((contribution, chunkContext) -> {
                log.info(">>>>> Start lectureStatusUpdateStep");

                LocalDateTime now = LocalDateTime.now();
                int closedCount = lectureRepository.closeExpiredLectures(now);

                log.info("Closed {} expired lectures.", closedCount);
                log.info(">>>>> End lectureStatusUpdateStep");
                return RepeatStatus.FINISHED;
            }, transactionManager)
            .build();
    }
}
