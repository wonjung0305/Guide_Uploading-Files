package com.example.gsuploadingfiles.storage;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    // StorageProperties 객체에서 설정된 저장 경로를 읽어옴
    @Autowired
    public FileSystemStorageService(StorageProperties properties){
        if(properties.getLocation().trim().length() == 0){
            throw new StorageException("File upload location can not be Empty.");
        }

        this.rootLocation = Paths.get(properties.getLocation());
    }

    // 파일 저장 메서드
    @Override
    public void store(MultipartFile file){
        try{
            // 파일 비었는지 확인
            if (file.isEmpty()){
                throw new StorageException("Failed to store empty file.");
            }

            // 저장될 최종 파일 경로 조립(루트 경로 + 업로드할 파일명
            Path destinationFile = this.rootLocation.resolve(
                    Paths.get(file.getOriginalFilename()))
                    .normalize().toAbsolutePath();

            // 디렉토리가 이탈하는 경우, 최종 파일의 부모 경로가 지정된 루트 경로와 다르면 에러
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())){
                throw new StorageException("Cannot store file outside current directory.");
            }

            // 파일 스트림 열고, 작업 끝나면 close
            try(InputStream inputStream = file.getInputStream()){
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        }catch (IOException e){
            throw new StorageException("Failed to store file.", e);
        }
    }

    // 모든 파일 목록 조회 메서드
    @Override
    public Stream<Path> loadAll(){
        try{
            return Files.walk(this.rootLocation, 1)
                    .filter(path -> !path.equals(this.rootLocation))   // 루트 폴더 제외
                    .map(this.rootLocation::relativize);   // 상대 경로로 반환
        }catch (IOException e){
            throw new StorageException("Failed to read stored files.", e);
        }
    }

    @Override
    public Path load(String filename){
        // rootLocation 경로 뒤에 filename을 덧붙임
        return rootLocation.resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename){
        try{
            Path file = load(filename);

            // Path 객체를 URI로 바꾼 뒤 스프링 UrlResource 객체로 생성
            Resource resource = new UrlResource(file.toUri());

            // 파일이 실제로 존재하거나 읽을 수 있는 권한이 있는지 확인
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new StorageFileNotFoundException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        // 루트 폴더와 그 하위의 모든 파일/폴더 삭제
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    // 저장소 초기화/폴더 생성 메서드
    @Override
    public void init() {
        try {
            // 애플리케이션 시작 시, "upload-dir" 폴더가 없으면 실제 디스크에 생성함
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }

}
