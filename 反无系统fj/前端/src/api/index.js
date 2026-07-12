/**
 * @description 自动import导入所有 api 模块
 */

const files = require.context('./', true, /\.js$/)
const modules = {}
files.keys().forEach((key) => {
	if(key=='./index.js'){
		return;
	}
	var key1 = key.replace(/(\.\/|\.js)/g, '');	//去除相对目录前缀和JS后缀
	let path = key1.split('/');
	let temp = modules;
	for(let item in path){
		if(item!=path.length-1){
			if(temp[path[item]]==null){
				temp[path[item]]={};
				temp = temp[path[item]];
			}
			else{
				temp = temp[path[item]];
			}
		}
		else{
			temp[path[item]] = files(key).default
		}
	}
})
export default modules