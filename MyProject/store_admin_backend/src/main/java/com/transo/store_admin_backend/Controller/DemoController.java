package com.transo.store_admin_backend.Controller;

import com.alibaba.fastjson.JSONArray;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.transo.store_bean.Entity.Order;
import com.transo.store_bean.Entity.Pool;
import com.transo.store_tools.Tools.JsonResult;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
@RestController
public class DemoController {

    @Autowired
    private EntityManager entityManager;

    //查询工厂
    private JPAQueryFactory queryFactory;

    //初始化查询工厂
    @PostConstruct
    public void init() {
        queryFactory = new JPAQueryFactory(entityManager);
    }

    @GetMapping(value = "myDemo")
    public JsonResult demo(){

        return new JsonResult(200);
    }



 public static void main(String[] args){
     List<String> list = Arrays.asList("1","121","122");
     Object[] objects = list.toArray();

     String  json = "[{\"name\":\"超级管理员\",\"id\":1},{\"name\":\"测试角色\",\"id\":4}]";

     JSONArray array = JSONArray.parseArray(json);
     Object[] objects1 = array.toArray();

     System.out.println(objects1[0]);


     // System.out.println( list.stream().filter(s -> s.startsWith("1")).map(x ->x.toUpperCase()).sorted(Comparator.comparing(String::toString)).collect(Collectors.toList()));
/*   OptionalInt max = list.stream().filter(s -> s.startsWith("1")).mapToInt(x -> x.length()).max();
     System.out.println(max.getAsInt());*/
//    list.stream().filter(s -> s.startsWith("1") && s.equals("1")).map(x ->x.toUpperCase()).sorted(Comparator.comparing(String::toString)).collect(Collectors.toList());

 };





}
