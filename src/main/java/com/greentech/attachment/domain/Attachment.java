package com.greentech.attachment.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * 첨부파일 메타데이터 - 바이너리는 스토리지 보관, DB 는 경로만 유지
 *
 * NOTE: 증명서·자격증 스캔본·사진 약 40GB 규모 - DB 저장 대상 아님
 * NOTE: owner_type + owner_id 다형 참조 - FK 제약 미설정
 */
@Entity
@Table(name = "attachment")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Attachment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false, length = 30)
    private OwnerType ownerType;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_path", nullable = false, length = 500)
    private String storedPath;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    @Builder.Default
    private long fileSize = 0L;

    /** SHA-256. 중복 업로드 판별·무결성 확인용 */
    @Column(length = 64)
    private String checksum;

    public enum OwnerType {
        EMPLOYEE, CERTIFICATE, LEAVE_REQUEST, PAYSLIP
    }

    public enum Category {
        PROFILE_PHOTO, CERTIFICATE_SCAN, PROOF_DOCUMENT, ETC
    }
}
