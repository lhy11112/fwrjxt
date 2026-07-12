package org.jeecg.modules.wrj.entity;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.aspect.annotation.Dict;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Description: 武警部队_无人机_基本信息管理
 * @Author: jeecg-boot
 * @Date:   2025-12-16
 * @Version: V1.0
 */
@Data
@TableName("wjbd_wrj_jbxxgl")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_wrj_jbxxgl对象", description="武警部队_无人机_基本信息管理")
public class WjbdWrjJbxxgl implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private java.lang.String id;
	/**名称(无人机)*/
	@Excel(name = "名称(无人机)", width = 15)
    @ApiModelProperty(value = "名称(无人机)")
    private java.lang.String mc;
	/**序列号(唯一识别码)*/
	@Excel(name = "序列号(唯一识别码)", width = 15)
    @ApiModelProperty(value = "序列号(唯一识别码)")
    private java.lang.String serialNumber;
	/**品牌（如大疆、极飞、彩虹）*/
	@Excel(name = "品牌（如大疆、极飞、彩虹）", width = 15)
    @ApiModelProperty(value = "品牌（如大疆、极飞、彩虹）")
    private java.lang.String brand;
	/**型号（如“Mavic 3”）*/
	@Excel(name = "型号（如“Mavic 3”）", width = 15)
    @ApiModelProperty(value = "型号（如“Mavic 3”）")
    private java.lang.String model;
	/**无人机类型（军用/民用）*/
	@Excel(name = "无人机类型（军用/民用）", width = 15)
    @ApiModelProperty(value = "无人机类型（军用/民用）")
    private java.lang.String type;
	/**地区分类（台湾、美国、韩国、中国、俄罗斯）*/
	@Excel(name = "地区分类（台湾、美国、韩国、中国、俄罗斯）", width = 15)
    @ApiModelProperty(value = "地区分类（台湾、美国、韩国、中国、俄罗斯）")
    private java.lang.String dqfl;
	/**种类分类（固定翼、六旋翼）*/
	@Excel(name = "种类分类（固定翼、六旋翼）", width = 15)
    @ApiModelProperty(value = "种类分类（固定翼、六旋翼）")
    private java.lang.String zlfl;
	/**简介*/
	@Excel(name = "简介", width = 15)
    @ApiModelProperty(value = "简介")
    private java.lang.String jj;
	/**机长*/
	@Excel(name = "机长", width = 15)
    @ApiModelProperty(value = "机长")
    private java.lang.String jc;
	/**系统操作员*/
	@Excel(name = "系统操作员", width = 15)
    @ApiModelProperty(value = "系统操作员")
    private java.lang.String xtczy;
	/**升限*/
	@Excel(name = "升限", width = 15)
    @ApiModelProperty(value = "升限")
    private java.lang.String sx;
	/**实用升限*/
	@Excel(name = "实用升限", width = 15)
    @ApiModelProperty(value = "实用升限")
    private java.lang.String sysx;
	/**最大升限*/
	@Excel(name = "最大升限", width = 15)
    @ApiModelProperty(value = "最大升限")
    private java.lang.String zdsx;
	/**有效升限*/
	@Excel(name = "有效升限", width = 15)
    @ApiModelProperty(value = "有效升限")
    private java.lang.String yxsx;
	/**机高*/
	@Excel(name = "机高", width = 15)
    @ApiModelProperty(value = "机高")
    private java.lang.String jg;
	/**作战半径*/
	@Excel(name = "作战半径", width = 15)
    @ApiModelProperty(value = "作战半径")
    private java.lang.String zzbj;
	/**控制半径*/
	@Excel(name = "控制半径", width = 15)
    @ApiModelProperty(value = "控制半径")
    private java.lang.String kzbj;
	/**侦察范围*/
	@Excel(name = "侦察范围", width = 15)
    @ApiModelProperty(value = "侦察范围")
    private java.lang.String zcfw;
	/**翼展*/
	@Excel(name = "翼展", width = 15)
    @ApiModelProperty(value = "翼展")
    private java.lang.String yz;
	/**机重、自重*/
	@Excel(name = "机重、自重", width = 15)
    @ApiModelProperty(value = "机重、自重")
    private java.lang.String jz;
	/**任务装备*/
	@Excel(name = "任务装备", width = 15)
    @ApiModelProperty(value = "任务装备")
    private java.lang.String rwzb;
	/**有效载荷*/
	@Excel(name = "有效载荷", width = 15)
    @ApiModelProperty(value = "有效载荷")
    private java.lang.String yxzh;
	/**最大速度*/
	@Excel(name = "最大速度", width = 15)
    @ApiModelProperty(value = "最大速度")
    private java.lang.String zdsd;
	/**最大起飞重量*/
	@Excel(name = "最大起飞重量", width = 15)
    @ApiModelProperty(value = "最大起飞重量")
    private java.lang.String zdqfzl;
	/**巡航速度*/
	@Excel(name = "巡航速度", width = 15)
    @ApiModelProperty(value = "巡航速度")
    private java.lang.String xhsd;
	/**巡航高度*/
	@Excel(name = "巡航高度", width = 15)
    @ApiModelProperty(value = "巡航高度")
    private java.lang.String xhgd;
	/**续航时间*/
	@Excel(name = "续航时间", width = 15)
    @ApiModelProperty(value = "续航时间")
    private java.lang.String xhsj;
	/**巡航距离*/
	@Excel(name = "巡航距离", width = 15)
    @ApiModelProperty(value = "巡航距离")
    private java.lang.String xhjl;
	/**气动布局*/
	@Excel(name = "气动布局", width = 15)
    @ApiModelProperty(value = "气动布局")
    private java.lang.String qdbj;
	/**发动机数量*/
	@Excel(name = "发动机数量", width = 15)
    @ApiModelProperty(value = "发动机数量")
    private java.lang.String fdjsl;
	/**飞行速度*/
	@Excel(name = "飞行速度", width = 15)
    @ApiModelProperty(value = "飞行速度")
    private java.lang.String fxsd;
	/**载重*/
	@Excel(name = "载重", width = 15)
    @ApiModelProperty(value = "载重")
    private java.lang.String zz;
	/**空重*/
	@Excel(name = "空重", width = 15)
    @ApiModelProperty(value = "空重")
    private java.lang.String kz;
	/**最大航程*/
	@Excel(name = "最大航程", width = 15)
    @ApiModelProperty(value = "最大航程")
    private java.lang.String zdhc;
	/**生产单位*/
	@Excel(name = "生产单位", width = 15)
    @ApiModelProperty(value = "生产单位")
    private java.lang.String scdw;
	/**企业名称*/
	@Excel(name = "企业名称", width = 15)
    @ApiModelProperty(value = "企业名称")
    private java.lang.String qymc;
	/**发动机*/
	@Excel(name = "发动机", width = 15)
    @ApiModelProperty(value = "发动机")
    private java.lang.String fdj;
	/**动力装置*/
	@Excel(name = "动力装置", width = 15)
    @ApiModelProperty(value = "动力装置")
    private java.lang.String dlzz;
	/**建造材料*/
	@Excel(name = "建造材料", width = 15)
    @ApiModelProperty(value = "建造材料")
    private java.lang.String jzcl;
	/**地面操作人员*/
	@Excel(name = "地面操作人员", width = 15)
    @ApiModelProperty(value = "地面操作人员")
    private java.lang.String dmczry;
	/**定位精度*/
	@Excel(name = "定位精度", width = 15)
    @ApiModelProperty(value = "定位精度")
    private java.lang.String dwjd;
	/**影像传输距离*/
	@Excel(name = "影像传输距离", width = 15)
    @ApiModelProperty(value = "影像传输距离")
    private java.lang.String yxcsjl;
	/**长度*/
	@Excel(name = "长度", width = 15)
    @ApiModelProperty(value = "长度")
    private java.lang.String cd;
	/**外形*/
	@Excel(name = "外形", width = 15)
    @ApiModelProperty(value = "外形")
    private java.lang.String wx;
	/**活动方式*/
	@Excel(name = "活动方式", width = 15)
    @ApiModelProperty(value = "活动方式")
    private java.lang.String hdfs;
	/**直径*/
	@Excel(name = "直径", width = 15)
    @ApiModelProperty(value = "直径")
    private java.lang.String zj;
	/**能源种类*/
	@Excel(name = "能源种类", width = 15)
    @ApiModelProperty(value = "能源种类")
    private java.lang.String nyzl;
	/**航速*/
	@Excel(name = "航速", width = 15)
    @ApiModelProperty(value = "航速")
    private java.lang.String hs;
	/**工作潜深*/
	@Excel(name = "工作潜深", width = 15)
    @ApiModelProperty(value = "工作潜深")
    private java.lang.String gzqs;
	/**自持力*/
	@Excel(name = "自持力", width = 15)
    @ApiModelProperty(value = "自持力")
    private java.lang.String zcl;
	/**航程*/
	@Excel(name = "航程", width = 15)
    @ApiModelProperty(value = "航程")
    private java.lang.String hc;
	/**搭载平台*/
	@Excel(name = "搭载平台", width = 15)
    @ApiModelProperty(value = "搭载平台")
    private java.lang.String dzpt;
	/**弹药、导弹*/
	@Excel(name = "弹药、导弹", width = 15)
    @ApiModelProperty(value = "弹药、导弹")
    private java.lang.String dy;
	/**电子设备*/
	@Excel(name = "电子设备", width = 15)
    @ApiModelProperty(value = "电子设备")
    private java.lang.String dzsb;
	/**火力打击能力*/
	@Excel(name = "火力打击能力", width = 15)
    @ApiModelProperty(value = "火力打击能力")
    private java.lang.String hldjnl;
	/**雷达及电子设备*/
	@Excel(name = "雷达及电子设备", width = 15)
    @ApiModelProperty(value = "雷达及电子设备")
    private java.lang.String ldjdzsb;
	/**特点*/
	@Excel(name = "特点", width = 15)
    @ApiModelProperty(value = "特点")
    private java.lang.String td;
	/**结构特点*/
	@Excel(name = "结构特点", width = 15)
    @ApiModelProperty(value = "结构特点")
    private java.lang.String jgtd;
	/**侦察装备、设备*/
	@Excel(name = "侦察装备、设备", width = 15)
    @ApiModelProperty(value = "侦察装备、设备")
    private java.lang.String zczb;
	/**作战能力*/
	@Excel(name = "作战能力", width = 15)
    @ApiModelProperty(value = "作战能力")
    private java.lang.String zznl;
	/**侦察监视能力、侦察能力*/
	@Excel(name = "侦察监视能力、侦察能力", width = 15)
    @ApiModelProperty(value = "侦察监视能力、侦察能力")
    private java.lang.String zcjsnl;
	/**编配及部署*/
	@Excel(name = "编配及部署", width = 15)
    @ApiModelProperty(value = "编配及部署")
    private java.lang.String bpjbs;
	/**备注*/
	@Excel(name = "备注", width = 15)
    @ApiModelProperty(value = "备注")
    private java.lang.String remark;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
    @ApiModelProperty(value = "创建人")
    private java.lang.String cjr;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private java.lang.String cjrid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private java.util.Date cjsj;
	/**操作人*/
	@Excel(name = "操作人", width = 15)
    @ApiModelProperty(value = "操作人")
    private java.lang.String czr;
	/**操作人ID*/
	@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
    private java.lang.String czrid;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "操作时间")
    private java.util.Date czsj;
	/**图片(无人机、装备图册)*/
	@Excel(name = "图片(无人机、装备图册)", width = 15)
    @ApiModelProperty(value = "图片(无人机、装备图册)")
    private java.lang.String tp;
	/**三维模型地址*/
	@Excel(name = "三维模型地址", width = 15)
    @ApiModelProperty(value = "三维模型地址")
    private java.lang.String mxdz;
    @Excel(name = "最大巡航速度", width = 15)
    @ApiModelProperty(value = "最大巡航速度")
    private java.lang.String zdxhsd;
    @Excel(name = "巡航时间", width = 15)
    @ApiModelProperty(value = "巡航时间")
    private java.lang.String wrjxhsj;
    @Excel(name = "探测能力", width = 15)
    @ApiModelProperty(value = "探测能力")
    private java.lang.String tcnl;
}
