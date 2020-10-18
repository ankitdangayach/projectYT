package com.example.project.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

@DisplayName("YT Controller Test")
@ExtendWith(SpringExtension.class)
class YouTubeApiServiceTest {

    @Mock
    JmsTemplate mockJmsTemplate;


    @InjectMocks
    YouTubeApiService subject;

    @Captor
    private ArgumentCaptor<String> queueAMessageCaptor;

    private static final String search = "telecom";
    private static final int max = 1;

    @Nested
    @DisplayName("Calling you tube search")
    class CallingYouTubeSearch {

        @Test
        @DisplayName("Should send message in queue A")
        void shouldSendMessageInQueueA() {
            subject.youTubeSearch(search, max);
            verify(mockJmsTemplate).convertAndSend(anyString(), queueAMessageCaptor.capture());
            String capturedQueueAMessageCaptor = queueAMessageCaptor.getValue();

            assertThat(capturedQueueAMessageCaptor).doesNotContain("{url}");
            assertThat(capturedQueueAMessageCaptor).doesNotContain("{videoTitle}");
            assertThat(capturedQueueAMessageCaptor).containsIgnoringCase("telecom");
        }

    }

}