package com.kevinguanchedarias.taggablecache.placeholderresolver;

import com.kevinguanchedarias.taggablecache.internal.model.ParsedKeyAndTagsModel;
import lombok.AllArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.List;
import java.util.stream.IntStream;

@AllArgsConstructor
public class SpringSpelPlaceholderResolver implements PlaceholderResolver {

    private final SpelExpressionParser spelExpressionParser;

    @Override
    public ParsedKeyAndTagsModel resolveExpressions(JoinPoint joinPoint, String key, List<String> tags) {
        var evaluationContext = buildEvaluationContext(joinPoint);
        return ParsedKeyAndTagsModel.builder()
                .key(resolveExpressionOrPlainText(key, evaluationContext))
                .tags(tags.stream()
                        .map(tag -> resolveExpressionOrPlainText(tag, evaluationContext)).toList()
                )
                .build();
    }

    private String resolveExpressionOrPlainText(String tag, EvaluationContext evaluationContext) {
        if (tag.contains("#")) {
            return spelExpressionParser.parseExpression(tag).getValue(evaluationContext, String.class);
        } else {
            return tag;
        }
    }

    private EvaluationContext buildEvaluationContext(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        EvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setVariable("className", methodSignature.getDeclaringType());
        evaluationContext.setVariable("methodName", methodSignature.getName());
        String[] parameterNames = methodSignature.getParameterNames();
        IntStream.range(0, parameterNames.length).forEach(i -> {
            final String parameterName = parameterNames[i];
            evaluationContext.setVariable(parameterName, joinPoint.getArgs()[i]);
        });
        return evaluationContext;
    }
}
