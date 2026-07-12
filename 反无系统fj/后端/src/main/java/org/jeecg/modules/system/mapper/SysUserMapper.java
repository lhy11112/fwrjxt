package org.jeecg.modules.system.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.common.system.vo.TabeleTypeVo;
import org.jeecg.modules.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.vo.SysUserDepVo;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
	/**
	  * 通过用户账号查询用户信息
	 * @param username
	 * @return
	 */
	public SysUser getUserByName(@Param("username") String username);
	/**
	 * 通过用户账号查询用户信息
	 * @param username
	 * @return
	 */
	public SysUser getUserByNames(@Param("username") String username);
	@Update(" update wjbd_dxyy_pkgl\n" +
			"        set dz = replace(dz, substring_index(dz,'/',3), #{newUrl})\n" +
			"        where DXYY = 2\n" +
			"           ")
	void initPkDz(String newUrl);
	@Update(" update aw_groc_pkgl\n" +
			"        set dz = replace(dz, substring_index(dz,'/',3), #{newUrl})\n" +
			"        where DXYY = 2\n" +
			"           ")
	void initPkDzAw(String newUrl);

	@Update(" update aw_jggl_wbyy\n" +
			"        set YYDZ = replace(YYDZ, substring_index(YYDZ,'/',3), #{newUrl})\n" +
			"        where YYBS ='WWCT'\n" +
			"           ")
	void initWbYy(String newUrl);

	@Select(" SELECT COLUMN_NAME, DATA_TYPE,CASE \n" +
			"        WHEN DATA_TYPE='decimal'||DATA_TYPE='int' THEN 8\n" +
			"        ELSE CHARACTER_MAXIMUM_LENGTH\n" +
			"    END AS MAX_SIZE,TABLE_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME like 'wjbd_dxyy%' \n" +
			"or  TABLE_NAME like 'wjbd_wwct%' ")
	List<Map<String,Object>>  getTableDataType();
}
