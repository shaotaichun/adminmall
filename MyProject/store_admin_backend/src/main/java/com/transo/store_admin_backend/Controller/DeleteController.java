package com.transo.store_admin_backend.Controller;

import com.alibaba.fastjson.JSONObject;
import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.*;
import com.transo.store_repository.Repository.*;
import com.transo.store_tools.Tools.ExceptionResult;
import com.transo.store_tools.Tools.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.beans.Transient;
import java.util.LinkedList;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "删除操作", tags = "删除操作")
@RequestMapping("/backEndAdminDelete")
public class DeleteController {
    @Value("${token.key}")
    private String token_key;
    @Autowired
    private StoreRepository storeRepository;
    @Autowired
    private GoodsRepository goodsRepository;
    @Autowired
    private GoodsClassificationRepository goodsClassificationRepository;
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
    @Autowired
    private GoodsWayRepository goodsWayRepository;
    @Autowired
    private StoreAndSecondClassificationRepository storeAndSecondClassificationRepository;
    @PostMapping(value = "deleteFirstClassification")
    @ApiOperation(value = "删除一级分类")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "一级分类ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteFirstClassification(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        if (secondClassificationRepository.findAllByFirstClassifyIdAndIsDelete(id,"1").size()>0){
            throw new ExceptionResult("该一级分类下存在二级分类关联,请先删除关联的二级分类!");
        }
        FirstClassification allById = firstClassificationRepository.findAllById(id);
        allById.setIsDelete("0");
        firstClassificationRepository.save(allById);
        return new JsonResult(200);
    }
    @PostMapping(value = "deleteSecondClassification")
    @ApiOperation(value = "删除二级分类")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "一级分类ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteSecondClassification(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        List<StoreAndSecondClassification> allBySecondClassificationId = storeAndSecondClassificationRepository.findAllBySecondClassificationId(id);
        List<Integer> list  = new LinkedList<>();
        for (StoreAndSecondClassification all:allBySecondClassificationId) {
            list.add(all.getStoreId());
        }
        if (storeRepository.findAllByInStoreId(list).size()>0){
            throw new ExceptionResult("该二级分类下存在商铺关联,请先删除关联的商铺!");
        }
        SecondClassification allById = secondClassificationRepository.findAllByIdAndIsDelete(id,"1");
        allById.setIsDelete("0");
        secondClassificationRepository.save(allById);
        return new JsonResult(200);
    }
    @PostMapping(value = "deleteStoreType")
    @ApiOperation(value = "删除商铺类型")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "商铺类型ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteStoreType(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        if (storeRepository.findAllByStoreTypeAndIsDelete(id,"1").size()>0){
            throw new ExceptionResult("该商铺类型下存在商铺关联,请先删除关联的商铺!");
        }
        StoreType allById = storeTypeRepository.findAllByStId(id);
        allById.setIsDelete("0");
        storeTypeRepository.save(allById);
        return new JsonResult(200);
    }
    @PostMapping(value = "deleteStore")
    @ApiOperation(value = "删除商铺")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "商铺类型ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteStore(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        Store allById = storeRepository.findAllByShopId(id);
        allById.setIsDelete("0");
        storeRepository.save(allById);
        return new JsonResult(200);
    }
    @PostMapping(value = "deleteAdvertisement")
    @ApiOperation(value = "删除广告")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "商铺类型ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteAdvertisement(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        Advertisement allById = advertisementRepository.findAllByAdId(id);
        allById.setIsDelete("0");
        advertisementRepository.save(allById);
        return new JsonResult(200);
    }
    @PostMapping(value = "deleteGoods")
    @ApiOperation(value = "删除商品")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "商品ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteGoods(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        Goods allById = goodsRepository.findAllBySpuId(id);
        allById.setIsDelete("0");
        goodsRepository.save(allById);
        return new JsonResult(200);
    }
    @PostMapping(value = "deleteGoodsClassification")
    @ApiOperation(value = "删除商品分类")
    @Transient
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "body", name = "id", value = "商品分类ID", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult deleteGoodsClassification(@RequestBody JSONObject obj){
        Integer id = obj.getInteger("id");
        if (goodsRepository.findAllByClassifyIdAndIsDelete(id,"1").size()>0){
            throw new ExceptionResult("该商品分类下有商品关联,请先删除关联的商品!");
        }
        GoodsClassification allById = goodsClassificationRepository.findAllByCategoryId(id);
        allById.setIsDelete("0");
        goodsClassificationRepository.save(allById);
        return new JsonResult(200);
    }
}
