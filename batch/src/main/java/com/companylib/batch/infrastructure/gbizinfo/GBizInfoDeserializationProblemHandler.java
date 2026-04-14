package com.companylib.batch.infrastructure.gbizinfo;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.DeserializationProblemHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * gBizINFO JSON デシリアライズ時の未知フィールドを WARN ログ出力して処理を継続するハンドラ。
 *
 * <p>未知フィールドが存在する場合、例外を投げずにログを出力して次フィールドに進む。
 * これにより gBizINFO の仕様変更・フィールド追加があっても連携が止まらない。
 * ログを確認次第、DTO と DB に新フィールドを追加すること。
 */
@Slf4j
public class GBizInfoDeserializationProblemHandler extends DeserializationProblemHandler {

    @Override
    public boolean handleUnknownProperty(
        DeserializationContext ctxt,
        JsonParser p,
        com.fasterxml.jackson.databind.JsonDeserializer<?> deserializer,
        Object beanOrClass,
        String propertyName
    ) throws IOException {
        String targetClass = beanOrClass instanceof Class<?>
            ? ((Class<?>) beanOrClass).getSimpleName()
            : beanOrClass.getClass().getSimpleName();

        log.warn("[gBizINFO] 未知フィールドを検出しました。DTO への追加を検討してください。"
            + " class={}, field={}", targetClass, propertyName);

        // トークンを読み飛ばして処理継続
        p.skipChildren();
        return true;
    }
}
