package org.open.address.controller;

import org.open.address.crawler.AddressCrawler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CrawlerController {

    private final AddressCrawler addressCrawler;

    public CrawlerController(AddressCrawler addressCrawler) {
        this.addressCrawler = addressCrawler;
    }

    @PostMapping(value = "/address/crawler")
    public void runCrawler(@RequestParam String type) throws Exception {
        addressCrawler.run(type);
    }

}
