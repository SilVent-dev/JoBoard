package br.com.joboard.servico;

import br.com.joboard.configuracao.SupabaseProperties;
import lombok.RequiredArgsConstructor;
import java.net.http.HttpRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StorageServico {

    private final SupabaseProperties supabaseProperties;

    private static final long TAMANHO_MAXIMO_BYTES = 5 * 1024 * 1024L;
    private static final String TIPO_ACEITO = "application/pdf";

    public String upload(MultipartFile arquivo, UUID usuarioId) {
        validarArquivo(arquivo);

        String path = "curriculos/" + usuarioId + "/" + UUID.randomUUID() + ".pdf";
        String endpoint = supabaseProperties.getUrl()
                + "/storage/v1/object/"
                + supabaseProperties.getStorage().getBucket()
                + "/"
                + path;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + supabaseProperties.getServiceRoleKey())
                    .header("Content-Type", "application/pdf")
                    .PUT(HttpRequest.BodyPublishers.ofByteArray(arquivo.getBytes()))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Falha no upload: " + response.body());
            }

            return endpoint; // URL salva no banco

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao fazer upload do arquivo", e);
        }
    }

    public void deletar(String urlArquivo) {
        String prefix = "/storage/v1/object/"
                + supabaseProperties.getStorage().getBucket() + "/";
        int index = urlArquivo.indexOf(prefix);
        if (index == -1) {
            throw new RuntimeException("URL de arquivo inválida: " + urlArquivo);
        }
        String path = urlArquivo.substring(index + prefix.length());

        String endpoint = supabaseProperties.getUrl()
                + "/storage/v1/object/"
                + supabaseProperties.getStorage().getBucket()
                + "/"
                + path;

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer " + supabaseProperties.getServiceRoleKey())
                    .DELETE()
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Falha ao deletar: " + response.body());
            }

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Erro ao deletar arquivo", e);
        }
    }

    private void validarArquivo(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo não pode ser vazio.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO_BYTES) {
            throw new IllegalArgumentException("Arquivo excede o tamanho máximo de 5MB.");
        }
        if (!TIPO_ACEITO.equals(arquivo.getContentType())) {
            throw new IllegalArgumentException("Apenas arquivos PDF são aceitos.");
        }
    }
}