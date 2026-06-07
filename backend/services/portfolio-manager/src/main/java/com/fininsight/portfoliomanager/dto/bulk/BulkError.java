package com.fininsight.portfoliomanager.dto.bulk;

public record BulkError(
    int index,
    String field,
    String message
) {}
