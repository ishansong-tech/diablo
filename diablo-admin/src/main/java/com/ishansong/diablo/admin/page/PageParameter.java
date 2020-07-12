package com.ishansong.diablo.admin.page;

import lombok.Data;

import java.io.Serializable;

@Data
public class PageParameter implements Serializable {

    private static final long serialVersionUID = -8324693985921606090L;

    private static final int DEFAULT_PAGE_SIZE = 10;

    private int currentPage;

    private int prePage;

    private int nextPage;

    private int pageSize;

    private int offset;

    private int totalPage;

    private int totalCount;

    public PageParameter() {
        this.currentPage = 1;
        this.pageSize = DEFAULT_PAGE_SIZE;
    }

    public PageParameter(final Integer currentPage, final Integer pageSize) {
        this.currentPage = currentPage == null || currentPage <= 0 ? 1 : currentPage;
        this.pageSize = pageSize == null || pageSize <= 0 ? DEFAULT_PAGE_SIZE : pageSize;
        this.offset = (this.currentPage - 1) * this.pageSize;
    }

    public PageParameter(final int currentPage, final int pageSize, final int totalCount) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalCount = totalCount;
        this.totalPage = (int) Math.ceil((double) totalCount / (double) pageSize);
        this.prePage = currentPage <= 1 ? 1 : currentPage - 1;
        this.nextPage = currentPage >= this.totalPage ? this.totalPage : currentPage + 1;
    }
}
