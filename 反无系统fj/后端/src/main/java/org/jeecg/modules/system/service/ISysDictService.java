package org.jeecg.modules.system.service;
import java.util.List;
import java.util.Map;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.modules.system.entity.SysDict;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * <p>
 * 字典表 服务类
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface ISysDictService extends IService<SysDict> {

    public List<DictModel> queryDictItemsByCode(String code);



     
    List<DictModel> queryTableDictItemsByCode(String table, String text, String code);

     
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql);

    public String queryDictTextByKey(String code, String key);

     
	String queryTableDictTextByKey(String table, String text, String code, String key);

	 
	List<String> queryTableDictByKeys(String table, String text, String code, String keys);

    /**
	/**
	 * 通过关键字查询字典表
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
	 * @return
	 */
	 
	public List<DictModel> queryTableDictItems(String table, String text, String code,String keyword);

	/**
	 * 查询字典表数据 只查询前10条
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
	 * @return
	 */
	public List<DictModel> queryLittleTableDictItems(String table, String text, String code,String keyword, int pageSize);
	/**
	 * 真实删除
	 * @param id
	 */
	public void deleteOneDictPhysically(String id);

	/**
	 * 修改delFlag
	 * @param delFlag
	 * @param id
	 */
	public void updateDictDelFlag(int delFlag,String id);

	/**
	 * 查询被逻辑删除的数据
	 * @return
	 */
	public List<SysDict> queryDeleteList();

	/**
	 * 分页查询
	 * @param query
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	 
	public List<DictModel> queryDictTablePageList(DictQuery query,int pageSize, int pageNo);

}
