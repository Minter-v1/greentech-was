package com.greentech.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI 문서 정의. Authorize 버튼에 JWT 입력 후 호출 가능 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI hrOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("greentech-was 인사관리시스템 API")
                        .version("v1")
                        .description("""
                                사내 인사·근태·급여 관리 REST API

                                인증 절차: `POST /api/v1/auth/login` 으로 accessToken 발급 후
                                우측 상단 Authorize 에 토큰 입력. Bearer 접두사 불필요.

                                초기 계정은 애플리케이션 기동 시 `app.security.bootstrap-password` 값으로 생성.
                                """)
                        .contact(new Contact().name("greentech 정보시스템팀")))
                .addServersItem(new Server().url("/").description("현재 서버"))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("로그인 응답의 accessToken 값")));
    }
}
