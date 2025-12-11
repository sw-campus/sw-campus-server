package com.swcampus.api.admin.request;

import jakarta.validation.constraints.NotNull;

public record BlindReviewRequest(
    @NotNull(message = "blurred 값은 필수입니다")
    Boolean blurred
) {}
