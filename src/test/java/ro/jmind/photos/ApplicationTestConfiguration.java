package ro.jmind.photos;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;


@Configuration
@PropertySource("classpath:application-test.properties")
@ComponentScan(basePackages = {"ro.jmind.photos.*"})
public class ApplicationTestConfiguration {

//    @Profile("test")
//    @Bean
//    public ExcelService excelService() {
//        return Mockito.mock(ExcelService.class);
//    }

}
