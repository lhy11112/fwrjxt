package org.jeecg.modules.system.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.system.entity.SysDict;
import org.jeecg.modules.system.model.DuplicateCheckVo;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface SysDictMapper extends BaseMapper<SysDict> {
	
	/**
	  *  重复检查SQL
	 * @return
	 */
	@DS("wj-dxyy-zjk-datasource")
	@Select("<script>   SELECT COUNT(*) FROM ${tableName} WHERE ${fieldName} = #{fieldVal} and ${fieldZj} &lt;&gt; #{dataId} </script> ")
	public Long duplicateCheckCountSql(DuplicateCheckVo duplicateCheckVo);
	@DS("wj-dxyy-zjk-datasource")
	@Select("<script>  SELECT COUNT(*) FROM ${tableName} WHERE ${fieldName} = #{fieldVal}   </script> ")
	public Long duplicateCheckCountSqlNoDataId(DuplicateCheckVo duplicateCheckVo);
	/**
	 *  重复检查SQL
	 * @return
	 */
@Select("<script> SELECT COUNT(*) FROM ${tableName} WHERE ${fieldName} = #{fieldVal} and ${fieldZj} &lt;&gt; #{dataId}  </script>")
	public Long duplicateCheckCountSqls(DuplicateCheckVo duplicateCheckVo);

    @Select("<script> SELECT COUNT(*) FROM ${tableName} WHERE ${fieldName} = #{fieldVal}  </script>")
	public Long duplicateCheckCountSqlNoDataIds(DuplicateCheckVo duplicateCheckVo);
	
	public List<DictModel> queryDictItemsByCode(@Param("code") String code);

	 @Select(" <script>  select ${text} as \"text\",${code} as \"value\" from ${table}  </script> ")
	public List<DictModel> queryTableDictItemsByCode(@Param("table") String table,@Param("text") String text,@Param("code") String code);

	 @Select(" <script>  \t\t   select ${text} as \"text\",${code} as \"value\" from ${table}\n" +
			 "\t\t<if test=\"filterSql != null and filterSql != ''\">\n" +
			 "\t\t\twhere ${filterSql}\n" +
			 "\t\t</if> </script> ")
	public List<DictModel> queryTableDictItemsByCodeAndFilter(@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("filterSql") String filterSql);

	 
	@Select(" <script>  select ${key} as \"label\",${value} as \"value\" from ${table}  </script>")
	public List<Map<String,String>> getDictByTableNgAlain(@Param("table") String table, @Param("key") String key, @Param("value") String value);

	public String queryDictTextByKey(@Param("code") String code,@Param("key") String key);

	 @Select(" <script>     select ${text} as \"text\" from ${table} where ${code}= #{key}  </script> ")
	public String queryTableDictTextByKey(@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("key") String key);

	@Select(" <script>  \t\tselect ${text} as \"text\", ${code} as \"value\" from ${table} where ${code} in\n" +
			"\t\t<foreach item=\"key\" collection=\"keyArray\" open=\"(\" separator=\",\" close=\")\">\n" +
			"\t\t\t#{key}\n" +
			"\t\t</foreach>  </script> ")
	public List<DictModel> queryTableDictByKeys(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("keyArray") String[] keyArray);

	/**
	 * 查询所有部门 作为字典信息 id -->value,departName -->text
	 * @return
	 */
	public List<DictModel> queryAllDepartBackDictModel();
	
	/**
	 * 查询所有用户  作为字典信息 username -->value,realname -->text
	 * @return
	 */
	public List<DictModel> queryAllUserBackDictModel();
	
	/**
	 * 通过关键字查询出字典表
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
	 * @return
	 */
	 @Select("<script> select ${text} as \"text\",${code} as \"value\" from ${table} where ${text} like #{keyword} </script>")
	public List<DictModel> queryTableDictItems(@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("keyword") String keyword);

	/**
	 * 分页查询字典表数据
	 * @param page
	 * @param query
	 * @return
	 */
	 @Select("<script>  \t\tselect ${query.text} as \"text\",${query.code} as \"value\" from ${query.table}\n" +
			 "\t\twhere 1 = 1\n" +
			 "\t\t<if test=\"query.keyword != null and query.keyword != ''\">\n" +
			 "\t\t\tand (${query.text} like '%${query.keyword}%' or ${query.code} like '%${query.keyword}%')\n" +
			 "\t\t</if>\n" +
			 "\t\t<if test=\"query.codeValue != null and query.codeValue != ''\">\n" +
			 "\t\t\tand ${query.code} = #{query.codeValue}\n" +
			 "\t\t</if> </script>")
	public Page<DictModel> queryDictTablePageList(Page page, @Param("query") DictQuery query);
}
