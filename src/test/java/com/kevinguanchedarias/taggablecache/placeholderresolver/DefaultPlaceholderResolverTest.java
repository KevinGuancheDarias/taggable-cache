package com.kevinguanchedarias.taggablecache.placeholderresolver;

import com.kevinguanchedarias.taggablecache.test.model.UserTestModel;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class DefaultPlaceholderResolverTest {
    private static final String NO_ARGS_METHOD_SIGNATURE = "withNoArgs()";
    private static final String NO_ARGS_EXPRESSION = "key_#className_#methodName";
    private static final String TAG_PLAIN_EXPRESSION = "tag:list";
    private static final String NO_ARGS_EXPRESSION_EXPECTED = "key_" + FakeClass.class.getName() + "_" + NO_ARGS_METHOD_SIGNATURE;

    private static final String WITH_ARGS_METHOD_SIGNATURE = "withArgs()";
    private static final String WITH_ARGS_EXPRESSION = "key_arg_#username_#age";
    private static final String TAG_WITH_ARGS = "user:#age";
    private static final String ARGUMENT_1_NAME = "username";
    private static final String ARGUMENT_1_VALUE = "John Connor";
    private static final String ARGUMENT_2_NAME = "age";
    private static final int ARGUMENT_2_VALUE = 19;

    private static final String WITH_COMPLEX_ARGS_METHOD_SIGNATURE = "withComplexArgs()";
    private static final String WITH_COMPLEX_ARGS_EXPRESSION = "key_arg_#user.name_#user.age";
    private static final String TAG_WITH_COMPLEX_ARGS = "user:#user.age";
    private static final String COMPLEX_ARG_NAME = "user";
    private static final UserTestModel COMPLEX_ARGUMENT_VALUE = UserTestModel.builder()
            .name(ARGUMENT_1_VALUE)
            .age(ARGUMENT_2_VALUE)
            .build();

    private final DefaultPlaceholderResolver defaultPlaceholderResolver = new DefaultPlaceholderResolver();

    private JoinPoint joinPointNoArgsSignatureMock;
    private JoinPoint joinPointWithArgsSignatureMock;
    private JoinPoint joinPointWithComplexArgsSignatureMock;

    private static class FakeClass {
    }

    @BeforeEach
    void setup() {
        joinPointNoArgsSignatureMock = mock(JoinPoint.class);
        joinPointWithArgsSignatureMock = mock(JoinPoint.class);
        joinPointWithComplexArgsSignatureMock = mock(JoinPoint.class);
        fakeNoArgsSignatureConfiguration();
        fakeArgsSignatureConfiguration();
        fakeComplexArgsSignatureConfiguration();
    }

    @Test
    void resolveExpressions_should_work_with_no_args() {
        var result = defaultPlaceholderResolver.resolveExpressions(
                joinPointNoArgsSignatureMock,
                NO_ARGS_EXPRESSION,
                List.of(TAG_PLAIN_EXPRESSION)
        );

        assertThat(result.getKey()).isEqualTo(NO_ARGS_EXPRESSION_EXPECTED);
        assertThat(result.getTags())
                .hasSize(1)
                .contains(TAG_PLAIN_EXPRESSION);
    }

    @Test
    void resolveExpressions_should_work_with_simple_args() {
        var result = defaultPlaceholderResolver.resolveExpressions(
                joinPointWithArgsSignatureMock,
                WITH_ARGS_EXPRESSION,
                List.of(TAG_WITH_ARGS)
        );

        assertThat(result.getKey()).isEqualTo("key_arg_John Connor_19");
        assertThat(result.getTags())
                .hasSize(1)
                .contains("user:19");
    }

    @Test
    void resolveExpression_should_work_with_complex_args() {
        var result = defaultPlaceholderResolver.resolveExpressions(
                joinPointWithComplexArgsSignatureMock,
                WITH_COMPLEX_ARGS_EXPRESSION,
                List.of(TAG_WITH_COMPLEX_ARGS)
        );

        assertThat(result.getKey()).isEqualTo("key_arg_John Connor_19");
        assertThat(result.getTags())
                .hasSize(1)
                .contains("user:19");
    }

    private void fakeNoArgsSignatureConfiguration() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        given(methodSignature.getDeclaringType()).willReturn(FakeClass.class);
        given(methodSignature.getName()).willReturn(NO_ARGS_METHOD_SIGNATURE);
        given(methodSignature.getParameterNames()).willReturn(new String[]{});
        given(joinPointNoArgsSignatureMock.getSignature()).willReturn(methodSignature);
    }

    private void fakeArgsSignatureConfiguration() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        given(methodSignature.getDeclaringType()).willReturn(FakeClass.class);
        given(methodSignature.getName()).willReturn(WITH_ARGS_METHOD_SIGNATURE);
        given(methodSignature.getParameterNames()).willReturn(new String[]{
                ARGUMENT_1_NAME, ARGUMENT_2_NAME
        });
        given(joinPointWithArgsSignatureMock.getSignature()).willReturn(methodSignature);
        given(joinPointWithArgsSignatureMock.getArgs()).willReturn(new Object[]{
                ARGUMENT_1_VALUE, ARGUMENT_2_VALUE
        });
    }

    private void fakeComplexArgsSignatureConfiguration() {
        MethodSignature methodSignature = mock(MethodSignature.class);
        given(methodSignature.getDeclaringType()).willReturn(FakeClass.class);
        given(methodSignature.getName()).willReturn(WITH_COMPLEX_ARGS_METHOD_SIGNATURE);
        given(methodSignature.getParameterNames()).willReturn(new String[]{
                COMPLEX_ARG_NAME
        });
        given(joinPointWithComplexArgsSignatureMock.getSignature()).willReturn(methodSignature);
        given(joinPointWithComplexArgsSignatureMock.getArgs()).willReturn(new Object[]{
                COMPLEX_ARGUMENT_VALUE
        });
    }
}
