package org.dromara.common.redis.aspect;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.dromara.common.redis.annotation.CacheStrictSync;
import org.dromara.common.redis.manager.StrictCacheSyncManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 缓存严格同步AOP切面
 * 
 * <p>拦截带有@CacheStrictSync注解的方法，在缓存操作完成后
 * 根据配置决定是否发布严格同步消息。</p>
 *
 * @author Lion Li
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class CacheStrictSyncAspect {

    private final StrictCacheSyncManager strictCacheSyncManager;
    
    /**
     * SpEL表达式解析器
     */
    private final ExpressionParser parser = new SpelExpressionParser();

    /**
     * 是否启用严格同步模式
     */
    @Value("${cache.sync.strict.enabled:false}")
    private boolean strictSyncEnabled;

    /**
     * 拦截带有@CacheStrictSync注解的方法
     */
    @Around("@annotation(cacheStrictSync)")
    public Object around(ProceedingJoinPoint joinPoint, CacheStrictSync cacheStrictSync) throws Throwable {
        // 如果未启用严格同步，直接执行原方法
        if (!strictSyncEnabled) {
            log.debug("严格缓存同步功能未启用，跳过同步处理");
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        // 检查条件表达式
        if (!evaluateCondition(cacheStrictSync.condition(), method, args)) {
            log.debug("严格同步条件不满足，跳过同步处理: {}", cacheStrictSync.condition());
            return joinPoint.proceed();
        }

        // 执行原方法
        Object result = joinPoint.proceed();

        try {
            // 执行严格同步逻辑
            handleStrictSync(method, args, result, cacheStrictSync);
        } catch (Exception e) {
            log.error("执行严格缓存同步失败", e);
            // 同步失败不影响主流程
        }

        return result;
    }

    /**
     * 处理严格同步逻辑
     */
    private void handleStrictSync(Method method, Object[] args, Object result, CacheStrictSync cacheStrictSync) {
        // 获取缓存名称
        Set<String> cacheNames = extractCacheNames(method, cacheStrictSync);
        if (cacheNames.isEmpty()) {
            log.warn("无法提取缓存名称，跳过严格同步: {}", method.getName());
            return;
        }

        // 获取缓存键
        Set<Object> cacheKeys = extractCacheKeys(method, args);

        // 确定同步操作类型
        CacheSyncOperation operation = determineSyncOperation(method);

        // 执行同步
        for (String cacheName : cacheNames) {
            switch (operation) {
                case PUT:
                    for (Object key : cacheKeys) {
                        strictCacheSyncManager.publishStrictPut(cacheName, key, cacheStrictSync);
                    }
                    break;
                case EVICT:
                    for (Object key : cacheKeys) {
                        strictCacheSyncManager.publishStrictEvict(cacheName, key, cacheStrictSync);
                    }
                    break;
                case CLEAR:
                    strictCacheSyncManager.publishStrictClear(cacheName, cacheStrictSync);
                    break;
                default:
                    log.warn("未知的缓存操作类型: {}", operation);
            }
        }

        log.debug("执行严格缓存同步: operation={}, cacheNames={}, keys={}, syncType={}", 
            operation, cacheNames, cacheKeys, cacheStrictSync.syncType());
    }

    /**
     * 提取缓存名称
     */
    private Set<String> extractCacheNames(Method method, CacheStrictSync cacheStrictSync) {
        Set<String> cacheNames = new HashSet<>();

        // 优先使用注解中指定的缓存名称
        if (ArrayUtil.isNotEmpty(cacheStrictSync.cacheNames())) {
            cacheNames.addAll(Arrays.asList(cacheStrictSync.cacheNames()));
            return cacheNames;
        }

        // 从Spring Cache注解中提取
        Cacheable cacheable = AnnotationUtils.findAnnotation(method, Cacheable.class);
        if (cacheable != null && ArrayUtil.isNotEmpty(cacheable.cacheNames())) {
            cacheNames.addAll(Arrays.asList(cacheable.cacheNames()));
        }

        CachePut cachePut = AnnotationUtils.findAnnotation(method, CachePut.class);
        if (cachePut != null && ArrayUtil.isNotEmpty(cachePut.cacheNames())) {
            cacheNames.addAll(Arrays.asList(cachePut.cacheNames()));
        }

        CacheEvict cacheEvict = AnnotationUtils.findAnnotation(method, CacheEvict.class);
        if (cacheEvict != null && ArrayUtil.isNotEmpty(cacheEvict.cacheNames())) {
            cacheNames.addAll(Arrays.asList(cacheEvict.cacheNames()));
        }

        return cacheNames;
    }

    /**
     * 提取缓存键（简化实现，实际应该解析SpEL表达式）
     */
    private Set<Object> extractCacheKeys(Method method, Object[] args) {
        Set<Object> keys = new HashSet<>();
        
        // 简化实现：使用方法参数作为键
        // 实际应该解析Spring Cache注解中的key表达式
        if (args.length > 0) {
            keys.add(args[0]); // 使用第一个参数作为键
        } else {
            keys.add(method.getName()); // 使用方法名作为键
        }
        
        return keys;
    }

    /**
     * 确定同步操作类型
     */
    private CacheSyncOperation determineSyncOperation(Method method) {
        if (AnnotationUtils.findAnnotation(method, CachePut.class) != null) {
            return CacheSyncOperation.PUT;
        }
        
        CacheEvict cacheEvict = AnnotationUtils.findAnnotation(method, CacheEvict.class);
        if (cacheEvict != null) {
            return cacheEvict.allEntries() ? CacheSyncOperation.CLEAR : CacheSyncOperation.EVICT;
        }
        
        if (AnnotationUtils.findAnnotation(method, Cacheable.class) != null) {
            return CacheSyncOperation.PUT; // Cacheable在缓存未命中时相当于PUT
        }
        
        return CacheSyncOperation.PUT; // 默认为PUT操作
    }

    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Method method, Object[] args) {
        if (StrUtil.isBlank(condition)) {
            return true; // 无条件时默认为true
        }

        try {
            Expression expression = parser.parseExpression(condition);
            EvaluationContext context = new StandardEvaluationContext();
            
            // 设置方法参数到上下文
            String[] paramNames = getParameterNames(method);
            for (int i = 0; i < args.length && i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }
            
            Boolean result = expression.getValue(context, Boolean.class);
            return result != null && result;
        } catch (Exception e) {
            log.warn("评估严格同步条件表达式失败: {}", condition, e);
            return false; // 表达式错误时默认为false
        }
    }

    /**
     * 获取方法参数名称（简化实现）
     */
    private String[] getParameterNames(Method method) {
        // 简化实现，实际应该使用参数名发现机制
        int paramCount = method.getParameterCount();
        String[] names = new String[paramCount];
        for (int i = 0; i < paramCount; i++) {
            names[i] = "arg" + i;
        }
        return names;
    }

    /**
     * 缓存同步操作类型
     */
    private enum CacheSyncOperation {
        PUT, EVICT, CLEAR
    }
}
