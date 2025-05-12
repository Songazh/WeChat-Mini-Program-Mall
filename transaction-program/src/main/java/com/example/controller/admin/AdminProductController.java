package com.example.controller.admin;

import com.example.entity.PageBean;
import com.example.entity.Product;
import com.example.entity.R;
import com.example.service.IProductService;
import com.example.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 后台管理-商品Controller
 */
@RestController
@RequestMapping("/admin/product")
public class AdminProductController {

    @Autowired
    private IProductService productService;

    @Value("${productImagesFilePath}")
    private String productImagesFilePath;

    @Value("${swiperImagesFilePath}")
    private String swiperImagesFilePath;


    /**
     * 根据条件分页查询
     * @param pageBean
     * @return
     */
    @RequestMapping("/list")
    public R list(@RequestBody PageBean pageBean){
        System.out.println(pageBean);
        Map<String,Object> map=new HashMap<>();
        map.put("name",pageBean.getQuery().trim());
        map.put("start",pageBean.getStart());
        map.put("pageSize",pageBean.getPageSize());
        List<Product> productList = productService.list(map);
        Long total = productService.getTotal(map);

        Map<String,Object> resultMap=new HashMap<>();
        resultMap.put("productList", productList);
        resultMap.put("total",total);
        return R.ok(resultMap);
    }

    /**
     * 更新热卖状态
     * @param id
     * @param hot
     * @return
     */
    @GetMapping("/updateHot/{id}/state/{hot}")
    public R updateHot(@PathVariable(value = "id") Integer id, @PathVariable(value = "hot") boolean hot){
        Product product = productService.getById(id);
        product.setHot(hot);
        if(hot){
            product.setHotDateTime(new Date());
        }else{
            product.setHotDateTime(null);
        }
        productService.saveOrUpdate(product);
        return R.ok();
    }

    /**
     * 更新swiper状态
     * @param id
     * @param swiper
     * @return
     */
    @GetMapping("/updateSwiper/{id}/state/{swiper}")
    public R updateSwiper(@PathVariable(value = "id") Integer id,@PathVariable(value = "swiper") boolean swiper){
        Product product = productService.getById(id);
        product.setSwiper(swiper);
        productService.saveOrUpdate(product);
        return R.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public R delete(@PathVariable(value = "id") Integer id){
        productService.removeById(id);
        return R.ok();
    }

    /**
     * 上传商品大类图片
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping("/uploadImage")
    public Map<String, Object> uploadImage(MultipartFile file)throws Exception{
        Map<String, Object> resultMap = new HashMap<>();
        if(!file.isEmpty()){
            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            // 获取文件的后缀名
            String suffixName = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName= DateUtil.getCurrentDateStr()+suffixName;

            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(productImagesFilePath+newFileName));
            resultMap.put("code", 0);
            resultMap.put("msg", "上传成功");
            Map<String, Object> dataMap=new HashMap<>();
            dataMap.put("title", newFileName);
            dataMap.put("src", "/image/product/"+newFileName);
            resultMap.put("data", dataMap);
        }
        return resultMap;
    }

    /**
     * 添加或者修改
     * @param product
     * @return
     */
    @RequestMapping("/save")
    public R save(@RequestBody Product product){
        if(product.getId()==null || product.getId()==-1){ // 添加
            productService.add(product);
        }else{
            productService.update(product);
        }
        return R.ok();
    }

    /**
     * 更新图片
     * @param product
     * @return
     */
    @PostMapping("/saveImage")
    public R saveImage(@RequestBody Product product){
        Product p = productService.getById(product.getId());
        p.setProPic(product.getProPic());
        productService.saveOrUpdate(p);
        return R.ok();
    }

    /**
     * 上传swiper幻灯图片
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping("/uploadSwiperImage")
    public Map<String,Object> uploadSwiperImage(MultipartFile file)throws Exception{
        Map<String,Object> resultMap=new HashMap<>();
        if(!file.isEmpty()){
            // 获取文件名
            String fileName = file.getOriginalFilename();
            // 获取文件的后缀名
            String suffixName = fileName.substring(fileName.lastIndexOf("."));
            String newFileName=DateUtil.getCurrentDateStr()+suffixName;

            FileUtils.copyInputStreamToFile(file.getInputStream(), new File(swiperImagesFilePath+newFileName));
            resultMap.put("code", 0);
            resultMap.put("msg", "上传成功");
            Map<String,Object> dataMap=new HashMap<>();
            dataMap.put("title", newFileName);
            dataMap.put("src", "/image/swiper/"+newFileName);
            resultMap.put("data", dataMap);
        }
        return resultMap;
    }

    /**
     * 更新swiper幻灯图片信息
     * @param product
     * @return
     */
    @PostMapping("/saveSwiper")
    public R saveSwiper(@RequestBody Product product){
        Product p = productService.getById(product.getId());
        p.setSwiperPic(product.getSwiperPic());
        p.setSwiperSort(product.getSwiperSort());
        productService.saveOrUpdate(p);
        return R.ok();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R findById(@PathVariable(value = "id") Integer id){
        System.out.println("id="+id);
        Product product = productService.findById(id);
        Map<String, Object> map=new HashMap<>();
        map.put("product", product);
        return R.ok(map);
    }
}
