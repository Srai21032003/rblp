package com.internship.rblp.service;

import io.reactivex.rxjava3.core.Single;
import io.vertx.rxjava3.core.Vertx;
import io.vertx.rxjava3.ext.web.FileUpload;

import java.io.File;
import java.nio.file.Paths;
import java.util.UUID;

public class FileStorageService {
    private final Vertx vertx;

    private static final String UPLOAD_BASE_DIR = "uploads" + File.separator + "kyc";

    public FileStorageService(Vertx vertx) {
        this.vertx = vertx;
    }

    public Single<String> saveFile(FileUpload fileUpload, String userId){
        return Single.fromCallable(()->{
           String originalName = fileUpload.fileName();
           String extension = "";
           int i = originalName.lastIndexOf('.');
           if(i>0){
               extension = originalName.substring(i);
           }

           String newFileName = UUID.randomUUID().toString().concat(extension);

            String absoluteTargetDir = Paths.get(UPLOAD_BASE_DIR, userId).toAbsolutePath().toString();
            String absoluteTargetPath = Paths.get(absoluteTargetDir, newFileName).toAbsolutePath().toString();

            return new FilePaths(absoluteTargetDir, absoluteTargetPath);
        }).flatMap(paths ->
            vertx.fileSystem().mkdirs(paths.dir)
                    .andThen(
                            vertx.fileSystem().move(fileUpload.uploadedFileName(), paths.fullPath)
                    ).toSingle(()-> paths.fullPath)
        );
    }

    private static class FilePaths{
        String dir;
        String fullPath;
        FilePaths(String d, String f){
            dir = d;
            fullPath = f;
        }
    }

}
