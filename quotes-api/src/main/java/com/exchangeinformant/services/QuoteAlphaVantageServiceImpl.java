package com.exchangeinformant.services;

import com.exchangeinformant.dto.StockDto;
import com.exchangeinformant.model.Stock;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


/**
 * Created in IntelliJ
 * User: e-davidenko
 * Date: 22.12.2022
 * Time: 9:38
 */
@Service
public class QuoteAlphaVantageServiceImpl implements QuoteService{

    private static final String API_KEY = "R5YMX285BT0WOJZ";

    private final WebClient webClient;

    public QuoteAlphaVantageServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public String getCurrentStock(String stockName) throws IOException, URISyntaxException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=" + stockName + API_KEY))
                .GET()
                .build();
        HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode nodeRoot = objectMapper.readTree(response.body());
        JsonNode nodePrice = nodeRoot.get("Global Quote").get("05. price");
        StockDto stockDto = new StockDto();
        stockDto.setCurrentPrice(nodePrice.asDouble());
        return response.body();
    }
}
