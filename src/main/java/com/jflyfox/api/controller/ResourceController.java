package com.jflyfox.api.controller;

import com.jfinal.plugin.activerecord.Page;
import com.jflyfox.api.form.ApiForm;
import com.jflyfox.api.form.ApiResp;
import com.jflyfox.api.model.IndexTheme;
import com.jflyfox.api.service.ApiService;
import com.jflyfox.api.service.impl.ApiV100Logic;
import com.jflyfox.component.base.BaseProjectController;
import com.jflyfox.jfinal.component.annotation.ControllerBind;
import com.jflyfox.jfinal.component.db.SQLUtils;
import com.jflyfox.modules.admin.article.TbArticle;
import com.jflyfox.modules.admin.folder.TbFolder;
import com.jflyfox.modules.admin.image.model.TbImage;
import com.jflyfox.modules.admin.site.TbSite;
import com.jflyfox.system.config.ConfigCache;
import com.jflyfox.system.dict.SysDictDetail;
import com.jflyfox.util.StrUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerBind(controllerKey = "/api/resource")
public class ResourceController extends BaseProjectController {
    ApiService service = new ApiService();

    ApiV100Logic apiV100Logic = new ApiV100Logic();

    public void list() {
        getResponse().addHeader("Access-Control-Allow-Origin", "*");
        List<String> list =  ApiService.methodList();
        renderJson(list);
    }

    //查询所有栏目
    public void columnInfoAll(){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");
        int siteId;
        String qsng_site_template_name_key="qsng_site_template_name";
       String qsng_site_template_name_value=  ConfigCache.getValue(qsng_site_template_name_key);
        List<TbSite> list =  TbSite.dao.findByWhere("where template=?",qsng_site_template_name_value);
        if(CollectionUtils.isNotEmpty(list)){
            siteId=list.get(0).getId();
        }else{
            siteId=10;
        }
        ApiForm form = new ApiForm();
        String p="{siteId:"+siteId+"}";
        form.setP(p);
        ApiResp resp= apiV100Logic.folders(form);
        renderJson(resp);
    }

    //查询该栏目下的文章
    public void columnInfoDetail(){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");

        ApiForm form = new ApiForm();
        int folderId = getParaToInt("folderId");
        TbArticle model = getModelByAttr(TbArticle.class);

        SQLUtils sql = new SQLUtils(" from tb_article t " //
                + " left join tb_folder f on f.id = t.folder_id " //
                + " where 1 = 1 ");
            sql.setAlias("t");
            sql.whereEquals("folder_id", folderId);
            sql.whereEquals("status", 1);

        /*// 站点设置
        int siteId = getSessionUser().getBackSiteId();
        sql.append(" and site_id = " + siteId);*/

        // 排序
        String orderBy = getBaseForm().getOrderBy();
        if (StrUtils.isEmpty(orderBy)) {
            sql.append(" order by t.folder_id,t.sort,t.create_time desc ");
        } else {
            sql.append(" order by t.").append(orderBy);
        }

        Page<TbArticle> page = TbArticle.dao.paginate(getPaginator(), "select t.*,f.name as folderName ", //
                sql.toString().toString());
        ApiResp resp= new ApiResp(form);
        Map map  = new HashMap();
        map.put("page",page);
        TbFolder tbFolder = TbFolder.dao.findById(folderId);
        map.put("tbFolder",tbFolder);
        resp.setData(map);
        renderJson(resp);
    }

