package com.example.gsuploadingfiles.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;

// application.properties 또는 application.yml 파일에서 storage 접두사로 시작하는 설정값들을
// 이 필드에 1:1로 자동 매핑(바인딩) 해줌
@ConfigurationProperties("storage")
public class StorageProperties {

    // 파일이 저장될 위치 (기본값 = upload-dir")
    private String location = "upload-dir";

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
