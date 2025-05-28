package co.fineants.api.domain.member.service;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import co.fineants.AbstractContainerBaseTest;
import co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException;

class ExchangeRateErrorMailHtmlRenderTest extends AbstractContainerBaseTest {

	@Autowired
	private ExchangeRateErrorMailHtmlRender render;

	@DisplayName("환율 업데이트 실패 알림 HTML 렌더링 테스트")
	@Test
	void givenExternalApiGetRequestException_whenRender_thenReturnHtml() {
		// given
		ExternalApiGetRequestException exception = new ExternalApiGetRequestException("Failed to fetch exchange rates",
			HttpStatus.BAD_REQUEST);
		String apiUrl = "https://exchange-rate-api1.p.rapidapi.com/latest";
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		String stackTrace = sw.toString();
		LocalDateTime failedAt = LocalDateTime.parse("2025-05-28T15:06:49.189586");
		Map<String, Object> variables = Map.of(
			"failedAt", failedAt.toString(),
			"apiUrl", apiUrl,
			"errorMessage", exception.getErrorCodeMessage(),
			"stackTrace", stackTrace
		);
		// when
		String html = render.render(variables);
		// then
		String expected = """
			<!doctype html>
			<html lang="ko">
			<head>
			    <meta charset="UTF-8">
			    <meta name="viewport"
			          content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
			    <meta http-equiv="X-UA-Compatible" content="IE=edge">
			    <title>환율 업데이트 실패 알림</title>
			</head>
			<body style="font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;">
			<div style="max-width: 600px; margin: 40px auto; background-color: #ffffff; padding: 30px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.05);">
			    <h2 style="color: #d32f2f;">환율 정보 업데이트 실패 알림</h2>
			    <p>안녕하세요, 관리자님.</p>
			    <p>금일 환율 정보를 업데이트하는 과정에서 아래와 같은 오류가 발생하였습니다.</p>
			   
			    <ul style="line-height: 1.6; color: #333;">
			        <li><strong>실패 시각:</strong> <span>2025-05-28T15:06:49.189586</span></li>
			        <li><strong>대상 API:</strong> <span>https://exchange-rate-api1.p.rapidapi.com/latest</span></li>
			        <li><strong>오류 메시지:</strong> <span>External API Get Request Error</span></li>
			    </ul>
			   
			    <p>해당 문제로 인해 서비스 내 환율 정보가 갱신되지 않았습니다.<br>
			        신속한 조치를 부탁드립니다.</p>
			   
			   
			    <div style="margin-top: 30px;">
			        <h3 style="color: #d32f2f;">에러 스택 트레이스</h3>
			        <pre style="background-color: #f8f8f8; padding: 15px; border-radius: 6px; color: #444; font-size: 13px; overflow-x: auto;">co.fineants.api.global.errors.exception.business.ExternalApiGetRequestException: Failed to fetch exchange rates
				at co.fineants.api.domain.member.service.ExchangeRateErrorMailHtmlRenderTest.givenExternalApiGetRequestException_whenRender_thenReturnHtml(ExchangeRateErrorMailHtmlRenderTest.java:26)
				at java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:104)
				at java.base/java.lang.reflect.Method.invoke(Method.java:578)
				at org.junit.platform.commons.util.ReflectionUtils.invokeMethod(ReflectionUtils.java:727)
				at org.junit.jupiter.engine.execution.MethodInvocation.proceed(MethodInvocation.java:60)
				at org.junit.jupiter.engine.execution.InvocationInterceptorChain$ValidatingInvocation.proceed(InvocationInterceptorChain.java:131)
				at org.junit.jupiter.engine.extension.TimeoutExtension.intercept(TimeoutExtension.java:156)
				at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestableMethod(TimeoutExtension.java:147)
				at org.junit.jupiter.engine.extension.TimeoutExtension.interceptTestMethod(TimeoutExtension.java:86)
				at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker$ReflectiveInterceptorCall.lambda$ofVoidMethod$0(InterceptingExecutableInvoker.java:103)
				at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.lambda$invoke$0(InterceptingExecutableInvoker.java:93)
				at org.junit.jupiter.engine.execution.InvocationInterceptorChain$InterceptedInvocation.proceed(InvocationInterceptorChain.java:106)
				at org.junit.jupiter.engine.execution.InvocationInterceptorChain.proceed(InvocationInterceptorChain.java:64)
				at org.junit.jupiter.engine.execution.InvocationInterceptorChain.chainAndInvoke(InvocationInterceptorChain.java:45)
				at org.junit.jupiter.engine.execution.InvocationInterceptorChain.invoke(InvocationInterceptorChain.java:37)
				at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:92)
				at org.junit.jupiter.engine.execution.InterceptingExecutableInvoker.invoke(InterceptingExecutableInvoker.java:86)
				at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.lambda$invokeTestMethod$7(TestMethodTestDescriptor.java:217)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.invokeTestMethod(TestMethodTestDescriptor.java:213)
				at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:138)
				at org.junit.jupiter.engine.descriptor.TestMethodTestDescriptor.execute(TestMethodTestDescriptor.java:68)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:151)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
				at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
				at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
				at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
				at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
				at java.base/java.util.ArrayList.forEach(ArrayList.java:1511)
				at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.invokeAll(SameThreadHierarchicalTestExecutorService.java:41)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$6(NodeTestTask.java:155)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$8(NodeTestTask.java:141)
				at org.junit.platform.engine.support.hierarchical.Node.around(Node.java:137)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.lambda$executeRecursively$9(NodeTestTask.java:139)
				at org.junit.platform.engine.support.hierarchical.ThrowableCollector.execute(ThrowableCollector.java:73)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.executeRecursively(NodeTestTask.java:138)
				at org.junit.platform.engine.support.hierarchical.NodeTestTask.execute(NodeTestTask.java:95)
				at org.junit.platform.engine.support.hierarchical.SameThreadHierarchicalTestExecutorService.submit(SameThreadHierarchicalTestExecutorService.java:35)
				at org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutor.execute(HierarchicalTestExecutor.java:57)
				at org.junit.platform.engine.support.hierarchical.HierarchicalTestEngine.execute(HierarchicalTestEngine.java:54)
				at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:147)
				at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:127)
				at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:90)
				at org.junit.platform.launcher.core.EngineExecutionOrchestrator.lambda$execute$0(EngineExecutionOrchestrator.java:55)
				at org.junit.platform.launcher.core.EngineExecutionOrchestrator.withInterceptedStreams(EngineExecutionOrchestrator.java:102)
				at org.junit.platform.launcher.core.EngineExecutionOrchestrator.execute(EngineExecutionOrchestrator.java:54)
				at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:114)
				at org.junit.platform.launcher.core.DefaultLauncher.execute(DefaultLauncher.java:86)
				at org.junit.platform.launcher.core.DefaultLauncherSession$DelegatingLauncher.execute(DefaultLauncherSession.java:86)
				at org.junit.platform.launcher.core.SessionPerRequestLauncher.execute(SessionPerRequestLauncher.java:53)
				at com.intellij.junit5.JUnit5IdeaTestRunner.startRunnerWithArgs(JUnit5IdeaTestRunner.java:57)
				at com.intellij.rt.junit.IdeaTestRunner$Repeater$1.execute(IdeaTestRunner.java:38)
				at com.intellij.rt.execution.junit.TestsRepeater.repeat(TestsRepeater.java:11)
				at com.intellij.rt.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:35)
				at com.intellij.rt.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:232)
				at com.intellij.rt.junit.JUnitStarter.main(JUnitStarter.java:55)
			</pre>
			    </div>
			   
			    <p>감사합니다.</p>
			   
			    <hr style="margin: 30px 0; border: none; border-top: 1px solid #eee;">
			    <p style="font-size: 14px; color: #888;">FineAnts 환율 시스템 모니터링 서비스</p>
			</div>
			</body>
			</html>
			""";
		Assertions.assertThat(html).isEqualTo(expected);
	}

}
