package com.example.project.messaging;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;


@DisplayName("YT Controller Test")
@ExtendWith(SpringExtension.class)
class ReceiverTest {

    @Mock
    JmsTemplate mockJmsTemplate;

    @InjectMocks
    Receiver subject;

    @Captor
    private ArgumentCaptor<String> queueBMessageCaptor;

    private static final String queueAMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<queueamessage>\n" +
            "    <url>https://www.youtube.com/watch?v=pPqjvus2ZEc</url>\n" +
            "    <videoTitle>15 Things You Didn’t Know About The Telecommunication Industry</videoTitle>\n" +
            "</queueamessage>";

    @Nested
    @DisplayName("Calling receiveMessage")
    class CallingReceiveMessage {

        @Test
        @DisplayName("should call convert and send")
        void shouldCallConvertAndSend() throws IOException {
            subject.receiveMessage(queueAMessage);
            verify(mockJmsTemplate).convertAndSend(anyString(), queueBMessageCaptor.capture());
            String capturedQueueAMessageCaptor = queueBMessageCaptor.getValue();

            assertThat(capturedQueueAMessageCaptor).doesNotContain("{url}");
            assertThat(capturedQueueAMessageCaptor).doesNotContain("{videoTitle}");
            assertThat(capturedQueueAMessageCaptor).containsIgnoringCase("telco");

        }

    }

}