package com.aimsp.intelligence.exception;

public class AiApiUnavailableException extends RuntimeException {
    public AiApiUnavailableException() {
        super("AI API가 현재 응답하지 않습니다. 잠시 후 다시 시도해주세요.");
    }
}
