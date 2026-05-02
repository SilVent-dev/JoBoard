package br.com.joboard;

import br.com.joboard.configuracao.SupabaseProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SupabaseProperties.class)
public class JoboardApplication {

	public static void main(String[] args) {
		SpringApplication.run(JoboardApplication.class, args);
	}

}
