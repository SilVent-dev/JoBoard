package br.com.joboard.configuracao;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "supabase")
@Getter
@Setter
public class SupabaseProperties {

    private String url;
    private String serviceRoleKey;
    private Storage storage = new Storage();

    @Getter
    @Setter
    public static class Storage {
        private String bucket;
    }
}