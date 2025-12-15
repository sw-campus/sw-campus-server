package com.swcampus.infra.ocr;

import java.util.List;

public record OcrResponse(
    String text,
    List<String> lines,
    List<Double> scores
) {}
