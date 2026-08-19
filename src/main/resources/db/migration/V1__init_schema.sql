-- MARK: 기준정보

CREATE TABLE department (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(20)  NOT NULL COMMENT '부서코드',
    name        VARCHAR(100) NOT NULL COMMENT '부서명',
    parent_id   BIGINT       NULL COMMENT '상위 부서',
    sort_order  INT          NOT NULL DEFAULT 0,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(50)  NULL,
    updated_at  DATETIME(6)  NOT NULL,
    updated_by  VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_department_code (code),
    KEY ix_department_parent (parent_id),
    CONSTRAINT fk_department_parent FOREIGN KEY (parent_id) REFERENCES department (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '부서';

CREATE TABLE job_position (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(20)  NOT NULL COMMENT '직위코드',
    name        VARCHAR(50)  NOT NULL COMMENT '직위명 (사원/대리/과장...)',
    level_no    INT          NOT NULL COMMENT '직위 서열 (숫자가 클수록 상위)',
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(50)  NULL,
    updated_at  DATETIME(6)  NOT NULL,
    updated_by  VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_job_position_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '직위';

CREATE TABLE leave_type (
    id                 BIGINT       NOT NULL AUTO_INCREMENT,
    code               VARCHAR(20)  NOT NULL COMMENT '휴가코드',
    name               VARCHAR(50)  NOT NULL COMMENT '휴가명',
    paid               BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '유급 여부',
    deduct_annual      BOOLEAN      NOT NULL DEFAULT TRUE COMMENT '연차에서 차감하는지',
    max_days_per_year  DECIMAL(5, 1) NULL COMMENT '연간 최대 사용일 (NULL = 제한없음)',
    active             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         DATETIME(6)  NOT NULL,
    created_by         VARCHAR(50)  NULL,
    updated_at         DATETIME(6)  NOT NULL,
    updated_by         VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_leave_type_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '휴가 종류';

CREATE TABLE pay_item (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    code        VARCHAR(30) NOT NULL COMMENT '항목코드',
    name        VARCHAR(50) NOT NULL COMMENT '항목명',
    item_type   VARCHAR(20) NOT NULL COMMENT 'EARNING(지급) / DEDUCTION(공제)',
    taxable     BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '과세 대상 여부',
    sort_order  INT         NOT NULL DEFAULT 0,
    active      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  DATETIME(6) NOT NULL,
    created_by  VARCHAR(50) NULL,
    updated_at  DATETIME(6) NOT NULL,
    updated_by  VARCHAR(50) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_pay_item_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '급여 항목 마스터';

-- MARK: 사원

CREATE TABLE employee (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    emp_no            VARCHAR(20)  NOT NULL COMMENT '사번',
    name              VARCHAR(50)  NOT NULL COMMENT '성명',
    name_en           VARCHAR(100) NULL,
    resident_no_enc   VARCHAR(512) NULL COMMENT '주민등록번호 (AES-GCM 암호화 저장)',
    birth_date        DATE         NULL,
    gender            VARCHAR(10)  NULL COMMENT 'MALE / FEMALE / OTHER',
    email             VARCHAR(120) NULL,
    department_id     BIGINT       NULL,
    job_position_id   BIGINT       NULL,
    manager_id        BIGINT       NULL COMMENT '직속 상급자',
    employment_type   VARCHAR(20)  NOT NULL COMMENT 'FULL_TIME / CONTRACT / PART_TIME / DISPATCH',
    status            VARCHAR(20)  NOT NULL COMMENT 'ACTIVE / ON_LEAVE / RESIGNED',
    hire_date         DATE         NOT NULL,
    resign_date       DATE         NULL,
    created_at        DATETIME(6)  NOT NULL,
    created_by        VARCHAR(50)  NULL,
    updated_at        DATETIME(6)  NOT NULL,
    updated_by        VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_emp_no (emp_no),
    KEY ix_employee_department (department_id),
    KEY ix_employee_status (status),
    KEY ix_employee_name (name),
    CONSTRAINT fk_employee_department FOREIGN KEY (department_id) REFERENCES department (id),
    CONSTRAINT fk_employee_position FOREIGN KEY (job_position_id) REFERENCES job_position (id),
    CONSTRAINT fk_employee_manager FOREIGN KEY (manager_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '사원';

CREATE TABLE employee_contact (
    id                  BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id         BIGINT       NOT NULL,
    mobile              VARCHAR(30)  NULL,
    tel                 VARCHAR(30)  NULL,
    zip_code            VARCHAR(10)  NULL,
    address1            VARCHAR(200) NULL,
    address2            VARCHAR(200) NULL,
    emergency_name      VARCHAR(50)  NULL COMMENT '비상연락처 성명',
    emergency_relation  VARCHAR(30)  NULL,
    emergency_phone     VARCHAR(30)  NULL,
    created_at          DATETIME(6)  NOT NULL,
    created_by          VARCHAR(50)  NULL,
    updated_at          DATETIME(6)  NOT NULL,
    updated_by          VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_contact_employee (employee_id),
    CONSTRAINT fk_employee_contact_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '사원 연락처';

CREATE TABLE employment_history (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id          BIGINT       NOT NULL,
    change_type          VARCHAR(30)  NOT NULL COMMENT 'HIRE / PROMOTION / TRANSFER / LEAVE_OF_ABSENCE / REINSTATE / RESIGN',
    effective_date       DATE         NOT NULL,
    before_department_id BIGINT       NULL,
    after_department_id  BIGINT       NULL,
    before_position_id   BIGINT       NULL,
    after_position_id    BIGINT       NULL,
    reason               VARCHAR(500) NULL,
    created_at           DATETIME(6)  NOT NULL,
    created_by           VARCHAR(50)  NULL,
    updated_at           DATETIME(6)  NOT NULL,
    updated_by           VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY ix_employment_history_employee (employee_id, effective_date),
    CONSTRAINT fk_employment_history_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '발령 이력';

CREATE TABLE education (
    id               BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id      BIGINT       NOT NULL,
    school_name      VARCHAR(100) NOT NULL,
    major            VARCHAR(100) NULL,
    degree           VARCHAR(20)  NOT NULL COMMENT 'HIGH_SCHOOL / ASSOCIATE / BACHELOR / MASTER / DOCTOR',
    admission_date   DATE         NULL,
    graduation_date  DATE         NULL,
    graduated        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at       DATETIME(6)  NOT NULL,
    created_by       VARCHAR(50)  NULL,
    updated_at       DATETIME(6)  NOT NULL,
    updated_by       VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY ix_education_employee (employee_id),
    CONSTRAINT fk_education_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '학력';

CREATE TABLE certificate (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id    BIGINT       NOT NULL,
    name           VARCHAR(100) NOT NULL COMMENT '자격증명',
    issuer         VARCHAR(100) NULL COMMENT '발급기관',
    license_no     VARCHAR(100) NULL,
    acquired_date  DATE         NULL,
    expiry_date    DATE         NULL,
    created_at     DATETIME(6)  NOT NULL,
    created_by     VARCHAR(50)  NULL,
    updated_at     DATETIME(6)  NOT NULL,
    updated_by     VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY ix_certificate_employee (employee_id),
    CONSTRAINT fk_certificate_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '자격증';

CREATE TABLE family_member (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    employee_id BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    relation    VARCHAR(30) NOT NULL COMMENT 'SPOUSE / CHILD / PARENT / SIBLING / OTHER',
    birth_date  DATE        NULL,
    dependent   BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '부양가족 여부 (연말정산)',
    cohabiting  BOOLEAN     NOT NULL DEFAULT FALSE COMMENT '동거 여부',
    created_at  DATETIME(6) NOT NULL,
    created_by  VARCHAR(50) NULL,
    updated_at  DATETIME(6) NOT NULL,
    updated_by  VARCHAR(50) NULL,
    PRIMARY KEY (id),
    KEY ix_family_member_employee (employee_id),
    CONSTRAINT fk_family_member_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '가족사항';

-- NOTE: owner_type 과 owner_id 로 다형 참조하므로 FK 제약 없음
CREATE TABLE attachment (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    owner_type     VARCHAR(30)  NOT NULL COMMENT 'EMPLOYEE / CERTIFICATE / LEAVE_REQUEST / PAYSLIP',
    owner_id       BIGINT       NOT NULL,
    category       VARCHAR(30)  NOT NULL COMMENT 'PROFILE_PHOTO / CERTIFICATE_SCAN / PROOF_DOCUMENT / ETC',
    original_name  VARCHAR(255) NOT NULL,
    stored_path    VARCHAR(500) NOT NULL COMMENT '스토리지 상대 경로',
    content_type   VARCHAR(100) NULL,
    file_size      BIGINT       NOT NULL DEFAULT 0,
    checksum       VARCHAR(64)  NULL COMMENT 'SHA-256',
    created_at     DATETIME(6)  NOT NULL,
    created_by     VARCHAR(50)  NULL,
    updated_at     DATETIME(6)  NOT NULL,
    updated_by     VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY ix_attachment_owner (owner_type, owner_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '첨부파일';

-- MARK: 근태

CREATE TABLE work_calendar (
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    calendar_date  DATE        NOT NULL,
    day_type       VARCHAR(20) NOT NULL COMMENT 'WORKDAY / WEEKEND / HOLIDAY',
    holiday_name   VARCHAR(50) NULL,
    created_at     DATETIME(6) NOT NULL,
    created_by     VARCHAR(50) NULL,
    updated_at     DATETIME(6) NOT NULL,
    updated_by     VARCHAR(50) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_work_calendar_date (calendar_date)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '근무 달력';

CREATE TABLE attendance (
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    employee_id       BIGINT      NOT NULL,
    work_date         DATE        NOT NULL,
    check_in_at       DATETIME(6) NULL,
    check_out_at      DATETIME(6) NULL,
    work_minutes      INT         NOT NULL DEFAULT 0 COMMENT '정규 근무 분',
    overtime_minutes  INT         NOT NULL DEFAULT 0 COMMENT '연장 근무 분',
    night_minutes     INT         NOT NULL DEFAULT 0 COMMENT '야간 근무 분 (22:00~06:00)',
    status            VARCHAR(20) NOT NULL COMMENT 'NORMAL / LATE / EARLY_LEAVE / ABSENT / ON_LEAVE / HOLIDAY',
    note              VARCHAR(255) NULL,
    created_at        DATETIME(6) NOT NULL,
    created_by        VARCHAR(50) NULL,
    updated_at        DATETIME(6) NOT NULL,
    updated_by        VARCHAR(50) NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_attendance_employee_date (employee_id, work_date),
    KEY ix_attendance_work_date (work_date),
    CONSTRAINT fk_attendance_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '일별 출퇴근';

CREATE TABLE leave_balance (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    employee_id    BIGINT        NOT NULL,
    leave_type_id  BIGINT        NOT NULL,
    year           INT           NOT NULL,
    granted_days   DECIMAL(5, 1) NOT NULL DEFAULT 0.0 COMMENT '부여일수',
    used_days      DECIMAL(5, 1) NOT NULL DEFAULT 0.0 COMMENT '사용일수',
    expires_on     DATE          NULL,
    created_at     DATETIME(6)   NOT NULL,
    created_by     VARCHAR(50)   NULL,
    updated_at     DATETIME(6)   NOT NULL,
    updated_by     VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_leave_balance (employee_id, leave_type_id, year),
    CONSTRAINT fk_leave_balance_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_leave_balance_type FOREIGN KEY (leave_type_id) REFERENCES leave_type (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '휴가 잔여';

CREATE TABLE leave_request (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    employee_id    BIGINT        NOT NULL,
    leave_type_id  BIGINT        NOT NULL,
    start_date     DATE          NOT NULL,
    end_date       DATE          NOT NULL,
    days           DECIMAL(5, 1) NOT NULL COMMENT '신청일수 (0.5 단위)',
    half_day       BOOLEAN       NOT NULL DEFAULT FALSE,
    reason         VARCHAR(500)  NULL,
    status         VARCHAR(20)   NOT NULL COMMENT 'REQUESTED / APPROVED / REJECTED / CANCELED',
    approver_id    BIGINT        NULL,
    approved_at    DATETIME(6)   NULL,
    reject_reason  VARCHAR(500)  NULL,
    created_at     DATETIME(6)   NOT NULL,
    created_by     VARCHAR(50)   NULL,
    updated_at     DATETIME(6)   NOT NULL,
    updated_by     VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    KEY ix_leave_request_employee (employee_id, start_date),
    KEY ix_leave_request_status (status),
    CONSTRAINT fk_leave_request_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_leave_request_type FOREIGN KEY (leave_type_id) REFERENCES leave_type (id),
    CONSTRAINT fk_leave_request_approver FOREIGN KEY (approver_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '휴가 신청';

CREATE TABLE overtime_request (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    employee_id   BIGINT       NOT NULL,
    work_date     DATE         NOT NULL,
    start_at      DATETIME(6)  NOT NULL,
    end_at        DATETIME(6)  NOT NULL,
    minutes       INT          NOT NULL,
    overtime_type VARCHAR(20)  NOT NULL COMMENT 'EXTENDED(연장) / NIGHT(야간) / HOLIDAY(휴일)',
    reason        VARCHAR(500) NULL,
    status        VARCHAR(20)  NOT NULL COMMENT 'REQUESTED / APPROVED / REJECTED / CANCELED',
    approver_id   BIGINT       NULL,
    approved_at   DATETIME(6)  NULL,
    created_at    DATETIME(6)  NOT NULL,
    created_by    VARCHAR(50)  NULL,
    updated_at    DATETIME(6)  NOT NULL,
    updated_by    VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    KEY ix_overtime_request_employee (employee_id, work_date),
    KEY ix_overtime_request_status (status),
    CONSTRAINT fk_overtime_request_employee FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_overtime_request_approver FOREIGN KEY (approver_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '연장근무 신청';

-- MARK: 급여

CREATE TABLE salary_contract (
    id              BIGINT         NOT NULL AUTO_INCREMENT,
    employee_id     BIGINT         NOT NULL,
    contract_no     VARCHAR(30)    NULL,
    pay_type        VARCHAR(20)    NOT NULL COMMENT 'ANNUAL(연봉제) / MONTHLY(월급제) / HOURLY(시급제)',
    annual_salary   DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '연봉 (원)',
    base_pay        DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '월 기본급 (원)',
    effective_from  DATE           NOT NULL,
    effective_to    DATE           NULL COMMENT 'NULL = 현재 유효',
    created_at      DATETIME(6)    NOT NULL,
    created_by      VARCHAR(50)    NULL,
    updated_at      DATETIME(6)    NOT NULL,
    updated_by      VARCHAR(50)    NULL,
    PRIMARY KEY (id),
    KEY ix_salary_contract_employee (employee_id, effective_from),
    CONSTRAINT fk_salary_contract_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '연봉계약';

CREATE TABLE deduction_rate (
    id              BIGINT        NOT NULL AUTO_INCREMENT,
    year            INT           NOT NULL,
    item_code       VARCHAR(30)   NOT NULL COMMENT 'pay_item.code 와 연결되는 공제 항목 코드',
    employee_rate   DECIMAL(8, 5) NOT NULL DEFAULT 0 COMMENT '근로자 부담 요율 (예: 0.04500)',
    employer_rate   DECIMAL(8, 5) NOT NULL DEFAULT 0 COMMENT '회사 부담 요율',
    description     VARCHAR(200)  NULL,
    created_at      DATETIME(6)   NOT NULL,
    created_by      VARCHAR(50)   NULL,
    updated_at      DATETIME(6)   NOT NULL,
    updated_by      VARCHAR(50)   NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_deduction_rate (year, item_code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '공제 요율';

CREATE TABLE payroll_run (
    id               BIGINT         NOT NULL AUTO_INCREMENT,
    pay_year_month   VARCHAR(7)     NOT NULL COMMENT 'YYYY-MM',
    status           VARCHAR(20)    NOT NULL COMMENT 'DRAFT / CALCULATED / CONFIRMED / CANCELED',
    pay_date         DATE           NULL COMMENT '지급일',
    target_count     INT            NOT NULL DEFAULT 0,
    total_gross      DECIMAL(18, 0) NOT NULL DEFAULT 0,
    total_deduction  DECIMAL(18, 0) NOT NULL DEFAULT 0,
    total_net        DECIMAL(18, 0) NOT NULL DEFAULT 0,
    executed_by      VARCHAR(50)    NULL,
    executed_at      DATETIME(6)    NULL,
    confirmed_at     DATETIME(6)    NULL,
    created_at       DATETIME(6)    NOT NULL,
    created_by       VARCHAR(50)    NULL,
    updated_at       DATETIME(6)    NOT NULL,
    updated_by       VARCHAR(50)    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payroll_run_year_month (pay_year_month)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '급여 정산 실행';

CREATE TABLE payslip (
    id                BIGINT         NOT NULL AUTO_INCREMENT,
    payroll_run_id    BIGINT         NOT NULL,
    employee_id       BIGINT         NOT NULL,
    emp_no            VARCHAR(20)    NOT NULL COMMENT '정산 시점 사번 스냅샷',
    employee_name     VARCHAR(50)    NOT NULL COMMENT '정산 시점 성명 스냅샷',
    department_name   VARCHAR(100)   NULL COMMENT '정산 시점 부서 스냅샷',
    position_name     VARCHAR(50)    NULL COMMENT '정산 시점 직위 스냅샷',
    work_days         INT            NOT NULL DEFAULT 0,
    overtime_minutes  INT            NOT NULL DEFAULT 0,
    gross_pay         DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '지급 합계',
    total_deduction   DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '공제 합계',
    net_pay           DECIMAL(15, 0) NOT NULL DEFAULT 0 COMMENT '실지급액',
    created_at        DATETIME(6)    NOT NULL,
    created_by        VARCHAR(50)    NULL,
    updated_at        DATETIME(6)    NOT NULL,
    updated_by        VARCHAR(50)    NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payslip_run_employee (payroll_run_id, employee_id),
    KEY ix_payslip_employee (employee_id),
    CONSTRAINT fk_payslip_run FOREIGN KEY (payroll_run_id) REFERENCES payroll_run (id),
    CONSTRAINT fk_payslip_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '급여 명세서';

CREATE TABLE payslip_detail (
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    payslip_id   BIGINT         NOT NULL,
    pay_item_id  BIGINT         NOT NULL,
    item_code    VARCHAR(30)    NOT NULL COMMENT '항목코드 스냅샷',
    item_name    VARCHAR(50)    NOT NULL COMMENT '항목명 스냅샷',
    item_type    VARCHAR(20)    NOT NULL COMMENT 'EARNING / DEDUCTION',
    amount       DECIMAL(15, 0) NOT NULL DEFAULT 0,
    note         VARCHAR(200)   NULL,
    created_at   DATETIME(6)    NOT NULL,
    created_by   VARCHAR(50)    NULL,
    updated_at   DATETIME(6)    NOT NULL,
    updated_by   VARCHAR(50)    NULL,
    PRIMARY KEY (id),
    KEY ix_payslip_detail_payslip (payslip_id),
    CONSTRAINT fk_payslip_detail_payslip FOREIGN KEY (payslip_id) REFERENCES payslip (id),
    CONSTRAINT fk_payslip_detail_item FOREIGN KEY (pay_item_id) REFERENCES pay_item (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '급여 명세서 상세';

-- MARK: 보안, 공통

CREATE TABLE app_user (
    id                   BIGINT       NOT NULL AUTO_INCREMENT,
    username             VARCHAR(50)  NOT NULL,
    password_hash        VARCHAR(100) NOT NULL COMMENT 'BCrypt',
    employee_id          BIGINT       NULL COMMENT '연결된 사원 (관리자 계정은 NULL 가능)',
    enabled              BOOLEAN      NOT NULL DEFAULT TRUE,
    locked               BOOLEAN      NOT NULL DEFAULT FALSE,
    failed_login_count   INT          NOT NULL DEFAULT 0,
    last_login_at        DATETIME(6)  NULL,
    password_changed_at  DATETIME(6)  NULL,
    created_at           DATETIME(6)  NOT NULL,
    created_by           VARCHAR(50)  NULL,
    updated_at           DATETIME(6)  NOT NULL,
    updated_by           VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_user_username (username),
    UNIQUE KEY uk_app_user_employee (employee_id),
    CONSTRAINT fk_app_user_employee FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '로그인 계정';

CREATE TABLE app_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    code        VARCHAR(30)  NOT NULL COMMENT 'ROLE_ADMIN / ROLE_HR / ROLE_MANAGER / ROLE_EMPLOYEE',
    name        VARCHAR(50)  NOT NULL,
    description VARCHAR(200) NULL,
    created_at  DATETIME(6)  NOT NULL,
    created_by  VARCHAR(50)  NULL,
    updated_at  DATETIME(6)  NOT NULL,
    updated_by  VARCHAR(50)  NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_app_role_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '권한';

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    KEY ix_user_role_role (role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES app_user (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES app_role (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '계정-권한 매핑';

CREATE TABLE audit_log (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    actor       VARCHAR(50)  NULL COMMENT '수행자 username',
    action      VARCHAR(50)  NOT NULL COMMENT 'LOGIN / CREATE / UPDATE / DELETE / PAYROLL_RUN ...',
    target_type VARCHAR(50)  NULL,
    target_id   VARCHAR(50)  NULL,
    detail      TEXT         NULL,
    ip          VARCHAR(45)  NULL,
    created_at  DATETIME(6)  NOT NULL,
    PRIMARY KEY (id),
    KEY ix_audit_log_actor (actor, created_at),
    KEY ix_audit_log_target (target_type, target_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT '감사 로그';
