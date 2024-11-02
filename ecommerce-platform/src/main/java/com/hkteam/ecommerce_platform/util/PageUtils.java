package com.hkteam.ecommerce_platform.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.hkteam.ecommerce_platform.exception.AppException;
import com.hkteam.ecommerce_platform.exception.ErrorCode;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PageUtils {
    public int[] validateAndConvertPageAndSize(String pageStr, String sizeStr) {
        int page;
        int size;

        try {
            page = Integer.parseInt(pageStr);
            size = Integer.parseInt(sizeStr);
        } catch (NumberFormatException e) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }

        if (page < 1 || size < 1) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }

        return new int[] {page, size};
    }

    public Pageable createPageable(String pageStr, String sizeStr, Sort sort) {
        int[] pageAndSize = validateAndConvertPageAndSize(pageStr, sizeStr);
        int page = pageAndSize[0];
        int size = pageAndSize[1];

        return PageRequest.of(page - 1, size, sort != null ? sort : Sort.unsorted());
    }

    public <T> void validatePageBounds(int page, Page<T> pageData) {
        if (pageData.getTotalPages() > 0 && (page - 1) >= pageData.getTotalPages()) {
            throw new AppException(ErrorCode.PAGE_NOT_FOUND);
        }
    }
}
