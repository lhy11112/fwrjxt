package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import java.util.List;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.jeecg.modules.wrj.vo.WjbdWrjKysqVo;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 武警部队_无人机_空域
 * @Author: jeecg-boot
 * @Date:   2025-09-15
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_ky")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_ky对象", description="武警部队_无人机_空域")
public class WjbdWrjKy implements Serializable {
    private static final long serialVersionUID = 1L;
    /**主键*/
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
    /**空域名称（如：**A区核心禁飞区**）*/
    @Excel(name = "空域名称（如：**A区核心禁飞区**）", width = 15)
    @ApiModelProperty(value = "空域名称（如：**A区核心禁飞区**）")
    private java.lang.String mc;
    /**空域类型(限制区、禁飞区、预警区、允许飞行区域)*/
    @Excel(name = "空域类型(默认限制区)", width = 15)
    @ApiModelProperty(value = "空域类型(限制区、禁飞区、预警区、允许飞行区域)")
    private java.lang.String lx="restricted";
    /**区域形状：1-圆形、2-多边形、3-矩形*/
    @Excel(name = "区域形状：1-圆形、2-多边形、3-矩形", width = 15)
    @ApiModelProperty(value = "区域形状：1-圆形、2-多边形、3-矩形")
    private java.lang.String xz="circle";
    /**中心点经度（圆形/矩形适用）*/
    @Excel(name = "中心点经度（圆形/矩形适用）", width = 15)
    @ApiModelProperty(value = "中心点经度（圆形/矩形适用）")
    private java.math.BigDecimal zxdjd;
    /**中心点纬度（圆形/矩形适用）*/
    @Excel(name = "中心点纬度（圆形/矩形适用）", width = 15)
    @ApiModelProperty(value = "中心点纬度（圆形/矩形适用）")
    private java.math.BigDecimal zxdwd;
    /**半径（单位：米，圆形适用）*/
    @Excel(name = "半径（单位：米，圆形适用）", width = 15)
    @ApiModelProperty(value = "半径（单位：米，圆形适用）")
    private java.math.BigDecimal bj;
    /**最小高度*/
    @ApiModelProperty(value = "最小高度")
    private java.lang.Integer zxgd;
    /**最大高度*/
    @ApiModelProperty(value = "最大高度")
    private java.lang.Integer zdgd;
    /**顶点坐标集合（多边形/矩形适用，格式：经纬度1;经纬度2）*/
    @ApiModelProperty(value = "顶点坐标集合（多边形/矩形适用，格式：经纬度1;经纬度2）")
    private java.lang.String ddzbjh;
    /**长度*/
    @ApiModelProperty(value = "长度")
    private java.math.BigDecimal cd;
    /**宽度*/
    @ApiModelProperty(value = "宽度")
    private java.math.BigDecimal kd;
    /**生效开始（NULL表示永久）*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "生效开始（NULL表示永久）")
    private java.util.Date kssj;
    /**生效结束（NULL表示永久）*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "生效结束（NULL表示永久）")
    private java.util.Date jssj;
    /**是否启用*/
    @ApiModelProperty(value = "是否启用")
    private java.lang.Integer sfqy;
    /**备注*/
    @Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String bz;
    /**空域颜色*/
    @Excel(name = "空域颜色", width = 15)
    @ApiModelProperty(value = "空域颜色")
    private java.lang.String ys;
    /**创建人*/
    //@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private java.lang.String cjr;
    /**创建人ID*/
    //@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private java.lang.String cjrid;
    /**创建时间*/
   // @Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date cjsj;
    /**操作人*/
    //@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
    private java.lang.String czr;
    /**操作人ID*/
    //@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
    private java.lang.String czrid;
    /**操作时间*/
    //@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "操作时间")
    private java.util.Date czsj;
    @TableField(exist = false)
    List<WjbdWrjKysqVo> wjbdWrjKysqVoList;

    /**反制圈半径*/
    @Excel(name = "反制圈半径（单位：米）", width = 15)
    @ApiModelProperty(value = "反制圈半径（单位：米）")
    private java.math.BigDecimal jfqbj;
    @Excel(name = "反制圈颜色", width = 15)
    @ApiModelProperty(value = "反制圈颜色")
    private java.lang.String jfqys;
    /**
     * 禁戒圈半径
     */
    @Excel(name = "禁戒圈半径（单位：米）", width = 15)
    @ApiModelProperty(value = "禁戒圈半径（单位：米）")
    private java.math.BigDecimal yjqbj;
    /**
     * 禁戒圈颜色
     */
    @Excel(name = "禁戒圈颜色", width = 15)
    @ApiModelProperty(value = "禁戒圈颜色")
    private java.lang.String yjqys;
}
