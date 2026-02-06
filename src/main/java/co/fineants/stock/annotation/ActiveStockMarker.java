package co.fineants.stock.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface ActiveStockMarker {
	String resourceId();      // SpEL 표현식 (예: "#id", "#ticker")

	ResourceType type();      // 리소스 타입
}
