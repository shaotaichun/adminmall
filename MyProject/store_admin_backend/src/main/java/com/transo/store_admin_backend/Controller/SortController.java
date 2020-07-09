package com.transo.store_admin_backend.Controller;

import com.alibaba.fastjson.JSONObject;
import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.Advertisement;
import com.transo.store_bean.Entity.FirstClassification;
import com.transo.store_bean.Entity.SecondClassification;
import com.transo.store_bean.Entity.Store;
import com.transo.store_repository.Repository.*;
import com.transo.store_tools.Tools.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "排序操作", tags = "排序操作")
@RequestMapping("/backEndAdminSort")
public class SortController {
    @Value("${token.key}")
    private String token_key;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private SecondStandardsRepository secondStandardsRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderAndGoodsRepository orderAndGoodsRepository;
    @Autowired
    private BaseAddressRepository baseAddressRepository;
    @Autowired
    private StoreTypeRepository storeTypeRepository;
    @Autowired
    private AdvertisementRepository advertisementRepository;
    @Autowired
    private FirstClassificationRepository firstClassificationRepository;
    @Autowired
    private SecondClassificationRepository secondClassificationRepository;
    @PostMapping(value = "SortStore")
    @ApiOperation(value = "对商铺进行排序")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "fsortNum", value = "排序号码1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "fid", value = "商铺ID1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lsortNum", value = "排序号码2", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lid", value = "商铺ID2", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult SortStore(@RequestBody JSONObject obj){
        int fsortNum = obj.getInteger("fsortNum");
        int fid = obj.getInteger("fid");
        Store allByShopId1 = storeRepository.findAllByShopId(fid);
        allByShopId1.setSort(fsortNum);
        storeRepository.save(allByShopId1);
        int lsortNum = obj.getInteger("lsortNum");
        int lid = obj.getInteger("lid");
        Store allByShopId2 = storeRepository.findAllByShopId(lid);
        allByShopId2.setSort(lsortNum);
        storeRepository.save(allByShopId2);
        return new JsonResult(200);
    }
    @PostMapping(value = "SortAdvertisement")
    @ApiOperation(value = "对广告进行排序")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "fsortNum", value = "排序号码1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "fid", value = "广告ID1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lsortNum", value = "排序号码2", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lid", value = "广告ID2", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult SortAdvertisement(@RequestBody JSONObject obj){
        int fsortNum = obj.getInteger("fsortNum");
        int fid = obj.getInteger("fid");
        Advertisement allByAdId1 = advertisementRepository.findAllByAdId(fid);
        allByAdId1.setSort(fsortNum);
        advertisementRepository.save(allByAdId1);
        int lsortNum = obj.getInteger("lsortNum");
        int lid = obj.getInteger("lid");
        Advertisement allByAdId2 = advertisementRepository.findAllByAdId(lid);
        allByAdId2.setSort(lsortNum);
        advertisementRepository.save(allByAdId2);
        return new JsonResult(200);

    }
    @PostMapping(value = "SortFirstClassification")
    @ApiOperation(value = "对一级分类进行排序")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "fsortNum", value = "排序号码1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "fid", value = "广告ID1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lsortNum", value = "排序号码2", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lid", value = "广告ID2", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult SortFirstClassification(@RequestBody JSONObject obj){
        int fsortNum = obj.getInteger("fsortNum");
        int fid = obj.getInteger("fid");
        FirstClassification allById = firstClassificationRepository.findAllById(fid);
        allById.setSort(fsortNum);
        firstClassificationRepository.save(allById);
        int lsortNum = obj.getInteger("lsortNum");
        int lid = obj.getInteger("lid");
        FirstClassification allByAdId2 = firstClassificationRepository.findAllById(lid);
        allByAdId2.setSort(lsortNum);
        firstClassificationRepository.save(allByAdId2);
        return new JsonResult(200);

    }
    @PostMapping(value = "SortSecondClassification")
    @ApiOperation(value = "对二级级分类进行排序")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "fsortNum", value = "排序号码1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "fid", value = "广告ID1", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lsortNum", value = "排序号码2", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "body", name = "lid", value = "广告ID2", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult SortSecondClassification(@RequestBody JSONObject obj){
        int fsortNum = obj.getInteger("fsortNum");
        int fid = obj.getInteger("fid");
        SecondClassification allByAdId1 = secondClassificationRepository.findAllByIdAndIsDelete(fid,"1");
        allByAdId1.setSort(fsortNum);
        secondClassificationRepository.save(allByAdId1);
        int lsortNum = obj.getInteger("lsortNum");
        int lid = obj.getInteger("lid");
        SecondClassification allByAdId2 = secondClassificationRepository.findAllByIdAndIsDelete(lid,"1");
        allByAdId2.setSort(lsortNum);
        secondClassificationRepository.save(allByAdId2);
        return new JsonResult(200);

    }
}
