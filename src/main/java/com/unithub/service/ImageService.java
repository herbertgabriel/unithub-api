package com.unithub.service;

import com.unithub.Exceptions.ImageDeletionException;
import com.unithub.Exceptions.ImageUploadException;
import com.unithub.model.Event;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    private final S3Client s3Client;
    @Value("${aws.bucket-name}")
    private String bucketName;
    @Value("${aws.endpoint}")
    private String endpoint;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_FILE_TYPES = List.of(
            "image/jpeg", "image/png", "image/svg+xml", "image/webp");
    private static final int MAX_IMAGES_PER_EVENT = 4;

    public ImageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadImage(MultipartFile file, Event event) throws ImageUploadException {
        try {
            // Validações iniciais
            validateFile(file);
            validateEventImageLimit(event);

            // Gera o nome do arquivo no S3
            String fileName = generateFileName(event);

            // Faz o upload para o S3
            uploadToS3(file, fileName);

            // Retorna a URL completa da imagem
            return buildImageUrl(fileName);
        } catch (IOException e) {
            throw new ImageUploadException("Falha ao processar o arquivo de imagem", e);
        } catch (S3Exception e) {
            throw new ImageUploadException("Falha ao enviar a imagem para o S3", e);
        } catch (Exception e) {
            throw new ImageUploadException("Erro inesperado ao fazer upload da imagem", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("O arquivo está vazio");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException(
                    "O arquivo excede o tamanho máximo permitido de 10 MB.");
        }

        if (!ALLOWED_FILE_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException(
                    "Tipo de arquivo não suportado. Apenas JPEG, PNG, SVG e WEBP são permitidos.");
        }
    }

    private void validateEventImageLimit(Event event) {
        if (event.getImages().size() >= MAX_IMAGES_PER_EVENT) {
            throw new IllegalArgumentException(
                    "O limite de " + MAX_IMAGES_PER_EVENT + " imagens por evento foi atingido.");
        }
    }

    private String generateFileName(Event event) {
        return String.format("%s/%d-%s",
                event.getEventId(),
                event.getImages() != null ? event.getImages().size() : 0,
                UUID.randomUUID().toString());
    }

    private void uploadToS3(MultipartFile file, String fileName) throws IOException {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        s3Client.putObject(putObjectRequest,
                RequestBody.fromBytes(file.getBytes()));
    }

    private String buildImageUrl(String fileName) {
        return String.format("%s/%s/%s", endpoint, bucketName, fileName);
    }

    public void deleteAllEventImages(Event event) {
        if (event == null || event.getImages() == null || event.getImages().isEmpty()) {
            return;
        }

        event.getImages().forEach(imageUrl -> {
            try {
                deleteImageFromS3(imageUrl);
            } catch (ImageDeletionException e) {
                // Continua tentando deletar as outras imagens
            }
        });

        event.getImages().clear();
    }

    public void deleteEventImage(Event event, String imageUrl) {
        if (event == null || imageUrl == null || !event.getImages().contains(imageUrl)) {
            return;
        }

        try {
            deleteImageFromS3(imageUrl);
            event.getImages().remove(imageUrl);
        } catch (ImageDeletionException e) {
            throw new RuntimeException("Não foi possível remover a imagem", e);
        }
    }

    private void deleteImageFromS3(String imageUrl) throws ImageDeletionException {
        try {
            // Extrai a chave do objeto do S3 da URL completa
            String objectKey = extractObjectKeyFromUrl(imageUrl);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build());
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return; // Não é erro crítico se a imagem já não existir
            }
            throw new ImageDeletionException("Erro ao deletar imagem do S3", e);
        }
    }

    private String extractObjectKeyFromUrl(String imageUrl) {
        // Remove o endpoint e o nome do bucket da URL
        String prefix = endpoint + "/" + bucketName + "/";
        if (imageUrl.startsWith(prefix)) {
            return imageUrl.substring(prefix.length());
        }
        throw new IllegalArgumentException("URL da imagem não corresponde ao padrão esperado");
    }
}