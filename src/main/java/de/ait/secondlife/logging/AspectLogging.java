package de.ait.secondlife.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class AspectLogging {

    @Pointcut("execution(* de.ait.secondlife.exception_handling.OfferExceptionHandler.*(..))")
    public void allMethodsOfOfferExceptionHandler() {}

    @After("allMethodsOfOfferExceptionHandler()")
    public void logAfterMethodsOfOfferExceptionHandler(JoinPoint joinPoint) {
        RuntimeException e = (RuntimeException) joinPoint.getArgs()[0];
        log.error(String.format("[%s] : %s", e.getClass(),e.getMessage()));
    }
}
