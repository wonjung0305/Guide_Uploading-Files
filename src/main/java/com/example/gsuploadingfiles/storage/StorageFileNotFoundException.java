package com.example.gsuploadingfiles.storage;

import com.example.gsuploadingfiles.storage.StorageException;

public class StorageFileNotFoundException extends StorageException{

    public StorageFileNotFoundException(String message){
        super(message);
    }

    public StorageFileNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}
