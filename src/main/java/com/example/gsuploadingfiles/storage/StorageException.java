package com.example.gsuploadingfiles.storage;

public class StorageException extends RuntimeException{

    // 에러 메시지만 전달하는 생성자
    public StorageException(String message){
        super(message);
    }

    // 에러 메시지 + 예외 이유를 함께 전달하는 생성자
    public StorageException(String message, Throwable cause){
        super(message, cause);
    }
}