    //查询该栏目信息
    public void columnInfo(){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");

        String id=getPara("folderId");
        ApiForm from = new ApiForm();
        ApiResp resp = new ApiResp(from);
        if(StringUtils.isEmpty(id)){
            resp.setCode(-2);
            resp.setMsg("folderId不能为null");
            renderJson(resp);
            return;
        }
        int id_value=0;
        try{
            id_value= Integer.parseInt(id);
        }catch (Exception e){
            resp.setCode(-2);
            resp.setMsg("folderId不是不正确");
            renderJson(resp);
            return;
        }

        TbFolder model = TbFolder.dao.findById(id_value);
        Map map = new HashMap();
        if(null!=model){
            map.put("model",model);
            resp.setData(map);
        }else {
            resp.setCode(-1);
            resp.setMsg("没有此栏目");
        }
        renderJson(resp);
    }
    //通过id获取编号
    public void getMaterialTypeCode(){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");

        int id=0;
        try {
             id =getParaToInt("id");
        }catch (Exception e){

        }
        ApiForm from = new ApiForm();
        ApiResp resp = new ApiResp(from);
        SysDictDetail sd =  getDictDetail(id);
        if(null==sd){
            resp.setCode(-1);
            resp.setMsg("查不出字典");
            renderJson(resp);
            return;
        }
        String code= sd.getStr("detail_code");
        Map map =new HashMap();
        if(StringUtils.isNotEmpty(code)){
            map.put("code",code);
            resp.setData(map);
        }else{
            resp.setCode(-1);
            resp.setMsg("字段为null");
        }
        renderJson(resp);
    }

    //获取数据字典
    public SysDictDetail getDictDetail(int id){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");

        StringBuffer sql = new StringBuffer("select t.*,d.dict_name from sys_dict_detail t,sys_dict d where t.dict_type = d.dict_type ");
            sql.append(" AND  t.detail_id = '").append(id).append("'");
        List<SysDictDetail> list= SysDictDetail.dao.find(sql.toString().toString());
        if(CollectionUtils.isNotEmpty(list)){
            return list.get(0);
        }
        return null;

    }

