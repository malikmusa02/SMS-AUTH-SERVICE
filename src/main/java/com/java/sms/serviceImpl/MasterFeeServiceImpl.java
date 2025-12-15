package com.java.sms.serviceImpl;


import com.java.sms.DataClass.MasterFeeRequest;
import com.java.sms.exception.ApiException;
import com.java.sms.mapper.MasterFeeMapper;
import com.java.sms.model.MasterFee;
import com.java.sms.model.enums.PaymentStructure;
import com.java.sms.repository.MasterFeeRepository;
import com.java.sms.response.MasterFeeResponse;
import com.java.sms.service.MasterFeeService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class MasterFeeServiceImpl implements MasterFeeService {


    private final MasterFeeRepository repo;

    public MasterFeeServiceImpl(MasterFeeRepository repo) {
        this.repo = repo;
    }




    @Override
    public MasterFeeResponse create(MasterFeeRequest req) {

        MasterFee entity = MasterFeeMapper.toEntity(req);
        MasterFee saved = repo.save(entity);
        return MasterFeeMapper.toResponse(saved);

//        MasterFee masterFee = new MasterFee();
//        masterFee.setPaymentStructure(PaymentStructure.valueOf(req.getPaymentStructure()));
//        MasterFee save = repo.save(masterFee);
//        return MasterFeeMapper.toResponse(save);


    }




    @Override
    public MasterFeeResponse update(Long id, MasterFeeRequest req) {


        MasterFee existing = repo.findById(id)
                .orElseThrow(() -> new ApiException("MasterFee not found: " + id, HttpStatus.NOT_FOUND));


        MasterFeeMapper.updateEntity(req, existing);
        MasterFee saved = repo.save(existing);
        return MasterFeeMapper.toResponse(saved);

    }




    @Override
    public MasterFeeResponse findById(Long id) {

        return repo.findById(id)
                .map(MasterFeeMapper::toResponse)
                .orElseThrow(() -> new ApiException("MasterFee not found: " + id, HttpStatus.NOT_FOUND));

    }




    @Override
    public List<MasterFeeResponse> findAll() {

        return repo.findAll().stream()
                .map(MasterFeeMapper::toResponse)
                .collect(Collectors.toList());

    }




    @Override
    public void delete(Long id) {

        if (!repo.existsById(id)) {
            throw new ApiException("MasterFee not found: " + id, HttpStatus.NOT_FOUND);
        }

        repo.deleteById(id);

    }



}
