-- =============================================================================
-- 기준정보 및 데모용 샘플 데이터
--
-- NOTE: 로그인 계정(app_user)은 시드 대상 제외
--       BCrypt 해시 하드코딩 회피 목적 - 기동 시 AccountBootstrapper 가
--       app.security.bootstrap-password 값으로 생성
-- =============================================================================

SET @now = NOW(6);

-- -----------------------------------------------------------------------------
-- 부서
-- -----------------------------------------------------------------------------
INSERT INTO department (id, code, name, parent_id, sort_order, active, created_at, updated_at) VALUES
    (1,  'D100', '경영지원본부', NULL, 10, TRUE, @now, @now),
    (2,  'D110', '인사팀',       1,    11, TRUE, @now, @now),
    (3,  'D120', '재무팀',       1,    12, TRUE, @now, @now),
    (4,  'D200', '생산본부',     NULL, 20, TRUE, @now, @now),
    (5,  'D210', '생산1팀',      4,    21, TRUE, @now, @now),
    (6,  'D220', '생산2팀',      4,    22, TRUE, @now, @now),
    (7,  'D230', '품질관리팀',   4,    23, TRUE, @now, @now),
    (8,  'D300', '영업본부',     NULL, 30, TRUE, @now, @now),
    (9,  'D310', '국내영업팀',   8,    31, TRUE, @now, @now),
    (10, 'D320', '해외영업팀',   8,    32, TRUE, @now, @now);

-- -----------------------------------------------------------------------------
-- 직위
-- -----------------------------------------------------------------------------
INSERT INTO job_position (id, code, name, level_no, active, created_at, updated_at) VALUES
    (1, 'P10', '사원', 1, TRUE, @now, @now),
    (2, 'P20', '주임', 2, TRUE, @now, @now),
    (3, 'P30', '대리', 3, TRUE, @now, @now),
    (4, 'P40', '과장', 4, TRUE, @now, @now),
    (5, 'P50', '차장', 5, TRUE, @now, @now),
    (6, 'P60', '부장', 6, TRUE, @now, @now),
    (7, 'P70', '이사', 7, TRUE, @now, @now);

-- -----------------------------------------------------------------------------
-- 휴가 종류
-- -----------------------------------------------------------------------------
INSERT INTO leave_type (id, code, name, paid, deduct_annual, max_days_per_year, active, created_at, updated_at) VALUES
    (1, 'ANNUAL',      '연차',       TRUE,  TRUE,  NULL, TRUE, @now, @now),
    (2, 'SICK',        '병가',       TRUE,  FALSE, 10.0, TRUE, @now, @now),
    (3, 'CONDOLENCE',  '경조사 휴가', TRUE,  FALSE, 5.0,  TRUE, @now, @now),
    (4, 'OFFICIAL',    '공가',       TRUE,  FALSE, NULL, TRUE, @now, @now),
    (5, 'MATERNITY',   '출산 휴가',   TRUE,  FALSE, 90.0, TRUE, @now, @now),
    (6, 'UNPAID',      '무급 휴가',   FALSE, FALSE, NULL, TRUE, @now, @now);

-- -----------------------------------------------------------------------------
-- 급여 항목
-- -----------------------------------------------------------------------------
INSERT INTO pay_item (id, code, name, item_type, taxable, sort_order, active, created_at, updated_at) VALUES
    -- 지급
    (1,  'BASE',                '기본급',         'EARNING',   TRUE,  10, TRUE, @now, @now),
    (2,  'POSITION_ALLOWANCE',  '직책수당',       'EARNING',   TRUE,  20, TRUE, @now, @now),
    (3,  'MEAL',                '식대',           'EARNING',   FALSE, 30, TRUE, @now, @now),
    (4,  'OVERTIME',            '연장근로수당',   'EARNING',   TRUE,  40, TRUE, @now, @now),
    (5,  'NIGHT',               '야간근로수당',   'EARNING',   TRUE,  50, TRUE, @now, @now),
    (6,  'HOLIDAY_WORK',        '휴일근로수당',   'EARNING',   TRUE,  60, TRUE, @now, @now),
    (7,  'BONUS',               '상여금',         'EARNING',   TRUE,  70, TRUE, @now, @now),
    -- 공제
    (8,  'NATIONAL_PENSION',    '국민연금',       'DEDUCTION', FALSE, 110, TRUE, @now, @now),
    (9,  'HEALTH_INSURANCE',    '건강보험',       'DEDUCTION', FALSE, 120, TRUE, @now, @now),
    (10, 'LONG_TERM_CARE',      '장기요양보험',   'DEDUCTION', FALSE, 130, TRUE, @now, @now),
    (11, 'EMPLOYMENT_INSURANCE','고용보험',       'DEDUCTION', FALSE, 140, TRUE, @now, @now),
    (12, 'INCOME_TAX',          '소득세',         'DEDUCTION', FALSE, 150, TRUE, @now, @now),
    (13, 'LOCAL_INCOME_TAX',    '지방소득세',     'DEDUCTION', FALSE, 160, TRUE, @now, @now);