    //获取首页主题
    public void index_theme_config(){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");
        IndexTheme it  = new IndexTheme();
        //首页图片相册系统配置参数
        String index_image_album =IndexThemeUtil.index_image_album;
        String index_image_album_value = ConfigCache.getValue("index_image_album");
        //footer背景图
        String footer_image =IndexThemeUtil.footer_image;
        String footer_image_value = ConfigCache.getValue("footer_image");
        //导航图片
        String banner_image =IndexThemeUtil.banner_image;
        String banner_image_value = ConfigCache.getValue("banner_image");
        //字体颜色
        String 	font_color =IndexThemeUtil.	font_color;
        String 	font_color_value = ConfigCache.getValue("font_color");
        //首页背景图
        String bg_image =IndexThemeUtil.bg_image;
        String bg_image_value = ConfigCache.getValue("bg_image");
        //首页背景颜色
        String bg_color =IndexThemeUtil.bg_color;
        String bg_color_value = ConfigCache.getValue("bg_color");

        //新增其他配置

        //logo图片
          String logo_image_url=IndexThemeUtil.logo_image_url;
        String logo_image_url_value=ConfigCache.getValue(logo_image_url);
        it.setLogo_image_url(logo_image_url_value);
        //首页左上角图片
        String index_left_top_image_url=IndexThemeUtil.index_left_top_image_url;
        String index_left_top_image_url_value=ConfigCache.getValue(index_left_top_image_url);
        it.setIndex_left_top_image_url(index_left_top_image_url_value);
        //首页右上角图片
         String index_right_top_image_url=IndexThemeUtil.index_right_top_image_url;
        String index_right_top_image_url_value=ConfigCache.getValue(index_right_top_image_url);
        it.setIndex_right_top_image_url(index_right_top_image_url_value);
        //二维码图片
       String code_2_image_url=IndexThemeUtil.code_2_image_url;
        String code_2_image_url_value=ConfigCache.getValue(code_2_image_url);
        it.setCode_2_image_url(code_2_image_url_value);
        //版权与支持
        String copyright_support_image_url=IndexThemeUtil.copyright_support_image_url;
        String copyright_support_image_url_value=ConfigCache.getValue(copyright_support_image_url);
        it.setCopyright_support_image_url(copyright_support_image_url_value);

        //文字选择背景图片
        String word_bg_color=IndexThemeUtil.word_bg_color;
        String word_bg_color_value=ConfigCache.getValue(word_bg_color);
        it.setWord_bg_color(word_bg_color_value);

        //左下角
        String index_left_bottom_url=IndexThemeUtil.index_left_bottom_url;
        String index_left_bottom_urlvalue=ConfigCache.getValue(index_left_bottom_url);
        it.setIndex_left_bottom_url(index_left_bottom_urlvalue);

        it.setBg_color(bg_color_value);
        //如果没有，使用默认的,默认的如果没设置本域名的，就是用网路路径
        if(StringUtils.isNotEmpty(bg_image_value)){
            bg_image_value=  bg_image_value.trim();
            it.setBg_image(bg_image_value);
        }else{
            TbImage ti= getImage(index_image_album_value,"bg_image");
            if(StringUtils.isNotEmpty(ti.getImageUrl())){
                it.setBg_image(ti.getImageUrl());
            }else if(StringUtils.isNotEmpty(ti.getImageNetUrl())){
                it.setBg_image(ti.getImageNetUrl());
            }

        }
        it.setFont_color(font_color_value);
        if(StringUtils.isNotEmpty(footer_image_value)){
            footer_image_value=  footer_image_value.trim();
            it.setFooter_image(footer_image_value);
        }else{
            TbImage ti= getImage(index_image_album_value,"footer_image");
            if(StringUtils.isNotEmpty(ti.getImageUrl())){
                it.setFooter_image(ti.getImageUrl());
            }else if (StringUtils.isNotEmpty(ti.getImageNetUrl())){
                it.setFooter_image(ti.getImageNetUrl());
            }
        }
        if(StringUtils.isNotEmpty(banner_image_value)){
            banner_image_value=  banner_image_value.trim();
            it.setBanner_image(banner_image_value);
        }else{
            TbImage ti= getImage(index_image_album_value,"banner_image");
            if(StringUtils.isNotEmpty(ti.getImageUrl())){
                it.setBanner_image(ti.getImageUrl());
            }else if(StringUtils.isNotEmpty(ti.getImageNetUrl())){
                it.setBanner_image(ti.getImageNetUrl());
            }
        }
       renderJson(it);
    }
    //根据相册id与图片名称获取图片
    public TbImage getImage(String albumId,String imagesName){
        if(StringUtils.isEmpty(albumId) || StringUtils.isEmpty(imagesName) ){
            return null;
        }
        SQLUtils sql = new SQLUtils(" from tb_image t where 1=1 ");
            sql.setAlias("t");
            sql.whereEquals("album_id", albumId);
            sql.whereLike("name", imagesName);
            sql.whereEquals("status", 1);
       List<TbImage> list= TbImage.dao.find(sql.toString().toString());
       if(CollectionUtils.isNotEmpty(list)){
           return list.get(0);
       }else {
           return null;
       }
    }

    //根据文章id查询文章
    public void getArticle(){
        getResponse().addHeader("Access-Control-Allow-Origin", "*");

        String id= getPara("id");
        ApiForm from = new ApiForm();
        ApiResp resp = new ApiResp(from);
        if(StringUtils.isEmpty(id)){
            resp.setCode(-2);
            resp.setMsg("参数不能为null");
            renderJson(resp);
            return;
        }
        SQLUtils sql = new SQLUtils("select t.*,f.name as folderName from tb_article t " //
                + " left join tb_folder f on f.id = t.folder_id " //
                + " where 1 = 1 ");
        sql.whereEquals("t.id", id);
        List<TbArticle> list = TbArticle.dao.find(sql.toString().toString());

        Map map = new HashMap();
        if(CollectionUtils.isNotEmpty(list)){
            TbArticle ta= list.get(0);
            map.put("article",ta);
            resp.setData(map);
        }else{
            resp.setCode(-1);
            resp.setMsg("没有查询到数据");
        }
        renderJson(resp);


    }
}
