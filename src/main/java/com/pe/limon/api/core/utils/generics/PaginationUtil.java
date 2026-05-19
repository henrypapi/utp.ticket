package com.pe.limon.api.core.utils.generics;

/**
 * Utility class for pagination-related operations.
 */
public class PaginationUtil {

    /**
     * Validates and adjusts page and size parameters for pagination.
     * - Page cannot be negative; defaults to 0 if negative.
     * - Size must be positive; defaults to 10 if not positive.
     * - Size cannot exceed 100; caps at 100 if greater.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @return an array containing the validated page and size [page, size]
     */
    public static int[] validatePageAndSize(int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (size > 100) size = 100; // Max size limit
        return new int[]{page, size};
    }
}