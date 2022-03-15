package com.kevinguanchedarias.taggablecache.placeholderresolver;

import com.kevinguanchedarias.taggablecache.internal.model.ParsedKeyAndTagsModel;
import org.apache.commons.beanutils.PropertyUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class DefaultPlaceholderResolver implements PlaceholderResolver {
    private final Pattern pattern = Pattern.compile("#[a-z0-9.]+", Pattern.CASE_INSENSITIVE);

    @Override
    public ParsedKeyAndTagsModel resolveExpressions(JoinPoint joinPoint, String key, List<String> tags) {
        var context = buildEvaluationContext(joinPoint);
        return ParsedKeyAndTagsModel.builder()
                .key(resolveExpressionOrPlain(key, context))
                .tags(tags.stream()
                        .map(tag -> resolveExpressionOrPlain(tag, context)).toList()
                )
                .build();
    }

    private String resolveExpressionOrPlain(String expression, Map<String, Object> context) {
        if (expression.contains("#")) {
            var matcher = pattern.matcher(expression);
            Set<String> variables = new HashSet<>();
            while (matcher.find()) {
                variables.add(findOutVariable(expression, matcher.start(), matcher.end()));
            }
            return applyVariables(expression, variables, context);
        } else {
            return expression;
        }
    }

    private String applyVariables(String expression, Set<String> variables, Map<String, Object> context) {
        var parsedExpression = expression;
        for (var variable : variables) {
            parsedExpression = parsedExpression.replace(variable, resolveVariableValue(variable, context));
        }
        return parsedExpression;
    }

    private String resolveVariableValue(String variableName, Map<String, Object> context) {
        var nameWithoutHash = variableName.substring(1);
        if (variableName.contains(".")) {
            var path = nameWithoutHash.split("\\.");
            return transversePath(nameWithoutHash, path, context);
        } else {
            return context.get(nameWithoutHash).toString();
        }
    }

    private String transversePath(String variableNameWithoutHash, String[] path, Map<String, Object> context) {
        var rootObject = path[0];
        var currentObject = context.get(rootObject);
        for (int i = 1; i < path.length; i++) {
            try {
                currentObject = PropertyUtils.getProperty(currentObject, path[i]);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalArgumentException("unable to transverse path " + variableNameWithoutHash, e);
            }
        }
        return currentObject.toString();
    }

    private String findOutVariable(String expression, int start, int end) {
        return expression.substring(start, end);
    }

    private Map<String, Object> buildEvaluationContext(JoinPoint joinPoint) {
        Map<String, Object> map = new HashMap<>();
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        map.put("className", methodSignature.getDeclaringType().getName());
        map.put("methodName", methodSignature.getName());
        String[] parameterNames = methodSignature.getParameterNames();
        IntStream.range(0, parameterNames.length).forEach(i -> {
            final String parameterName = parameterNames[i];
            map.put(parameterName, joinPoint.getArgs()[i]);
        });
        return Collections.unmodifiableMap(map);
    }
}
