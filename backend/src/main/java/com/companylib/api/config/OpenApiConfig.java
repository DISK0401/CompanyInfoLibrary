package com.companylib.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        Schema<?> errorResponse = new ObjectSchema()
            .description("エラーレスポンス")
            .addProperty("error", new StringSchema().description("エラーメッセージ").example("指定された企業は見つかりませんでした。"));

        Schema<?> detailsSchema = new ObjectSchema()
            .description("フィールド別エラー詳細")
            .additionalProperties(new StringSchema());

        Schema<?> validationErrorResponse = new ObjectSchema()
            .description("バリデーションエラーレスポンス")
            .addProperty("error", new StringSchema().description("エラー概要").example("バリデーションエラー"))
            .addProperty("details", detailsSchema);

        return new OpenAPI()
            .info(new Info()
                .title("CompanyInfoLibrary API")
                .description("企業情報を検索・取得するためのREST API")
                .version("v1.0.0"))
            .components(new Components()
                .addSchemas("ErrorResponse", errorResponse)
                .addSchemas("ValidationErrorResponse", validationErrorResponse));
    }
}
