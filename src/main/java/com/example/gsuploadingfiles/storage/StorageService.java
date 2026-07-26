package com.example.gsuploadingfiles.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    // 애플리케이션 시작 시 저장소를 초기화/생성
    void init();

    //  Multipart 객체를 디스크 저장소에 물리적으로 저장하는 메서드
    void store(MultipartFile file);

    // 저장소에 있는 모든 파일의 경로 목록을 Stream 형태로 조회하는 메서드
    Stream<Path> loadAll();

    // 파일 이름을 받아 상대 경로 객체를 반환하는 메서드
    Path load(String filename);

    // 파일 이름을 받아 Resource 객체(다운로드/읽기)로 로드하는 메서드
    Resource loadAsResource(String filename);

    // 저장소 모든 파일/폴더 삭제
    void deleteAll();
}
