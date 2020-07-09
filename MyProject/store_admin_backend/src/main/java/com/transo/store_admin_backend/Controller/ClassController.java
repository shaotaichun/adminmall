package com.transo.store_admin_backend.Controller;

import com.transo.store_admin_backend.Annotations.LoginRequired;
import com.transo.store_bean.Entity.*;
import com.transo.store_repository.Repository.*;
import com.transo.store_tools.Tools.ExceptionResult;
import com.transo.store_tools.Tools.JsonResult;
import com.transo.store_tools.Tools.RowData;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
@Api(value = "字典操作IDEA提交", tags = "字典操作")
@RequestMapping("/backEndAdminClass")
public class ClassController {
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
    @Autowired
    private GoodsWayRepository goodsWayRepository;
    @PostMapping(value = "addStoreType")
    @ApiOperation(value = "添加/修改商铺类型")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addStoreType(@RequestBody @Valid @ApiParam(name = "商铺类型模型", value = "传入json格式", required = true) StoreType storeType){
        storeTypeRepository.save(storeType);
        return new JsonResult(200);
    }
    @GetMapping(value = "showStoreType")
    @ApiOperation(value = "展示商铺类型")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showStoreType(@RequestParam(value = "page")int page,@RequestParam(value = "pageSize")int pageSize){
        Pageable pageable = PageRequest.of(page-1,pageSize);
        Page<StoreType> all = storeTypeRepository.findAll(new Specification<StoreType>() {
            @Override
            public Predicate toPredicate(Root<StoreType> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("isDelete").as(String.class),"1"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        return new JsonResult(new RowData((int)all.getTotalElements(),all.getContent()), 200);
    }
    @PostMapping(value = "addAdvertisement")
    @ApiOperation(value = "添加/修改广告")
    @Transactional
    /*@LoginRequired(required = true)*/
    public JsonResult addAdvertisement(@RequestBody @Valid @ApiParam(name = "广告模型", value = "传入json格式", required = true) Advertisement advertisement){
        Integer sort = advertisement.getSort();
        if (advertisementRepository.findAllByOrderBySortDesc().size()>0&&  (sort == null||sort ==0)){//新增广告并且广告数大于0
             sort = advertisementRepository.findAllByOrderBySortDesc().get(0).getSort().intValue()+1;
        }else if (advertisementRepository.findAllByOrderBySortDesc().size() ==0){//新增第一条广告
            sort = 1;
        }
        advertisement.setSort(sort);
        advertisementRepository.save(advertisement);
        return new JsonResult(200);
    }
    @GetMapping(value = "showAdvertisement")
    @ApiOperation(value = "展示广告")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showAdvertisement(@RequestParam(value = "page")int page,@RequestParam(value = "pageSize")int pageSize){
        Pageable pageable = PageRequest.of(page-1,pageSize,Sort.by(Sort.Order.asc("sort")));
        Page<Advertisement> all = advertisementRepository.findAll(new Specification<Advertisement>() {
            @Override
            public Predicate toPredicate(Root<Advertisement> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("isDelete").as(String.class),"1"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        return new JsonResult(new RowData((int)all.getTotalElements(),all.getContent()), 200);
    }
    @PostMapping(value = "addFirstClassification")
    @ApiOperation(value = "添加/修改一级分类")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addFirstClassification(@RequestBody @Valid @ApiParam(name = "一级分类模型", value = "传入json格式", required = true)FirstClassification firstClassification){
        Integer sort = firstClassification.getSort();
        if (firstClassificationRepository.findAllByOrderBySortDesc().size()>0&& (sort == null||sort ==0)){//新增广告并且广告数大于0
            sort = firstClassificationRepository.findAllByOrderBySortDesc().get(0).getSort().intValue()+1;
        }else if (firstClassificationRepository.findAllByOrderBySortDesc().size() ==0){//新增第一条广告
            sort = 1;
        }
        firstClassification.setSort(sort);
        firstClassificationRepository.save(firstClassification);
        return new JsonResult(200);
    }
    @GetMapping(value = "showFirstClassification")
    @ApiOperation(value = "展示一、二级分类")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showFirstClassification(@RequestParam(value = "page")int page,@RequestParam(value = "pageSize")int pageSize){
        Pageable pageable = PageRequest.of(page-1,pageSize,Sort.by(Sort.Order.asc("sort")));
        Page<FirstClassification> all = firstClassificationRepository.findAll(new Specification<FirstClassification>() {
            @Override
            public Predicate toPredicate(Root<FirstClassification> root, CriteriaQuery<?> cq, CriteriaBuilder cb) {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("isDelete").as(String.class),"1"));
                return cb.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        }, pageable);
        for (FirstClassification alt:all) {
            List<SecondClassification> allByFirstClassifyId = secondClassificationRepository.findAllByFirstClassifyIdAndIsDeleteOrderBySortAsc(alt.getId(),"1");
            alt.setSecondClassifications(allByFirstClassifyId);
        }
        return new JsonResult(new RowData((int)all.getTotalElements(),all.getContent()), 200);
    }
    @PostMapping(value = "addSecondClassification")
    @ApiOperation(value = "添加/修改二级分类")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addSecondClassification(@RequestBody @Valid @ApiParam(name = "一级分类模型", value = "传入json格式", required = true) SecondClassification secondClassification){
        Integer firstClassifyId = secondClassification.getFirstClassifyId();//改二级分类所属的一级分类ID
        List<FirstClassification> all = firstClassificationRepository.findAll();
        Boolean b  = false;
        for (FirstClassification alt:all) {
            if (firstClassifyId == alt.getId()){
                b = true;
            }
        }
        if (b){
            Integer sort = secondClassification.getSort();
            if (secondClassificationRepository.findAllByOrderBySortDesc().size()>0&&  (sort == null||sort ==0)){//新增广告并且广告数大于0
                sort = secondClassificationRepository.findAllByOrderBySortDesc().get(0).getSort().intValue()+1;
            }else if (secondClassificationRepository.findAllByOrderBySortDesc().size() ==0){//新增第一条广告
                sort = 1;
            }
        secondClassification.setSort(sort);
        secondClassificationRepository.save(secondClassification);
        return new JsonResult(200);
        }else{
            throw new ExceptionResult("该二级分类所属的一级分类不存在!");
        }
    }
    @GetMapping(value = "showGoodsWay")
    @ApiOperation(value = "展示提货方式")
    @Transactional
    @ApiImplicitParams({
            @ApiImplicitParam(paramType = "param", name = "page", value = "页码", required = true, dataType = "Integer"),
            @ApiImplicitParam(paramType = "param", name = "pageSize", value = "单页显示数量", required = true, dataType = "Integer"),
    })
    @LoginRequired(required = true)
    public JsonResult showGoodsWay(@RequestParam(value = "page")int page,@RequestParam(value = "pageSize")int pageSize){
        Pageable pageable = PageRequest.of(page-1,pageSize);
        Page<GoodsWay> all = goodsWayRepository.findAll(pageable);
        return new JsonResult(new RowData((int)all.getTotalElements(),all.getContent()), 200);
    }
    @PostMapping(value = "addGoodsWay")
    @ApiOperation(value = "添加/修改提货方式")
    @Transactional
    @LoginRequired(required = true)
    public JsonResult addSecondClassification(@RequestBody @Valid @ApiParam(name = "提货方式模型", value = "传入json格式", required = true) GoodsWay goodsWay){
        goodsWayRepository.save(goodsWay);
        return new JsonResult(200);
    }

}