-- -----------------------------------------------------------------------------
-- 공제 요율
--
-- FIXME: 계산 로직 검증용 예시값 - 운영 적용 전 해당 연도 고시 요율로 갱신 필수
-- NOTE: LONG_TERM_CARE 는 급여가 아닌 건강보험료액 기준 요율
-- NOTE: INCOME_TAX 는 간이세액표 대신 단일 근사 요율 적용한 단순화 구현
-- NOTE: LOCAL_INCOME_TAX 는 소득세액 대비 10%
-- -----------------------------------------------------------------------------
INSERT INTO deduction_rate (year, item_code, employee_rate, employer_rate, description, created_at, updated_at) VALUES
    (2026, 'NATIONAL_PENSION',     0.04500, 0.04500, '과세소득 기준 (예시값)',                @now, @now),
    (2026, 'HEALTH_INSURANCE',     0.03545, 0.03545, '과세소득 기준 (예시값)',                @now, @now),
    (2026, 'LONG_TERM_CARE',       0.12950, 0.12950, '건강보험료액 기준 (예시값)',            @now, @now),
    (2026, 'EMPLOYMENT_INSURANCE', 0.00900, 0.01150, '과세소득 기준 (예시값)',                @now, @now),
    (2026, 'INCOME_TAX',           0.03000, 0.00000, '간이세액표 대체 근사 요율 (예시값)',    @now, @now),
    (2026, 'LOCAL_INCOME_TAX',     0.10000, 0.00000, '소득세액의 10% (예시값)',               @now, @now);

-- -----------------------------------------------------------------------------
-- 권한
-- -----------------------------------------------------------------------------
INSERT INTO app_role (id, code, name, description, created_at, updated_at) VALUES
    (1, 'ROLE_ADMIN',    '시스템 관리자', '전체 기능 및 계정 관리',              @now, @now),
    (2, 'ROLE_HR',       '인사담당자',   '사원/근태/급여 전체 조회 및 처리',     @now, @now),
    (3, 'ROLE_MANAGER',  '부서장',       '소속 부서원 근태/휴가 승인',           @now, @now),
    (4, 'ROLE_EMPLOYEE', '일반 사원',    '본인 정보 조회 및 신청',               @now, @now);

-- -----------------------------------------------------------------------------
-- 근무 달력 - 2026년 고정일 공휴일 한정
--
-- NOTE: 설날·추석·부처님오신날은 음력 기준이라 연도별 상이 - 시드 대상 제외
-- NOTE: 음력 공휴일은 인사팀이 관리 API 로 매년 등록하는 운영 전제
-- NOTE: 토·일요일은 등록 없이 날짜로 계산
-- -----------------------------------------------------------------------------
INSERT INTO work_calendar (calendar_date, day_type, holiday_name, created_at, updated_at) VALUES
    ('2026-01-01', 'HOLIDAY', '신정',     @now, @now),
    ('2026-03-01', 'HOLIDAY', '삼일절',   @now, @now),
    ('2026-05-05', 'HOLIDAY', '어린이날', @now, @now),
    ('2026-06-06', 'HOLIDAY', '현충일',   @now, @now),
    ('2026-08-15', 'HOLIDAY', '광복절',   @now, @now),
    ('2026-10-03', 'HOLIDAY', '개천절',   @now, @now),
    ('2026-10-09', 'HOLIDAY', '한글날',   @now, @now),
    ('2026-12-25', 'HOLIDAY', '성탄절',   @now, @now);

-- -----------------------------------------------------------------------------
-- 샘플 사원 (데모/검증용)
-- -----------------------------------------------------------------------------
INSERT INTO employee (id, emp_no, name, email, department_id, job_position_id, manager_id,
                      employment_type, status, hire_date, birth_date, gender, created_at, updated_at) VALUES
    (1,  '20180101', '김성호', 'sh.kim@greentech.co.kr',  1, 7, NULL, 'FULL_TIME', 'ACTIVE',   '2018-01-02', '1975-04-11', 'MALE',   @now, @now),
    (2,  '20190201', '박은주', 'ej.park@greentech.co.kr', 2, 6, 1,    'FULL_TIME', 'ACTIVE',   '2019-02-01', '1982-09-23', 'FEMALE', @now, @now),
    (3,  '20200301', '이재훈', 'jh.lee@greentech.co.kr',  2, 3, 2,    'FULL_TIME', 'ACTIVE',   '2020-03-02', '1990-01-15', 'MALE',   @now, @now),
    (4,  '20200302', '최다인', 'di.choi@greentech.co.kr', 3, 4, 1,    'FULL_TIME', 'ACTIVE',   '2020-03-02', '1988-07-30', 'FEMALE', @now, @now),
    (5,  '20210401', '정우성', 'ws.jung@greentech.co.kr', 4, 6, NULL, 'FULL_TIME', 'ACTIVE',   '2021-04-01', '1979-11-02', 'MALE',   @now, @now),
    (6,  '20210402', '한지민', 'jm.han@greentech.co.kr',  5, 4, 5,    'FULL_TIME', 'ACTIVE',   '2021-04-01', '1986-05-19', 'FEMALE', @now, @now),
    (7,  '20220501', '오세훈', 'sh.oh@greentech.co.kr',   5, 1, 6,    'FULL_TIME', 'ACTIVE',   '2022-05-02', '1996-03-08', 'MALE',   @now, @now),
    (8,  '20220502', '윤아름', 'ar.yoon@greentech.co.kr', 7, 3, 5,    'FULL_TIME', 'ACTIVE',   '2022-05-02', '1993-12-25', 'FEMALE', @now, @now),
    (9,  '20230601', '강민수', 'ms.kang@greentech.co.kr', 9, 3, 1,    'FULL_TIME', 'ACTIVE',   '2023-06-01', '1991-08-14', 'MALE',   @now, @now),
    (10, '20240701', '서지우', 'jw.seo@greentech.co.kr',  6, 1, 5,    'CONTRACT',  'ACTIVE',   '2024-07-01', '1999-02-27', 'FEMALE', @now, @now);

