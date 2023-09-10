package com.roshini.spring.batchprocessing.importcsv.config;

import com.roshini.spring.batchprocessing.importcsv.entity.Customer;
import com.roshini.spring.batchprocessing.importcsv.repository.CustomerRepository;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableBatchProcessing
public class BatchProcessingConfig {

    private final PlatformTransactionManager transactionManager;

    private final DataSource dataSource;

    private final CustomerRepository customerRepository;

    @Autowired
    public BatchProcessingConfig(PlatformTransactionManager transactionManager, DataSource dataSource, CustomerRepository customerRepository) {
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
        this.customerRepository = customerRepository;
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.setDatabaseType("MYSQL");
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public Step step1() throws Exception {
        return new StepBuilder("csv-step")
                .repository(jobRepository())
                .<Customer, Customer>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor())
                .transactionManager(this.transactionManager)
                .build();
    }

    @Bean
    public Job runJob() throws Exception {
        return new JobBuilder("importCustomers")
                .incrementer(new RunIdIncrementer())
                .repository(jobRepository())
                .flow(step1()).end().build();

    }

    @Bean
    public TaskExecutor taskExecutor() {
        SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor();
        asyncTaskExecutor.setConcurrencyLimit(10);
        return asyncTaskExecutor;
    }

    @Bean
    public FlatFileItemReader<Customer> reader() {
        FlatFileItemReader<Customer> itemReader = new FlatFileItemReader<>();
        itemReader.setResource(new FileSystemResource("src/main/resources/customer.csv"));
        itemReader.setName("CsvFileReader");
        itemReader.setLinesToSkip(1);
        itemReader.setLineMapper(csvFileLineMapper());
        return itemReader;
    }

    private LineMapper<Customer> csvFileLineMapper() {
        DefaultLineMapper<Customer> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");
        tokenizer.setStrict(false);
        tokenizer.setNames("id", "firstName", "lastName", "email", "gender", "contactNo", "country", "dob");

        BeanWrapperFieldSetMapper<Customer> fieldSetMapper = new BeanWrapperFieldSetMapper<>();
        fieldSetMapper.setTargetType(Customer.class);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(fieldSetMapper);

        return lineMapper;
    }

    @Bean
    public CustomerProcessor processor() {
        return new CustomerProcessor();
    }

    @Bean
    public RepositoryItemWriter<Customer> writer() {
        RepositoryItemWriter<Customer> writer = new RepositoryItemWriter<>();
        writer.setRepository(customerRepository);
        writer.setMethodName("save");
        return writer;
    }
}
