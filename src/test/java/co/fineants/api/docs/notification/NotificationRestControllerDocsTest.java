package co.fineants.api.docs.notification;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.payload.JsonFieldType;

import co.fineants.api.docs.RestDocsSupport;
import co.fineants.api.domain.common.notification.PortfolioMaximumLossNotifiable;
import co.fineants.api.domain.common.notification.PortfolioTargetGainNotifiable;
import co.fineants.api.domain.common.notification.TargetPriceNotificationNotifiable;
import co.fineants.api.domain.member.domain.entity.Member;
import co.fineants.api.domain.notification.controller.NotificationRestController;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.NotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessage;
import co.fineants.api.domain.notification.domain.dto.response.PortfolioNotifyMessageItem;
import co.fineants.api.domain.notification.domain.dto.response.StockNotifyMessage;
import co.fineants.api.domain.notification.domain.entity.Notification;
import co.fineants.api.domain.notification.service.NotificationService;
import co.fineants.api.domain.portfolio.domain.entity.Portfolio;
import co.fineants.api.domain.stock.domain.entity.Stock;
import co.fineants.api.domain.stock_target_price.domain.dto.response.TargetPriceNotifyMessageItem;
import co.fineants.api.domain.stock_target_price.domain.entity.StockTargetPrice;
import co.fineants.api.domain.stock_target_price.domain.entity.TargetPriceNotification;

class NotificationRestControllerDocsTest extends RestDocsSupport {

	private final NotificationService service = Mockito.mock(NotificationService.class);

	@Override
	protected Object initController() {
		return new NotificationRestController(service);
	}

