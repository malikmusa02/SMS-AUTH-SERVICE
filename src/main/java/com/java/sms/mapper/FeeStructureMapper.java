package com.java.sms.mapper;



import com.java.sms.DataClass.FeeStructureRequest;
import com.java.sms.model.FeeStructure;
import com.java.sms.model.MasterFee;
import com.java.sms.model.enums.FeeType;
import com.java.sms.response.FeeStructureResponse;

import java.util.HashSet;


public final class FeeStructureMapper {

    private FeeStructureMapper() {}

    public static FeeStructureResponse toResponse(FeeStructure entity) {
        if (entity == null)
            return null;

        return FeeStructureResponse.builder()
                .id(entity.getId())
                .masterFeeId(entity.getMasterFee() != null ? entity.getMasterFee().getId() : null)
                .feeType(entity.getFeeType() != null ? entity.getFeeType().name() : null)
                .feeAmount(entity.getFeeAmount())
                .yearLevelIds(entity.getYearLevelIds())
                .build();
    }





    public static FeeStructure toEntity(FeeStructureRequest req, MasterFee masterFee) {

//        FeeStructure e = new FeeStructure();
//        e.setMasterFee(masterFee);
//        e.setFeeType(normalizeFeeType(req.getFeeType()));
//        e.setFeeAmount(req.getFeeAmount());
//        e.setYearLevelIds(req.getYearLevelIds());
//        return e;

            FeeStructure fs = new FeeStructure();
            fs.setMasterFee(masterFee);
            // if you have enum parsing
            fs.setFeeType(FeeType.valueOf(req.getFeeType())); // adjust if needed
            fs.setFeeAmount(req.getFeeAmount());

            if (req.getYearLevelIds() != null) {
                fs.setYearLevelIds(new HashSet<>(req.getYearLevelIds()));
            } else {
                fs.setYearLevelIds(new HashSet<>());
            }
            return fs;


    }




    public static void updateFromRequest(FeeStructureRequest req, FeeStructure entity, MasterFee masterFee) {

        entity.setMasterFee(masterFee);

        entity.setFeeType(normalizeFeeType(req.getFeeType()));

        entity.setFeeAmount(req.getFeeAmount());

        entity.setYearLevelIds(req.getYearLevelIds());

    }

    private static FeeType normalizeFeeType(String raw) {

        if (raw == null) return null;

        // accept "Tuition Fee", "TUITION_FEE" or "tuition_fee"

        String s = raw.trim().replace(" ", "_").replace("-", "_").toUpperCase();
        return FeeType.valueOf(s);

    }

}

