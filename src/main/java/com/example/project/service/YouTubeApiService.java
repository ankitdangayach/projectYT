package com.example.project.service;

import com.example.project.dtos.QueueAMessage;
import com.example.project.dtos.YouTubeItem;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class YouTubeApiService {

    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final String PROPERTIES_FILENAME = "youtube";

    private static final String GOOGLE_YOUTUBE_URL = "https://www.youtube.com/watch?v=";
    private static final String YOUTUBE_SEARCH_TYPE = "video";
    private static final String YOUTUBE_SEARCH_FIELDS = "items(id/kind,id/videoId,snippet/title,snippet/description,snippet/thumbnails/default/url)";
    private static final String YOUTUBE_API_APPLICATION = "google-youtube-api-search";
    private static final String YOUTUBE_APIKEY_ENV = "INSERT-YOUR-API-KEY-HERE";

    private static final long NUMBER_OF_VIDEOS_RETURNED = 25;

    private static final YouTube youtube;

    private static final ResourceBundle propertiesBundle;

    private final JmsTemplate jmsTemplate;

    static {

        propertiesBundle = ResourceBundle.getBundle(PROPERTIES_FILENAME);

        youtube = new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY,
                request -> {
                }).setApplicationName(YOUTUBE_API_APPLICATION).build();
    }

    public void youTubeSearch(String searchQuery, int maxSearch) {
        log.info("Starting YouTube search... " + searchQuery);

        List<YouTubeItem> rvalue = new ArrayList<>();

        try {

            if (youtube != null) {

                YouTube.Search.List search = youtube.search().list("snippet");
                String apiKey = System.getenv(YOUTUBE_APIKEY_ENV);

                if (apiKey == null) {
                    apiKey = propertiesBundle.getString("youtube.apikey");
                }

                search.setKey(apiKey);
                search.setQ(searchQuery);
                search.setType(YOUTUBE_SEARCH_TYPE);

                String youTubeFields = propertiesBundle.getString("youtube.fields");

                if (youTubeFields != null && !youTubeFields.isEmpty()) {
                    search.setFields(youTubeFields);
                } else {
                    search.setFields(YOUTUBE_SEARCH_FIELDS);
                }

                if (maxSearch < 1) {
                    String maxVideosReturned = propertiesBundle.getString("youtube.maxvid");

                    if (maxVideosReturned != null && !maxVideosReturned.isEmpty()) {
                        search.setMaxResults(Long.valueOf(maxVideosReturned));
                    } else {
                        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
                    }
                } else {
                    search.setMaxResults((long) maxSearch);
                }

                SearchListResponse searchResponse = search.execute();
                List<SearchResult> searchResultList = searchResponse.getItems();

                if (searchResultList != null && searchResultList.size() > 0) {

                    for (SearchResult youTubeSearchResult : searchResultList) {
                        QueueAMessage queueAMessage = QueueAMessage.builder()
                                .url(GOOGLE_YOUTUBE_URL + youTubeSearchResult.getId().getVideoId())
                                .videoTitle(youTubeSearchResult.getSnippet().getTitle())
                                .build();
                        log.info("Sending the item to QueueA {}", queueAMessage);
                        sendQueueAMessage("QueueA", queueAMessage);
                    }

                } else {
                    log.info("No search results got from YouTube API");
                }

            } else {
                log.warn("YouTube API not initialized correctly!");
            }

        } catch (GoogleJsonResponseException e) {
            log.warn("There was a service error: " + e.getDetails().getCode() + " : "
                    + e.getDetails().getMessage());
        } catch (IOException e) {
            log.warn("There was an IO error: " + e.getCause() + " : " + e.getMessage());
        } catch (Throwable t) {
            log.warn("Severe errors!", t);
            t.printStackTrace();
        }
    }

    private void sendQueueAMessage(String queueName, QueueAMessage queueAMessage) throws IOException {
        String xmlQueueAMessage = readFileFromClasspath("/templates/QueueAMessage.xml");

        xmlQueueAMessage = xmlQueueAMessage.replace("{url}", queueAMessage.getUrl());
        xmlQueueAMessage = xmlQueueAMessage.replace("{videoTitle}", queueAMessage.getVideoTitle());
        log.info("Sending the item to QueueA {}", xmlQueueAMessage);
        jmsTemplate.convertAndSend(queueName, xmlQueueAMessage);
    }

    public static String readFileFromClasspath(final String relativeFilePathInResources) throws IOException {
        InputStream resource = new ClassPathResource(relativeFilePathInResources).getInputStream();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource))) {
            return reader.lines()
                    .collect(Collectors.joining("\n"));
        }
    }
}

