# NCP WAS 배포 인계서

## 1. 인계 개요

- 대상 서비스: Greentech HRMS WAS
- 대상 환경: NCP Ubuntu 22.04 LTS
- 배포 방식: Docker 이미지 tar 파일을 NCP Object Storage로 전달 후 서버에서 로드
- Container Registry, Terraform, CI/CD는 이번 배포 범위에서 제외
- 애플리케이션 실행 프로필: `prod`
- WAS 서비스 포트: `41783/tcp`
- 작성일: 2026-08-19

## 2. 배포 이미지 정보

| 항목 | 값 |
| --- | --- |
| 배포 대상 이미지 | `greentech-was:20260819-1` |
| Image ID | 재빌드 후 기입 |
| 플랫폼 | `linux/amd64` |
| 이미지 크기 | 재빌드 후 기입 |
| Java Runtime | Eclipse Temurin JRE 21 |
| 컨테이너 사용자 | `greentech` |
| 컨테이너 포트 | `41783/tcp` |
| 실행 파일 | `/app/app.jar` |

기존 `greentech-was:20260819` 이미지는 로그 파일 보강 전 Smoke Test용으로만 유지한다. 운영 배포에는 `greentech-was:20260819-1`을 사용한다. 이미지는 환경설정과 비밀값을 포함하지 않으며 모든 운영 설정은 컨테이너 실행 시 `.env.prod`로 주입한다.

## 3. 사전 준비

- NCP 서버 CPU 아키텍처가 `x86_64`인지 확인
- Ubuntu 22.04 LTS 서버에 Docker Engine 설치
- 서버에서 NCP Object Storage에 접근할 수 있도록 `s3cmd` 설정
- Cloud DB for MySQL의 Private Endpoint와 변경 포트 확인
- WAS ACG와 Cloud DB ACG 간 통신 규칙 적용
- 운영 프론트엔드 Origin 확정
- `.env.prod`를 서버에 별도 생성하고 권한을 `600`으로 제한
- 호스트 로그 디렉터리 `/var/log/greentech/was` 생성
- CLA Custom Log 경로 등록 확인

```bash
uname -m
```

예상 결과:

```text
x86_64
```

## 4. 이미지 Object Storage 전달

### 4.1 이미지 tar 생성

빌드 PC에서 실행한다.

```bash
docker save \
  -o greentech-was-20260819-1.tar \
  greentech-was:20260819-1
```

### 4.2 무결성 검증값 생성

macOS 빌드 PC:

```bash
shasum -a 256 greentech-was-20260819-1.tar
```

산출된 SHA-256 값은 이미지 전달 시 별도로 기록한다.

```text
SHA-256: ________________________________________________
```

### 4.3 Object Storage 업로드

```bash
s3cmd put \
  greentech-was-20260819-1.tar \
  s3://<BUCKET_NAME>/docker-images/greentech-was-20260819-1.tar
```

업로드 확인:

```bash
s3cmd ls s3://<BUCKET_NAME>/docker-images/
```

## 5. Ubuntu 서버 이미지 반입

### 5.1 이미지 다운로드

```bash
s3cmd get \
  s3://<BUCKET_NAME>/docker-images/greentech-was-20260819-1.tar \
  /opt/greentech/greentech-was-20260819-1.tar
```

### 5.2 무결성 확인

```bash
sha256sum /opt/greentech/greentech-was-20260819-1.tar
```

빌드 PC에서 기록한 SHA-256 값과 일치해야 한다.

### 5.3 이미지 로드

```bash
sudo docker load -i /opt/greentech/greentech-was-20260819-1.tar
```

```bash
sudo docker image inspect greentech-was:20260819-1 \
  --format '{{.Os}}/{{.Architecture}} {{json .Config.ExposedPorts}}'
```

예상 결과:

```text
linux/amd64 {"41783/tcp":{}}
```

## 6. 운영 환경변수

운영 서버의 `/opt/greentech/.env.prod`에 다음 필드를 정의한다. 실제 비밀값은 문서, Git 저장소, 메신저에 기록하지 않는다.

NCP Secret Manager는 `.env.prod` 파일을 자동으로 생성하지 않는다. 배포 담당 주체가 Secret Manager 값을 조회해 런타임 파일을 생성하는 절차 또는 배포 스크립트를 별도로 준비해야 한다. 조회 권한은 최소 범위로 부여하고 생성된 파일은 `root:root`, 권한 `600`으로 관리한다.

