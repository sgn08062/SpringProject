package com.example.myFarm.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// 이 어노테이션이 어디에 적용될 수 있는가? -> ElementType.METHOD 는 메서드 위에만 적용할 수 있다는 의미
// 추후에 메서드가 아닌 다른 곳에 적용이 필요할 시 변경 요함
@Target(ElementType.METHOD) 
// 이 어노테이션의 정보를 언제까지 유지할 것인가? -> RetentionPolicy.RUNTIME 는 프로그램 실행 중을 의미
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginCheck {
    // 실제 사용 시 @LoginCheck 라는 이름으로 커스텀 어노테이션을 사용한다.
}
