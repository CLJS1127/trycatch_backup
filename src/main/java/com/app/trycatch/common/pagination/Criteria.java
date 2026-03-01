package com.app.trycatch.common.pagination;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class Criteria {
    private int page;
    private int pageCount;
    private int startPage;
    private int endPage;
    private int rowCount;
    private int realEnd;
    private int offset;
    private int count;
    private boolean hasMore;
    private int total;

    public Criteria(int page, int total) {
        this.total = total;
        rowCount = 10;
        pageCount = 10;
        count = rowCount + 1;
        this.page = Math.max(1, page);
        offset = (this.page - 1) * rowCount;
        endPage = (int)(Math.ceil(this.page / (double)pageCount) * pageCount);
        startPage = endPage - pageCount + 1;
        realEnd = (int)(Math.ceil(total / (double)rowCount));
        endPage = Math.min(endPage, Math.max(1, realEnd));
        startPage = Math.max(1, startPage);
    }
}
