package com.example.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class YouTubeItem {

    private String url;
    private String title;
    private String thumbnail;
    private String description;
}
