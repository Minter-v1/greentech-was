package com.greentech.attachment.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    StoredFile store(String objectKey, MultipartFile file);

    Resource load(String objectKey);

    void delete(String objectKey);

    record StoredFile(String objectKey, String checksum) {
    }
}
