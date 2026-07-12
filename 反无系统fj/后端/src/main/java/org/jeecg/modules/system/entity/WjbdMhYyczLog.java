package org.jeecg.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 武警部队_典型应用_操作日志
 * @Author: jeecg-boot
 * @Date:   2025-07-29
 *
 */
@Data
@TableName("wjbd_mh_yycz_log")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ApiModel(value="wjbd_mh_yycz_log对象", description="武警部队_典型应用_操作日志")
public class WjbdMhYyczLog implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "主键")
    private String id;
	/**应用名称*/
	@Excel(name = "应用名称", width = 15)
    @ApiModelProperty(value = "应用名称")
    private String yymc;
	/**应用模块（子系统/模块/配块/分析系统名称）*/
	@Excel(name = "应用模块（子系统/模块/配块/分析系统名称）", width = 15)
    @ApiModelProperty(value = "应用模块（子系统/模块/配块/分析系统名称）")
    private String yymk;
	/**日志类型（0问答、1查询、2添加、3修改、4删除、5调用、6导入、7导出）*/
	@Excel(name = "日志类型（0问答、1查询、2添加、3修改、4删除、5调用、6导入、7导出）", width = 15)
    @ApiModelProperty(value = "日志类型（0问答、1查询、2添加、3修改、4删除、5调用、6导入、7导出）")
    private Integer rzlx;
	/**日志内容*/
	@Excel(name = "日志内容", width = 15)
    @ApiModelProperty(value = "日志内容")
    private String rznr;
	/**操作用户名*/
	@Excel(name = "操作用户名", width = 15)
    @ApiModelProperty(value = "操作用户名")
    private String czyhm;
	/**操作用户姓名*/
	@Excel(name = "操作用户姓名", width = 15)
    @ApiModelProperty(value = "操作用户姓名")
    private String czyhxm;
	/**操作IP*/
	@Excel(name = "操作IP", width = 15)
    @ApiModelProperty(value = "操作IP")
    private String czip;
	/**创建人ID*/
	@Excel(name = "创建人ID", width = 15)
    @ApiModelProperty(value = "创建人ID")
    private String cjrid;
	/**创建时间*/
	@Excel(name = "创建时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "创建时间")
    private Date cjsj;
	/**操作人ID*/
	@Excel(name = "操作人ID", width = 15)
    @ApiModelProperty(value = "操作人ID")
    private String czrid;
	/**操作时间*/
	@Excel(name = "操作时间", width = 15, format = "yyyy-MM-dd")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    @ApiModelProperty(value = "操作时间")
    private Date czsj;
	/**是否删除*/
	@Excel(name = "是否删除", width = 15)
    @ApiModelProperty(value = "是否删除")
    private Integer ljsc;
}