	@DisplayName("사용자는 한 포트폴리오의 목표 수익률 도달 알림을 전송받습니다")
	@Test
	void notifyPortfolioTargetGainMessages() throws Exception {
		// given
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		PortfolioNotifyMessage message = (PortfolioNotifyMessage)PortfolioTargetGainNotifiable.from(portfolio, true)
			.createMessage("token");
		Notification notification = createPortfolioNotification(message, portfolio, member);
		PortfolioNotifyMessageItem item = (PortfolioNotifyMessageItem)PortfolioNotifyMessageItem.from(notification);
		List<NotifyMessageItem> items = List.of(item);

		given(service.notifyTargetGain(anyLong())).willReturn(items);

		// when
		mockMvc.perform(post("/api/notifications/portfolios/{portfolioId}/notify/target-gain", portfolio.getId())
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 목표 수익률 알림 메시지가 전송되었습니다")))
			.andExpect(
				jsonPath("data.notifications[0].notificationId").value(equalTo(item.getNotificationId().intValue())))
			.andExpect(jsonPath("data.notifications[0].isRead").value(equalTo(item.getIsRead())))
			.andExpect(jsonPath("data.notifications[0].title").value(equalTo(item.getTitle())))
			.andExpect(jsonPath("data.notifications[0].content").value(equalTo(item.getContent())))
			.andExpect(jsonPath("data.notifications[0].type").value(equalTo(item.getType().name())))
			.andExpect(jsonPath("data.notifications[0].referenceId").value(equalTo(item.getReferenceId())))
			.andExpect(jsonPath("data.notifications[0].memberId").value(equalTo(item.getMemberId().intValue())))
			.andExpect(jsonPath("data.notifications[0].link").value(equalTo(item.getLink())))
			.andExpect(jsonPath("data.notifications[0].messageIds").value(equalTo(item.getMessageIds())))
			.andExpect(jsonPath("data.notifications[0].name").value(equalTo(item.getName())))
			.andDo(
				document(
					"notification_portfolio_target_gain-notify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.notifications[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록번호"),
						fieldWithPath("data.notifications[].isRead").type(JsonFieldType.BOOLEAN)
							.description("알림 읽음 여부"),
						fieldWithPath("data.notifications[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.notifications[].content").type(JsonFieldType.STRING)
							.description("알림 내용"),
						fieldWithPath("data.notifications[].type").type(JsonFieldType.STRING)
							.description("알림 종류"),
						fieldWithPath("data.notifications[].referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호"),
						fieldWithPath("data.notifications[].memberId").type(JsonFieldType.NUMBER)
							.description("회원 등록 번호"),
						fieldWithPath("data.notifications[].link").type(JsonFieldType.STRING)
							.description("알림 링크"),
						fieldWithPath("data.notifications[].messageIds").type(JsonFieldType.ARRAY)
							.description("알림 메시지 아이디"),
						fieldWithPath("data.notifications[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름")
					)
				)
			);
	}

	@DisplayName("사용자는 한 포트폴리오의 최대 손실율 도달 알림을 전송받습니다")
	@Test
	void notifyPortfolioMaxLossMessages() throws Exception {
		// given
		Long memberId = 1L;
		Member member = createMember();
		Portfolio portfolio = createPortfolio(member);
		PortfolioNotifyMessage message = (PortfolioNotifyMessage)PortfolioMaximumLossNotifiable.from(portfolio, true)
			.createMessage("token");

		Notification notification = createPortfolioNotification(message, portfolio, member);
		PortfolioNotifyMessageItem item = PortfolioNotifyMessageItem.builder()
			.notificationId(notification.getId())
			.isRead(notification.getIsRead())
			.title(notification.getTitle())
			.content(notification.getContent())
			.type(notification.getType())
			.referenceId(notification.getReferenceId())
			.memberId(memberId)
			.link(notification.getLink())
			.messageIds(notification.getMessageIds())
			.name(notification.getName())
			.build();
		List<NotifyMessageItem> items = List.of(item);
		given(service.notifyMaxLoss(anyLong())).willReturn(items);

		// when
		mockMvc.perform(post("/api/notifications/portfolios/{portfolioId}/notify/max-loss", portfolio.getId())
				.cookie(createTokenCookies()))
			.andExpect(status().isOk())
			.andExpect(jsonPath("code").value(equalTo(200)))
			.andExpect(jsonPath("status").value(equalTo("OK")))
			.andExpect(jsonPath("message").value(equalTo("포트폴리오 최대 손실율 알림 메시지가 전송되었습니다")))
			.andExpect(
				jsonPath("data.notifications[0].notificationId").value(equalTo(item.getNotificationId().intValue())))
			.andExpect(jsonPath("data.notifications[0].isRead").value(equalTo(item.getIsRead())))
			.andExpect(jsonPath("data.notifications[0].title").value(equalTo(item.getTitle())))
			.andExpect(jsonPath("data.notifications[0].content").value(equalTo(item.getContent())))
			.andExpect(jsonPath("data.notifications[0].type").value(equalTo(item.getType().name())))
			.andExpect(jsonPath("data.notifications[0].referenceId").value(equalTo(item.getReferenceId())))
			.andExpect(jsonPath("data.notifications[0].memberId").value(equalTo(item.getMemberId().intValue())))
			.andExpect(jsonPath("data.notifications[0].link").value(equalTo(item.getLink())))
			.andExpect(jsonPath("data.notifications[0].messageIds").value(equalTo(item.getMessageIds())))
			.andExpect(jsonPath("data.notifications[0].name").value(equalTo(item.getName())))
			.andDo(
				document(
					"notification_portfolio_max_loss-notify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					pathParameters(
						parameterWithName("portfolioId").description("포트폴리오 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.notifications[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록번호"),
						fieldWithPath("data.notifications[].isRead").type(JsonFieldType.BOOLEAN)
							.description("알림 읽음 여부"),
						fieldWithPath("data.notifications[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.notifications[].content").type(JsonFieldType.STRING)
							.description("알림 내용"),
						fieldWithPath("data.notifications[].type").type(JsonFieldType.STRING)
							.description("알림 종류"),
						fieldWithPath("data.notifications[].referenceId").type(JsonFieldType.STRING)
							.description("참조 등록번호"),
						fieldWithPath("data.notifications[].memberId").type(JsonFieldType.NUMBER)
							.description("회원 등록 번호"),
						fieldWithPath("data.notifications[].link").type(JsonFieldType.STRING)
							.description("알림 링크"),
						fieldWithPath("data.notifications[].messageIds").type(JsonFieldType.ARRAY)
							.description("알림 메시지 아이디"),
						fieldWithPath("data.notifications[].name").type(JsonFieldType.STRING)
							.description("포트폴리오 이름")
					)
				)
			);
	}

	@DisplayName("종목 지정가 알림 발송 API")
	@Test
	void sendStockTargetPriceNotification() throws Exception {
		// given
		Member member = createMember();
		Stock stock = createSamsungStock();
		StockTargetPrice stockTargetPrice = createStockTargetPrice(member, stock);
		TargetPriceNotification targetPriceNotification = createTargetPriceNotification(stockTargetPrice);
		NotifyMessage message = TargetPriceNotificationNotifiable.from(targetPriceNotification, true)
			.createMessage("token");
		Notification notification = createStockNotification((StockNotifyMessage)message, member);
		TargetPriceNotifyMessageItem item = (TargetPriceNotifyMessageItem)TargetPriceNotifyMessageItem.from(
			notification);
		given(service.notifyTargetPrice(anyLong())).willReturn(List.of(item));

		// when
		mockMvc.perform(RestDocumentationRequestBuilders.post("/api/stocks/target-price/notifications/send")
				.queryParam("memberId", String.valueOf(member.getId()))
				.cookie(createTokenCookies()))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("code").value(equalTo(201)))
			.andExpect(jsonPath("status").value(equalTo("Created")))
			.andExpect(jsonPath("message").value(equalTo("종목 지정가 알림을 발송하였습니다")))
			.andExpect(jsonPath("data.notifications").isArray())
			.andExpect(
				jsonPath("data.notifications[0].notificationId").value(equalTo(item.getNotificationId().intValue())))
			.andExpect(jsonPath("data.notifications[0].isRead").value(equalTo(item.getIsRead())))
			.andExpect(jsonPath("data.notifications[0].title").value(equalTo(item.getTitle())))
			.andExpect(jsonPath("data.notifications[0].content").value(equalTo(item.getContent())))
			.andExpect(jsonPath("data.notifications[0].type").value(equalTo(item.getType().name())))
			.andExpect(jsonPath("data.notifications[0].referenceId").value(equalTo(item.getReferenceId())))
			.andExpect(jsonPath("data.notifications[0].memberId").value(equalTo(item.getMemberId().intValue())))
			.andExpect(jsonPath("data.notifications[0].link").value(equalTo(item.getLink())))
			.andExpect(jsonPath("data.notifications[0].messageIds").value(equalTo(item.getMessageIds())))
			.andExpect(jsonPath("data.notifications[0].stockName").value(equalTo(item.getStockName())))
			.andExpect(jsonPath("data.notifications[0].targetPrice").value(
				equalTo(item.getTargetPrice().toInteger().intValue())))
			.andExpect(jsonPath("data.notifications[0].targetPriceNotificationId").value(
				equalTo(item.getTargetPriceNotificationId().intValue())))
			.andDo(
				document(
					"stock_target_price-notification-notify",
					preprocessRequest(prettyPrint()),
					preprocessResponse(prettyPrint()),
					queryParameters(
						parameterWithName("memberId").description("회원 등록번호")
					),
					responseFields(
						fieldWithPath("code").type(JsonFieldType.NUMBER)
							.description("코드"),
						fieldWithPath("status").type(JsonFieldType.STRING)
							.description("상태"),
						fieldWithPath("message").type(JsonFieldType.STRING)
							.description("메시지"),
						fieldWithPath("data").type(JsonFieldType.OBJECT)
							.description("응답 데이터"),
						fieldWithPath("data.notifications").type(JsonFieldType.ARRAY)
							.description("종목 지정가 알림 리스트"),
						fieldWithPath("data.notifications[].notificationId").type(JsonFieldType.NUMBER)
							.description("알림 등록 번호"),
						fieldWithPath("data.notifications[].isRead").type(JsonFieldType.BOOLEAN)
							.description("알림 읽음 여부"),
						fieldWithPath("data.notifications[].title").type(JsonFieldType.STRING)
							.description("알림 제목"),
						fieldWithPath("data.notifications[].content").type(JsonFieldType.STRING)
							.description("알림 내용"),
						fieldWithPath("data.notifications[].type").type(JsonFieldType.STRING)
							.description("알림 종류"),
						fieldWithPath("data.notifications[].referenceId").type(JsonFieldType.STRING)
							.description("참조 등록 아이"),
						fieldWithPath("data.notifications[].memberId").type(JsonFieldType.NUMBER)
							.description("회원 등록 번호"),
						fieldWithPath("data.notifications[].link").type(JsonFieldType.STRING)
							.description("알림 링크"),
						fieldWithPath("data.notifications[].messageIds").type(JsonFieldType.ARRAY)
							.description("알림 메시지 등록번호"),
						fieldWithPath("data.notifications[].stockName").type(JsonFieldType.STRING)
							.description("종목 이름"),
						fieldWithPath("data.notifications[].targetPrice").type(JsonFieldType.NUMBER)
							.description("지정"),
						fieldWithPath("data.notifications[].targetPriceNotificationId").type(JsonFieldType.NUMBER)
							.description("지정가 알림 등록 번호")
					)
				)
			);
	}
}
