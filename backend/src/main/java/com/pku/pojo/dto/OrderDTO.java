package com.pku.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
public class OrderDTO {
    private Long userId;
    private Long goodsId;
    private Integer goodsCount;
}
