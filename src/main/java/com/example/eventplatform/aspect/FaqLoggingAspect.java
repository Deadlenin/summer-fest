package com.example.eventplatform.aspect;

import com.example.eventplatform.dto.FaqResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class FaqLoggingAspect {

    @Around("@annotation(LogFaqWrite)")
    public Object logFaqWrite(ProceedingJoinPoint joinPoint) throws Throwable {
        String operation = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();

        long startedAt = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long durationMs = System.currentTimeMillis() - startedAt;

            if (result instanceof FaqResponse response) {
                log.info(
                        "FAQ {} saved to DB: id={}, question={}, sortOrder={}, isActive={}, durationMs={}",
                        operation,
                        response.id(),
                        response.question(),
                        response.sortOrder(),
                        response.isActive(),
                        durationMs
                );
            } else if ("delete".equals(operation) && args.length > 0 && args[0] instanceof UUID id) {
                log.info("FAQ deleted from DB: id={}, durationMs={}", id, durationMs);
            } else {
                log.info("FAQ {} completed successfully, durationMs={}", operation, durationMs);
            }

            return result;
        } catch (Throwable exception) {
            log.error(
                    "FAQ {} failed, durationMs={}",
                    operation,
                    System.currentTimeMillis() - startedAt,
                    exception
            );
            throw exception;
        }
    }
}
