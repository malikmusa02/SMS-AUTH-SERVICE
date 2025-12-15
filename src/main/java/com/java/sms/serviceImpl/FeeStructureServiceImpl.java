package com.java.sms.serviceImpl;


import com.java.sms.DataClass.FeeStructureRequest;
import com.java.sms.exception.ApiException;
import com.java.sms.mapper.FeeStructureMapper;
import com.java.sms.model.FeeStructure;
import com.java.sms.model.MasterFee;
import com.java.sms.openFeignClient.YearLevelClient;
import com.java.sms.repository.FeeStructureRepository;
import com.java.sms.repository.MasterFeeRepository;
import com.java.sms.response.FeeStructureResponse;
import com.java.sms.service.FeeStructureService;
import feign.FeignException;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class FeeStructureServiceImpl implements FeeStructureService {

    private final FeeStructureRepository repo;
    private final MasterFeeRepository masterFeeRepo;
    private final YearLevelClient yearLevelClient;


    public FeeStructureServiceImpl(FeeStructureRepository repo,
                                   MasterFeeRepository masterFeeRepo,
                                   YearLevelClient yearLevelClient) {
        this.repo = repo;
        this.masterFeeRepo = masterFeeRepo;
        this.yearLevelClient = yearLevelClient;
    }



    @Override
    public FeeStructureResponse create(FeeStructureRequest request) {

        MasterFee master = masterFeeRepo.findById(request.getMasterFeeId())
                .orElseThrow(() -> new ApiException("MasterFee not found: " + request.getMasterFeeId(),HttpStatus.NOT_FOUND));

        Set<Long> validated = validateYearLevels(request.getYearLevelIds());

        FeeStructure entity = FeeStructureMapper.toEntity(request, master);
        entity.setYearLevelIds(validated);

        FeeStructure saved = repo.save(entity);
        return FeeStructureMapper.toResponse(saved);
    }






    @Override
    public FeeStructureResponse update(Long id, FeeStructureRequest request) {

        FeeStructure existing = repo.findById(id)
                .orElseThrow(() -> new ApiException("FeeStructure not found: " + id,HttpStatus.NOT_FOUND));


        MasterFee master = masterFeeRepo.findById(request.getMasterFeeId())
                .orElseThrow(() -> new ApiException("MasterFee not found: " + request.getMasterFeeId(), HttpStatus.NOT_FOUND));


        Set<Long> validated = validateYearLevels(request.getYearLevelIds());

        FeeStructureMapper.updateFromRequest(request, existing, master);
        existing.setYearLevelIds(validated);

        FeeStructure saved = repo.save(existing);
        return FeeStructureMapper.toResponse(saved);

    }





    @Override
    public FeeStructureResponse findById(Long id) {

        return repo.findById(id).map(FeeStructureMapper::toResponse)
                .orElseThrow(() -> new ApiException("FeeStructure not found: " + id,HttpStatus.NOT_FOUND));
    }




    @Override
    public List<FeeStructureResponse> findAll() {
        return repo.findAll().stream()
                .map(FeeStructureMapper::toResponse)
                .collect(Collectors.toList());
    }




    @Override
    public List<FeeStructureResponse> findByYearLevelId(Long yearLevelId) {
        return repo.findByYearLevelId(yearLevelId)
                .stream().map(FeeStructureMapper::toResponse)
                .collect(Collectors.toList());
    }




    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ApiException("FeeStructure not found: " + id, HttpStatus.NOT_FOUND);
        repo.deleteById(id);
    }





    // Validate each YearLevel id exists in Django via Feign. Throws ApiException if upstream fails.
    private Set<Long> validateYearLevels(Set<Long> yearLevelIds) {

        if (yearLevelIds == null || yearLevelIds.isEmpty())
            return Collections.emptySet();

        Set<Long> validated = new HashSet<>();

        for (Long id : yearLevelIds) {

            try {
                yearLevelClient.getYearLevelById(id); // will throw FeignException.NotFound if missing
                validated.add(id);

            } catch (FeignException.NotFound nf) {
                throw new ApiException("YearLevel not found in Django: " + id,
                        HttpStatus.BAD_REQUEST);

            } catch (FeignException fe) {
                throw new ApiException("Error calling YearLevel service: " + fe.status(),
                        HttpStatus.SERVICE_UNAVAILABLE);
            }

        }

        return validated;
    }


}