UPDATE employee SET manager_id = 1 WHERE id = 5;

INSERT INTO employee_contact (employee_id, mobile, zip_code, address1, address2,
                              emergency_name, emergency_relation, emergency_phone, created_at, updated_at) VALUES
    (1,  '010-1000-1001', '13529', '경기도 성남시 분당구 판교로 100', '101동 1001호', '김지영', 'SPOUSE', '010-2000-1001', @now, @now),
    (2,  '010-1000-1002', '06236', '서울특별시 강남구 테헤란로 200',  '3층',         '박준수', 'SPOUSE', '010-2000-1002', @now, @now),
    (3,  '010-1000-1003', '16419', '경기도 수원시 영통구 광교로 30',  '202호',       '이순임', 'PARENT', '010-2000-1003', @now, @now),
    (4,  '010-1000-1004', '04524', '서울특별시 중구 세종대로 110',    '',            '최민호', 'SPOUSE', '010-2000-1004', @now, @now),
    (5,  '010-1000-1005', '15073', '경기도 시흥시 공단1대로 250',     '',            '정혜원', 'SPOUSE', '010-2000-1005', @now, @now);

-- 연봉계약 (현재 유효분)
INSERT INTO salary_contract (employee_id, contract_no, pay_type, annual_salary, base_pay, effective_from, effective_to, created_at, updated_at) VALUES
    (1,  'SC-2026-001', 'ANNUAL', 96000000, 8000000, '2026-01-01', NULL, @now, @now),
    (2,  'SC-2026-002', 'ANNUAL', 72000000, 6000000, '2026-01-01', NULL, @now, @now),
    (3,  'SC-2026-003', 'ANNUAL', 48000000, 4000000, '2026-01-01', NULL, @now, @now),
    (4,  'SC-2026-004', 'ANNUAL', 54000000, 4500000, '2026-01-01', NULL, @now, @now),
    (5,  'SC-2026-005', 'ANNUAL', 84000000, 7000000, '2026-01-01', NULL, @now, @now),
    (6,  'SC-2026-006', 'ANNUAL', 57600000, 4800000, '2026-01-01', NULL, @now, @now),
    (7,  'SC-2026-007', 'ANNUAL', 39600000, 3300000, '2026-01-01', NULL, @now, @now),
    (8,  'SC-2026-008', 'ANNUAL', 46800000, 3900000, '2026-01-01', NULL, @now, @now),
    (9,  'SC-2026-009', 'ANNUAL', 50400000, 4200000, '2026-01-01', NULL, @now, @now),
    (10, 'SC-2026-010', 'MONTHLY', 33600000, 2800000, '2026-01-01', NULL, @now, @now);

-- 2026년 연차 부여
INSERT INTO leave_balance (employee_id, leave_type_id, year, granted_days, used_days, expires_on, created_at, updated_at) VALUES
    (1,  1, 2026, 21.0, 0.0, '2026-12-31', @now, @now),
    (2,  1, 2026, 19.0, 2.0, '2026-12-31', @now, @now),
    (3,  1, 2026, 17.0, 3.0, '2026-12-31', @now, @now),
    (4,  1, 2026, 17.0, 0.0, '2026-12-31', @now, @now),
    (5,  1, 2026, 16.0, 1.0, '2026-12-31', @now, @now),
    (6,  1, 2026, 16.0, 0.0, '2026-12-31', @now, @now),
    (7,  1, 2026, 15.0, 4.0, '2026-12-31', @now, @now),
    (8,  1, 2026, 15.0, 0.0, '2026-12-31', @now, @now),
    (9,  1, 2026, 15.0, 1.0, '2026-12-31', @now, @now),
    (10, 1, 2026, 15.0, 0.0, '2026-12-31', @now, @now);
