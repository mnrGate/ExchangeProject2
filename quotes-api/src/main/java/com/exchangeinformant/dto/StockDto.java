package com.exchangeinformant.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created in IntelliJ
 * User: e-davidenko
 * Date: 21.12.2022
 * Time: 13:52
 */

@Data
@NoArgsConstructor
public class StockDto {

    private double currentPrice;
}
