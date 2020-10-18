package com.example.project.controller;

import com.example.project.service.YouTubeApiService;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/project")
@Slf4j
@AllArgsConstructor
public class YtController {

    YouTubeApiService service;

    @PostMapping(value = "/", produces = MediaType.APPLICATION_XML_VALUE)
    @ApiOperation("Retrieve all videos metadata containing the word title")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void searchYouTube(@RequestParam(value = "search") String search,
                              @RequestParam(value = "items", required = false, defaultValue = "25") String items) {
        int max = Integer.parseInt(items);
        service.youTubeSearch(search, max);
    }
}
