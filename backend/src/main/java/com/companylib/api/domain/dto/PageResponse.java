package com.companylib.api.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;
import lombok.Getter;
import java.util.List;

@Schema(description = "ページングレスポンス")
@Getter
public class PageResponse<T> {

    @Schema(description = "取得したデータの一覧")
    private final List<T> content;

    @Schema(description = "現在のページ番号（0始まり）", example = "0")
    private final int page;

    @Schema(description = "1ページあたりの件数", example = "20")
    private final int size;

    @Schema(description = "全件数", example = "1500")
    private final long totalElements;

    @Schema(description = "総ページ数", example = "75")
    private final int totalPages;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.page = page.getNumber();
        this.size = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
    }
}
