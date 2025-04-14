package com.example.retry;

import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;


@SpringBootApplication
public class RetryReplayFrameworkApplication {
	private static final Logger logger = LoggerFactory.getLogger(RetryReplayFrameworkApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(RetryReplayFrameworkApplication.class, args);
	}

	@Bean
	public SpringBeanJobFactory springBeanJobFactory(ApplicationContext applicationContext) {
		SpringBeanJobFactory jobFactory = new SpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		logger.info("Configured SpringBeanJobFactory");
		return jobFactory;
	}

	@Bean
	public SchedulerFactoryBean schedulerFactoryBean(@Autowired SpringBeanJobFactory springBeanJobFactory) {
		SchedulerFactoryBean factory = new SchedulerFactoryBean();
		factory.setJobFactory(springBeanJobFactory);
		factory.setWaitForJobsToCompleteOnShutdown(true);
		factory.setStartupDelay(5); // Delay to ensure context is ready
		logger.info("Configured SchedulerFactoryBean with SpringBeanJobFactory");
		return factory;
	}

	@Bean
	public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws Exception {
		Scheduler scheduler = schedulerFactoryBean.getScheduler();
		logger.info("Scheduler initialized: {}", scheduler.getSchedulerName());
		scheduler.start();
		return scheduler;
	}
}