package com.ndumiso.SpringBatchDemo.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClientBusinessAddress {

    private String addressLineOne;

    private String addressLineTwo;

    private String addressLineThree;

    private String postalCode;

    // and so on ....

}
