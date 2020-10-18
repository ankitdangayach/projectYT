package com.example.project.controller;

import com.example.project.service.YouTubeApiService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.Mockito.verify;

@DisplayName("YT Controller Test")
@ExtendWith(SpringExtension.class)
class YtControllerTest {

    @Mock
    YouTubeApiService mockYouTubeApiService;

    @InjectMocks
    YtController subject;

    private static final String search = "telecom";
    private static final String items = "100";

    @Nested
    @DisplayName("Calling search you tube")
    class CallingSearchYouTube {
        @Test
        @DisplayName("Should call youtube api service to search videos")
        void shouldCallYoutubeApiServiceToSearchVideos() {
            int max = Integer.parseInt(items);
            subject.searchYouTube(search, items);
            verify(mockYouTubeApiService).youTubeSearch(search, max);
        }
    }


}