package com.greentech.attachment.repository;

import com.greentech.attachment.domain.Attachment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {

    List<Attachment> findByOwnerTypeAndOwnerIdOrderByIdDesc(
            Attachment.OwnerType ownerType, Long ownerId);
}
