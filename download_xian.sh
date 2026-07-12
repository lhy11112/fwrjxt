#!/bin/bash
# 下载西安所有层级瓦片到后端缓存
# 用法: bash download_xian.sh

CENTER_LAT=34.08
CENTER_LON=108.94
PROXY="http://localhost:8080/api/v1/tiles"
TMPDIR="/tmp/tiles_dl"
mkdir -p "$TMPDIR"

echo "西安 ($CENTER_LAT N, $CENTER_LON E)"
echo "代理: $PROXY"
echo ""

# 先测试连通性
TEST=$(curl -s -o /dev/null -w "%{http_code}" --max-time 10 "$PROXY/10/407/820" 2>/dev/null)
if [ "$TEST" != "200" ]; then
    echo "后端不通! $TEST"
    exit 1
fi
echo "后端连接正常"
echo ""

# 瓦片坐标计算函数
# lat/lon → tile x,y at zoom z
tile_x() {
    local lon=$1 z=$2
    local n=$(echo "2^$z" | bc)
    echo "scale=0; (($lon + 180) / 360 * $n) / 1" | bc
}

tile_y() {
    local lat=$1 z=$2
    local n=$(echo "2^$z" | bc)
    local rad=$(echo "scale=10; $lat * 3.1415926535 / 180" | bc)
    local tan_val=$(echo "scale=10; s($rad) / c($rad)" | bc 2>/dev/null || echo 0)
    local sec_val=$(echo "scale=10; 1 / c($rad)" | bc 2>/dev/null || echo 0)
    local inner=$(echo "scale=10; $tan_val + $sec_val" | bc 2>/dev/null || echo 0)
    local ln_val=$(echo "scale=10; l($inner) / 3.1415926535" | bc 2>/dev/null || echo 0)
    echo "scale=0; (1 - $ln_val) / 2 * $n / 1" | bc 2>/dev/null || echo 0
}

# bc 不支持三角函数，改用 awk 计算
calc_tile() {
    awk -v lat="$CENTER_LAT" -v lon="$CENTER_LON" -v z="$1" '
    BEGIN {
        n = 2 ^ z
        x = int((lon + 180) / 360 * n)
        pi = 3.141592653589793
        lat_rad = lat * pi / 180
        y = int((1 - log(tan(lat_rad) + 1 / cos(lat_rad)) / pi) / 2 * n)
        printf "%d %d", x, y
    }'
}

# 主下载循环
TOTAL=0
OK=0
FAIL=0
MAX_PARALLEL=8

download_one() {
    local z=$1 x=$2 y=$3
    local code=$(curl -s -o /dev/null -w "%{http_code}" --max-time 15 "$PROXY/$z/$y/$x" 2>/dev/null)
    if [ "$code" = "200" ]; then
        echo "OK" > "$TMPDIR/${z}_${x}_${y}"
    else
        echo "FAIL:$code" > "$TMPDIR/${z}_${x}_${y}"
    fi
}

for z in $(seq 0 18); do
    read CX CY <<< $(calc_tile $z)

    # 每级网格大小
    case $z in
        0|1|2|3) H=1 ;;
        4) H=2 ;;  5) H=3 ;;  6) H=4 ;;
        7) H=5 ;;  8) H=6 ;;  9) H=8 ;;
        10) H=10 ;; 11) H=12 ;; 12) H=15 ;;
        13) H=20 ;; 14) H=25 ;; 15) H=30 ;;
        16) H=40 ;; 17) H=50 ;; 18) H=60 ;;
    esac

    SX=$((CX - H))
    EX=$((CX + H))
    SY=$((CY - H))
    EY=$((CY + H))

    LEVEL_TILES=$(( (EX - SX + 1) * (EY - SY + 1) ))
    echo "Zoom $z: 中心($CX,$CY)  范围 x[$SX-$EX] y[$SY-$EY]  共 $LEVEL_TILES 瓦片"

    # 下载当前层级所有瓦片
    for x in $(seq $SX $EX); do
        for y in $(seq $SY $EY); do
            download_one $z $x $y &
            TOTAL=$((TOTAL + 1))

            # 控制并行数
            while [ $(jobs -r | wc -l) -ge $MAX_PARALLEL ]; do
                sleep 0.1
            done
        done
    done

    # 等待当前层级完成
    wait

    # 统计
    LEVEL_OK=$(grep -l "OK" "$TMPDIR/${z}_*" 2>/dev/null | wc -l)
    OK=$((OK + LEVEL_OK))

    echo "  → 完成, 成功: $LEVEL_OK"
    echo ""
done

echo "总共: $TOTAL 瓦片, 成功: $OK"
echo "缓存位置: data/map_tiles/"
echo "缓存文件数: $(find data/map_tiles -name '*.png' 2>/dev/null | wc -l)"
