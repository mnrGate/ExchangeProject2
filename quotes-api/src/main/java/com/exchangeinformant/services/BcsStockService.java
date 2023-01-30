package com.exchangeinformant.services;

import com.exchangeinformant.configuration.BcsConfig;
import com.exchangeinformant.dto.InfoDTO;
import com.exchangeinformant.dto.NameDTO;
import com.exchangeinformant.dto.RootDTO;
import com.exchangeinformant.dto.StockDTO;
import com.exchangeinformant.exception.ErrorCodes;
import com.exchangeinformant.exception.QuotesException;
import com.exchangeinformant.model.Info;
import com.exchangeinformant.model.Stock;
import com.exchangeinformant.repository.InfoRepository;
import com.exchangeinformant.repository.NameRepository;
import com.exchangeinformant.repository.StockRepository;
import com.exchangeinformant.util.Bcs;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Created in IntelliJ
 * User: e-davidenko
 * Date: 05.01.2023
 * Time: 17:27
 */
@Service
@Bcs
public class BcsStockService implements StockService {

    private final WebClient webClient;
    private final BcsConfig bcsConfig;
    private final InfoRepository infoRepository;

    private final StockRepository stockRepository;
    private final NameRepository nameRepository;


    public BcsStockService(WebClient webClient, BcsConfig bcsConfig, InfoRepository infoRepository, StockRepository stockRepository, NameRepository nameRepository) {
        this.webClient = webClient;
        this.bcsConfig = bcsConfig;
        this.infoRepository = infoRepository;
        this.stockRepository = stockRepository;
        this.nameRepository = nameRepository;
    }

    @Override
    public void updateAllStocks() {
        List<NameDTO> allStocks = nameRepository.findAll();
        for (NameDTO stock : allStocks) {
            try {
                Mono<List<StockDTO>> mono = webClient
                        .get()
                        .uri(bcsConfig.getUrl() + String.format(bcsConfig.getOneStock(), stock.getSecureCode()))
                        .retrieve()
                        .bodyToMono(new ParameterizedTypeReference<>() {});
                StockDTO stockDTO = Objects.requireNonNull(mono.block()).get(0);
                stockRepository.save(new Stock(stockDTO.getSecureCode(),stockDTO.getIssuer(), stockDTO.getCurrency()));
                InfoDTO infoDTO = stockDTO.getInfoList();
                Info info = convertInfoDTOToInfo(infoDTO);
                info.setSecureCode(stock.getSecureCode());
                infoRepository.save(info);
            } catch (WebClientRequestException e) {
                e.printStackTrace();
                throw new QuotesException(ErrorCodes.UPDATE_PROBLEM.name());
            }
        }
        System.out.printf("%s: Updated Successfully%n", LocalDateTime.now());
    }

    @Scheduled(cron = "0 */1 * * * *")
    @Override
    public void getAllStocks() {
        try {
            List<NameDTO> mono =  webClient
                    .get()
                    .uri(bcsConfig.getUrl() + bcsConfig.getAllStocks())
                    .header("partner-token", bcsConfig.getPartnerToken())
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<RootDTO>(){})
                    .block()
                    .getNameDTO();
            for (NameDTO name : mono) {
                System.out.println(name.getSecureCode() +" " + name.getIssuer());
                nameRepository.save(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("%s: Found Names Successfully%n", LocalDateTime.now());
    }

    @Override
    public List<NameDTO> getAllNames() {
        return  nameRepository.findAll();
    }


//    @Override
//    public List<Stock> getStocksByCodes(List<String> codes) {
//        List<Stock> result = new ArrayList<>();
//        for(String code : codes){
//            result.add(stockRepository.findBySecureCode(code));
//        }
//        return result;
//    }

    private Info convertInfoDTOToInfo(InfoDTO infoDTO) {
        Info info = new Info();
        info.setUpdatedAt(LocalDateTime.now());
        info.setLastPrice(infoDTO.getLastPrice());
        return info;
    }

    private Stock convertNameDTOToStock(NameDTO nameDTO) {
        Stock stock = new Stock();
        stock.setCurrency(nameDTO.getCurrency());
        stock.setIssuer(nameDTO.getIssuer());
        stock.setSecureCode(nameDTO.getSecureCode());
        return stock;
    }





}
