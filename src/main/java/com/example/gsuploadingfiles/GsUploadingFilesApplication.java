package com.example.gsuploadingfiles;

import com.example.gsuploadingfiles.storage.StorageProperties;
import com.example.gsuploadingfiles.storage.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class) // storage.location 값을 읽을 수 있도록
public class GsUploadingFilesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GsUploadingFilesApplication.class, args);
    }

    // 스프링 부트 애플리케이션이 서버에 완전히 구동된 직후 구동되는 특수 인터페이스
    @Bean
    CommandLineRunner init(StorageService storageService){
        // 람다 표현식
        return (args) -> {
            storageService.deleteAll(); // 애프리케이션 시작 시, 기존 upload-dir 폴더 및 내부 파일 모두 삭제
            storageService.init(); // upload-dir 폴더 새로 생성
        };
    }
}
