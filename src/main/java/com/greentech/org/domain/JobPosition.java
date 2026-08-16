package com.greentech.org.domain;

import com.greentech.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/** 직위. 테이블명은 MySQL 예약어 회피용 job_position */
@Entity
@Table(name = "job_position")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JobPosition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20, unique = true)
    private String code;

    @Column(nullable = false, length = 50)
    private String name;

    /** 값이 클수록 상위 직위 */
    @Column(name = "level_no", nullable = false)
    private int levelNo;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
