package com.example.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.entity.BigType;
import com.example.entity.PageBean;
import com.example.entity.R;
import com.example.entity.SmallType;
import com.example.service.IBigTypeService;
import com.example.service.ISmallTypeService;
import com.example.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理-商品大类Controller
 */
@RestController
@RequestMapping("/admin/bigType")
public class AdminBigTypeController {

    @Autowired
    private IBigTypeService bigTypeService;

    @Autowired
    private ISmallTypeService smallTypeService;

    @Value("${bigTypeImagesFilePath}")
    private String bigTypeImagesFilePath;

    /**
     * 根据条件分页查询商品大类信息
     * @param pageBean
     * @return
     */
    @RequestMapping("/list")
    public R list(@RequestBody PageBean pageBean){
        System.out.println(pageBean);
        String query=pageBean.getQuery().trim();
        Page<BigType> page=new Page<>(pageBean.getPageNum(), pageBean.getPageSize());
        Page<BigType> pageResult = bigTypeService.page(page, new QueryWrapper<BigType>().like("name", query));
        Map<String,Object> map=new HashMap<>();
        map.put("bigTypeList", pageResult.getRecords());
        map.put("total", pageResult.getTotal());
        return R.ok(map);
    }

    /**
     * 添加或者修改
     * @param bigType
     * @return
     */
    @PostMapping("/save")
    public R save(@RequestBody BigType bigType){
        if(bigType.getId()==null || bigType.getId()==-1){
            bigTypeService.save(bigType);
        }else{
            bigTypeService.saveOrUpdate(bigType);
        }
        return R.ok();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @GetMapping("/delete/{id}")
    public R delete(@PathVariable(value = "id") Integer id){
        // 加个判断 大类下面如果有小类，返回报错提示
        if(smallTypeService.count(new QueryWrapper<SmallType>().eq("bigTypeId",id))>0){
            return R.error(500,"大类下面有小类信息，不能删除");
        }else{
            bigTypeService.removeById(id);
            return R.ok();
        }
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R findById(@PathVariable(value = "id") Integer id){
        System.out.println("id="+id);
        BigType bigType = bigTypeService.getById(id);
        Map<String, Object> map=new HashMap<>();
        map.put("bigType",bigType);
        return R.ok(map);
    }

    /**
     * 上传商品大类图片
     * @param file
     * @return
     * @throws Exception
     */
    @RequestMapping("/uploadImage")
    public Map<String, Object> uploadImage(MultipartFile file)throws Exception{
        Map<String, Object> resultMap=new HashMap<String,Object>();
        System.out.println("开始上传文件，上传路径: " + bigTypeImagesFilePath);
        System.out.println("文件是否为空: " + file.isEmpty());
        System.out.println("文件大小: " + file.getSize() + " bytes");
        
        if(!file.isEmpty()){
            try {
                // 获取文件名
                String fileName = file.getOriginalFilename();
                System.out.println("原始文件名: " + fileName);
                // 获取文件的后缀名
                String suffixName = fileName.substring(fileName.lastIndexOf("."));
                String newFileName= DateUtil.getCurrentDateStr()+suffixName;
                System.out.println("新文件名: " + newFileName);
                
                String fullPath = bigTypeImagesFilePath + newFileName;
                System.out.println("完整保存路径: " + fullPath);
                
                File targetFile = new File(fullPath);
                System.out.println("目标文件路径: " + targetFile.getAbsolutePath());
                System.out.println("父目录存在: " + targetFile.getParentFile().exists());
                System.out.println("父目录可写: " + targetFile.getParentFile().canWrite());

                FileUtils.copyInputStreamToFile(file.getInputStream(), targetFile);
                System.out.println("文件保存成功!");
                System.out.println("保存后文件存在: " + targetFile.exists());
                System.out.println("保存后文件大小: " + targetFile.length() + " bytes");
                
                resultMap.put("code", 0);
                resultMap.put("msg", "上传成功");
                Map<String, Object> dataMap = new HashMap<String, Object>();
                dataMap.put("title", newFileName);
                dataMap.put("src", "/image/bigTypeImgs/"+newFileName);
                resultMap.put("data", dataMap);
            } catch (Exception e) {
                System.err.println("文件上传失败: " + e.getMessage());
                e.printStackTrace();
                resultMap.put("code", 500);
                resultMap.put("msg", "上传失败: " + e.getMessage());
            }
        } else {
            System.out.println("文件为空，上传失败");
            resultMap.put("code", 500);
            resultMap.put("msg", "文件为空");
        }

        return resultMap;
    }

    /**
     * 查询所有数据 下拉框用到
     * @return
     */
    @RequestMapping("/listAll")
    public R listAll(){
        Map<String, Object> map=new HashMap<>();
        map.put("bigTypeList", bigTypeService.list());
        return R.ok(map);
    }

}