```dotenv
# Cloud DB
DB_URL=
DB_USERNAME=
DB_PASSWORD=
DB_POOL_SIZE=

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=41783
TZ=
JAVA_OPTS=
LOG_LEVEL_APP=
LOG_LEVEL_SQL=
LOG_DIR=/app/logs
ACCESS_LOG_MAX_DAYS=
LOG_MAX_HISTORY=
LOG_MAX_FILE_SIZE=
LOG_TOTAL_SIZE_CAP=
CORS_ALLOWED_ORIGINS=

# Security
JWT_SECRET=
ACCESS_TOKEN_TTL=
FIELD_ENCRYPTION_KEY=

# Initial administrator
ADMIN_USERNAME=
ADMIN_PASSWORD=
ADMIN_RESET_PASSWORD=

# NCP Object Storage
NCP_STORAGE_ENDPOINT=
NCP_STORAGE_REGION=
NCP_STORAGE_BUCKET=
NCP_STORAGE_ACCESS_KEY=
NCP_STORAGE_SECRET_KEY=
```

환경변수 작성 기준:

- `DB_URL`: Cloud DB Private Endpoint와 변경 포트를 포함한 JDBC URL
- `JWT_SECRET`: 최소 32바이트
- `FIELD_ENCRYPTION_KEY`: 16·24·32바이트 AES 키를 Base64로 인코딩한 값
- `ACCESS_TOKEN_TTL`: Spring Duration 형식
- `CORS_ALLOWED_ORIGINS`: 운영 프론트엔드 Origin을 입력하며 경로와 마지막 `/`는 제외
- `ADMIN_RESET_PASSWORD`: 운영 기동 시 관리자 비밀번호를 초기화하지 않으면 `false`
- `NCP_STORAGE_ENDPOINT`: WAS가 접근할 NCP Object Storage Endpoint
- 여러 CORS Origin은 쉼표로 구분
- `LOG_DIR`: 컨테이너 내부 로그 디렉터리 `/app/logs`
- `ACCESS_LOG_MAX_DAYS`: 호스트에 유지할 Tomcat 접근 로그 회전 파일 보관 일수
- `LOG_MAX_HISTORY`, `LOG_MAX_FILE_SIZE`, `LOG_TOTAL_SIZE_CAP`: 애플리케이션·에러 로그의 로컬 회전 기준

파일 권한 설정:

```bash
sudo chown root:root /opt/greentech/.env.prod
```

```bash
sudo chmod 600 /opt/greentech/.env.prod
```

## 7. 네트워크 및 보안 규칙

### WAS

- 애플리케이션 수신 포트: `41783/tcp`
- `41783`은 인터넷 전체에 개방하지 않는다.
- Inbound Source는 내부 Load Balancer 또는 허용된 Web 서버/ACG로 제한한다.
- SSH `22/tcp`는 Bastion Host 또는 관리자 허용 IP로 제한한다.

### Cloud DB for MySQL

- Cloud DB의 변경된 포트를 `DB_URL`에 반영한다.
- Cloud DB ACG Inbound Source는 WAS ACG로 제한한다.
- WAS 서버에서 Cloud DB Private Endpoint로 연결 가능해야 한다.

```bash
nc -vz <CLOUD_DB_PRIVATE_HOST> <DB_PORT>
```

### NCP Object Storage

- WAS에서 Object Storage Endpoint의 `443/tcp`로 통신 가능해야 한다.
- 운영 애플리케이션은 `prod` 프로필에서 Object Storage를 파일 저장소로 사용한다.
- Access Key와 Secret Key는 이미지나 Git 저장소에 포함하지 않는다.

### Load Balancer 사용 시

- Target Port: `41783`
- Health Check Path: `/actuator/health`
- 정상 응답 코드: `200`
- 외부 TLS 종료 시 WAS의 `forward-headers-strategy=framework` 설정이 적용된다.

## 8. 컨테이너 실행

### 8.1 호스트 로그 디렉터리 준비

컨테이너는 UID/GID `10001:10001`의 `greentech` 사용자로 실행된다.

```bash
sudo install -d \
  -o 10001 \
  -g 10001 \
  -m 0750 \
  /var/log/greentech/was
```

### 8.2 컨테이너 기동

```bash
sudo docker run -d \
  --name greentech-was \
  --restart unless-stopped \
  --env-file /opt/greentech/.env.prod \
  -p 41783:41783 \
  --mount type=bind,source=/var/log/greentech/was,target=/app/logs \
  greentech-was:20260819-1
```

컨테이너 상태 확인:

```bash
sudo docker ps --filter name=greentech-was
```

로그 확인:

```bash
sudo docker logs --tail 200 greentech-was
```

환경변수 값을 로그나 점검 결과에 출력하지 않는다.

### 8.3 CLA 수집 파일 확인

