package com.kevinguanchedarias.taggablecache.util;

import com.kevinguanchedarias.taggablecache.test.FakeAnnotation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

class AspectUtilsTest {
    @Test
    void findAnnotation_should_work() throws NoSuchMethodException {
        var joinPointMock = mock(JoinPoint.class);
        var methodSignatureMock = mock(MethodSignature.class);
        var fakeMethod = getClass().getMethod("fakeMethod");
        given(joinPointMock.getSignature()).willReturn(methodSignatureMock);
        given(methodSignatureMock.getMethod()).willReturn(fakeMethod);

        var result = AspectUtils.findAnnotation(joinPointMock, FakeAnnotation.class);
        assertThat(result).isInstanceOf(FakeAnnotation.class);
        assertThat(result.value()).isEqualTo("foo");

    }

    @FakeAnnotation("foo")
    public void fakeMethod() {
        // Just for the sake of making Method class spy-able
    }
}
