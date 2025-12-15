package com.java.sms.mapper;


import com.java.sms.DataClass.MasterFeeRequest;
import com.java.sms.model.MasterFee;
import com.java.sms.model.enums.PaymentStructure;
import com.java.sms.response.MasterFeeResponse;

public final class MasterFeeMapper {

    private MasterFeeMapper() {}




    public static MasterFee toEntity(MasterFeeRequest req) {

        MasterFee m = new MasterFee();
        m.setPaymentStructure(PaymentStructure.fromString(req.getPaymentStructure()));
        return m;

    }



    public static void updateEntity(MasterFeeRequest req, MasterFee entity) {

        entity.setPaymentStructure(PaymentStructure.fromString(req.getPaymentStructure()));

    }



    public static MasterFeeResponse toResponse(MasterFee entity) {

        return MasterFeeResponse.builder()
                .id(entity.getId())
                .paymentStructure(entity.getPaymentStructure() != null
                        ? entity.getPaymentStructure().name().toLowerCase()
                        : null)
                .build();
    }


}