| CLA Log Type | 호스트 경로 | 생성 주체 |
| --- | --- | --- |
| `greentech_was_app` | `/var/log/greentech/was/application.log` | Logback |
| `greentech_was_error` | `/var/log/greentech/was/error.log` | Logback |
| `greentech_was_access` | `/var/log/greentech/was/access.log` | Tomcat Access Log |
| `greentech_was_auth` | `/var/log/auth.log` | Ubuntu 호스트 |

`/var/log/auth.log`는 컨테이너에 마운트하지 않는다. Ubuntu의 SSH·sudo 등 인증 로그를 CLA Agent가 호스트에서 직접 수집한다.

Ubuntu 최소 이미지에서 `/var/log/auth.log`가 없다면 `rsyslog` 기동과 auth facility 파일 출력을 먼저 확인한다.

```bash
sudo systemctl is-active rsyslog
```

```bash
sudo ls -l \
  /var/log/greentech/was/application.log \
  /var/log/greentech/was/error.log \
  /var/log/greentech/was/access.log \
  /var/log/auth.log
```

`access.log`는 첫 HTTP 요청 이후 생성될 수 있다. `error.log`는 ERROR 이벤트가 없어도 Logback appender 초기화 시 생성 여부를 확인하고, CLA는 활성 파일 경로만 수집하도록 유지한다.

## 9. 배포 검증

### 9.1 애플리케이션 헬스체크

```bash
curl -fsS http://127.0.0.1:41783/actuator/health
```

예상 결과:

```json
{"status":"UP"}
```

### 9.2 기능 검증

- 관리자 계정 로그인
- `/api/v1/auth/me` 사용자 및 권한 응답 확인
- 사원 목록과 사원 상세 조회
- 학력·자격증·가족사항 등록 및 수정
- 휴가·연장근무 신청과 권한별 결재 범위 확인
- 프로필 사진 또는 첨부파일 업로드
- Object Storage 객체 생성 확인
- 첨부파일 조회 및 삭제 확인
- 애플리케이션 재시작 후 파일 조회 확인

### 9.3 로그 확인 항목

- Spring Profile이 `prod`로 활성화됐는지 확인
- 애플리케이션이 `41783`에서 기동됐는지 확인
- `application.log`, `error.log`, `access.log` 파일 생성과 UID/GID 확인
- CLA의 4개 Custom Log 수집 상태 확인
- Flyway Migration 성공 여부 확인
- Cloud DB 연결 오류 여부 확인
- Object Storage 인증·버킷·Endpoint 오류 여부 확인
- CORS 차단 오류 여부 확인

## 10. 장애 확인 순서

1. `docker ps -a`로 컨테이너 종료 여부 확인
2. `docker logs --tail 200 greentech-was`로 최초 오류 확인
3. `.env.prod`의 필수 필드 누락 여부 확인
4. Cloud DB Private Endpoint와 변경 포트 연결 확인
5. WAS ACG와 Cloud DB ACG 규칙 확인
6. Object Storage Endpoint, Bucket, Key 권한 확인
7. `/app/logs` bind mount와 호스트 디렉터리 권한 확인
8. Load Balancer Target 상태와 `41783` 헬스체크 확인

## 11. 롤백 기준

- 이전 정상 이미지 태그와 tar 파일을 배포 전에 보존한다.
- DB Migration이 포함된 경우 스키마 하위 호환 여부를 먼저 확인한다.
- 롤백 전에 장애 컨테이너 로그를 보존한다.
- 이전 이미지로 컨테이너를 다시 생성하되 동일한 `.env.prod` 사용 여부를 검토한다.
- 파일 데이터는 Object Storage에 저장되므로 컨테이너 교체와 분리해서 관리한다.

## 12. 인프라팀 최종 체크리스트

- [ ] Ubuntu 서버 아키텍처 `x86_64` 확인
- [ ] Docker Engine 정상 동작 확인
- [ ] 이미지 SHA-256 일치 확인
- [ ] `greentech-was:20260819-1` 로드 확인
- [ ] `.env.prod` 작성 및 권한 `600` 적용
- [ ] Cloud DB 변경 포트 및 Private Endpoint 반영
- [ ] WAS `41783` ACG Source 제한
- [ ] Object Storage `443` 통신 확인
- [ ] Load Balancer Target Port `41783` 적용
- [ ] `/actuator/health` 응답 확인
- [ ] `application.log`, `error.log`, `access.log` 생성 확인
- [ ] `/var/log/auth.log` 포함 CLA 4개 Log Type 수집 확인
- [ ] 로그인 및 핵심 업무 API 스모크 테스트 완료
- [ ] 파일 업로드·조회·삭제와 Object Storage 객체 확인
- [ ] 이전 정상 이미지 보존
