// 地名, 地址匹配服务
import { cfg3D } from "@/utils/Map/config";
export function getGeocoding(address) {
    return new Promise((resolve) => {
        $.ajax({
            url: `${cfg3D.server.baseUrl}/addressmatch-DiMingDiZhiKu/restjsr/v1/address/geocoding`,
            type: "get",
            data: {
                address: address,
                filter: "陕西省, 西安市",
                fromIndex: 0,
                toIndex: 1,
                maxReturn: -1
            },
            dataType: "json", // 设置响应体的类型
            success: function (res) {
                resolve(res);
            }
        })
    })
}