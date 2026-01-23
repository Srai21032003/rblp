package com.internship.rblp.repository;

import com.internship.rblp.models.entities.BulkUpload;
import com.internship.rblp.models.entities.BulkUploadError;
import io.ebean.DB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BulkUploadRepository {

    public void save(BulkUpload upload) {
        DB.save(upload);
    }

    public void saveError(BulkUploadError error) {
        DB.save(error);
    }

    public Optional<BulkUpload> findById(UUID id) {
        return Optional.ofNullable(DB.find(BulkUpload.class).setId(id).findOne());
    }

    // Fetch errors for a specific upload
    public List<BulkUploadError> findErrorsByUploadId(UUID uploadId) {
        return DB.find(BulkUploadError.class)
                .where().eq("bulkUpload.id", uploadId)
                .findList();
    }
}